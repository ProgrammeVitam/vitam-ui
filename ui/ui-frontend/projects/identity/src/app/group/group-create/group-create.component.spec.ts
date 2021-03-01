import { TranslateModule } from '@ngx-translate/core';
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
import { AuthService, ConfirmDialogService, Group, LevelInputModule, ProfileService } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';

import { Component, forwardRef, Input, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { GroupService } from '../group.service';
import { GroupValidators } from '../group.validators';
import { GroupCreateComponent } from './group-create.component';

@Component({
  selector: 'app-profiles-form',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => ProfilesFormStubComponent),
    multi: true,
  }]
})
class ProfilesFormStubComponent implements ControlValueAccessor {
  @Input() level: string;
  writeValue() {}
  registerOnChange() {}
  registerOnTouched() {}
}

const expectedGroup: Group = {
  id: '1',
  customerId: '4242442',
  enabled: true,
  name: 'Group Name',
  description: 'Group Description',
  level : '',
  usersCount: 0,
  profileIds: ['profile1', 'profile2'],
  profiles: [],
  readonly : false,
};

let fixture: ComponentFixture<GroupCreateComponent>;
let component: GroupCreateComponent;

class Page {

  get submit() { return fixture.nativeElement.querySelector('button[type=submit]'); }
  control(name: string) { return fixture.nativeElement.querySelector('[formControlName=' + name + ']'); }

}

let page: Page;

describe('GroupCreateComponent', () => {

  beforeEach(waitForAsync(() => {
    const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
    const profileGroupServiceSpy = jasmine.createSpyObj('GroupService', { create: of({}) });
    const groupValidatorsSpy = jasmine.createSpyObj('GroupValidators', { nameExists: () => of(null) });

    TestBed.configureTestingModule({
      imports: [
          MatProgressBarModule,
          ReactiveFormsModule,
          NoopAnimationsModule,
          VitamUICommonTestModule,
          LevelInputModule,
          TranslateModule.forRoot(),
      ],
      declarations: [
        ProfilesFormStubComponent,
        GroupCreateComponent,
      ],
      providers: [
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: GroupService, useValue: profileGroupServiceSpy },
        { provide: AuthService, useValue: { user: { customerId: '4242442', level: '' } } },
        { provide: ProfileService, useValue: { list: () => of([]) } },
        { provide: GroupValidators, useValue: groupValidatorsSpy },
        { provide: ConfirmDialogService, useValue: { listenToEscapeKeyPress: () => EMPTY } },
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GroupCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    page = new Page();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('DOM', () => {
    it('should have the right inputs', () => {
      expect(page.control('name')).toBeTruthy();
      expect(page.control('description')).toBeTruthy();
      expect(page.control('level')).toBeTruthy();
    });

    it('should have a submit button', () => {
      expect(page.submit).toBeTruthy();
      expect(page.submit.attributes.disabled).toBeTruthy();
      component.form.setValue({
        customerId: expectedGroup.customerId,
        name: expectedGroup.name,
        level: '',
        enabled: expectedGroup.enabled,
        description: expectedGroup.description,
        profileIds: expectedGroup.profileIds
      });
      fixture.detectChanges();
      expect(page.submit.attributes.disabled).toBeFalsy();
    });
  });

  describe('Form', () => {
    it('should be invalid when empty', () => {
      expect(component.form.invalid).toBeTruthy();
    });

    it('should be valid', () => {
      component.form.setValue({
        customerId: expectedGroup.customerId,
        name: expectedGroup.name,
        level: '',
        enabled: expectedGroup.enabled,
        description: expectedGroup.description,
        profileIds: expectedGroup.profileIds
      });
      expect(component.form.valid).toBeTruthy();
    });

    describe('Validators', () => {

      describe('fields', () => {
        it('should be required', () => {
          expect(setControlValue('name', '').invalid).toBeTruthy('name');
          expect(setControlValue('name', 'nn').valid).toBeTruthy('name');

          expect(setControlValue('description', '').invalid).toBeTruthy('description');
          expect(setControlValue('description', 'tttt').valid).toBeTruthy('description');

          expect(setControlValue('profileIds', '').invalid).toBeTruthy('profileIds');
          expect(setControlValue('profileIds', []).invalid).toBeTruthy('profileIds');
          expect(setControlValue('profileIds', ['test1']).valid).toBeTruthy('profileIds');

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
      const matDialogRef =  TestBed.inject(MatDialogRef);
      component.onCancel();
      expect(matDialogRef.close).toHaveBeenCalledTimes(1);
    });

    it('should not call create()', () => {
      const groupService =  TestBed.inject(GroupService);
      component.onSubmit();
      expect(groupService.create).toHaveBeenCalledTimes(0);
    });

    it('should call create()', () => {
      const groupService =  TestBed.inject(GroupService);
      const matDialogRef =  TestBed.inject(MatDialogRef);
      component.form.setValue({
        customerId: expectedGroup.customerId,
        name: expectedGroup.name,
        level: '',
        enabled: expectedGroup.enabled,
        description: expectedGroup.description,
        profileIds: expectedGroup.profileIds
      });
      component.onSubmit();
      expect(groupService.create).toHaveBeenCalledTimes(1);
      expect(matDialogRef.close).toHaveBeenCalledTimes(1);
    });
  });

});
