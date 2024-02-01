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
/* tslint:disable: no-magic-numbers max-file-line-count max-classes-per-file */

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { GroupValidators } from '../group.validators';

import { UnitsFormComponent } from './units-form.component';

describe('UnitsFormComponent', () => {
  let component: UnitsFormComponent;
  let fixture: ComponentFixture<UnitsFormComponent>;

  beforeEach(waitForAsync(() => {
    const groupValidatorSpy = jasmine.createSpyObj('GroupService', { unitExists: () => of(null) });

    TestBed.configureTestingModule({
      imports: [FormsModule, ReactiveFormsModule, MatProgressSpinnerModule, NoopAnimationsModule, VitamUICommonTestModule],
      declarations: [UnitsFormComponent],
      providers: [{ provide: GroupValidators, useValue: groupValidatorSpy }],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UnitsFormComponent);
    component = fixture.componentInstance;
    component.ngOnInit();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('form valid when not empty', () => {
    component.unitControl.setValue('toto');
    const result = component.unitControl.valid;
    expect(result).toBe(true);
  });

  it('when add then unit is added', () => {
    component.unitControl.setValue('unit1');
    component.add();

    expect(component.units).toContain('unit1');
    expect(component.unitControl.value).toBeNull();
  });

  it('when remove then unit is removed', () => {
    component.units = ['unit1'];
    component.remove('unit1');

    expect(component.units.length).toEqual(0);
  });

  it('when add a unit already added then form is invalid', () => {
    component.units = ['unit1'];
    component.unitControl.setValue('unit1');

    expect(component.unitControl.valid).toBeFalsy();
    expect(component.unitControl.errors.unitAlreadyAdd).toBeTruthy();
  });
});
