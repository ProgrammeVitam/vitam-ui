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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { DescriptionLevel, FilingHoldingSchemeNode, ResultFacet, SearchCriteriaDto, UnitType } from 'vitamui-library';
import { ArchiveCollectService } from '../../../../archive-collect.service';
import { ArchiveFacetsService } from '../../../services/archive-facets.service';
import { ArchiveSharedDataService } from '../../../services/archive-shared-data.service';
import { LeavesTreeComponent } from './leaves-tree.component';

export function newNode(
  currentId: string,
  currentChildren: FilingHoldingSchemeNode[] = [],
  currentDescriptionLevel: DescriptionLevel = DescriptionLevel.ITEM,
  currentCount?: number,
): FilingHoldingSchemeNode {
  return {
    id: currentId,
    title: currentId,
    unitType: UnitType.INGEST,
    descriptionLevel: currentDescriptionLevel,
    checked: false,
    children: currentChildren,
    vitamId: 'whatever',
    count: currentCount,
  };
}

export function newTreeNode(currentId: string, count: number, currentChildren: FilingHoldingSchemeNode[] = []): FilingHoldingSchemeNode {
  return newNode(currentId, currentChildren, DescriptionLevel.RECORD_GRP, count);
}

describe('LeavesTreeComponent', () => {
  let component: LeavesTreeComponent;
  let fixture: ComponentFixture<LeavesTreeComponent>;
  let nestedDataSource: MatTreeNestedDataSource<FilingHoldingSchemeNode>;
  let resultFacets: ResultFacet[];

  let archiveServiceStub: Partial<ArchiveCollectService>;
  let archiveFacetsServicStube: Partial<ArchiveFacetsService>;
  const archiveSharedDataServiceStub = jasmine.createSpyObj<ArchiveSharedDataService>('ArchiveSharedDataService', ['getSearchCriterias']);
  const searchCriteria: SearchCriteriaDto = {
    pageNumber: 0,
    size: 1,
    criteriaList: [],
    sortingCriteria: null,
    trackTotalHits: false,
    computeFacets: false,
  };

  beforeEach(async () => {
    archiveServiceStub = {};
    archiveFacetsServicStube = {};

    archiveSharedDataServiceStub.getSearchCriterias.and.returnValue(of(searchCriteria));
    archiveSharedDataServiceStub.selectedUnit$ = of();

    await TestBed.configureTestingModule({
      imports: [BrowserAnimationsModule, TranslateModule.forRoot()],
      declarations: [LeavesTreeComponent],
      providers: [
        { provide: ArchiveCollectService, useValue: archiveServiceStub },
        { provide: ArchiveSharedDataService, useValue: archiveSharedDataServiceStub },
        { provide: ArchiveFacetsService, useValue: archiveFacetsServicStube },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LeavesTreeComponent);
    component = fixture.componentInstance;
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

  it('LeavesTreeComponent should be stable after creation', () => {
    expect(component).toBeTruthy();
    // expect(component.projectId).toBeDefined();
    expect(component.nestedTreeControlLeaves).toBeDefined();
  });

  it('should return vitamui-icon-folder as response ', () => {
    const filingHoldingSchemaNode: FilingHoldingSchemeNode = {
      id: 'filingHoldingSchemaNodeId',
      title: 'string',
      unitType: UnitType.INGEST,
      descriptionLevel: DescriptionLevel.ITEM,
      label: 'string',
      children: [],
      count: 55,
      vitamId: 'vitamId',
      checked: true,
      hidden: false,
      isLoadingChildren: true,
      paginatedChildrenLoaded: 5,
    };

    const response = component.getNodeUnitIcon(filingHoldingSchemaNode);

    expect(response).toEqual('vitamui-icon-folder');
  });
});
