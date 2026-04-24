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
import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { ReassignmentDialogService } from './reassignment-dialog.service';
import { ArchiveService } from '../../../archive.service';
import { SnackBarService, SearchCriteriaEltDto, CriteriaOperator, CriteriaDataType, SearchCriteriaTypeEnum } from 'vitamui-library';
import { ReassignmentMode } from '../../../models/reassign-request.interface';

describe('ReassignmentDialogService', () => {
  let service: ReassignmentDialogService;
  let dialogSpy: any;
  let archiveServiceSpy: any;
  let snackBarSpy: any;

  const mockCriteria: SearchCriteriaEltDto[] = [
    {
      criteria: 'GUID',
      values: [{ id: 'aeaqaaaabieci5gnciz5kam2cby53jaaaabq', value: 'aeaqaaaabieci5gnciz5kam2cby53jaaaabq' }],
      operator: CriteriaOperator.EQ,
      category: SearchCriteriaTypeEnum.FIELDS,
      dataType: CriteriaDataType.STRING,
    },
  ];
  const itemSelected = 1;
  const tenantId = 0;
  const apiResponse = 'aeaqaaaabieci5gnciz5kam2cby53jaaaabq';

  beforeEach(() => {
    dialogSpy = {
      open: vi.fn().mockName('MatDialog.open'),
    };
    archiveServiceSpy = {
      launchReassignmentAction: vi.fn().mockName('ArchiveService.launchReassignmentAction'),
    };
    snackBarSpy = {
      open: vi.fn().mockName('SnackBarService.open'),
      openSnackBar: vi.fn().mockName('SnackBarService.openSnackBar'),
    };

    TestBed.configureTestingModule({
      providers: [
        ReassignmentDialogService,
        { provide: MatDialog, useValue: dialogSpy },
        { provide: ArchiveService, useValue: archiveServiceSpy },
        { provide: SnackBarService, useValue: snackBarSpy },
      ],
    });

    service = TestBed.inject(ReassignmentDialogService);
  });

  it('should stop flow when dialog returns null', () => {
    dialogSpy.open.mockReturnValue({ afterClosed: () => of(null) } as any);

    service.launchReassignmentModal(mockCriteria, itemSelected, tenantId);

    expect(archiveServiceSpy.launchReassignmentAction).not.toHaveBeenCalled();
    expect(snackBarSpy.open).not.toHaveBeenCalled();
  });

  it('should call API with correctly built ReassignRequestDto', () => {
    const dialogResult = { sourceOriginatingAgency: 'agencie-a', targetOriginatingAgency: 'agencie-b', propagateToObjectGroups: true };
    dialogSpy.open.mockReturnValue({ afterClosed: () => of(dialogResult) } as any);
    archiveServiceSpy.launchReassignmentAction.mockReturnValue(of(apiResponse));

    service.launchReassignmentModal(mockCriteria, itemSelected, tenantId);

    expect(archiveServiceSpy.launchReassignmentAction).toHaveBeenCalledWith(
      expect.objectContaining({
        reassignMode: ReassignmentMode.BY_ID,
        sourceOriginatingAgency: 'agencie-a',
        targetOriginatingAgency: 'agencie-b',
        propagateToObjectGroups: true,
        searchCriteria: {
          criteriaList: mockCriteria,
          pageNumber: 0,
          size: 1,
        },
      }),
    );
  });

  it('should show snackbar with operation link when reassignment succeeds', () => {
    const dialogResult = { sourceOriginatingAgency: 'agencie-a', targetOriginatingAgency: 'agencie-b', propagateToObjectGroups: true };

    dialogSpy.open.mockReturnValue({ afterClosed: () => of(dialogResult) } as any);
    archiveServiceSpy.launchReassignmentAction.mockReturnValue(of(apiResponse));

    service.launchReassignmentModal(mockCriteria, itemSelected, tenantId);

    expect(snackBarSpy.open).toHaveBeenCalledWith(
      expect.objectContaining({
        message: 'ARCHIVE_SEARCH.ORIGINATING_AGENCY_REASSIGNMENT.REASSIGNMENT_LAUNCHED',
        buttons: [
          expect.objectContaining({
            path: `/tenant/${tenantId}?guid=aeaqaaaabieci5gnciz5kam2cby53jaaaabq`,
          }),
        ],
      }),
    );
  });

  it('should show alert dialog when ERROR_MULTIPLE_SP error occurs', () => {
    const dialogResult = { sourceOriginatingAgency: 'agencie-a', targetOriginatingAgency: 'agencie-b', propagateToObjectGroups: true };
    const errorResponse = new HttpErrorResponse({
      error: 'ERROR_MULTIPLE_SP',
      status: 400,
      statusText: 'Bad Request',
    });

    dialogSpy.open.mockReturnValue({ afterClosed: () => of(dialogResult) } as any);
    archiveServiceSpy.launchReassignmentAction.mockReturnValue(throwError(() => errorResponse));

    service.launchReassignmentModal(mockCriteria, itemSelected, tenantId);

    expect(dialogSpy.open).toHaveBeenCalledTimes(2); // Once for reassignment modal, once for error dialog
    expect(snackBarSpy.open).not.toHaveBeenCalled();
  });

  it('should show snackbar error when bad request error occurs', () => {
    const dialogResult = { sourceOriginatingAgency: 'agencie-a', targetOriginatingAgency: 'agencie-b', propagateToObjectGroups: true };
    const errorResponse = new HttpErrorResponse({
      error: 'Some validation error',
      status: 400,
      statusText: 'Bad Request',
    });

    dialogSpy.open.mockReturnValue({ afterClosed: () => of(dialogResult) } as any);
    archiveServiceSpy.launchReassignmentAction.mockReturnValue(throwError(() => errorResponse));

    service.launchReassignmentModal(mockCriteria, itemSelected, tenantId);

    expect(snackBarSpy.open).toHaveBeenCalledWith(
      expect.objectContaining({
        message: 'ARCHIVE_SEARCH.ORIGINATING_AGENCY_REASSIGNMENT.BAD_REQUEST_ERROR',
        icon: 'vitamui-icon-close',
        duration: 10000,
      }),
    );
  });

  it('should show snackbar error when server error occurs', () => {
    const dialogResult = { sourceOriginatingAgency: 'agencie-a', targetOriginatingAgency: 'agencie-b', propagateToObjectGroups: true };
    const errorResponse = new HttpErrorResponse({
      error: 'Internal server error',
      status: 500,
      statusText: 'Internal Server Error',
    });

    dialogSpy.open.mockReturnValue({ afterClosed: () => of(dialogResult) } as any);
    archiveServiceSpy.launchReassignmentAction.mockReturnValue(throwError(() => errorResponse));

    service.launchReassignmentModal(mockCriteria, itemSelected, tenantId);

    expect(snackBarSpy.open).toHaveBeenCalledWith(
      expect.objectContaining({
        message: 'ARCHIVE_SEARCH.ORIGINATING_AGENCY_REASSIGNMENT.SERVER_ERROR',
        icon: 'vitamui-icon-close',
        duration: 10000,
      }),
    );
  });
});
