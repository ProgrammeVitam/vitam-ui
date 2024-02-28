/*
 *
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
 */

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { EMPTY } from 'rxjs';
import { BASE_URL, Context, ContextPermission, InjectorModule, LoggerModule, WINDOW_LOCATION } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';

import { ContextService } from '../../context.service';
import { ContextPermissionTabComponent } from './context-permission-tab.component';

const snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open', 'openFromComponent']);

const permissions: ContextPermission[] = [
  {
    tenant: '3',
    accessContracts: [],
    ingestContracts: [],
  },
  {
    tenant: '11',
    accessContracts: [],
    ingestContracts: [],
  },
  {
    tenant: '1',
    accessContracts: [
      'contract_with_field_EveryDataObjectVersion',
      'contrat_tous_producteur',
      'contrat_producteur1',
      'ContratTNR',
      'contrat_inactif_tous_producteurs',
    ],
    ingestContracts: [
      'ArchivalAgreement1',
      'IC_2194_DIFF',
      'ArchivalAgreement0',
      'ArchivalAgreement0Test',
      'IC_2194',
      'ArchivalAgreementWithProfil',
      'IC-000001',
      'IC_2194_INACTIVE_PROFIL',
    ],
  },
  {
    tenant: '0',
    accessContracts: [
      'DefaultWritePermissions',
      'contrat_EveryOriginatingAgency_false',
      'Air_France',
      'Societe_archeologique_de_Touraine',
      'ContratTNR',
      'AllUpdatesAllowed',
      'AccessContract0',
      'contrat_EveryOriginatingAgency_true',
      'contrat_modification_interdites',
      'contract_with_field_EveryDataObjectVersion',
      'NoUpdatesAllowed',
      'Zimbabwe_Societe_archeologique_du_Zimbabwe',
      'OnlyDescUpdateAllowed',
      'contrat_modification_autorisees',
      'ACForNodeOperations',
      'Zimbabwe_ZIM_archives_nationales',
      'SIA_archives_nationales',
      'Zimbabwe_Air_Zimbabwe',
    ],
    ingestContracts: [
      'ArchivalAgreement1',
      'ArchivalAgreement0',
      'contrat_de_rattachement_TNR',
      'ArchivalAgreement987',
      'ZimbabweArchivalAgreement0',
      'ZimbabweArchivalAgreement1',
      'Accepte_les_objets_non_identifies',
      'Accepte_tous_les_formats',
      'ZimbabweArchivalAgreement0Test',
      'Accepte_formats_liste_blanche',
      'ArchivalAgreement0Test',
      'Contract_with_the_less_fields_as_possible',
      'ArchivalAgreement9872',
      'ArchivalAgreement9871',
      'Minimal_contract_with_the_less_fields_as_possible',
      'sameNAmeIngestContract0',
      'IC-000001',
      'sameNAmeIngestContract1',
      'Rejette_les_objets_non_identifies',
    ],
  },
];

const context: Context = {
  id: 'aegqaaaaaahbzl4naaovqamcuzgxyjyaaaaq',
  name: 'admin-context',
  identifier: 'CT-000001',
  status: 'ACTIVE',
  creationDate: '2022-08-16T10:57:52.168',
  lastUpdate: '2022-08-16T13:12:53.416',
  enableControl: 'false',
  securityProfile: 'admin-security-profile',
  permissions,
  activationDate: 'activationDate',
  deactivationDate: 'deactivationDate',
};

describe('ContextPermissionTabComponent', () => {
  let component: ContextPermissionTabComponent;
  let fixture: ComponentFixture<ContextPermissionTabComponent>;

  const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['open', 'close']);
  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open', 'close']);

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        VitamUICommonTestModule,
        InjectorModule,
        LoggerModule.forRoot(),
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot(),
      ],
      declarations: [ContextPermissionTabComponent],
      providers: [
        FormBuilder,
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: ContextService, useValue: { updated: EMPTY } },
        { provide: MatSnackBar, useValue: snackBarSpy },
        { provide: WINDOW_LOCATION, useValue: window.location },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextPermissionTabComponent);
    component = fixture.componentInstance;
    component.context = context;
    component.previousValue = (): Context => context;
    fixture.detectChanges();
  });

  it('Component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('Should return true when the two permissions arrays are the same', () => {
    // Given
    const permissionsFirstArray: ContextPermission[] = [{ tenant: '1', accessContracts: [], ingestContracts: [] }];
    const permissionsSecondArray: ContextPermission[] = [{ tenant: '1', accessContracts: [], ingestContracts: [] }];

    // Then
    expect(component.samePermission(permissionsFirstArray, permissionsSecondArray)).toBeTruthy();
  });

  it('Should return false when the two arrays are not the same', () => {
    // Given
    const firstArray = ['test1', 'test2', 'test3'];
    const secondArray = ['test1', 'test2', 'test5'];

    // Then
    expect(component.sameArray(firstArray, secondArray)).toBeFalsy();
  });

  it('Should get details about all given ingestContracts', () => {
    // Given
    component.context = context;

    // When
    const ingestContractsNames = [
      'ArchivalAgreement1',
      'IC_2194_DIFF',
      'ArchivalAgreement0',
      'ArchivalAgreement0Test',
      'IC_2194',
      'ArchivalAgreementWithProfil',
      'IC-000001',
      'IC_2194_INACTIVE_PROFIL',
    ];

    // Then
    expect(component.getIngestContractNames(ingestContractsNames)).toBeDefined();
    expect(component.getIngestContractNames(ingestContractsNames).split(',').length).toBe(8);
  });

  it('Should get details about all given accessContracts', () => {
    // Given
    component.context = context;

    // When
    const accessContracts = [
      'contract_with_field_EveryDataObjectVersion',
      'contrat_tous_producteur',
      'contrat_producteur1',
      'ContratTNR',
      'contrat_inactif_tous_producteurs',
    ];

    // Then
    expect(component.getAccessContractNames(accessContracts)).toBeDefined();
    expect(component.getAccessContractNames(accessContracts).split(',').length).toBe(5);
  });

  it('Should return true when we have the same arrays', () => {
    // Given
    const firstArray = ['test1', 'test2', 'test3'];
    const secondArray = ['test1', 'test2', 'test3'];

    // Then
    expect(component.sameArray(firstArray, secondArray)).toBeTruthy();
  });

  it('Should return false when the two permissions arrays are not the same', () => {
    // Given
    const permissionsFirstArray: ContextPermission[] = [{ tenant: '1', accessContracts: [], ingestContracts: [] }];
    const permissionsSecondArray: ContextPermission[] = [{ tenant: '2', accessContracts: [], ingestContracts: [] }];

    // Then
    expect(component.samePermission(permissionsFirstArray, permissionsSecondArray)).toBeFalsy();
  });
});
