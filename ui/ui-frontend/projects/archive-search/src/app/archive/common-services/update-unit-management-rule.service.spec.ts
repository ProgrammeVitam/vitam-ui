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
import { Router } from '@angular/router';
import { of, Subscription } from 'rxjs';
import { APP_ROUTE_PREFIX } from 'vitamui-library';
import { ManagementRulesSharedDataService } from '../../core/management-rules-shared-data.service';
import { ArchiveService } from '../archive.service';
import { UpdateUnitManagementRuleService } from './update-unit-management-rule.service';

describe('UpdateUnitManagementRuleService', () => {
  let service: UpdateUnitManagementRuleService;

  const managementRulesSharedDataServiceMock = {
    getCriteriaSearchDSLQuery: () => of({}),
    getManagementRules: () => of(),
    getAccessContract: () => of('AccessContract'),
    getselectedItems: () => of(35),
    getCriteriaSearchListToSave: () => of({}),
    getRuleActions: () => of({}),
    emitselectedItems: vi.fn(),
    emitCriteriaSearchListToSave: vi.fn(),
    emitCriteriaSearchDSLQuery: vi.fn(),
  };

  const archiveSearchServiceMock = {
    archive: () => of('test archive'),
    search: () => of([]),
    getAccessContractById: () => of({}),
    reclassification: () => of({}),
    searchArchiveUnitsByCriteria: () => of({ totalResults: 0 }),
    getTotalTrackHitsByCriteria: () => of({}),
    openSnackBarForWorkflow: () => of({}),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        { provide: ManagementRulesSharedDataService, useValue: managementRulesSharedDataServiceMock },
        UpdateUnitManagementRuleService,
        { provide: APP_ROUTE_PREFIX, useValue: '/feature' },
        { provide: MatDialog, useValue: { open: () => ({ afterClosed: () => of(true) }) } },
        { provide: ArchiveService, useValue: archiveSearchServiceMock },
      ],
    });
    service = TestBed.inject(UpdateUnitManagementRuleService);
  });

  it('should the service be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return APPRAISAL_RULE', () => {
    // Given
    const expectedResponse = 'APPRAISAL_RULE';
    const ruleCategoryName = 'AppraisalRule';

    // When
    const response = service.getRuleManagementCategory(ruleCategoryName);

    // Then
    expect(response).not.toBeNull();
    expect(response).toEqual(expectedResponse);
  });

  it('should navigate to the update rules page of the feature once confirmed when no holding unit is selected', () => {
    const router = { navigate: vi.fn() } as unknown as Router;

    service.goToUpdateManagementRule([], [], 0, 1, 0, router, 35, new Subscription(), {} as TemplateRef<any>, {} as TemplateRef<any>);

    expect(managementRulesSharedDataServiceMock.emitselectedItems).toHaveBeenCalledWith(35);
    expect(router.navigate).toHaveBeenCalledWith(['/feature/archive-search/update-rules/tenant/', 1], {
      queryParamsHandling: 'preserve',
    });
  });
});
