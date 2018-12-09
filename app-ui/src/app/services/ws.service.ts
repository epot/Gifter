import { Injectable } from '@angular/core';
import { Subject, Observable, Observer } from 'rxjs';
import { AuthService } from 'ng2-ui-auth';

@Injectable()
export class WSService {
  private headers = new Headers({ 'Content-Type': 'application/json' });
  private wsEventUrl = '/api/ws/events/'; // URL to web api

  constructor(public auth: AuthService) {}

  public connectEventWS(
    location: Location,
    eventId: number
  ): Subject<MessageEvent> {
    const wsProtocol = location.protocol === 'https:' ? 'wss' : 'ws';
    let token = '';
    if (this.auth.isAuthenticated()) {
      token = this.auth.getToken();
    }
    return this.create(
      wsProtocol +
        '://' +
        location.host +
        this.wsEventUrl +
        eventId +
        '?X-Auth-Token=' +
        token
    );
  }

  private create(url: string): Subject<MessageEvent> {
    const ws = new WebSocket(url);
    const observable = Observable.create((obs: Observer<MessageEvent>) => {
      ws.onmessage = obs.next.bind(obs);
      ws.onerror = obs.error.bind(obs);
      ws.onclose = obs.complete.bind(obs);
      return ws.close.bind(ws);
    });
    const observer = {
      next: (data: Object) => {
        if (ws.readyState === WebSocket.OPEN) {
          ws.send(JSON.stringify(data));
        }
      }
    };
    return Subject.create(observer, observable);
  }
}
