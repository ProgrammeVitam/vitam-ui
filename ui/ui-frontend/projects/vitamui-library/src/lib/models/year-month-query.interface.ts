import {DateQuery} from './date-query.interface';

export interface YearMonthQuery extends DateQuery {
  year: number;
  month?: number;
}
