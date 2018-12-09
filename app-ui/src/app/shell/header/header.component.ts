import { Component, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

import { TokenUser } from '../../token-user';
import { UserService } from '../../services/user.service';
import { ErrorHandleService } from '../../services/error-handle.service';
import { I18nService } from '@app/core';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnDestroy {
  menuHidden = true;
  public user: TokenUser;
  private userSubscription: Subscription;

  constructor(
    private router: Router,
    private i18nService: I18nService,
    private userService: UserService,
    private eh: ErrorHandleService
  ) {
    this.userSubscription = userService.userChanged$.subscribe(user => {
      this.user = user;
    });
  }

  isAuthenticated(): boolean {
    return this.userService.isAuthenticated();
  }

  logout() {
    this.userService.logout().subscribe({
      error: (err: any) => this.eh.handleError(err),
      complete: () => this.router.navigateByUrl('/login')
    });
  }

  ngOnDestroy() {
    // prevent memory leak when component destroyed
    this.userSubscription.unsubscribe();
  }
  toggleMenu() {
    this.menuHidden = !this.menuHidden;
  }

  setLanguage(language: string) {
    this.i18nService.language = language;
  }

  get currentLanguage(): string {
    return this.i18nService.language;
  }

  get languages(): string[] {
    return this.i18nService.supportedLanguages;
  }
}
