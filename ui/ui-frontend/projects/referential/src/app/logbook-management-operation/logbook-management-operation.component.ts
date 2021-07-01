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

import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatSidenav } from '@angular/material/sidenav';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from 'ui-frontend-common';
import { OperationDetails } from '../models/operation-response.interface';
import { LogbookManagementOperationListComponent } from './logbook-management-operation-list/logbook-management-operation-list.component';

@Component({
  selector: 'app-logbook-management-operation',
  templateUrl: './logbook-management-operation.component.html',
  styleUrls: ['./logbook-management-operation.component.scss'],
})
export class LogbookManagementOperationComponent implements OnInit {
  tenantIdentifier: number;
  dateRangeFilterForm: FormGroup;
  showStartDateMax = false;
  searchCriteria: any = {};
  tenant: any;
  openedItem: OperationDetails;
  @ViewChild('panel') panel: MatSidenav;

  @ViewChild(LogbookManagementOperationListComponent, { static: true })
  logbookManagementOperationListComponent: LogbookManagementOperationListComponent;

  constructor(private formBuilder: FormBuilder, private route: ActivatedRoute, private authService: AuthService) {
    this.dateRangeFilterForm = this.formBuilder.group({
      startDateMin: null,
      startDateMax: null,
    });

    this.dateRangeFilterForm.get('startDateMin').valueChanges.subscribe((value) => {
      if (value) {
        this.searchCriteria.startDateMin =
          this.getDay(new Date(value).getDate()) +
          '/' +
          this.getMonth(new Date(value).getMonth() + 1) +
          '/' +
          new Date(value).getFullYear().toString();
        this.logbookManagementOperationListComponent.searchOperationsList(this.searchCriteria);
      }
    });

    this.dateRangeFilterForm.get('startDateMax').valueChanges.subscribe((value) => {
      if (value) {
        this.searchCriteria.startDateMax =
          this.getDay(new Date(value).getDate()) +
          '/' +
          this.getMonth(new Date(value).getMonth() + 1) +
          '/' +
          new Date(value).getFullYear().toString();
        this.logbookManagementOperationListComponent.searchOperationsList(this.searchCriteria);
      }
    });
    if (this.route && this.route.paramMap) {
      this.route.paramMap.subscribe((paramMap) => (this.tenantIdentifier = +paramMap.get('tenantIdentifier')));
      this.tenant = this.authService.getTenantByAppAndIdentifier(this.route.snapshot.data.appId, this.tenantIdentifier);
    }
  }

  ngOnInit(): void {}

  showIntervalDate(value: boolean) {
    this.showStartDateMax = value;
    if (!value) {
      this.clearDate('startDateMax');
    }
  }

  onSearchSubmit(search: string) {
    this.searchCriteria.id = search;
    this.logbookManagementOperationListComponent.searchOperationsList(this.searchCriteria);
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

    this.logbookManagementOperationListComponent.searchOperationsList(this.searchCriteria);
  }

  refresh() {
    this.logbookManagementOperationListComponent.searchOperationsList(this.searchCriteria);
  }

  private getMonth(num: number): string {
    if (num > 9) {
      return num.toString();
    } else {
      return '0' + num.toString();
    }
  }

  private getDay(day: number): string {
    if (day > 9) {
      return day.toString();
    } else {
      return '0' + day.toString();
    }
  }

  showOperation(item: OperationDetails) {
    this.openPanel(item);
  }

  openPanel(item: OperationDetails) {
    this.openedItem = item;
    if (this.panel && !this.panel.opened) {
      this.panel.open();
    }
  }

  closePanel() {
    if (this.panel && this.panel.opened) {
      this.panel.close();
    }
  }
}
