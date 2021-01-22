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
/* tslint:disable: max-classes-per-file directive-selector */
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSelectModule} from '@angular/material/select';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {EMPTY, of} from 'rxjs';
import {ConfirmDialogService} from 'ui-frontend-common';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';

import {RuleService} from '../rule.service';
import {RULE_MEASUREMENTS, RULE_TYPES} from '../rules.constants';
import {RuleCreateComponent} from './rule-create.component';
import {RuleCreateValidators} from './rule-create.validators';

const expectedRule = {
  ruleId: '424242',
  ruleType: RULE_TYPES[0].key,
  ruleValue: '111',
  ruleDescription: 'DESC',
  ruleDuration: '10',
  ruleMeasurement: RULE_MEASUREMENTS[0].key
};

let component: RuleCreateComponent;
let fixture: ComponentFixture<RuleCreateComponent>;

class Page {

  get submit() {
    return fixture.nativeElement.querySelector('button[type=submit]');
  }

  control(name: string) {
    return fixture.nativeElement.querySelector('[formControlName=' + name + ']');
  }

}

let page: Page;

describe('RuleCreateComponent', () => {

  beforeEach(waitForAsync(() => {
    const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
    const ruleServiceSpy = jasmine.createSpyObj('RuleService', {
      create: of({}),
      existsProperties: of(false)
    });
    const ruleCreateValidators: RuleCreateValidators = new RuleCreateValidators(null);
    const ruleCreateValidatorsSpy = jasmine.createSpyObj(
      'RuleCreateValidators', {
        uniqueRuleId: () => of(null),
        ruleIdPattern: ruleCreateValidators.ruleIdPattern()
      }
    );

    TestBed.configureTestingModule({
      declarations: [
        RuleCreateComponent
      ],
      imports: [
        ReactiveFormsModule,
        NoopAnimationsModule,
        MatFormFieldModule,
        MatSelectModule,
        MatButtonToggleModule,
        MatProgressBarModule,
        MatProgressSpinnerModule,
        MatDialogModule,
        VitamUICommonTestModule,
      ],
      providers: [
        {provide: RuleService, useValue: ruleServiceSpy},
        {provide: RuleCreateValidators, useValue: ruleCreateValidatorsSpy},
        {provide: ConfirmDialogService, useValue: {listenToEscapeKeyPress: () => EMPTY}},
        {provide: MatDialogRef, useValue: matDialogRefSpy},
        {provide: MAT_DIALOG_DATA, useValue: {}}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RuleCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    page = new Page();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Template', () => {
    it('should have the right inputs', () => {
      expect(page.control('ruleId')).toBeTruthy();
      expect(page.control('ruleType')).toBeTruthy();
      expect(page.control('ruleValue')).toBeTruthy();
      expect(page.control('ruleDescription')).toBeTruthy();
      expect(page.control('ruleDuration')).toBeTruthy();
      expect(page.control('ruleMeasurement')).toBeTruthy();
    });

    it('should have a submit button', () => {
      expect(page.submit).toBeTruthy();
      expect(page.submit.attributes.disabled).toBeTruthy();
      component.form.setValue(expectedRule);
      fixture.detectChanges();

      console.log('ATTRIBUTES : ', page.submit.attributes);
      console.log('DISABLED : ', page.submit.attributes.disabled);

      expect(page.submit.attributes.disabled).toBeFalsy();
    });
  });

  describe('Form', () => {
    it('should be invalid when empty', () => {
      expect(component.form.invalid).toBeTruthy();
    });

    it('should be valid', () => {
      component.form.setValue(expectedRule);
      expect(component.form.valid).toBeTruthy();
    });

    describe('Validators', () => {

      describe('fields', () => {
        it('should be required', () => {
          expect(setControlValue('ruleId', '').invalid).toBeTruthy('empty ruleId invalid');
          expect(setControlValue('ruleId', 'ÀÖØöøÿ ').invalid).toBeTruthy('ruleId pattern invalid');
          expect(setControlValue('ruleId', 'azerty').valid).toBeTruthy('ruleId valid');

          expect(setControlValue('ruleType', '').invalid).toBeTruthy('empty ruleType invalid');
          expect(setControlValue('ruleType', RULE_TYPES[0].key).valid).toBeTruthy('ruleType valid');

          expect(setControlValue('ruleValue', '').invalid).toBeTruthy('empty ruleValue invalid');
          expect(setControlValue('ruleValue', '111').valid).toBeTruthy('ruleValue valid');

          expect(setControlValue('ruleDuration', '').invalid).toBeTruthy('empty ruleDuration invalid');
          expect(setControlValue('ruleDuration', '10').valid).toBeTruthy('ruleDuration valid');

          expect(setControlValue('ruleMeasurement', '').invalid).toBeTruthy('empty ruleMeasurement invalid');
          expect(setControlValue('ruleMeasurement', RULE_MEASUREMENTS[0].key).valid).toBeTruthy('ruleMeasurement valid');
        });
      });

      function setControlValue(name: string | Array<string | number>, value: any) {
        const control = component.form.get(name);
        control.setValue(value);

        return control;
      }
    });
  });

  describe('Component', () => {
    it('should call dialogRef.close', () => {
      const matDialogRef = TestBed.inject(MatDialogRef);
      component.onCancel();
      expect(matDialogRef.close).toHaveBeenCalledTimes(1);
    });

    it('should not call create()', () => {
      const ruleService = TestBed.inject(RuleService);
      component.onSubmit();
      expect(ruleService.create).toHaveBeenCalledTimes(0);
    });

    it('should call create()', () => {
      const ruleService = TestBed.inject(RuleService);
      const matDialogRef = TestBed.inject(MatDialogRef);
      component.form.setValue(expectedRule);
      component.onSubmit();
      expect(ruleService.create).toHaveBeenCalledTimes(1);
      expect(matDialogRef.close).toHaveBeenCalledTimes(1);
    });
  });

});
