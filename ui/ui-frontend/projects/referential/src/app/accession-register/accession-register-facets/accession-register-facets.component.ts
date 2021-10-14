import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FacetDetails } from 'ui-frontend-common/app/modules/models/operation/facet-details.interface';
import { AccessionRegistersService } from '../accession-register.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-accession-register-facets',
  templateUrl: './accession-register-facets.component.html',
  styleUrls: ['./accession-register-facets.component.scss'],
})
export class AccessionRegisterFacetsComponent implements OnInit {
  @Output() showAdvancedSearchPanel = new EventEmitter<boolean>();

  stateFacetDetails$: Observable<FacetDetails[]>;

  constructor(public accessionRegistersService: AccessionRegistersService) {}

  ngOnInit(): void {
    this.stateFacetDetails$ = this.accessionRegistersService.getFacetDetailsStats();
  }

  onDateCriteriaChange(dateCriteria: { startDateMin: string; startDateMax: string }) {
    this.accessionRegistersService.notifyDateIntervalChange(dateCriteria);
  }
}
