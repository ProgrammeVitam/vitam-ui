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
import { Component, OnInit, ViewChild, HostListener } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { IngestListComponent } from './ingest-list/ingest-list.component';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';

import { GlobalEventService, SidenavPage, SearchBarComponent, AdminUserProfile, Direction } from 'ui-frontend-common';
import { UploadComponent } from '../core/common/upload.component';
import { UploadService } from '../core/common/upload.service';
import { IngestList } from '../core/common/ingest-list';


@Component({
  selector: 'app-ingest',
  templateUrl: './ingest.component.html',
  styleUrls: ['./ingest.component.scss']
})
export class IngestComponent extends SidenavPage<any> implements OnInit {
  search: string;
  tenantIdentifier: string;
  guard = true;
  connectedUserInfo: AdminUserProfile;
  dateRangeFilterForm: FormGroup;
  inProgress = false;
  filters: any = {};
  ingestList: IngestList = new IngestList();

  @ViewChild(SearchBarComponent, {static: true}) searchBar: SearchBarComponent;
  @ViewChild(IngestListComponent, {static: true}) ingestListComponent: IngestListComponent;

  constructor( private router: Router, private route: ActivatedRoute,
               globalEventService: GlobalEventService, public dialog: MatDialog, private formBuilder: FormBuilder,
               private uploadSipService: UploadService) {
    super(route, globalEventService);

    route.params.subscribe(params => {
      this.tenantIdentifier = params.tenantIdentifier;
    });

    this.dateRangeFilterForm = this.formBuilder.group({
      startDate: null,
      endDate: null
    });
    this.dateRangeFilterForm.controls.startDate.valueChanges.subscribe(value => {
      this.filters.startDate = value;
      this.ingestListComponent.filters = this.filters;
    });
    this.dateRangeFilterForm.controls.endDate.valueChanges.subscribe((value: Date) => {
      if (value) {
        value.setDate(value.getDate());
      }
      this.filters.endDate = value;
      this.ingestListComponent.filters = this.filters;
      this.ingestListComponent.direction = Direction.DESCENDANT;
    });
  }

  onSearchSubmit(search: string) {
    this.search = search || '';
  }

  clearDate(date: 'startDate' | 'endDate') {
    if (date === 'startDate') {
      this.dateRangeFilterForm.get(date).reset(null, {emitEvent: false});
      this.filters.startDate = null;
    } else if (date === 'endDate') {
      this.dateRangeFilterForm.get(date).reset(null, {emitEvent: false});
      this.filters.endDate = null;
    } else {
      console.error('clearDate() error: unknown date ' + date);
    }
  }

  resetFilters() {
    this.dateRangeFilterForm.reset();
    this.searchBar.reset();
  }

  @HostListener('window:beforeunload', ['$event'])
  beforeunloadHandler(event: any) {
    if (this.ingestList.wipNumber > 0) {
      event.preventDefault();
      console.log('ingest wip = ', this.ingestList.wipNumber);
      event.returnValue = 'Unsaved changes';
      console.log('before check');
      return 'Vous avez des ingests en cours de téléchargement. Êtes-vous sûr de vouloir quitter la page ?';
    }
  }

  ngOnInit() {
    this.uploadSipService.filesStatus().subscribe((ingestList) => {
      this.ingestList = ingestList;
    });
  }

  showIngest(item: Event) {
    this.openPanel(item);
  }

  openImportSipDialog(type: string) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = 'vitamui-modal';
    dialogConfig.disableClose = false;

    dialogConfig.data = {
      tenantIdentifier: this.tenantIdentifier,
      givenContextId: type
    };

    const dialogRef = this.dialog.open(UploadComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
      }
    });
  }

  changeTenant(tenantIdentifier: number) {
    this.router.navigate(['..', tenantIdentifier], {relativeTo: this.route});
  }

  refresh() {
    this.ingestListComponent.direction = Direction.DESCENDANT;
    this.ingestListComponent.emitOrderChange();
  }
}
