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
/* eslint-disable max-classes-per-file, @angular-eslint/directive-selector */
import { Component, forwardRef, Input, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { EMPTY, of } from 'rxjs';
import { BASE_URL, ConfirmDialogService, LoggerModule, MiscValidators, SelectComponent } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { ContextService } from '../context.service';
import { ContextCreateComponent } from './context-create.component';
import { ContextCreateValidators } from './context-create.validators';
import { SecurityProfileService } from '../../security-profile/security-profile.service';
import { TranslateModule } from '@ngx-translate/core';
import { ContextEditPermissionModule } from './context-edit-permission/context-edit-permission.module';

@Component({
  selector: 'app-owner-form',
  template: '',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => OwnerFormStubComponent),
      multi: true,
    },
  ],
  standalone: false,
})
class OwnerFormStubComponent implements ControlValueAccessor {
  @Input() contextInfo: any;

  writeValue() {}

  registerOnChange() {}

  registerOnTouched() {}
}

const expectedContext = {
  status: 'INACTIVE',
  name: 'John Doe',
  identifier: 'jdoe',
  securityProfile: 'securityProfile',
  enableControl: false,
  permissions: [] as unknown[],
};

class Page {
  constructor(private fixture: ComponentFixture<ContextCreateComponent>) {}
  get submit() {
    return this.fixture.nativeElement.querySelector('button[type=submit]');
  }

  control(name: string) {
    return this.fixture.nativeElement.querySelector('[formControlName=' + name + ']');
  }
}

const securityProfileServiceMock = {
  getAll: () => of([]),
};

describe('ContextCreateComponent', () => {
  let component: ContextCreateComponent;
  let fixture: ComponentFixture<ContextCreateComponent>;
  let page: Page;

  beforeEach(async () => {
    const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
    const contextServiceSpy = jasmine.createSpyObj('ContextService', { create: of({}), existsProperties: of({}) });
    await TestBed.configureTestingModule({
      imports: [
        ContextEditPermissionModule,
        LoggerModule.forRoot(),
        MatButtonToggleModule,
        MatDialogActions,
        MatFormFieldModule,
        MatProgressBarModule,
        MatProgressSpinnerModule,
        MatSelectModule,
        NoopAnimationsModule,
        ReactiveFormsModule,
        SelectComponent,
        TranslateModule.forRoot(),
        VitamUICommonTestModule,
      ],
      declarations: [ContextCreateComponent, OwnerFormStubComponent],
      providers: [
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: ContextService, useValue: contextServiceSpy },
        ContextCreateValidators,
        { provide: ConfirmDialogService, useValue: { listenToEscapeKeyPress: () => EMPTY } },
        { provide: SecurityProfileService, useValue: securityProfileServiceMock },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    page = new Page(fixture);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('applies MiscValidators.requiredIdentifier to identifier control', () => {
    expect(component.form.get('identifier').hasValidator(MiscValidators.requiredIdentifier)).toBeTruthy();
  });

  describe('Template', () => {
    it('should have the right inputs', () => {
      expect(page.control('name')).toBeTruthy();
    });

    it('should have a submit button', () => {
      expect(page.submit).toBeTruthy();
    });
  });

  describe('Component', () => {
    it('should call dialogRef.close', () => {
      const matDialogRef = TestBed.inject(MatDialogRef);
      component.onCancel();
      expect(matDialogRef.close).toHaveBeenCalledTimes(1);
    });

    it('should not call create()', () => {
      const contextService = TestBed.inject(ContextService);
      component.onSubmit();
      expect(contextService.create).toHaveBeenCalledTimes(0);
    });

    it('should call create()', () => {
      const contextService = TestBed.inject(ContextService);
      const matDialogRef = TestBed.inject(MatDialogRef);
      component.form.setValue(expectedContext);
      component.onSubmit();
      expect(contextService.create).toHaveBeenCalledTimes(1);
      expect(matDialogRef.close).toHaveBeenCalledTimes(1);
    });
  });
});
