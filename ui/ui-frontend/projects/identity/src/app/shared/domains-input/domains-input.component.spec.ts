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


import { Component, ViewChild } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { of } from 'rxjs';

import { input } from 'ui-frontend-common/testing';
import { CustomerCreateValidators } from '../../customer/customer-create/customer-create.validators';
import { DomainsInputComponent } from './domains-input.component';

@Component({ template: '<app-domains-input [(ngModel)]="domains" [(selected)]="selected"></app-domains-input>'})
export class TestHostComponent {
  @ViewChild(DomainsInputComponent, { static: false }) component: DomainsInputComponent;
  domains: string[];
  selected: string;
}

let testhost: TestHostComponent;
let fixture: ComponentFixture<TestHostComponent>;

describe('DomainsInputComponent', () => {

  beforeEach(waitForAsync(() => {
    const customerCreateValidatorsSpy = jasmine.createSpyObj(
      'CustomerCreateValidators',
      { uniqueDomain: of(null) }
    );

    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        ReactiveFormsModule,
        MatProgressSpinnerModule,
      ],
      declarations: [ TestHostComponent, DomainsInputComponent ],
      providers: [
        { provide: CustomerCreateValidators, useValue: customerCreateValidatorsSpy },
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

  it('should have a list of domains', waitForAsync(() => {
    testhost.domains = [
      'toto.titi',
      'titi.tutu',
      'tata.tete',
    ];
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      fixture.detectChanges();
      expect(testhost.component.domains).toEqual(testhost.domains);
    });
  }));

  it('should add one domain', () => {
    testhost.component.control.setValue('test.com');
    testhost.component.add();
    fixture.detectChanges();
    expect(testhost.domains).toEqual(['test.com']);
  });

  it('should not add a domain already in the list', () => {
    const elInput = fixture.nativeElement.querySelector('input');
    input(elInput, 'test.com');
    testhost.component.add();
    input(elInput, 'toto.co.uk');
    testhost.component.add();
    input(elInput, 'test.com');
    testhost.component.add();
    fixture.detectChanges();
    expect(testhost.domains).toEqual(['test.com', 'toto.co.uk']);
  });

  it('should not add a domain that exists in the DB');

  it('should trim the value', () => {
    const elInput = fixture.nativeElement.querySelector('input');
    input(elInput, '        test.com          ');
    testhost.component.add();
    fixture.detectChanges();
    expect(testhost.domains).toEqual(['test.com']);
  });

  it('should not add an invalid domain', () => {
    const elInput = fixture.nativeElement.querySelector('input');
    input(elInput, 'this is invalid');
    testhost.component.add();
    fixture.detectChanges();
    expect(testhost.domains).toBeUndefined();
  });

  it('should emit the selected value', () => {
    const elInput = fixture.nativeElement.querySelector('input');
    input(elInput, 'test.com');
    expect(testhost.selected).toBeUndefined();
    testhost.component.add();
    expect(testhost.selected).toBe('test.com');
  });

  it('should emit the selected value', waitForAsync(() => {
    testhost.domains = ['test.com', 'toto.co.uk', 'tata.fr'];
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      fixture.detectChanges();
      const chips = fixture.nativeElement.querySelectorAll('.vitamui-chip');
      expect(chips.length).toBe(3);
      chips[1].click();
      expect(testhost.selected).toBe('toto.co.uk');
    });
  }));

  it('should set the selected value', waitForAsync(() => {
    testhost.domains = ['test.com', 'toto.co.uk', 'tata.fr'];
    testhost.selected = 'tata.fr';
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      fixture.detectChanges();
      expect(testhost.component.selected).toBe('tata.fr');
    });
  }));

  it('should disable the add button', () => {
    const elInput = fixture.nativeElement.querySelector('input');
    const elButton = fixture.nativeElement.querySelector('button');
    expect(elButton.attributes.disabled).toBeTruthy();
    input(elInput, 'test.com');
    fixture.detectChanges();
    expect(elButton.attributes.disabled).toBeFalsy();
    input(elInput, 'invalid domain');
    fixture.detectChanges();
    expect(elButton.attributes.disabled).toBeTruthy();
  });

  it('should remove the domain', waitForAsync(() => {
    testhost.domains = ['test.com', 'toto.co.uk', 'tata.fr'];
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      fixture.detectChanges();
      expect(testhost.component.domains).toEqual(testhost.domains);
      testhost.component.remove('tata.fr');
      expect(testhost.component.domains).toEqual(['test.com', 'toto.co.uk']);
    });
  }));

  it('should unset the selected item', waitForAsync(() => {
    testhost.domains = ['test.com', 'toto.co.uk', 'tata.fr'];
    testhost.selected = 'tata.fr';
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      fixture.detectChanges();
      expect(testhost.selected).toBe('tata.fr');
      expect(testhost.component.domains).toEqual(testhost.domains);
      testhost.component.remove('tata.fr');
      expect(testhost.component.domains).toEqual(['test.com', 'toto.co.uk']);
      expect(testhost.selected).toBeFalsy();
    });
  }));
});
