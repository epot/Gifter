import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, ParamMap } from '@angular/router';
import * as $ from 'jquery';

import { UserService } from '../services/user.service';
import { EventsService } from '../services/events.service';
import { TokenUser } from '../token-user';

@Component({
  selector: 'my-gift-view',
  template: require('./gift-view.component.html')
})
export class GiftViewComponent implements OnInit, OnDestroy {
  public user: TokenUser;
  private _userSubscription: Subscription;
  public gift: Object;
  public history: Object[];
  public loadingGift;
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

    this.loadingGift = true;
    this.route.paramMap
      .switchMap((params: ParamMap) => {
          return this.eventsService.getGiftWithDetails(+params.get('giftid'));
        }
      )
      .subscribe(response => {
        this.loadingGift = false;
        this.gift = response['gift'];
        this.history = response['history'];
      },
      err => {
        this.error = err;
      });
  }

  ngOnDestroy() {
    // prevent memory leak when component destroyed
    this._userSubscription.unsubscribe();
  }
}
