import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { ActivatedRoute, Params } from '@angular/router';
import {
  FormGroup,
  FormControl,
  FormArray,
  Validators,
  FormBuilder
} from '@angular/forms';
import { Router } from '@angular/router';

import { FormHelperService } from '../services/form-helper.service';
import { ErrorHandleService } from '../services/error-handle.service';
import { UserService } from '../services/user.service';
import { EventsService } from '../services/events.service';
import { TokenUser } from '../token-user';

@Component({
  selector: 'my-gift-edit',
  templateUrl: './new-gift.component.html'
})
export class NewGiftComponent implements OnInit, OnDestroy {
  public user: TokenUser;
  private _userSubscription: Subscription;
  public participants: Object[];
  public loadingGift;
  public error: any;
  public eventid: number;
  public urls: string[];
  form: FormGroup;

  constructor(
    private userService: UserService,
    private eventsService: EventsService,
    private fb: FormBuilder,
    public fh: FormHelperService,
    private router: Router,
    private eh: ErrorHandleService,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.user = this.userService.getUser();
    this._userSubscription = this.userService.userChanged$.subscribe(user => {
      this.user = user;
    });

    this.route.params.forEach((params: Params) => {
      this.eventid = +params['id'];
      this.form = this.fb.group({
        name: new FormControl('', [Validators.required]),
        toId: new FormControl('', [Validators.required]),
        secret: new FormControl(false, [Validators.required])
      });
      this.urls = [];
      this.eventsService.getEventParticipants(this.eventid).then(
        response => {
          this.participants = response['participants'];
        },
        err => {
          this.error = err;
        }
      );
    });
  }

  ngOnDestroy() {
    // prevent memory leak when component destroyed
    this._userSubscription.unsubscribe();
  }

  submit(formData: any) {
    this.eventsService
      .editGift(this.eventid, {
        name: formData['name'],
        secret: formData['secret'],
        creatorid: this.user['id'],
        eventid: this.eventid,
        to: formData['toId'],
        urls: this.urls
      })
      .then(response => {
        this.router.navigateByUrl('/events/' + this.eventid);
      })
      .catch(err => {
        this.eh.handleError(err);
      });
  }

  addLink() {
    this.urls.push('');
  }

  // https://stackoverflow.com/questions/42322968/angular2-dynamic-input-field-lose-focus-when-input-changes
  trackByFn(index: any, item: any) {
    return index;
  }

  removeLink(i: number) {
    const url = this.urls[i]; // Item to remove
    this.urls = this.urls.filter(obj => obj !== url);
  }
}
