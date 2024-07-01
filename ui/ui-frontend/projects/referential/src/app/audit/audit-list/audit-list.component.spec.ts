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
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Event, TableFilterComponent } from 'vitamui-library';
import { AuditService } from '../audit.service';
import { AuditListComponent } from './audit-list.component';
import { VitamUICommonTestModule } from 'vitamui-library/testing';

describe('AuditListComponent', () => {
  let component: AuditListComponent;
  let fixture: ComponentFixture<AuditListComponent>;

  beforeEach(async () => {
    const auditServiceMock = {
      search: () => of(null),
    };

    await TestBed.configureTestingModule({
      imports: [VitamUICommonTestModule, TableFilterComponent, AuditListComponent],
      providers: [{ provide: AuditService, useValue: auditServiceMock }],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AuditListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('Component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('Audit status should be OK when the last event is OK', () => {
    // Given
    const audit: Event = {
      id: 'aeeaaaaaaghhhxzdaayfsamdb6bwiriaaaaq',
      idRequest: 'aeeaaaaaaghhhxzdaayfsamdb6bwiriaaaaq',
      type: 'PROCESS_AUDIT',
      typeProc: 'AUDIT',
      obIdReq: 'obIdReq',
      data: 'data',
      outcome: 'OK',
      outDetail: 'PROCESS_AUDIT.STARTED',
      outMessage: 'Début audit',
      objectId: 'aeeaaaaaaghhhxzdaayfsamdb6bwiriaaaaq',
      idAppSession: 'AUDIT_APP166149000110:18b963b9-129f-4704-ba6e-600278466343:Contexte UI Referential:1:-:1',
      agId: '247521940',
      agIdApp: 'vitamui-context',
      rightsStatementIdentifier: 'bad json format',
      parentId: null,
      dateTime: null,
      collectionName: 'collectionName',
      parsedData: {
        dataKey: 'dataValue',
      },
      agIdExt: 'agIdExt',
      events: [
        null,
        {
          id: 'aeeaaaaaaghhhxzdaayfsamdb6bwiriaaaaq',
          idRequest: 'aeeaaaaaaghhhxzdaayfsamdb6bwiriaaaaq',
          type: 'PROCESS_AUDIT',
          typeProc: 'AUDIT',
          obIdReq: 'obIdReq',
          data: 'data',
          outcome: 'OK',
          outDetail: 'PROCESS_AUDIT.STARTED',
          outMessage: 'Audit terminé avec succès',
          objectId: 'aeeaaaaaaghhhxzdaayfsamdb6bwiriaaaaq',
          agId: '247521940',
          agIdApp: 'vitamui-context',
          rightsStatementIdentifier: '{"AccessContract":"ContratTNR"}',
          parentId: null,
          dateTime: null,
          collectionName: 'collectionName',
          parsedData: {
            dataKey: 'dataValue',
          },
          agIdExt: 'agIdExt',
        },
      ],
    };

    // When
    const returnedStatus = component.auditStatus(audit);

    // Then
    expect(returnedStatus).toBeDefined();
    expect(returnedStatus).toEqual('OK');
  });

  it('Audit status should return the outCome of the principal event when events is an empty array', () => {
    // Given
    const audit: Event = {
      id: 'aeeaaaaaaghhhxzdaayfsamdb6bwiriaaaaq',
      idRequest: 'aeeaaaaaaghhhxzdaayfsamdb6bwiriaaaaq',
      type: 'PROCESS_AUDIT',
      typeProc: 'AUDIT',
      obIdReq: 'obIdReq',
      data: 'data',
      outcome: 'STARTED',
      parentId: null,
      outDetail: 'PROCESS_AUDIT.STARTED',
      outMessage: 'Début audit',
      objectId: 'aeeaaaaaaghhhxzdaayfsamdb6bwiriaaaaq',
      idAppSession: 'AUDIT_APP166149000110:18b963b9-129f-4704-ba6e-600278466343:Contexte UI Referential:1:-:1',
      agId: '247521940',
      agIdApp: 'vitamui-context',
      rightsStatementIdentifier: '{"AccessContract":"ContratTNR"}',
      dateTime: null,
      collectionName: 'collectionName',
      parsedData: {
        dataKey: 'dataValue',
      },
      agIdExt: 'agIdExt',
      events: [],
    };

    // When
    const returnedStatus = component.auditStatus(audit);

    // Then
    expect(returnedStatus).toBeDefined();
    expect(returnedStatus).toEqual('STARTED');
  });

  it('Audit Message should be the outMessage when the last event', () => {
    // Given
    const returnedMessage = 'Audit terminé avec succès';
    const audit: Event = {
      id: 'aeeaaaaaaghhhxzdaayfsamdb6bwiriaaaaq',
      idRequest: 'aeeaaaaaaghhhxzdaayfsamdb6bwiriaaaaq',
      type: 'PROCESS_AUDIT',
      typeProc: 'AUDIT',
      obIdReq: 'obIdReq',
      data: 'data',
      outcome: 'OK',
      outDetail: 'PROCESS_AUDIT.STARTED',
      outMessage: 'Début audit',
      objectId: 'aeeaaaaaaghhhxzdaayfsamdb6bwiriaaaaq',
      idAppSession: 'AUDIT_APP166149000110:18b963b9-129f-4704-ba6e-600278466343:Contexte UI Referential:1:-:1',
      agId: '247521940',
      agIdApp: 'vitamui-context',
      rightsStatementIdentifier: '{"AccessContract":"ContratTNR"}',
      parentId: null,
      dateTime: null,
      collectionName: 'collectionName',
      parsedData: {
        dataKey: 'dataValue',
      },
      agIdExt: 'agIdExt',
      events: [
        null,
        null,
        {
          id: 'aeeaaaaaaghhhxzdaayfsamdb6bwiriaaaaq',
          idRequest: 'aeeaaaaaaghhhxzdaayfsamdb6bwiriaaaaq',
          type: 'PROCESS_AUDIT',
          typeProc: 'AUDIT',
          obIdReq: 'obIdReq',
          data: 'data',
          parentId: null,
          outcome: 'OK',
          outDetail: 'PROCESS_AUDIT.STARTED',
          outMessage: 'Audit terminé avec succès',
          objectId: 'aeeaaaaaaghhhxzdaayfsamdb6bwiriaaaaq',
          agId: '247521940',
          agIdApp: 'vitamui-context',
          rightsStatementIdentifier: '{"AccessContract":"ContratTNR"}',
          dateTime: null,
          collectionName: 'collectionName',
          parsedData: {
            dataKey: 'dataValue',
          },
          agIdExt: 'agIdExt',
        },
      ],
    };

    // When
    const response = component.auditMessage(audit);

    // Then
    expect(response).toBeDefined();
    expect(response).toEqual(returnedMessage);
  });

  it('Audit status should return the outMessage of the principal event when events is an empty array', () => {
    // Given
    const returnedMessage = 'Début audit';
    const audit: Event = {
      id: 'aeeaaaaaaghhhxzdaayfsamdb6bwiriaaaaq',
      idRequest: 'aeeaaaaaaghhhxzdaayfsamdb6bwiriaaaaq',
      type: 'PROCESS_AUDIT',
      typeProc: 'AUDIT',
      obIdReq: 'obIdReq',
      data: 'data',
      outcome: 'STARTED',
      outDetail: 'PROCESS_AUDIT.STARTED',
      outMessage: 'Début audit',
      objectId: 'aeeaaaaaaghhhxzdaayfsamdb6bwiriaaaaq',
      idAppSession: 'AUDIT_APP166149000110:18b963b9-129f-4704-ba6e-600278466343:Contexte UI Referential:1:-:1',
      agId: '247521940',
      agIdApp: 'vitamui-context',
      rightsStatementIdentifier: '{"AccessContract":"ContratTNR"}',
      parentId: null,
      dateTime: null,
      collectionName: 'collectionName',
      parsedData: {
        dataKey: 'dataValue',
      },
      agIdExt: 'agIdExt',
      events: [],
    };

    // When
    const response = component.auditMessage(audit);

    // Then
    expect(response).toBeDefined();
    expect(response).toEqual(returnedMessage);
  });
});
