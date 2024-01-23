/*
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

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { BASE_URL, InjectorModule, LoggerModule, WINDOW_LOCATION } from 'ui-frontend-common';
import { environment } from '../../environments/environment';
import { DepositStatus, GetorixDeposit } from './core/model/getorix-deposit.interface';
import { GetorixDepositComponent } from './getorix-deposit.component';
import { GetorixDepositService } from './getorix-deposit.service';

describe('GetorixDepositComponent', () => {
  let component: GetorixDepositComponent;
  let fixture: ComponentFixture<GetorixDepositComponent>;

  const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
  let getorixExamle_1 = {
    id: 'getorix_id',
    depositStatus: DepositStatus.VALIDATED,
    operationName: 'operationName',
    operationType: 'operationType',
    nationalNumber: 'nationalNumber',
  } as GetorixDeposit;

  let getorixExamle_2 = {
    id: 'getorix_id2',
    depositStatus: DepositStatus.DRAFT,
    operationName: 'operationName2',
    operationType: 'operationType2',
    nationalNumber: 'nationalNumber2',
  } as GetorixDeposit;

  let getorixExamle_3 = {
    id: 'getorix_id3',
    depositStatus: DepositStatus.ACK_OK,
    operationName: 'operationName3',
    operationType: 'operationType3',
    nationalNumber: 'nationalNumber3',
  } as GetorixDeposit;

  let getorixDepositList: GetorixDeposit[] = [];

  getorixDepositList.push(getorixExamle_1, getorixExamle_2, getorixExamle_3);

  const getorixDepositServiceMock = {
    createGetorixDeposit: () => of({}),
    getLastThreeOperations: () => of(getorixDepositList),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [GetorixDepositComponent],
      imports: [HttpClientTestingModule, TranslateModule.forRoot(), InjectorModule, LoggerModule.forRoot()],
      providers: [
        {
          provide: Router,
          useValue: routerSpy,
        },
        {
          provide: ActivatedRoute,
          useValue: {
            navigate: () => {},
            params: of({ tenantIdentifier: 1 }),
            data: of({ appId: 'GETORIX_DEPOSIT_APP' }),
            events: of({}),
            snapshot: {
              queryParamMap: {
                get: () => 'operationId',
              },
            },
          },
        },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: environment, useValue: environment },
        { provide: GetorixDepositService, useValue: getorixDepositServiceMock },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GetorixDepositComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should redirect to /create', () => {
    const router = TestBed.inject(Router);
    component.startDepositCreation();
    expect(router.navigate).toHaveBeenCalledWith([undefined, 'create']);
  });

  it('component should work correctly calling getLastThreeOperations', () => {
    component.getLastThreeOperations();
    expect(component).toBeTruthy();
  });

  it('component should work correctly calling OpenGetorixOperationDetails when the opration is draft', () => {
    let getorixExamle = {
      id: 'getorix_id2',
      depositStatus: DepositStatus.DRAFT,
      operationName: 'operationName2',
      operationType: 'operationType2',
      nationalNumber: 'nationalNumber2',
    } as GetorixDeposit;
    component.OpenGetorixOperationDetails(getorixExamle);
    expect(component).toBeTruthy();
  });

  describe('DOM', () => {
    it('should have 2 buttons ', () => {
      // When
      const nativeElement = fixture.nativeElement;
      const elementBtn = nativeElement.querySelectorAll('button');

      // Then
      expect(elementBtn.length).toBe(2);
    });
  });
});
