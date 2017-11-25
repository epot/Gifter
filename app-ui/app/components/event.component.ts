import { Component, Input, OnInit, OnDestroy, AfterViewInit, ViewChild } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { FormGroup, FormControl, Validators, FormBuilder } from '@angular/forms';
import { ModalComponent } from 'ng2-bs3-modal/ng2-bs3-modal';

import { UserService } from '../services/user.service';
import { EventsService } from '../services/events.service';
import { WSService } from '../services/ws.service';
import { TokenUser } from '../token-user';
import { ErrorHandleService } from '../services/error-handle.service'
import { FormHelperService } from '../services/form-helper.service';

@Component({
  selector: 'my-event',
  templateUrl: 'templates/event.component.html'
})
export class EventComponent implements OnInit, OnDestroy {
  @ViewChild('buyGiftModal') buyGiftModal: ModalComponent;
  @ViewChild('deleteGiftModal') deleteGiftModal: ModalComponent;
  @ViewChild('commentGiftModal') commentGiftModal: ModalComponent;
  public user: TokenUser;
  private _userSubscription: Subscription;
  public event: Object;
  public gifts: Object[];
  public hasComments: Map<string, boolean> = new Map<string, boolean>();
  public participants: Object[];
  public comments: Object[];
  public giftToComment: Object;
  public loadingEvent;
  public error: any;
  public giftToBuy: Object;
  public giftToBuyNewStatus: string;
  addParticipantForm: FormGroup;
  deleteGiftId: number;
  currentComment: string;
  recipients: any[];
  visible = true;

  constructor(
    private userService: UserService,
    private eventsService: EventsService,
    private wsService: WSService,
    private eh: ErrorHandleService,
    private fb: FormBuilder,
    public fh: FormHelperService,
    private route: ActivatedRoute
  ) {
  }

  ngOnInit() {
    this.user = this.userService.getUser();
    this._userSubscription = this.userService.userChanged$.subscribe(
      user => {
        this.user = user;
    });

    this.loadingEvent = true;
    this.route.paramMap
      .switchMap((params: ParamMap) => {
          return this.eventsService.getEventWithDetails(+params.get('id'));
        }
      )
      .subscribe(response => {
        this.addParticipantForm = this.fb.group({
          'email': new FormControl('', [Validators.required])
        });

        this.loadingEvent = false;
        this.event = response['event'];
        this.gifts = response['gifts'].map(item => item.gift);
        for (const elt of response['gifts']) {
          this.hasComments[elt.gift.id] = elt.hasCommentNotification;
        }
        this.recipients = [];
        this.recipients.push({label: 'Everyone', value: null});
        const uniqueRecipients = new Set();
        for (const g of this.gifts) {
          if (g['to']) {
            uniqueRecipients.add(g['to']['userName']);
          }
        }
        for (const r of uniqueRecipients) {
          this.recipients.push({label: r, value: r});
        }

        this.participants = response['participants'];
        this.wsService.connectEventWS(location, +this.event['id']).subscribe(
          value => {
            const json = JSON.parse(value.data.toString());
            if (json['comment'] && this.user && json['user']['id'] !== this.user['id']) {
              if (json['comment']['category'] === 'Gift') {
                this.hasComments[json['comment']['objectid']] = true;
              }
            } else if (json['gift']) {
              this.updateGift(json['gift']);
            }
          },
          error =>  {
            console.log('Error ws: ' + error);
          },
          () =>  {
            console.log('Completed ws');
          }
      );

      },
      err => {
        this.error = err;
      });
  }

  // ugly hack https://stackoverflow.com/questions/40077150/how-to-programmaticaly-trigger-refresh-primeng-datatable-when-a-button-is-clicke
  updateTable(): void {
    this.visible = false;
    setTimeout(() => this.visible = true, 0);
  }

  openBuyGiftModal(gift: Object) {
    if (!this.canBuyGift(gift)) {
      return;
    }
    this.giftToBuy = gift;
    this.giftToBuyNewStatus = gift['status'];
    this.buyGiftModal.open();
  }

  updateGift(gift: Object) {
    const id = gift['id'];
    const updateItem = this.gifts.find(x => x['id'] === id);
    if (updateItem) {
      const index = this.gifts.indexOf(updateItem);
      this.gifts[index] = gift;
    } else {
      this.gifts.push(gift);
      this.hasComments[id] = false;
    }
    this.updateTable();
  }

  buyGift() {
    this.eventsService.updateGiftStatus(this.giftToBuy['id'], this.giftToBuyNewStatus).then(response => {
      this.buyGiftModal.close();
      this.updateGift(response);
    }
  ).catch(err => {
      this.eh.handleError(err);
    });
  }

  canBuyGift(gift: Object) {
    return gift['to']['id'] !== this.user['id'] &&
      (!gift['from'] || gift['from']['id'] === this.user['id']);
   }

  addParticipant(formData: any) {
    this.eventsService.addParticipant(this.event['id'], {
      email: formData['email'],
      role: 'Owner' // hardcoded for now
    })
    .then(response => {
      this.participants.push(response);
    }
    ).catch(err => {
      this.eh.handleError(err);
    });
  }

  openDeleteGiftModal(id: number) {
    this.deleteGiftId = id;
    this.deleteGiftModal.open();
  }

  deleteGift() {
    this.eventsService.deleteGift(this.event['id'], this.deleteGiftId).then(_ => {
      this.gifts = this.gifts.filter(obj => obj['id'] !== this.deleteGiftId);
      this.deleteGiftModal.close();
    }
    ).catch(err => {
      this.eh.handleError(err);
    });
  }

  openCommentGiftModal(gift: Object) {
    this.giftToComment = gift;
    this.currentComment = '';
    this.hasComments[gift['id']] = false;
    this.commentGiftModal.open();
    this.eventsService.getGiftComments(this.event['id'], gift['id']).then(response => {
      this.comments = response['comments'];
    }
    ).catch(err => {
      this.eh.handleError(err);
    });
  }

  addComment() {
    this.eventsService.addGiftComments(this.event['id'], this.giftToComment['id'], this.currentComment).then(response => {
      this.currentComment = '';
      this.comments = response['comments'];
    }
    ).catch(err => {
      this.eh.handleError(err);
    });
  }

  ngOnDestroy() {
    // prevent memory leak when component destroyed
    this._userSubscription.unsubscribe();
  }
}
