import {Id} from 'ui-frontend-common';

export interface Ontology extends Id {
  id: string;
  tenant: number;
  version: number;
  identifier: string;
  shortName: string;
  description: string;
  creationDate: string;
  lastUpdate: string;
  sedaField: string;
  apiField: string;
  type: string;
  origin: string;
  collections: Array<string>;
}
