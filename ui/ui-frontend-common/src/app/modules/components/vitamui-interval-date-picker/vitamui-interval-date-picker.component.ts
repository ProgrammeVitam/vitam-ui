import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
import {debounceTime} from "rxjs/operators";

@Component({
  selector: 'vitamui-interval-date-picker',
  templateUrl: './vitamui-interval-date-picker.component.html',
  styleUrls: ['./vitamui-interval-date-picker.component.scss']
})
export class VitamuiIntervalDatePickerComponent implements OnInit {
  @Input() label: string;
  @Output() criteriaChange = new EventEmitter<{ startDateMin: string; startDateMax: string }>();

  dateRangeFilterForm: FormGroup;
  showStartDateMax = false;
  searchCriteria: any = {};

  constructor(private formBuilder: FormBuilder) {}

  ngOnInit(): void {
    this.dateRangeFilterForm = this.formBuilder.group({
      startDateMin: null,
      startDateMax: null,
    });

    this.dateRangeFilterForm.valueChanges.pipe(debounceTime(200)).subscribe((value) => {
      if (value) {
        this.searchCriteria = {
          startDateMin: null,
          startDateMax: null,
        };

        if (value.startDateMin != null) {
          this.searchCriteria.startDateMin = VitamuiIntervalDatePickerComponent.fetchDate(value.startDateMin);
        }

        if (value.startDateMax != null) {
          this.searchCriteria.startDateMax = VitamuiIntervalDatePickerComponent.fetchDate(value.startDateMax);
        }

        this.criteriaChange.emit(this.searchCriteria);
      }
    });
  }

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

  showIntervalDate(value: boolean) {
    this.showStartDateMax = value;
    if (!value) {
      this.clearDate('startDateMax');
    }
  }

  clearDate(date: 'startDateMin' | 'startDateMax') {
    if (date === 'startDateMin') {
      this.dateRangeFilterForm.get(date).reset(null);
      this.searchCriteria.startDateMin = null;
    } else if (date === 'startDateMax') {
      this.dateRangeFilterForm.get(date).reset(null);
      this.searchCriteria.startDateMax = null;
    } else {
      console.error('clearDate() error: unknown date ' + date);
    }
  }
}
