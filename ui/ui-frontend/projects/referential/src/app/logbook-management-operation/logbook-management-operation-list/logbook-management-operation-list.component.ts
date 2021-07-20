/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2021)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
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
  page: number;
  elementInPage: number;
  filter = false;
  operationsList: OperationsResults;
  results: OperationDetails[];
  filtredByIdentifier = false;
  filtredByStatus = false;
  filtredByDate = false;
  hasNext = false;
  hasPrevious = false;
  filterMap: { [key: string]: any[] } = {
    categories: [],
  };
  resultsFiltred: OperationDetails[];
  totalResults: number;
  statusFacetDetails: FacetDetails[] = [];
  stateFacetDetails: FacetDetails[] = [];
  stateFacetTitle: string;
  statusFacetTitle: string;

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

  constructor(public logbookManagementOperationService: LogbookManagementOperationService, private translate: TranslateService) {}

  searchOperationsList(searchCriteria: any) {
    this.filterMap.categories = [];
    this.initializeParameters(this.filter);
    this.logbookManagementOperationService.listOperationsDetails(searchCriteria).subscribe((data) => {
      this.operationsList = data;
      this.initializeFacet();
      this.filter = false;
      this.results = this.operationsList.results.slice(0, this.elementInPage);
      this.totalResults = this.operationsList.hits.total;
      this.hasNext = this.totalResults > 10 ? true : false;
      this.hasPrevious = false;
    });
  }

  initializeFacet() {
    this.stateFacetTitle = this.translate.instant('LOGBOOK_OPERATION_LIST.STATE');
    this.statusFacetTitle = this.translate.instant('LOGBOOK_OPERATION_LIST.STATUS');

    this.initializeFacetDetails();
    this.stateFacetDetails.push({
      title: this.translate.instant('LOGBOOK_OPERATION_LIST.RESULT_STATE.IN_PROGRESS'),
      totalResults: this.getTotalResultsByState(this.operationsList, 'RUNNING'),
      clickable: true,
      color: Colors.DEFAULT,
      filter: 'RUNNING',
    });
    this.stateFacetDetails.push({
      title: this.translate.instant('LOGBOOK_OPERATION_LIST.RESULT_STATE.BREAK'),
      totalResults: this.getTotalResultsByState(this.operationsList, 'PAUSE'),
      clickable: true,
      color: Colors.DEFAULT,
      filter: 'PAUSE',
    });
    this.stateFacetDetails.push({
      title: this.translate.instant('LOGBOOK_OPERATION_LIST.RESULT_STATE.FINISHED'),
      totalResults: this.getTotalResultsByState(this.operationsList, 'COMPLETED'),
      clickable: true,
      color: Colors.DEFAULT,
      filter: 'COMPLETED',
    });

    this.statusFacetDetails.push({
      title: this.translate.instant('LOGBOOK_OPERATION_LIST.RESULT_STATUS.SUCCESS'),
      totalResults: this.getTotalResultsByStatus(this.operationsList, 'OK'),
      clickable: true,
      color: Colors.OK_COLOR,
      filter: 'OK',
    });
    this.statusFacetDetails.push({
      title: this.translate.instant('LOGBOOK_OPERATION_LIST.RESULT_STATUS.WARNING'),
      totalResults: this.getTotalResultsByStatus(this.operationsList, 'WARNING'),
      clickable: true,
      color: Colors.WARNING_COLOR,
      filter: 'WARNING',
    });
    this.statusFacetDetails.push({
      title: this.translate.instant('LOGBOOK_OPERATION_LIST.RESULT_STATUS.ERROR'),
      totalResults: this.getTotalResultsByStatus(this.operationsList, 'KO'),
      clickable: true,
      color: Colors.KO_COLOR,
      filter: 'KO',
    });
    this.statusFacetDetails.push({
      title: this.translate.instant('LOGBOOK_OPERATION_LIST.RESULT_STATUS.FATAL'),
      totalResults: this.getTotalResultsByStatus(this.operationsList, 'FATAL'),
      clickable: true,
      color: Colors.FATAL_COLOR,
      filter: 'FATAL',
    });
  }

  ngOnInit() {
    this.searchOperationsList({});
  }

  filterByOerationCategory() {
    this.initializeParameters(true);
    this.resultsFiltred = this.operationsList.results.filter((x) => this.filterMap.categories.includes(x.processType));
    if (this.filterMap.categories.length === 0) {
      this.resultsFiltred = this.operationsList.results;
    }
    this.results = this.resultsFiltred.slice(0, 10);
    this.hasNext = this.resultsFiltred.length > 10 ? true : false;
  }

  next() {
    this.hasPrevious = true;
    if (this.filter) {
      this.results = this.sliceResults(this.resultsFiltred, this.elementInPage, this.page);
      this.page++;
    } else {
      this.results = this.sliceResults(this.operationsList.results, this.elementInPage, this.page);
      this.page++;
    }
    if (this.results.length < 10) {
      this.hasNext = false;
    }
  }

  previous() {
    this.hasNext = true;
    if (this.filter) {
      this.results = this.sliceResults(this.resultsFiltred, this.elementInPage, this.page - 2);
      this.page--;
    } else {
      this.results = this.sliceResults(this.operationsList.results, this.elementInPage, this.page - 2);

      this.page--;
    }
    if (this.page === 1) {
      this.hasPrevious = false;
    }
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
    this.results = resultsToShow.slice(0, 10);
    this.hasNext = this.results.length < 10 ? false : true;
  }

  getTotalResultsByState(operationsList: OperationsResults, state: string): number {
    if (operationsList && operationsList.results) {
      return operationsList.results.filter((element) => element.globalState === state).length;
    }
  }

  getTotalResultsByStatus(operationsList: OperationsResults, status: string) {
    if (operationsList && operationsList.results) {
      return operationsList.results.filter((element) => element.stepStatus === status).length;
    }
  }

  operationStatus(operation: OperationDetails): string {
    return operation.stepStatus;
  }
  getProcessTypeByValue(value: string) {
    return this.OperationCategories.find((category) => category.value === value).key;
  }

  getOperationsByGlobalState(state: string) {
    this.filterMap.categories = [];
    this.initializeParameters(true);
    this.resultsFiltred = this.operationsList.results.filter((element) => element.globalState === state);
    this.results = this.resultsFiltred.slice(0, 10);
    this.hasNext = this.results.length < 10 ? false : true;
  }

  getOperationsByStatus(status: string) {
    this.filterMap.categories = [];
    this.initializeParameters(true);
    this.resultsFiltred = this.operationsList.results.filter((element) => element.stepStatus === status);
    this.results = this.resultsFiltred.slice(0, 10);
    this.hasNext = this.results.length < 10 ? false : true;
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
    this.page = 1;
    this.elementInPage = 10;
    this.hasPrevious = false;
  }

  private sliceResults(sourceResults: OperationDetails[], elementInPage: number, page: number): OperationDetails[] {
    return sourceResults.slice(elementInPage * page, (page + 1) * elementInPage);
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
}
