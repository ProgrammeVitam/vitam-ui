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

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { BASE_URL, InjectorModule, IntermediaryVersionEnum, LoggerModule, ManagementContract, WINDOW_LOCATION } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { ManagementContractService } from '../../management-contract.service';
import { ManagementContractInformationTabComponent } from './management-contract-information-tab.component';

describe('ManagementContractInformationTabComponent', () => {
  let component: ManagementContractInformationTabComponent;
  let fixture: ComponentFixture<ManagementContractInformationTabComponent>;

  const managementContract: ManagementContract = {
    id: 'contractId',
    name: 'Contrat de gestion avec stockage',
    identifier: 'MCDefaultStorageAll',
    description: 'Contrat de gestion valide déclarant pas de surcharge pour le stockage avec la stratégie par défaut',
    status: 'ACTIVE',
    lastUpdate: '10/12/2016',
    creationDate: '10/12/2016',
    activationDate: '10/12/2016',
    deactivationDate: '10/12/2016',
    tenant: 10,
    version: 2,
    storage: {
      unitStrategy: 'default',
      objectGroupStrategy: 'default',
      objectStrategy: 'default',
    },
    versionRetentionPolicy: {
      usages: null,
      initialVersion: true,
      intermediaryVersionEnum: IntermediaryVersionEnum.ALL,
    },
  };

  const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close', 'keydownEvents']);
  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
  matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

  const managementContractServiceMock = {
    get: () => of({}),
    getAll: () => of([]),
    getAllForTenant: () => of([]),
    exists: () => of(true),
    existsProperties: () => of(true),
    patch: (payload: any) => of(payload),
    create: () => of({}),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        MatSidenavModule,
        InjectorModule,
        VitamUICommonTestModule,
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        RouterTestingModule,
        LoggerModule.forRoot(),
      ],
      declarations: [ManagementContractInformationTabComponent],
      providers: [
        FormBuilder,
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: ManagementContractService, useValue: managementContractServiceMock },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ManagementContractInformationTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should not call patch of ManagementContractService when thre is no diff', () => {
    // Given
    const managementContractForm = {
      name: 'Contrat de gestion avec stockage',
      identifier: 'MCDefaultStorageAll',
      description: 'Contrat de gestion valide déclarant pas de surcharge pour le stockage avec la stratégie par défaut',
      status: true,
    };
    component._inputManagementContract = managementContract;
    component.form.setValue(managementContractForm);

    spyOn(managementContractServiceMock, 'patch').and.callThrough();

    // When
    component.prepareSubmit();

    // Then

    expect(managementContractServiceMock.patch).not.toHaveBeenCalled();
  });

  it('should return the correct managementContract sended', () => {
    // Given
    component._inputManagementContract = managementContract;

    // When
    const managementContratForm = component.previousValue();

    // Then
    expect(managementContratForm).not.toBeNull();
    expect(managementContratForm.status).toEqual(true);
    expect(managementContratForm.name).toEqual('Contrat de gestion avec stockage');
    expect(managementContratForm.version).toEqual(2);
    expect(managementContratForm.storage.objectGroupStrategy).toEqual('default');
  });

  it('should call get and patch of ManagementContractService', () => {
    // Given
    const managementContractForm = {
      name: 'new Name Contrat de gestion avec stockage',
      identifier: 'MCDefaultStorageAll',
      description: 'new Contrat de gestion valide déclarant pas de surcharge pour le stockage avec la stratégie par défaut',
      status: true,
    };
    component._inputManagementContract = managementContract;
    component.form.setValue(managementContractForm);
    spyOn(managementContractServiceMock, 'get').and.callThrough();
    spyOn(managementContractServiceMock, 'patch').and.callThrough();

    // When
    component.onSubmit();

    // Then
    expect(managementContractServiceMock.get).toHaveBeenCalled();
    expect(managementContractServiceMock.patch).toHaveBeenCalled();
  });

  describe('DOM', () => {
    it('should have 1 vitamui common textarea', () => {
      const nativeElement = fixture.nativeElement;
      const elementVitamTextArea = nativeElement.querySelectorAll('vitamui-common-textarea');
      expect(elementVitamTextArea.length).toBe(1);
    });

    it('should have 6 rows', () => {
      const nativeElement = fixture.nativeElement;
      const elementRow = nativeElement.querySelectorAll('.row');
      expect(elementRow.length).toBe(6);
    });

    it('should have 3 columns', () => {
      const nativeElement = fixture.nativeElement;
      const elementRow = nativeElement.querySelectorAll('.col-10');
      expect(elementRow.length).toBe(3);
    });

    it('should have 2 vitamui common input', () => {
      const nativeElement = fixture.nativeElement;
      const elementVitamUiInput = nativeElement.querySelectorAll('vitamui-common-input');
      expect(elementVitamUiInput.length).toBe(2);
    });
  });

  it('should return false', () => {
    // Given
    const managementContractForm = {
      name: 'new Name Contrat de gestion avec stockage',
      identifier: 'MCDefaultStorageAll',
      description: 'new Contrat de gestion valide déclarant pas de surcharge pour le stockage avec la stratégie par défaut',
      status: true,
    };
    component._inputManagementContract = managementContract;
    component.form.setValue(managementContractForm);

    // When
    const response = component.unchanged();

    // Then
    expect(response).toBeFalsy();
  });

  it('should not patch activation/deactivation date when status is not changed', () => {
    spyOn(managementContractServiceMock, 'patch').and.callThrough();

    // Given
    component.inputManagementContract = managementContract;

    // When
    component.form.setValue({
      identifier: 'MCDefaultStorageAll',
      name: 'Management contract name updated',
      description: 'Management contract description updated',
      status: true,
    });
    component.onSubmit();

    // Then
    expect(managementContractServiceMock.patch).toHaveBeenCalledWith({
      id: 'contractId',
      identifier: 'MCDefaultStorageAll',
      name: 'Management contract name updated',
      description: 'Management contract description updated',
      // status not changed and should not be present.
    });
  });

  it('should patch activation/deactivation date when status changed', () => {
    spyOn(managementContractServiceMock, 'patch').and.callThrough();

    // Given
    component.inputManagementContract = managementContract;

    // When
    component.form.setValue({
      identifier: 'MCDefaultStorageAll',
      name: 'Management contract name updated',
      description: 'Management contract description updated',
      status: false,
    });
    component.onSubmit();

    // Then
    expect(managementContractServiceMock.patch).toHaveBeenCalledWith({
      id: 'contractId',
      identifier: 'MCDefaultStorageAll',
      name: 'Management contract name updated',
      description: 'Management contract description updated',
      status: 'INACTIVE',
      activationDate: null,
      deactivationDate: jasmine.any(String),
    });
  });
});
