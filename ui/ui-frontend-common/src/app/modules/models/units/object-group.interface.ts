/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

import { ObjectQualifierType } from './unit.enums';

// TODO: rename in ObjectGroup
/** Object associated to a unit */
export interface ApiUnitObject {
  '#id': string;
  '#tenant': string;
  '#unitups': Array<string>;
  '#allunitups': Array<string>;
  '#operations': Array<string>;
  '#opi': string;
  '#originating_agency': string;
  '#originating_agencies': Array<string>;
  '#storage': StorageDto;
  '#nbobjects': StorageDto;
  'FileInfo': FileInfoDto;
  '#qualifiers': Array<QualifierDto>;
  '#approximate_creation_date': string;
  '#approximate_update_date': string;
}

export interface QualifierDto {
  qualifier: ObjectQualifierType;
  '#nbc': string;
  versions: Array<VersionDto>;
}

export interface VersionDto {
  '#rank': string;
  '#id': string;
  DataObjectVersion: string;
  DataObjectGroupId: string;
  FormatIdentification: FormatIdentificationDto;
  FileInfo: FileInfoDto;
  Metadata: MetadataDto;
  Size: number;
  Uri: string;
  MessageDigest: string;
  Algorithm: string;
  '#storage': StorageDto;
  PhysicalDimensions: PhysicalDimensionsDto;
  PhysicalId: string;
  OtherMetadata: Map<string, any>;
  '#opi': string;
  DataObjectProfile: string;
}

/** Not returned by API. */
export interface VersionWithQualifierDto extends VersionDto {
  qualifier: ObjectQualifierType;
  version: number;
  opened: boolean;
}

export interface MetadataDto {
  Document: Map<string, any>;
  Text: Map<string, any>;
  Image: Map<string, any>;
  Audio: Map<string, any>;
  Video: Map<string, any>;
}

export interface StorageDto {
  strategyId: string;
  '#nbc': string;
  offerIds: string;
}

export interface FileInfoDto {
  Filename: string;
  CreatingApplicationName: string;
  CreatingApplicationVersion: string;
  CreatingOs: string;
  CreatingOsVersion: string;
  LastModified: string;
  DateCreatedByApplication: string;
}

export interface FormatIdentificationDto {
  FormatLitteral: string;
  MimeType: string;
  FormatId: string;
  Encoding: string;
}

export interface PhysicalDimensionsDto {
  Width: MeasurementDto;
  Height: MeasurementDto;
  Depth: MeasurementDto;
  Shape: string;
  Diameter: MeasurementDto;
  Length: MeasurementDto;
  Thickness: MeasurementDto;
  Weight: MeasurementDto;
  NumberOfPage: number;
}

export interface MeasurementDto {
  dValue: number;
  unit: string;
}
