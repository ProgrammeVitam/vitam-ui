/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { MatLegacySnackBarModule as MatSnackBarModule } from '@angular/material/legacy-snack-bar';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import {
  AccessContract,
  BASE_URL,
  ExternalParameters,
  ExternalParametersService,
  LoggerModule,
  SearchUnitApiService,
  Status,
} from 'vitamui-library';
import { AccessContractNodesTabComponent } from './access-contract-nodes-tab.component';

describe('AccessContractNodesTabComponent', () => {
  let component: AccessContractNodesTabComponent;
  let fixture: ComponentFixture<AccessContractNodesTabComponent>;

  const accessContractValue: AccessContract = {
    tenant: 0,
    version: 1,
    description: 'desc',
    status: 'ACTIVE',
    id: 'vitam_id',
    name: 'Name',
    identifier: 'SP-000001',
    everyOriginatingAgency: true,
    originatingAgencies: ['test'],
    everyDataObjectVersion: true,
    dataObjectVersion: ['test'],
    creationDate: '01-01-20',
    lastUpdate: '01-01-20',
    activationDate: '01-01-20',
    deactivationDate: '01-01-20',
    writingPermission: true,
    writingRestrictedDesc: true,
    accessLog: Status.INACTIVE,
    ruleFilter: true,
    ruleCategoryToFilter: ['rule'],
    rootUnits: [''],
    excludedRootUnits: [''],
    ruleCategoryToFilterForTheOtherOriginatingAgencies: [],
    doNotFilterFilingSchemes: false,
  };

  beforeEach(async () => {
    const parameters: Map<string, string> = new Map<string, string>();
    parameters.set(ExternalParameters.PARAM_ACCESS_CONTRACT, '1');
    const externalParametersServiceMock = {
      getUserExternalParameters: () => of(parameters),
    };

    const unitValueMock = {
      getByDsl: () => of({}),
    };

    await TestBed.configureTestingModule({
      declarations: [AccessContractNodesTabComponent],
      imports: [MatSnackBarModule, HttpClientTestingModule, LoggerModule.forRoot(), TranslateModule.forRoot()],
      providers: [
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ExternalParametersService, useValue: externalParametersServiceMock },
        { provide: SearchUnitApiService, useValue: unitValueMock },
        { provide: MatDialog, useValue: {} },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessContractNodesTabComponent);
    component = fixture.componentInstance;
    component.accessContract = accessContractValue;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
