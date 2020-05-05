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
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, NgModel } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { input } from '../../../../../testing/src/helpers';
import { VitamUIInputPositiveNumberComponent } from './vitamui-input-positive-number.component';

@Component({
  template: `
    <vitamui-common-input-positive-number [(ngModel)]="value" #input="ngModel"></vitamui-common-input-positive-number>
  `
})
class TesthostComponent {
  @ViewChild(VitamUIInputPositiveNumberComponent, {static: false}) VitamUIInputPositiveNumberComponent: VitamUIInputPositiveNumberComponent;
  @ViewChild('input', {static: false}) ngModel: NgModel;

  value = 'initial value';
}

let testhost: TesthostComponent;
let fixture: ComponentFixture<TesthostComponent>;

describe('VitamUIInputPositiveNumberComponent', () => {

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [FormsModule, NoopAnimationsModule, MatProgressSpinnerModule],
      declarations: [VitamUIInputPositiveNumberComponent, TesthostComponent]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TesthostComponent);
    testhost = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(testhost).toBeTruthy();
  });

  it('should display the value', async(() => {
    testhost.value = 'value to display';
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      fixture.detectChanges();
      expect(testhost.VitamUIInputPositiveNumberComponent.value).toBe('value to display');
    });
  }));

  it('should emit the typed in value', () => {
    const elInput = fixture.nativeElement.querySelector('input');
    input(elInput, 'typed in value');
    fixture.detectChanges();
    expect(testhost.value).toBe('typed in value');
  });

  it('should focus the input on click', () => {
    const elVitamUIInput = fixture.nativeElement.querySelector('vitamui-common-input-positive-number');
    const elInput = fixture.nativeElement.querySelector('input');
    elVitamUIInput.click();
    expect(document.activeElement).toBe(elInput);
  });

  it('should set focus to true', () => {
    testhost.VitamUIInputPositiveNumberComponent.onFocus();
    expect(testhost.VitamUIInputPositiveNumberComponent.focused).toBe(true);
  });

  it('should set focus to false', () => {
    testhost.VitamUIInputPositiveNumberComponent.onFocus();
    testhost.VitamUIInputPositiveNumberComponent.onBlur();
    expect(testhost.VitamUIInputPositiveNumberComponent.focused).toBe(false);
  });

});
