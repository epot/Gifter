import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { CalendarModule } from 'primeng/calendar';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { CoreModule } from '@app/core';
import { SharedModule } from '@app/shared';
import { NewEventRoutingModule } from './new-event-routing.module';
import { NewEventComponent } from './new-event.component';

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
    NewEventRoutingModule
  ],
  declarations: [NewEventComponent],
  providers: [DatePipe]
})
export class NewEventModule {}
