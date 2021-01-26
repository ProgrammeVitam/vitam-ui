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
import { Component } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

import { WINDOW_LOCATION } from '../../injection-tokens';
import { SlideToggleComponent } from './slide-toggle.component';

@Component({
  template: `<vitamui-common-slide-toggle [(ngModel)]="stateOn" [disabled]="disabled" [required]="required">
  </vitamui-common-slide-toggle>`
})
class TesthostComponent {
  stateOn = false;
  disabled = false;
}

let fixture: ComponentFixture<TesthostComponent>;
let testhost: TesthostComponent;

describe('SlideToggleComponent', () => {

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [FormsModule, HttpClientTestingModule, TranslateModule.forRoot()],
      providers: [
        { provide: WINDOW_LOCATION, useValue: {} },
      ],
      declarations: [ TesthostComponent, SlideToggleComponent ],
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

  it('should be off by default', () => {
    expect(testhost.stateOn).toBeFalsy();
    const elInput = fixture.nativeElement.querySelector('input[type=checkbox]');
    fixture.detectChanges();
    expect(elInput.checked).toBeFalsy();
  });

  it('should be on', () => {
    const elInput = fixture.nativeElement.querySelector('input[type=checkbox]');
    testhost.stateOn = true;
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      fixture.detectChanges();
      expect(elInput.checked).toBeTruthy();
    });
  });

  it('should turn on on click', () => {
    const elInput = fixture.nativeElement.querySelector('input[type=checkbox]');
    elInput.click();
    expect(testhost.stateOn).toBeTruthy();
  });

  it('should turn on and off', () => {
    const elInput = fixture.nativeElement.querySelector('input[type=checkbox]');
    elInput.click();
    expect(testhost.stateOn).toBeTruthy();
    elInput.click();
    expect(testhost.stateOn).toBeFalsy();
  });

  it('should not toggle when disabled', () => {
    testhost.disabled = true;
    fixture.detectChanges();
    const elInput = fixture.nativeElement.querySelector('input[type=checkbox]');
    elInput.click();
    expect(testhost.stateOn).toBeFalsy();
  });
});
