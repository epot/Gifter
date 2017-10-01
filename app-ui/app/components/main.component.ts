import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';

import { UserService } from '../services/user.service';
import { EventsService } from '../services/events.service';
import { TokenUser } from '../token-user';

@Component({
  selector: 'my-main',
  template: require('./main.component.html')
})
export class MainComponent implements OnInit, OnDestroy {
  public user: TokenUser;
  private _userSubscription: Subscription;
  public events: Object[];
  public loadingEvents;
  public error: any;

  constructor(
    private userService: UserService,
    private eventsService: EventsService
  ) {
  }

  ngOnInit() {
    this.getEvents();
    this.user = this.userService.getUser();
    this._userSubscription = this.userService.userChanged$.subscribe(
      user => {
        this.user = user;
    });
  }

  getEvents(): void {
    this.loadingEvents = true;
    this.eventsService
        .getEvents()
        .then(events => {
            this.loadingEvents = false;
            this.events = events;
          }
        ).catch(err => {
            this.loadingEvents = false;
            this.error = err;
        });
  }

  ngOnDestroy() {
    // prevent memory leak when component destroyed
    this._userSubscription.unsubscribe();
  }
}
