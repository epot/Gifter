import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { extract } from '@app/core';
import { NewEventComponent } from './new-event.component';
import { Shell } from '@app/shell/shell.service';
import { AuthGuardService } from '../services/auth-guard.service';

const routes: Routes = [
  Shell.childRoutes([
    {
      path: 'event/new',
      component: NewEventComponent,
      data: { title: extract('New event') },
      canActivate: [AuthGuardService]
    }
  ])
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: []
})
export class NewEventRoutingModule {}
