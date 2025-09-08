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
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { debounceTime } from 'rxjs/operators';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'vitamui-interval-date-picker',
  templateUrl: './vitamui-interval-date-picker.component.html',
  styleUrls: ['./vitamui-interval-date-picker.component.scss'],
  standalone: false,
})
export class VitamuiIntervalDatePickerComponent implements OnInit {
  constructor(private formBuilder: FormBuilder) {}
  @Input() label: string;
  @Output() criteriaChange = new EventEmitter<{ dateMin: string; dateMax: string }>();

  dateRangeFilterForm: FormGroup;
  showDateMax = false;
  searchCriteria: any = {};

  private static fetchDate(boundedDate: string) {
    return (
      VitamuiIntervalDatePickerComponent.getDay(new Date(boundedDate).getDate()) +
      '/' +
      VitamuiIntervalDatePickerComponent.getMonth(new Date(boundedDate).getMonth() + 1) +
      '/' +
      new Date(boundedDate).getFullYear().toString()
    );
  }

  private static getMonth(num: number): string {
    if (num > 9) {
      return num.toString();
    } else {
      return '0' + num.toString();
    }
  }

  private static getDay(day: number): string {
    if (day > 9) {
      return day.toString();
    } else {
      return '0' + day.toString();
    }
  }

  ngOnInit(): void {
    this.dateRangeFilterForm = this.formBuilder.group({
      dateMin: null,
      dateMax: null,
    });

    this.dateRangeFilterForm.valueChanges.pipe(debounceTime(200)).subscribe((value) => {
      if (value) {
        this.searchCriteria = {
          dateMin: null,
          dateMax: null,
        };

        if (value.dateMin) {
          this.searchCriteria.dateMin = VitamuiIntervalDatePickerComponent.fetchDate(value.dateMin);
        }

        if (value.dateMax) {
          this.searchCriteria.dateMax = VitamuiIntervalDatePickerComponent.fetchDate(value.dateMax);
        }

        this.criteriaChange.emit(this.searchCriteria);
      }
    });
  }

  showIntervalDate(value: boolean) {
    this.showDateMax = value;
    if (!value) {
      this.clearDate('dateMin');
      this.clearDate('dateMax');
    }
  }

  clearDate(date: 'dateMin' | 'dateMax') {
    if (date === 'dateMin') {
      this.dateRangeFilterForm.get(date).reset(null);
      this.searchCriteria.dateMin = null;
    } else if (date === 'dateMax') {
      this.dateRangeFilterForm.get(date).reset(null);
      this.searchCriteria.dateMax = null;
    } else {
      console.error('clearDate() error: unknown date ' + date);
    }
  }
}
