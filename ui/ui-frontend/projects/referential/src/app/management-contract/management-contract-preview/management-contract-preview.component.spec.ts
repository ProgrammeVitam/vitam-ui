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
import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterTestingModule } from '@angular/router/testing';
import { InjectorModule, IntermediaryVersionEnum, LoggerModule, WINDOW_LOCATION } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { ManagementContractPreviewComponent } from './management-contract-preview.component';

describe('ManagementContractPreviewComponent', () => {
  let component: ManagementContractPreviewComponent;
  let fixture: ComponentFixture<ManagementContractPreviewComponent>;

  @Pipe({ name: 'truncate' })
  class TruncateStubPipe implements PipeTransform {
    transform(value: string = ''): string {
      return value;
    }
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [NO_ERRORS_SCHEMA],
      imports: [
        MatSidenavModule,
        InjectorModule,
        VitamUICommonTestModule,
        RouterTestingModule,
        LoggerModule.forRoot(),
        MatDialogModule,
        ManagementContractPreviewComponent,
        TruncateStubPipe,
      ],
      providers: [
        {
          provide: WINDOW_LOCATION,
          useValue: window.location,
        },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ManagementContractPreviewComponent);
    component = fixture.componentInstance;
    component.inputManagementContract = {
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
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should return the exact array ', () => {
    // Given
    component.tabUpdated = [false, false];
    const expectedArray = [true, false];
    // When
    component.updatedChange(true, 0);

    // Then
    expect(component.tabUpdated).not.toBeNull();
    expect(component.tabUpdated.length).toEqual(2);
    expect(component.tabUpdated).toEqual(expectedArray);
  });
});
