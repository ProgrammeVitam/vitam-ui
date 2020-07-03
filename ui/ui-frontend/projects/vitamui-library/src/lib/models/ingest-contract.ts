import {Id} from 'ui-frontend-common';

export interface IngestContract extends Id {
  tenant: number;
  version: number;
  name: string;
  identifier: string;
  description: string;
  status: string;
  creationDate: string;
  lastUpdate: string;
  activationDate: string;
  deactivationDate: string;
  checkParentLink: string;
  linkParentId: string; // Rattachement SIP
  checkParentId: Array<string>; // UA
  masterMandatory: boolean;
  everyDataObjectVersion: boolean;
  dataObjectVersion: Array<string>;
  formatUnidentifiedAuthorized: boolean;
  everyFormatType: boolean;
  formatType: Array<string>;
  archiveProfiles: Array<string>;
  managementContractId: string;
}
