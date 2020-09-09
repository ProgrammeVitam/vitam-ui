import {Id} from 'ui-frontend-common';

export interface Rule extends Id {
    id: string;
    tenant: number;
    version: number;
    ruleId: string;
    ruleType: string;
    ruleValue: string;
    ruleDescription: string;
    ruleDuration: string;
    ruleMeasurement: string;
    creationDate: string;
    updateDate: string;
}
