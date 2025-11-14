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
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { finalize, Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { isEmpty } from 'underscore';
import { Agency, AgencyService, ApplicationId, diff, MiscValidators, Role, SecurityService, VitamUICommonModule } from 'vitamui-library';
import { TranslatePipe } from '@ngx-translate/core';
import { AsyncPipe, NgIf } from '@angular/common';
import { AgencyCreateValidators } from '../../agency-create/agency-create.validators';

@Component({
  selector: 'app-agency-information-tab',
  templateUrl: './agency-information-tab.component.html',
  styleUrls: ['./agency-information-tab.component.scss'],
  imports: [ReactiveFormsModule, VitamUICommonModule, TranslatePipe, AsyncPipe, NgIf],
})
export class AgencyInformationTabComponent {
  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();

  tenantIdentifier: number;
  isLoading = false;
  checkUpdateRole = new Observable<boolean>();

  private _agency: Agency;

  form: FormGroup;
  previousValue = (): any => {
    return (Object.keys(this.form.controls || {}) as (keyof Agency)[]).reduce((acc: any, key) => {
      acc[key] = this._agency[key];
      return acc;
    }, {} as Partial<Agency>);
  };

  @Input()
  set agency(agency: Agency) {
    if (!agency.description) {
      agency.description = '';
    }
    this._agency = agency;
    this.resetForm(this.agency);
    this.updated.emit(false);
  }

  get agency(): Agency {
    return this._agency;
  }

  @Input()
  set readOnly(readOnly: boolean) {
    if (readOnly && this.form.enabled) {
      this.form.disable({ emitEvent: false });
    } else if (this.form.disabled) {
      this.form.enable({ emitEvent: false });
    }
  }

  constructor(
    private route: ActivatedRoute,
    private formBuilder: FormBuilder,
    private agencyService: AgencyService,
    private securityService: SecurityService,
    private agencyCreateValidators: AgencyCreateValidators,
  ) {
    this.form = this.formBuilder.group({
      name: [null, [MiscValidators.requiredNotBlank]],
      description: [null],
    });

    this.route.params.subscribe((params) => {
      this.tenantIdentifier = +params.tenantIdentifier;
    });

    this.checkUpdateRole = this.securityService.hasRole$(ApplicationId.AGENCIES_APP, Role.ROLE_UPDATE_AGENCIES, this.tenantIdentifier);
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  isInvalid(): boolean {
    return this.form.get('name').invalid || this.form.get('name').pending;
  }

  prepareSubmit(): Observable<Agency> {
    return of(diff(this.form.getRawValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => this.copyProperties(formData)),
      switchMap((agency) => this.agencyService.patch(agency).pipe(catchError(() => of(null)))),
    );
  }

  copyProperties(formData: { [key: string]: any }): Agency {
    return {
      ...this.agency,
      ...formData,
    };
  }

  onSubmit() {
    this.isLoading = true;
    if (this.isInvalid()) {
      return;
    }
    this.prepareSubmit()
      .pipe(
        switchMap(() => this.agencyService.get(this._agency.identifier)),
        finalize(() => (this.isLoading = false)),
      )
      .subscribe({
        next: (agency) => {
          this.agency = agency;
        },
        error: (e) => console.error(e),
      });
  }

  resetForm(agency: Agency) {
    this.form.get('name').setAsyncValidators(this.agencyCreateValidators.uniqueName(agency.name)); // Keep this line before reset to make sure the new form value is used for validation.
    this.form.reset(agency, { emitEvent: false });
  }
}
