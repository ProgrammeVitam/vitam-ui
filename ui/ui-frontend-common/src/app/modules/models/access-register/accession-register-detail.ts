import { Id } from '../id.interface';
import { AccessionRegisterStatus } from './accession-register-status';
import { RegisterValueDetailModel } from './register-value-detail-model';
import { RegisterValueEventModel } from './register-value-event-model';

export interface AccessionRegisterDetail extends Id {
  tenant: number;
  version: number;
  originatingAgency: string;
  submissionAgency: string;
  originatingAgencyLabel: string;
  archivalAgreement: string;
  startDate: string;
  endDate: string;
  lastUpdate: string;
  opi: string;
  opc: string;
  acquisitionInformation: string;
  events: RegisterValueEventModel[];
  status: AccessionRegisterStatus;
  objectSize: RegisterValueDetailModel;
  totalObjectsGroups: RegisterValueDetailModel;
  totalObjects: RegisterValueDetailModel;
  totalUnits: RegisterValueDetailModel;
  operationsIds: string[];
  archivalProfile?: string;
  operationType: string;
  legalStatus?: string;
  obIdIn: string;
  comment: string[];
}
