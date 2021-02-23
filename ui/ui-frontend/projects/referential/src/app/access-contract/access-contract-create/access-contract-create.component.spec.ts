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
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {EMPTY, of} from 'rxjs';
import {ConfirmDialogService, ExternalParametersService} from 'ui-frontend-common';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';

import {AgencyService} from '../../agency/agency.service';
import {AccessContractService} from '../access-contract.service';
import {AccessContractCreateComponent} from './access-contract-create.component';
import {AccessContractCreateValidators} from './access-contract-create.validators';

const expectedAccessContract = {
  identifier: 'AC_ID',
  name: 'AC_Name',
  status: 'ACTIVE',
  description: 'description',
  everyOriginatingAgency: false,
  everyDataObjectVersion: false,
  dataObjectVersion: [''],
  writingPermission: false,
  writingRestrictedDesc: false,
  accessLog: 'ACTIVE',
  ruleCategoryToFilter: [''],
  originatingAgencies: [''],
  rootUnits: [''],
  excludedRootUnits: ['']
};

let component: AccessContractCreateComponent;
let fixture: ComponentFixture<AccessContractCreateComponent>;

class Page {

  get submit() {
    return fixture.nativeElement.querySelector('button[type=submit]');
  }

  control(name: string) {
    return fixture.nativeElement.querySelector('[formControlName=' + name + ']');
  }

}

let page: Page;

describe('AccessContractCreateComponent', () => {

  beforeEach(waitForAsync(() => {
    const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
    const agencyServiceSpy = jasmine.createSpyObj('AgencyService', {getAll: of([])});
    const accessContractServiceSpy = jasmine.createSpyObj('AccessContractService', {create: of({}), getAll: of([])});
    const accessContractCreateValidatorsSpy = jasmine.createSpyObj(
      'AccessContractCreateValidators', 
      {
        uniqueName: () => of(null), uniqueNameWhileEdit: of(null),
        uniqueIdentifier: () => of(null), identifierToIgnore: ''
      }
    );

    const parameters: Map<string, string> = new Map<string, string>();
    const externalParametersServiceMock = {
      getUserExternalParameters: () => of(parameters)
    };

    TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        MatFormFieldModule,
        MatSelectModule,
        MatButtonToggleModule,
        MatProgressBarModule,
        MatSnackBarModule,
        NoopAnimationsModule,
        MatProgressSpinnerModule,
        VitamUICommonTestModule,
      ],
      declarations: [
        AccessContractCreateComponent
      ],
      providers: [
        {provide: MatDialogRef, useValue: matDialogRefSpy},
        {provide: MAT_DIALOG_DATA, useValue: {}},
        {provide: AgencyService, useValue: agencyServiceSpy},
        {provide: AccessContractService, useValue: accessContractServiceSpy},
        {provide: ExternalParametersService, useValue: externalParametersServiceMock},
        {provide: AccessContractCreateValidators, useValue: accessContractCreateValidatorsSpy},
        {provide: ConfirmDialogService, useValue: {listenToEscapeKeyPress: () => EMPTY}}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessContractCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    page = new Page();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Template', () => {
    // TODO: Why X it ?
    xit('should have the right inputs', () => {
      expect(page.control('identifier')).toBeTruthy();
      expect(page.control('status')).toBeTruthy();
      expect(page.control('name')).toBeTruthy();
      expect(page.control('description')).toBeTruthy();
      expect(page.control('accessLog')).toBeTruthy();
      expect(page.control('ruleCategoryToFilter')).toBeTruthy();
      // Step 2
      expect(page.control('everyOriginatingAgency')).toBeTruthy();
      expect(page.control('originatingAgencies')).toBeTruthy();
      expect(page.control('everyDataObjectVersion')).toBeTruthy();
      expect(page.control('dataObjectVersion')).toBeTruthy();
      // Step 3
      expect(page.control('writingPermission')).toBeTruthy();
      expect(page.control('writingRestrictedDesc')).toBeTruthy();
      // Step 4
      expect(page.control('rootUnits')).toBeTruthy();
      expect(page.control('excludedRootUnits')).toBeTruthy();
    });

    it('should have a submit button', () => {
      expect(page.submit).toBeTruthy();
      expect(page.submit.attributes.disabled).toBeTruthy();
      component.form.setValue(expectedAccessContract);
      fixture.detectChanges();
      expect(page.submit.attributes.disabled).toBeFalsy();
    });
  });

  describe('Form', () => {
    it('should be invalid when empty', () => {
      expect(component.form.invalid).toBeTruthy();
    });

    it('should be valid', () => {
      component.form.setValue(expectedAccessContract);
      expect(component.form.valid).toBeTruthy();
    });

    describe('Validators', () => {

      describe('fields', () => {
        it('should be required', () => {
          expect(setControlValue('name', '').invalid).toBeTruthy();
          expect(setControlValue('name', 'nn').valid).toBeTruthy();

          expect(setControlValue('identifier', '').invalid).toBeTruthy();
          expect(setControlValue('identifier', 'tt').valid).toBeTruthy();
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
      const accessContractService = TestBed.inject(AccessContractService);
      component.onSubmit();
      expect(accessContractService.create).toHaveBeenCalledTimes(0);
    });

    it('should call create()', () => {
      const accessContractService = TestBed.inject(AccessContractService);
      const matDialogRef = TestBed.inject(MatDialogRef);
      component.form.setValue(expectedAccessContract);
      component.onSubmit();
      expect(accessContractService.create).toHaveBeenCalledTimes(1);
      expect(matDialogRef.close).toHaveBeenCalledTimes(1);
    });
  });

});
