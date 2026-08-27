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
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';
import { BASE_URL, InjectorModule, IntermediaryVersionEnum, LoggerModule, ManagementContract, WINDOW_LOCATION } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { ManagementContractService } from '../../management-contract.service';
import { ManagementContractStorageTabComponent } from './management-contract-storage-tab.component';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('ManagementContractStorageTabComponent', () => {
  let component: ManagementContractStorageTabComponent;
  let fixture: ComponentFixture<ManagementContractStorageTabComponent>;

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

  const matDialogRefSpy = {
    close: vi.fn().mockName('MatDialogRef.close'),
    keydownEvents: vi.fn().mockName('MatDialogRef.keydownEvents'),
  };
  const matDialogSpy = {
    open: vi.fn().mockName('MatDialog.open'),
  };
  matDialogSpy.open.mockReturnValue({ afterClosed: () => of(true) });

  const managementContractServiceMock = {
    get: () => of({}),
    getAll: () => of([]),
    getAllForTenant: () => of([]),
    exists: () => of(true),
    existsProperties: () => of(true),
    patch: () => of({}),
    create: () => of({}),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [NO_ERRORS_SCHEMA],
      imports: [
        MatSidenavModule,
        InjectorModule,
        VitamUICommonTestModule,
        RouterTestingModule,
        LoggerModule.forRoot(),
        ManagementContractStorageTabComponent,
      ],
      providers: [
        FormBuilder,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: ManagementContractService, useValue: managementContractServiceMock },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ManagementContractStorageTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should not call patch of ManagementContractService when thre is no diff', () => {
    // Given
    const storageStrategyForm = {
      unitStrategy: 'default',
      objectGroupStrategy: 'default',
      objectStrategy: 'default',
    };
    component._inputManagementContract = managementContract;
    component.form.setValue(storageStrategyForm);

    vi.spyOn(managementContractServiceMock, 'patch');

    // When
    component.prepareSubmit();

    // Then

    expect(managementContractServiceMock.patch).not.toHaveBeenCalled();
  });

  it('should return the correct StorageStrategy sended', () => {
    // Given
    component._inputManagementContract = managementContract;

    // When
    const storageStrategy = component.previousValue();

    // Then
    expect(storageStrategy).not.toBeNull();
    expect(storageStrategy.unitStrategy).toEqual('default');
    expect(storageStrategy.objectStrategy).toEqual('default');
    expect(storageStrategy.objectGroupStrategy).toEqual('default');
  });

  it('should call get and patch of ManagementContractService', () => {
    // Given
    const storageStrategyForm = {
      unitStrategy: 'new Name Contrat de gestion avec stockage',
      objectGroupStrategy: 'MCDefaultStorageAll',
      objectStrategy: 'new Contrat de gestion valide déclarant pas de surcharge pour le stockage avec la stratégie par défaut',
    };
    // component.inputManagementContract = managementContract;
    component._inputManagementContract = managementContract;
    component.form.setValue(storageStrategyForm);
    vi.spyOn(managementContractServiceMock, 'get');
    vi.spyOn(managementContractServiceMock, 'patch');

    // When
    component.onSubmit();

    // Then
    expect(managementContractServiceMock.get).toHaveBeenCalled();
    expect(managementContractServiceMock.patch).toHaveBeenCalled();
  });

  describe('DOM', () => {
    it('should have 3 vitamui input', () => {
      const nativeElement = fixture.nativeElement;
      const elementVitamUiInput = nativeElement.querySelectorAll('vitamui-input');
      expect(elementVitamUiInput.length).toBe(3);
    });
  });

  it('should return false', () => {
    // Given
    const storageStrategyForm = {
      unitStrategy: 'new Name Contrat de gestion avec stockage',
      objectGroupStrategy: 'MCDefaultStorageAll',
      objectStrategy: 'new Contrat de gestion valide déclarant pas de surcharge pour le stockage avec la stratégie par défaut',
    };
    component._inputManagementContract = managementContract;
    component.form.setValue(storageStrategyForm);

    // When
    const response = component.unchanged();

    // Then
    expect(response).toBeFalsy();
  });
});
