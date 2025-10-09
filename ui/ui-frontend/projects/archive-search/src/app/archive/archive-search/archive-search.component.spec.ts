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
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { environment } from 'projects/archive-search/src/environments/environment';
import { Observable, of } from 'rxjs';
import {
  BASE_URL,
  InjectorModule,
  LoggerModule,
  PagedResult,
  SchemaService,
  SearchCriteriaDto,
  SearchCriteriaStatusEnum,
  UnitType,
  VitamuiRoles,
} from 'vitamui-library';
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
import arrayWithExactContents = jasmine.arrayWithExactContents;

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
  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);

  matDialogSpy.open.and.returnValue({
    afterClosed: jasmine.createSpy('afterClosed').and.returnValue(of(true)), // Simule une fermeture normale
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

  const computeActivatedRoute = (queryParams: Params = {}) => {
    return {
      params: of({ tenantIdentifier: 1 }),
      queryParamMap: of({ keys: Object.keys(queryParams) }),
      snapshot: { queryParamMap: { keys: Object.keys(queryParams) }, queryParams: queryParams },
    };
  };

  const setupTest = async (queryParams: Params) => {
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    spyOn(archiveServiceStub, 'searchArchiveUnitsByCriteria').and.callThrough();

    await TestBed.configureTestingModule({
      declarations: [ArchiveSearchComponent, SimpleCriteriaSearchComponent],
      schemas: [NO_ERRORS_SCHEMA],
      imports: [
        InjectorModule,
        LoggerModule.forRoot(),
        MatMenuModule,
        MatProgressSpinnerModule,
        MatSidenavModule,
        MatTreeModule,
        RouterTestingModule,
        TranslateModule.forRoot({
          loader: { provide: TranslateLoader, useClass: FakeLoader },
        }),
      ],
      providers: [
        ArchiveSearchHelperService,
        ArchiveSharedDataService,
        { provide: ActivatedRoute, useValue: computeActivatedRoute(queryParams) },
        { provide: ArchiveService, useValue: archiveServiceStub },
        { provide: ArchiveUnitDipService, useValue: archiveUnitDipServiceMock },
        { provide: ArchiveUnitEliminationService, useValue: archiveUnitEliminationServiceMock },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ComputeInheritedRulesService, useValue: computeInheritedRulesServiceMock },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: Router, useValue: routerSpy },
        { provide: SchemaService, useValue: { getDescriptiveSchemaTree: () => of(), getSchema: () => of([]) } },
        { provide: UpdateUnitManagementRuleService, useValue: updateUnitManagementRuleServiceMock },
        { provide: environment, useValue: environment },
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

    it('should have the corrects values', () => {
      expect(component.DEFAULT_ELIMINATION_ANALYSIS_THRESHOLD).toEqual(100000);
      expect(component.DEFAULT_DIP_EXPORT_THRESHOLD).toEqual(100000);
      expect(component.DEFAULT_ELIMINATION_THRESHOLD).toEqual(10000);
      expect(component.DEFAULT_TRANSFER_THRESHOLD).toEqual(100000);
      expect(component.DEFAULT_UPDATE_MGT_RULES_THRESHOLD).toEqual(100000);
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
      spyOn(archiveServiceStub, 'hasArchiveSearchRole').and.callThrough();
      // When
      component.checkUserHasRole(VitamuiRoles.ROLE_EXPORT_DIP, 1);

      // Then
      expect(archiveServiceStub.hasArchiveSearchRole).toHaveBeenCalled();
    });
    it('should open a modal with TransferAcknowledgmentComponent', () => {
      component.accessContractId = 'accessContract';
      component.showAcknowledgmentTransferForm();
      expect(matDialogSpy.open).toHaveBeenCalledWith(TransferAcknowledgmentComponent, {
        disableClose: true,
        data: {
          accessContract: 'accessContract',
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
        const event: Event = jasmine.createSpyObj<Event>(['stopPropagation'], { target: { checked: false } as HTMLInputElement });
        const unit = {
          '#id': '1234',
          '#unitups': [''],
          '#allunitups': [''],
          '#unitType': UnitType.HOLDING_UNIT,
          '#opi': '1234',
        };
        component.checkChildrenBoxChange(unit, event);
        expect(component.listOfUAIdToExclude.length).toBe(1);
        expect(component.listOfUAIdToExclude[0]).toEqual({ value: '1234', id: '1234' });
        expect(component.listOfUAIdToInclude.length).toBe(0);
        expect(component.isIndeterminate).toBeTrue();
        expect(component.selectedItemCount).toBe(0);
        expect(component.itemNotSelected).toBe(0);
        expect(component.selectedHoldingUnitItemCount).toBe(0);
        expect(event.stopPropagation).toHaveBeenCalled();
      });

      it('should exclude the selected child when parent is checked, from the list listOfUAIdToExclude', () => {
        component.isAllChecked = true;
        component.itemNotSelected = 1;
        const event: Event = jasmine.createSpyObj<Event>(['stopPropagation'], { target: { checked: true } as HTMLInputElement });
        const unit = {
          '#id': '1234',
          '#unitups': [''],
          '#allunitups': [''],
          '#unitType': UnitType.HOLDING_UNIT,
          '#opi': '1234',
        };
        component.checkChildrenBoxChange(unit, event);
        expect(component.listOfUAIdToExclude.length).toBe(0);
        expect(component.listOfUAIdToInclude.length).toBe(0);
        expect(component.isIndeterminate).toBeFalsy();
        expect(component.selectedItemCount).toBe(1);
        expect(component.itemNotSelected).toBe(0);
        expect(component.selectedHoldingUnitItemCount).toBe(1);
        expect(event.stopPropagation).toHaveBeenCalled();
      });

      it('should include the selected child when parent is unchecked, into the list listOfUAIdToInclude', () => {
        component.isAllChecked = false;
        const event: Event = jasmine.createSpyObj<Event>(['stopPropagation'], { target: { checked: true } as HTMLInputElement });
        const unit = {
          '#id': '1234',
          '#unitups': [''],
          '#allunitups': [''],
          '#unitType': UnitType.HOLDING_UNIT,
          '#opi': '1234',
        };
        component.checkChildrenBoxChange(unit, event);
        expect(component.listOfUAIdToInclude.length).toBe(1);
        expect(component.listOfUAIdToInclude[0]).toEqual({ value: '1234', id: '1234' });
        expect(component.listOfUAIdToExclude.length).toBe(0);
        expect(component.isIndeterminate).toBeFalsy();
        expect(component.selectedItemCount).toBe(1);
        expect(component.itemNotSelected).toBe(0);
        expect(component.selectedHoldingUnitItemCount).toBe(1);
        expect(event.stopPropagation).toHaveBeenCalled();
      });

      it('should not include the unselected child when parent is unchecked, into the list listOfUAIdToInclude', () => {
        component.isAllChecked = false;
        const event: Event = jasmine.createSpyObj<Event>(['stopPropagation'], { target: { checked: false } as HTMLInputElement });
        const unit = {
          '#id': '1234',
          '#unitups': [''],
          '#allunitups': [''],
          '#unitType': UnitType.HOLDING_UNIT,
          '#opi': '1234',
        };
        component.checkChildrenBoxChange(unit, event);
        expect(component.listOfUAIdToInclude.length).toBe(0);
        expect(component.listOfUAIdToExclude.length).toBe(0);
        expect(component.isIndeterminate).toBeFalsy();
        expect(component.selectedItemCount).toBe(0);
        expect(component.itemNotSelected).toBe(0);
        expect(component.selectedHoldingUnitItemCount).toBe(0);
        expect(event.stopPropagation).toHaveBeenCalled();
      });

      it('should not increase selectedHoldingUnitItemCount if unitType is not HOLDING_UNIT', () => {
        component.isAllChecked = false;
        const event: Event = jasmine.createSpyObj<Event>(['stopPropagation'], { target: { checked: true } as HTMLInputElement });
        const unit = {
          '#id': '1234',
          '#unitups': [''],
          '#allunitups': [''],
          '#unitType': UnitType.FILING_UNIT,
          '#opi': '1234',
        };
        component.checkChildrenBoxChange(unit, event);
        expect(component.listOfUAIdToInclude.length).toBe(1);
        expect(component.listOfUAIdToInclude[0]).toEqual({ value: '1234', id: '1234' });
        expect(component.listOfUAIdToExclude.length).toBe(0);
        expect(component.isIndeterminate).toBeFalsy();
        expect(component.selectedItemCount).toBe(1);
        expect(component.itemNotSelected).toBe(0);
        expect(component.selectedHoldingUnitItemCount).toBe(0);
        expect(event.stopPropagation).toHaveBeenCalled();
      });
    });
  });

  describe('queryParams', () => {
    it('should be set to archives with or without object by default', async () => {
      const { routerSpy } = await setupTest({});
      expect(routerSpy.navigate.calls.first().args[1].queryParams).toEqual({
        archiveUnitType: 'ARCHIVE_UNIT_WITH_OBJECTS,ARCHIVE_UNIT_WITHOUT_OBJECTS',
      });
    });

    it('should trigger a search with criteria matching the queryParams in the URL on page access', async () => {
      await setupTest({ opi: '1234' });

      await fixture.whenStable();

      const firstCall = (archiveServiceStub.searchArchiveUnitsByCriteria as jasmine.Spy).calls.first().args[0];

      expect(firstCall).toEqual(
        jasmine.objectContaining({
          criteriaList: jasmine.arrayContaining([
            jasmine.objectContaining({
              criteria: 'opi',
              values: [jasmine.objectContaining({ id: 'opi', value: '1234' })],
            }),
          ]),
        }),
      );
    });
    it('should update criteria when a virtual node is checked', async () => {
      await setupTest({ opi: '1234' });

      await fixture.whenStable();

      const virtualNode1: NodeData = {
        checked: true,
        virtualPath: 'virtualPath',
        id: 'virtualPath',
        realParentId: 'someRealParentId',
        realParentTitle: 'someRealParentTitle',
        isVirtual: true,
        title: 'virtualPath',
      };

      const virtualNode2: NodeData = {
        checked: true,
        virtualPath: 'virtualPath',
        id: 'virtualPath',
        realParentId: 'someOtherRealParentId',
        realParentTitle: 'someOtherRealParentTitle',
        isVirtual: true,
        title: 'virtualPath',
      };

      archiveSharedDataService.emitNode(virtualNode1);
      archiveSharedDataService.emitNode(virtualNode2);
      fixture.detectChanges();

      expect(component.searchCriterias.has('VIRTUAL')).toBeTrue();

      const virtualCriteria = component.searchCriterias.get('VIRTUAL');
      expect(virtualCriteria?.values?.length).toEqual(2);

      const virtualValues = virtualCriteria.values.filter((value) => value?.value?.id === 'virtualPath');

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
        id: 'virtualPath',
        realParentId: 'someRealParentId',
        realParentTitle: 'someRealParentTitle',
        isVirtual: true,
        title: 'virtualPath',
      };

      let virtualNode2: NodeData = {
        checked: true,
        virtualPath: 'virtualPath',
        id: 'virtualPath',
        realParentId: 'someOtherRealParentId',
        realParentTitle: 'someOtherRealParentTitle',
        isVirtual: true,
        title: 'virtualPath',
      };

      archiveSharedDataService.emitNode(virtualNode1);
      archiveSharedDataService.emitNode(virtualNode2);
      fixture.detectChanges();

      expect(component.searchCriterias.has('VIRTUAL')).toBeTrue();

      let virtualCriteria = component.searchCriterias.get('VIRTUAL');
      expect(virtualCriteria?.values?.length).toEqual(2);

      let virtualValues = virtualCriteria.values.filter((value) => value?.value?.id === 'virtualPath');

      virtualNode1.checked = false;
      archiveSharedDataService.emitNode(virtualNode1);
      expect(component.searchCriterias.has('VIRTUAL')).toBeTrue();
      expect(virtualCriteria?.values?.length).toEqual(1);

      expect('someRealParentId').toEqual(virtualValues[0].value.virtualNodeRealParentId);
      expect('someRealParentTitle').toEqual(virtualValues[0].value.virtualNodeRealParentTitle);
    });
  });
});
