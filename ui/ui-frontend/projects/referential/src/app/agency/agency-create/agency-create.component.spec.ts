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
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSelectModule} from '@angular/material/select';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {EMPTY, of} from 'rxjs';
import {ConfirmDialogService} from 'ui-frontend-common';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';

import {DomainsInputModule} from '../../../../../identity/src/app/shared/domains-input';
import {AgencyService} from '../agency.service';
import {AgencyCreateComponent} from './agency-create.component';
import {AgencyCreateValidators} from './agency-create.validators';

const expectedAgency = {
  name: 'Random Agency',
  identifier: 'Random Agency',
  description: 'My Beautiful description',
};

let component: AgencyCreateComponent;
let fixture: ComponentFixture<AgencyCreateComponent>;

class Page {

  get submit() {
    return fixture.nativeElement.querySelector('button[type=submit]');
  }

  control(name: string) {
    return fixture.nativeElement.querySelector('[formControlName=' + name + ']');
  }

}

let page: Page;

describe('AgencyCreateComponent', () => {

  beforeEach(waitForAsync(() => {
    const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

    const agencyServiceSpy = jasmine.createSpyObj('AgencyService', {create: of({})});
    const agencyValidatorSpy = jasmine.createSpyObj(
      'AgencyCreateValidators',
      {uniqueIdentifier: () => of(null), uniqueName: () => of(null)}
    );
    TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        MatFormFieldModule,
        MatSelectModule,
        MatButtonToggleModule,
        MatProgressBarModule,
        NoopAnimationsModule,
        DomainsInputModule,
        MatProgressSpinnerModule,
        VitamUICommonTestModule,
      ],
      declarations: [
        AgencyCreateComponent
      ],
      providers: [
        {provide: MatDialogRef, useValue: matDialogRefSpy},
        {provide: MAT_DIALOG_DATA, useValue: {}},
        {provide: AgencyService, useValue: agencyServiceSpy},
        {provide: AgencyCreateValidators, useValue: agencyValidatorSpy},
        {provide: ConfirmDialogService, useValue: {listenToEscapeKeyPress: () => EMPTY}},
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AgencyCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    page = new Page();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Template', () => {
    it('should have the right inputs', () => {
      expect(page.control('name')).toBeTruthy();
      expect(page.control('identifier')).toBeTruthy();
      expect(page.control('description')).toBeTruthy();
    });

    it('should have a submit button', () => {
      expect(page.submit).toBeTruthy();
      expect(page.submit.attributes.disabled).toBeTruthy();
      component.form.setValue(expectedAgency);
      fixture.detectChanges();
      expect(page.submit.attributes.disabled).toBeFalsy();
    });
  });

  describe('Form', () => {
    it('should be invalid when empty', () => {
      expect(component.form.invalid).toBeTruthy();
    });

    it('should be valid', () => {
      component.form.setValue(expectedAgency);
      expect(component.form.valid).toBeTruthy();
    });

    describe('Validators', () => {

      describe('fields', () => {
        it('should be required', () => {
          expect(setControlValue('name', '').invalid).toBeTruthy();
          expect(setControlValue('name', 'name').valid).toBeTruthy();

          expect(setControlValue('identifier', '').invalid).toBeTruthy();
          expect(setControlValue('identifier', 'identifier').valid).toBeTruthy();
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
      const agencyService = TestBed.inject(AgencyService);
      component.onSubmit();
      expect(agencyService.create).toHaveBeenCalledTimes(0);
    });

    it('should call create()', () => {
      const agencyService = TestBed.inject(AgencyService);
      const matDialogRef = TestBed.inject(MatDialogRef);
      component.form.setValue(expectedAgency);
      component.onSubmit();
      expect(agencyService.create).toHaveBeenCalledTimes(1);
      expect(matDialogRef.close).toHaveBeenCalledTimes(1);
    });
  });

});
