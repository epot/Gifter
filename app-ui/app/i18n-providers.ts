import { TRANSLATIONS, TRANSLATIONS_FORMAT, LOCALE_ID, StaticProvider, MissingTranslationStrategy } from '@angular/core';
import { CompilerConfig } from '@angular/compiler';
declare var System: any;

export function getTranslationProviders(): Promise<StaticProvider[]> {

  // Get the locale id from the global
  const locale = document['locale'] as string;

  // return no providers if fail to get translation file for locale
  const noProviders: StaticProvider[] = [];

  // No locale or U.S. English: no translation providers
  if (!locale || locale === 'en-US') {
    return Promise.resolve(noProviders);
  }

  return getTranslationsWithES6Import(locale)
    .then( (translations: string ) => [
      { provide: TRANSLATIONS, useValue: translations },
      { provide: TRANSLATIONS_FORMAT, useValue: 'xlf' },
      { provide: LOCALE_ID, useValue: locale },
    ])
    .catch(() => noProviders); // ignore if file not found
}

function getTranslationsWithES6Import(locale: string) {
  /**
   * System.import in Webpack is an alias of the standard dynamic import()
   * but the latter can't be used because Typescript would complain.
   * @see https://webpack.js.org/guides/code-splitting-import/#dynamic-import
   * @see https://github.com/Microsoft/TypeScript/issues/12364
   *
   * Also the file name must not used backticks and the file extension must be
   * within the function call otherwise Webpack won't know how to load it.
   * @see https://github.com/angular/angular-cli/issues/5285#issuecomment-285059969
   */
  return System.import('./locale/messages.' + locale + '.xlf');
}
