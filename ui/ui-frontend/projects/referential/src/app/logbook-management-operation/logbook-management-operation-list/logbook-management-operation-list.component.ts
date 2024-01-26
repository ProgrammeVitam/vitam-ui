/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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

import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { Colors } from 'ui-frontend-common';
import { FacetDetails } from 'ui-frontend-common/app/modules/models/operation/facet-details.interface';
import { OperationCategory, OperationDetails, OperationsResults } from '../../models/operation-response.interface';
import { LogbookManagementOperationService } from '../logbook-management-operation.service';

@Component({
  selector: 'app-logbook-management-operation-list',
  templateUrl: './logbook-management-operation-list.component.html',
  styleUrls: ['./logbook-management-operation-list.component.scss'],
})
export class LogbookManagementOperationListComponent implements OnInit {
  elementInPage: number;
  filter = false;
  operationsList: OperationsResults;
  results: OperationDetails[];
  filtredByIdentifier = false;
  filtredByStatus = false;
  filtredByDate = false;
  filterMap: { [key: string]: any[] } = {
    categories: [],
  };
  resultsFiltred: OperationDetails[];
  totalResults: number;
  statusFacetDetails: FacetDetails[] = [];
  stateFacetDetails: FacetDetails[] = [];
  stateFacetTitle: string;
  statusFacetTitle: string;
  resultShowed: number;
  show = false;
  updatedoperationSub: Subscription;

  @Output() operationClick = new EventEmitter<OperationDetails>();

  OperationCategories: OperationCategory[] = [
    { key: 'TRACEABILITY', value: 'Sécurisation' },
    { key: 'INGEST', value: 'Entrée' },
    { key: 'STORAGE_BACKUP', value: 'Sauvegarde des journaux des écritures' },
    { key: 'AUDIT', value: 'Audit' },
    { key: 'MASTERDATA', value: 'Données de base' },
    { key: 'ELIMINATION', value: 'Élimination des unités archivistiques' },
    { key: 'CHECK', value: 'Vérification des journaux sécurisés' },
    { key: 'UPDATE', value: 'Mise à jour' },
    { key: 'EXPORT_DIP', value: 'Export du DIP' },
    { key: 'RECLASSIFICATION', value: 'Modification d’arborescence des unités archivistiques' },
    { key: 'PRESERVATION', value: 'Processus global de préservation' },
    { key: 'EXTERNAL_LOGBOOK', value: 'Journalisation d’événements externes' },
  ];

  constructor(
    public logbookManagementOperationService: LogbookManagementOperationService,
    private translate: TranslateService,
  ) {}

  searchOperationsList(searchCriteria: any) {
    this.filterMap.categories = [];
    this.initializeParameters(this.filter);
    this.logbookManagementOperationService.listOperationsDetails(searchCriteria).subscribe((data) => {
      this.operationsList = data;
      this.initializeFacet();
      this.filter = false;
      this.results = this.operationsList.results.slice(0, this.elementInPage);
      this.totalResults = this.operationsList.hits.total;
      this.show = this.totalResults < 20 ? true : false;
    });
  }

  initializeFacet() {
    this.stateFacetTitle = this.translate.instant('LOGBOOK_OPERATION_LIST.STATE');
    this.statusFacetTitle = this.translate.instant('LOGBOOK_OPERATION_LIST.STATUS');

    this.initializeFacetDetails();
    this.stateFacetDetails.push({
      title: this.translate.instant('LOGBOOK_OPERATION_LIST.RESULT_STATE.IN_PROGRESS'),
      totalResults: this.getTotalResultsByState(this.operationsList, 'RUNNING').toString(),
      clickable: true,
      color: Colors.DEFAULT,
      filter: 'RUNNING',
    });
    this.stateFacetDetails.push({
      title: this.translate.instant('LOGBOOK_OPERATION_LIST.RESULT_STATE.BREAK'),
      totalResults: this.getTotalResultsByState(this.operationsList, 'PAUSE').toString(),
      clickable: true,
      color: Colors.DEFAULT,
      filter: 'PAUSE',
    });
    this.stateFacetDetails.push({
      title: this.translate.instant('LOGBOOK_OPERATION_LIST.RESULT_STATE.FINISHED'),
      totalResults: this.getTotalResultsByState(this.operationsList, 'COMPLETED').toString(),
      clickable: true,
      color: Colors.DEFAULT,
      filter: 'COMPLETED',
    });

    this.statusFacetDetails.push({
      title: this.translate.instant('LOGBOOK_OPERATION_LIST.RESULT_STATUS.SUCCESS'),
      totalResults: this.getTotalResultsByStatus(this.operationsList, 'OK').toString(),
      clickable: true,
      color: Colors.OK_COLOR,
      filter: 'OK',
    });
    this.statusFacetDetails.push({
      title: this.translate.instant('LOGBOOK_OPERATION_LIST.RESULT_STATUS.WARNING'),
      totalResults: this.getTotalResultsByStatus(this.operationsList, 'WARNING').toString(),
      clickable: true,
      color: Colors.WARNING_COLOR,
      filter: 'WARNING',
    });
    this.statusFacetDetails.push({
      title: this.translate.instant('LOGBOOK_OPERATION_LIST.RESULT_STATUS.ERROR'),
      totalResults: this.getTotalResultsByStatus(this.operationsList, 'KO').toString(),
      clickable: true,
      color: Colors.KO_COLOR,
      filter: 'KO',
    });
    this.statusFacetDetails.push({
      title: this.translate.instant('LOGBOOK_OPERATION_LIST.RESULT_STATUS.FATAL'),
      totalResults: this.getTotalResultsByStatus(this.operationsList, 'FATAL').toString(),
      clickable: true,
      color: Colors.FATAL_COLOR,
      filter: 'FATAL',
    });
  }

  ngOnInit() {
    this.searchOperationsList({});

    if (this.logbookManagementOperationService.operationUpdated) {
      this.updatedoperationSub = this.logbookManagementOperationService.operationUpdated.subscribe((operationUpdated: OperationDetails) => {
        const operationIndex = this.results.findIndex((operation) => operationUpdated.operationId === operation.operationId);
        if (operationIndex > -1) {
          this.logbookManagementOperationService.listOperationsDetails({ id: operationUpdated.operationId }).subscribe((results) => {
            if (results.results) {
              this.results[operationIndex] = results.results[0];
            }
          });
        }
      });
    }
  }

  filterByOerationCategory() {
    window.scroll(0, 20);
    this.initializeParameters(true);
    this.resultsFiltred = this.operationsList.results.filter((operation) => this.filterMap.categories.includes(operation.processType));
    if (this.filterMap.categories.length === 0) {
      this.resultsFiltred = this.operationsList.results;
    }
    this.totalResults = this.resultsFiltred.length;
    this.results = this.resultsFiltred.slice(0, 20);
    this.show = this.resultsFiltred.length < 20 ? true : false;
  }

  orderByParam(property: any, filtredElement: string) {
    this.initializeParameters(this.filter);
    const resultsToShow: any[] = this.filter ? this.resultsFiltred : this.operationsList.results;
    if (!this.getParamShow(filtredElement)) {
      resultsToShow.sort((a, b) => (a[property] > b[property] ? 1 : -1));
      this.changeParamShow(filtredElement, true);
    } else {
      resultsToShow.sort((a, b) => (a[property] < b[property] ? 1 : -1));
      this.changeParamShow(filtredElement, false);
    }
    this.results = resultsToShow.slice(0, 20);
    this.show = this.results.length < 20 ? true : false;
  }

  getOperationsByGlobalState(state: string) {
    window.scroll(0, 20);
    this.filterMap.categories = [];
    this.initializeParameters(true);
    this.resultsFiltred = this.operationsList.results.filter((element) => element.globalState === state);
    this.totalResults = this.resultsFiltred.length;
    this.results = this.resultsFiltred.slice(0, 20);
    this.show = this.results.length < 20 ? true : false;
  }

  getOperationsByStatus(status: string) {
    window.scroll(0, 20);
    this.filterMap.categories = [];
    this.initializeParameters(true);
    this.resultsFiltred = this.operationsList.results.filter((element) => element.stepStatus === status);
    this.totalResults = this.resultsFiltred.length;
    this.results = this.resultsFiltred.slice(0, 20);
    this.show = this.results.length < 20 ? true : false;
  }

  getTotalResultsByStatus(operationsList: OperationsResults, status: string): number {
    if (operationsList && operationsList.results) {
      return operationsList.results.filter((element) => element.stepStatus === status).length;
    }
  }

  getTotalResultsByState(operationsList: OperationsResults, state: string): number {
    if (operationsList && operationsList.results) {
      return operationsList.results.filter((element) => element.globalState === state).length;
    }
  }

  operationStatus(operation: OperationDetails): string {
    return operation.stepStatus;
  }
  getProcessTypeByValue(value: string) {
    return this.OperationCategories.find((category) => category.value === value).key;
  }

  onScroll() {
    this.loadMore();
  }

  loadMore() {
    if (this.filter) {
      this.scrollAction(this.resultsFiltred);
    } else {
      this.scrollAction(this.operationsList.results);
    }
  }

  private changeParamShow(parameter: string, change: boolean) {
    if (parameter === 'Date') {
      this.filtredByDate = change;
    }
    if (parameter === 'Identifier') {
      this.filtredByIdentifier = change;
    }
    if (parameter === 'Status') {
      this.filtredByStatus = change;
    }
  }

  private initializeParameters(filter: boolean) {
    this.filter = filter;
    this.elementInPage = 20;
  }

  private getParamShow(parameter: string): boolean {
    if (parameter === 'Date') {
      return this.filtredByDate;
    }
    if (parameter === 'Identifier') {
      return this.filtredByIdentifier;
    }
    if (parameter === 'Status') {
      return this.filtredByStatus;
    }
  }

  private initializeFacetDetails() {
    this.stateFacetDetails = [];
    this.statusFacetDetails = [];
  }

  private scrollAction(operationsList: OperationDetails[]) {
    if (this.elementInPage < operationsList.length) {
      this.elementInPage = this.elementInPage + 20;
      this.results = operationsList.slice(0, this.elementInPage);
    } else {
      this.show = true;
    }
  }

  orderByStatus() {
    this.initializeParameters(this.filter);
    const resultsToShow: any[] = this.filter ? this.resultsFiltred : this.operationsList.results;
    if (!this.getParamShow('Status')) {
      resultsToShow.sort((a, b) =>
        this.translate.instant('STATUS_VALUE.' + a.stepStatus) > this.translate.instant('STATUS_VALUE.' + b.stepStatus) ? 1 : -1,
      );
      this.changeParamShow('Status', true);
    } else {
      resultsToShow.sort((a, b) =>
        this.translate.instant('STATUS_VALUE.' + a.stepStatus) < this.translate.instant('STATUS_VALUE.' + b.stepStatus) ? 1 : -1,
      );
      this.changeParamShow('Status', false);
    }

    this.results = resultsToShow.slice(0, 20);
    this.show = this.results.length < 20 ? true : false;
  }
}
