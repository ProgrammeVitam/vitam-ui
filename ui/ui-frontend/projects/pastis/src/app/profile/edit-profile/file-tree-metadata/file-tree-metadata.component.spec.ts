/*
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
 *
 *
 */
import { APP_BASE_HREF } from '@angular/common';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ToastrModule } from 'ngx-toastr';
import { BASE_URL, LoggerModule, WINDOW_LOCATION } from 'ui-frontend-common';
import { PastisApiService } from '../../../core/api/api.pastis.service';
import { PastisConfiguration } from '../../../core/classes/pastis-configuration';
import { FileService } from '../../../core/services/file.service';
import { ProfileService } from '../../../core/services/profile.service';
import { SedaService } from '../../../core/services/seda.service';
import { DataTypeConstants, FileNode, TypeConstants, ValueOrDataConstants } from '../../../models/file-node';
import { MetadataHeaders } from '../../../models/models';
import { ProfileMode } from '../../../models/profile-response';
import { SedaBoolean, SedaCardinality, SedaCollections, SedaData } from '../../../models/seda-data';
import { FileTreeMetadataComponent } from './file-tree-metadata.component';
import { FileTreeMetadataService } from './file-tree-metadata.service';

describe('FileTreeMetadataComponent', () => {
  let component: FileTreeMetadataComponent;
  let fixture: ComponentFixture<FileTreeMetadataComponent>;
  let fileService: FileService;
  let sedaService: SedaService;
  let profileService: ProfileService;
  const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['open', 'close']);
  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open', 'close']);
  const metadataHeaders: MetadataHeaders = {
    id: 0,
    nomDuChamp: '',
    nomDuChampFr: '',
    nomDuChampEdit: '',
    type: '',
    valeurFixe: '',
    cardinalite: [],
    commentaire: '',
    enumeration: [],
  };
  const PA_MANDATORY_ENUM_FIELDS = [
    'NeedAuthorization',
    'LegalStatus',
    'DescriptionLevel',
    'KeywordType',
    'PreventInheritance',
    'FinalAction',
    'NeedReassessingAuthorization',
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FileTreeMetadataComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      providers: [
        FileTreeMetadataService,
        FileService,
        ProfileService,
        PastisApiService,
        PastisConfiguration,
        SedaService,
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: APP_BASE_HREF, useValue: '/' },
      ],
      imports: [
        HttpClientTestingModule,
        ToastrModule.forRoot({
          positionClass: 'toast-bottom-right',
        }),
        MatSnackBarModule,
        RouterModule.forRoot([]),
        LoggerModule.forRoot(),
        TranslateModule.forRoot({}),
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fileService = TestBed.inject(FileService);
    sedaService = TestBed.inject(SedaService);
    profileService = TestBed.inject(ProfileService);

    profileService.profileMode = ProfileMode.PUA;

    fixture = TestBed.createComponent(FileTreeMetadataComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    // expected
    expect(component).toBeTruthy();
  });

  it('should return enumeration value', () => {
    // expected
    metadataHeaders.type = 'enumeration';
    PA_MANDATORY_ENUM_FIELDS.forEach((fieldName) => {
      metadataHeaders.nomDuChamp = fieldName;
      expect(component.getMetadataInputType(metadataHeaders)).toEqual('enumeration');
    });
    // unexpected
    metadataHeaders.nomDuChamp = 'StartDate';
    metadataHeaders.type = 'date';
    expect(component.getMetadataInputType(metadataHeaders)).not.toEqual('enumeration');
  });

  it('should return date value', () => {
    // expected
    metadataHeaders.nomDuChamp = 'StartDate';
    metadataHeaders.type = 'date';
    expect(component.getMetadataInputType(metadataHeaders)).toEqual('date');
    // unexpected
    metadataHeaders.nomDuChamp = 'Compressed';
    metadataHeaders.type = 'boolean';
    expect(component.getMetadataInputType(metadataHeaders)).not.toEqual('date');
  });

  it('should return empty string value', () => {
    // expected
    metadataHeaders.nomDuChamp = 'Compressed';
    metadataHeaders.type = 'boolean';
    expect(component.getMetadataInputType(metadataHeaders)).toEqual('');
    // unexpected
    metadataHeaders.nomDuChamp = 'StartDate';
    metadataHeaders.type = 'date';
    expect(component.getMetadataInputType(metadataHeaders)).not.toEqual('');
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should be candidate to additional properties toggling', () => {
    const fileNode: FileNode = {
      additionalProperties: true,
      id: 1,
      parentId: 0,
      name: 'Fake FileNode',
      groupOrChoice: '',
      choices: '',
      valueOrData: ValueOrDataConstants.value,
      value: 'idk',
      type: TypeConstants.element,
      dataType: DataTypeConstants.string,
      cardinality: SedaCardinality.ZERO_OR_ONE,
      level: 1,
      children: [],
      parent: null,
      sedaData: {
        Cardinality: SedaCardinality.ZERO_OR_ONE,
        Children: [],
        Choice: SedaBoolean.YES,
        Collection: SedaCollections.OBJECT,
        Definition: '',
        Element: '',
        Enumeration: [],
        Extensible: SedaBoolean.YES,
        Name: 'Fake Seda Node',
        NameFr: 'Faux Noeud Seda',
        Type: 'object',
      },
    };

    expect(component.canEnableAdditionalPropertiesEdition(fileNode)).toBeTruthy();
  });

  it('should not be candidate to additional properties toggling', () => {
    const fileNode: FileNode = {
      additionalProperties: true,
      id: 1,
      parentId: 0,
      name: 'Fake FileNode',
      groupOrChoice: '',
      choices: '',
      valueOrData: ValueOrDataConstants.value,
      value: 'idk',
      type: TypeConstants.element,
      dataType: DataTypeConstants.string,
      cardinality: SedaCardinality.ZERO_OR_ONE,
      level: 1,
      children: [],
      parent: null,
      sedaData: {
        Cardinality: SedaCardinality.ZERO_OR_ONE,
        Children: [],
        Choice: SedaBoolean.YES,
        Collection: SedaCollections.OBJECT,
        Definition: '',
        Element: '',
        Enumeration: [],
        Extensible: SedaBoolean.NO,
        Name: 'Fake Seda Node',
        NameFr: 'Faux Noeud Seda',
        Type: 'object',
      },
    };

    expect(component.canEnableAdditionalPropertiesEdition(fileNode)).toBeFalsy();
  });

  it('should reset controls', () => {
    component.arrayControl = ['aaa'];
    component.enumerationControl = true;
    component.expressionControl = true;
    component.enumsControlSeleted = ['bbb'];
    component.editedEnumControl = ['ccc'];
    component.openControls = true;
    component.regex = '^w+$';
    component.customRegex = '^w+$';
    component.enumerationsSedaControl = ['ddd'];

    component.resetControls();

    expect(component.arrayControl).toEqual([]);
    expect(component.enumerationControl).toEqual(false);
    expect(component.expressionControl).toEqual(false);
    expect(component.enumsControlSeleted).toEqual([]);
    expect(component.editedEnumControl).toEqual([]);
    expect(component.openControls).toEqual(false);
    expect(component.regex).toEqual(undefined);
    expect(component.customRegex).toEqual(undefined);
    expect(component.enumerationsSedaControl).toEqual([]);
  });

  it('should create component with file node filled', () => {
    expect(sedaService).toBeDefined('Seda service is not defined');
    expect(sedaService.sedaRules).toBeDefined('Seda rules are not defined');

    const sedaRule: SedaData = sedaService.sedaRules[0];

    expect(sedaRule).toBeDefined('First seda rules tree is not defined');
    expect(sedaRule.Children).toBeDefined('First seda rules tree children are not defined');

    const managementSedaNode: SedaData = sedaService.findSedaChildByName('Management', sedaRule);
    const fileNode: FileNode = {
      additionalProperties: true,
      id: 1,
      parentId: 0,
      name: 'Fake FileNode',
      groupOrChoice: '',
      choices: '',
      valueOrData: ValueOrDataConstants.value,
      value: 'idk',
      type: TypeConstants.element,
      dataType: DataTypeConstants.string,
      cardinality: SedaCardinality.ZERO_OR_ONE,
      level: 1,
      children: [],
      parent: null,
      sedaData: managementSedaNode,
    };

    fileService.nodeChange.next(fileNode);
    fileService.currentTree.next([fileNode]);

    expect(component).toBeTruthy();
    expect(component.clickedNode).toBeDefined('clickedNode is undefined');
  });

  it('should create component in standalone mode', () => {
    component.isStandalone = true;

    expect(component).toBeTruthy();
    expect(component.isStandalone).toBeTruthy();
  });
});
