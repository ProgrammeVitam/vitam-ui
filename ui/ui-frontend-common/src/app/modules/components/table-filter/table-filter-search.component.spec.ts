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
// tslint:disable:no-magic-numbers
import { Component } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { MatPseudoCheckboxModule } from '@angular/material/core';
import { By } from '@angular/platform-browser';

import { TableFilterOptionComponent } from './table-filter-option/table-filter-option.component';
import { TableFilterSearchComponent } from './table-filter-search.component';
import { TableFilterComponent } from './table-filter.component';

@Component({
  template: `
    <vitamui-common-table-filter-search
      [(filter)]="filter"
      [options]="options"
      (filterClose)="onClose()"
    >
    </vitamui-common-table-filter-search>
  `
})
export class TestHostComponent {
  filter: any;
  options: any = [
    { value: 0, label: '0 - John' },
    { value: 1, label: '1 - Johnny' },
    { value: 2, label: '2 - Sarah' },
    { value: 3, label: '3 - Jacky' },
  ];

  onClose() { }
}

describe('TableFilterSearchComponent', () => {
  let testhost: TestHostComponent;
  let fixture: ComponentFixture<TestHostComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        MatPseudoCheckboxModule,
        FormsModule,
      ],
      declarations: [
        TestHostComponent,
        TableFilterSearchComponent,
        TableFilterComponent,
        TableFilterOptionComponent,
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestHostComponent);
    testhost = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(testhost).toBeTruthy();
  });

  it('should display the list of options', () => {
    const options = fixture.debugElement.queryAll(By.directive(TableFilterOptionComponent));
    expect(options.length).toBe(4);

    expect(options[0].nativeElement.textContent).toContain(testhost.options[0].label);
    expect(options[0].componentInstance.value).toBe(testhost.options[0].value);

    expect(options[1].nativeElement.textContent).toContain(testhost.options[1].label);
    expect(options[1].componentInstance.value).toBe(testhost.options[1].value);

    expect(options[2].nativeElement.textContent).toContain(testhost.options[2].label);
    expect(options[2].componentInstance.value).toBe(testhost.options[2].value);

    expect(options[3].nativeElement.textContent).toContain(testhost.options[3].label);
    expect(options[3].componentInstance.value).toBe(testhost.options[3].value);
  });

  it('should select the values on click', () => {
    const options = fixture.debugElement.queryAll(By.directive(TableFilterOptionComponent));

    expect(testhost.filter).toBeFalsy();

    options[0].triggerEventHandler('click', null);

    fixture.detectChanges();

    expect(testhost.filter).toEqual([0]);
    expect(options[0].componentInstance.selected).toBeTruthy();
  });

  it('should deselect the values on click on a selected option', () => {
    const options = fixture.debugElement.queryAll(By.directive(TableFilterOptionComponent));

    expect(testhost.filter).toBeFalsy();

    options[0].triggerEventHandler('click', null);

    fixture.detectChanges();

    options[0].triggerEventHandler('click', null);

    expect(testhost.filter).toEqual([]);
  });

  it('should only show options containing "Jo"', () => {
    const searchInput = fixture.nativeElement.querySelector('input');

    searchInput.value = 'Jo';
    searchInput.dispatchEvent(new Event('input'));

    fixture.detectChanges();

    const options = fixture.debugElement.queryAll(By.directive(TableFilterOptionComponent));

    expect(options.length).toBe(4);
    expect(options[0].nativeElement.className).not.toContain('hidden');
    expect(options[1].nativeElement.className).not.toContain('hidden');
    expect(options[2].nativeElement.className).toContain('hidden');
    expect(options[3].nativeElement.className).toContain('hidden');
  });

});
