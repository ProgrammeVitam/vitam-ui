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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SearchType, SearchWithTypeSelectorComponent } from './search-with-type-selector.component';
import { By } from '@angular/platform-browser';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { MatMenuHarness } from '@angular/material/menu/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Component, ViewChild } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { input } from '../../../../testing/src';

const types: SearchType[] = [
  { label: 'Recherche approchante', value: 'approx' },
  { label: 'Recherche exacte', value: 'strict' },
];

const placeholder = 'test';

@Component({
  template:
    '<vitamui-search-with-type-selector [placeholder]="placeholder" [types]="types" [formControl]="control"></vitamui-search-with-type-selector>',
  imports: [SearchWithTypeSelectorComponent, ReactiveFormsModule],
})
class TestHostComponent {
  @ViewChild(SearchWithTypeSelectorComponent)
  searchWithTypeSelectorComponent: SearchWithTypeSelectorComponent;

  types = types;
  placeholder = placeholder;
  control: FormControl = new FormControl({ value: null, disabled: false });
}

describe('SearchWithTypeSelectorComponent', () => {
  let hostFixture: ComponentFixture<TestHostComponent>;
  let testHostComponent: TestHostComponent;
  let typeSelectorMenu: MatMenuHarness;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NoopAnimationsModule, TestHostComponent],
    }).compileComponents();

    hostFixture = TestBed.createComponent(TestHostComponent);
    testHostComponent = hostFixture.componentInstance;
    hostFixture.detectChanges();

    typeSelectorMenu = await TestbedHarnessEnvironment.loader(hostFixture).getHarness(MatMenuHarness);
  });

  it('should create', () => {
    expect(testHostComponent).toBeTruthy();
  });

  it('click on type selector button should open menu with available types', async () => {
    const typeButton = hostFixture.debugElement.query(By.css('button')).nativeElement;

    expect(await typeSelectorMenu.isOpen()).toBeFalsy();
    typeButton.click();
    expect(await typeSelectorMenu.isOpen()).toBeTruthy();

    const menuItems = await typeSelectorMenu.getItems();
    const menuItemsTexts = await Promise.all(menuItems.map(async (item) => await item.getText()));
    expect(menuItemsTexts).toEqual(types.map((type) => type.label));
  });

  it('choice of a type should reflect in control value and input label', async () => {
    const inputLabel = hostFixture.debugElement.query(By.css('label')).nativeElement;
    await typeSelectorMenu.open();
    const menuItems = await typeSelectorMenu.getItems();

    // By default, control value is null and label displays first type
    expect(testHostComponent.control.value).toBeNull();
    expect(inputLabel.textContent).toEqual(`${placeholder} (${types[0].label.toLowerCase()})`);

    // When second type is selected, control value and label are updated
    await menuItems[1].click();
    expect(testHostComponent.control.value).toEqual({ type: types[1] });
    expect(inputLabel.textContent).toEqual(`${placeholder} (${types[1].label.toLowerCase()})`);

    // When first type is selected, control value and label are updated
    await menuItems[0].click();
    expect(testHostComponent.control.value).toEqual({ type: types[0] });
    expect(inputLabel.textContent).toEqual(`${placeholder} (${types[0].label.toLowerCase()})`);
  });

  it('writing in input should update control value', async () => {
    const inputEl = hostFixture.debugElement.query(By.css('input')).nativeElement;

    expect(testHostComponent.control.value).toBeNull();
    input(inputEl, 'My test');
    expect(testHostComponent.control.value).toEqual({ type: types[0], value: 'My test' });
  });

  it('disabling the control should mark the component as disabled', async () => {
    const element: HTMLElement = hostFixture.debugElement.query(By.css('.vitamui-input')).nativeElement;

    expect(element.classList.contains('disabled')).toBe(false);
    testHostComponent.control.disable();
    hostFixture.detectChanges();
    expect(element.classList.contains('disabled')).toBe(true);
  });

  it('a disabled type should make it disabled in the menu', async () => {
    await typeSelectorMenu.open();
    const menuItems = await typeSelectorMenu.getItems();
    const firstMenuElement = await menuItems[0].host();

    expect(await firstMenuElement.getAttribute('disabled')).toBeNull();
    testHostComponent.types[0].disabled = true;
    expect(await firstMenuElement.getAttribute('disabled')).toEqual('true');
    testHostComponent.types[0].disabled = false;
    expect(await firstMenuElement.getAttribute('disabled')).toBeNull();
  });
});
