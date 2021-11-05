import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import {
  FormBuilder,
  FormControl,
  Validators,
  FormGroup
} from '@angular/forms';

import { environment } from '@env/environment';
import { Logger, I18nService } from '@app/core';
import { ErrorHandleService } from '../services/error-handle.service';
import { LoginData } from '../login-data';
import { UserService } from '../services/user.service';
import { FormHelperService } from '../services/form-helper.service';

const log = new Logger('Login');

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html'
})
export class LoginComponent implements OnInit {
  version: string = environment.version;
  error: string;
  form: FormGroup;
  isLoading = false;

  constructor(
    private auth: AuthService,
    private router: Router,
    public fh: FormHelperService,
    private fb: FormBuilder,
    private i18nService: I18nService,
    private eh: ErrorHandleService,
    private userService: UserService
  ) {}

  ngOnInit() {
    this.form = this.fb.group({
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required]),
      rememberMe: new FormControl(true)
    });
    if (this.auth.isAuthenticated()) {
      this.router.navigateByUrl('/');
    }
  }

  login(loginData: LoginData) {
    this.auth.login(loginData).subscribe({
      error: (err: any) => this.eh.handleError(err),
      complete: () => {
        this.userService.renewUser();
        this.router.navigateByUrl('/');
      }
    });
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

  authenticate(provider: string) {
    this.auth.authenticate(provider).subscribe({
      error: (err: any) => this.eh.handleError(err),
      complete: () => {
        this.userService.renewUser();
        this.router.navigateByUrl('/');
      }
    });
  }
}
