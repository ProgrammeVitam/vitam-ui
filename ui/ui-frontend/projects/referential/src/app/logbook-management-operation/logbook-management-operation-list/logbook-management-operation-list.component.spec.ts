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

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { OperationsResults } from '../../models/operation-response.interface';
import { LogbookManagementOperationService } from '../logbook-management-operation.service';
import { LogbookManagementOperationListComponent } from './logbook-management-operation-list.component';

@Pipe({
  name: 'truncate',
  standalone: true,
})
class MockTruncatePipe implements PipeTransform {
  transform(value: number): number {
    return value;
  }
}

describe('LogbookManagementOperationListComponent', () => {
  let component: LogbookManagementOperationListComponent;
  let fixture: ComponentFixture<LogbookManagementOperationListComponent>;

  let operationsResults: OperationsResults = {
    hits: [],
    results: [],
    facetResults: [],
    context: [],
  };

  const logbookManagementOperationServiceMock = {
    listOperationsDetails: () => of(operationsResults),
  };
  beforeEach(async () => {
    operationsResults = {
      hits: { total: 3 },
      results: [
        {
          globalState: 'COMPLETED',
          nextStep: '',
          operationId: 'aecaaaaaagfogjqbaai6malzqushoeyaaaaq',
          previousStep: '',
          processDate: new Date(),
          processType: 'TRACEABILITY',
          stepByStep: false,
          stepStatus: 'OK',
        },
        {
          globalState: 'RUNNING',
          nextStep: '',
          operationId: 'aecaaaaaagfogjqbaai6malzquerteaaaq',
          previousStep: '',
          processDate: new Date(),
          processType: 'TRACEABILITY',
          stepByStep: false,
          stepStatus: 'WARNING',
        },
        {
          globalState: 'PAUSE',
          nextStep: '',
          operationId: 'aecaaereragfogjqbaai6malzquerteaaaq',
          previousStep: '',
          processDate: new Date(),
          processType: 'TRACEABILITY',
          stepByStep: false,
          stepStatus: 'KO',
        },
      ],
      facetResults: [],
      context: [],
    };
    await TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot(), HttpClientTestingModule, LogbookManagementOperationListComponent, MockTruncatePipe],
      providers: [{ provide: LogbookManagementOperationService, useValue: logbookManagementOperationServiceMock }],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    const logbookManagementOperationService = TestBed.get(LogbookManagementOperationService);
    spyOn(logbookManagementOperationService, 'listOperationsDetails').and.callThrough();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LogbookManagementOperationListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('should the id of the first operation of results be ', () => {
    component.orderByParam('stepStatus', 'Status');
    expect(component.results[0].operationId).toEqual('aecaaereragfogjqbaai6malzquerteaaaq');
  });
  it('should get one operation as results', () => {
    component.getOperationsByStatus('WARNING');
    expect(component.results.length).toEqual(1);
  });
  it('should have one operation as results', () => {
    component.getOperationsByGlobalState('PAUSE');
    expect(component.results.length).toEqual(1);
  });
  it('should return one element as result', () => {
    expect(component.getTotalResultsByState(operationsResults, 'RUNNING')).toEqual(1);
  });
  it('should have one element as result', () => {
    expect(component.getTotalResultsByStatus(operationsResults, 'OK')).toEqual(1);
  });
  it('should create three state facet component', () => {
    component.initializeFacet();
    expect(component.stateFacetDetails.length).toEqual(3);
  });
  it('should create four status facet component', () => {
    component.initializeFacet();
    expect(component.statusFacetDetails.length).toEqual(4);
  });
  it('should show the message End of Results', () => {
    component.loadMore();
    expect(component.show).toBe(true);
  });
  it('should show return ELIMINATION as result', () => {
    expect(component.getProcessTypeByValue('Élimination des unités archivistiques')).toBe('ELIMINATION');
  });
  it('should the id of the second operation of results be', () => {
    component.orderByStatus();
    expect(component.results[1].operationId).toEqual('aecaaaaaagfogjqbaai6malzqushoeyaaaaq');
  });
});
