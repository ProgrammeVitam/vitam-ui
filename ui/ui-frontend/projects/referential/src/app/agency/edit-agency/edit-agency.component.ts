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
import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';

import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  Agency,
  ApplicationId,
  BreadCrumbData,
  EditObject,
  EditObjectService,
  ObjectEditorModule,
  ProfiledSchemaElement,
  SpinnerOverlayService,
  TemplateService,
  TenantSelectionService,
  TypeService,
  VitamUICommonModule,
  VitamUILibraryModule,
  SnackBarService,
  AgencyService,
} from 'vitamui-library';
import { agencyTemplate } from '../agency.template';
import { schema } from '../agency.schema';
import { filter, finalize, of, Subscription, switchMap } from 'rxjs';
import { tap } from 'rxjs/operators';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-edit-agency',
  templateUrl: 'edit-agency.component.html',
  styleUrls: ['edit-agency.component.scss'],
  imports: [
    RouterModule,
    VitamUICommonModule,
    VitamUILibraryModule,
    FormsModule,
    ReactiveFormsModule,
    ObjectEditorModule,
    MatDialogModule,
    TranslatePipe,
  ],
})
export class EditAgencyComponent implements OnInit, OnDestroy {
  @ViewChild('confirmCancelDialog', { static: true })
  confirmCancelDialog: TemplateRef<EditAgencyComponent>;
  dialogRefToClose: MatDialogRef<EditAgencyComponent>;

  private subscriptions = new Subscription();

  protected readonly template = agencyTemplate;

  agency: Agency;
  breadcrumbData: BreadCrumbData[] = [{ identifier: ApplicationId.PORTAL_APP }, { identifier: ApplicationId.AGENCIES_APP }];
  editObject: EditObject;

  constructor(
    private route: ActivatedRoute,
    private agencyService: AgencyService,
    private editObjectService: EditObjectService,
    private templateService: TemplateService,
    private typeService: TypeService,
    private spinnerService: SpinnerOverlayService,
    private router: Router,
    private tenantSelectionService: TenantSelectionService,
    private dialog: MatDialog,
    private translateService: TranslateService,
    private snackBarService: SnackBarService,
  ) {
    this.agency = this.router.getCurrentNavigation()?.extras?.state?.agency;
  }

  ngOnInit() {
    of(this.agency)
      .pipe(
        switchMap((agency: Agency) => {
          if (agency) return of(agency);

          return this.route.params.pipe(switchMap((params) => this.agencyService.get(params?.agencyIdentifier)));
        }),
      )
      .subscribe({
        next: (agency) => {
          this.agency = agency;
          this.breadcrumbData.push({ label: this.agency.identifier });

          const translatedSchema = this.translateComments(schema);
          const templateSchema = this.editObjectService.createTemplateSchema(agencyTemplate, translatedSchema);
          const data = this.templateService.toProjected(this.agency, agencyTemplate);
          this.editObject = this.editObjectService.editObject('', data, agencyTemplate, templateSchema);
        },
      });
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  save() {
    const formData = this.editObject.control.value;
    const agency = this.templateService.toOriginal(formData, agencyTemplate);
    const partialAgency: Partial<Agency> = Object.entries(agency).reduce(
      (acc, [key, value]) =>
        this.typeService.isConsistent(value)
          ? {
              ...acc,
              [key]: value,
            }
          : acc,
      { id: this.agency.id },
    );
    this.subscriptions.add(
      of(partialAgency)
        .pipe(
          filter((agency) => this.typeService.isConsistent(agency)),
          tap(() => this.spinnerService.open()),
          switchMap((agency) => this.agencyService.patch(agency as Agency)),
          finalize(() => {
            this.spinnerService.close();
            this.snackBarService.open({
              message: 'SNACKBAR.SUCCESSFUL_UPDATE',
              icon: 'vitamui-icon-agent',
              buttons: [
                {
                  appId: ApplicationId.LOGBOOK_OPERATION_APP,
                  label: 'SNACKBAR.VIEW_THE_OPERATIONS_LOG',
                },
              ],
            });
            this.router.navigate(['/agency/tenant/', this.tenantSelectionService.getSelectedTenant().identifier]);
          }),
        )
        .subscribe((agency) => {
          this.agency = agency;
          this.editObject.control.markAsPristine();
        }),
    );
  }

  cancel() {
    if (this.editObject.control.pristine) {
      this.router.navigate(['/agency/tenant/', this.tenantSelectionService.getSelectedTenant().identifier]);
    } else {
      this.dialogRefToClose = this.dialog.open(this.confirmCancelDialog);
    }
  }

  confirmCancel() {
    this.dialogRefToClose.close(true);
    this.router.navigate(['/agency/tenant/', this.tenantSelectionService.getSelectedTenant().identifier]);
  }

  cancelCancel() {
    this.dialogRefToClose.close(true);
  }

  private translateComments(schema: ProfiledSchemaElement[]): ProfiledSchemaElement[] {
    return schema.map((element) => {
      if (element.Control?.Comment) {
        return {
          ...element,
          Control: {
            ...element.Control,
            Comment: this.translateService.instant(element.Control?.Comment),
          },
        };
      }

      return element;
    });
  }
}
