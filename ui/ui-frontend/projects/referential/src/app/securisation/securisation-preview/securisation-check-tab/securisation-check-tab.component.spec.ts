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
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {ActivatedRoute} from '@angular/router';

import { NO_ERRORS_SCHEMA } from '@angular/core';
import {of} from 'rxjs';
import {AccessContractService} from '../../../access-contract/access-contract.service';
import {SecurisationService} from '../../securisation.service';
import { SecurisationCheckTabComponent } from './securisation-check-tab.component';

describe('SecurisationCheckTabComponent', () => {
  let component: SecurisationCheckTabComponent;
  let fixture: ComponentFixture<SecurisationCheckTabComponent>;

  const securisationValue = {
    id: 'id',
    idAppSession: 'idAppSession',
    idRequest: 'idRequest',
    parentId: 'parentId',
    type: 'type',
    typeProc: 'typeProc',
    dateTime: new Date('1995-12-17'),
    outcome: 'outcome',
    outDetail: 'outDetail',
    outMessage: 'outMessage',
    data: 'data',
    parsedData: {
      Size: 2
    },
    objectId: 'objectId',
    collectionName: 'collectionName',
    agId: 'agId',
    agIdApp: 'agIdApp',
    agIdExt: 'agIdExt',
    rightsStatementIdentifier: 'rightsStatementIdentifier',
    obIdReq: 'obIdReq',
    events: [{
      id: 'id2',
      idAppSession: 'idAppSession2',
      idRequest: 'idRequest2',
      parentId: 'id',
      type: 'type',
      obIdReq: 'obIdReq',
      typeProc: 'typeProc',
      dateTime: new Date('1995-12-17'),
      outcome: 'outcome',
      outDetail: 'outDetail',
      outMessage: 'outMessage',
      data: 'data',
      parsedData: {
        dataKey: 'dataValue'
      },
      objectId: 'objectId',
      collectionName: 'collectionName',
      agId: 'agId',
      agIdApp: 'agIdApp',
      agIdExt: 'agIdExt',
      rightsStatementIdentifier: 'rightsStatementIdentifier'
    }]
  };

  beforeEach(waitForAsync(() => {
    const activatedRouteMock = {
      params: of({tenantIdentifier: 1}),
      data: of({appId: 'TRACEABILITY_APP'})
    };

    const accessContractServiceMock = {
      getAllForTenant: () => of([])
    };

    TestBed.configureTestingModule({
      declarations: [ SecurisationCheckTabComponent ],
      providers: [
        {provide: AccessContractService, useValue: accessContractServiceMock},
        {provide: SecurisationService, useValue: {}},
        {provide: ActivatedRoute, useValue: activatedRouteMock}
      ],
      schemas: [NO_ERRORS_SCHEMA],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SecurisationCheckTabComponent);
    component = fixture.componentInstance;
    component.securisation = securisationValue;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
