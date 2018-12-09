import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';

import { TokenUser } from '../token-user';
import { UserService } from '../services/user.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html'
})
export class ProfileComponent implements OnInit, OnDestroy {
  public user: TokenUser;
  private userSubscription: Subscription;

  constructor(private userService: UserService) {}

  ngOnInit() {
    this.user = this.userService.getUser();
    this.userSubscription = this.userService.userChanged$.subscribe(user => {
      this.user = user;
    });
  }

  ngOnDestroy() {
    // prevent memory leak when component destroyed
    this.userSubscription.unsubscribe();
  }
}
