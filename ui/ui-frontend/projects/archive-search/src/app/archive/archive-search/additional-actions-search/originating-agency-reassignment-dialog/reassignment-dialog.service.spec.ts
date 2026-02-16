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
import { of } from 'rxjs';
import { ReassignmentDialogService } from './reassignment-dialog.service';
import { ArchiveService } from '../../../archive.service';
import { SnackBarService, SearchCriteriaEltDto, CriteriaOperator, CriteriaDataType, SearchCriteriaTypeEnum } from 'vitamui-library';

describe('ReassignmentDialogService', () => {
  let service: ReassignmentDialogService;
  let dialogSpy: jasmine.SpyObj<MatDialog>;
  let archiveServiceSpy: jasmine.SpyObj<ArchiveService>;
  let snackBarSpy: jasmine.SpyObj<SnackBarService>;

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
    dialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
    archiveServiceSpy = jasmine.createSpyObj('ArchiveService', ['launchReassignmentAction']);
    snackBarSpy = jasmine.createSpyObj('SnackBarService', ['open']);

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
    dialogSpy.open.and.returnValue({ afterClosed: () => of(null) } as any);

    service.lanchReassignmentModal(mockCriteria, itemSelected, tenantId);

    expect(archiveServiceSpy.launchReassignmentAction).not.toHaveBeenCalled();
    expect(snackBarSpy.open).not.toHaveBeenCalled();
  });

  it('should call API with correctly built ReassignRequestDto', () => {
    const dialogResult = { fromAgency: 'agencie-a', toAgency: 'agencie-b', propagateToObjectGroups: true };
    dialogSpy.open.and.returnValue({ afterClosed: () => of(dialogResult) } as any);
    archiveServiceSpy.launchReassignmentAction.and.returnValue(of(apiResponse));

    service.lanchReassignmentModal(mockCriteria, itemSelected, tenantId);

    expect(archiveServiceSpy.launchReassignmentAction).toHaveBeenCalledWith(
      jasmine.objectContaining({
        ...dialogResult,
        searchCriteria: {
          criteriaList: mockCriteria,
          pageNumber: 0,
          size: 1,
        },
      }),
    );
  });

  it('should show snackbar with operation link when reassignment succeeds', () => {
    const dialogResult = { fromAgency: 'agencie-a', toAgency: 'agencie-b', propagateToObjectGroups: true };

    dialogSpy.open.and.returnValue({ afterClosed: () => of(dialogResult) } as any);
    archiveServiceSpy.launchReassignmentAction.and.returnValue(of(apiResponse));

    service.lanchReassignmentModal(mockCriteria, itemSelected, tenantId);

    expect(snackBarSpy.open).toHaveBeenCalledWith(
      jasmine.objectContaining({
        message: 'ARCHIVE_SEARCH.ELIMINATION.ELIMINATION_LAUNCHED',
        buttons: [
          jasmine.objectContaining({
            path: `/tenant/${tenantId}?guid=aeaqaaaabieci5gnciz5kam2cby53jaaaabq`,
          }),
        ],
      }),
    );
  });
});
