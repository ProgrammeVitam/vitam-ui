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
import { AfterViewInit, Component, inject, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import {
  DatepickerComponent,
  GlobalEventService,
  SidenavPage,
  VitamuiBannerComponent,
  VitamuiTitleBreadcrumbComponent,
} from 'vitamui-library';
import { EventFilter } from './event-filter.interface';
import { LogbookOperationListComponent } from './logbook-operation-list/logbook-operation-list.component';
import { MatSidenav, MatSidenavContainer, MatSidenavContent } from '@angular/material/sidenav';
import { LogbookOperationDetailComponent } from './logbook-operation-detail/logbook-operation-detail.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-logbook-operation',
  templateUrl: './logbook-operation.component.html',
  styleUrls: ['./logbook-operation.component.scss'],
  imports: [
    MatSidenavContainer,
    MatSidenav,
    LogbookOperationDetailComponent,
    MatSidenavContent,
    VitamuiTitleBreadcrumbComponent,
    VitamuiBannerComponent,
    ReactiveFormsModule,
    DatepickerComponent,
    LogbookOperationListComponent,
    TranslatePipe,
  ],
})
export class LogbookOperationComponent extends SidenavPage<any> implements OnInit, AfterViewInit {
  route: ActivatedRoute;
  dialog = inject(MatDialog);
  private formBuilder = inject(FormBuilder);

  @ViewChild(LogbookOperationListComponent, { static: true }) list: LogbookOperationListComponent;
  @ViewChild(VitamuiBannerComponent, { static: true }) bannerComponent: VitamuiBannerComponent;

  public search = '';
  public tenantIdentifier: number;
  public dateRangeFilterForm: FormGroup;
  public filters: Readonly<EventFilter> = {};
  private openOperationDetailAfterLoading: boolean;

  constructor() {
    const route = inject(ActivatedRoute);
    const globalEventService = inject(GlobalEventService);

    super(route, globalEventService);

    this.route = route;
  }

  ngOnInit() {
    this.route.paramMap.subscribe((paramMap) => (this.tenantIdentifier = +paramMap.get('tenantIdentifier')));

    this.dateRangeFilterForm = this.formBuilder.group({
      startDate: null,
      endDate: null,
    });

    this.dateRangeFilterForm.valueChanges.subscribe((value) => {
      this.filters = {
        type: this.filters.type,
        status: this.filters.status,
        dateRange: {
          startDate: value.startDate ?? null,
          endDate: value.endDate ?? null,
        },
      };
    });
    this.route.queryParams.subscribe((params) => {
      if (params['guid']) {
        this.onSearchSubmit(params['guid']);
        this.openOperationDetailAfterLoading = true;
      }
    });
  }

  override ngAfterViewInit() {
    this.route.queryParams.subscribe((params) => {
      if (params['guid']) {
        this.bannerComponent.setValue(params['guid']);
      }
    });
  }

  public refreshList(): void {
    this.list.refreshList();
  }

  public onSearchSubmit(search: string): void {
    this.search = search || '';
  }

  private openOperationDetail(): void {
    this.list.eventClick.emit(this.list.dataSource[0]);
  }

  onFinishedLoading() {
    if (this.openOperationDetailAfterLoading) {
      this.openOperationDetail();
      this.openOperationDetailAfterLoading = false;
    }
  }
}
