import { Component, Input, OnInit, OnDestroy, AfterViewInit, ViewChild } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { FormGroup, FormControl, Validators, FormBuilder } from '@angular/forms';
import { ModalComponent } from 'ng2-bs3-modal/ng2-bs3-modal';
import * as $ from 'jquery';

import { UserService } from '../services/user.service';
import { EventsService } from '../services/events.service';
import { TokenUser } from '../token-user';
import { ErrorHandleService } from '../services/error-handle.service'
import { FormHelperService } from '../services/form-helper.service';

@Component({
  selector: 'my-event',
  template: require('./event.component.html')
})
export class EventComponent implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild('buyGiftModal') buyGiftModal: ModalComponent;
  @ViewChild('deleteGiftModal') deleteGiftModal: ModalComponent;
  public user: TokenUser;
  private _userSubscription: Subscription;
  public event: Object;
  public gifts: Object[];
  public hasComments: Map<string, boolean> = new Map<string, boolean>();
  public participants: Object[];
  public loadingEvent;
  public error: any;
  public giftToBuy: Object;
  public giftToBuyNewStatus: string;
  addParticipantForm: FormGroup;
  deleteGiftId: number;

  constructor(
    private userService: UserService,
    private eventsService: EventsService,
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
        this.participants = response['participants'];
      },
      err => {
        this.error = err;
      });
  }

  ngAfterViewInit() {
    ($('[data-toggle="tooltip"]') as any).tooltip();
  }

  openBuyGiftModal(gift: Object) {
    this.giftToBuy = gift;
    this.giftToBuyNewStatus = gift['status'];
    this.buyGiftModal.open();
  }

  buyGift() {
    this.eventsService.updateGiftStatus(this.giftToBuy['id'], this.giftToBuyNewStatus).then(response => {
      this.buyGiftModal.close();
      const updateItem = this.gifts.find(x => x['id'] === this.giftToBuy['id']);
      const index = this.gifts.indexOf(updateItem);
      this.gifts[index] = response;
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
    console.log('coin ' + formData['email']);
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

  ngOnDestroy() {
    // prevent memory leak when component destroyed
    this._userSubscription.unsubscribe();
  }
}
