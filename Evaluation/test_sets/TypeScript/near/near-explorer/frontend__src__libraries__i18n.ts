import { i18n, createInstance } from "i18next";
import { initReactI18next } from "react-i18next";

import ruTranslation from "../translations/ru/common.json";
import enTranslation from "../translations/en/common.json";
import viTranslation from "../translations/vi/common.json";
import zhHansTranslation from "../translations/zh-hans/common.json";

export const LANGUAGES = ["en", "ru", "vi", "zh-Hans"] as const;
export type Language = typeof LANGUAGES[number];
export const NAMESPACES = ["common"] as const;
export type I18nNamespace = typeof NAMESPACES[number];

export const DEFAULT_LANGUAGE: Language = "en";
export const DEFAULT_NAMESPACE: I18nNamespace = "common";

export type ResourceType = {
  common: typeof enTranslation;
};

export const resources: Record<Language, ResourceType> = {
  en: { common: enTranslation },
  ru: { common: ruTranslation },
  vi: { common: viTranslation },
  "zh-Hans": { common: zhHansTranslation },
};

export const initializeI18n = async (language?: Language): Promise<i18n> => {
  const instance = createInstance({
    resources,
    fallbackLng: DEFAULT_LANGUAGE,
    lng: language || DEFAULT_LANGUAGE,
    supportedLngs: LANGUAGES,
    defaultNS: DEFAULT_NAMESPACE,
    interpolation: {
      escapeValue: false,
      prefix: "${",
      suffix: "}",
    },
  }).use(initReactI18next);
  await instance.init();
  return instance;
};
