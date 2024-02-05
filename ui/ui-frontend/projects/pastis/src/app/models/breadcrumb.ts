import { FileNode } from './file-node';

export interface BreadcrumbDataTop {
  label: string;
  url?: string;
  external?: boolean;
}

export interface BreadcrumbDataMetadata {
  label: string;
  node?: FileNode;
}
