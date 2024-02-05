import { MissingTranslationHandler, MissingTranslationHandlerParams } from '@ngx-translate/core';

declare interface VitamuiInterpolateParams {
  default: string;
}

export class VitamuiMissingTranslationHandler implements MissingTranslationHandler {
  handle(params: MissingTranslationHandlerParams) {
    const interParams = params.interpolateParams as VitamuiInterpolateParams;
    if (interParams && interParams.default) {
      return interParams.default;
    }

    return params.key;
  }
}
