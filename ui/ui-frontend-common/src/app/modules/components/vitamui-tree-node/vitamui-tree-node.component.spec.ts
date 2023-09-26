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

import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { FilingHoldingSchemeNode, UnitType } from '../../models';
import { VitamuiTreeNodeComponent } from './vitamui-tree-node.component';

@Pipe({ name: 'truncate' })
class MockTruncatePipe implements PipeTransform {
  transform(value: number): number {
    return value;
  }
}

describe('VitamuiTreeNodeComponent', () => {
  let component: VitamuiTreeNodeComponent;
  let fixture: ComponentFixture<VitamuiTreeNodeComponent>;

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

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [],
      declarations: [VitamuiTreeNodeComponent, MockTruncatePipe],
      providers: [],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(VitamuiTreeNodeComponent);
    component = fixture.componentInstance;

    spyOn(component.nodeToggle, 'emit');
    spyOn(component.checkboxClick, 'emit');
    spyOn(component.labelClick, 'emit');
    component.node = node;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit checkboxClick event when checkbox is clicked', () => {
    // When click on the checkbox
    const nativeElement = fixture.nativeElement;
    const checkBox = nativeElement.querySelector('mat-checkbox');
    checkBox.dispatchEvent(new Event('click'));
    fixture.detectChanges();

    // checkboxClick event is emit
    expect(component.checkboxClick.emit).toHaveBeenCalled();
    expect(component.labelClick.emit).not.toHaveBeenCalled();
  });

  it('should emit labelClick event when label is clicked', () => {
    // When click on the checkbox
    const nativeElement = fixture.nativeElement;
    const label = nativeElement.querySelector('label');
    label.dispatchEvent(new Event('click'));
    fixture.detectChanges();

    // labelClick event is emit
    expect(component.labelClick.emit).toHaveBeenCalled();
    expect(component.checkboxClick.emit).not.toHaveBeenCalled();
  });

  it('should emit labelClick event and node should be checked when property labelIsLinkedToCheckbox is set to true and label is clicked', () => {
    // Link label and checkbox
    component.labelIsLinkedToCheckbox = true;
    component.node.checked = false;

    // When click on the checkbox
    const nativeElement = fixture.nativeElement;
    const label = nativeElement.querySelector('label');
    label.dispatchEvent(new Event('click'));
    fixture.detectChanges();

    // labelClick event is emit
    expect(component.node.checked).toBeTruthy();
    expect(component.labelClick.emit).toHaveBeenCalled();
  });
});
