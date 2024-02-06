/**
 * aggregation operation types
 */
export enum AggregationOperationType {
  /**
   * GET  distinct values from the aggregation
   */
  DISTINCT = 'DISTINCT',

  /**
   * GET count for fields from the aggregation
   */
  COUNT = 'COUNT',

  /**
   * GET sum for fields from the aggregation
   */
  SUM = 'SUM',
}
