import {DateQuery} from './date-query.interface';

export interface DateRangeQuery extends DateQuery {

  period: { startDate: Date } | { endDate: Date } | { startDate: Date, endDate: Date };

}
