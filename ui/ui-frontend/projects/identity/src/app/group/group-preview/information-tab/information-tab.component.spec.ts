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
import { Component, Directive, Input, ViewChild } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { EMPTY, of } from 'rxjs';
import { AuthService, BASE_URL, Group, LoggerModule, WINDOW_LOCATION } from 'vitamui-library';
import { CountryService } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { GroupService } from '../../group.service';
import { GroupValidators } from '../../group.validators';
import { InformationTabComponent } from './information-tab.component';

let expectedGroup: Group;

// eslint-disable-next-line @angular-eslint/directive-selector
@Directive({ selector: '[matTooltip]' })
class MatTooltipStubDirective {
  @Input() matTooltip: any;
  @Input() matTooltipDisabled: any;
  @Input() matTooltipClass: any;
}

@Component({
  template: `<app-information-tab [group]="group" [readOnly]="readOnly"></app-information-tab>`,
})
class TestHostComponent {
  group = expectedGroup;
  readOnly = false;

  @ViewChild(InformationTabComponent, { static: false }) component: InformationTabComponent;
}

describe('Profile Group InformationTabComponent', () => {
  let testhost: TestHostComponent;
  let fixture: ComponentFixture<TestHostComponent>;
  const groupServiceSpy = jasmine.createSpyObj('GroupService', { patch: of({}) });
  const groupValidatorsSpy = jasmine.createSpyObj('GroupValidators', { nameExists: () => of(null) });
  const authServiceMock = { user: { level: '' } };
  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
  matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

  beforeEach(waitForAsync(() => {
    expectedGroup = {
      id: '42',
      enabled: true,
      identifier: '1',
      customerId: '4242442',
      name: 'Group Name',
      description: 'Group Description',
      level: '',
      usersCount: 0,
      profileIds: [],
      profiles: [],
      units: [],
      readonly: false,
    };

    TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, VitamUICommonTestModule, LoggerModule.forRoot(), HttpClientTestingModule],
      declarations: [InformationTabComponent, TestHostComponent, MatTooltipStubDirective],
      providers: [
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: GroupService, useValue: groupServiceSpy },
        { provide: GroupValidators, useValue: groupValidatorsSpy },
        { provide: AuthService, useValue: authServiceMock },
        { provide: CountryService, useValue: { getAvailableCountries: () => EMPTY } },
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestHostComponent);
    testhost = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(testhost).toBeTruthy();
  });

  describe('Class', () => {
    it('should have the correct fields', () => {
      expect(testhost.component.form.get('id')).not.toBeNull();
      expect(testhost.component.form.get('name')).not.toBeNull();
      expect(testhost.component.form.get('description')).not.toBeNull();
    });

    it('should have the required validator', () => {
      testhost.component.form.setValue({
        id: null,
        identifier: null,
        name: null,
        level: null,
        enabled: false,
        description: null,
      });
      expect(testhost.component.form.get('id').valid).toBeFalsy('id');
      expect(testhost.component.form.get('name').valid).toBeFalsy('name');
      expect(testhost.component.form.get('description').valid).toBeFalsy('description');
    });

    it('should be valid and call patch()', () => {
      testhost.component.form.setValue({
        id: expectedGroup.id,
        identifier: expectedGroup.identifier,
        enabled: expectedGroup.enabled,
        name: expectedGroup.name,
        level: '',
        description: expectedGroup.description,
      });
      expect(testhost.component.form.valid).toBeTruthy();
    });

    it('should disable then enable the form', () => {
      testhost.readOnly = true;
      fixture.detectChanges();
      expect(testhost.component.form.disabled).toBe(true);
      testhost.readOnly = false;
      fixture.detectChanges();
      expect(testhost.component.form.disabled).toBe(false);
    });
  });
});
