import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {debounceTime} from 'rxjs/operators';

@Component({
  // tslint:disable-next-line: component-selector
  selector: 'vitamui-interval-date-picker',
  templateUrl: './vitamui-interval-date-picker.component.html',
  styleUrls: ['./vitamui-interval-date-picker.component.scss']
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

        if (value.dateMin != null) {
          this.searchCriteria.dateMin = VitamuiIntervalDatePickerComponent.fetchDate(value.dateMin);
        }

        if (value.dateMax != null) {
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
