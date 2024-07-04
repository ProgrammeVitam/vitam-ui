import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { Observable } from 'rxjs';
import { FacetDetails, VitamUICommonModule, LogbookOperationFacetComponent } from 'vitamui-library';
import { AccessionRegistersService } from '../accession-register.service';
import { TranslateModule } from '@ngx-translate/core';
import { NgIf, AsyncPipe } from '@angular/common';

@Component({
  selector: 'app-accession-register-facets',
  templateUrl: './accession-register-facets.component.html',
  styleUrls: ['./accession-register-facets.component.scss'],
  standalone: true,
  imports: [NgIf, VitamUICommonModule, LogbookOperationFacetComponent, AsyncPipe, TranslateModule],
})
export class AccessionRegisterFacetsComponent implements OnInit {
  @Output() showAdvancedSearchPanel = new EventEmitter<boolean>();

  stateFacetDetails$: Observable<FacetDetails[]>;
  advancedSearchPanelOpenState$: Observable<boolean>;

  constructor(public accessionRegistersService: AccessionRegistersService) {}

  ngOnInit(): void {
    this.advancedSearchPanelOpenState$ = this.accessionRegistersService.isOpenAdvancedSearchPanel();
    this.stateFacetDetails$ = this.accessionRegistersService.getStats();
  }

  onDateCriteriaChange(dateCriteria: { dateMin: string; dateMax: string }) {
    this.accessionRegistersService.notifyDateIntervalChange({ endDateMin: dateCriteria.dateMin, endDateMax: dateCriteria.dateMax });
  }
}
