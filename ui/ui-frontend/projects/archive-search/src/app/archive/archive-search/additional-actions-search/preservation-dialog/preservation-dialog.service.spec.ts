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
import { PreservationDialogService } from './preservation-dialog.service';
import { ArchiveService } from '../../../archive.service';
import { SearchCriteriaEltDto } from 'vitamui-library';
import { SnackBarService, CriteriaOperator, CriteriaDataType, SearchCriteriaTypeEnum } from 'vitamui-library';
import { PreservationUsage, PreservationVersion } from '../../../models/preservation-request.interface';

describe('PreservationDialogService', () => {
  let service: PreservationDialogService;
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
      launchPreservation: vi.fn().mockName('ArchiveService.launchPreservation'),
    };
    snackBarSpy = {
      open: vi.fn().mockName('SnackBarService.open'),
    };

    TestBed.configureTestingModule({
      providers: [
        PreservationDialogService,
        { provide: MatDialog, useValue: dialogSpy },
        { provide: ArchiveService, useValue: archiveServiceSpy },
        { provide: SnackBarService, useValue: snackBarSpy },
      ],
    });

    service = TestBed.inject(PreservationDialogService);
  });

  it('should not launch preservation when dialog is cancelled', () => {
    dialogSpy.open.mockReturnValue({ afterClosed: () => of(null) } as any);

    service.launchPreservationModal(mockCriteria, itemSelected, tenantId);

    expect(archiveServiceSpy.launchPreservation).not.toHaveBeenCalled();
    expect(snackBarSpy.open).not.toHaveBeenCalled();
  });

  it('should call API with correctly built PreservationRequestDto', () => {
    const dialogResult = {
      sourceUsage: PreservationUsage.BINARYMASTER,
      version: PreservationVersion.LAST,
      scenarioIdentifier: 'PSC-000001',
      targetUsage: PreservationUsage.DISSEMINATION,
    };
    dialogSpy.open.mockReturnValue({ afterClosed: () => of(dialogResult) } as any);
    archiveServiceSpy.launchPreservation.mockReturnValue(of(apiResponse));

    service.launchPreservationModal(mockCriteria, itemSelected, tenantId);

    expect(archiveServiceSpy.launchPreservation).toHaveBeenCalledWith(
      expect.objectContaining({
        scenarioIdentifier: 'PSC-000001',
        sourceUsage: PreservationUsage.BINARYMASTER,
        targetUsage: PreservationUsage.DISSEMINATION,
        version: PreservationVersion.LAST,
        searchCriteria: {
          criteriaList: mockCriteria,
          pageNumber: 0,
          size: 1,
        },
      }),
    );
  });

  it('should show snackbar with operation link when preservation launch succeeds', () => {
    const dialogResult = {
      sourceUsage: PreservationUsage.BINARYMASTER,
      version: PreservationVersion.LAST,
      scenarioIdentifier: 'PSC-000001',
    };

    dialogSpy.open.mockReturnValue({ afterClosed: () => of(dialogResult) } as any);
    archiveServiceSpy.launchPreservation.mockReturnValue(of(apiResponse));

    service.launchPreservationModal(mockCriteria, itemSelected, tenantId);

    expect(snackBarSpy.open).toHaveBeenCalledWith(
      expect.objectContaining({
        message: 'ARCHIVE_SEARCH.PRESERVATION.LAUNCHED',
        buttons: [
          expect.objectContaining({
            path: `/tenant/${tenantId}?guid=${apiResponse}`,
          }),
        ],
      }),
    );
  });

  it('should show snackbar error when the launch request fails', () => {
    const dialogResult = {
      sourceUsage: PreservationUsage.BINARYMASTER,
      version: PreservationVersion.LAST,
      scenarioIdentifier: 'PSC-000001',
    };
    const errorResponse = new HttpErrorResponse({
      error: 'Internal server error',
      status: 500,
      statusText: 'Internal Server Error',
    });

    dialogSpy.open.mockReturnValue({ afterClosed: () => of(dialogResult) } as any);
    archiveServiceSpy.launchPreservation.mockReturnValue(throwError(() => errorResponse));

    service.launchPreservationModal(mockCriteria, itemSelected, tenantId);

    expect(snackBarSpy.open).toHaveBeenCalledWith(
      expect.objectContaining({
        message: 'ARCHIVE_SEARCH.PRESERVATION.ERROR',
        icon: 'vitamui-icon-close',
        duration: 10000,
      }),
    );
  });
});
