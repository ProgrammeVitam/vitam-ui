import { Id } from '../../app/modules';

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
  typeDetail: string;
  stringSize: string;
  collections: Array<string>;
}
