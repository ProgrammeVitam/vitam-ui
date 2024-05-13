import { Unit } from 'vitamui-library';

export interface SearchResponse<T = Unit> {
  $hits: any;
  $results: T[];
  $facetResults?: any[];
}
