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
import { Component, HostListener, inject, OnInit, viewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import {
  AdminUserProfile,
  DatepickerComponent,
  Direction,
  GlobalEventService,
  SearchBarComponent,
  SidenavPage,
  TooltipDirective,
  VitamuiBannerComponent,
  VitamuiMenuButtonComponent,
  VitamuiTitleBreadcrumbComponent,
} from 'vitamui-library';
import { IngestList } from '../core/common/ingest-list';
import { IngestType } from '../core/common/ingest-type.enum';
import { UploadComponent } from '../core/common/upload.component';
import { UploadService } from '../core/common/upload.service';
import { LogbookOperation } from '../models/logbook-event.interface';
import { IngestListComponent } from './ingest-list/ingest-list.component';
import { MatSidenav, MatSidenavContainer, MatSidenavContent } from '@angular/material/sidenav';
import { IngestPreviewComponent } from './ingest-preview/ingest-preview.component';
import { NgStyle } from '@angular/common';
import { MatMenuItem } from '@angular/material/menu';
import { UploadTrackingComponent } from '../shared/upload-tracking/upload-tracking.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-ingest',
  templateUrl: './ingest.component.html',
  styleUrls: ['./ingest.component.scss'],
  imports: [
    MatSidenavContainer,
    MatSidenav,
    IngestPreviewComponent,
    MatSidenavContent,
    VitamuiTitleBreadcrumbComponent,
    VitamuiBannerComponent,
    TooltipDirective,
    NgStyle,
    VitamuiMenuButtonComponent,
    MatMenuItem,
    ReactiveFormsModule,
    DatepickerComponent,
    UploadTrackingComponent,
    IngestListComponent,
    TranslatePipe,
  ],
})
export class IngestComponent extends SidenavPage<LogbookOperation> implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);
  private readonly formBuilder = inject(FormBuilder);
  private readonly uploadSipService = inject(UploadService);

  readonly searchBar = viewChild.required(SearchBarComponent);
  readonly ingestListComponent = viewChild.required(IngestListComponent);

  readonly IngestType = IngestType;

  search = '';
  tenantIdentifier = '';
  guard = true;
  connectedUserInfo!: AdminUserProfile;
  dateRangeFilterForm!: ReturnType<typeof this.buildDateRangeForm>;
  filters: { startDate?: Date; endDate?: Date } = {};
  ingestList: IngestList = new IngestList();
  ingestThatHasChanged: LogbookOperation | null = null;

  constructor() {
    const globalEventService = inject(GlobalEventService);
    const route = inject(ActivatedRoute);
    super(route, globalEventService);
  }

  ngOnInit(): void {
    this.initTenantFromRoute();
    this.initDateRangeForm();
    this.subscribeToUploadStatus();
  }

  @HostListener('window:beforeunload', ['$event'])
  onBeforeUnload(event: BeforeUnloadEvent): string | undefined {
    if (this.ingestList.wipNumber > 0) {
      event.preventDefault();
      const message = 'Vous avez des ingests en cours de téléchargement. Êtes-vous sûr de vouloir quitter la page ?';
      event.returnValue = message;
      return message;
    }
    return undefined;
  }

  onSearchSubmit(search: string): void {
    this.search = search ?? '';
  }

  openImportSipDialog(type: IngestType): void {
    const dialogConfig = {
      disableClose: false,
      data: {
        tenantIdentifier: this.tenantIdentifier,
        givenContextId: type,
      },
    };

    this.dialog.open(UploadComponent, dialogConfig).afterClosed().subscribe();
  }

  refresh(): void {
    const list = this.ingestListComponent();
    list.direction = Direction.DESCENDANT;
    list.emitOrderChange();
  }

  ingestChangedStatus(ingest: LogbookOperation): void {
    this.ingestThatHasChanged = ingest;
  }

  private initTenantFromRoute(): void {
    this.route.params.subscribe((params) => {
      this.tenantIdentifier = params['tenantIdentifier'] ?? '';
    });
  }

  private initDateRangeForm(): void {
    this.dateRangeFilterForm = this.buildDateRangeForm();

    this.dateRangeFilterForm.controls.startDate.valueChanges.subscribe((value) => {
      this.filters = { ...this.filters, startDate: value };
    });

    this.dateRangeFilterForm.controls.endDate.valueChanges.subscribe((value) => {
      this.filters = { ...this.filters, endDate: value };
      this.ingestListComponent().direction = Direction.DESCENDANT;
    });
  }

  private buildDateRangeForm() {
    return this.formBuilder.group({
      startDate: null as Date | null,
      endDate: null as Date | null,
    });
  }

  private subscribeToUploadStatus(): void {
    this.uploadSipService.filesStatus().subscribe((ingestList) => {
      this.ingestList = ingestList;
    });
  }
}
