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
import { OverlayContainer } from '@angular/cdk/overlay';
import { Component, ViewChild } from '@angular/core';
import { ComponentFixture, inject, TestBed, waitForAsync } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { VitamUICommonTestModule } from 'vitamui-library/testing';

import { PatternComponent } from './pattern.component';
import { TranslateModule } from '@ngx-translate/core';
import { SelectComponent } from '../select/select.component';
const objectContaining = expect.objectContaining;
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { MatSelectHarness } from '@angular/material/select/testing';

@Component({
  template: `<app-pattern [(ngModel)]="patterns" [options]="options"></app-pattern>`,
  standalone: false,
})
class TestHostComponent {
  patterns: string[];
  options = [
    { value: 'option1.com', disabled: false },
    { value: 'option2.com', disabled: false },
    { value: 'option3.com', disabled: false },
    { value: 'option4.com', disabled: true },
  ];

  @ViewChild(PatternComponent, { static: false })
  component: PatternComponent;
}

describe('PatternComponent', () => {
  let testhost: TestHostComponent;
  let fixture: ComponentFixture<TestHostComponent>;
  let selectHarness: MatSelectHarness;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        FormsModule,
        ReactiveFormsModule,
        MatSelectModule,
        NoopAnimationsModule,
        VitamUICommonTestModule,
        TranslateModule.forRoot(),
        PatternComponent,
        SelectComponent,
      ],
      declarations: [TestHostComponent],
    }).compileComponents();
  });

  beforeEach(async () => {
    fixture = TestBed.createComponent(TestHostComponent);
    testhost = fixture.componentInstance;
    fixture.detectChanges();

    selectHarness = await TestbedHarnessEnvironment.loader(fixture).getHarness(MatSelectHarness);
  });

  it('should create', () => {
    expect(testhost).toBeTruthy();
  });

  it('should set the patterns', waitForAsync(() => {
    testhost.patterns = ['option1.com', 'option2.com'];
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      fixture.detectChanges();
      expect(testhost.component.patterns).toEqual(['option1.com', 'option2.com']);
    });
  }));

  function add(value: string) {
    testhost.component.control.setValue(value);
    testhost.component.add();
  }

  it('should add the pattern', () => {
    add('option2.com');
    expect(testhost.component.patterns).toEqual(['option2.com']);
    expect(testhost.patterns).toEqual(['option2.com']);
  });

  it('should not add a pattern already selected', () => {
    add('option2.com');
    expect(testhost.component.patterns).toEqual(['option2.com']);
    add('option2.com');
    expect(testhost.component.patterns).toEqual(['option2.com']);
  });

  it('should not add an empty pattern', () => {
    add('');
    expect(testhost.component.patterns).toEqual([]);
  });

  it('should remove the pattern', () => {
    add('option2.com');
    expect(testhost.component.patterns).toEqual(['option2.com']);
    expect(testhost.patterns).toEqual(['option2.com']);
    testhost.component.remove('option2.com');
    expect(testhost.component.patterns).toEqual([]);
    expect(testhost.patterns).toEqual([]);
  });

  it('should do nothing', () => {
    add('option2.com');
    expect(testhost.component.patterns).toEqual(['option2.com']);
    expect(testhost.patterns).toEqual(['option2.com']);
    testhost.component.remove('option3.com');
    expect(testhost.patterns).toEqual(['option2.com']);
    expect(testhost.patterns).toEqual(['option2.com']);
  });

  it('should disable then enable the select list', () => {
    expect(testhost.component.control.disabled).toBe(false);
    add('option1.com');
    expect(testhost.component.control.disabled).toBe(false);
    add('option2.com');
    expect(testhost.component.control.disabled).toBe(false);
    add('option3.com');
    expect(testhost.component.control.disabled).toBe(true);
    testhost.component.remove('option3.com');
    expect(testhost.component.control.disabled).toBe(false);
  });

  it('should return the available options', () => {
    expect(testhost.component.availableOptions).toEqual([
      objectContaining({ key: 'option1.com', disabled: false }),
      objectContaining({ key: 'option2.com', disabled: false }),
      objectContaining({ key: 'option3.com', disabled: false }),
      objectContaining({ key: 'option4.com', disabled: true }),
    ]);
    add('option2.com');
    expect(testhost.component.availableOptions).toEqual([
      objectContaining({ key: 'option1.com', disabled: false }),
      objectContaining({ key: 'option2.com', disabled: true }),
      objectContaining({ key: 'option3.com', disabled: false }),
      objectContaining({ key: 'option4.com', disabled: true }),
    ]);
    add('option1.com');
    expect(testhost.component.availableOptions).toEqual([
      objectContaining({ key: 'option1.com', disabled: true }),
      objectContaining({ key: 'option2.com', disabled: true }),
      objectContaining({ key: 'option3.com', disabled: false }),
      objectContaining({ key: 'option4.com', disabled: true }),
    ]);
    add('option3.com');
    expect(testhost.component.availableOptions).toEqual([
      objectContaining({ key: 'option1.com', disabled: true }),
      objectContaining({ key: 'option2.com', disabled: true }),
      objectContaining({ key: 'option3.com', disabled: true }),
      objectContaining({ key: 'option4.com', disabled: true }),
    ]);
  });

  it('should return true when the pattern can be added, false otherwise', () => {
    add('option2.com');
    testhost.component.control.setValue('option1.com');
    expect(testhost.component.controlValueValid()).toBe(true);
    testhost.component.control.setValue('option2.com');
    expect(testhost.component.controlValueValid()).toBe(false);
    testhost.component.control.setValue('');
    expect(testhost.component.controlValueValid()).toBe(false);
  });

  it('should return true if an option can be selected', () => {
    add('option2.com');
    expect(testhost.component.isAvailable('option1.com')).toBe(true);
    expect(testhost.component.isAvailable('option2.com')).toBe(false);
  });

  it('should return the enabled options', () => {
    expect(testhost.component.enabledOptions()).toEqual([
      objectContaining({ key: 'option1.com', disabled: false }),
      objectContaining({ key: 'option2.com', disabled: false }),
      objectContaining({ key: 'option3.com', disabled: false }),
    ]);
  });

  describe('DOM', () => {
    let overlayContainerElement: HTMLElement;

    beforeEach(() => {
      inject([OverlayContainer], (oc: OverlayContainer) => {
        overlayContainerElement = oc.getContainerElement();
      })();
    });

    it('should have a select with the patterns', async () => {
      await selectHarness.open();

      const elOptions = overlayContainerElement.querySelectorAll('mat-option');
      expect(elOptions.length).toBe(4);
      expect(elOptions[0].textContent).toContain('option1.com');
      expect(elOptions[1].textContent).toContain('option2.com');
      expect(elOptions[2].textContent).toContain('option3.com');
      expect(elOptions[3].textContent).toContain('option4.com');
      expect(elOptions[3].textContent).toContain('SHARED.PATTERN_ALREADY_USED');
      expect(elOptions[3].className).toContain('mdc-list-item--disabled');
    });

    it('should hide the already selected options', async () => {
      add('option3.com');
      await selectHarness.open();

      const selectOptions = await selectHarness.getOptions();

      expect(await selectOptions[0].getText()).toContain('option1.com');
      expect(await selectOptions[0].isDisabled()).toBe(false);

      expect(await selectOptions[1].getText()).toContain('option2.com');
      expect(await selectOptions[1].isDisabled()).toBe(false);

      expect(await selectOptions[2].getText()).toContain('option3.com');
      expect(await selectOptions[2].isDisabled()).toBe(true);

      expect(await selectOptions[3].getText()).toContain('option4.com');
      expect(await selectOptions[3].isDisabled()).toBe(true);
    });

    it('should have a list of selected patterns', () => {
      add('option1.com');
      add('option2.com');
      fixture.detectChanges();
      const elPatterns = fixture.nativeElement.querySelectorAll('.vitamui-chip-list .vitamui-chip');
      expect(elPatterns.length).toBe(2);
      expect(elPatterns[0].textContent).toContain('option1.com');
      expect(elPatterns[1].textContent).toContain('option2.com');
    });

    it('should remove the pattern', () => {
      vi.spyOn(testhost.component, 'remove');
      add('option1.com');
      add('option2.com');
      fixture.detectChanges();
      const elPatterns = fixture.nativeElement.querySelectorAll('.vitamui-chip-list .vitamui-chip');
      const elRemoveButton = elPatterns[0].querySelector('.vitamui-remove-chip');
      elRemoveButton.click();
      expect(testhost.component.remove).toHaveBeenCalledWith('option1.com');
    });

    it('should call add() on click', () => {
      vi.spyOn(testhost.component, 'add');
      testhost.component.control.setValue('option2.com');
      fixture.detectChanges();
      const elAddButton = fixture.nativeElement.querySelector('button');
      elAddButton.click();
      expect(testhost.component.add).toHaveBeenCalled();
    });
  });
});
