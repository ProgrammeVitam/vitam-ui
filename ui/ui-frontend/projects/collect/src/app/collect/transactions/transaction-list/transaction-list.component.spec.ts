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

import { DatePipe } from '@angular/common';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { MatLegacyDialog as MatDialog, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatLegacySnackBarModule as MatSnackBarModule } from '@angular/material/legacy-snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { BASE_URL, InjectorModule, LoggerModule, StartupService, Transaction, TransactionStatus, WINDOW_LOCATION } from 'vitamui-library';
import { environment } from '../../../../../../archive-search/src/environments/environment';
import { VitamUISnackBar } from '../../shared/vitamui-snack-bar/vitamui-snack-bar.service';
import { TransactionResolver } from '../transaction-resolver.service';
import { TransactionsService } from '../transactions.service';
import { TransactionListComponent } from './transaction-list.component';

const translations: any = { TEST: 'Mock translate test' };

class FakeLoader implements TranslateLoader {
  getTranslation(): Observable<any> {
    return of(translations);
  }
}

describe('TransactionListComponent', () => {
  let component: TransactionListComponent;
  let fixture: ComponentFixture<TransactionListComponent>;
  const transaction: Transaction = {
    id: 'transactionId',
    projectId: 'projectId',
    status: TransactionStatus.OPEN,
    archivalAgreement: 'archivalAgreement',
    messageIdentifier: 'messageIdentifier',
    archivalAgencyIdentifier: 'archivalAgencyIdentifier',
    transferringAgencyIdentifier: 'transferringAgencyIdentifier',
    originatingAgencyIdentifier: 'originatingAgencyIdentifier',
    submissionAgencyIdentifier: 'submissionAgencyIdentifier',
    archiveProfile: 'archivalProfile',
    comment: 'comment',
    legalStatus: 'A legal status',
  };

  const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['open']);
  matDialogRefSpy.open.and.returnValue({ afterClosed: () => of(true) });

  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
  matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

  const snackBarSpy = jasmine.createSpyObj('VitamUISnackBarService', ['open']);

  const TransactionsServiceStub = jasmine.createSpyObj(
    'TransactionsService',

    {
      sendTransaction: of({}),
      abortTransaction: of({}),
      editTransaction: of({}),
      validateTransaction: of({}),
      search: of([transaction]),
    },
  );

  const TransactionResolverStub = {
    resolve: () => {
      return true;
    },
  };

  const startUpServiceMock = {
    getPortalUrl: () => '',
    setTenantIdentifier: () => {},
    getLogoutUrl: () => '',
    getCasUrl: () => '',
    getSearchUrl: () => '',
    getArchivesSearchUrl: () => '',
    getReferentialUrl: () => '',
    getTenantIdentifier: () => '',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TransactionListComponent],
      imports: [
        InjectorModule,
        MatSidenavModule,
        MatSnackBarModule,
        BrowserAnimationsModule,
        LoggerModule.forRoot(),
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot({
          loader: { provide: TranslateLoader, useClass: FakeLoader },
        }),
      ],
      providers: [
        DatePipe,
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MatDialog, useValue: matDialogRefSpy },
        { provide: VitamUISnackBar, useValue: snackBarSpy },
        { provide: TransactionsService, useValue: TransactionsServiceStub },
        { provide: TransactionResolver, useValue: TransactionResolverStub },
        { provide: StartupService, useValue: startUpServiceMock },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({ tenantIdentifier: 1 }),
            data: of({ appId: 'COLLECT_APP' }),
            snapshot: {
              queryParamMap: {
                get: () => 'project messageIdentifier',
              },
            },
          },
        },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: environment, useValue: environment },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TransactionListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should send Transaction', () => {
    const transactionService = TestBed.inject(TransactionsService);
    component.sendTransaction(transaction);
    expect(transactionService.sendTransaction).toHaveBeenCalled();
  });

  it('should validate Transaction', () => {
    const transactionService = TestBed.inject(TransactionsService);
    component.validateTransaction(transaction);
    expect(transactionService.validateTransaction).toHaveBeenCalled();
  });

  it('should abort Transaction', () => {
    const transactionService = TestBed.inject(TransactionsService);
    component.abortTransaction(transaction);
    expect(transactionService.abortTransaction).toHaveBeenCalled();
  });

  it('should edit Transaction', () => {
    const transactionService = TestBed.inject(TransactionsService);
    component.editTransaction(transaction);
    expect(transactionService.editTransaction).toHaveBeenCalled();
  });
});
