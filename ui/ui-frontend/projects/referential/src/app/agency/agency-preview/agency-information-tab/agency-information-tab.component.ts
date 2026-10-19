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
import { Component, inject, signal, output, input, model, OnChanges, Signal, SimpleChanges } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { disabled, form, FormField, FormRoot, required } from '@angular/forms/signals';
import { ActivatedRoute } from '@angular/router';
import { finalize, Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { isEmpty } from 'underscore';
import { Agency, AgencyService, ApplicationId, diff, Role, SecurityService, VitamUICommonModule } from 'vitamui-library';
import { TranslatePipe } from '@ngx-translate/core';
import { AsyncPipe } from '@angular/common';
import { AgencyCreateValidators } from '../../agency-create/agency-create.validators';

interface AgencyInformationForm {
  name: string;
  description: string;
}

@Component({
  selector: 'app-agency-information-tab',
  templateUrl: './agency-information-tab.component.html',
  styleUrls: ['./agency-information-tab.component.scss'],
  imports: [FormRoot, FormField, VitamUICommonModule, TranslatePipe, AsyncPipe],
})
export class AgencyInformationTabComponent implements OnChanges {
  private readonly route = inject(ActivatedRoute);
  private readonly agencyService = inject(AgencyService);
  private readonly securityService = inject(SecurityService);
  private readonly agencyCreateValidators = inject(AgencyCreateValidators);

  readonly updated = output<boolean>();

  tenantIdentifier: number;
  isLoading = signal(false);
  checkUpdateRole = new Observable<boolean>();
  private readonly hasUpdateRole: Signal<boolean>;
  private readonly nameToIgnore = signal<string>(undefined);

  private readonly formModel = signal<AgencyInformationForm>({ name: null, description: null });
  readonly form = form(
    this.formModel,
    (path) => {
      disabled(path, () => !!this.readOnly());
      disabled(path.name, () => this.hasUpdateRole() === false);
      disabled(path.description, () => this.hasUpdateRole() === false);
      required(path.name);
      this.agencyCreateValidators.onlyWhitespaces(path.name);
      this.agencyCreateValidators.uniqueName(path.name, () => this.nameToIgnore());
    },
    {
      submission: {
        action: async () => {
          this.onSubmit();
        },
        ignoreValidators: 'all',
      },
    },
  );
  previousValue = (): any => {
    return (Object.keys(this.formModel()) as (keyof Agency)[]).reduce((acc: any, key) => {
      acc[key] = this.agency()[key];
      return acc;
    }, {} as Partial<Agency>);
  };

  readonly agency = model<Agency>(undefined);

  readonly readOnly = input<boolean>(undefined);

  constructor() {
    this.route.params.subscribe((params) => {
      this.tenantIdentifier = +params['tenantIdentifier'];
    });

    this.checkUpdateRole = this.securityService.hasRole$(ApplicationId.AGENCIES_APP, Role.ROLE_UPDATE_AGENCIES, this.tenantIdentifier);
    this.hasUpdateRole = toSignal(this.checkUpdateRole);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['agency']) {
      this.setAgency(this.agency());
    }
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.formModel(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  isInvalid(): boolean {
    return this.form.name().invalid() || this.form.name().pending();
  }

  prepareSubmit(): Observable<Agency> {
    return of(diff(this.formModel(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => this.copyProperties(formData)),
      switchMap((agency) => this.agencyService.patch(agency).pipe(catchError(() => of(null)))),
    );
  }

  copyProperties(formData: { [key: string]: any }): Agency {
    return {
      ...this.agency(),
      ...formData,
    };
  }

  onSubmit() {
    this.isLoading.set(true);
    if (this.isInvalid()) {
      return;
    }
    this.prepareSubmit()
      .pipe(
        switchMap(() => this.agencyService.get(this.agency().identifier)),
        finalize(() => this.isLoading.set(false)),
      )
      .subscribe({
        next: (agency) => {
          this.setAgency(agency);
        },
        error: (e) => console.error(e),
      });
  }

  private setAgency(agency: Agency) {
    if (!agency.description) {
      agency.description = '';
    }
    this.agency.set(agency);
    this.resetForm(this.agency());
    this.updated.emit(false);
  }

  resetForm(agency: Agency) {
    this.nameToIgnore.set(agency.name); // Keep this line before reset to make sure the new form value is used for validation.
    this.form().reset({ name: agency.name ?? null, description: agency.description ?? null });
  }
}
