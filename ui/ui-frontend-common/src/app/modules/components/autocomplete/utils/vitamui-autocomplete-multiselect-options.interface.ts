import { Option } from './option.interface';

export interface VitamuiAutocompleteMultiselectOptions {
  options: Option[];
  customSorting?: (a: Option, b: Option) => number;
}
