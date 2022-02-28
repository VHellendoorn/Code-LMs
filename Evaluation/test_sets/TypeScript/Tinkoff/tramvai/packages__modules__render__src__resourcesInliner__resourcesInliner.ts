import isUndefined from '@tinkoff/utils/is/undefined';
import isEmpty from '@tinkoff/utils/is/empty';
import type { PageResource, RESOURCE_INLINE_OPTIONS } from '@tramvai/tokens-render';
import { ResourceType } from '@tramvai/tokens-render';
import { isAbsoluteUrl } from '@tinkoff/url';
import { getFile, getFileContentLength } from './externalFilesHelper';
import type { RESOURCES_REGISTRY_CACHE } from './tokens';
import { processFile } from './fileProcessor';

const getInlineType = (type: PageResource['type']) => {
  switch (type) {
    case ResourceType.style:
      return ResourceType.inlineStyle;
    case ResourceType.script:
      return ResourceType.inlineScript;
    default:
      return type;
  }
};

const getResourceUrl = (resource: PageResource) => {
  if (isEmpty(resource.payload) || !isAbsoluteUrl(resource.payload)) {
    return undefined;
  }
  return resource.payload.startsWith('//')
    ? `https://${resource.payload.substr(2)}`
    : resource.payload;
};

export interface ResourcesInlinerType {
  shouldAddResource(resource: PageResource): boolean;
  shouldInline(resource: PageResource): boolean;

  inlineResource(resource: PageResource): PageResource[];
}

export class ResourcesInliner implements ResourcesInlinerType {
  private resourceInlineThreshold?: typeof RESOURCE_INLINE_OPTIONS;
  private resourcesRegistryCache: typeof RESOURCES_REGISTRY_CACHE;

  private scheduleFileLoad = (resource: PageResource, resourceInlineThreshold: number) => {
    const url = getResourceUrl(resource);
    const requestKey = `file${url}`;
    const urlIsDisabled = this.resourcesRegistryCache.disabledUrlsCache.get(url);
    const requestIsInProgress = this.resourcesRegistryCache.requestsCache.get(requestKey);
    if (!requestIsInProgress && !urlIsDisabled) {
      const result = this.resourcesRegistryCache.filesCache.get(url);
      if (result) {
        return result;
      }
      const getFilePromise = getFile(url)
        .then((file) => {
          if (file === undefined) {
            this.resourcesRegistryCache.disabledUrlsCache.set(url, true);
            return;
          }
          const size = file.length;
          if (size < resourceInlineThreshold) {
            this.resourcesRegistryCache.filesCache.set(url, processFile(resource, file));
          }
          this.resourcesRegistryCache.sizeCache.set(url, size);
        })
        .finally(() => {
          this.resourcesRegistryCache.requestsCache.set(requestKey, undefined);
        });
      this.resourcesRegistryCache.requestsCache.set(requestKey, getFilePromise);
    }
  };

  private scheduleFileSizeLoad = (resource: PageResource, resourceInlineThreshold: number) => {
    const url = getResourceUrl(resource);
    const requestKey = `size${url}`;
    if (!this.resourcesRegistryCache.requestsCache.has(requestKey)) {
      const result = this.resourcesRegistryCache.sizeCache.get(url);
      if (result) {
        return result;
      }
      const getFileSizePromise = getFileContentLength(url)
        .then((contentLength: string) => {
          const size = isUndefined(contentLength) ? 0 : +contentLength;
          if (size) {
            this.resourcesRegistryCache.sizeCache.set(url, size);
          }
          if (size < resourceInlineThreshold) {
            this.scheduleFileLoad(resource, resourceInlineThreshold);
          }
        })
        .finally(() => {
          this.resourcesRegistryCache.requestsCache.set(requestKey, undefined);
        });
      this.resourcesRegistryCache.requestsCache.set(requestKey, getFileSizePromise);
    }
  };

  constructor({ resourcesRegistryCache, resourceInlineThreshold }) {
    this.resourcesRegistryCache = resourcesRegistryCache;
    this.resourceInlineThreshold = resourceInlineThreshold;
  }

  // Метод проверки, стоит ли добавлять preload-ресурс
  shouldAddResource(resource: PageResource) {
    if (resource.type !== ResourceType.preloadLink) {
      // Мы фильтруем только preloadLink, если это ресурс другого типа он должен
      // попасть в итоговую выборку.
      return true;
    }

    const url = getResourceUrl(resource);

    if (isUndefined(url)) {
      // Если у ресурса нет URL'а, в кеше этого файла точно нет
      return true;
    }
    // Если файл лежит в кеше, значит он прошёл все проверки и будет инлайниться =>
    // нам не нужно добавлять для него preload.
    return !this.resourcesRegistryCache.filesCache.has(url);
  }

  // Метод проверки, должен ли быть заинлайнен в HTML-страницу ресурс.
  shouldInline(resource: PageResource) {
    if (!(this.resourceInlineThreshold?.types || []).includes(resource.type)) {
      return false;
    }
    const resourceInlineThreshold = this.resourceInlineThreshold.threshold;

    if (isUndefined(resourceInlineThreshold)) {
      return false;
    }

    const url = getResourceUrl(resource);

    if (isUndefined(url)) {
      return false;
    }

    if (!this.resourcesRegistryCache.sizeCache.has(url)) {
      this.scheduleFileSizeLoad(resource, resourceInlineThreshold);
      return false;
    }

    const size = this.resourcesRegistryCache.sizeCache.get(url);

    if (size > resourceInlineThreshold) {
      return false;
    }

    if (!this.resourcesRegistryCache.filesCache.has(url)) {
      this.scheduleFileLoad(resource, resourceInlineThreshold);
      return false;
    }

    return true;
  }

  inlineResource(resource: PageResource): PageResource[] {
    const url = getResourceUrl(resource);
    if (isUndefined(url)) {
      // В теории, такого быть не должно, но добавим проверку на всякий случай
      return [resource];
    }
    const text = this.resourcesRegistryCache.filesCache.get(url);
    if (isEmpty(text)) {
      return [resource];
    }

    const result = [];
    if (process.env.NODE_ENV === 'development') {
      // Добавляем html комментарии для упрощения отладки инлайнинга в dev режиме
      result.push({
        slot: resource.slot,
        type: ResourceType.asIs,
        payload: `<!-- Inlined file ${url} -->`,
      });
    }
    result.push({
      ...resource,
      type: getInlineType(resource.type),
      payload: text,
    });
    if (resource.type === ResourceType.style) {
      // Если не добавить data-href, extract-css-chunks-webpack-plugin
      // добавит (https://github.com/faceyspacey/extract-css-chunks-webpack-plugin/blob/master/src/index.js#L346)
      // в head ссылку на файл и инлайнинг будет бесполезен, т.к. браузер всё равно пойдёт скачивать этот файл.
      // При этом в случае css-файлов он ищет тэг link, а при инлайнинге мы вставляем style => мы не можем
      // использовать тэг выше, а приходится генерировать новый.
      result.push({
        slot: resource.slot,
        type: ResourceType.style,
        payload: null,
        attrs: {
          'data-href': resource.payload,
        },
      });
    }
    return result;
  }
}
