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
import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  Agency,
  ApplicationId,
  BreadCrumbData,
  EditObject,
  EditObjectService,
  ObjectEditorModule,
  SpinnerOverlayService,
  TemplateService,
  TypeService,
  VitamUICommonModule,
  VitamUILibraryModule,
  ProfiledSchemaElement,
} from 'vitamui-library';
import { template } from '../agency.template';
import { schema } from '../agency.schema';
import { AgencyService } from '../agency.service';
import { filter, finalize, of, Subscription, switchMap } from 'rxjs';
import { tap } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-edit-agency',
  templateUrl: 'edit-agency.component.html',
  styles: `
    vitamui-common-editor-banner {
      width: 320%;
      translate: -35%;
      padding-left: 112%;
    }
  `,
  imports: [CommonModule, RouterModule, VitamUICommonModule, VitamUILibraryModule, FormsModule, ReactiveFormsModule, ObjectEditorModule],
  standalone: true,
})
export class EditAgencyComponent implements OnInit, OnDestroy {
  private subscriptions = new Subscription();

  protected readonly template = template;

  agency: Agency;
  breadcrumbData: BreadCrumbData[] = [{ identifier: ApplicationId.PORTAL_APP }, { identifier: ApplicationId.AGENCIES_APP }];
  editObject: EditObject;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private agencyService: AgencyService,
    private editObjectService: EditObjectService,
    private templateService: TemplateService,
    private typeService: TypeService,
    private spinnerService: SpinnerOverlayService,
    private translateService: TranslateService,
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
          const templateSchema = this.editObjectService.createTemplateSchema(template, translatedSchema);
          const data = this.templateService.toProjected(this.agency, template);
          this.editObject = this.editObjectService.editObject('', data, template, templateSchema);
        },
      });
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  save() {
    const formData = this.editObject.control.value;
    const agency = this.templateService.toOriginal(formData, template);
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
          switchMap(() => this.agencyService.patch(partialAgency as Agency)),
          finalize(() => this.spinnerService.close()),
        )
        .subscribe((agency) => (this.agency = agency)),
    );
  }

  cancel() {
    if (this.editObject.control.pristine) return history.back();
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
