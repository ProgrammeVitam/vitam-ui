import { Unit } from "../../../core/models/unit.interface";

export interface SearchResponse<T = Unit> {
  $hits: any;
  $results: T[];
  $facetResults?: any[];
}
