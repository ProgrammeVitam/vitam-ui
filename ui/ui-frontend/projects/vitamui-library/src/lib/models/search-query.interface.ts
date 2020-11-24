import {SearchCriteria} from './search-criteria.interface';

export interface SearchQuery {
  criteria: SearchCriteria;
  projectionFields?: string[];
}
