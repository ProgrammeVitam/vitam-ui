import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { SearchCriteria, SearchCriteriaTypeEnum } from '../models/search.criteria';

@Component({
  selector: 'app-criteria-search',
  templateUrl: './criteria-search.component.html',
  styleUrls: ['./criteria-search.component.scss'],
})
export class CriteriaSearchComponent implements OnInit {
  constructor() {}

  @Input()
  criteriaKey: string;

  @Input()
  criteriaVal: SearchCriteria;

  @Output() criteriaRemoveEvent: EventEmitter<any> = new EventEmitter();

  ngOnInit(): void {}

  removeCriteria(keyElt: string, valueElt: string) {
    this.criteriaRemoveEvent.emit({ keyElt, valueElt });
  }

  getCategoryName(categoryEnum: SearchCriteriaTypeEnum): string {
    return SearchCriteriaTypeEnum[categoryEnum];
  }
}
