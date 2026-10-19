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

import { GroupValidators } from '../group.validators';
import { UnitsFormComponent } from './units-form.component';

@Component({
  template: `<app-units-form [formField]="hostForm.units" customer="42"></app-units-form>`,
  imports: [UnitsFormComponent, FormField],
})
class TestHostComponent {
  readonly initialUnits = ['b'];
  readonly hostForm = form(signal<{ units: string[] }>({ units: this.initialUnits }));
  readonly unitsForm = viewChild(UnitsFormComponent);
}

describe('UnitsFormComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
      providers: [{ provide: GroupValidators, useValue: { unitExistsRule: vi.fn() } }],
    })
      .overrideComponent(UnitsFormComponent, { set: { template: '' } })
      .compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should add a unit in a new sorted array', async () => {
    const unitsForm = host.unitsForm();
    unitsForm.unitForm.unit().value.set(' a ');
    unitsForm.add();
    await fixture.whenStable();

    expect(host.hostForm.units().value()).toEqual(['a', 'b']);
    expect(host.initialUnits).toEqual(['b']);
    expect(host.hostForm().dirty()).toBe(true);
    expect(unitsForm.unitForm.unit().value()).toEqual('');
  });

  it('should not add a unit already added', () => {
    const unitsForm = host.unitsForm();
    unitsForm.unitForm.unit().value.set('B');
    expect(
      unitsForm.unitForm
        .unit()
        .errors()
        .map((error) => error.kind),
    ).toEqual(['unitAlreadyAdd']);
    unitsForm.add();
    expect(host.hostForm.units().value()).toEqual(['b']);
  });

  it('should remove a unit', async () => {
    host.unitsForm().remove('b');
    await fixture.whenStable();
    expect(host.hostForm.units().value()).toEqual([]);
    expect(host.unitsForm().removedUnits).toEqual(['b']);
  });
});
