import {CommonModule} from '@angular/common'
import {ModuleWithProviders, NgModule} from '@angular/core'
import {ReactiveFormsModule} from '@angular/forms'
import {FormatterParser} from './formatter-parser'

import {FormatterParserDirective} from './formatter-parser.directive'
import {FormatterParserCollectorService} from './formatter-parser.service'
import {InputContextService} from './input-context.service'

export * from './formatter-parser.directive'
export * from './formatter-parser.service'
export * from './formatter-parser'
export * from './formatter-parser.injectionToken'
export * from './input-context.service'

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  declarations: [FormatterParserDirective],
  exports: [FormatterParserDirective, ReactiveFormsModule]
})
export class FormatterParserModule {

  static forRoot(): ModuleWithProviders {
    return {
      ngModule: FormatterParserModule,
      providers: [
        FormatterParserCollectorService,
        FormatterParser,
        InputContextService
      ]
    };
  }
}
