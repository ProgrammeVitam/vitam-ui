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
import { SelectComponent } from './select.component';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Component, ViewChild } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { MatSelectHarness } from '@angular/material/select/testing';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { TranslateModule } from '@ngx-translate/core';
import { input } from '../../../../testing/src';

const placeholder = 'test';
const searchBarPlaceHolder = 'search test';
const options = {
  options: [
    { key: 'option1', label: 'option 1' },
    { key: 'option2', label: 'option 2' },
    { key: 'option3', label: 'option 3' },
    { key: 'something-else', label: 'something else' },
  ],
};

@Component({
  template:
    '<vitamui-select [placeholder]="placeholder" [options]="options" [formControl]="control" [multiple]="multiple"></vitamui-select>',
  imports: [ReactiveFormsModule, SelectComponent],
})
class TestHostComponent {
  @ViewChild(SelectComponent)
  selectComponent: SelectComponent;

  options = options;
  placeholder = placeholder;
  searchBarPlaceHolder = searchBarPlaceHolder;
  multiple?: boolean;
  control: FormControl = new FormControl({ value: null, disabled: false });
}

describe('SelectComponent', () => {
  let hostFixture: ComponentFixture<TestHostComponent>;
  let testHostComponent: TestHostComponent;
  let selectHarness: MatSelectHarness;

  function init(isMultiple?: boolean) {
    return async () => {
      await TestBed.configureTestingModule({
        imports: [NoopAnimationsModule, TranslateModule.forRoot(), TestHostComponent],
      }).compileComponents();

      hostFixture = TestBed.createComponent(TestHostComponent);
      hostFixture.componentInstance.multiple = isMultiple;
      testHostComponent = hostFixture.componentInstance;
      hostFixture.detectChanges();

      selectHarness = await TestbedHarnessEnvironment.loader(hostFixture).getHarness(MatSelectHarness);
    };
  }

  function commonBetweenMultiAndNoMulti() {
    it('should display placeholder', () => {
      const labelElement = hostFixture.debugElement.query(By.css('mat-label')).nativeElement;
      expect(labelElement.textContent).toEqual(placeholder);
    });

    it('should open the select on click', async () => {
      const labelElement = hostFixture.debugElement.query(By.css('mat-label')).nativeElement;

      expect(await selectHarness.isOpen()).toBeFalsy();
      labelElement.click();
      expect(await selectHarness.isOpen()).toBeTruthy();
    });

    it('should filter options when filtering', async () => {
      await selectHarness.open();

      const searchesToTest = ['option', 'option 1', 'something'];
      for (const search of searchesToTest) {
        input(document.querySelector('input'), search);
        hostFixture.detectChanges();

        const expectedOptions = options.options.filter((option) => option.label.includes(search));
        expect(testHostComponent.selectComponent.displayedOptions.length).toEqual(expectedOptions.length);
      }
    });
  }

  describe('in NON multiple mode', () => {
    beforeEach(init(false));

    it('should be non multiple and with search', () => {
      expect(testHostComponent.selectComponent.multiple).toBeFalse();
      expect(testHostComponent.selectComponent.enableSearch).toBeTrue();
    });

    commonBetweenMultiAndNoMulti();

    it('should select value when user selects a value', async () => {
      const labelElement = hostFixture.debugElement.query(By.css('mat-label')).nativeElement;

      expect(testHostComponent.control.value).toBeNull();

      for (const option of options.options) {
        const i = options.options.indexOf(option);
        labelElement.click();
        const selectOptions = await selectHarness.getOptions();
        expect(selectOptions.length).toBe(options.options.length);

        await selectOptions[i].click();
        const valueElement = hostFixture.debugElement.query(By.css('mat-select-trigger')).nativeElement;
        expect(testHostComponent.control.value).toEqual(option.key);
        expect(valueElement.textContent.trim()).toEqual(option.label);
      }
    });

    it('should deselect value when user clicks on selected value', async () => {
      const labelElement = hostFixture.debugElement.query(By.css('mat-label')).nativeElement;

      // Set 1st value
      testHostComponent.control.setValue(options.options[0].key);
      // Check value is set
      expect(testHostComponent.control.value).toEqual(options.options[0].key);

      // Click on 1st value (should deselect it)
      labelElement.click();
      const selectOptions = await selectHarness.getOptions();
      await selectOptions[0].click();

      // Check value is unset
      expect(testHostComponent.control.value).toEqual(undefined);
    });
  });

  describe('in multiple mode', () => {
    beforeEach(init(true));

    it('should be multiple and with search, selectAll and displaySelected', () => {
      expect(testHostComponent.selectComponent.multiple).toBeTrue();
      expect(testHostComponent.selectComponent.enableSearch).toBeTrue();
      expect(testHostComponent.selectComponent.enableSelectAll).toBeTrue();
      expect(testHostComponent.selectComponent.enableDisplaySelected).toBeTrue();
    });

    commonBetweenMultiAndNoMulti();

    it('should select multiple values when user selects multiple values', async () => {
      const labelElement = hostFixture.debugElement.query(By.css('mat-label')).nativeElement;

      expect(testHostComponent.control.value).toBeNull();

      labelElement.click();
      const selectOptions = await selectHarness.getOptions();
      expect(selectOptions.length).toBe(options.options.length + 1); // +1 for select all

      const expectedValues = [];

      for (const option of options.options) {
        const i = options.options.indexOf(option);

        await selectOptions[i + 1].click();
        expectedValues.push(option.key);
        const valueElement = hostFixture.debugElement.query(By.css('mat-select-trigger')).nativeElement;
        expect(testHostComponent.control.value).toEqual(expectedValues);
        expect(valueElement.textContent.trim().startsWith(expectedValues.length)).toBeTrue();
      }
    });

    it('should toggle selecting all values when user clicks on select all', async () => {
      const allValues = options.options.map((option) => option.key);

      const labelElement = hostFixture.debugElement.query(By.css('mat-label')).nativeElement;
      await labelElement.click();

      expect(testHostComponent.control.value).toBeNull();

      const selectAllElement = (await selectHarness.getOptions())[0];

      await selectAllElement.click();
      expect(testHostComponent.control.value).toEqual(allValues);

      await selectAllElement.click();
      expect(testHostComponent.control.value).toEqual([]);
    });

    it('should have right selection after click on selectAll, then click on an option and click again on the same option', async () => {
      const labelElement = hostFixture.debugElement.query(By.css('mat-label')).nativeElement;
      await labelElement.click();

      const selectAllElement = (await selectHarness.getOptions())[0];
      await selectAllElement.click();

      const selectOptions = await selectHarness.getOptions();
      await selectOptions[0].click();
      await selectOptions[0].click();

      const allValues = options.options.map((option) => option.key);
      expect(testHostComponent.control.value).toEqual(allValues);
    });
  });
});
