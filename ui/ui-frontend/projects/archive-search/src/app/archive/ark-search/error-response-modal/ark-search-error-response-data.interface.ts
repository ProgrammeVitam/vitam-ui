export interface ArkSearchErrorResponseData {
  arkId: string;
  status: ArkStatus;
}

export enum ArkStatus {
  TRANSFERRED = 'TRANSFERRED',
  ELIMINATED = 'ELIMINATED',
}
