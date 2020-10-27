export interface SearchCriteriaValue {
    value?: string;
    valueShown?: boolean;
};

export interface SearchCriteria {
    key: string;
    label: string;
    values?: SearchCriteriaValue[];
};
 