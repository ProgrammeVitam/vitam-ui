/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */

import { TestBed } from '@angular/core/testing';
import { SchemaService } from './schema.service';
import { of } from 'rxjs';
import { Schema, SchemaElement } from '../models';
import { SchemaApiService } from '../api/schema-api.service';
import { ItemNode } from '../components/autocomplete';

describe('SchemaService', () => {
  const schema: Schema[] = [
    [
      {
        id: null,
        Tenant: null,
        Version: null,
        Path: 'AcquiredDate',
        StringSize: null,
        Cardinality: 'ONE',
        FieldName: 'AcquiredDate',
        ShortName: 'Date de numérisation',
        Description: 'Mapping : unit-es-mapping.json. Références : ARKMS.DateAcquired',
        CreationDate: null,
        LastUpdate: null,
        SedaField: 'AcquiredDate',
        ApiField: 'AcquiredDate',
        Type: 'DATE',
        Origin: 'INTERNAL',
        Collection: 'ARCHIVE_UNIT',
        SedaVersions: ['2.1', '2.2', '2.3'],
        RootPaths: null,
        Category: 'DESCRIPTION',
        ApiPath: 'AcquiredDate',
        DataType: 'DATETIME',
        Control: null,
        EffectiveCardinality: null,
      } as SchemaElement,
      {
        id: null,
        Tenant: null,
        Version: null,
        Path: 'Addressee',
        StringSize: null,
        Cardinality: 'MANY',
        FieldName: 'Addressee',
        ShortName: 'Destinataire',
        Description: null,
        CreationDate: null,
        LastUpdate: null,
        SedaField: null,
        ApiField: 'Addressee',
        Type: 'OBJECT',
        Origin: 'INTERNAL',
        Collection: 'ARCHIVE_UNIT',
        SedaVersions: ['2.1', '2.2', '2.3'],
        RootPaths: null,
        Category: 'DESCRIPTION',
        ApiPath: 'Addressee',
        DataType: 'OBJECT',
        Control: null,
        EffectiveCardinality: null,
      } as SchemaElement,
      {
        id: null,
        Tenant: null,
        Version: null,
        Path: 'Addressee.Activity',
        StringSize: 'SHORT',
        Cardinality: 'MANY',
        FieldName: 'Activity',
        ShortName: 'Activité',
        Description:
          "Mapping : unit-es-mapping.json. En plus des balises Tag et Keyword, il est possible d'indexer les objets avec des éléments pré-définis : Activité.",
        CreationDate: null,
        LastUpdate: null,
        SedaField: 'Activity',
        ApiField: 'Activity',
        Type: 'TEXT',
        Origin: 'INTERNAL',
        Collection: 'ARCHIVE_UNIT',
        SedaVersions: ['2.1', '2.2', '2.3'],
        RootPaths: null,
        Category: 'DESCRIPTION',
        ApiPath: 'Addressee.Activity',
        DataType: 'STRING',
        Control: null,
        EffectiveCardinality: null,
      } as SchemaElement,
      {
        id: null,
        Tenant: null,
        Version: null,
        Path: 'Addressee.BirthDate',
        StringSize: null,
        Cardinality: 'ONE',
        FieldName: 'BirthDate',
        ShortName: 'Date de naissance',
        Description: 'Mapping : unit-es-mapping.json',
        CreationDate: null,
        LastUpdate: null,
        SedaField: 'BirthDate',
        ApiField: 'BirthDate',
        Type: 'DATE',
        Origin: 'INTERNAL',
        Collection: 'ARCHIVE_UNIT',
        SedaVersions: ['2.1', '2.2', '2.3'],
        RootPaths: null,
        Category: 'DESCRIPTION',
        ApiPath: 'Addressee.BirthDate',
        DataType: 'DATETIME',
        Control: null,
        EffectiveCardinality: null,
      } as SchemaElement,
      {
        id: null,
        Tenant: null,
        Version: null,
        Path: 'Blob8',
        StringSize: null,
        Cardinality: 'ONE_REQUIRED',
        FieldName: 'Blob8',
        ShortName: 'blob',
        Description: 'blob',
        CreationDate: null,
        LastUpdate: null,
        SedaField: null,
        ApiField: null,
        Type: 'OBJECT',
        Origin: 'EXTERNAL',
        Collection: 'ARCHIVE_UNIT',
        SedaVersions: null,
        RootPaths: null,
        Category: 'DESCRIPTION',
        ApiPath: 'Blob8',
        DataType: 'OBJECT',
        Control: null,
        EffectiveCardinality: null,
      } as SchemaElement,
      {
        id: null,
        Tenant: null,
        Version: null,
        Path: 'Blob8.MyDate',
        StringSize: null,
        Cardinality: 'ONE',
        FieldName: 'MyDate',
        ShortName: 'blob',
        Description: 'Extension au SEDA. Elément de type date',
        CreationDate: null,
        LastUpdate: null,
        SedaField: null,
        ApiField: null,
        Type: 'DATE',
        Origin: 'EXTERNAL',
        Collection: 'ARCHIVE_UNIT',
        SedaVersions: null,
        RootPaths: null,
        Category: 'DESCRIPTION',
        ApiPath: 'Blob8.MyDate',
        DataType: 'DATETIME',
        Control: null,
        EffectiveCardinality: null,
      } as SchemaElement,
    ],
  ];

  let schemaService: SchemaService;
  const schemaApiServiceMock = {
    getSchemas: of(schema),
    getArchiveUnitProfileSchema: of({}),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SchemaService, { provide: SchemaApiService, useValue: schemaApiServiceMock }],
    });
    schemaService = TestBed.inject(SchemaService);
  });

  it('should be created', () => {
    expect(schemaService).toBeTruthy();
  });

  describe('getDescriptiveSchemaTree', () => {
    const nodes: ItemNode<SchemaElement>[] = [];
    it('should ', () => {
      const result = schemaService.getDescriptiveSchemaTree();
      result.subscribe((results) => {
        expect(results).toEqual(nodes);
      });
    });
  });
});
