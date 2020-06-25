import {Id} from 'ui-frontend-common';

export const FILE_FORMAT_EXTERNAL_PREFIX = 'EXTERNAL_';

export interface FileFormat extends Id {
  // Vitam Version
  documentVersion: number;
  // Format Version
  version: string;
  // Pronom Version
  versionPronom: string;
  name: string;
  description: string;
  puid: string;
  mimeType: string;
  hasPriorityOverFileFormatIDs: string[];
  group: string;
  alert: boolean;
  comment: string;
  extensions: string[];
  createdDate: string;
}
