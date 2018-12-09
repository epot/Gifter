import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { extract } from '@app/core';
import { HomeComponent } from './home.component';
import { ProfileComponent } from './profile.component';
import { Shell } from '@app/shell/shell.service';
import { AuthGuardService } from '../services/auth-guard.service';

const routes: Routes = [
  Shell.childRoutes([
    {
      path: '',
      redirectTo: '/home',
      pathMatch: 'full',
      canActivate: [AuthGuardService]
    },
    {
      path: 'profile',
      component: ProfileComponent,
      canActivate: [AuthGuardService]
    },
    {
      path: 'home',
      component: HomeComponent,
      data: { title: extract('Home') },
      canActivate: [AuthGuardService]
    }
  ])
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: []
})
export class HomeRoutingModule {}
