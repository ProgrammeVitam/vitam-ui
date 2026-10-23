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
import { Component, inject, signal, output, input, OnChanges, SimpleChanges } from '@angular/core';
import { disabled, form, FormField, FormRoot, maxLength, minLength, required } from '@angular/forms/signals';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap, tap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import {
  Context,
  DatepickerComponent,
  diff,
  InputComponent,
  Option,
  requiredValue,
  SelectComponent,
  SlideToggleComponent,
} from 'vitamui-library';
import { RULE_TYPES } from '../../../rule/rules.constants';
import { SecurityProfileService } from '../../../security-profile/security-profile.service';
import { ContextService } from '../../context.service';
import { ContextCreateValidators } from '../../context-create/context-create.validators';
import { TranslatePipe } from '@ngx-translate/core';

interface ContextInformationForm {
  name: string;
  status: string;
  securityProfile: string;
  enableControl: boolean;
  creationDate: string;
  activationDate: string;
  lastUpdate: string;
  deactivationDate: string;
}

const FORM_KEYS: (keyof ContextInformationForm)[] = [
  'name',
  'status',
  'securityProfile',
  'enableControl',
  'creationDate',
  'activationDate',
  'lastUpdate',
  'deactivationDate',
];

@Component({
  selector: 'app-context-information-tab',
  templateUrl: './context-information-tab.component.html',
  styleUrls: ['./context-information-tab.component.scss'],
  imports: [FormRoot, FormField, SlideToggleComponent, InputComponent, SelectComponent, DatepickerComponent, TranslatePipe],
})
export class ContextInformationTabComponent implements OnChanges {
  private readonly contextService = inject(ContextService);
  private readonly securityProfileService = inject(SecurityProfileService);
  private readonly contextCreateValidators = inject(ContextCreateValidators);

  readonly updated = output<boolean>();

  // Name of the displayed context, ignored by the unique name validator
  private readonly nameToIgnore = signal<string>(undefined);

  readonly form = form(
    signal<ContextInformationForm>(this.toFormValue({} as Context)),
    (path) => {
      required(path.name);
      minLength(path.name, 2);
      maxLength(path.name, 100);
      this.contextCreateValidators.allowedName(path.name);
      this.contextCreateValidators.uniqueName(path.name, () => this.nameToIgnore());
      required(path.status);
      required(path.securityProfile);
      requiredValue(path.enableControl);
      required(path.creationDate);
      disabled(path, () => !!this.readOnly());
      disabled(path.creationDate);
      disabled(path.activationDate);
      disabled(path.lastUpdate);
      disabled(path.deactivationDate);
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

  submitted = signal(false);

  securityProfiles: Option[] = [];

  private _context: Context;

  rules: Option[] = RULE_TYPES;

  previousValue = (): any => this.toFormValue(this._context);

  readonly context = input<Context>(undefined);

  readonly readOnly = input<boolean>(undefined);

  constructor() {
    this.securityProfileService.getAll().subscribe((securityProfiles) => {
      this.securityProfiles = securityProfiles.map((x) => ({ label: x.name, key: x.identifier }));
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['context']) {
      this.setContext(this.context());
    }
  }

  private toFormValue(context: Context): ContextInformationForm {
    return FORM_KEYS.reduce((acc: any, key) => {
      acc[key] = (context as any)[key] ?? null;
      return acc;
    }, {}) as ContextInformationForm;
  }

  private setContext(context: Context) {
    this._context = context;
    this.resetForm(this._context);
    this.updated.emit(false);
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.form().value(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  isInvalid(): boolean {
    return false;
  }

  prepareSubmit(): Observable<Context> {
    const diffValue = diff(this.form().value(), this.previousValue());

    return of(diffValue).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.previousValue().id, identifier: this.previousValue().identifier }, formData)),
      switchMap((formData: { id: string; [key: string]: any }) => {
        // Update the activation and deactivation dates if the context status has changed before sending the data
        if (formData['status']) {
          if (formData['status'] === 'ACTIVE') {
            formData['activationDate'] = new Date();
            formData['deactivationDate'] = '';
          } else {
            formData['status'] = 'INACTIVE';
            formData['activationDate'] = '';
            formData['deactivationDate'] = new Date();
          }
        }
        return this.contextService.patch(formData).pipe(catchError(() => of(null)));
      }),
    );
  }

  onSubmit() {
    this.submitted.set(true);
    if (this.isInvalid()) {
      return;
    }
    this.prepareSubmit().subscribe(
      () => {
        this.contextService
          .get(this._context.identifier)
          .pipe(tap((response) => this.contextService.updated.next(response)))
          .subscribe((response) => {
            this.submitted.set(false);
            this.setContext(response);
          });
      },
      () => {
        this.submitted.set(false);
      },
    );
  }

  resetForm(context: Context) {
    this.nameToIgnore.set(context.name); // Keep this line before reset to make sure the new form value is used for validation.
    this.form().reset(this.toFormValue(context));
  }

  onStatusChange(active: boolean) {
    this.form.status().value.set(active ? 'ACTIVE' : 'INACTIVE');
  }
}
