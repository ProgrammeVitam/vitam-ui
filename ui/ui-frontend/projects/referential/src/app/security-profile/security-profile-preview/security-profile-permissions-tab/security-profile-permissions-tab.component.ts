import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FormBuilder, FormControl, FormGroup} from '@angular/forms';
import {SecurityProfile} from 'projects/vitamui-library/src/public-api';
import {Observable, of} from 'rxjs';
import {catchError, filter, map, switchMap} from 'rxjs/operators';
import {diff, Option} from 'ui-frontend-common';
import {extend, isEmpty} from 'underscore';

import {SecurityProfileService} from '../../security-profile.service';

@Component({
  selector: 'app-security-profile-permissions-tab',
  templateUrl: './security-profile-permissions-tab.component.html',
  styleUrls: ['./security-profile-permissions-tab.component.scss']
})
export class SecurityProfilePermissionsTabComponent {
  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();

  form: FormGroup;

  submited = false;

  ruleFilter = new FormControl();

  // tslint:disable-next-line:variable-name
  private _SecurityProfile: SecurityProfile;

  // FIXME: Get list from common var ?
  rules: Option[] = [
    {key: 'StorageRule', label: 'Durée d\'utilité courante', info: ''},
    {key: 'ReuseRule', label: 'Durée de réutilisation', info: ''},
    {key: 'ClassificationRule', label: 'Durée de classification', info: ''},
    {key: 'DisseminationRule', label: 'Délai de diffusion', info: ''},
    {key: 'AdministrationRule', label: 'Durée d\'utilité administrative', info: ''},
    {key: 'AppraisalRule', label: 'Délai de communicabilité', info: ''}
  ];

  previousValue = (): SecurityProfile => {
    return this._SecurityProfile;
  }

  @Input()
  // tslint:disable-next-line:no-shadowed-variable
  set SecurityProfile(SecurityProfile: SecurityProfile) {
    this._SecurityProfile = SecurityProfile;
    this.resetForm(this.SecurityProfile);
  }

  get SecurityProfile(): SecurityProfile {
    return this._SecurityProfile;
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
    // tslint:disable-next-line:no-shadowed-variable
    private SecurityProfileService: SecurityProfileService
  ) {
    this.form = this.formBuilder.group({
      fullAccess: [null],
      permissions: [null]
    });

    this.ruleFilter.valueChanges.subscribe((val) => {
      if (val === true) {
        this.form.controls.ruleCategoryToFilter.setValue(new Array<string>());
      }
    });
  }

  unchanged(): boolean {
    let unchanged = true;

    if (this.form.getRawValue().permissions.length !== this.previousValue().permissions.length) {
      unchanged = false;
    } else {
      const previousPermissions = this.previousValue().permissions;
      // tslint:disable-next-line:no-shadowed-variable
      const diff = this.form.getRawValue().permissions.filter((permission: string) => {
        return previousPermissions.indexOf(permission) === -1;
      });
      if (diff.length > 0) {
        unchanged = false;
      }
    }

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
        ) => this.SecurityProfileService.patch(formData).pipe(catchError(() => of(null)))));
  }

  onSubmit() {
    this.submited = true;
    if (this.isInvalid()) {
      return;
    }
    this.prepareSubmit().subscribe(() => {
      this.SecurityProfileService.get(this.SecurityProfile.identifier).subscribe(
        response => {
          this.submited = false;
          this.SecurityProfile = response;
        }
      );
    }, () => {
      this.submited = false;
    });
  }

  // tslint:disable-next-line:no-shadowed-variable
  resetForm(SecurityProfile: SecurityProfile) {
    this.form.reset(SecurityProfile, {emitEvent: false});
  }
}
