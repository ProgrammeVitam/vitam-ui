/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2021)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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

import { Component, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { LogbookService } from 'vitamui-library';
import { OperationDetails } from '../../../models/operation-response.interface';
import { LogbookManagementOperationInformationTabComponent } from './logbook-management-operation-information-tab.component';

let expectedOperation: OperationDetails;

@Component({
  template: ``,
})
class TestLogbookInformationComponent {
  operation = expectedOperation;
  tenantIdentifier = 1;
  tenant = 'tenant';
}

describe('LogbookManagementOperationInformationTabComponent', () => {
  let logbookInformationComponent: TestLogbookInformationComponent;
  let fixture: ComponentFixture<TestLogbookInformationComponent>;

  beforeEach(async () => {
    expectedOperation = {
      globalState: 'PAUSE',
      nextStep: 'nextStep',
      operationId: 'aecaaereragfogjqbaai6malzquerteaaaq',
      previousStep: 'previousStep',
      processDate: new Date(),
      processType: 'TRACEABILITY',
      stepByStep: true,
      stepStatus: 'KO',
    };
    await TestBed.configureTestingModule({
      declarations: [LogbookManagementOperationInformationTabComponent, TestLogbookInformationComponent],
      imports: [TranslateModule.forRoot()],
      providers: [{ provide: LogbookService, useValue: {} }],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TestLogbookInformationComponent);
    logbookInformationComponent = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(logbookInformationComponent).toBeTruthy();
  });

  describe('Class', () => {
    it('should have the correct fields', () => {
      expect(logbookInformationComponent.operation.globalState).not.toBeNull();
      expect(logbookInformationComponent.operation.nextStep).not.toBeNull();
      expect(logbookInformationComponent.operation.operationId).not.toBeNull();
      expect(logbookInformationComponent.operation.previousStep).not.toBeNull();
      expect(logbookInformationComponent.operation.processDate).not.toBeNull();
      expect(logbookInformationComponent.operation.stepByStep).not.toBeNull();
      expect(logbookInformationComponent.operation.stepStatus).not.toBeFalsy();
    });

    it('should have Tenant and tenantIdentifier', () => {
      expect(logbookInformationComponent.tenant).not.toBeNull();
      expect(logbookInformationComponent.tenantIdentifier).not.toBeNull();
    });
  });
});
