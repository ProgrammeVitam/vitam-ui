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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSidenavModule } from '@angular/material/sidenav';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { environment } from 'projects/collect/src/environments/environment';
import { Observable, of } from 'rxjs';
import {
  BASE_URL,
  ConfigService,
  ExternalParameters,
  ExternalParametersService,
  InjectorModule,
  LoggerModule,
  PagedResult,
  Project,
  SchemaService,
  SearchCriteriaDto,
  Transaction,
  TransactionStatus,
} from 'vitamui-library';

import { ArchiveSearchCollectComponent } from './archive-search-collect.component';
import { ArchiveSearchHelperService } from './archive-search-criteria/services/archive-search-helper.service';
import { ArchiveSharedDataService } from '../core/archive-shared-data.service';
import { ArchiveCollectService } from './archive-collect.service';
import { SimpleCriteriaSearchComponent } from './archive-search-criteria/components/simple-criteria-search/simple-criteria-search.component';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { MatMenuModule } from '@angular/material/menu';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { NodeData } from '../../../../../archive-search/src/app/archive/models/nodedata.interface';
import arrayWithExactContents = jasmine.arrayWithExactContents;

const translations: any = { TEST: 'Mock translate test' };

class FakeLoader implements TranslateLoader {
  getTranslation(): Observable<any> {
    return of(translations);
  }
}

describe('ArchiveSearchCollectComponent', () => {
  let component: ArchiveSearchCollectComponent;
  let fixture: ComponentFixture<ArchiveSearchCollectComponent>;
  const pagedResult: PagedResult = { pageNumbers: 1, facets: [], results: [], totalResults: 1 };
  let archiveSharedDataService: ArchiveSharedDataService;
  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
  matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

  const project: Project = {} as Project;

  const transaction: Transaction = {
    id: 'transactionId',
    archivalAgreement: 'archivalAgreement',
    messageIdentifier: 'messageIdentifier',
    archivalAgencyIdentifier: 'archivalAgencyIdentifier',
    transferringAgencyIdentifier: 'transferringAgencyIdentifier',
    originatingAgencyIdentifier: 'originatingAgencyIdentifier',
    submissionAgencyIdentifier: 'submissionAgencyIdentifier',
    archiveProfile: 'archivalProfile',
    projectId: 'ProjectId',
    comment: 'I am a comment',
    status: TransactionStatus.SENDING,
    legalStatus: 'A legal status',
    acquisitionInformation: 'Protocol',
  };

  const archiveCollectServiceStub = {
    getAccessContractById: () => of({}),
    getLastTransactionByProjectId: () => of(transaction),
    getProjectById: () => of(project),
    getTotalTrackHitsByCriteria: () => of(42),
    hasCollectRole: () => of(false),
    loadFilingHoldingSchemeTree: () => of([]),
    searchArchiveUnitsByCriteria: (_criteriaDto: SearchCriteriaDto, _transactionId: string) => of(pagedResult),
  };

  const externalParametersServiceStub = {
    getUserExternalParameters: () => of(new Map<string, string>([[ExternalParameters.PARAM_ACCESS_CONTRACT, 'SomeAccessContract']])),
  };

  const computeActivatedRoute = (queryParams: Params = {}) => {
    return {
      params: of({ tenantIdentifier: 1 }),
      queryParamMap: of({ keys: Object.keys(queryParams) }),
      data: of(),
      snapshot: {
        queryParamMap: {
          keys: Object.keys(queryParams),
        },
        queryParams: queryParams,
      },
    };
  };

  const setupTest = async (queryParams: Params, withSimpleCriteria = false) => {
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    spyOn(archiveCollectServiceStub, 'searchArchiveUnitsByCriteria').and.callThrough();

    const declarations = withSimpleCriteria
      ? [ArchiveSearchCollectComponent, SimpleCriteriaSearchComponent]
      : [ArchiveSearchCollectComponent];

    await TestBed.configureTestingModule({
      declarations: declarations,
      schemas: [NO_ERRORS_SCHEMA],
      imports: [
        BrowserAnimationsModule,
        InjectorModule,
        LoggerModule.forRoot(),
        MatMenuModule,
        MatSidenavModule,
        RouterTestingModule,
        TranslateModule.forRoot({
          loader: { provide: TranslateLoader, useClass: FakeLoader },
        }),
      ],
      providers: [
        ArchiveSearchHelperService,
        ArchiveSharedDataService,
        { provide: ActivatedRoute, useValue: computeActivatedRoute(queryParams) },
        { provide: ArchiveCollectService, useValue: archiveCollectServiceStub },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ConfigService, useValue: { config$: of() } },
        { provide: ExternalParametersService, useValue: externalParametersServiceStub },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: Router, useValue: routerSpy },
        { provide: SchemaService, useValue: { getDescriptiveSchemaTree: () => of(), getSchema: () => of([]) } },
        { provide: environment, useValue: environment },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ArchiveSearchCollectComponent);
    component = fixture.componentInstance;
    component.transaction = transaction;
    archiveSharedDataService = TestBed.inject(ArchiveSharedDataService);
    fixture.detectChanges();

    return { routerSpy };
  };

  describe('', () => {
    beforeEach(async () => await setupTest({}));

    it('component should be created', () => {
      expect(component).toBeTruthy();
    });

    it('Some parameters should be true after initializing selection', () => {
      // When
      component.submit();

      // Then
      expect(component.submited).toBeTruthy();
      expect(component.itemSelected).toBe(0);
    });

    it('Some parameters should be false after initializing selection', () => {
      // When
      component.submit();

      // Then
      expect(component.isIndeterminate).toBeFalsy();
      expect(component.isAllChecked).toBeFalsy();
      expect(component.itemNotSelected).toBe(0);
    });

    describe('checkChildrenBoxChange', () => {
      it('should include the unselected child when parent is checked, into the list listOfUAIdToExclude', () => {
        component.isAllChecked = true;
        const event: Event = jasmine.createSpyObj<Event>(['stopPropagation'], { target: { checked: false } as HTMLInputElement });
        component.checkChildrenBoxChange('1234', event);
        expect(component.listOfUAIdToExclude.length).toBe(1);
        expect(component.listOfUAIdToExclude[0]).toEqual({ value: '1234', id: '1234' });
        expect(component.listOfUAIdToInclude.length).toBe(0);
        expect(component.isIndeterminate).toBeTrue();
        expect(component.itemSelected).toBe(0);
        expect(component.itemNotSelected).toBe(0);
        expect(event.stopPropagation).toHaveBeenCalled();
      });

      it('should exclude the selected child when parent is checked, from the list listOfUAIdToExclude', () => {
        component.isAllChecked = true;
        component.itemNotSelected = 1;
        const event: Event = jasmine.createSpyObj<Event>(['stopPropagation'], { target: { checked: true } as HTMLInputElement });
        component.checkChildrenBoxChange('1234', event);
        expect(component.listOfUAIdToExclude.length).toBe(0);
        expect(component.listOfUAIdToInclude.length).toBe(0);
        expect(component.isIndeterminate).toBeFalsy();
        expect(component.itemSelected).toBe(1);
        expect(component.itemNotSelected).toBe(0);
        expect(event.stopPropagation).toHaveBeenCalled();
      });

      it('should include the selected child when parent is unchecked, into the list listOfUAIdToInclude', () => {
        component.isAllChecked = false;
        const event: Event = jasmine.createSpyObj<Event>(['stopPropagation'], { target: { checked: true } as HTMLInputElement });
        component.checkChildrenBoxChange('1234', event);
        expect(component.listOfUAIdToInclude.length).toBe(1);
        expect(component.listOfUAIdToInclude[0]).toEqual({ value: '1234', id: '1234' });
        expect(component.listOfUAIdToExclude.length).toBe(0);
        expect(component.isIndeterminate).toBeFalsy();
        expect(component.itemSelected).toBe(1);
        expect(component.itemNotSelected).toBe(0);
      });

      it('should not include the unselected child when parent is unchecked, into the list listOfUAIdToInclude', () => {
        component.isAllChecked = false;
        const event: Event = jasmine.createSpyObj<Event>(['stopPropagation'], { target: { checked: false } as HTMLInputElement });
        component.checkChildrenBoxChange('1234', event);
        expect(component.listOfUAIdToInclude.length).toBe(0);
        expect(component.listOfUAIdToExclude.length).toBe(0);
        expect(component.isIndeterminate).toBeFalsy();
        expect(component.itemSelected).toBe(0);
        expect(component.itemNotSelected).toBe(0);
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

    // FIXME: disabled because for some reason it raises an ExpressionChangedAfterItHasBeenCheckedError
    xit('should trigger a search with criteria matching the queryParams in the URL on page access', async () => {
      await setupTest({ guid: '1234' }, true);

      await fixture.whenStable();

      expect(archiveCollectServiceStub.searchArchiveUnitsByCriteria).toHaveBeenCalledOnceWith(
        jasmine.objectContaining({
          criteriaList: [
            {
              criteria: 'GUID',
              values: [{ id: 'guid', value: '1234' }],
              operator: 'EQ',
              category: 'FIELDS',
              dataType: 'STRING',
            },
          ],
        }),
        transaction.id,
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
