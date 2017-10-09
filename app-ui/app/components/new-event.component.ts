import { Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators, FormBuilder } from '@angular/forms';
import { DatePipe } from '@angular/common';

import { FormHelperService } from '../services/form-helper.service';
import { ErrorHandleService } from '../services/error-handle.service';
import { EventsService } from '../services/events.service';

@Component({
    selector: 'new-event',
    templateUrl: 'templates/new-event.component.html'
})
export class NewEventComponent implements OnInit {
    form: FormGroup;
    passwordFieldName = 'password';
    repeatedPasswordFieldName = 'passwordConfirmation';

    constructor(private eventsService: EventsService,
                private router: Router,
                private fb: FormBuilder,
                public fh: FormHelperService,
                private eh: ErrorHandleService,
                public datepipe: DatePipe) {
    }

    ngOnInit() {
        this.form = this.fb.group({
            'name': new FormControl('', [Validators.required]),
            'date': new FormControl('', [Validators.required]),
            'type': new FormControl('', [Validators.required])
        });
    }

    submit(formData: any) {
        this.eventsService.newEvent({
            name: formData['name'],
            dateStr: this.datepipe.transform(formData['date'], 'dd-MM-yyyy'),
            type: formData['type']
        })
        .then(response => {
            this.router.navigateByUrl('/events/' + response['id']);
          }
        ).catch(err => {
            this.eh.handleError(err);
        });
    }
}
