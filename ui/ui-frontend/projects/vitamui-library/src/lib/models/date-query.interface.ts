export type DateType =
  'creationDate' |
  'startDate' |
  'endDate' |
  'digitalizationDate' |
  'sendDate' |
  'admissionDate' |
  'registrationDate' |
  'operationDate' |
  'duaStartDate' |
  'duaEndDate';

export interface DateQuery {
  typeDate: DateType;
}
