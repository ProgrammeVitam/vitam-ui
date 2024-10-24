import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { Observable } from 'rxjs';
import { AccessionRegistersService } from '../accession-register.service';

@Component({
  selector: 'app-accession-register-facets',
  templateUrl: './accession-register-facets.component.html',
  styleUrls: ['./accession-register-facets.component.scss'],
})
export class AccessionRegisterFacetsComponent implements OnInit {
  @Output() showAdvancedSearchPanel = new EventEmitter<boolean>();
  advancedSearchPanelOpenState$: Observable<boolean>;

  constructor(public accessionRegistersService: AccessionRegistersService) {}

  ngOnInit(): void {
    this.advancedSearchPanelOpenState$ = this.accessionRegistersService.isOpenAdvancedSearchPanel();
  }

  onDateCriteriaChange(dateCriteria: { dateMin: string; dateMax: string }) {
    this.accessionRegistersService.notifyDateIntervalChange({ endDateMin: dateCriteria.dateMin, endDateMax: dateCriteria.dateMax });
  }
}
