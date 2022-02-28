import {
  createMarkdownParser as createDefaultMarkdownParser,
  MarkdownParser,
  MarkdownParserOptions as DefaultMarkdownParserOptions,
} from '@vitebook/markdown/node';

import {
  codePlugin,
  CodePluginOptions,
  customComponentPlugin,
  CustomComponentPluginOptions,
  hoistTagsPlugin,
  HoistTagsPluginOptions,
} from './plugins';

export type MarkdownParserOptions = DefaultMarkdownParserOptions & {
  code?: false | CodePluginOptions;
  customComponent?: false | CustomComponentPluginOptions;
  hoistTags?: false | HoistTagsPluginOptions;
};

export function createMarkdownParser({
  code,
  customComponent,
  hoistTags,
  links,
  ...markdownOptions
}: MarkdownParserOptions): Promise<MarkdownParser> {
  return createDefaultMarkdownParser({
    ...markdownOptions,
    // Use Vue specific plugins.
    code: false,
    customComponent: false,
    links:
      links !== false
        ? {
            ...links,
            externalIcon: false,
          }
        : false,
    async configureParser(parser) {
      await markdownOptions.configureParser?.(parser);

      // Treat unknown html tags as custom components.
      if (customComponent !== false) {
        parser.use(customComponentPlugin, customComponent);
      }

      // Hoist Vue SFC blocks and extract them into `env`.
      if (hoistTags !== false) {
        parser.use<HoistTagsPluginOptions>(hoistTagsPlugin, hoistTags);
      }

      // Process code fences.
      if (code !== false) {
        parser.use<CodePluginOptions>(codePlugin, code);
      }
    },
  });
}
