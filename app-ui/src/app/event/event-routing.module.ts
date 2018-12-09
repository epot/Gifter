import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { EventComponent } from './event.component';
import { Shell } from '@app/shell/shell.service';
import { AuthGuardService } from '../services/auth-guard.service';

const routes: Routes = [
  Shell.childRoutes([
    {
      path: 'events/:id',
      component: EventComponent,
      canActivate: [AuthGuardService]
    }
  ])
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: []
})
export class EventRoutingModule {}
