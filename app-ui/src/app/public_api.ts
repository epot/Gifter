import { Ng2UiAuthModule } from './auth/ng2-ui-auth.module';
import { LocalService } from './auth/local.service';
import { Oauth2Service } from './auth/oauth2.service';
import { Oauth1Service } from './auth/oauth1.service';
import { PopupService } from './auth/popup.service';
import { OauthService } from './auth/oauth.service';
import { SharedService } from './auth/shared.service';
import { StorageService } from './auth/storage-service';
import { BrowserStorageService } from './auth/browser-storage.service';
import { AuthService } from './auth/auth.service';
import { ConfigService, CONFIG_OPTIONS } from './auth/config.service';
import { JwtInterceptor } from './auth/interceptor.service';
import { IProviders } from './auth/config-interfaces';
import { StorageType } from './auth/storage-type.enum';

/*
 * Public API Surface of ng2-ui-auth
 */
export {
  Ng2UiAuthModule,
  LocalService,
  Oauth2Service,
  Oauth1Service,
  PopupService,
  OauthService,
  SharedService,
  StorageService,
  BrowserStorageService,
  AuthService,
  ConfigService,
  JwtInterceptor,
  CONFIG_OPTIONS,
  IProviders,
  StorageType
};
