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
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTreeModule, MatTreeNestedDataSource } from '@angular/material/tree';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { DescriptionLevel, FilingHoldingSchemeNode, InjectorModule, LoggerModule } from 'ui-frontend-common';
import { ArchiveSharedDataService } from '../../../core/archive-shared-data.service';
import { ArchiveService } from '../../archive.service';
import { ArchiveFacetsService } from '../../common-services/archive-facets.service';
import { PagedResult, ResultFacet } from '../../models/search.criteria';
import { newTreeNode } from '../filing-holding-scheme.handler.spec';
import { LeavesTreeComponent } from './leaves-tree.component';

const translations: any = { TEST: 'Mock translate test' };

class FakeLoader implements TranslateLoader {
  getTranslation(): Observable<any> {
    return of(translations);
  }
}

describe('LeavesTreeComponent', () => {
  let component: LeavesTreeComponent;
  let fixture: ComponentFixture<LeavesTreeComponent>;

  let nestedDataSource: MatTreeNestedDataSource<FilingHoldingSchemeNode>;
  let resultFacets: ResultFacet[];

  const pagedResult: PagedResult = { pageNumbers: 1, facets: [], results: [], totalResults: 1 };

  const archiveServiceStub = {
    loadFilingHoldingSchemeTree: () => of([]),
    getOntologiesFromJson: () => of([]),
    searchArchiveUnitsByCriteria: () => of(pagedResult),
    hasArchiveSearchRole: () => of(true),
    getAccessContractById: () => of({}),
    hasAccessContractPermissions: () => of(true)
  };
  const archiveFacetsServicStube = {
    extractNodesFacetsResults: () => of(),
    extractRulesFacetsResults: () => of(),
  };
  const archiveSharedDataServiceStub = {
    getSearchCriterias: () => of(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        MatMenuModule,
        MatTreeModule,
        MatProgressSpinnerModule,
        MatSidenavModule,
        InjectorModule,
        LoggerModule.forRoot(),
        TranslateModule.forRoot({
          loader: { provide: TranslateLoader, useClass: FakeLoader },
        }),
        MatSnackBarModule,
        HttpClientTestingModule,
        RouterTestingModule,
      ],
      declarations: [LeavesTreeComponent],
      providers: [
        { provide: ArchiveService, useValue: archiveServiceStub },
        { provide: ArchiveSharedDataService, useValue: archiveSharedDataServiceStub },
        { provide: ArchiveFacetsService, useValue: archiveFacetsServicStube },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LeavesTreeComponent);
    component = fixture.componentInstance;
    component.accessContract = 'accessContractForTest';
    component.loadingNodeUnit = true;
    nestedDataSource = new MatTreeNestedDataSource();
    nestedDataSource.data = [
      newTreeNode('node-0', 1, [
        newTreeNode('node-0-0', 0),
        newTreeNode('node-0-1', 1),
        newTreeNode('node-0-2', 0),
        newTreeNode('node-0-3', 0),
        newTreeNode('node-0-4', 0),
      ]),
    ];
    component.nestedDataSourceLeaves = nestedDataSource;
    resultFacets = [
      { node: 'node-0', count: 1 },
      { node: 'node-0-1', count: 1 },
    ];
    component.searchRequestResultFacets = resultFacets;
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should return true when isLoadingChildren', () => {
    const filingHoldingSchemaNode: FilingHoldingSchemeNode = {
      id: 'filingHoldingSchemaNodeId',
      title: 'string',
      type: 'INGEST',
      descriptionLevel: 'Item',
      label: 'string',
      children: [],
      count: 55,
      vitamId: 'vitamId',
      checked: true,
      hidden: false,
      isLoadingChildren: true,
      paginatedChildrenLoaded: 5,
      canLoadMoreMatchingChildren: true,
    };
    component.showEveryNodes = false;
    const response = component.canLoadMoreUAForNode(filingHoldingSchemaNode);
    expect(response).toBeFalsy();
  });

  it('should return true when count is defined', () => {
    const filingHoldingSchemaNode: FilingHoldingSchemeNode = {
      id: 'filingHoldingSchemaNodeId',
      title: 'string',
      type: 'HoldingSchema',
      label: 'string',
      children: [],
      vitamId: 'string',
      checked: true,
      count: 19,
      hidden: false,
      isLoadingChildren: false,
      paginatedChildrenLoaded: 5,
    };
    const response = component.nodeHasPositiveCount(filingHoldingSchemaNode);
    expect(response).toBeTruthy();
  });

  it('LeavesTreeComponent should be stable after creation', () => {
    expect(component.showEveryNodes).toBeFalsy();
    expect(component.nestedTreeControlLeaves).toBeDefined();
  });

  it('should return false when the unit type is not an Ingest', () => {
    const filingHoldingSchemaNode: FilingHoldingSchemeNode = {
      id: 'filingHoldingSchemaNodeId',
      title: 'string',
      type: 'HoldingSchema',
      label: 'string',
      children: [],
      count: 55,
      vitamId: 'vitamId',
      checked: true,
      isLoadingChildren: false,
      paginatedChildrenLoaded: 5,
    };
    const response = component.nodeIsUAWithoutChildren(1, filingHoldingSchemaNode);
    expect(response).toBeFalsy();
  });

  it('should return false when the count is not defined', () => {
    const filingHoldingSchemaNode: FilingHoldingSchemeNode = {
      id: 'filingHoldingSchemaNodeId',
      title: 'string',
      type: 'INGEST',
      label: 'string',
      children: [],
      vitamId: 'vitamId',
      checked: true,
      hidden: false,
      isLoadingChildren: false,
      paginatedChildrenLoaded: 5,
    };
    const response = component.nodeHasPositiveCount(filingHoldingSchemaNode);
    expect(response).toBeFalsy();
  });

  it('showEveryNodes should be true', () => {
    component.showEveryNodes = false;
    component.switchViewAllNodes();
    expect(component.showEveryNodes).toBeTruthy();
  });

  it('should return false when the unit type is not an Ingest', () => {
    const filingHoldingSchemaNode: FilingHoldingSchemeNode = {
      id: 'filingHoldingSchemaNodeId',
      title: 'string',
      unitType: 'HoldingSchema',
      label: 'string',
      children: [],
      count: 55,
      vitamId: 'vitamId',
      checked: true,
      isLoadingChildren: false,
      paginatedChildrenLoaded: 5,
    };
    const response = component.nodeIsUAWithChildren(1, filingHoldingSchemaNode);
    expect(response).toBeFalsy();
  });

  it('should return true when the unit type is an Ingest', () => {
    const node: FilingHoldingSchemeNode = {
      id: 'filingHoldingSchemaNodeId',
      title: 'string',
      unitType: 'INGEST',
      descriptionLevel: DescriptionLevel.ITEM,
      label: 'string',
      children: [],
      count: 55,
      vitamId: 'vitamId',
      checked: true,
      isLoadingChildren: false,
      paginatedChildrenLoaded: 5,
    };
    const response = component.nodeIsUAWithoutChildren(1, node);
    expect(response).toBeTruthy();
  });

  it('should return false when isLoadingChildren', () => {
    const filingHoldingSchemaNode: FilingHoldingSchemeNode = {
      id: 'filingHoldingSchemaNodeId',
      title: 'string',
      unitType: 'INGEST',
      descriptionLevel: 'Item',
      label: 'string',
      children: [],
      count: 55,
      vitamId: 'vitamId',
      checked: true,
      isLoadingChildren: true,
      paginatedChildrenLoaded: 5,
    };
    const response = component.canLoadMoreUAForNode(filingHoldingSchemaNode);
    expect(response).toBeFalsy();
  });

  it('should return vitamui-icon-folder as response ', () => {
    const filingHoldingSchemaNode: FilingHoldingSchemeNode = {
      id: 'filingHoldingSchemaNodeId',
      title: 'string',
      unitType: 'INGEST',
      descriptionLevel: 'Item',
      label: 'string',
      children: [],
      count: 55,
      vitamId: 'vitamId',
      checked: true,
      isLoadingChildren: true,
      paginatedChildrenLoaded: 5,
    };

    const response = component.getNodeUnitIcon(filingHoldingSchemaNode);

    expect(response).toEqual('vitamui-icon-folder');
  });

  describe('DOM', () => {
    it('should have 1 tree title ', () => {
      // When
      const nativeElement = fixture.nativeElement;
      const elementTitle = nativeElement.querySelectorAll('.tree-title');

      // Then
      expect(elementTitle.length).toBe(1);
      expect(elementTitle[0].textContent).toContain('ARCHIVE_SEARCH.FILING_SHCEMA.TREE_LEAVES_TITLE');
    });
  });
});
