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
import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap, tap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import { Context, Option } from 'vitamui-library';
import { diff, SlideToggleComponent, InputComponent, SelectComponent, DatepickerComponent } from 'vitamui-library';
import { RULE_TYPES } from '../../../rule/rules.constants';
import { SecurityProfileService } from '../../../security-profile/security-profile.service';
import { ContextService } from '../../context.service';
import { ContextCreateValidators } from '../../context-create/context-create.validators';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-context-information-tab',
  templateUrl: './context-information-tab.component.html',
  styleUrls: ['./context-information-tab.component.scss'],
  imports: [ReactiveFormsModule, SlideToggleComponent, InputComponent, SelectComponent, DatepickerComponent, TranslatePipe],
})
export class ContextInformationTabComponent {
  private formBuilder = inject(FormBuilder);
  private contextService = inject(ContextService);
  private securityProfileService = inject(SecurityProfileService);
  private contextCreateValidators = inject(ContextCreateValidators);

  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();

  form: FormGroup;

  submitted = false;

  statusControl = new FormControl();

  securityProfiles: Option[] = [];

  private _context: Context;

  rules: Option[] = RULE_TYPES;

  previousValue = (): any => {
    return (Object.keys(this.form.controls || {}) as (keyof Context)[]).reduce((acc: any, key) => {
      acc[key] = this._context[key] ?? null;
      return acc;
    }, {} as Partial<Context>);
  };

  @Input()
  set context(Context: Context) {
    this._context = Context;
    this.resetForm(this.context);
    this.updated.emit(false);
  }

  get context(): Context {
    return this._context;
  }

  @Input()
  set readOnly(readOnly: boolean) {
    if (readOnly && this.form.enabled) {
      this.form.disable({ emitEvent: false });
    } else if (this.form.disabled) {
      this.form.enable({ emitEvent: false });
      this.form.get('identifier').disable({ emitEvent: false });
    }
  }

  constructor() {
    this.form = this.formBuilder.group({
      name: [
        null,
        [Validators.required, Validators.minLength(2), Validators.maxLength(100), this.contextCreateValidators.allowedName()],
        this.contextCreateValidators.uniqueName(),
      ],
      status: [null, Validators.required],
      securityProfile: [null, Validators.required],
      enableControl: [null, Validators.required],
      creationDate: [{ value: null, disabled: true }, Validators.required],
      activationDate: [{ value: null, disabled: true }],
      lastUpdate: [{ value: null, disabled: true }],
      deactivationDate: [{ value: null, disabled: true }],
    });

    this.securityProfileService.getAll().subscribe((securityProfiles) => {
      this.securityProfiles = securityProfiles.map((x) => ({ label: x.name, key: x.identifier }));
    });

    this.statusControl.valueChanges.subscribe((value) => {
      this.form.controls['status'].setValue((value = value === false ? 'INACTIVE' : 'ACTIVE'));
    });
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  isInvalid(): boolean {
    return false;
  }

  prepareSubmit(): Observable<Context> {
    const diffValue = diff(this.form.getRawValue(), this.previousValue());

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
    this.submitted = true;
    if (this.isInvalid()) {
      return;
    }
    this.prepareSubmit().subscribe(
      () => {
        this.contextService
          .get(this._context.identifier)
          .pipe(tap((response) => this.contextService.updated.next(response)))
          .subscribe((response) => {
            this.submitted = false;
            this.context = response;
          });
      },
      () => {
        this.submitted = false;
      },
    );
  }

  resetForm(context: Context) {
    this.statusControl.setValue(context.status === 'ACTIVE');
    this.form.get('name').setAsyncValidators(this.contextCreateValidators.uniqueName(context.name)); // Keep this line before reset to make sure the new form value is used for validation.
    this.form.reset(context, { emitEvent: false });
  }
}
