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
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';
import { AuthService, Group } from 'vitamui-library';

import { GroupService } from '../../group.service';
import { GroupValidators } from '../../group.validators';
import { InformationTabComponent } from './information-tab.component';

const group = {
  id: '42',
  identifier: '1',
  customerId: '4242',
  enabled: true,
  name: 'Group',
  description: 'Description',
  level: 'LEVEL',
  usersCount: 2,
  profileIds: [] as string[],
} as unknown as Group;

describe('Group InformationTabComponent', () => {
  let fixture: ComponentFixture<InformationTabComponent>;
  let component: InformationTabComponent;
  let groupServiceSpy: { patch: ReturnType<typeof vi.fn> };

  const waitForUpdate = async () => {
    TestBed.tick();
    await vi.advanceTimersByTimeAsync(500);
  };

  beforeEach(async () => {
    vi.useFakeTimers();
    groupServiceSpy = { patch: vi.fn().mockName('GroupService.patch').mockReturnValue(of({})) };
    await TestBed.configureTestingModule({
      imports: [InformationTabComponent],
      providers: [
        { provide: GroupService, useValue: groupServiceSpy },
        { provide: GroupValidators, useValue: { nameExists: vi.fn().mockReturnValue(() => of(null)) } },
        { provide: AuthService, useValue: { user: { level: '', customerId: '4242' } } },
        { provide: MatDialog, useValue: {} },
      ],
    })
      .overrideComponent(InformationTabComponent, { set: { template: '' } })
      .compileComponents();

    fixture = TestBed.createComponent(InformationTabComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('group', group);
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should set the activation state of the fields from the group', () => {
    expect(component.form.identifier().disabled()).toBe(true);
    expect(component.form.enabled().disabled()).toBe(true);
    expect(component.form.level().disabled()).toBe(false);
    expect(component.form.name().disabled()).toBe(false);
  });

  it('should patch the modified fields once', async () => {
    await waitForUpdate();
    expect(groupServiceSpy.patch).not.toHaveBeenCalled();

    component.form.description().value.set('New description');
    await waitForUpdate();
    expect(groupServiceSpy.patch).toHaveBeenCalledWith({ id: '42', description: 'New description' });

    component.form.name().value.set('New name');
    await waitForUpdate();
    expect(groupServiceSpy.patch).toHaveBeenLastCalledWith({ id: '42', name: 'New name' });
    expect(groupServiceSpy.patch).toHaveBeenCalledTimes(2);
  });
});
