import {Option} from 'ui-frontend-common';

export const LOGBOOK_OPERATION_CATEGORIES: Option[] = [
    {key: 'AUDIT', label: 'Audit', info: ''},
    {key: 'MASTERDATA', label: 'Données de base', info: ''},
    {key: 'ELIMINATION', label: 'Élimination', info: ''},
    {key: 'INGEST', label: 'Entrée', info: ''},
    {key: 'EXPORT_DIP', label: 'Export DIP', info: ''},
    {key: 'UPDATE', label: 'Mise à jour', info: ''},
    {key: 'PRESERVATION', label: 'Préservation', info: ''},
    {key: 'RECLASSIFICATION', label: 'Réorganisation', info: ''},
    {key: 'STORAGE_BACKUP', label: 'Sauvegarde d\'écriture', info: ''},
    {key: 'TRACEABILITY', label: 'Sécurisation', info: ''},
    {key: 'CHECK', label: 'Vérification', info: ''},
    {key: 'EXTERNAL_LOGBOOK', label: 'Journalisation externe', info: ''}
];

export const NULL_TYPE: Option[] = [
    {key: null, label: 'Tous', info: ''}
];
