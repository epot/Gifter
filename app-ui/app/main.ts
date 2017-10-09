
import 'reflect-metadata';
import 'core-js';
import 'zone.js';

import 'bootstrap/js/modal';
import 'bootstrap/js/dropdown';
import 'bootstrap/js/tooltip';

import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { getTranslationProviders } from './i18n-providers';
import { AppModule } from './app.module';

getTranslationProviders().then(providers => {
  const options = { providers };
  platformBrowserDynamic().bootstrapModule(AppModule, options);
});
