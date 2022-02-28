import {
  I18nObject
} from './i18n.interface';

export function fillTranslationValues(translation: string = '', values: {}): string {
  return translation.replace(/{\w+}/g, (key: string) => {
    const parsedKey: string = key.substring(1, key.length - 1);

    return (values[parsedKey] || 'undefined').toString();
  });
}

export function reduceTranslations(translations: I18nObject, path: string): I18nObject | string {
  const splittedPath: string[] = (path || '').split('.');

  return Object.keys(translations).length > 0 ?
    splittedPath.reduce((accumulator: I18nObject | string, item: string): I18nObject => {
      return accumulator[item];
    }, translations || {}) :
    '';
}
