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
import { Component, OnInit, ViewChild } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { ActivatedRoute } from '@angular/router';

import { TranslateService } from '@ngx-translate/core';
import { ApplicationId, GlobalEventService, SidenavPage, SecurityService, Role } from 'vitamui-library';
import { Ontology } from 'vitamui-library';
import { FileTypes } from 'vitamui-library';
import { ImportDialogParam, ReferentialTypes } from '../shared/import-dialog/import-dialog-param.interface';
import { ImportDialogComponent } from '../shared/import-dialog/import-dialog.component';
import { OntologyCreateComponent } from './ontology-create/ontology-create.component';
import { OntologyListComponent } from './ontology-group/ontology-list/ontology-list.component';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-ontology',
  templateUrl: './ontology.component.html',
  styleUrls: ['./ontology.component.scss'],
})
export class OntologyComponent extends SidenavPage<Ontology> implements OnInit {
  @ViewChild(OntologyListComponent, { static: true }) ontologyListComponent: OntologyListComponent;
  search = '';
  filters: string;
  tenantId: number;
  canImportOntology$: Observable<boolean>;
  canImportSchema$: Observable<boolean>;
  canCreateVocabulary$: Observable<boolean>;

  constructor(
    public dialog: MatDialog,
    private route: ActivatedRoute,
    globalEventService: GlobalEventService,
    private translateService: TranslateService,
    private securityService: SecurityService,
  ) {
    super(route, globalEventService);
  }

  ngOnInit(): void {
    this.initializeTenantId();
    this.initializePermissions();
    this.subscribeToTenantChanges();
  }

  private initializeTenantId(): void {
    this.route.params.subscribe((params) => {
      this.tenantId = +params.tenantIdentifier;
    });
  }

  private subscribeToTenantChanges(): void {
    this.globalEventService.tenantEvent.subscribe(() => this.refreshList());
  }

  private initializePermissions(): void {
    this.canImportSchema$ = this.securityService.hasRole(ApplicationId.ONTOLOGY_APP, this.tenantId, Role.ROLE_IMPORT_SCHEMAS);
    this.canImportOntology$ = this.securityService.hasRole(ApplicationId.ONTOLOGY_APP, this.tenantId, Role.ROLE_IMPORT_ONTOLOGY);
    this.canCreateVocabulary$ = this.securityService.hasRole(ApplicationId.ONTOLOGY_APP, this.tenantId, Role.ROLE_CREATE_ONTOLOGIES);
  }

  openCreateOntologyDialog() {
    const dialogRef = this.dialog.open(OntologyCreateComponent, { panelClass: 'vitamui-modal', disableClose: true });
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

  showOntology(item: Ontology) {
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
      'IMPORT_DIALOG.TITLE',
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
      'IMPORT_DIALOG.SCHEMA_FORMAT_CSV',
    );
    this.openImportDialog(params);
  }

  private openImportDialog(params: ImportDialogParam) {
    this.dialog
      .open(ImportDialogComponent, {
        panelClass: 'vitamui-modal',
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
