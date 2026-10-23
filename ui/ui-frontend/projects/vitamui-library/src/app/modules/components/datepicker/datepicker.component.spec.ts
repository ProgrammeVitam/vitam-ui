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
import { DatePipe } from '@angular/common';
import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { form, FormField, required } from '@angular/forms/signals';
import { provideNativeDateAdapter } from '@angular/material/core';
import { provideTranslateService } from '@ngx-translate/core';

import { input } from '../../../../../testing/src/helpers';
import { DatepickerComponent } from './datepicker.component';

const providers = [DatePipe, provideNativeDateAdapter(), provideTranslateService()];

describe('DatepickerComponent', () => {
  describe('signal forms', () => {
    @Component({
      template: `<vitamui-datepicker [formField]="testForm.date" format="dd/MM/yyyy" outputType="Date"></vitamui-datepicker>`,
      imports: [DatepickerComponent, FormField],
    })
    class SignalFormHostComponent {
      readonly testForm = form(signal<{ date: Date | null }>({ date: new Date(2024, 0, 31) }), (path) => {
        required(path.date);
      });
    }

    let fixture: ComponentFixture<SignalFormHostComponent>;
    let host: SignalFormHostComponent;
    let textInput: HTMLInputElement;

    beforeEach(async () => {
      await TestBed.configureTestingModule({ imports: [SignalFormHostComponent], providers }).compileComponents();
      fixture = TestBed.createComponent(SignalFormHostComponent);
      host = fixture.componentInstance;
      await fixture.whenStable();
      textInput = fixture.nativeElement.querySelectorAll('input')[1];
    });

    it('should display the initial value of the field', () => {
      expect(textInput.value).toEqual('31/01/2024');
      expect(fixture.nativeElement.querySelector('.required-marker')).toBeTruthy();
    });

    it('should update the field value when a date is typed', () => {
      input(textInput, '15/02/2025');
      expect(host.testForm.date().value()).toEqual(new Date(2025, 1, 15));
    });

    it('should display the value set on the field', async () => {
      host.testForm.date().value.set(new Date(2023, 11, 25));
      await fixture.whenStable();
      expect(textInput.value).toEqual('25/12/2023');
    });
  });

  describe('reactive forms', () => {
    @Component({
      template: `<vitamui-datepicker [formControl]="control" format="dd/MM/yyyy"></vitamui-datepicker>`,
      imports: [DatepickerComponent, ReactiveFormsModule],
    })
    class ReactiveFormHostComponent {
      readonly control = new FormControl('31/01/2024');
    }

    it('should display the value and update the control', async () => {
      await TestBed.configureTestingModule({ imports: [ReactiveFormHostComponent], providers }).compileComponents();
      const fixture = TestBed.createComponent(ReactiveFormHostComponent);
      await fixture.whenStable();
      const textInput: HTMLInputElement = fixture.nativeElement.querySelectorAll('input')[1];

      expect(textInput.value).toEqual('31/01/2024');
      input(textInput, '15/02/2025');
      expect(fixture.componentInstance.control.value).toEqual('15/02/2025');
    });
  });
});
