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
import { Component } from '@angular/core';
import { CustomValidators, DatePattern, DatepickerComponent } from 'vitamui-library';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgClass, NgTemplateOutlet } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatNativeDateModule } from '@angular/material/core';

@Component({
  selector: 'design-system-datepicker',
  imports: [ReactiveFormsModule, DatepickerComponent, NgClass, NgTemplateOutlet, MatFormFieldModule, MatNativeDateModule],
  templateUrl: './design-system-datepicker.component.html',
  styleUrl: './design-system-datepicker.component.scss',
})
export class DesignSystemDatepickerComponent {
  datepickerYearEmpty = new FormControl();
  datepickerMonthEmpty = new FormControl();
  datepickerDayEmpty = new FormControl();
  datepickerYear = new FormControl('2022');
  datepickerMonth = new FormControl('2018-05');
  datepickerDay = new FormControl('2022-06-16');

  startDate = new FormControl();
  endDate = new FormControl();

  datepickerEmptyError = (() => {
    const fc = new FormControl(null, Validators.required);
    fc.markAsTouched();
    return fc;
  })();
  datepickerErrorYear = (() => {
    const fc = new FormControl('202255', [Validators.required, CustomValidators.date(DatePattern.YEAR)]);
    fc.markAsTouched();
    return fc;
  })();
  datepickerErrorMonth = (() => {
    const fc = new FormControl('2018-13', [Validators.required, CustomValidators.date(DatePattern.YEAR_MONTH)]);
    fc.markAsTouched();
    return fc;
  })();
  datepickerErrorDay = (() => {
    const fc = new FormControl('2024-02-30', [Validators.required, CustomValidators.date(DatePattern.YEAR_MONTH_DAY)]);
    fc.markAsTouched();
    return fc;
  })();
  datepickerDisabledEmpty = (() => {
    const fc = new FormControl('');
    fc.disable();
    return fc;
  })();
  datepickerDisabledYear = (() => {
    const fc = new FormControl('2022');
    fc.disable();
    return fc;
  })();
  datepickerDisabledMonth = (() => {
    const fc = new FormControl('2019-02');
    fc.disable();
    return fc;
  })();
  datepickerDisabledDay = (() => {
    const fc = new FormControl('2024-01-01');
    fc.disable();
    return fc;
  })();
}
