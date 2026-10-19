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
import { Component, signal, viewChild } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { form, FormField } from '@angular/forms/signals';
import { provideTranslateService } from '@ngx-translate/core';

import { PatternComponent } from './pattern.component';

const options = [{ value: 'a.com' }, { value: 'b.com' }];

@Component({
  template: `<app-pattern [formField]="testForm.patterns" [options]="options"></app-pattern>`,
  imports: [PatternComponent, FormField],
})
class SignalFormHostComponent {
  readonly options = options;
  readonly testForm = form(signal<{ patterns: string[] }>({ patterns: [] }));
  readonly pattern = viewChild(PatternComponent);
}

@Component({
  template: `<app-pattern [formControl]="control" [options]="options"></app-pattern>`,
  imports: [PatternComponent, ReactiveFormsModule],
})
class ReactiveFormHostComponent {
  readonly options = options;
  readonly control = new FormControl<string[]>([]);
  readonly pattern = viewChild(PatternComponent);
}

describe('PatternComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SignalFormHostComponent, ReactiveFormHostComponent],
      providers: [provideTranslateService()],
    }).compileComponents();
  });

  it('should add the selected patterns to the signal forms field and disable the select when all options are added', async () => {
    const fixture: ComponentFixture<SignalFormHostComponent> = TestBed.createComponent(SignalFormHostComponent);
    await fixture.whenStable();
    const pattern = fixture.componentInstance.pattern();

    pattern.selectedPatternField().value.set('a.com');
    pattern.add();
    pattern.selectedPatternField().value.set('b.com');
    pattern.add();

    expect(fixture.componentInstance.testForm.patterns().value()).toEqual(['a.com', 'b.com']);
    expect(pattern.selectedPatternField().disabled()).toBe(true);

    pattern.remove('a.com');
    expect(fixture.componentInstance.testForm.patterns().value()).toEqual(['b.com']);
    expect(pattern.selectedPatternField().disabled()).toBe(false);
  });

  it('should add the selected patterns to the reactive form control', async () => {
    const fixture: ComponentFixture<ReactiveFormHostComponent> = TestBed.createComponent(ReactiveFormHostComponent);
    await fixture.whenStable();
    const pattern = fixture.componentInstance.pattern();

    pattern.selectedPatternField().value.set('a.com');
    pattern.add();

    expect(fixture.componentInstance.control.value).toEqual(['a.com']);
    expect(pattern.controlValueValid()).toBe(false);
  });
});
