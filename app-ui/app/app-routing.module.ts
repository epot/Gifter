import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { MainComponent } from './components/main.component';
import { SignInComponent } from './components/sign-in.component';
import { SignUpComponent } from './components/sign-up.component';
import { NewEventComponent } from './components/new-event.component';
import { GiftViewComponent } from './components/gift-view.component';
import { GiftAddComponent } from './components/gift-add.component';
import { GiftEditComponent } from './components/gift-edit.component';
import { EventComponent } from './components/event.component';
import { AuthGuardService } from './services/auth-guard.service';

export const CLIENT_ROUTER_PROVIDERS = [
    AuthGuardService
];

const routes: Routes = [
  { path: '', redirectTo: 'events', pathMatch: 'full' },
  { path: 'events', component: MainComponent, canActivate: [AuthGuardService] },
  { path: 'events/:id', component: EventComponent, canActivate: [AuthGuardService] },
  { path: 'events/:id/addGift', component: GiftAddComponent, canActivate: [AuthGuardService] },
  { path: 'events/:id/gifts/:giftid/view', component: GiftViewComponent, canActivate: [AuthGuardService] },
  { path: 'events/:id/gifts/:giftid/edit', component: GiftEditComponent, canActivate: [AuthGuardService] },
  { path: 'signIn', component: SignInComponent },
  { path: 'signUp', component: SignUpComponent },
  { path: 'newEvent', component: NewEventComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
