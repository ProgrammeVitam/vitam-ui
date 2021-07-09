import {AccessionRegisters} from './accession-registers';
import {AccessionRegisterStatus} from './accession-registers-status';
import {RegisterValueDetailModel} from './register-value-detail-model';
import {RegisterValueEventModel} from './register-value-event-model';

export interface AccessionRegisterDetail extends AccessionRegisters {

  submissionAgency: string;
  archivalAgreement: string;
  originatingAgencyLabel: string;
  startDate: string;
  endDate: string;
  lastUpdate: string;
  opi: string;
  opc: string;
  opType: string;
  acquisitionInformation: string;
  RegisterValueEventModel: RegisterValueEventModel[];
  status: AccessionRegisterStatus;
  objectSize: RegisterValueDetailModel;
  totalObjectsGroups: RegisterValueDetailModel;
  totalObjects: RegisterValueDetailModel;
  totalUnits: RegisterValueDetailModel;
  operationsIds: string[];
}
