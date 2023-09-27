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

import { QualifierDto, VersionDto } from './object-group.interface';
import { qualifiersToVersionsWithQualifier } from './object-group.utils';
import { ObjectQualifierType } from './object-qualifier.enums';

describe('unit-object-helper tests', () => {

  it('qualifiersToVersionsWithQualifier tests', () => {
    expect(qualifiersToVersionsWithQualifier(null)).toEqual([]);
    const qualifiers = [
      newQualifier(ObjectQualifierType.DISSEMINATION, [
        newVersion(ObjectQualifierType.DISSEMINATION, 1),
        newVersion(ObjectQualifierType.DISSEMINATION, 2),
      ]),
      newQualifier(ObjectQualifierType.BINARYMASTER, [
        newVersion(ObjectQualifierType.BINARYMASTER, 1),
        newVersion(ObjectQualifierType.BINARYMASTER, 3),
        newVersion(ObjectQualifierType.BINARYMASTER, 2),
      ]),
    ];
    const versionsWithQualifiers = qualifiersToVersionsWithQualifier(qualifiers);
    expect(versionsWithQualifiers.length).toEqual(5);
    expect(versionsWithQualifiers[0].qualifier).toEqual(ObjectQualifierType.BINARYMASTER);
    expect(versionsWithQualifiers[0].version).toEqual(1);
    expect(versionsWithQualifiers[1].qualifier).toEqual(ObjectQualifierType.BINARYMASTER);
    expect(versionsWithQualifiers[1].version).toEqual(2);
    expect(versionsWithQualifiers[2].qualifier).toEqual(ObjectQualifierType.BINARYMASTER);
    expect(versionsWithQualifiers[2].version).toEqual(3);
    expect(versionsWithQualifiers[3].qualifier).toEqual(ObjectQualifierType.DISSEMINATION);
    expect(versionsWithQualifiers[3].version).toEqual(1);
    expect(versionsWithQualifiers[4].qualifier).toEqual(ObjectQualifierType.DISSEMINATION);
    expect(versionsWithQualifiers[4].version).toEqual(2);
  });

  function newQualifier(qualifier: ObjectQualifierType, versions: Array<VersionDto>): QualifierDto {
    return {
      qualifier,
      versions,
      '#nbc': null
    };
  }

  function newVersion(qualifier: ObjectQualifierType, version: number): VersionDto {
    const dataObjectVersion = qualifier + '_' + version;
    return {
      '#id': 'ID-' + dataObjectVersion,
      '#rank': 'RANK-' + dataObjectVersion,
      DataObjectVersion: dataObjectVersion,
      DataObjectGroupId: 'DataObjectGroupId',
      FormatIdentification: null,
      FileInfo: {
        Filename: 'Filename-' + dataObjectVersion,
        CreatingApplicationName: 'CreatingApplicationName',
        CreatingApplicationVersion: 'CreatingApplicationVersion',
        CreatingOs: 'CreatingOs',
        CreatingOsVersion: 'CreatingOsVersion',
        LastModified: 'LastModified',
        DateCreatedByApplication: 'DateCreatedByApplication',
      },
      Metadata: null,
      Size: 1344,
      Uri: 'Uri',
      MessageDigest: 'MessageDigest',
      Algorithm: 'Algorithm',
      PhysicalDimensions: null,
      PhysicalId: 'PhysicalId',
      OtherMetadata: null,
      '#opi': null,
      '#storage': null,
      DataObjectProfile: null
    };
  }

});
