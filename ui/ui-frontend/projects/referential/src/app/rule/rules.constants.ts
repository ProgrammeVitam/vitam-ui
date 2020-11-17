import {Option} from 'ui-frontend-common';

export const RULE_TYPES: Option[] = [
    {key: 'StorageRule', label: 'Durée d’utilité courante', info: ''},
    {key: 'AppraisalRule', label: 'Durée d’utilité administrative', info: ''},
    {key: 'AccessRule', label: 'Délai de communicabilité', info: ''},
    {key: 'ReuseRule', label: 'Durée de réutilisation', info: ''},
    {key: 'DisseminationRule', label: 'Délai de diffusion', info: ''},
    {key: 'ClassificationRule', label: 'Durée de classification', info: ''}
];

export const NULL_TYPE: Option[] = [
    {key: null, label: 'Tous', info: ''}
];

export const RULE_MEASUREMENTS: Option[] = [
    {key: 'Day', label: 'Jour', info: ''},
    {key: 'Month', label: 'Mois', info: ''},
    {key: 'Year', label: 'Année', info: ''}
];
