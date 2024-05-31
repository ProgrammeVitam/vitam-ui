import { AlertAnalytics, Application } from 'vitamui-library';
import { ContentTypeEnum } from './content.enum';

export interface Content {
  type: ContentTypeEnum;
  data: AlertAnalytics[] | Application[];
}
