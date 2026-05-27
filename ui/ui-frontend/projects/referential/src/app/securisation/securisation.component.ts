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
import { Component, ViewChild, inject } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { Event } from '../../../../vitamui-library/src/lib/models/event';
import { GlobalEventService } from '../../../../vitamui-library/src/app/modules/global-event.service';
import { SearchBarComponent } from '../../../../vitamui-library/src/app/modules/components/search-bar/search-bar.component';
import { SidenavPage } from '../../../../vitamui-library/src/app/modules/sidenav-page.class';
import { SecurisationListComponent } from './securisation-list/securisation-list.component';
import { DateTime } from 'luxon';

@Component({
  selector: 'app-securisation',
  templateUrl: './securisation.component.html',
  styleUrls: ['./securisation.component.scss'],
  standalone: false,
})
export class SecurisationComponent extends SidenavPage<Event> {
  dialog = inject(MatDialog);
  route: ActivatedRoute;
  globalEventService: GlobalEventService;
  private formBuilder = inject(FormBuilder);

  search: string;
  dateRangeFilterForm: FormGroup;
  filters: any = {};

  @ViewChild(SearchBarComponent, { static: true }) searchBar: SearchBarComponent;
  @ViewChild(SecurisationListComponent, { static: true }) securisationListComponent: SecurisationListComponent;

  constructor() {
    const route = inject(ActivatedRoute);
    const globalEventService = inject(GlobalEventService);

    super(route, globalEventService);
    this.route = route;
    this.globalEventService = globalEventService;

    this.dateRangeFilterForm = this.formBuilder.group({
      startDate: null,
      endDate: null,
      types: [],
    });

    this.dateRangeFilterForm.controls.startDate.valueChanges.subscribe((value) => {
      this.filters = { ...this.filters, startDate: value ? DateTime.fromJSDate(value).startOf('day').toISO() : null };
    });

    this.dateRangeFilterForm.controls.endDate.valueChanges.subscribe((value) => {
      this.filters = { ...this.filters, endDate: value ? DateTime.fromJSDate(value).endOf('day').toISO() : null };
    });

    this.dateRangeFilterForm.controls.types.valueChanges.subscribe((value) => {
      this.filters.types = value;
      this.securisationListComponent.filters = this.filters;
    });
  }

  public onSearchSubmit(search: string): void {
    this.search = search || '';
  }

  public showSecurisation(item: Event): void {
    this.openPanel(item);
  }
}
