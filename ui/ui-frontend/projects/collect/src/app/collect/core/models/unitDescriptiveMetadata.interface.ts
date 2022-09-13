import { Id } from 'ui-frontend-common';

export interface UnitDescriptiveMetadataDto extends Id {
  Title: string;
  Description: string;
  'Title_.fr': string;
  'Title_.en': string;
  'Description_.fr': string;
  'Description_.en': string;
  DescriptionLevel: string;
  StartDate: string;
  EndDate: string;
  unsetAction: string[];
}
