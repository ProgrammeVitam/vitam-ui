import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {SecurityProfile} from 'projects/vitamui-library/src/public-api';

import {Observable, of} from 'rxjs';
import {catchError, filter, map, switchMap} from 'rxjs/operators';
import {diff, Option} from 'ui-frontend-common';
import {extend, isEmpty} from 'underscore';

import {SecurityProfileService} from '../../security-profile.service';

@Component({
  selector: 'app-security-profile-information-tab',
  templateUrl: './security-profile-information-tab.component.html',
  styleUrls: ['./security-profile-information-tab.component.scss']
})
export class SecurityProfileInformationTabComponent {
  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() fullAccessUpdated: EventEmitter<boolean> = new EventEmitter<boolean>();


  // FIXME: Get list from common var ?
  rules: Option[] = [
    {key: 'StorageRule', label: 'Durée d\'utilité courante', info: ''},
    {key: 'ReuseRule', label: 'Durée de réutilisation', info: ''},
    {key: 'ClassificationRule', label: 'Durée de classification', info: ''},
    {key: 'DisseminationRule', label: 'Délai de diffusion', info: ''},
    {key: 'AdministrationRule', label: 'Durée d\'utilité administrative', info: ''},
    {key: 'AppraisalRule', label: 'Délai de communicabilité', info: ''}
  ];

  form: FormGroup;

  submited = false;

  ruleFilter = new FormControl();

  // tslint:disable-next-line:variable-name
  private _securityProfile: SecurityProfile;

  previousValue = (): SecurityProfile => {
    return this._securityProfile;
  }

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
      this.form.disable({emitEvent: false});
    } else if (this.form.disabled) {
      this.form.enable({emitEvent: false});
      this.form.get('identifier').disable({emitEvent: false});
    }
  }

  constructor(
    private formBuilder: FormBuilder,
    private securityProfileService: SecurityProfileService
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
      map((formData) => extend({id: this.previousValue().id, identifier: this.previousValue().identifier}, formData)),
      switchMap(
        (formData: { id: string, [key: string]: any }
        ) => this.securityProfileService.patch(formData).pipe(catchError(() => of(null)))));
  }

  onSubmit() {
    this.submited = true;
    if (this.isInvalid()) {
      return;
    }
    this.prepareSubmit().subscribe(() => {
      this.securityProfileService.get(this._securityProfile.identifier).subscribe(
        response => {
          this.submited = false;
          this.securityProfile = response;
        }
      );
      this.fullAccessUpdated.emit(this.form.value.fullAccess);
    }, () => {
      this.submited = false;
    });
  }

  // tslint:disable-next-line:no-shadowed-variable
  resetForm(SecurityProfile: SecurityProfile) {
    this.form.reset(SecurityProfile, {emitEvent: false});
  }
}
