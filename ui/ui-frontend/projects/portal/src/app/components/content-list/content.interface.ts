import { AlertAnalytics, Application } from 'ui-frontend-common';
import { ContentTypeEnum } from './content.enum';

export interface Content {
  type: ContentTypeEnum;
  data: AlertAnalytics[] | Application[];
}
