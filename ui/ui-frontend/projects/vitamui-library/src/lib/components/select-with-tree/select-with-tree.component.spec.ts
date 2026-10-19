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
import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { form, FormField } from '@angular/forms/signals';
import { provideTranslateService } from '@ngx-translate/core';
import { SelectWithTreeComponent } from './select-with-tree.component';
import { MatSelectModule } from '@angular/material/select';
import { ItemNode } from '../../../app/modules/components/autocomplete/utils/item-node.interface';

class Item {
  value: string;
}

describe('SelectWithTreeComponent', () => {
  let component: SelectWithTreeComponent<Item>;
  let fixture: ComponentFixture<SelectWithTreeComponent<Item>>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MatSelectModule],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectWithTreeComponent<Item>);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('getDisplayValue', (item: Item) => item.value);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('signal forms', () => {
    const itemA: Item = { value: 'A' };
    const itemB: Item = { value: 'B' };
    const options: ItemNode<Item>[] = [
      { item: itemA, children: [] },
      { item: itemB, children: [] },
    ];

    @Component({
      template: `<vitamui-select-with-tree
        [formField]="testForm.items"
        [options]="options"
        [multiple]="true"
        [getDisplayValue]="getDisplayValue"
      ></vitamui-select-with-tree>`,
      imports: [SelectWithTreeComponent, FormField],
    })
    class SignalFormHostComponent {
      readonly options = options;
      readonly getDisplayValue = (item: Item) => item.value;
      readonly testForm = form(signal<{ items: Item[] }>({ items: [itemB] }));
    }

    it('should apply the initial value of the field once the tree is built', async () => {
      TestBed.resetTestingModule();
      await TestBed.configureTestingModule({
        imports: [SignalFormHostComponent],
        providers: [provideTranslateService()],
      }).compileComponents();
      const hostFixture = TestBed.createComponent(SignalFormHostComponent);
      await hostFixture.whenStable();

      const select: SelectWithTreeComponent<Item> = hostFixture.debugElement.children[0].componentInstance;
      expect(select.checklistSelection.selected.map((node) => node.item)).toEqual([itemB]);
    });
  });
});
