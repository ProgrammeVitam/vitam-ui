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
import { ComponentFixture, fakeAsync, TestBed, waitForAsync } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';

import { input } from '../../../../../testing/src/helpers';
import { CommonTooltipModule } from '../common-tooltip/common-tooltip.module';
import { VitamuiRepeatableInputComponent } from './vitamui-repeatable-input.component';

let component: VitamuiRepeatableInputComponent;
let fixture: ComponentFixture<VitamuiRepeatableInputComponent>;

class TranslateServiceStub {
  onTranslationChange = of({ lang: 'fr', translations: {} });
  onLangChange = of({ translations: {} });
  onDefaultLangChange = of();

  get(_key: string | Array<string>, _interpolateParams?: Object): Observable<string | any> {
    return of('');
  }

  getParsedResult(_translations: any, _key: any, _interpolateParams?: Object): any {
    return of();
  }
}

function getInputs() {
  return fixture.nativeElement.querySelectorAll('input, textarea');
}

describe('VitamuiRepeatableInputComponent', () => {
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [FormsModule, NoopAnimationsModule, MatProgressSpinnerModule, TranslateModule.forRoot(), CommonTooltipModule],
      declarations: [VitamuiRepeatableInputComponent],
      providers: [{ provide: TranslateService, useClass: TranslateServiceStub }],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VitamuiRepeatableInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should update component items and input values on writeValue', () => {
    const values = ['test1', 'test2'];
    component.writeValue(values);
    fixture.detectChanges();

    expect(component.items.length).toEqual(2);
    expect(component.items[0].value).toEqual(values[0]);
    expect(component.items[1].value).toEqual(values[1]);

    fixture.whenStable().then(() => {
      const inputs = getInputs();
      expect(inputs[0].value).toEqual(values[0]);
      expect(inputs[1].value).toEqual(values[1]);
    });
  });

  it('should update component item value and call onChange on input change', fakeAsync(() => {
    const onChangeSpy = spyOn(component, 'onChange');
    const initialValues = ['value1'];
    component.writeValue(initialValues);
    fixture.detectChanges();

    const updatedValue = 'updated value';
    input(getInputs()[0], updatedValue);

    expect(component.items[0].value).toEqual(updatedValue);
    expect(onChangeSpy).toHaveBeenCalledWith([updatedValue]);
  }));

  it('should focus the first input on component click', () => {
    component.writeValue(['value1']);
    fixture.detectChanges();

    fixture.nativeElement.click();
    expect(document.activeElement).toBe(getInputs()[0]);
  });

  it('should set focus to current element and unset on blur', () => {
    component.writeValue(['value1', 'value2']);
    fixture.detectChanges();

    component.onFocus(0);
    expect(component.focused).toBe(0);

    component.onFocus(1);
    expect(component.focused).toBe(1);
    fixture.detectChanges();

    component.onBlur(1);
    expect(component.focused).toBeNull();
  });

  it('should add input', () => {
    const initialValues = ['value1'];
    component.writeValue(initialValues);
    fixture.detectChanges();
    expect(getInputs().length).toBe(1);

    // We add an input
    component.addInput();
    fixture.detectChanges();
    expect(getInputs().length).toBe(2);

    // We set a value to that new input
    const onChangeSpy = spyOn(component, 'onChange');
    const newValue = 'value2';
    input(getInputs()[1], newValue);
    fixture.detectChanges();
    expect(onChangeSpy).toHaveBeenCalledWith([...initialValues, newValue]);
  });

  it('should remove input', () => {
    const onChangeSpy = spyOn(component, 'onChange');
    const initialValues = ['value1', 'value2'];
    component.writeValue(initialValues);
    fixture.detectChanges();
    expect(getInputs().length).toBe(2);

    component.removeInput(0);
    fixture.detectChanges();
    expect(getInputs().length).toBe(1);
    expect(onChangeSpy).toHaveBeenCalledWith([initialValues[1]]);
  });

  it('should remove empty input on blur', () => {
    const initialValues = ['value1', 'value2'];
    component.writeValue(initialValues);
    fixture.detectChanges();

    const onChangeSpy = spyOn(component, 'onChange');
    expect(getInputs().length).toBe(2);
    input(getInputs()[1], '');
    component.onBlur(1);

    fixture.detectChanges();

    expect(getInputs().length).toBe(1);

    expect(onChangeSpy).toHaveBeenCalledWith([initialValues[0]]);
  });
});
