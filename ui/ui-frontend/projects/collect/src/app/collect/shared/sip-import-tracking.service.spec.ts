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
import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { defer, of, throwError } from 'rxjs';
import { LoggerModule } from 'vitamui-library';

import { TransactionApiService } from '../core/api/transaction-api.service';
import { OperationState, OperationStatus } from '../models/operation-status.interface';
import { SipImportTrackingService } from './sip-import-tracking.service';

describe('SipImportTrackingService', () => {
  let service: SipImportTrackingService;
  let transactionApiService: { getOperationStatus: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    transactionApiService = { getOperationStatus: vi.fn() };
    TestBed.configureTestingModule({
      imports: [LoggerModule.forRoot()],
      providers: [{ provide: TransactionApiService, useValue: transactionApiService }],
    });
    service = TestBed.inject(SipImportTrackingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should report the SIP import as in progress until the operation is completed', fakeAsync(() => {
    const statuses: OperationStatus[] = [
      { globalState: OperationState.RUNNING, globalStatus: 'STARTED' },
      { globalState: OperationState.COMPLETED, globalStatus: 'OK' },
    ];
    let call = 0;
    // Like HttpClient, the returned observable is cold: each poll triggers a new request
    transactionApiService.getOperationStatus.mockReturnValue(defer(() => of(statuses[Math.min(call++, statuses.length - 1)])));

    service.trackSipImport('transactionId', 'operationId');
    expect(service.isSipImportInProgress('transactionId')).toBe(true);

    tick(5_000);
    expect(service.isSipImportInProgress('transactionId')).toBe(false);
  }));

  it('should stop blocking the transaction when the status polling fails', fakeAsync(() => {
    transactionApiService.getOperationStatus.mockReturnValue(throwError(() => new Error('network error')));

    service.trackSipImport('transactionId', 'operationId');

    tick(5_000);
    expect(service.isSipImportInProgress('transactionId')).toBe(false);
  }));

  it('should not track anything without an operation id', () => {
    service.trackSipImport('transactionId', null);
    expect(service.isSipImportInProgress('transactionId')).toBe(false);
    expect(transactionApiService.getOperationStatus).not.toHaveBeenCalled();
  });
});
