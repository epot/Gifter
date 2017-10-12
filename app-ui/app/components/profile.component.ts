import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { AuthService } from '../ng2-ui-auth/auth.service';
import { Subscription } from 'rxjs/Subscription';

import { TokenUser } from '../token-user';
import { UserService } from '../services/user.service';

@Component({
  selector: 'my-profile',
  templateUrl: 'templates/profile.component.html'
})
export class ProfileComponent implements OnInit, OnDestroy {
  public user: TokenUser;
  private userSubscription: Subscription;

  constructor(private userService: UserService) {
  }

  ngOnInit() {
    this.user = this.userService.getUser();
    this.userSubscription = this.userService.userChanged$.subscribe(
      user => {
        this.user = user;
    });
  }

  ngOnDestroy() {
    // prevent memory leak when component destroyed
    this.userSubscription.unsubscribe();
  }
}
