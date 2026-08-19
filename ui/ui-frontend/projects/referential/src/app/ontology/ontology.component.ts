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
import { Component, inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';

import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import {
  ApplicationId,
  ClickOutsideDirective,
  FileTypes,
  GlobalEventService,
  Ontology,
  Role,
  SchemaElement,
  SchemaService,
  SecurityService,
  SidenavPage,
  VitamuiBannerComponent,
  VitamuiMenuButtonComponent,
  VitamuiTitleBreadcrumbComponent,
} from 'vitamui-library';
import { ImportDialogParam, ReferentialTypes } from '../shared/import-dialog/import-dialog-param.interface';
import { ImportDialogComponent } from '../shared/import-dialog/import-dialog.component';
import { OntologyCreateComponent } from './ontology-create/ontology-create.component';
import { OntologyListComponent } from './ontology-group/ontology-list/ontology-list.component';
import { Subscription } from 'rxjs';
import { OntologyService } from './ontology.service';
import { MatSidenav, MatSidenavContainer, MatSidenavContent } from '@angular/material/sidenav';
import { OntologyPreviewComponent } from './ontology-preview/ontology-preview.component';
import { MatMenuItem } from '@angular/material/menu';
import { OntologyGroupComponent } from './ontology-group/ontology-group.component';

@Component({
  selector: 'app-ontology',
  templateUrl: './ontology.component.html',
  styleUrls: ['./ontology.component.scss'],
  imports: [
    MatSidenavContainer,
    MatSidenav,
    ClickOutsideDirective,
    OntologyPreviewComponent,
    MatSidenavContent,
    VitamuiTitleBreadcrumbComponent,
    VitamuiBannerComponent,
    VitamuiMenuButtonComponent,
    MatMenuItem,
    OntologyGroupComponent,
    TranslatePipe,
  ],
})
export class OntologyComponent extends SidenavPage<Ontology | SchemaElement> implements OnInit, OnDestroy {
  dialog = inject(MatDialog);
  route: ActivatedRoute;
  private translateService = inject(TranslateService);
  private securityService = inject(SecurityService);
  private ontologyService = inject(OntologyService);
  private schemaService = inject(SchemaService);

  private previousTab: string | null = null;
  private subscription: Subscription;
  @ViewChild(OntologyListComponent, { static: true }) ontologyListComponent: OntologyListComponent;
  search = '';
  filters: string;
  tenantId: number;
  canImportOntology: boolean;
  canImportSchema: boolean;
  canCreateVocabulary: boolean;

  constructor() {
    const route = inject(ActivatedRoute);
    const globalEventService = inject(GlobalEventService);

    super(route, globalEventService);

    this.route = route;
  }

  ngOnInit(): void {
    this.initializeTenantId();
    this.initializePermissions();
    this.subscribeToTenantChanges();

    this.subscription = this.route.queryParams.subscribe((params) => {
      const currentTab = params['tab'];
      if (this.previousTab !== null && this.previousTab !== currentTab) {
        this.closePanel();
      }
      this.previousTab = currentTab;
    });
  }

  private initializeTenantId(): void {
    this.route.params.subscribe((params) => {
      this.tenantId = +params['tenantIdentifier'];
    });
  }

  private subscribeToTenantChanges(): void {
    this.globalEventService.tenantEvent.subscribe(() => this.refreshList());
  }

  private initializePermissions(): void {
    this.canImportSchema = this.securityService.hasRole(ApplicationId.ONTOLOGY_APP, Role.ROLE_IMPORT_SCHEMAS, this.tenantId);
    this.canImportOntology = this.securityService.hasRole(ApplicationId.ONTOLOGY_APP, Role.ROLE_IMPORT_ONTOLOGIES, this.tenantId);
    this.canCreateVocabulary = this.securityService.hasRole(ApplicationId.ONTOLOGY_APP, Role.ROLE_CREATE_ONTOLOGIES, this.tenantId);
  }

  openCreateOntologyDialog() {
    const dialogRef = this.dialog.open(OntologyCreateComponent, { disableClose: true });
    dialogRef.afterClosed().subscribe((result) => {
      if (result?.success) {
        //TODO(refacto): created$ in service, refresh in component
        this.refreshList();
      }
      if (result?.action === 'restart') {
        this.openCreateOntologyDialog();
      }
    });
  }

  private refreshList() {
    this.ontologyListComponent?.searchOntologyOrdered();
  }

  onSearchSubmit(search: string) {
    this.search = search || '';
  }

  showOntology(item: Ontology | SchemaElement) {
    this.dialog.closeAll();
    this.openPanel(item);
  }

  private getImportDialogParams(
    type: ReferentialTypes,
    titleKey: string,
    subtitleKey: string,
    allowedFiles: FileTypes[],
    formatKey: string,
  ): ImportDialogParam {
    return {
      title: this.translateService.instant(titleKey),
      subtitle: this.translateService.instant(subtitleKey),
      allowedFiles,
      fileFormatDetailInfo: this.translateService.instant(formatKey),
      referential: type,
      successMessage: 'SNACKBAR.IMPORT_REFERENTIAL_SUCCESSED',
      errorMessage: 'SNACKBAR.IMPORT_REFERENTIAL_FAILED',
      iconMessage: 'vitamui-icon-ontologie',
    };
  }

  openOntologyImportDialog() {
    const params = this.getImportDialogParams(
      ReferentialTypes.ONTOLOGY,
      'IMPORT_DIALOG.ONTOLOGY_TITLE',
      'IMPORT_DIALOG.ONTOLOGY_SUBTITLE',
      [FileTypes.JSON],
      'IMPORT_DIALOG.ONTOLOGY_FORMAT_JSON',
    );
    this.openImportDialog(params);
  }

  openSchemaImportDialog() {
    const params = this.getImportDialogParams(
      ReferentialTypes.SCHEMA_UNIT,
      'IMPORT_DIALOG.SCHEMA_TITLE',
      'IMPORT_DIALOG.SCHEMA_SUBTITLE',
      [FileTypes.CSV],
      'IMPORT_DIALOG.SCHEMA_FORMAT_CSV_SEMICOLON',
    );
    this.openImportDialog(params);
  }

  onClose() {
    this.ontologyService.selectedId$.next(null);
    this.schemaService.selectedPath$.next(null);
  }

  override ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  private openImportDialog(params: ImportDialogParam) {
    this.dialog
      .open(ImportDialogComponent, {
        disableClose: true,
        data: params,
      })
      .afterClosed()
      .subscribe((result) => {
        if (result?.successfulImport) {
          this.refreshList();
        }
      });
  }
}
