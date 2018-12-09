import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { LoaderComponent } from './loader/loader.component';
import { LeftMenuComponent } from './left-menu/left-menu.component';

@NgModule({
  imports: [CommonModule, RouterModule],
  declarations: [LoaderComponent, LeftMenuComponent],
  exports: [LoaderComponent, LeftMenuComponent]
})
export class SharedModule {}
