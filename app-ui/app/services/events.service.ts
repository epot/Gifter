import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'ng2-ui-auth';
import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';
import { IntervalObservable } from 'rxjs/observable/IntervalObservable';
import 'rxjs/add/operator/toPromise';

import { TokenUser } from '../token-user';

@Injectable()
export class EventsService {
  public user: TokenUser;
  expiration: Date;
  secret: Observable<Object>;
  userChangedSource = new Subject<TokenUser>();
  userChanged$ = this.userChangedSource.asObservable();

  private headers = new Headers({'Content-Type': 'application/json'});

  constructor(
    private auth: AuthService,
    private router: Router,
    private http: HttpClient) {
  }

  getEvents(): Promise<Object[]> {
    return this.http.get('/api/events')
      .toPromise()
      .then(response => {
          return response['events'];
        })
      .catch(this.handleError);
  }

  deleteEvent(id: number): Promise<Object> {
    return this.http.delete('/api/events/' + id)
      .toPromise()
      .then(response => {
          return response;
        })
      .catch(this.handleError);
  }
  
  getGiftComments(eventid: number, giftid: number): Promise<Object> {
    return this.http.get('/api/events/' + eventid + '/gifts/' + giftid + '/comments')
      .toPromise()
      .then(response => {
          return response;
        })
      .catch(this.handleError);
  }

  addGiftComments(eventid: number, giftid: number, comment: string): Promise<Object> {
    return this.http.post('/api/events/' + eventid + '/gifts/' + giftid + '/comments', {comment: comment})
      .toPromise()
      .then(response => {
          return response;
        })
      .catch(this.handleError);
  }

  deleteGift(eventid: number, giftid: number): Promise<Object> {
    return this.http.delete('/api/events/' + eventid + '/gifts/' + giftid)
      .toPromise()
      .then(response => {
          return response;
        })
      .catch(this.handleError);
  }

  getEventWithDetails(id: number): Promise<Object> {
    return this.http.get('/api/events/' + id + '/details')
      .toPromise()
      .then(response => {
          return response;
        })
      .catch(this.handleError);
  }

  getEventParticipants(id: number): Promise<Object> {
    return this.http.get('/api/events/' + id + '/participants')
      .toPromise()
      .then(response => {
          return response;
        })
      .catch(this.handleError);
  }

  getGift(id: number): Promise<Object> {
    return this.http.get('/api/gifts/' + id)
      .toPromise()
      .then(response => {
          return response;
        })
      .catch(this.handleError);
  }
  getGiftWithDetails(id: number): Promise<Object> {
    return this.http.get('/api/gifts/' + id + '/details')
      .toPromise()
      .then(response => {
          return response;
        })
      .catch(this.handleError);
  }

  updateGiftStatus(id: number, newStatus: string): Promise<Object> {
    return this.http.post('/api/gifts/' + id + '/status', {status: newStatus})
      .toPromise()
      .then(response => {
          return response;
        })
      .catch(this.handleError);
  }

  newEvent(data: any): Promise<Object> {
    return this.http.post('/api/events', data)
      .toPromise()
      .then(response => {
          return response;
        })
      .catch(this.handleError);
  }

  addParticipant(eventid: number, data: any): Promise<Object> {
    return this.http.post('/api/events/' + eventid + '/participants', data)
      .toPromise()
      .then(response => {
          return response;
        })
      .catch(this.handleError);
  }

  private handleError(error: any): Promise<any> {
    console.error('An error occurred', error); // for demo purposes only
    return Promise.reject(error.message || error);
  }

  editGift(eventid: number, payload: any): Promise<Object> {
    return this.http.post('/api/events/' + eventid + '/gifts', payload)
      .toPromise()
      .then(response => {
          return response;
        })
      .catch(this.handleError);
  }
}
