import { Component, Input, OnInit, OnDestroy, AfterViewInit, ViewChild } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { ModalComponent } from 'ng2-bs3-modal/ng2-bs3-modal';
import * as $ from 'jquery';

import { UserService } from '../services/user.service';
import { EventsService } from '../services/events.service';
import { TokenUser } from '../token-user';
import { ErrorHandleService } from '../services/error-handle.service'

@Component({
  selector: 'my-event',
  template: require('./event.component.html')
})
export class EventComponent implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild('changeGiftModal') changeGiftModal: ModalComponent;
  public user: TokenUser;
  private _userSubscription: Subscription;
  public event: Object;
  public gifts: Object[];
  public hasComments: Map<string, boolean> = new Map<string, boolean>();
  public participants: Object[];
  public loadingEvent;
  public error: any;
  public giftChange: Object;
  public giftChangeNewStatus: string;

  constructor(
    private userService: UserService,
    private eventsService: EventsService,
    private eh: ErrorHandleService,
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

  openChangeGiftModal(gift: Object) {
    this.giftChange = gift;
    this.giftChangeNewStatus = gift['status'];
    this.changeGiftModal.open();
  }

  changeGift() {
    this.eventsService.updateGiftStatus(this.giftChange['id'], this.giftChangeNewStatus).then(response => {
      this.changeGiftModal.close();
      const updateItem = this.gifts.find(x => x['id'] === this.giftChange['id']);
      const index = this.gifts.indexOf(updateItem);
      this.gifts[index] = response;
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
