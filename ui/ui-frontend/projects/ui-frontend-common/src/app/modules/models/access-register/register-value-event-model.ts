export interface RegisterValueEventModel {
  Opc: string;
  OpType: RegisterValueEventType;
  Gots: number;
  Units: number;
  Objects: number;
  ObjSize: number;
  CreationDate: string;
}

export enum RegisterValueEventType {
  INGEST = 'INGEST',
  PRESERVATION = 'PRESERVATION',
  TRANSFER_REPLY = 'TRANSFER_REPLY',
  ELIMINATION = 'ELIMINATION',
}
