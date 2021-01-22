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
import { EMPTY, of } from 'rxjs';
import { BASE_URL, ConfirmDialogService } from 'ui-frontend-common';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { Component, forwardRef, Input, NO_ERRORS_SCHEMA } from '@angular/core';
import { GroupService } from '../../../group.service';
import { ProfilesEditComponent } from './profiles-edit.component';

@Component({
  selector: 'app-profiles-form',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => ProfilesFormStubComponent),
    multi: true,
  }]
})
class ProfilesFormStubComponent {
  @Input() level: any;

  writeValue() {}
  registerOnChange() { }
  registerOnTouched() { }
}

describe('ProfilesEditComponent', () => {
  let component: ProfilesEditComponent;
  let fixture: ComponentFixture<ProfilesEditComponent>;

  beforeEach(waitForAsync(() => {
    const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        MatProgressBarModule,
        ReactiveFormsModule,
        NoopAnimationsModule,
      ],
      declarations: [ ProfilesEditComponent, ProfilesFormStubComponent ],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: { group: { id: '42', name: 'Test', profileIds: [] } } },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: GroupService, useValue: { patch: () => of({ result: 'test' }) } },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ConfirmDialogService, useValue: { listenToEscapeKeyPress: () => EMPTY } },
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfilesEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('DOM', () => {

    it('should have a title', () => {
      const elTitle = fixture.nativeElement.querySelector('.text, .large');
      expect(elTitle).toBeTruthy();
      expect(elTitle.textContent).toContain('Modification des profils de "Test"');
    });

    it('should have a app-profiles-form', () => {
      const elProfilesForm = fixture.nativeElement.querySelector('app-profiles-form[formControlName=profileIds]');
      expect(elProfilesForm).toBeTruthy();
    });

    it('should have a submit button', () => {
      spyOn(component, 'onSubmit');
      component.form.setValue({ profileIds: ['1'] });
      component.form.markAsDirty();
      fixture.detectChanges();
      const elSubmit = fixture.nativeElement.querySelector('button[type=submit]');
      expect(elSubmit).toBeTruthy();
      expect(elSubmit.textContent).toContain('Terminer');
      elSubmit.click();
      expect(component.onSubmit).toHaveBeenCalledTimes(1);
    });

    it('should have a cancel button', () => {
      spyOn(component, 'onCancel');
      const elCancel = fixture.nativeElement.querySelector('button[type=button].btn.cancel');
      expect(elCancel).toBeTruthy();
      expect(elCancel.textContent).toContain('Annuler');
      elCancel.click();
      expect(component.onCancel).toHaveBeenCalledTimes(1);
    });

  });

  describe('Component', () => {

    it('should call groupService.patch', () => {
      const groupService = TestBed.inject(GroupService);
      spyOn(groupService, 'patch').and.callThrough();
      const matDialogRefSpy = TestBed.inject(MatDialogRef);
      component.form.setValue({ profileIds: ['1', '2', '3'] });
      component.form.markAsDirty();
      component.onSubmit();
      expect(groupService.patch).toHaveBeenCalledWith({ id: '42', profileIds: ['1', '2', '3'] });
      expect(matDialogRefSpy.close).toHaveBeenCalledWith({ result: 'test' });
    });

    it('should not call profileGroupService.patch', () => {
      const profileGroupService = TestBed.inject(GroupService);
      spyOn(profileGroupService, 'patch').and.callThrough();
      component.form.markAsDirty();
      component.onSubmit();
      expect(profileGroupService.patch).toHaveBeenCalledTimes(0);
    });

    it('should not call profileGroupService.patch', () => {
      const profileGroupService = TestBed.inject(GroupService);
      spyOn(profileGroupService, 'patch').and.callThrough();
      component.form.setValue({ profileIds: ['1', '2', '3'] });
      component.onSubmit();
      expect(profileGroupService.patch).toHaveBeenCalledTimes(0);
    });

    it('should call MatDialogRef.close', () => {
      const matDialogRefSpy = TestBed.inject(MatDialogRef);
      component.onCancel();
      expect(matDialogRefSpy.close).toHaveBeenCalledTimes(1);
    });

  });
});
