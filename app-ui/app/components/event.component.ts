import { Component, Input, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, ParamMap } from '@angular/router';
import * as $ from 'jquery';

import { UserService } from '../services/user.service';
import { EventsService } from '../services/events.service';
import { TokenUser } from '../token-user';

@Component({
  selector: 'my-event',
  template: require('./event.component.html')
})
export class EventComponent implements OnInit, OnDestroy, AfterViewInit {
  public user: TokenUser;
  private _userSubscription: Subscription;
  public event: Object;
  public gifts: Object[];
  public hasComments: Map<string, boolean> = new Map<string, boolean>();
  public participants: Object[];
  public loadingEvent;
  public error: any;

  constructor(
    private userService: UserService,
    private eventsService: EventsService,
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

  ngOnDestroy() {
    // prevent memory leak when component destroyed
    this._userSubscription.unsubscribe();
  }
}
