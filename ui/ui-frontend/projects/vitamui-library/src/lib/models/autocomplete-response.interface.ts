export interface AutocompleteResponse {
  name: 'search_autocompletion';
  buckets: Array<{ value: string, count: number }>;
}
