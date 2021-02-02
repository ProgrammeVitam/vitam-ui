export interface Unit {
  '#id': string;
  '#unitups': string[];
  '#allunitups': string[];
  '#unitType': string;
  '#object'?: string;

  Title?: string;
  Title_?: any;
  Description?: string;
  DescriptionLevel?: string;
  CreatedDate?: Date;
  StartDate?: Date;
  EndDate?: Date;
  AcquiredDate?: Date;
  SentDate?: Date;
  ReceivedDate?: Date;
  RegisteredDate?: Date;
  TransactedDate?: Date;
  DuaStartDate?: Date;
  DuaEndDate?: Date;
  OriginatingAgencyArchiveUnitIdentifier?: string;
  Status?: string;
  Vtag?: Array<{ Key: string[], Value: string[] }>;
  Keyword?: Array<{ KeywordReference: string, KeywordContent: string }>;
  Type?: string;
  PhysicalAgency?: string[];
  PhysicalStatus?: string[];
  PhysicalType?: string[];


  // This does not come from the API. It is built from the unit info
  isDigital?: boolean;
  isPhysical?: boolean;

  [key: string]: any;

}
