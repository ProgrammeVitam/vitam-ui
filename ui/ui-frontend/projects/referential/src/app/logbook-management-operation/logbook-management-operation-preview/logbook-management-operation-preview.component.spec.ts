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
import { Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { BASE_URL, WINDOW_LOCATION } from 'vitamui-library';
import { OperationsResults } from '../../models/operation-response.interface';
import { LogbookManagementOperationService } from '../logbook-management-operation.service';
import { LogbookManagementOperationPreviewComponent } from './logbook-management-operation-preview.component';

describe('LogbookManagementOperationPreviewComponent', () => {
  let component: LogbookManagementOperationPreviewComponent;
  let fixture: ComponentFixture<LogbookManagementOperationPreviewComponent>;
  const operationsResults: OperationsResults = {
    hits: [],
    results: [],
    facetResults: [],
    context: [],
  };

  @Pipe({ name: 'truncate' })
  class MockTruncatePipe implements PipeTransform {
    transform(value: number): number {
      return value;
    }
  }

  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
  matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

  const logbookManagementOperationServiceMock = {
    listOperationsDetails: () => of(operationsResults),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LogbookManagementOperationPreviewComponent, MockTruncatePipe],
      imports: [TranslateModule.forRoot(), HttpClientTestingModule],
      providers: [
        { provide: LogbookManagementOperationService, useValue: logbookManagementOperationServiceMock },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: WINDOW_LOCATION, useValue: {} },
        { provide: BASE_URL, useValue: '/fake-api' },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LogbookManagementOperationPreviewComponent);
    component = fixture.componentInstance;
    component.operation = {
      globalState: 'PAUSE',
      nextStep: '',
      operationId: 'aecaaereragfogjqbaai6malzquerteaaaq',
      previousStep: '',
      processDate: new Date(),
      processType: 'TRACEABILITY',
      stepByStep: false,
      stepStatus: 'KO',
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('should get the correct Operation status', () => {
    expect(component.operationStatus(component.operation)).toEqual('KO');
  });
  it('should get the correct Operation status', () => {
    expect(component.operation.globalState).toEqual('PAUSE');
  });
});
