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
import { GlobalEventService, SearchBarComponent, SidenavPage } from 'ui-frontend-common';

import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import {MatDialog} from '@angular/material/dialog';
import { EventFilter } from './event-filter.interface';
import { LogbookOperationListComponent } from './logbook-operation-list/logbook-operation-list.component';

@Component({
  selector: 'app-logbook-operation',
  templateUrl: './logbook-operation.component.html',
  styleUrls: ['./logbook-operation.component.scss']
})
export class LogbookOperationComponent extends SidenavPage<any> implements OnInit {

  search = '';
  dateRangeFilterForm: FormGroup;
  tenantIdentifier: number;
  filters: Readonly<EventFilter> = {};


  @ViewChild(SearchBarComponent, {static: true}) searchBar: SearchBarComponent;
  @ViewChild(LogbookOperationListComponent, { static: true }) list: LogbookOperationListComponent;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    public dialog: MatDialog,
    private formBuilder: FormBuilder,
    globalEventService: GlobalEventService
  ) {
    super(route, globalEventService);

    this.route.paramMap.subscribe((paramMap) => this.tenantIdentifier = + paramMap.get('tenantIdentifier'));

    this.dateRangeFilterForm = this.formBuilder.group({
      startDate: null,
      endDate: null
    });

    this.dateRangeFilterForm.valueChanges.subscribe((value) => {
      this.filters = {
        type: this.filters.type,
        status: this.filters.status,
        dateRange: value
      };
    });
  }

  ngOnInit() {
    if (!this.list) {
      console.error('LogbookOperationComponent Error: no list in the template');
    }
  }

  changeTenant(tenantIdentifier: number) {
    this.router.navigate(['..', tenantIdentifier], { relativeTo: this.route });
  }

  onSearchSubmit(search: string) {
    this.search = search || '';
  }

  clearDate(date: 'startDate' | 'endDate') {
    if (date === 'startDate') {
      this.dateRangeFilterForm.get(date).reset(null, { emitEvent: false });
    } else if (date === 'endDate') {
      this.dateRangeFilterForm.get(date).reset(null, { emitEvent: false });
    } else {
      console.error('clearDate() error: unknown date ' + date);
    }
  }

  refreshList() {
    if (!this.list) {
      return;
    }

    this.list.refreshList();
  }

  resetFilters() {
    this.clearDate('startDate');
    this.clearDate('endDate');
    this.searchBar.reset();
    this.list.resetFilters();
  }
}
