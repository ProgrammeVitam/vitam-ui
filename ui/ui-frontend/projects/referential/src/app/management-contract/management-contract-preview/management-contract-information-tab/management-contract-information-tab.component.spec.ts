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
import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { BASE_URL, InjectorModule, IntermediaryVersionEnum, LoggerModule, ManagementContract, WINDOW_LOCATION } from 'vitamui-library';
import { InputStubComponent, VitamUICommonTestModule } from 'vitamui-library/testing';
import { ManagementContractService } from '../../management-contract.service';
import { ManagementContractInformationTabComponent } from './management-contract-information-tab.component';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { By } from '@angular/platform-browser';

describe('ManagementContractInformationTabComponent', () => {
  let component: ManagementContractInformationTabComponent;
  let fixture: ComponentFixture<ManagementContractInformationTabComponent>;

  @Pipe({
    name: 'dateTime',
    standalone: false,
  })
  class DateTimeStubPipe implements PipeTransform {
    transform(value: string = ''): string {
      return value;
    }
  }

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
    patch: (payload: any) => of(payload),
    create: () => of({}),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ManagementContractInformationTabComponent, DateTimeStubPipe],
      schemas: [NO_ERRORS_SCHEMA],
      imports: [
        MatSidenavModule,
        InjectorModule,
        VitamUICommonTestModule,
        TranslateModule.forRoot(),
        RouterTestingModule,
        LoggerModule.forRoot(),
      ],
      providers: [
        FormBuilder,
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: ManagementContractService, useValue: managementContractServiceMock },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
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

  it('should not call patch of ManagementContractService when there is no diff', () => {
    // Given
    const managementContractForm = {
      name: 'Contrat de gestion avec stockage',
      identifier: 'MCDefaultStorageAll',
      description: 'Contrat de gestion valide déclarant pas de surcharge pour le stockage avec la stratégie par défaut',
      status: true,
    };
    component._inputManagementContract = managementContract;
    component.form.setValue(managementContractForm);

    vi.spyOn(managementContractServiceMock, 'patch');

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
    expect(managementContratForm.id).toEqual('contractId');
    expect(managementContratForm.identifier).toEqual('MCDefaultStorageAll');
    expect(managementContratForm.status).toEqual(true);
    expect(managementContratForm.name).toEqual('Contrat de gestion avec stockage');
    expect(managementContratForm.description).toEqual(
      'Contrat de gestion valide déclarant pas de surcharge pour le stockage avec la stratégie par défaut',
    );
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
    vi.spyOn(managementContractServiceMock, 'get');
    vi.spyOn(managementContractServiceMock, 'patch');

    // When
    component.onSubmit();

    // Then
    expect(managementContractServiceMock.get).toHaveBeenCalled();
    expect(managementContractServiceMock.patch).toHaveBeenCalled();
  });

  describe('DOM', () => {
    it('should have 1 vitamui input [textarea]=true', () => {
      const elementVitamTextArea = fixture.debugElement
        .queryAll(By.directive(InputStubComponent))
        .filter((input) => input.componentInstance.textarea);
      expect(elementVitamTextArea.length).toBeGreaterThanOrEqual(0);
    });

    it('should have 2 vitamui input [textarea]=false', () => {
      const elementVitamUiInput = fixture.debugElement
        .queryAll(By.directive(InputStubComponent))
        .filter((input) => !input.componentInstance.textarea);
      expect(elementVitamUiInput.length).toBeGreaterThanOrEqual(0);
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
    vi.spyOn(managementContractServiceMock, 'patch');

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
    vi.spyOn(managementContractServiceMock, 'patch');

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
      deactivationDate: expect.any(String),
    });
  });
});
