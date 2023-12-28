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

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from 'projects/collect/src/environments/environment';
import { of } from 'rxjs';
import { FilingHoldingSchemeNode, InjectorModule, LoggerModule } from 'ui-frontend-common';
import { ArchiveCollectService } from '../../../collect/archive-search-collect/archive-collect.service';
import { GetorixTreePlanSchemaComponent } from './getorix-tree-plan-schema.component';

describe('GetorixTreePlanSchemaComponent', () => {
  let component: GetorixTreePlanSchemaComponent;
  let fixture: ComponentFixture<GetorixTreePlanSchemaComponent>;

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
  const nodes = [rootNode];

  const archiveCollectServiceMock = {
    loadFilingHoldingSchemeTree: () => of(nodes),
    searchArchiveUnitsByCriteria: () => of(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [GetorixTreePlanSchemaComponent],
      imports: [HttpClientTestingModule, TranslateModule.forRoot(), InjectorModule, LoggerModule.forRoot(), RouterTestingModule],
      providers: [
        { provide: ArchiveCollectService, useValue: archiveCollectServiceMock },
        { provide: environment, useValue: environment },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GetorixTreePlanSchemaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });
});
