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

import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { diff, Option, SecurityProfile } from 'vitamui-library';
import { extend, isEmpty } from 'underscore';
import { SecurityProfileService } from '../../security-profile.service';

@Component({
  selector: 'app-security-profile-information-tab',
  templateUrl: './security-profile-information-tab.component.html',
  styleUrls: ['./security-profile-information-tab.component.scss'],
})
export class SecurityProfileInformationTabComponent {
  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() fullAccessUpdated: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() securityProfileUpdated: EventEmitter<SecurityProfile> = new EventEmitter<SecurityProfile>();

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

  submitted = false;

  ruleFilter = new FormControl();

  // tslint:disable-next-line:variable-name
  private _securityProfile: SecurityProfile;

  previousValue = (): SecurityProfile => {
    return this._securityProfile;
  };

  @Input()
  // tslint:disable-next-line:no-shadowed-variable
  set securityProfile(SecurityProfile: SecurityProfile) {
    this._securityProfile = SecurityProfile;
    this.resetForm(this.securityProfile);
    this.updated.emit(false);
  }

  get securityProfile(): SecurityProfile {
    return this._securityProfile;
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

  constructor(
    private formBuilder: FormBuilder,
    private securityProfileService: SecurityProfileService,
  ) {
    this.form = this.formBuilder.group({
      identifier: [null, Validators.required],
      name: [null, Validators.required],
      fullAccess: [null],
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

  prepareSubmit(): Observable<SecurityProfile> {
    return of(diff(this.form.getRawValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.previousValue().id, identifier: this.previousValue().identifier }, formData)),
      switchMap((formData: { id: string; [key: string]: any }) => {
        if (formData.fullAccess) {
          formData = { ...formData, permissions: [] };
        }
        return this.securityProfileService.patch(formData).pipe(catchError(() => of(null)));
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
        this.securityProfileService.get(this._securityProfile.identifier).subscribe((response) => {
          this.submitted = false;
          this.securityProfile = response;
          this.fullAccessUpdated.emit(this.form.value.fullAccess);
          this.securityProfileUpdated.emit(response);
        });
      },
      () => {
        this.submitted = false;
      },
    );
  }

  // tslint:disable-next-line:no-shadowed-variable
  resetForm(SecurityProfile: SecurityProfile) {
    this.form.reset(SecurityProfile, { emitEvent: false });
  }
}
