/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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

import { PatternComponent } from './pattern.component';

@Component({
  template: `<app-pattern [(ngModel)]="patterns" [options]="options"></app-pattern>`
})
class TestHostComponent {
  patterns: string[];
  options = [
    { value: 'option1.com', disabled: false },
    { value: 'option2.com', disabled: false },
    { value: 'option3.com', disabled: false },
    { value: 'option4.com', disabled: true },
  ];

  @ViewChild(PatternComponent, { static: false }) component: PatternComponent;
}

describe('PatternComponent', () => {
  let testhost: TestHostComponent;
  let fixture: ComponentFixture<TestHostComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        ReactiveFormsModule,
        MatSelectModule,
        NoopAnimationsModule,
      ],
      declarations: [ PatternComponent, TestHostComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestHostComponent);
    testhost = fixture.componentInstance;
    fixture.detectChanges();
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

  it('should set the options', () => {
    expect(testhost.component.options).toEqual(testhost.options);
  });

  it('should add the pattern', () => {
    testhost.component.control.setValue('option2.com');
    testhost.component.add();
    expect(testhost.component.patterns).toEqual(['option2.com']);
    expect(testhost.patterns).toEqual(['option2.com']);
  });

  it('should not add a pattern already selected', () => {
    testhost.component.control.setValue('option2.com');
    testhost.component.add();
    expect(testhost.component.patterns).toEqual(['option2.com']);
    testhost.component.add();
    expect(testhost.component.patterns).toEqual(['option2.com']);
  });

  it('should not add an empty pattern', () => {
    testhost.component.control.setValue('');
    testhost.component.add();
    expect(testhost.component.patterns).toEqual([]);
  });

  it('should remove the pattern', () => {
    testhost.component.control.setValue('option2.com');
    testhost.component.add();
    expect(testhost.component.patterns).toEqual(['option2.com']);
    expect(testhost.patterns).toEqual(['option2.com']);
    testhost.component.remove('option2.com');
    expect(testhost.component.patterns).toEqual([]);
    expect(testhost.patterns).toEqual([]);
  });

  it('should do nothing', () => {
    testhost.component.control.setValue('option2.com');
    testhost.component.add();
    expect(testhost.component.patterns).toEqual(['option2.com']);
    expect(testhost.patterns).toEqual(['option2.com']);
    testhost.component.remove('option3.com');
    expect(testhost.patterns).toEqual(['option2.com']);
    expect(testhost.patterns).toEqual(['option2.com']);
  });

  it('should disable then enable the select list', () => {
    expect(testhost.component.control.disabled).toBe(false);
    testhost.component.control.setValue('option1.com');
    testhost.component.add();
    expect(testhost.component.control.disabled).toBe(false);
    testhost.component.control.setValue('option2.com');
    testhost.component.add();
    expect(testhost.component.control.disabled).toBe(false);
    testhost.component.control.setValue('option3.com');
    testhost.component.add();
    expect(testhost.component.control.disabled).toBe(true);
    testhost.component.remove('option3.com');
    expect(testhost.component.control.disabled).toBe(false);
  });

  it('should return the available options', () => {
    testhost.component.patterns = [];
    expect(testhost.component.availableOptions).toEqual([
      { value: 'option1.com', disabled: false },
      { value: 'option2.com', disabled: false },
      { value: 'option3.com', disabled: false },
      { value: 'option4.com', disabled: true },
    ]);
    testhost.component.patterns = ['option2.com'];
    expect(testhost.component.availableOptions).toEqual([
      { value: 'option1.com', disabled: false },
      { value: 'option3.com', disabled: false },
      { value: 'option4.com', disabled: true },
    ]);
    testhost.component.patterns = ['option1.com', 'option2.com'];
    expect(testhost.component.availableOptions).toEqual([
      { value: 'option3.com', disabled: false },
      { value: 'option4.com', disabled: true },
    ]);
    testhost.component.patterns = ['option1.com', 'option2.com', 'option3.com'];
    expect(testhost.component.availableOptions).toEqual([{ value: 'option4.com', disabled: true }]);
  });

  it('should return true when the pattern can be added, false otherwise', () => {
    testhost.component.patterns = ['option2.com'];
    testhost.component.control.setValue('option1.com');
    expect(testhost.component.controlValueValid()).toBe(true);
    testhost.component.control.setValue('option2.com');
    expect(testhost.component.controlValueValid()).toBe(false);
    testhost.component.control.setValue('');
    expect(testhost.component.controlValueValid()).toBe(false);
  });

  it('should return true if an option can be selected', () => {
    testhost.component.patterns = ['option2.com'];
    expect(testhost.component.isAvailable('option1.com')).toBe(true);
    expect(testhost.component.isAvailable('option2.com')).toBe(false);
  });

  it('should return the enabled options', () => {
    expect(testhost.component.enabledOptions).toEqual([
      { value: 'option1.com', disabled: false },
      { value: 'option2.com', disabled: false },
      { value: 'option3.com', disabled: false },
    ]);
  });

  describe('DOM', () => {
    let overlayContainerElement: HTMLElement;

    beforeEach(() => {
      inject([OverlayContainer], (oc: OverlayContainer) => {
        overlayContainerElement = oc.getContainerElement();
      })();
    });

    it('should have a select with the patterns', () => {
      const elSelect = fixture.nativeElement.querySelector('mat-select');
      expect(elSelect).toBeTruthy();
      elSelect.click();
      fixture.detectChanges();
      const elOptions = overlayContainerElement.querySelectorAll('mat-option');
      expect(elOptions.length).toBe(4);
      expect(elOptions[0].textContent).toContain('option1.com');
      expect(elOptions[1].textContent).toContain('option2.com');
      expect(elOptions[2].textContent).toContain('option3.com');
      expect(elOptions[3].textContent).toContain('option4.com');
      expect(elOptions[3].textContent).toContain('(déjà utilisé)');
      expect(elOptions[3].className).toContain('mat-option-disabled');
    });

    it('should hide the already selected options', () => {
      testhost.component.patterns = ['option3.com'];
      const elSelect = fixture.nativeElement.querySelector('mat-select');
      expect(elSelect).toBeTruthy();
      elSelect.click();
      fixture.detectChanges();
      const elOptions = overlayContainerElement.querySelectorAll('mat-option');
      expect(elOptions.length).toBe(3);
      expect(elOptions[0].textContent).toContain('option1.com');
      expect(elOptions[1].textContent).toContain('option2.com');
      expect(elOptions[2].textContent).toContain('option4.com');
    });

    it('should have a list of selected patterns', () => {
      testhost.component.patterns = ['option1.com', 'option2.com'];
      fixture.detectChanges();
      const elPatterns = fixture.nativeElement.querySelectorAll('.vitamui-chip-list .vitamui-chip');
      expect(elPatterns.length).toBe(2);
      expect(elPatterns[0].textContent).toContain('option1.com');
      expect(elPatterns[1].textContent).toContain('option2.com');
    });

    it('should remove the pattern', () => {
      spyOn(testhost.component, 'remove').and.callThrough();
      testhost.component.patterns = ['option1.com', 'option2.com'];
      fixture.detectChanges();
      const elPatterns = fixture.nativeElement.querySelectorAll('.vitamui-chip-list .vitamui-chip');
      const elRemoveButton = elPatterns[0].querySelector('.vitamui-remove-chip');
      elRemoveButton.click();
      expect(testhost.component.remove).toHaveBeenCalledWith('option1.com');
    });

    it('should call add() on click', () => {
      spyOn(testhost.component, 'add').and.callThrough();
      testhost.component.control.setValue('option2.com');
      fixture.detectChanges();
      const elAddButton = fixture.nativeElement.querySelector('button');
      elAddButton.click();
      expect(testhost.component.add).toHaveBeenCalled();
    });

  });

});
