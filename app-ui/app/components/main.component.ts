import { Component, Input, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { ModalComponent } from 'ng2-bs3-modal/ng2-bs3-modal';

import { UserService } from '../services/user.service';
import { EventsService } from '../services/events.service';
import { TokenUser } from '../token-user';
import { ErrorHandleService } from '../services/error-handle.service'

@Component({
  selector: 'my-main',
  template: require('./main.component.html')
})
export class MainComponent implements OnInit, OnDestroy {
  @ViewChild('deleteEventModal') deleteEventModal: ModalComponent;
  public user: TokenUser;
  private _userSubscription: Subscription;
  public events: Object[];
  public loadingEvents;
  public error: any;
  public deleteEventId: number;

  constructor(
    private userService: UserService,
    private eventsService: EventsService,
    private eh: ErrorHandleService
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

  openDeleteEventModal(eventid: number) {
    this.deleteEventId = eventid;
    this.deleteEventModal.open();
  }

  deleteEvent() {
    this.eventsService.deleteEvent(this.deleteEventId).then(_ => {
      this.events = this.events.filter(obj => obj['id'] !== this.deleteEventId);
      this.deleteEventModal.close();
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
