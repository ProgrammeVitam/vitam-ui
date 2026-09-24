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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { VitamUICommonTestModule } from 'vitamui-library/testing';

import { Collection, SchemaElement, SchemaService, VitamUILibraryModule } from 'vitamui-library';
import { SchemaInformationTabComponent } from './schema-information-tab.component';
import { of } from 'rxjs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('SchemaInformationTabComponent', () => {
  let component: SchemaInformationTabComponent;
  let fixture: ComponentFixture<SchemaInformationTabComponent>;

  const schemaServiceMock = {
    patch: (_data: any) => of(null),
  };

  const schemaValue: SchemaElement = {
    id: 'id',
    Tenant: 0,
    CreationDate: '01-01-2020',
    LastUpdate: '01-01-2020',
    SedaField: 'MyText',
    ApiField: 'MyText',
    Origin: 'EXTERNAL',
    ShortName: 'Name',
    FieldName: 'SP-000001',
    Type: 'TEXT',
    Collection: Collection.ARCHIVE_UNIT,
    Description: 'Mon Ontologie',
    DataType: 'STRING',
    StringSize: 'MEDIUM',
    Path: 'document_title',
    ApiPath: 'metadata.document_title',
    Category: 'DESCRIPTION',
    CustomSearchTypes: ['fulltext', 'exact'],
    SedaVersions: ['2.1', '2.2', '2.3'],
    Cardinality: 'ONE',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MatFormFieldModule, MatInputModule, MatSelectModule, ReactiveFormsModule, VitamUICommonTestModule, VitamUILibraryModule],
      providers: [FormBuilder, { provide: SchemaService, useValue: schemaServiceMock }],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(SchemaInformationTabComponent);
    component = fixture.componentInstance;
    component.inputSchema = schemaValue;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should patch the form with inputSchema values', () => {
    expect(component.form.get('Path')?.value).toEqual('document_title');
    expect(component.form.get('ApiPath')?.value).toEqual('metadata.document_title');
    expect(component.form.get('ShortName')?.value).toEqual('Name');
    expect(component.form.get('Origin')?.value).toEqual('EXTERNAL');
  });

  it('should set isExternal to true when Origin is EXTERNAL', () => {
    expect(component.isExternal).toBe(true);
  });

  it('should show stringSize when Type is TEXT', () => {
    expect(component.stringSizeVisible).toBe(true);
  });

  it('should show custom search types when Type is not OBJECT and CustomSearchTypes is not empty', () => {
    expect(component.showCustomSearchTypes).toBe(true);
  });
});
