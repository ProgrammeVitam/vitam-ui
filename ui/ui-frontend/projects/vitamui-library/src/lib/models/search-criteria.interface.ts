import {DateType} from './date-query.interface';
import {DateRangeQuery} from './date-range-query.interface';
import {PreciseDateQuery} from './precise-date-query.interface';
import {YearMonthQuery} from './year-month-query.interface';

export interface SearchCriteria {
  textSearch?: string[];
  dateSearch?: Array<DateRangeQuery | YearMonthQuery | PreciseDateQuery>;
  serieSearch?: string[][];
  metadata?: {
    unitType?: string;
    folderIdIn?: string[];
    vtagExists?: boolean;
    descriptionLevel?: string;
    idIn?: string[];
    collectionId?: string;
    archiveUnitsToDestroy?: boolean;
    notCommunicableStatus?: boolean;
    frozenStatus?: string [];
    finalAction?: string[];
    documentType?: string[];
  };
  autocomplete?: string;
  facet?: { typeDate: DateType, years: number[] };
}
