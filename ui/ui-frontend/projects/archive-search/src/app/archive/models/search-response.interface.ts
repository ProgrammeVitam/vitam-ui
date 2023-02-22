import { Unit } from 'ui-frontend-common';

export interface SearchResponse<T = Unit> {
  $hits: any;
  $results: T[];
  $facetResults?: any[];
}
