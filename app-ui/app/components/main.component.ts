import { Component, Input, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { UserService } from '../services/user.service';
import { EventsService } from '../services/events.service';
import { TokenUser } from '../token-user';
import { ErrorHandleService } from '../services/error-handle.service';

@Component({
  selector: 'my-main',
  templateUrl: 'templates/main.component.html'
})
export class MainComponent implements OnInit, OnDestroy {
  @ViewChild('deleteEventModal') deleteEventModal;
  public user: TokenUser;
  private _userSubscription: Subscription;
  public events: Object[];
  public loadingEvents;
  public error: any;
  public deleteEventId: number;

  constructor(
    private userService: UserService,
    private eventsService: EventsService,
    private eh: ErrorHandleService,
    private modalService: NgbModal
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
    this.modalService.open(this.deleteEventModal).result.then((result) => {
      this.deleteEvent();
    }, (reason) => {
      return;
    });
  }

  deleteEvent() {
    this.eventsService.deleteEvent(this.deleteEventId).then(_ => {
      this.events = this.events.filter(obj => obj['id'] !== this.deleteEventId);
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
