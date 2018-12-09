import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { CalendarModule } from 'primeng/primeng';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { CoreModule } from '@app/core';
import { SharedModule } from '@app/shared';
import { GiftRoutingModule } from './gift-routing.module';
import { NewGiftComponent } from './new-gift.component';
import { EditGiftComponent } from './edit-gift.component';
import { ViewGiftComponent } from './view-gift.component';

@NgModule({
  imports: [
    ReactiveFormsModule,
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    CommonModule,
    TranslateModule,
    CoreModule,
    CalendarModule,
    SharedModule,
    GiftRoutingModule
  ],
  declarations: [NewGiftComponent, EditGiftComponent, ViewGiftComponent],
  providers: [DatePipe]
})
export class GiftModule {}
