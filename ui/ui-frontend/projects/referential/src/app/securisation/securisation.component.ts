import {Component, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {ActivatedRoute, Router} from '@angular/router';
import {Event} from 'projects/vitamui-library/src/public-api';
import {GlobalEventService, Option, SearchBarComponent, SidenavPage} from 'ui-frontend-common';

import {SecurisationListComponent} from './securisation-list/securisation-list.component';

@Component({
  selector: 'app-securisation',
  templateUrl: './securisation.component.html',
  styleUrls: ['./securisation.component.scss']
})
export class SecurisationComponent extends SidenavPage<Event> implements OnInit {

  search: string;
  dateRangeFilterForm: FormGroup;
  filters: any = {};

  traceabilityTypes: Option[] = [
    {key: 'STP_OP_SECURISATION', label: 'Opérations'},
    {key: 'LOGBOOK_UNIT_LFC_TRACEABILITY', label: 'Cycle de vie des unité archivistiques'},
    {key: 'LOGBOOK_OBJECTGROUP_LFC_TRACEABILITY', label: 'Cycle de vie des groupes d\'objets'},
    {key: 'STP_STORAGE_SECURISATION', label: 'Journal des écritures'}
  ];

  @ViewChild(SearchBarComponent, {static: true}) searchBar: SearchBarComponent;
  @ViewChild(SecurisationListComponent, {static: true}) securisationListComponent: SecurisationListComponent;

  constructor(
    public dialog: MatDialog,
    private router: Router,
    private route: ActivatedRoute,
    globalEventService: GlobalEventService,
    private formBuilder: FormBuilder) {
    super(route, globalEventService);

    this.dateRangeFilterForm = this.formBuilder.group({
      startDate: null,
      endDate: null,
      types: []
    });

    this.dateRangeFilterForm.controls.startDate.valueChanges.subscribe(value => {
      this.filters.startDate = value;
      this.securisationListComponent.filters = this.filters;
    });
    this.dateRangeFilterForm.controls.endDate.valueChanges.subscribe(value => {
      if (value) {
        value.setDate(value.getDate() + 1);
      }
      this.filters.endDate = value;
      this.securisationListComponent.filters = this.filters;
    });
    this.dateRangeFilterForm.controls.types.valueChanges.subscribe(value => {
      this.filters.types = value;
      this.securisationListComponent.filters = this.filters;
    });
  }

  onSearchSubmit(search: string) {
    this.search = search || '';
  }

  clearDate(date: 'startDate' | 'endDate') {
    if (date === 'startDate') {
      this.dateRangeFilterForm.get(date).reset(null, {emitEvent: false});
    } else if (date === 'endDate') {
      this.dateRangeFilterForm.get(date).reset(null, {emitEvent: false});
    } else {
      console.error('clearDate() error: unknown date ' + date);
    }
  }

  resetFilters() {
    this.dateRangeFilterForm.reset();
    this.searchBar.reset();
  }

  ngOnInit() {
  }

  showSecurisation(item: Event) {
    this.openPanel(item);
  }

  changeTenant(tenantIdentifier: number) {
    this.router.navigate(['..', tenantIdentifier], { relativeTo: this.route });
  }
}
