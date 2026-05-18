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
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Component, ViewChild, NgModule, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { EMPTY, of } from 'rxjs';
import { AuthService, BASE_URL, CountryService, Group, LoggerModule, WINDOW_LOCATION } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { GroupService } from '../../group.service';
import { GroupValidators } from '../../group.validators';
import { InformationTabComponent } from './information-tab.component';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

let expectedGroup: Group;

@Component({
  template: ` <app-information-tab [group]="group" [readOnly]="readOnly"></app-information-tab>`,
  standalone: false,
})
class TestHostComponent {
  group = expectedGroup;
  readOnly = false;

  @ViewChild(InformationTabComponent, { static: false })
  component: InformationTabComponent;
}

@NgModule({ declarations: [TestHostComponent], schemas: [NO_ERRORS_SCHEMA] })
class TestHostModule {}

describe('Profile Group InformationTabComponent', () => {
  let testhost: TestHostComponent;
  let fixture: ComponentFixture<TestHostComponent>;
  const groupServiceSpy = {
    patch: vi.fn().mockName('GroupService.patch').mockReturnValue(of({})),
  };
  const groupValidatorsSpy = {
    nameExists: vi
      .fn()
      .mockName('GroupValidators.nameExists')
      .mockReturnValue(() => of(null)),
  };
  const authServiceMock = { user: { level: '' } };
  const matDialogSpy = {
    open: vi.fn().mockName('MatDialog.open'),
  };
  matDialogSpy.open.mockReturnValue({ afterClosed: () => of(true) });

  beforeEach(async () => {
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

    await TestBed.configureTestingModule({
      declarations: [InformationTabComponent, TestHostComponent],
      schemas: [NO_ERRORS_SCHEMA],
      imports: [ReactiveFormsModule, VitamUICommonTestModule, LoggerModule.forRoot()],
      providers: [
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: GroupService, useValue: groupServiceSpy },
        { provide: GroupValidators, useValue: groupValidatorsSpy },
        { provide: AuthService, useValue: authServiceMock },
        { provide: CountryService, useValue: { getAvailableCountries: () => EMPTY } },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
    })
      .overrideComponent(InformationTabComponent, { set: { template: '' } })
      .compileComponents();
  });

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
      expect(testhost.component.form.get('id').valid).toBeFalsy();
      expect(testhost.component.form.get('name').valid).toBeFalsy();
      expect(testhost.component.form.get('description').valid).toBeFalsy();
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
      testhost.component.readOnly = true;
      testhost.component.ngOnChanges({
        readOnly: { previousValue: false, currentValue: true, firstChange: false, isFirstChange: () => false },
      });
      expect(testhost.component.form.disabled).toBe(true);
      testhost.component.readOnly = false;
      testhost.component.ngOnChanges({
        readOnly: { previousValue: true, currentValue: false, firstChange: false, isFirstChange: () => false },
      });
      expect(testhost.component.form.disabled).toBe(false);
    });
  });
});
