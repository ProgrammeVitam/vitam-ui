/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2020)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/
import { PuaData } from './pua-data';
import { SedaData } from './seda-data';

/**
 * Json node data with nested structure. Each node has a name and a value or a list of children
 */
export enum TypeConstants {
  element = 'element',
  attribute = 'attribute',
}

/**
 * Json node data with nested structure. Each node has a name and a value or a list of children
 */
/**
 * Json node data with nested structure. Each node has a name and a value or a list of children
 */
export enum CardinalityConstants {
  'Zero or More' = '0-N',
  'One Or More' = '1-N',
  'Optional' = '0-1',
  'Obligatoire' = '1',
}

/**
 * Json node data with nested structure. Each node has a name and a value or a list of children
 */
export enum DataTypeConstants {
  string = 'string',
  dateTime = 'dateTime',
  dateOrDateTime = 'dateOrDateTime',
  date = 'date',
  ID = 'ID',
  'id' = 'id',
  anyURI = 'anyURI',
  token = 'token',
  tokenType = 'tokenType',
  base64Binary = 'base64Binary',
  positiveInteger = 'positiveInteger',
  boolean = 'boolean',
  decimal = 'decimal',
  int = 'int',
  language = 'language',
  NCName = 'NCName',
  undefined = 'undefined',
}

/**
 * Json node data with nested structure. Each node has a name and a value or a list of children
 */
export enum ValueOrDataConstants {
  value = 'value',
  data = 'data',
  nsName = 'nsName',
  undefined = 'undefined',
}

export enum DateFormatType {
  dateType = 'DateType',
  date = 'date',
  dateTime = 'dateTime',
}

export interface FileNode {
  editName?: string;
  additionalProperties: boolean;
  id: number;
  parentId: number;
  name: string;
  groupOrChoice: string;
  choices: string;
  valueOrData: ValueOrDataConstants;
  value: string;
  type: TypeConstants;
  dataType: DataTypeConstants;
  cardinality: string;
  level: number;
  documentation?: string;
  children: FileNode[];
  parent: FileNode;
  sedaData: SedaData;
  nonEditFileNode?: boolean;
  puaData?: PuaData;
}

// for debug purpose
export function nodeToString(node: FileNode): string {
  if (!node) {
    return;
  }
  return (
    '{' +
    '"name": "' +
    node.name +
    '",' +
    '"cardinality": "' +
    node.cardinality +
    '",' +
    '"children": ' +
    nodesToString(node.children) +
    '}'
  );
}

// for debug purpose
export function nodesToString(nodes: FileNode[]): string {
  if (!nodes || nodes.length < 1) {
    return '[]';
  }
  return '[' + nodes.map((node) => nodeToString(node)).join(',') + ']';
}

export interface FileNodeInsertParams {
  node: FileNode;
  elementsToAdd: SedaData[];
}

export interface FileNodeInsertAttributeParams {
  node: FileNode;
  elementsToAdd: FileNode[];
}

export enum nodeNameToLabel {
  'notice' = 'PROFILE.EDIT_PROFILE.NOTICE_TAB',
  'ArchiveTransfer' = 'PROFILE.EDIT_PROFILE.ENTETE',
  'ManagementMetadata' = 'PROFILE.EDIT_PROFILE.REGLES',
  'DescriptiveMetadata' = 'PROFILE.EDIT_PROFILE.UNITES_ARCHIVES',
  'DataObjectPackage' = 'PROFILE.EDIT_PROFILE.OBJETS',
}
