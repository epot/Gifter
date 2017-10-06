import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { NgModule } from '@angular/core';
import { HttpClientModule, HTTP_INTERCEPTORS  } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Ng2UiAuthModule } from 'ng2-ui-auth';
import { ToastModule } from 'ng2-toastr/ng2-toastr';
import { CookieModule } from 'ngx-cookie';
import { CalendarModule } from 'primeng/primeng';
import { DatePipe } from '@angular/common';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { Ng2Bs3ModalModule } from 'ng2-bs3-modal/ng2-bs3-modal';
import { TabsModule } from 'ngx-tabs';

import { AppRoutingModule, CLIENT_ROUTER_PROVIDERS } from './app-routing.module';
import { MyAuthConfig } from './config';
import { TokenInterceptor } from './token-interceptor';
import { AppComponent } from './components/app.component';
import { HeaderComponent } from './components/header.component';
import { MainComponent } from './components/main.component';
import { SignInComponent } from './components/sign-in.component';
import { SignUpComponent } from './components/sign-up.component';
import { LeftMenuComponent } from './components/left-menu.component';
import { NewEventComponent } from './components/new-event.component';
import { GiftViewComponent } from './components/gift-view.component';
import { GiftEditComponent } from './components/gift-edit.component';
import { EventComponent } from './components/event.component';
import { AuthGuardService } from './services/auth-guard.service';
import { ErrorHandleService } from './services/error-handle.service';
import { FormHelperService } from './services/form-helper.service';
import { JsonHttpGateway } from './services/json-http.service';
import { UserService } from './services/user.service';
import { EventsService } from './services/events.service';
import { GiftAddComponent } from './components/gift-add.component';
import { ResetPasswordComponent } from './components/reset-password.component';
import { ForgotPasswordComponent } from './components/forgot-password.component';
import { PasswordService } from './services/password.service';

@NgModule({
  imports: [
    NgxDatatableModule,
    BrowserModule,
    BrowserAnimationsModule,
    ReactiveFormsModule,
    CalendarModule,
    FormsModule,
    AppRoutingModule,
    HttpClientModule,
    Ng2Bs3ModalModule,
    TabsModule,
    Ng2UiAuthModule.forRoot(MyAuthConfig),
    ToastModule.forRoot(),
    CookieModule.forRoot(),
  ],
  declarations: [
    AppComponent,
    MainComponent,
    SignInComponent,
    HeaderComponent,
    SignUpComponent,
    LeftMenuComponent,
    NewEventComponent,
    EventComponent,
    GiftViewComponent,
    GiftEditComponent,
    GiftAddComponent,
    ResetPasswordComponent,
    ForgotPasswordComponent,
  ],
  providers: [
    ErrorHandleService,
    FormHelperService,
    CLIENT_ROUTER_PROVIDERS,
    { provide: LocationStrategy, useClass: HashLocationStrategy },
    { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true},
    AuthGuardService,
    JsonHttpGateway,
    UserService,
    EventsService,
    DatePipe,
    PasswordService,
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
