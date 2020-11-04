export enum SearchCriteriaStatusEnum {
    NOT_INCLUDED ='NOT_INCLUDED',
    INCLUDED= 'INCLUDED',
    IN_PROGRESS = 'IN_PROGRESS'
  }

export interface SearchCriteriaValue {
    value?: string;
    valueShown?: boolean;
    status: SearchCriteriaStatusEnum;
};

export interface SearchCriteria {
    key: string;
    label: string;
    values?: SearchCriteriaValue[];
};

export interface OntologySubModel{
    Identifier: string;
    ApiField: string;
    Description: string; 
    Type: string;
    Origin: string;
    CreationDate: any ;
    ShortName: string;
}
 