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
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { Event } from 'projects/vitamui-library/src/public-api';
import { GlobalEventService, Option, SearchBarComponent, SidenavPage } from 'ui-frontend-common';

import { SecurisationListComponent } from './securisation-list/securisation-list.component';

@Component({
  selector: 'app-securisation',
  templateUrl: './securisation.component.html',
  styleUrls: ['./securisation.component.scss'],
})
export class SecurisationComponent extends SidenavPage<Event> implements OnInit {
  search: string;
  dateRangeFilterForm: FormGroup;
  filters: any = {};

  traceabilityTypes: Option[] = [
    { key: 'STP_OP_SECURISATION', label: 'Opérations' },
    { key: 'LOGBOOK_UNIT_LFC_TRACEABILITY', label: 'Cycle de vie des unité archivistiques' },
    { key: 'LOGBOOK_OBJECTGROUP_LFC_TRACEABILITY', label: "Cycle de vie des groupes d'objets" },
    { key: 'STP_STORAGE_SECURISATION', label: 'Journal des écritures' },
  ];

  @ViewChild(SearchBarComponent, { static: true }) searchBar: SearchBarComponent;
  @ViewChild(SecurisationListComponent, { static: true }) securisationListComponent: SecurisationListComponent;

  constructor(
    public dialog: MatDialog,
    private router: Router,
    private route: ActivatedRoute,
    globalEventService: GlobalEventService,
    private formBuilder: FormBuilder,
  ) {
    super(route, globalEventService);

    this.dateRangeFilterForm = this.formBuilder.group({
      startDate: null,
      endDate: null,
      types: [],
    });

    this.dateRangeFilterForm.controls.startDate.valueChanges.subscribe((value) => {
      this.filters.startDate = value;
      this.securisationListComponent.filters = this.filters;
    });
    this.dateRangeFilterForm.controls.endDate.valueChanges.subscribe((value) => {
      if (value) {
        value.setDate(value.getDate() + 1);
      }
      this.filters.endDate = value;
      this.securisationListComponent.filters = this.filters;
    });
    this.dateRangeFilterForm.controls.types.valueChanges.subscribe((value) => {
      this.filters.types = value;
      this.securisationListComponent.filters = this.filters;
    });
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

  resetFilters() {
    this.dateRangeFilterForm.reset();
    this.searchBar.reset();
  }

  ngOnInit() {}

  showSecurisation(item: Event) {
    this.openPanel(item);
  }

  changeTenant(tenantIdentifier: number) {
    this.router.navigate(['..', tenantIdentifier], { relativeTo: this.route });
  }
}
