import {Id} from 'ui-frontend-common';

export interface ManagementContract extends Id {
  tenant: number;
  version: number;
  name: string;
  identifier: string;
  description: string;
  status: string;
  creationDate: string;
  lastUpdate: string;
  activationDate: string;
  deactivationDate: string;
  storage: StorageStrategy;
  versionRetentionPolicyDto: VersionRetentionPolicy;
}

export interface StorageStrategy {
  unitStrategy: string,
  objectGroupStrategy: string,
  objectStrategy: string
}

export interface VersionRetentionPolicy {
  initialVersion: boolean;
  intermediaryVersionEnum: string;
  usages: Set<VersionUsage>;  
}

export interface VersionUsage {
  usageName: string;
  initialVersion: boolean;
  intermediaryVersion: IntermediaryVersionEnum;
}

export enum IntermediaryVersionEnum {
  ALL = "ALL",
  LAST = "LAST",
  NONE = "NONE"
}