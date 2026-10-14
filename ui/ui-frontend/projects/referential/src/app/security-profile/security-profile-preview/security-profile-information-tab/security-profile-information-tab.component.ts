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
import { Component, inject, signal, output, input, model, OnChanges, SimpleChanges } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import { diff, InputComponent, Option, SecurityProfile, SlideToggleComponent } from 'vitamui-library';
import { SecurityProfileService } from '../../security-profile.service';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-security-profile-information-tab',
  templateUrl: './security-profile-information-tab.component.html',
  styleUrls: ['./security-profile-information-tab.component.scss'],
  imports: [ReactiveFormsModule, InputComponent, SlideToggleComponent, TranslatePipe],
})
export class SecurityProfileInformationTabComponent implements OnChanges {
  private readonly formBuilder = inject(FormBuilder);
  private readonly securityProfileService = inject(SecurityProfileService);

  readonly updated = output<boolean>();
  readonly fullAccessUpdated = output<boolean>();
  readonly securityProfileUpdated = output<SecurityProfile>();

  // FIXME: Get list from common var ?
  rules: Option[] = [
    { key: 'StorageRule', label: "Durée d'utilité courante", info: '' },
    { key: 'ReuseRule', label: 'Durée de réutilisation', info: '' },
    { key: 'ClassificationRule', label: 'Durée de classification', info: '' },
    { key: 'DisseminationRule', label: 'Délai de diffusion', info: '' },
    { key: 'AdministrationRule', label: "Durée d'utilité administrative", info: '' },
    { key: 'AppraisalRule', label: 'Délai de communicabilité', info: '' },
  ];

  form: FormGroup;

  submitted = signal(false);

  ruleFilter = new FormControl();

  previousValue = (): any => {
    return (Object.keys(this.form.controls || {}) as (keyof SecurityProfile)[]).reduce((acc: any, key) => {
      acc[key] = this.securityProfile()[key];
      return acc;
    }, {} as Partial<SecurityProfile>);
  };

  readonly securityProfile = model<SecurityProfile>(undefined);

  readonly readOnly = input<boolean>(undefined);

  constructor() {
    this.form = this.formBuilder.group({
      identifier: [null, Validators.required],
      name: [null, Validators.required],
      fullAccess: [null],
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['securityProfile']) {
      this.setSecurityProfile(this.securityProfile());
    }
    if (changes['readOnly']) {
      const readOnly = this.readOnly();
      if (readOnly && this.form.enabled) {
        this.form.disable({ emitEvent: false });
      } else if (this.form.disabled) {
        this.form.enable({ emitEvent: false });
        this.form.get('identifier').disable({ emitEvent: false });
      }
    }
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  prepareSubmit(): Observable<SecurityProfile> {
    return of(diff(this.form.getRawValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.previousValue().id, identifier: this.previousValue().identifier }, formData)),
      switchMap((formData: { id: string; [key: string]: any }) => {
        if (formData['fullAccess']) {
          formData = { ...formData, permissions: [] };
        }
        return this.securityProfileService.patch(formData).pipe(catchError(() => of(null)));
      }),
    );
  }

  onSubmit() {
    this.submitted.set(true);
    if (this.form.invalid) {
      return;
    }
    this.prepareSubmit().subscribe(
      () => {
        this.securityProfileService.get(this.securityProfile().identifier).subscribe((response) => {
          this.submitted.set(false);
          this.setSecurityProfile(response);
          this.fullAccessUpdated.emit(this.form.value.fullAccess);
          this.securityProfileUpdated.emit(response);
        });
      },
      () => {
        this.submitted.set(false);
      },
    );
  }

  private setSecurityProfile(securityProfile: SecurityProfile) {
    this.securityProfile.set(securityProfile);
    this.resetForm(this.securityProfile());
    this.updated.emit(false);
  }

  resetForm(SecurityProfile: SecurityProfile) {
    this.form.reset(SecurityProfile, { emitEvent: false });
  }
}
