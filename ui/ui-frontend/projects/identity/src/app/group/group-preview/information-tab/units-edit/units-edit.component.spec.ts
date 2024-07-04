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

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Component, forwardRef, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { MatLegacyProgressBarModule as MatProgressBarModule } from '@angular/material/legacy-progress-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { EMPTY, of } from 'rxjs';
import { BASE_URL, ConfirmDialogService } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { GroupService } from '../../../group.service';
import { UnitsEditComponent } from './units-edit.component';

@Component({
  selector: 'app-units-form',
  template: '',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => UnitsFormStubComponent),
      multi: true,
    },
  ],
  standalone: true,
  imports: [HttpClientTestingModule, MatProgressBarModule, ReactiveFormsModule, VitamUICommonTestModule],
})
class UnitsFormStubComponent {
  writeValue() {}
  registerOnChange() {}
  registerOnTouched() {}
}

describe('UnitsEditComponent', () => {
  let component: UnitsEditComponent;
  let fixture: ComponentFixture<UnitsEditComponent>;

  beforeEach(async () => {
    const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        MatProgressBarModule,
        ReactiveFormsModule,
        NoopAnimationsModule,
        VitamUICommonTestModule,
        UnitsEditComponent,
        UnitsFormStubComponent,
      ],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: { group: { id: '42', name: 'Test', units: [] } } },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: GroupService, useValue: { patch: () => of({ result: 'test' }) } },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ConfirmDialogService, useValue: { listenToEscapeKeyPress: () => EMPTY } },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UnitsEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Component', () => {
    it('should call groupService.patch when form is dirty', () => {
      const groupService = TestBed.inject(GroupService);
      spyOn(groupService, 'patch').and.callThrough();
      const matDialogRefSpy = TestBed.inject(MatDialogRef);
      component.form.setValue({ units: ['1', '2', '3'] });
      component.form.markAsDirty();
      component.onSubmit();
      expect(groupService.patch).toHaveBeenCalledWith({ id: '42', units: ['1', '2', '3'] });
      expect(matDialogRefSpy.close).toHaveBeenCalledWith({ result: 'test' });
    });

    it('should not call groupService.patch when form is pristine', () => {
      const groupService = TestBed.inject(GroupService);
      spyOn(groupService, 'patch').and.callThrough();
      component.form.markAsPristine();
      component.onSubmit();
      expect(groupService.patch).toHaveBeenCalledTimes(0);
    });

    it('should call MatDialogRef close when dialog is closed', () => {
      const matDialogRefSpy = TestBed.inject(MatDialogRef);
      component.onCancel();
      expect(matDialogRefSpy.close).toHaveBeenCalledTimes(1);
    });
  });
});
