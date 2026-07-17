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
import { SelectComponent, VitamuiSelectOptions } from './select.component';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Component, ViewChild } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { MatSelectHarness } from '@angular/material/select/testing';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { input } from '../../../../testing/src';

const placeholder = 'test';
const searchBarPlaceHolder = 'search test';
const defaultOptions = {
  options: [
    { key: 'option1', label: 'option 1' },
    { key: 'option2', label: 'option 2' },
    { key: 'option3', label: 'option 3' },
    { key: 'something-else', label: 'something else' },
  ],
};

@Component({
  template:
    '<vitamui-select [placeholder]="placeholder" [options]="options" [formControl]="control" [multiple]="multiple" [enableSelectAll]="enableSelectAll"></vitamui-select>',
  imports: [ReactiveFormsModule, SelectComponent],
})
class TestHostComponent {
  @ViewChild(SelectComponent)
  selectComponent: SelectComponent;

  options: VitamuiSelectOptions | any[];
  enableSelectAll: boolean;
  placeholder = placeholder;
  searchBarPlaceHolder = searchBarPlaceHolder;
  multiple?: boolean;
  control: FormControl = new FormControl({ value: null, disabled: false });
}

describe('SelectComponent', () => {
  let hostFixture: ComponentFixture<TestHostComponent>;
  let testHostComponent: TestHostComponent;
  let selectHarness: MatSelectHarness;

  function init(
    isMultiple?: boolean,
    {
      enableSelectAll,
      options,
    }: {
      enableSelectAll: boolean;
      options: VitamuiSelectOptions | any[];
    } = {
      enableSelectAll: true,
      options: defaultOptions,
    },
  ) {
    return async () => {
      await TestBed.configureTestingModule({
        imports: [NoopAnimationsModule, TestHostComponent],
      }).compileComponents();

      hostFixture = TestBed.createComponent(TestHostComponent);
      hostFixture.componentInstance.options = options;
      hostFixture.componentInstance.multiple = isMultiple;
      hostFixture.componentInstance.enableSelectAll = enableSelectAll;
      testHostComponent = hostFixture.componentInstance;
      hostFixture.detectChanges();

      selectHarness = await TestbedHarnessEnvironment.loader(hostFixture).getHarness(MatSelectHarness);
    };
  }

  function commonBetweenMultiAndNoMulti() {
    it('should display placeholder', () => {
      const labelElement = hostFixture.debugElement.query(By.css('mat-label')).nativeElement;
      expect(labelElement.textContent.trim()).toEqual(placeholder);
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

        const expectedOptions = defaultOptions.options.filter((option) => option.label.includes(search));
        expect(testHostComponent.selectComponent.displayedOptions.length).toEqual(expectedOptions.length);
      }
    });
  }

  describe('in NON multiple mode', () => {
    beforeEach(init(false));

    it('should be non multiple and with search', () => {
      expect(testHostComponent.selectComponent.multiple).toBe(false);
      expect(testHostComponent.selectComponent.enableSearch).toBe(true);
    });

    commonBetweenMultiAndNoMulti();

    it('should select value when user selects a value', async () => {
      const labelElement = hostFixture.debugElement.query(By.css('mat-label')).nativeElement;

      expect(testHostComponent.control.value).toBeNull();

      for (const option of defaultOptions.options) {
        const i = defaultOptions.options.indexOf(option);
        labelElement.click();
        const selectOptions = await selectHarness.getOptions();
        expect(selectOptions.length).toBe(defaultOptions.options.length);

        await selectOptions[i].click();
        const valueElement = hostFixture.debugElement.query(By.css('mat-select-trigger')).nativeElement;
        expect(testHostComponent.control.value).toEqual(option.key);
        expect(valueElement.textContent.trim()).toEqual(option.label);
      }
    });

    it('should deselect value when user clicks on selected value', async () => {
      const labelElement = hostFixture.debugElement.query(By.css('mat-label')).nativeElement;

      // Set 1st value
      testHostComponent.control.setValue(defaultOptions.options[0].key);
      // Check value is set
      expect(testHostComponent.control.value).toEqual(defaultOptions.options[0].key);

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
      expect(testHostComponent.selectComponent.multiple).toBe(true);
      expect(testHostComponent.selectComponent.enableSearch).toBe(true);
      expect(testHostComponent.selectComponent.enableSelectAll).toBe(true);
      expect(testHostComponent.selectComponent.enableDisplaySelected).toBe(true);
    });

    commonBetweenMultiAndNoMulti();

    it('should select multiple values when user selects multiple values', async () => {
      const labelElement = hostFixture.debugElement.query(By.css('mat-label')).nativeElement;

      expect(testHostComponent.control.value).toBeNull();

      labelElement.click();
      const selectOptions = await selectHarness.getOptions();
      expect(selectOptions.length).toBe(defaultOptions.options.length + 1); // +1 for select all

      const expectedValues = [];

      for (const option of defaultOptions.options) {
        const i = defaultOptions.options.indexOf(option);

        await selectOptions[i + 1].click();
        expectedValues.push(option.key);
        const valueElement = hostFixture.debugElement.query(By.css('mat-select-trigger')).nativeElement;
        expect(testHostComponent.control.value).toEqual(expectedValues);
        expect(valueElement.textContent.trim().startsWith(expectedValues.length)).toBe(true);
      }
    });

    it('should toggle selecting all values when user clicks on select all', async () => {
      const allValues = defaultOptions.options.map((option) => option.key);

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

      const allValues = defaultOptions.options.map((option) => option.key);
      expect(testHostComponent.control.value).toEqual(allValues);
    });

    it('should correctly deselect an option without affecting others', async () => {
      testHostComponent.control.setValue(['option1', 'option2', 'option3']);
      hostFixture.detectChanges();
      await hostFixture.whenStable();

      const labelElement = hostFixture.debugElement.query(By.css('mat-label')).nativeElement;
      labelElement.click();

      const selectOptions = await selectHarness.getOptions();
      await selectOptions[2].click();

      expect(testHostComponent.control.value).toEqual(['option1', 'option3']);
      expect(testHostComponent.control.value).not.toContain('option2');
    });

    it('should display preselected values correctly on load', async () => {
      const preselectedValues = ['option2', 'option3'];
      testHostComponent.control.setValue(preselectedValues);
      hostFixture.detectChanges();
      await hostFixture.whenStable();

      const valueElement = hostFixture.debugElement.query(By.css('mat-select-trigger')).nativeElement;
      expect(valueElement.textContent.trim().startsWith('2')).toBe(true);
    });

    it('should maintain selection integrity when options are reloaded', async () => {
      testHostComponent.control.setValue(['option1', 'option3']);
      hostFixture.detectChanges();
      await hostFixture.whenStable();

      testHostComponent.options = {
        options: [
          { key: 'option1', label: 'Updated Option 1' },
          { key: 'option2', label: 'Updated Option 2' },
          { key: 'option3', label: 'Updated Option 3' },
        ],
      };
      hostFixture.detectChanges();
      await hostFixture.whenStable();

      expect(testHostComponent.control.value).toEqual(['option1', 'option3']);
    });
  });

  describe('in NON multiple mode (selection integrity)', () => {
    beforeEach(init(false));

    it('should only select one option at a time', async () => {
      const labelElement = hostFixture.debugElement.query(By.css('mat-label')).nativeElement;

      labelElement.click();
      const selectOptions = await selectHarness.getOptions();
      await selectOptions[0].click();

      expect(testHostComponent.control.value).toBe('option1');

      labelElement.click();
      const selectOptions2 = await selectHarness.getOptions();
      await selectOptions2[1].click();

      expect(testHostComponent.control.value).toBe('option2');
    });

    it('should only select the chosen value and not others', async () => {
      const labelElement = hostFixture.debugElement.query(By.css('mat-label')).nativeElement;

      labelElement.click();
      const selectOptions = await selectHarness.getOptions();
      await selectOptions[1].click();

      expect(testHostComponent.control.value).toBe('option2');
      expect(testHostComponent.control.value).not.toBe('option1');
      expect(testHostComponent.control.value).not.toBe('option3');
      expect(testHostComponent.control.value).not.toBe('something-else');
    });

    it('should display preselected value correctly on load', async () => {
      testHostComponent.control.setValue('option3');
      hostFixture.detectChanges();
      await hostFixture.whenStable();

      const valueElement = hostFixture.debugElement.query(By.css('mat-select-trigger')).nativeElement;
      expect(valueElement.textContent.trim()).toBe('option 3');
    });
  });

  describe('in multiple mode, without select all and with custom options', () => {
    beforeEach(
      init(true, {
        enableSelectAll: false,
        options: {
          options: [
            { key: 'DE', label: 'Allemagne', disabled: false },
            { key: 'BE', label: 'Belgique', disabled: false },
            { key: 'DK', label: 'Danemark', disabled: true },
            { key: 'ES', label: 'Espagne', disabled: false },
            { key: 'FR', label: 'France', disabled: false },
            { key: 'IT', label: 'Italie', disabled: false },
            { key: 'PT', label: 'Portugal', disabled: false },
            { key: 'GB', label: 'Royaume-Uni', disabled: false },
          ],
        },
      }),
    );

    it('should only select chosen options and not others', async () => {
      const labelElement = hostFixture.debugElement.query(By.css('mat-label')).nativeElement;
      labelElement.click();

      const selectOptions = await selectHarness.getOptions();
      await selectOptions[0].click();
      await selectOptions[3].click();

      expect(testHostComponent.control.value).toEqual(['DE', 'ES']);
      expect(testHostComponent.control.value).not.toContain('BE');
    });
  });

  describe('regression test for bug (Referential fields appear empty in edit mode despite a saved value)', () => {
    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [NoopAnimationsModule, TestHostComponent],
      }).compileComponents();

      hostFixture = TestBed.createComponent(TestHostComponent);
      testHostComponent = hostFixture.componentInstance;
    });

    it('should display preselected value in edit mode even when it is far down in a long list', async () => {
      // Create a large list of options (100 items)
      const largeOptionList = Array.from({ length: 100 }, (_, i) => ({
        key: `option${i}`,
        label: `Option ${i}`,
      }));

      testHostComponent.options = { options: largeOptionList };
      testHostComponent.multiple = false;

      // Preselect an option far down the list (option #95)
      testHostComponent.control.setValue('option95');

      hostFixture.detectChanges();
      await hostFixture.whenStable();

      selectHarness = await TestbedHarnessEnvironment.loader(hostFixture).getHarness(MatSelectHarness);

      // Verify the selected value is displayed in the trigger
      const valueText = await selectHarness.getValueText();
      expect(valueText).toBe('Option 95');

      // Open the select
      await selectHarness.open();
      await hostFixture.whenStable();

      // Verify the value is still displayed after opening
      const valueTextAfterOpen = await selectHarness.getValueText();
      expect(valueTextAfterOpen).toBe('Option 95');
    });
  });
});
