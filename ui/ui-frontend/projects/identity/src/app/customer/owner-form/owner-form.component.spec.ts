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
import { Component, signal, viewChild } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { form, FormField } from '@angular/forms/signals';
import { EMPTY } from 'rxjs';
import { CountryService, StartupService } from 'vitamui-library';

import { OwnerFormComponent, OwnerFormModel } from './owner-form.component';
import { OwnerFormValidators } from './owner-form.validators';

@Component({
  template: `<app-owner-form [formField]="hostForm.owner" [customerId]="'42'"></app-owner-form>`,
  imports: [OwnerFormComponent, FormField],
})
class TestHostComponent {
  readonly hostForm = form(signal<{ owner: OwnerFormModel }>({ owner: null }));
  readonly ownerForm = viewChild(OwnerFormComponent);
}

describe('OwnerFormComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
      providers: [
        { provide: OwnerFormValidators, useValue: { uniqueCodeRule: vi.fn() } },
        { provide: CountryService, useValue: { getAvailableCountries: () => EMPTY } },
        { provide: StartupService, useValue: { getConfigNumberValue: () => 100 } },
      ],
    })
      .overrideComponent(OwnerFormComponent, { set: { template: '' } })
      .compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
    await fixture.whenStable();
  });

  const fillOwner = async (code: string) => {
    const ownerForm = host.ownerForm().form;
    ownerForm.code().value.set(code);
    ownerForm.name().value.set('Owner');
    ownerForm.companyName().value.set('Company');
    await fixture.whenStable();
  };

  it('should not send a value while the owner is invalid', async () => {
    expect(host.hostForm.owner().value()).toBeNull();
    expect(host.hostForm().dirty()).toBe(false);
    expect(host.ownerForm().form.customerId().value()).toEqual('42');

    await fillOwner('ABC');
    expect(host.hostForm.owner().value()).toBeNull();
  });

  it('should send the owner when it is valid', async () => {
    await fillOwner('ABCDEF');
    expect(host.hostForm.owner().value()).toEqual(
      expect.objectContaining({ code: 'ABCDEF', name: 'Owner', companyName: 'Company', customerId: '42' }),
    );

    host.ownerForm().form.name().value.set('');
    await fixture.whenStable();
    expect(host.hostForm.owner().value()).toBeNull();
  });

  it('should fill the form with the owner set by the parent', async () => {
    host.hostForm.owner().value.set({ code: 'OWNER1', name: 'Name', companyName: 'Company' } as OwnerFormModel);
    await fixture.whenStable();
    expect(host.ownerForm().form.code().value()).toEqual('OWNER1');
    expect(host.ownerForm().form.address.country().value()).toBeNull();
  });
});
