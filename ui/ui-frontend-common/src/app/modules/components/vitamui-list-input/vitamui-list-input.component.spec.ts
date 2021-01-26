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
/* tslint:disable: no-magic-numbers */

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Component, ViewChild } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';

import { input } from '../../../../../testing/src';
import { WINDOW_LOCATION } from '../../injection-tokens';
import { VitamUIListInputComponent } from './vitamui-list-input.component';

@Component({ template: '<vitamui-common-list-input [(ngModel)]="values" [validator]="validators"></vitamui-common-list-input>'})
class TestHostComponent {
  @ViewChild(VitamUIListInputComponent) component: VitamUIListInputComponent;
  values: string[];
  validators = Validators.maxLength(10);
}

let testhost: TestHostComponent;
let fixture: ComponentFixture<TestHostComponent>;

describe('VitamUIListInputComponent', () => {

  beforeEach(waitForAsync(() => {

    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        ReactiveFormsModule,
        MatProgressSpinnerModule,
        HttpClientTestingModule,
        TranslateModule.forRoot(),
      ],
      declarations: [ TestHostComponent, VitamUIListInputComponent ],
      providers: [
        { provide: WINDOW_LOCATION, useValue: {} },
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
    testhost.values = [
      'toto.titi',
      'titi.tutu',
      'tata.tete',
    ];
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      fixture.detectChanges();
      expect(testhost.component.values).toEqual(testhost.values);
    });
  }));

  it('should add one value', () => {
    testhost.component.control.setValue('test.com');
    testhost.component.add();
    fixture.detectChanges();
    expect(testhost.values).toEqual(['test.com']);
  });

  it('should not add a value already in the list', () => {
    const elInput = fixture.nativeElement.querySelector('input');
    input(elInput, 'test.com');
    testhost.component.add();
    input(elInput, 'toto.co.uk');
    testhost.component.add();
    input(elInput, 'test.com');
    testhost.component.add();
    fixture.detectChanges();
    expect(testhost.values).toEqual(['test.com', 'toto.co.uk']);
  });

  it('should trim the value', () => {
    const elInput = fixture.nativeElement.querySelector('input');
    input(elInput, '  test  ');
    testhost.component.add();
    fixture.detectChanges();
    expect(testhost.values).toEqual(['test']);
  });

  it('should not add an invalid domain', () => {
    const elInput = fixture.nativeElement.querySelector('input');
    input(elInput, 'this is invalid');
    testhost.component.add();
    fixture.detectChanges();
    expect(testhost.values).toBeUndefined();
  });

  it('should disable the add button', () => {
    const elInput = fixture.nativeElement.querySelector('input');
    const elButton = fixture.nativeElement.querySelector('button');
    expect(elButton.attributes.disabled).toBeTruthy('should be disabled by default');
    input(elInput, 'test.com');
    fixture.detectChanges();
    expect(elButton.attributes.disabled).toBeFalsy();
    input(elInput, 'invalid value more than 10 characters');
    fixture.detectChanges();
    expect(elButton.attributes.disabled).toBeTruthy();
  });

  it('should remove the value', waitForAsync(() => {
    testhost.values = ['test.com', 'toto.co.uk', 'tata.fr'];
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      fixture.detectChanges();
      expect(testhost.component.values).toEqual(testhost.values);
      testhost.component.remove('tata.fr');
      expect(testhost.component.values).toEqual(['test.com', 'toto.co.uk']);
    });
  }));

});
