import { AggregationOperationType } from './aggregation-operation-type';
import { Direction } from './direction.enum';
import { PageRequest } from './page-request.model';

export class RequestParam extends PageRequest {
  /**
   * aggregation groups
   */
  groups: { fields: string[]; operator: AggregationOperationType; fieldOperator?: string };

  /**
   * response excluded fields
   */
  excludeFields: string[];

  constructor(
    page: number,
    size: number,
    orderBy: string,
    direction: Direction,
    criteria: string,
    groups: { fields: string[]; operator: AggregationOperationType; fieldOperator?: string },
  ) {
    super(page, size, orderBy, direction, criteria);
    this.groups = groups;
  }

  /**
   * get as URL params
   */
  get httpParams() {
    // @ts-ignore
    let params = super.httpParams;
    if (this.groups) {
      params = params.set('operator', this.groups.operator);

      for (const field of this.groups.fields) {
        params = params.append('fields', field);
      }

      if (this.groups.fieldOperator) {
        params = params.append('fieldOperator', this.groups.fieldOperator);
      }
    }
    for (const excludefield of this.excludeFields ?? []) {
      params = params.append('excludeFields', excludefield);
    }
    return params;
  }
}
