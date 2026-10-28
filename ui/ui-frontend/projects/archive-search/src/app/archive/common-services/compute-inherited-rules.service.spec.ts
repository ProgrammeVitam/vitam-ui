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
import { TemplateRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';
import {
  ApplicationId,
  CriteriaDataType,
  CriteriaOperator,
  SearchCriteriaEltDto,
  SearchCriteriaTypeEnum,
  SnackBarService,
  UnitType,
} from 'vitamui-library';
import { ArchiveSearchComponent } from '../archive-search/archive-search.component';
import { ArchiveService } from '../archive.service';
import { ComputeInheritedRulesService } from './compute-inherited-rules.service';

describe('ComputeInheritedRulesService', () => {
  let service: ComputeInheritedRulesService;
  let archiveServiceMock: {
    searchArchiveUnitsByCriteria: ReturnType<typeof vi.fn>;
    launchComputedInheritedRules: ReturnType<typeof vi.fn>;
  };
  let dialogMock: { open: ReturnType<typeof vi.fn> };
  let snackBarServiceMock: { open: ReturnType<typeof vi.fn> };

  const alertDialog = { name: 'alert' } as unknown as TemplateRef<ArchiveSearchComponent>;
  const confirmDialog = { name: 'confirm' } as unknown as TemplateRef<ArchiveSearchComponent>;
  const titleCriteria: SearchCriteriaEltDto = {
    criteria: 'TITLE',
    values: [{ id: 'foo', value: 'foo' }],
    operator: CriteriaOperator.EQ,
    category: SearchCriteriaTypeEnum.FIELDS,
    dataType: CriteriaDataType.STRING,
  };
  const holdingUnitCriteria: SearchCriteriaEltDto = {
    criteria: 'ARCHIVE_UNIT_HOLDING_UNIT',
    values: [{ value: UnitType.HOLDING_UNIT, id: UnitType.HOLDING_UNIT }],
    operator: CriteriaOperator.EQ,
    category: SearchCriteriaTypeEnum.FIELDS,
    dataType: CriteriaDataType.STRING,
  };

  const dialogClosingWith = (result: unknown) => ({ afterClosed: () => of(result) });

  const launch = (criteria: SearchCriteriaEltDto[] = [{ ...titleCriteria }]) =>
    service.launchComputedInheritedRulesModal(criteria, 0, 7, 2, alertDialog, confirmDialog);

  beforeEach(() => {
    archiveServiceMock = {
      searchArchiveUnitsByCriteria: vi.fn(),
      launchComputedInheritedRules: vi.fn().mockReturnValue(of('operation-guid')),
    };
    dialogMock = { open: vi.fn() };
    snackBarServiceMock = { open: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        { provide: ArchiveService, useValue: archiveServiceMock },
        { provide: MatDialog, useValue: dialogMock },
        { provide: SnackBarService, useValue: snackBarServiceMock },
        { provide: TranslateService, useValue: { getCurrentLang: () => 'fr' } },
      ],
    });
    service = TestBed.inject(ComputeInheritedRulesService);
  });

  it('should first search holding units matching the criteria, with paging and language', () => {
    archiveServiceMock.searchArchiveUnitsByCriteria.mockReturnValue(of({ totalResults: 0 }));
    dialogMock.open.mockReturnValue(dialogClosingWith(false));

    launch();

    expect(archiveServiceMock.searchArchiveUnitsByCriteria).toHaveBeenCalledTimes(1);
    expect(archiveServiceMock.searchArchiveUnitsByCriteria).toHaveBeenCalledWith({
      criteriaList: [titleCriteria, holdingUnitCriteria],
      pageNumber: 2,
      size: 10,
      language: 'fr',
    });
  });

  it('should open the alert dialog and never launch the computation when holding units are selected', () => {
    archiveServiceMock.searchArchiveUnitsByCriteria.mockReturnValue(of({ totalResults: 3 }));
    dialogMock.open.mockReturnValue(dialogClosingWith(true));

    launch();

    expect(dialogMock.open).toHaveBeenCalledTimes(1);
    expect(dialogMock.open).toHaveBeenCalledWith(alertDialog);
    expect(archiveServiceMock.launchComputedInheritedRules).not.toHaveBeenCalled();
    expect(snackBarServiceMock.open).not.toHaveBeenCalled();
  });

  it('should not launch the computation when the confirmation dialog is dismissed', () => {
    archiveServiceMock.searchArchiveUnitsByCriteria.mockReturnValue(of({ totalResults: 0 }));
    dialogMock.open.mockReturnValue(dialogClosingWith(undefined));

    launch();

    expect(dialogMock.open).toHaveBeenCalledWith(confirmDialog);
    expect(archiveServiceMock.launchComputedInheritedRules).not.toHaveBeenCalled();
    expect(snackBarServiceMock.open).not.toHaveBeenCalled();
  });

  it('should launch the computation without the holding-unit criteria once confirmed and notify with a link to the operation', () => {
    archiveServiceMock.searchArchiveUnitsByCriteria.mockReturnValue(of({ totalResults: 0 }));
    dialogMock.open.mockReturnValue(dialogClosingWith(true));

    launch();

    expect(dialogMock.open).toHaveBeenCalledWith(confirmDialog);
    expect(archiveServiceMock.launchComputedInheritedRules).toHaveBeenCalledWith({
      criteriaList: [titleCriteria],
      pageNumber: 2,
      size: 10,
      language: 'fr',
    });
    expect(snackBarServiceMock.open).toHaveBeenCalledWith({
      message: 'ARCHIVE_SEARCH.COMPUTED_INHERITED_RULES.OPERATION_MESSAGE',
      buttons: [
        {
          appId: ApplicationId.LOGBOOK_OPERATION_APP,
          path: '/tenant/7?guid=operation-guid',
          label: 'SNACK_BAR.TO_OPERATION_APP',
        },
      ],
      duration: 100_000,
    });
  });
});
