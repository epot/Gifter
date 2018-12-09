import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { extract } from '@app/core';
import { NewGiftComponent } from './new-gift.component';
import { EditGiftComponent } from './edit-gift.component';
import { ViewGiftComponent } from './view-gift.component';
import { Shell } from '@app/shell/shell.service';
import { AuthGuardService } from '../services/auth-guard.service';

const routes: Routes = [
  Shell.childRoutes([
    {
      path: 'events/:id/gift/new',
      component: NewGiftComponent,
      data: { title: extract('New gift') },
      canActivate: [AuthGuardService]
    },
    {
      path: 'events/:id/gifts/:giftid/view',
      component: ViewGiftComponent,
      data: { title: extract('View gift') },
      canActivate: [AuthGuardService]
    },
    {
      path: 'events/:id/gifts/:giftid/edit',
      component: EditGiftComponent,
      data: { title: extract('Edit gift') },
      canActivate: [AuthGuardService]
    }
  ])
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: []
})
export class GiftRoutingModule {}
