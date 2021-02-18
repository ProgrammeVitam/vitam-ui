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
import {Component, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {ActivatedRoute, Router} from '@angular/router';
import {Event} from 'projects/vitamui-library/src/public-api';
import {GlobalEventService, Option, SearchBarComponent, SidenavPage} from 'ui-frontend-common';

import {AuditCreateComponent} from './audit-create/audit-create.component';
import {AuditListComponent} from './audit-list/audit-list.component';

@Component({
  selector: 'app-audit',
  templateUrl: './audit.component.html',
  styleUrls: ['./audit.component.scss']
})
export class AuditComponent extends SidenavPage<Event> implements OnInit {

  search: string;
  tenantIdentifier: string;

  auditTypes: Option[] = [
    {key: 'PROCESS_AUDIT', label: 'Integrité et Existence'},
    {key: 'EVIDENCE_AUDIT', label: 'Cohérence'},
    {key: 'RECTIFICATION_AUDIT', label: 'Correctif'}
  ];

  dateRangeFilterForm: FormGroup;

  filters: any = {};

  @ViewChild(SearchBarComponent, {static: true}) searchBar: SearchBarComponent;
  @ViewChild(AuditListComponent, {static: true}) auditListComponent: AuditListComponent;

  constructor(
    public dialog: MatDialog,
    private router: Router,
    private route: ActivatedRoute,
    globalEventService: GlobalEventService,
    private formBuilder: FormBuilder) {
    super(route, globalEventService);

    route.params.subscribe(params => {
      this.tenantIdentifier = params.tenantIdentifier;
    });

    this.dateRangeFilterForm = this.formBuilder.group({
      startDate: null,
      endDate: null,
      types: []
    });

    this.dateRangeFilterForm.controls.startDate.valueChanges.subscribe(value => {
      this.filters.startDate = value;
      this.auditListComponent.filters = this.filters;
    });
    this.dateRangeFilterForm.controls.endDate.valueChanges.subscribe((value: Date) => {
      if (value) {
        value.setDate(value.getDate() + 1);
      }
      this.filters.endDate = value;
      this.auditListComponent.filters = this.filters;
    });
    this.dateRangeFilterForm.controls.types.valueChanges.subscribe(value => {
      this.filters.types = value;
      this.auditListComponent.filters = this.filters;
    });
  }

  openCreateAuditDialog() {
    const dialogRef = this.dialog.open(AuditCreateComponent, {panelClass: 'vitamui-modal', disableClose: true});
    dialogRef.componentInstance.tenantIdentifier = +this.tenantIdentifier;
    dialogRef.afterClosed().subscribe((result) => {
      if (result !== undefined && result.success) {
        this.refreshList();
      }
    });
  }

  private refreshList() {
    if (!this.auditListComponent) {
      return;
    }
    this.auditListComponent.searchAuditOrdered();
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

  showAudit(item: Event) {
    this.openPanel(item);
  }

  changeTenant(tenantIdentifier: number) {
    this.router.navigate(['..', tenantIdentifier], {relativeTo: this.route});
  }
}
