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

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { MatLegacyFormFieldModule as MatFormFieldModule } from '@angular/material/legacy-form-field';
import { MatLegacyProgressBarModule as MatProgressBarModule } from '@angular/material/legacy-progress-bar';
import { MatLegacyProgressSpinnerModule as MatProgressSpinnerModule } from '@angular/material/legacy-progress-spinner';
import { MatLegacySelectModule as MatSelectModule } from '@angular/material/legacy-select';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { EMPTY, of } from 'rxjs';
import { ConfirmDialogService } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';

import { OntologyService } from '../ontology.service';
import { OntologyCreateComponent } from './ontology-create.component';
import { OntologyCreateValidators } from './ontology-create.validators';

const expectedOntology = {
  shortName: 'Name',
  identifier: 'identifier',
  type: 'TEXT',
  collections: ['ObjectGroup'],
  description: 'Mon Ontologie',
  origin: 'INTERNAL',
  typeDetail: 'STRING',
  stringSize: 'MEDIUM',
};

let component: OntologyCreateComponent;
let fixture: ComponentFixture<OntologyCreateComponent>;

class Page {
  get submit() {
    return fixture.nativeElement.querySelector('button[type=submit]');
  }

  control(name: string) {
    return fixture.nativeElement.querySelector('[formControlName=' + name + ']');
  }
}

let page: Page;

describe('OntologyCreateComponent', () => {
  beforeEach(async () => {
    const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
    const ontologyServiceSpy = jasmine.createSpyObj('OntologyService', {
      create: of({}),
    });

    const ontologyCreateValidatorsSpy = jasmine.createSpyObj('OntologyCreateValidators', {
      uniqueID: () => () => of(null),
      patternID: () => of(null),
    });

    await TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        MatFormFieldModule,
        MatSelectModule,
        MatButtonToggleModule,
        MatProgressBarModule,
        NoopAnimationsModule,
        MatProgressSpinnerModule,
        VitamUICommonTestModule,
      ],
      declarations: [OntologyCreateComponent],
      providers: [
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: OntologyService, useValue: ontologyServiceSpy },
        { provide: ConfirmDialogService, useValue: { listenToEscapeKeyPress: () => EMPTY } },
        { provide: OntologyCreateValidators, useValue: ontologyCreateValidatorsSpy },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OntologyCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    page = new Page();
  });

  it('Component should be created', () => {
    expect(component).toBeTruthy();
  });

  describe('Template', () => {
    it('should have the right inputs', () => {
      expect(page.control('identifier')).toBeTruthy('id');
      expect(page.control('shortName')).toBeTruthy('name');
      expect(page.control('type')).toBeTruthy('type');
      expect(page.control('collections')).toBeTruthy('collections');
      expect(page.control('description')).toBeTruthy('desc');
    });

    it('should have a submit button', () => {
      expect(page.submit).toBeTruthy();
      expect(page.submit.attributes.disabled).toBeTruthy();
      component.form.setValue(expectedOntology);
      fixture.detectChanges();
      expect(page.submit.attributes.type).toBeDefined();
    });
  });

  describe('Form', () => {
    it('should be invalid when empty', () => {
      expect(component.form.invalid).toBeTruthy();
    });

    describe('Validators', () => {
      describe('fields', () => {
        it('should be required', () => {
          expect(setControlValue('shortName', 'n').invalid).toBeTruthy('shortName too short');
          expect(setControlValue('shortName', 'name').valid).toBeTruthy('shortName');

          expect(setControlValue('identifier', '').invalid).toBeTruthy('identifier required');
          expect(setControlValue('identifier', 'i').invalid).toBeTruthy('identifier too short');

          expect(setControlValue('type', '').invalid).toBeTruthy('type required');
          expect(setControlValue('type', 't').valid).toBeTruthy('type');
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
      const customerService = TestBed.inject(OntologyService);
      component.onSubmit();
      expect(customerService.create).toHaveBeenCalledTimes(0);
    });

    it('should call create()', () => {
      const customerService = TestBed.inject(OntologyService);
      const matDialogRef = TestBed.inject(MatDialogRef);
      component.form.setValue(expectedOntology);
      component.onSubmit();
      expect(customerService.create).toHaveBeenCalledTimes(0);
      expect(matDialogRef.close).toHaveBeenCalledTimes(0);
    });
  });
});
