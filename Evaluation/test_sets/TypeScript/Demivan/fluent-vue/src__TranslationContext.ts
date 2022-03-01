import type { Message, Pattern } from '@fluent/bundle/esm/ast'
import type { FluentBundle, FluentVariable } from '@fluent/bundle'
import type { Ref } from 'vue-demi'

import { mapBundleSync } from '@fluent/sequence'
import { warn } from './util/warn'

export interface TranslationWithAttrs {
  value: string
  hasValue: boolean
  attributes: Record<string, string>
}

export class TranslationContext {
  bundles: Ref<Iterable<FluentBundle>>

  constructor(bundles: Ref<Iterable<FluentBundle>>) {
    this.bundles = bundles
  }

  getBundle(key: string): FluentBundle | null {
    return mapBundleSync(this.bundles.value, key)
  }

  getMessage(bundle: FluentBundle | null, key: string): Message | null {
    const message = bundle?.getMessage(key)

    if (message === undefined) {
      warn(`Could not find translation for key [${key}]`)
      return null
    }

    return message
  }

  formatPattern(
    bundle: FluentBundle,
    message: Pattern,
    value?: Record<string, FluentVariable>,
  ): string {
    const errors: Error[] = []
    const formatted = bundle.formatPattern(message, value, errors)

    for (const error of errors)
      warn('Error when formatting', error)

    return formatted
  }

  private _format(
    context: FluentBundle | null,
    message: Message | null,
    value?: Record<string, FluentVariable>,
  ): string | null {
    if (context === null || message === null || message.value === null)
      return null

    return this.formatPattern(context, message.value, value)
  }

  format = (key: string, value?: Record<string, FluentVariable>): string => {
    const context = this.getBundle(key)
    const message = this.getMessage(context, key)
    return this._format(context, message, value) ?? key
  }

  private _formatAttrs(
    context: FluentBundle | null,
    message: Message | null,
    value?: Record<string, FluentVariable>,
  ): Record<string, string> | null {
    if (context === null || message === null)
      return null

    const result: Record<string, string> = {}
    for (const [attrName, attrValue] of Object.entries(message.attributes))
      result[attrName] = this.formatPattern(context, attrValue, value)

    return result
  }

  formatAttrs = (key: string, value?: Record<string, FluentVariable>): Record<string, string> => {
    const context = this.getBundle(key)
    const message = this.getMessage(context, key)
    return this._formatAttrs(context, message, value) ?? {}
  }

  formatWithAttrs = (key: string, value?: Record<string, FluentVariable>): TranslationWithAttrs => {
    const context = this.getBundle(key)
    const message = this.getMessage(context, key)

    const formatValue = this._format(context, message, value)

    return {
      value: formatValue ?? key,
      attributes: this._formatAttrs(context, message, value) ?? {},
      hasValue: formatValue !== null,
    }
  }

  $t = this.format
  $ta = this.formatAttrs
}
