import { Option } from 'ui-frontend-common';

export const USAGES: Option[] = [
  { key: 'all', label: 'Tous les usages', info: ''}
];

export interface  PreservationPolicyMetadata {
  usage: string[];
  initialVersionConservation: string[];
  versionsToPreserve: string[];
}

export const RETENTION_POLICY_DEFAULT: PreservationPolicyMetadata[] = [
  {
    usage: ['Tous les usages'],
    initialVersionConservation: ['oui'],
    versionsToPreserve: ['Toutes']
  }
];
