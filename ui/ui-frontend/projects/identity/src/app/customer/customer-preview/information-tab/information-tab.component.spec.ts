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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EMPTY, of } from 'rxjs';
import { CountryService, Customer, StartupService } from 'vitamui-library';

import { CustomerService } from '../../../core/customer.service';
import { CustomerCreateValidators } from '../../customer-create/customer-create.validators';
import { InformationTabComponent } from './information-tab.component';

const customer = {
  id: '42',
  identifier: '1',
  code: 'CODE',
  name: 'Name',
  companyName: 'Company',
  passwordRevocationDelay: 6,
  otp: 'OPTIONAL',
  address: { street: 'street', zipCode: '75000', city: 'Paris', country: 'FR' },
  internalCode: null,
  language: 'FRENCH',
  emailDomains: ['test.com'],
  defaultEmailDomain: 'test.com',
  gdprAlert: true,
  gdprAlertDelay: 72,
} as unknown as Customer;

describe('Customer InformationTabComponent', () => {
  let fixture: ComponentFixture<InformationTabComponent>;
  let component: InformationTabComponent;
  let customerServiceSpy: { patch: ReturnType<typeof vi.fn> };

  const waitForUpdate = async (time = 500) => {
    TestBed.tick();
    await vi.advanceTimersByTimeAsync(time);
  };

  beforeEach(async () => {
    vi.useFakeTimers();
    customerServiceSpy = {
      patch: vi
        .fn()
        .mockName('CustomerService.patch')
        .mockReturnValue(of({ ...customer, name: 'New name' })),
    };
    await TestBed.configureTestingModule({
      imports: [InformationTabComponent],
      providers: [
        { provide: CustomerService, useValue: customerServiceSpy },
        {
          provide: CustomerCreateValidators,
          useValue: { uniqueCodeRule: vi.fn(), uniqueCode: vi.fn().mockReturnValue(() => of(null)) },
        },
        { provide: CountryService, useValue: { getAvailableCountries: () => EMPTY } },
        { provide: StartupService, useValue: { getConfigNumberValue: () => 100 } },
      ],
    })
      .overrideComponent(InformationTabComponent, { set: { template: '' } })
      .compileComponents();

    fixture = TestBed.createComponent(InformationTabComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('customer', customer);
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should fill the form with the customer without updating it', async () => {
    await waitForUpdate();
    expect(component.form.name().value()).toEqual('Name');
    expect(component.form.address.city().value()).toEqual('Paris');
    expect(customerServiceSpy.patch).not.toHaveBeenCalled();
  });

  it('should patch the modified fields only', async () => {
    component.form.name().value.set('New name');
    await waitForUpdate();
    expect(customerServiceSpy.patch).toHaveBeenCalledWith({ id: '42', name: 'New name' });

    // The form is reset with the patched customer: no other update
    await waitForUpdate();
    expect(customerServiceSpy.patch).toHaveBeenCalledTimes(1);
    expect(component.form.name().value()).toEqual('New name');
  });

  it('should not patch the customer when the form is invalid', async () => {
    component.form.name().value.set('');
    await waitForUpdate();
    expect(customerServiceSpy.patch).not.toHaveBeenCalled();
  });

  it('should disable the form in read only mode', () => {
    fixture.componentRef.setInput('readOnly', true);
    fixture.detectChanges();
    expect(component.form().disabled()).toBe(true);
  });

  it('should disable the GDPR fields', () => {
    expect(component.form.gdprAlertDelay().disabled()).toBe(false);
    fixture.componentRef.setInput('gdprReadOnlyStatus', true);
    fixture.detectChanges();
    expect(component.form.gdprAlert().disabled()).toBe(true);
    expect(component.form.gdprAlertDelay().disabled()).toBe(true);
  });
});
