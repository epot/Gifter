import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { ServiceWorkerModule } from '@angular/service-worker';
import { TranslateModule } from '@ngx-translate/core';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { Ng2UiAuthModule } from './auth/ng2-ui-auth.module';
import { StorageType } from './auth/storage-type.enum';
import { ToastrModule } from 'ngx-toastr';
import { CookieModule } from 'ngx-cookie';

import { TokenInterceptor } from './token-interceptor';

import { environment } from '@env/environment';
import { CoreModule } from '@app/core';
import { SharedModule } from '@app/shared';
import { EventModule } from './event/event.module';
import { HomeModule } from './home/home.module';
import { NewEventModule } from './new-event/new-event.module';
import { GiftModule } from './gift/gift.module';
import { ShellModule } from './shell/shell.module';
import { AboutModule } from './about/about.module';
import { LoginModule } from './login/login.module';
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { UserService } from './services/user.service';
import { ErrorHandleService } from './services/error-handle.service';
import { FormHelperService } from './services/form-helper.service';
import { AuthGuardService } from './services/auth-guard.service';
import { EventsService } from './services/events.service';

export const GOOGLE_CLIENT_ID =
  '174400993910-63csgump26j3jjh4havakk0lmhhee1tl.apps.googleusercontent.com';

@NgModule({
  imports: [
    BrowserModule,
    ServiceWorkerModule.register('./ngsw-worker.js', {
      enabled: environment.production
    }),
    FormsModule,
    HttpClientModule,
    TranslateModule.forRoot(),
    NgbModule,
    CoreModule,
    SharedModule,
    ShellModule,
    HomeModule,
    EventModule,
    NewEventModule,
    GiftModule,
    AboutModule,
    LoginModule,
    ToastrModule.forRoot(),
    CookieModule.forRoot(),
    Ng2UiAuthModule.forRoot({
      baseUrl: '/',
      loginUrl: '/login',
      signupUrl: '/signUp',
      tokenName: 'token',
      tokenPrefix: 'ng2-ui-auth', // Local Storage name prefix
      authHeader: 'X-Auth-Token',
      storageType: StorageType.COOKIE,
      providers: {
        google: {
          clientId: GOOGLE_CLIENT_ID,
          url: '/authenticate/google',
          redirectUri: environment.localUrl + '/oauth2'
        }
      }
    }),
    AppRoutingModule // must be imported as the last module as it contains the fallback route
  ],
  declarations: [AppComponent],
  providers: [
    UserService,
    ErrorHandleService,
    FormHelperService,
    AuthGuardService,
    EventsService,
    { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
