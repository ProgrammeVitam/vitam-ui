import type { Mock } from 'vitest';
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
import { Location } from '@angular/common';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatTreeModule } from '@angular/material/tree';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateLoader } from '@ngx-translate/core';
import { environment } from 'projects/archive-search/src/environments/environment';
import { Observable, of } from 'rxjs';
import {
  BASE_URL,
  InjectorModule,
  LoggerModule,
  PagedResult,
  SchemaService,
  SearchCriteriaDto,
  SearchCriteriaService,
  SearchCriteriaStatusEnum,
  SearchCriteriaTypeEnum,
  SecurityService,
  UnitType,
  VitamTenantConfigService,
  VitamuiRoles,
} from 'vitamui-library';
import { tenantConfigServiceMock } from 'vitamui-library/testing';
import { ArchiveSharedDataService } from '../../core/archive-shared-data.service';
import { ArchiveService } from '../archive.service';
import { ArchiveSearchHelperService } from '../common-services/archive-search-helper.service';
import { ArchiveUnitDipService } from '../common-services/archive-unit-dip.service';
import { ArchiveUnitEliminationService } from '../common-services/archive-unit-elimination.service';
import { ComputeInheritedRulesService } from '../common-services/compute-inherited-rules.service';
import { UpdateUnitManagementRuleService } from '../common-services/update-unit-management-rule.service';
import { ArchiveSearchComponent } from './archive-search.component';
import { TransferAcknowledgmentComponent } from './transfer-acknowledgment/transfer-acknowledgment.component';
import { SimpleCriteriaSearchComponent } from './simple-criteria-search/simple-criteria-search.component';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { NodeData } from '../models/nodedata.interface';

const arrayWithExactContents = <T>(arr: T[]) => expect.arrayContaining(arr as any);

const translations: any = { TEST: 'Mock translate test' };

class FakeLoader implements TranslateLoader {
  getTranslation(): Observable<any> {
    return of(translations);
  }
}

describe('ArchiveSearchComponent', () => {
  let component: ArchiveSearchComponent;
  let fixture: ComponentFixture<ArchiveSearchComponent>;
  const pagedResult: PagedResult = { pageNumbers: 1, facets: [], results: [], totalResults: 1 };
  let archiveSharedDataService: ArchiveSharedDataService;
  const matDialogSpy = {
    open: vi.fn().mockName('MatDialog.open'),
  };

  matDialogSpy.open.mockReturnValue({
    afterClosed: vi.fn().mockReturnValue(of(true)), // Simule une fermeture normale
  });

  const archiveServiceStub = {
    getAccessContractById: () => of({}),
    getTotalTrackHitsByCriteria: () => of(42),
    hasArchiveSearchRole: () => of(true),
    isAccessRuleCriteria: () => false,
    isAppraisalRuleCriteria: () => false,
    isDisseminationRuleCriteria: () => false,
    isEliminationTenchnicalIdCriteria: () => false,
    isReuseRuleCriteria: () => false,
    isStorageRuleCriteria: () => false,
    isWaitingToRecalculateCriteria: () => false,
    loadFilingHoldingSchemeTree: () => of([]),
    searchArchiveUnitsByCriteria: (_criteriaDto: SearchCriteriaDto) => of(pagedResult),
  };

  const updateUnitManagementRuleServiceMock = {
    goToUpdateManagementRule: () => of(),
  };
  const archiveUnitEliminationServiceMock = {
    launchEliminationAnalysisModal: () => of(),
    launchEliminationModal: () => of(),
  };
  const archiveUnitDipServiceMock = {
    launchExportDipModal: () => of(),
  };
  const computeInheritedRulesServiceMock = {
    launchComputedInheritedRulesModal: () => of(),
  };

  const securityServiceStub = {
    user: {
      profileGroup: {
        profiles: [
          {
            applicationName: 'ARCHIVE_SEARCH_MANAGEMENT_APP',
            name: 'Default',
          },
        ],
      },
    },
    hasRole$: () => of(false),
  };

  const searchCriteriaServiceMock = {
    ready: vi.fn().mockResolvedValue(undefined),
    toSearchCriteria: vi.fn().mockImplementation(async (obj: Record<string, string | string[]>) =>
      Object.entries(obj).flatMap(([key, values]) => {
        const arr = Array.isArray(values) ? values : [values];
        return arr.map((value) => ({
          keyElt: key,
          valueElt: { id: key, value },
          labelElt: value,
          keyTranslated: false,
          operator: 'EQ',
          category: SearchCriteriaTypeEnum.FIELDS,
          valueTranslated: false,
          dataType: 'STRING',
        }));
      }),
    ),
  };

  const computeActivatedRoute = (queryParams: Params = {}) => {
    return {
      params: of({ tenantIdentifier: 1 }),
      queryParamMap: of({ keys: Object.keys(queryParams) }),
      snapshot: { queryParamMap: { keys: Object.keys(queryParams) }, queryParams: queryParams },
    };
  };

  const setupTest = async (queryParams: Params) => {
    const routerSpy = {
      navigate: vi.fn().mockName('Router.navigate'),
      createUrlTree: vi.fn().mockName('Router.createUrlTree'),
      serializeUrl: vi.fn().mockName('Router.serializeUrl'),
      parseUrl: vi.fn().mockName('Router.parseUrl'),
    };
    routerSpy.createUrlTree.mockReturnValue({});
    routerSpy.serializeUrl.mockReturnValue('/test-url');
    routerSpy.parseUrl.mockReturnValue({ queryParams: {} });

    const locationSpy = {
      replaceState: vi.fn().mockName('Location.replaceState'),
      path: vi.fn().mockName('Location.path'),
    };
    locationSpy.path.mockReturnValue('/test-url');

    vi.spyOn(archiveServiceStub, 'searchArchiveUnitsByCriteria');

    await TestBed.configureTestingModule({
      schemas: [NO_ERRORS_SCHEMA],
      imports: [
        InjectorModule,
        LoggerModule.forRoot(),
        MatMenuModule,
        MatProgressSpinnerModule,
        MatSidenavModule,
        MatTreeModule,
        RouterTestingModule,
        ArchiveSearchComponent,
        SimpleCriteriaSearchComponent,
      ],
      providers: [
        ArchiveSearchHelperService,
        ArchiveSharedDataService,
        { provide: ActivatedRoute, useValue: computeActivatedRoute(queryParams) },
        { provide: ArchiveService, useValue: archiveServiceStub },
        { provide: SecurityService, useValue: securityServiceStub },
        { provide: ArchiveUnitDipService, useValue: archiveUnitDipServiceMock },
        { provide: ArchiveUnitEliminationService, useValue: archiveUnitEliminationServiceMock },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ComputeInheritedRulesService, useValue: computeInheritedRulesServiceMock },
        { provide: Location, useValue: locationSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: Router, useValue: routerSpy },
        { provide: SchemaService, useValue: { getDescriptiveSchemaTree: () => of(), getSchema: () => of([]) } },
        { provide: SearchCriteriaService, useValue: searchCriteriaServiceMock },
        { provide: UpdateUnitManagementRuleService, useValue: updateUnitManagementRuleServiceMock },
        { provide: environment, useValue: environment },
        {
          provide: VitamTenantConfigService,
          useValue: tenantConfigServiceMock,
        },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ArchiveSearchComponent);
    component = fixture.componentInstance;
    archiveSharedDataService = TestBed.inject(ArchiveSharedDataService);
    fixture.detectChanges();

    return { routerSpy };
  };

  describe('', () => {
    beforeEach(async () => await setupTest({}));

    it('Component should be created', () => {
      expect(component).toBeTruthy();
    });

    it('should be true', () => {
      component.showHideDuaEndDate(true);
      expect(component.showDuaEndDate).toBeTruthy();
    });

    it('should be false', () => {
      component.showHidePanel(false);
      expect(component.showCriteriaPanel).toBeFalsy();
    });

    it('should call hasArchiveSearchRole', () => {
      vi.spyOn(archiveServiceStub, 'hasArchiveSearchRole');
      // When
      component.checkUserHasRole(VitamuiRoles.ROLE_EXPORT_DIP, 1);

      // Then
      expect(archiveServiceStub.hasArchiveSearchRole).toHaveBeenCalled();
    });
    it('should open a modal with TransferAcknowledgmentComponent', () => {
      component.showAcknowledgmentTransferForm();
      expect(matDialogSpy.open).toHaveBeenCalledWith(TransferAcknowledgmentComponent, {
        disableClose: true,
        data: {
          tenantIdentifier: '1',
        },
      });
    });

    describe('submit', () => {
      it('should check all criteria as included when submit', () => {
        component.submit(true);
        component.searchCriterias.forEach((criteria) => {
          criteria.values.forEach((criteriaValue) => {
            expect(criteriaValue.status).toEqual(SearchCriteriaStatusEnum.NOT_INCLUDED);
          });
        });
      });
    });

    describe('DOM', () => {
      it('should have 5 rows ', () => {
        // When
        const nativeElement = fixture.nativeElement;
        const elementRow = nativeElement.querySelectorAll('.row');

        // Then
        expect(elementRow.length).toBe(5);
      });

      it('should have 1 vitamui-menu-button ', () => {
        // When
        const nativeElement = fixture.nativeElement;
        const elementRow = nativeElement.querySelectorAll('vitamui-menu-button');

        // Then
        expect(elementRow.length).toBe(1);
      });
      it('should have 2 buttons ', () => {
        const elementBtn = fixture.nativeElement.querySelectorAll('button[type=button]');
        expect(elementBtn.length).toBe(2);
      });
    });

    describe('checkChildrenBoxChange', () => {
      it('should include the unselected child when parent is checked, into the list listOfUAIdToExclude', () => {
        component.isAllChecked = true;
        const event = {
          checked: false,
        };
        const unit = {
          '#id': '1234',
          '#unitups': [''],
          '#allunitups': [''],
          '#unitType': UnitType.HOLDING_UNIT,
          '#opi': '1234',
        };
        component.checkChildrenBoxChange(unit, event as any);
        expect(component.listOfUAIdToExclude.length).toBe(1);
        expect(component.listOfUAIdToExclude[0]).toEqual({ value: '1234', id: '1234' });
        expect(component.listOfUAIdToInclude.length).toBe(0);
        expect(component.isIndeterminate).toBe(true);
        expect(component.selectedItemCount).toBe(0);
        expect(component.itemNotSelected).toBe(0);
        expect(component.selectedHoldingUnitItemCount).toBe(0);
      });

      it('should exclude the selected child when parent is checked, from the list listOfUAIdToExclude', () => {
        component.isAllChecked = true;
        component.itemNotSelected = 1;
        const event = {
          checked: true,
        };
        const unit = {
          '#id': '1234',
          '#unitups': [''],
          '#allunitups': [''],
          '#unitType': UnitType.HOLDING_UNIT,
          '#opi': '1234',
        };
        component.checkChildrenBoxChange(unit, event as any);
        expect(component.listOfUAIdToExclude.length).toBe(0);
        expect(component.listOfUAIdToInclude.length).toBe(0);
        expect(component.isIndeterminate).toBeFalsy();
        expect(component.selectedItemCount).toBe(1);
        expect(component.itemNotSelected).toBe(0);
        expect(component.selectedHoldingUnitItemCount).toBe(1);
      });

      it('should include the selected child when parent is unchecked, into the list listOfUAIdToInclude', () => {
        component.isAllChecked = false;
        const event = {
          checked: true,
        };
        const unit = {
          '#id': '1234',
          '#unitups': [''],
          '#allunitups': [''],
          '#unitType': UnitType.HOLDING_UNIT,
          '#opi': '1234',
        };
        component.checkChildrenBoxChange(unit, event as any);
        expect(component.listOfUAIdToInclude.length).toBe(1);
        expect(component.listOfUAIdToInclude[0]).toEqual({ value: '1234', id: '1234' });
        expect(component.listOfUAIdToExclude.length).toBe(0);
        expect(component.isIndeterminate).toBeFalsy();
        expect(component.selectedItemCount).toBe(1);
        expect(component.itemNotSelected).toBe(0);
        expect(component.selectedHoldingUnitItemCount).toBe(1);
      });

      it('should not include the unselected child when parent is unchecked, into the list listOfUAIdToInclude', () => {
        component.isAllChecked = false;
        const event = {
          checked: false,
        };
        const unit = {
          '#id': '1234',
          '#unitups': [''],
          '#allunitups': [''],
          '#unitType': UnitType.HOLDING_UNIT,
          '#opi': '1234',
        };
        component.checkChildrenBoxChange(unit, event as any);
        expect(component.listOfUAIdToInclude.length).toBe(0);
        expect(component.listOfUAIdToExclude.length).toBe(0);
        expect(component.isIndeterminate).toBeFalsy();
        expect(component.selectedItemCount).toBe(0);
        expect(component.itemNotSelected).toBe(0);
        expect(component.selectedHoldingUnitItemCount).toBe(0);
      });

      it('should not increase selectedHoldingUnitItemCount if unitType is not HOLDING_UNIT', () => {
        component.isAllChecked = false;
        const event = {
          checked: true,
        };
        const unit = {
          '#id': '1234',
          '#unitups': [''],
          '#allunitups': [''],
          '#unitType': UnitType.FILING_UNIT,
          '#opi': '1234',
        };
        component.checkChildrenBoxChange(unit, event as any);
        expect(component.listOfUAIdToInclude.length).toBe(1);
        expect(component.listOfUAIdToInclude[0]).toEqual({ value: '1234', id: '1234' });
        expect(component.listOfUAIdToExclude.length).toBe(0);
        expect(component.isIndeterminate).toBeFalsy();
        expect(component.selectedItemCount).toBe(1);
        expect(component.itemNotSelected).toBe(0);
        expect(component.selectedHoldingUnitItemCount).toBe(0);
      });
    });
  });

  describe('queryParams', () => {
    it('should be set to archives with or without object by default', async () => {
      const { routerSpy } = await setupTest({});
      expect(vi.mocked(routerSpy.navigate).mock.calls[0][1].queryParams).toEqual({
        archiveUnitType: 'ARCHIVE_UNIT_WITH_OBJECTS,ARCHIVE_UNIT_WITHOUT_OBJECTS',
      });
    });

    it('should trigger a search with criteria matching the queryParams in the URL on page access', async () => {
      vi.useFakeTimers();

      await setupTest({ opi: '1234' });

      // flush ready().then() promise chain
      await fixture.whenStable();

      // flush the setTimeout(() => this.submit(true)) inside ngAfterViewInit
      vi.runAllTimers();

      // let Angular process the submit() call
      await fixture.whenStable();

      const calls = vi.mocked(archiveServiceStub.searchArchiveUnitsByCriteria as Mock).mock.calls;

      vi.useRealTimers();

      expect(calls.length).toBeGreaterThan(0);

      const matchingCall = calls
        .map((call) => call[0])
        .find((criteria) =>
          criteria?.criteriaList?.some((c: any) => c.criteria === 'opi' && c.values?.some((v: any) => v.value === '1234')),
        );

      expect(matchingCall).toBeTruthy();
    });
    it('should update criteria when a virtual node is checked', async () => {
      await setupTest({ opi: '1234' });

      await fixture.whenStable();

      const virtualNode1: NodeData = {
        checked: true,
        virtualPath: 'virtualPath',
        id: 'VIRTUAL',
        realParentId: 'someRealParentId',
        realParentTitle: 'someRealParentTitle',
        isVirtual: true,
        title: 'virtualPath',
      };

      const virtualNode2: NodeData = {
        checked: true,
        virtualPath: 'virtualPath',
        id: 'VIRTUAL',
        realParentId: 'someOtherRealParentId',
        realParentTitle: 'someOtherRealParentTitle',
        isVirtual: true,
        title: 'virtualPath',
      };

      archiveSharedDataService.emitNode(virtualNode1);
      archiveSharedDataService.emitNode(virtualNode2);

      expect(component.searchCriterias.has('VIRTUAL')).toBe(true);

      const virtualCriteria = component.searchCriterias.get('VIRTUAL');
      expect(virtualCriteria?.values?.length).toEqual(2);

      const virtualValues = virtualCriteria.values.filter((value) => value?.value?.id === 'VIRTUAL');

      expect(virtualValues.map((o) => o.value.virtualNodeRealParentId)).toEqual(
        arrayWithExactContents(['someRealParentId', 'someOtherRealParentId']),
      );

      expect(virtualValues.map((o) => o.value.virtualNodeRealParentTitle)).toEqual(
        arrayWithExactContents(['someRealParentTitle', 'someOtherRealParentTitle']),
      );
    });

    it('should update criteria when a virtual node is unchecked', async () => {
      await setupTest({ opi: '1234' });

      await fixture.whenStable();

      let virtualNode1: NodeData = {
        checked: true,
        virtualPath: 'virtualPath',
        id: 'VIRTUAL',
        realParentId: 'someRealParentId',
        realParentTitle: 'someRealParentTitle',
        isVirtual: true,
        title: 'virtualPath',
      };

      let virtualNode2: NodeData = {
        checked: true,
        virtualPath: 'virtualPath',
        id: 'VIRTUAL',
        realParentId: 'someOtherRealParentId',
        realParentTitle: 'someOtherRealParentTitle',
        isVirtual: true,
        title: 'virtualPath',
      };

      archiveSharedDataService.emitNode(virtualNode1);
      archiveSharedDataService.emitNode(virtualNode2);

      expect(component.searchCriterias.has('VIRTUAL')).toBe(true);

      let virtualCriteria = component.searchCriterias.get('VIRTUAL');
      expect(virtualCriteria?.values?.length).toEqual(2);

      let virtualValues = virtualCriteria.values.filter((value) => value?.value?.id === 'VIRTUAL');

      virtualNode1.checked = false;
      archiveSharedDataService.emitNode(virtualNode1);
      expect(component.searchCriterias.has('VIRTUAL')).toBe(true);
      expect(virtualCriteria?.values?.length).toEqual(1);

      expect('someRealParentId').toEqual(virtualValues[0].value.virtualNodeRealParentId);
      expect('someRealParentTitle').toEqual(virtualValues[0].value.virtualNodeRealParentTitle);
    });
  });
});
