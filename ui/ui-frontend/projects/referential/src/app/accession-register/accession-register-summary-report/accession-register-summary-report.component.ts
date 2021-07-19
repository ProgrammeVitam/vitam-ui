import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { Colors } from 'ui-frontend-common';
import { FacetDetails } from 'ui-frontend-common/app/modules/models/operation/facet-details.interface';
import { AccessionRegisterStats } from '../../../../../vitamui-library/src/lib/models/accession-registers-stats';
import { HttpHeaders } from '@angular/common/http';
import { AccessionRegistersService } from '../accession-register.service';

@Component({
  selector: 'app-accession-register-summary-report',
  templateUrl: './accession-register-summary-report.component.html',
  styleUrls: ['./accession-register-summary-report.component.scss'],
})
export class AccessionRegisterSummaryReportComponent implements OnInit {
  @Output() showAdvancedSearchPanel = new EventEmitter<boolean>();

  statusFacetDetails: FacetDetails[] = [];
  stateFacetDetails: FacetDetails[] = [];
  stateFacetTitle: string;
  statusFacetTitle: string;
  accessionRegisterStats: AccessionRegisterStats;

  constructor(public accessionRegistersService: AccessionRegistersService) {}

  ngOnInit(): void {
    this.accessionRegistersService.getStats(new HttpHeaders({ 'X-Access-Contract-Id': 'ContratTNR' })).subscribe((data) => {
      console.log('accessionRegisterStats', data);
      this.accessionRegisterStats = data;
      this.initializeFacet();
    });
  }

  initializeFacet() {
    //this.stateFacetTitle='titre de la facette';
    //this.statusFacetTitle='statistiques des registres de fonds';

    this.initializeFacetDetails();

    this.stateFacetDetails.push({
      title: "Toutes les opérations d'entrées",
      totalResults: this.accessionRegisterStats.totalUnits,
      clickable: false,
      color: Colors.DEFAULT,
      filter: 'RUNNING',
    });
    this.stateFacetDetails.push({
      title: 'Toutes les unités archivistiques',
      totalResults: this.accessionRegisterStats.totalUnits,
      clickable: false,
      color: Colors.DEFAULT,
      filter: 'RUNNING',
    });
    this.stateFacetDetails.push({
      title: "Tout les groupes d'objets",
      totalResults: this.accessionRegisterStats.totalObjectsGroups,
      clickable: false,
      color: Colors.DEFAULT,
      filter: 'PAUSE',
    });
    this.stateFacetDetails.push({
      title: 'Tout les objets',
      totalResults: this.accessionRegisterStats.totalObjects,
      clickable: false,
      color: Colors.DEFAULT,
      filter: 'COMPLETED',
    });
    this.stateFacetDetails.push({
      title: 'Volumétrie totale',
      totalResults: this.accessionRegisterStats.objectSizes,
      clickable: false,
      color: Colors.DEFAULT,
      filter: 'COMPLETED',
    });
  }

  private initializeFacetDetails() {
    this.stateFacetDetails = [];
    this.statusFacetDetails = [];
  }

  getOperationsByStatus(logbookOperation: any) {
    console.log(logbookOperation);
  }

  onDateCriteriaChange(dateCriteria: { startDateMin: string; showStartDateMax: string }) {
    console.log(dateCriteria);
    // this.logbookManagementOperationListComponent.searchOperationsList(this.searchCriteria);
  }
}
