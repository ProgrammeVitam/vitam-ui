import { AccessionRegisterStatus } from './accession-register-status';
import { RegisterValueDetailModel } from './register-value-detail-model';
import { RegisterValueEventModel } from './register-value-event-model';
import {Id} from "../id.interface";

export interface AccessionRegisterDetail extends Id {
  tenant: number;
  version: number;
  originatingAgency: string;
  submissionAgency: string;
  archivalAgreement: string;
  archivalProfile?: string;
  originatingAgencyLabel: string;
  startDate: string;
  endDate: string;
  lastUpdate: string;
  opi: string;
  opc: string;
  operationType: string;
  acquisitionInformation: string;
  events: RegisterValueEventModel[];
  status: AccessionRegisterStatus;
  objectSize: RegisterValueDetailModel;
  totalObjectsGroups: RegisterValueDetailModel;
  totalObjects: RegisterValueDetailModel;
  totalUnits: RegisterValueDetailModel;
  operationsIds: string[];
  legalStatus?: string;
}
