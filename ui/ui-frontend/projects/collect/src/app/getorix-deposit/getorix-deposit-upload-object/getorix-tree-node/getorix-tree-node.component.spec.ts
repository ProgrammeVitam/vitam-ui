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

import { Pipe, PipeTransform } from '@angular/core';
import { DescriptionLevel, FilingHoldingSchemeNode, UnitType } from 'ui-frontend-common';
import { GetorixTreeNodeComponent } from './getorix-tree-node.component';

describe('GetorixTreeNodeComponent', () => {
  let component: GetorixTreeNodeComponent;
  let fixture: ComponentFixture<GetorixTreeNodeComponent>;

  const node: FilingHoldingSchemeNode = {
    id: 'id',
    title: 'label',
    type: 'RecordGroup',
    children: [],
    vitamId: 'vitamId',
    checked: true,
    hidden: true,
    hasObject: false,
    unitType: UnitType.INGEST,
  };

  @Pipe({ name: 'truncate' })
  class MockTruncatePipe implements PipeTransform {
    transform(value: number): number {
      return value;
    }
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [GetorixTreeNodeComponent, MockTruncatePipe],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GetorixTreeNodeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    component.node = node;
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('nodeSelected should be checked and search is starting when ORPHANS_NODE', () => {
    let node: FilingHoldingSchemeNode = {
      id: 'ORPHANS_NODE',
      title: 'label',
      type: 'RecordGroup',
      children: [],
      vitamId: 'vitamId',
      checked: false,
      hidden: true,
      hasObject: false,
      unitType: UnitType.INGEST,
      descriptionLevel: DescriptionLevel.COLLECTION,
    };

    component.onLabelClick(node);
    expect(component).toBeTruthy();
    expect(node.checked).toBeTruthy();
  });

  it('component should work correctly when the descriptionLevel is ITEM', () => {
    let node: FilingHoldingSchemeNode = {
      id: 'id',
      title: 'label',
      type: 'RecordGroup',
      children: [],
      vitamId: 'vitamId',
      checked: true,
      hidden: true,
      hasObject: false,
      unitType: UnitType.INGEST,
      descriptionLevel: DescriptionLevel.ITEM,
    };

    component.onLabelClick(node);
    expect(component).toBeTruthy();
  });

  it('nodeSelected should be checked and search is starting', () => {
    let node: FilingHoldingSchemeNode = {
      id: 'id',
      title: 'label',
      type: 'RecordGroup',
      children: [],
      vitamId: 'vitamId',
      checked: false,
      hidden: true,
      hasObject: false,
      unitType: UnitType.INGEST,
      descriptionLevel: DescriptionLevel.COLLECTION,
    };

    component.onLabelClick(node);
    expect(component).toBeTruthy();
    expect(node.checked).toBeTruthy();
  });
});
