import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { TableModule } from 'primeng/table';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { DropdownModule } from 'primeng/dropdown';
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
    TableModule,
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
