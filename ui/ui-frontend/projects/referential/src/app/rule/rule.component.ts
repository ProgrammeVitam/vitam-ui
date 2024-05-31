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
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { ApplicationId, GlobalEventService, Role, Rule, RuleService, SecurityService, SidenavPage } from 'vitamui-library';
import { FileTypes } from 'vitamui-library';
import { ImportDialogParam, ReferentialTypes } from '../shared/import-dialog/import-dialog-param.interface';
import { ImportDialogComponent } from '../shared/import-dialog/import-dialog.component';
import { RuleCreateComponent } from './rule-create/rule-create.component';
import { RuleListComponent } from './rule-list/rule-list.component';

@Component({
  selector: 'app-rules',
  templateUrl: './rule.component.html',
  styleUrls: ['./rule.component.scss'],
})
export class RuleComponent extends SidenavPage<Rule> implements OnInit {
  @ViewChild(RuleListComponent, { static: true }) ruleListComponentListComponent: RuleListComponent;

  search: string = '';
  filters: string;
  tenantId: number;

  checkCreateRole = new Observable<boolean>();
  checkImportRole = new Observable<boolean>();
  checkExportRole = new Observable<boolean>();

  constructor(
    public ruleService: RuleService,
    public dialog: MatDialog,
    private route: ActivatedRoute,
    private router: Router,
    private translateService: TranslateService,
    globalEventService: GlobalEventService,
    private securityService: SecurityService,
  ) {
    super(route, globalEventService);
    globalEventService.tenantEvent.subscribe(() => {
      this.refreshList();
    });

    this.route.params.subscribe((params) => {
      if (params.tenantIdentifier) {
        // tslint:disable-next-line:radix
        this.tenantId = parseInt(params.tenantIdentifier);
      }
    });
  }

  openCreateRuleDialog() {
    const dialogRef = this.dialog.open(RuleCreateComponent, {
      panelClass: 'vitamui-modal',
      disableClose: true,
    });
    dialogRef.componentInstance.tenantIdentifier = this.tenantId;
    dialogRef.afterClosed().subscribe((result) => {
      if (result !== undefined) {
        this.refreshList();
      }
    });
  }

  private refreshList() {
    if (!this.ruleListComponentListComponent) {
      return;
    }
    this.ruleListComponentListComponent.searchRuleOrdered();
  }

  changeTenant(tenantIdentifier: number) {
    this.tenantId = tenantIdentifier;
    this.router.navigate(['..', tenantIdentifier], { relativeTo: this.route });
  }

  onSearchSubmit(search: string) {
    this.search = search || '';
  }

  ngOnInit() {
    this.checkCreateRole = this.securityService.hasRole(ApplicationId.RULES_APP, this.tenantId, Role.ROLE_CREATE_RULES);
    this.checkImportRole = this.securityService.hasRole(ApplicationId.RULES_APP, this.tenantId, Role.ROLE_IMPORT_RULES);
    this.checkExportRole = this.securityService.hasRole(ApplicationId.RULES_APP, this.tenantId, Role.ROLE_EXPORT_RULES);
  }

  showRule(item: Rule) {
    this.openPanel(item);
  }

  exportRules() {
    this.ruleService.export();
  }

  openRuleImportDialog() {
    const params: ImportDialogParam = {
      title: this.translateService.instant('IMPORT_DIALOG.TITLE'),
      subtitle: this.translateService.instant('IMPORT_DIALOG.RULES_SUBTITLE'),
      allowedFiles: [FileTypes.CSV],
      referential: ReferentialTypes.RULE,
      successMessage: 'SNACKBAR.IMPORT_REFERENTIAL_SUCCESSED',
      errorMessage: 'SNACKBAR.IMPORT_REFERENTIAL_FAILED',
      iconMessage: 'vitamui-icon-rules',
    };

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
