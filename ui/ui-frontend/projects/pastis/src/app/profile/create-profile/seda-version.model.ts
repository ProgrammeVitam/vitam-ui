export interface SedaOption {
  version: number;
  label: string;
}

export const sedaOptions: SedaOption[] = [
  { version: 2.1, label: 'SEDA 2.1' },
  { version: 2.2, label: 'SEDA 2.2' },
  { version: 2.3, label: 'SEDA 2.3' },
];
