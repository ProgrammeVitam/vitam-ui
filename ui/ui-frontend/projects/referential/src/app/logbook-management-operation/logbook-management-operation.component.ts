/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { MatSidenav } from '@angular/material/sidenav';
import { ActivatedRoute } from '@angular/router';
import { AuthService, DateService, FrenchDate, QueryParamsService, Tenant } from 'vitamui-library';
import { OperationDetails } from '../models/operation-response.interface';
import { LogbookManagementOperationListComponent } from './logbook-management-operation-list/logbook-management-operation-list.component';
import { tap } from 'rxjs/operators';
import { BehaviorSubject, Subscription } from 'rxjs';

interface FormData {
  startDateMin?: string;
  startDateMax?: string;
  search?: string;
}

interface OperationSearch {
  id?: string;
  startDateMin?: string;
  startDateMax?: string;
}

@Component({
  selector: 'app-logbook-management-operation',
  templateUrl: './logbook-management-operation.component.html',
  styleUrls: ['./logbook-management-operation.component.scss'],
  standalone: false,
})
export class LogbookManagementOperationComponent implements OnInit, OnDestroy {
  tenantIdentifier: number;
  dateRangeFilterForm = this.formBuilder.group<FormData>({
    startDateMin: null,
    startDateMax: null,
  });
  searchValue: string = null;
  showStartDateMax = false;
  searchCriteria: BehaviorSubject<OperationSearch> = new BehaviorSubject({});
  search$ = this.searchCriteria.asObservable();
  tenant: Tenant;
  openedItem: OperationDetails;
  @ViewChild('panel') panel: MatSidenav;

  @ViewChild(LogbookManagementOperationListComponent, { static: true })
  logbookManagementOperationListComponent: LogbookManagementOperationListComponent;

  private subscriptions = new Subscription();

  constructor(
    private route: ActivatedRoute,
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private queryParamsService: QueryParamsService,
    private dateService: DateService,
  ) {
    if (this.route && this.route.paramMap) {
      this.route.paramMap.subscribe((paramMap) => (this.tenantIdentifier = +paramMap.get('tenantIdentifier')));
      this.tenant = this.authService.getTenantByAppAndIdentifier(this.route.snapshot.data.appId, this.tenantIdentifier);
    }
  }

  ngOnInit() {
    this.subscriptions.add(
      ...['startDateMin', 'startDateMax'].map((field) => {
        return this.dateRangeFilterForm
          .get(field)
          .valueChanges.pipe(tap((value: string) => this.updateDateSearchCriteria(value as FrenchDate, field)))
          .subscribe();
      }),
    );

    this.subscriptions.add(
      this.queryParamsService
        .getQueryParams()
        .pipe(
          tap((queryParams) => {
            const formData: FormData = {
              startDateMin: queryParams?.startDateMin || null,
              startDateMax: queryParams?.startDateMax || null,
            };
            const hasChanged =
              Object.entries(this.dateRangeFilterForm.value).filter(([key, value]) => (formData as any)[key] !== value).length > 0;
            if (hasChanged) {
              this.dateRangeFilterForm.setValue(formData);
            }
            this.searchValue = queryParams.search;
          }),
        )
        .subscribe(),
    );

    this.search$.pipe(tap((operationSearch) => this.updateQueryParams(operationSearch))).subscribe(() => this.search());
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  showIntervalDate(value: boolean) {
    this.showStartDateMax = value;
    if (!value) {
      this.clearDate('startDateMax');
    }
  }

  updateSearchValue(searchValue: string): void {
    const { id, ...rest } = this.searchCriteria.value;
    const nextValue = searchValue?.trim() || null;
    const hasChanged = id !== nextValue;

    if (hasChanged) this.searchCriteria.next({ id: nextValue, ...rest });
  }

  clearDate(date: 'startDateMin' | 'startDateMax') {
    this.dateRangeFilterForm.get(date).setValue(null, { onlySelf: false, emitEvent: true });
  }

  search() {
    const s = Object.entries(this.toFrenchDate(this.searchCriteria.value))
      .filter(([_key, value]) => Boolean(value) && !(typeof value === 'boolean'))
      .reduce((acc, [key, value]) => ({ ...acc, [key]: value }), {});
    this.logbookManagementOperationListComponent.searchOperationsList(s);
  }

  showOperation(item: OperationDetails) {
    this.openPanel(item);
  }

  openPanel(item: OperationDetails) {
    this.openedItem = item;
    if (this.panel && !this.panel.opened) {
      this.panel.open().then();
    }
  }

  closePanel() {
    if (this.panel && this.panel.opened) {
      this.panel.close().then();
    }
  }

  private updateDateSearchCriteria(frenchDate: FrenchDate, field: string): void {
    const isoDate = this.dateService.toIsoDate(frenchDate);
    const hasChanged = (this.searchCriteria.value as any)[field] !== isoDate;

    if (!hasChanged) return;

    const rest = Object.entries(this.searchCriteria.value)
      .filter(([key]) => key !== field)
      .reduce((acc, [key, value]: [key: string, value: any]) => ({ ...acc, [key]: value }), {});

    this.searchCriteria.next({ [field]: this.dateService.toIsoDate(frenchDate), ...rest });
  }

  private updateQueryParams(operationSearch: OperationSearch) {
    const mapping = [
      {
        source: 'id',
        target: 'search',
      },
    ];
    const queryParams: FormData = this.queryParamsService.transform(operationSearch, mapping);

    this.queryParamsService.setQueryParams(queryParams);
  }

  private toFrenchDate(operationSearch: OperationSearch): OperationSearch {
    const { id, ...rest } = operationSearch;

    const restToFrenchDate = Object.entries(rest).map(([key, value]) => {
      return { [key]: this.dateService.toFrenchDate(value) };
    });

    return { id, ...Object.assign({}, ...restToFrenchDate) } as OperationSearch;
  }
}
