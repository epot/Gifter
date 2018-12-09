import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { LoginRoutingModule } from './login-routing.module';
import { LoginComponent } from './login.component';
import { OAuth2Component } from './oauth2.component';
import { SignUpComponent } from './signup.component';
import { ForgotPasswordComponent } from './forgot-password.component';
import { ResetPasswordComponent } from './reset-password.component';
import { PasswordService } from '../services/password.service';

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TranslateModule,
    NgbModule,
    LoginRoutingModule
  ],
  declarations: [
    LoginComponent,
    OAuth2Component,
    SignUpComponent,
    ForgotPasswordComponent,
    ResetPasswordComponent
  ],
  providers: [PasswordService]
})
export class LoginModule {}
