import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import {
  DataTableModule,
  SharedModule as PrimeNgSharedModule
} from 'primeng/primeng';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { DropdownModule } from 'primeng/components/dropdown/dropdown';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { CoreModule } from '@app/core';
import { SharedModule } from '@app/shared';
import { EventRoutingModule } from './event-routing.module';
import { EventComponent } from './event.component';
import { WSService } from '../services/ws.service';

@NgModule({
  imports: [
    ReactiveFormsModule,
    FormsModule,
    DataTableModule,
    PrimeNgSharedModule,
    NgbModule,
    DropdownModule,
    CommonModule,
    TranslateModule,
    CoreModule,
    SharedModule,
    EventRoutingModule
  ],
  declarations: [EventComponent],
  providers: [WSService]
})
export class EventModule {}
