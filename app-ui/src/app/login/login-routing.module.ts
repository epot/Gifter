import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { extract } from '@app/core';
import { LoginComponent } from './login.component';
import { OAuth2Component } from './oauth2.component';
import { SignUpComponent } from './signup.component';
import { ForgotPasswordComponent } from './forgot-password.component';
import { ResetPasswordComponent } from './reset-password.component';

const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    data: { title: extract('Login') }
  },
  {
    path: 'signup',
    component: SignUpComponent,
    data: { title: extract('Sign up') }
  },
  {
    path: 'forgot-password',
    component: ForgotPasswordComponent,
    data: { title: extract('Forgot your password?') }
  },
  {
    path: 'reset-password/:token',
    component: ResetPasswordComponent,
    data: { title: extract('Reset your password?') }
  },
  {
    path: 'oauth2',
    component: OAuth2Component,
    data: { title: extract('Login') }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: []
})
export class LoginRoutingModule {}
