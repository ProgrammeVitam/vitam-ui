import {Unit} from './unit.interface';

export interface SearchResponse<T = Unit> {
  $hits: any;
  $results: T[];
  $facetResults?: any[];
}
