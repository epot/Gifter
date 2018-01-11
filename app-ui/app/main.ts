
import 'reflect-metadata';
import 'core-js';
import 'zone.js';

import 'bootstrap';
import 'ngx-toastr/toastr.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'font-awesome/css/font-awesome.min.css';
import 'primeng/resources/primeng.min.css';
import 'primeng/resources/themes/omega/theme.css';

import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { getTranslationProviders } from './i18n-providers';
import { AppModule } from './app.module';
import { enableProdMode, TRANSLATIONS, TRANSLATIONS_FORMAT } from '@angular/core';

getTranslationProviders().then(providers => {
  platformBrowserDynamic().bootstrapModule(AppModule, {
    providers: providers
  });
});
