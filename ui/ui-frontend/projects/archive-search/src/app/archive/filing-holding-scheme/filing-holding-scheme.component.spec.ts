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
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatLegacyMenuModule as MatMenuModule } from '@angular/material/legacy-menu';
import { MatLegacyProgressSpinnerModule as MatProgressSpinnerModule } from '@angular/material/legacy-progress-spinner';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatTreeModule } from '@angular/material/tree';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';
import { FilingHoldingSchemeNode, InjectorModule, LoggerModule } from 'vitamui-library';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from '../../../environments/environment.prod';
import { ArchiveApiService } from '../../core/api/archive-api.service';
import { ArchiveService } from '../archive.service';
import { FilingHoldingSchemeComponent } from './filing-holding-scheme.component';

describe('FilingHoldingSchemeComponent', () => {
  let component: FilingHoldingSchemeComponent;
  let fixture: ComponentFixture<FilingHoldingSchemeComponent>;

  const archiveServiceStub = {
    loadFilingHoldingSchemeTree: () => of([]),
  };

  const archiveServiceMock = {
    archive: () => of('test archive'),
    search: () => of([]),
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
        TranslateModule.forRoot(),
        RouterTestingModule,
      ],
      declarations: [FilingHoldingSchemeComponent],
      providers: [
        { provide: ArchiveService, useValue: archiveServiceStub },
        { provide: ArchiveApiService, useValue: archiveServiceMock },
        {
          provide: ActivatedRoute,
          useValue: { params: of({ tenantIdentifier: 1 }), data: of({ appId: 'ARCHIVE_SEARCH_MANAGEMENT_APP' }) },
        },
        { provide: environment, useValue: environment },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FilingHoldingSchemeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  describe('Checkbox-Clic', () => {
    let nodes: FilingHoldingSchemeNode[] = [];

    beforeEach(() => {
      // Given
      const rootNode: FilingHoldingSchemeNode = {
        id: 'rootId',
        title: 'RootTitle',
        type: 'RecordGrp',
        children: [],
        vitamId: 'rootId',
        checked: false,
        hidden: false,
      };

      const rootChildren: FilingHoldingSchemeNode[] = [
        {
          id: 'rootChild-1',
          title: 'RootChild 1',
          type: 'RecordGrp',
          children: [],
          vitamId: 'rootChild-1',
          checked: false,
          hidden: false,
        },
        {
          id: 'rootChild-2',
          title: 'RootChild 2',
          type: 'RecordGrp',
          children: [],
          vitamId: 'rootChild-2',
          checked: false,
          hidden: false,
        },
      ];

      rootNode.children = rootChildren;
      rootChildren[0].children = [
        {
          id: 'leaf-1',
          title: 'Leaf 1',
          type: 'RecordGrp',
          children: [],
          vitamId: 'leaf-1',
          checked: false,
          hidden: false,
        },
        {
          id: 'leaf-2',
          title: 'Leaf 2',
          type: 'RecordGrp',
          children: [],
          vitamId: 'leaf-2',
          checked: false,
          hidden: false,
        },
      ];

      nodes = [rootNode];
      component.nestedDataSourceFull.data = nodes;
      component.nestedTreeControlFull.dataNodes = nodes;
    });

    describe('emitNode', () => {
      it('should check all nodes when select root node', () => {
        // When: emit the node to archive-search component
        nodes[0].checked = true;
        component.addToSearchCriteria(nodes[0]);

        // Then: Check the nodeData object recieved
        expect(component.nodeData.title).toEqual('RootTitle');
        expect(component.nodeData.id).toEqual('rootId');
        expect(component.nodeData.checked).toBeTruthy('Root node should be checked');
      });
    });
    describe('showAllTreeNodes', () => {
      it('should check all nodes when select root node', () => {
        component.switchViewAllNodes();
        component.nestedDataSourceFull.data.forEach((node) => {
          expect(node.hidden).toBeFalsy();
        });
      });
    });
  });
});
