import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { Subject } from 'rxjs';
import { Observable } from 'rxjs';

import { TokenUser } from '../token-user';

@Injectable()
export class UserService {
  public user: TokenUser;
  expiration: Date;
  userChangedSource = new Subject<TokenUser>();
  userChanged$ = this.userChangedSource.asObservable();

  private headers = new Headers({ 'Content-Type': 'application/json' });

  constructor(
    private auth: AuthService,
    private router: Router,
    private http: HttpClient
  ) {
    this.renewUser().catch(_ => _);
  }

  logout() {
    this.user = null;
    return this.auth.logout();
  }

  getUser() {
    return this.user;
  }

  renewUser(): Promise<TokenUser> {
    this.expiration = this.auth.getExpirationDate();

    return this.http
      .get('/api/user')
      .toPromise()
      .then(response => {
        this.user = response as TokenUser;
        this.userChangedSource.next(this.user);
        return this.user;
      })
      .catch(this.handleError);
  }

  isAuthenticated() {
    return this.user && this.auth.isAuthenticated();
  }

  private handleError(error: any): Promise<any> {
    console.error('An error occurred', error); // for demo purposes only
    return Promise.reject(error.message || error);
  }
}
