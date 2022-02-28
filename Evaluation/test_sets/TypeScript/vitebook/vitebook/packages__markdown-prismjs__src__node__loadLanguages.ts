// eslint-disable-next-line import/default
import prism from 'prismjs';
import rawLoadLanguages from 'prismjs/components/index';

// eslint-disable-next-line import/no-named-as-default-member
const { languages } = prism;

rawLoadLanguages.silent = true;

export const loadLanguages = (langs: string[]): void => {
  const langsToLoad = langs.filter((item) => !languages[item]);
  if (langsToLoad.length) {
    rawLoadLanguages(langsToLoad);
  }
};
