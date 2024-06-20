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
import { Component, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { ActivatedRoute } from '@angular/router';
import moment from 'moment';
import { Event, GlobalEventService, SearchBarComponent, SidenavPage } from 'vitamui-library';
import { AuditCreateComponent } from './audit-create/audit-create.component';
import { AuditListComponent } from './audit-list/audit-list.component';

@Component({
  selector: 'app-audit',
  templateUrl: './audit.component.html',
  styleUrls: ['./audit.component.scss'],
})
export class AuditComponent extends SidenavPage<Event> {
  public dateRangeFilterForm: FormGroup;
  public filters: any = {};
  public search: string;
  public tenantIdentifier: string;

  @ViewChild(SearchBarComponent, { static: true }) searchBar: SearchBarComponent;
  @ViewChild(AuditListComponent, { static: true }) auditListComponent: AuditListComponent;

  constructor(
    public dialog: MatDialog,
    public route: ActivatedRoute,
    public globalEventService: GlobalEventService,
    private formBuilder: FormBuilder,
  ) {
    super(route, globalEventService);

    route.params.subscribe((params) => {
      this.tenantIdentifier = params.tenantIdentifier;
    });

    this.dateRangeFilterForm = this.formBuilder.group({
      startDate: null,
      endDate: null,
    });

    this.dateRangeFilterForm.controls.startDate.valueChanges.subscribe((value) => {
      this.filters = { ...this.filters, startDate: value };
    });

    this.dateRangeFilterForm.controls.endDate.valueChanges.subscribe((value: Date) => {
      let updatedDate = value ? moment(value).endOf('day') : null;
      this.filters = { ...this.filters, endDate: updatedDate };
    });
  }

  openCreateAuditDialog() {
    const dialogRef = this.dialog.open(AuditCreateComponent, { panelClass: 'vitamui-modal', disableClose: true });
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

  clearDate(dateToClear: 'startDate' | 'endDate', $event: any, input: HTMLInputElement): void {
    if (!!this.dateRangeFilterForm.get(dateToClear).value) {
      this.dateRangeFilterForm.get(dateToClear).reset();
    }

    input.value = null;
    $event.stopPropagation();
  }

  showAudit(item: Event) {
    this.openPanel(item);
  }
}
