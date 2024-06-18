import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { NotificationService } from '../../../../core/services/notification.service';
import { ProfileService } from '../../../../core/services/profile.service';
import { ArchivalProfileUnit } from '../../../../models/archival-profile-unit';
import { Profile } from '../../../../models/profile';
import { ProfileDescription } from '../../../../models/profile-description.model';
import { ProfileType } from '../../../../models/profile-type.enum';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'profile-information-tab',
  templateUrl: './profile-information-tab.component.html',
  styleUrls: ['./profile-information-tab.component.scss'],
})
export class ProfileInformationTabComponent {
  @Input()
  set inputProfile(profileDescription: ProfileDescription) {
    this._inputProfile = profileDescription;
    this.statusProfile.setValue(this.inputProfile.status !== 'INACTIVE');
    this.resetForm(this.inputProfile);
    this.updated.emit(false);
  }

  get inputProfile(): ProfileDescription {
    return this._inputProfile;
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
    private profileService: ProfileService,
    private loggingService: NotificationService,
    private translateService: TranslateService,
  ) {
    this.form = this.formBuilder.group({
      identifier: [null, Validators.required],
      id: [null, Validators.required],
      type: [null],
      description: [null],
      name: [null, Validators.required],
      creationDate: [null],
      status: [null, Validators.required],
    });

    this.statusProfile.valueChanges.subscribe((value) => {
      this.form.controls.status.setValue(value === false ? 'INACTIVE' : 'ACTIVE');
    });
  }

  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();
  // eslint-disable-next-line @angular-eslint/no-output-native
  @Output() closed: EventEmitter<boolean> = new EventEmitter<boolean>();
  form: FormGroup;

  statusProfile = new FormControl();

  submited = false;

  private _inputProfile: ProfileDescription;
  pending = false;

  updateProfile(inputProfile: ProfileDescription): Observable<Profile | ArchivalProfileUnit | ProfileDescription> {
    const profileDescription = { ...inputProfile, ...this.form.value };
    if (inputProfile.type === ProfileType.PA) {
      return this.profileService.updateProfilePa(profileDescription as Profile).pipe(catchError(() => of(null)));
    } else {
      return this.profileService.updateProfilePua(profileDescription as ArchivalProfileUnit).pipe(catchError(() => of(null)));
    }
  }

  canSubmit() {
    return this.form.valid && !this.submited && this.formHasChanged();
  }

  formHasChanged() {
    for (const k of Object.keys(this.form.value)) {
      const key = k as keyof ProfileDescription;
      if (!this.form.value[key] && !this._inputProfile[key]) {
        continue;
      }
      if (this.form.value[key] !== this._inputProfile[key]) {
        this.updated.emit(true);
        return true;
      }
    }
    return false;
  }

  onSubmit() {
    this.pending = !this.pending;
    this.submited = true;
    this.updateProfile(this.inputProfile).subscribe(
      () => {
        this.submited = false;
        this.pending = !this.pending;
        this.inputProfile = this._inputProfile;
        this.loggingService.showSuccess(this.translateService.instant('PROFILE.LIST_PROFILE.PROFILE_PREVIEW.MODIFICATION_SUCCESS'));
        this.profileService.refreshListProfiles();
        this.closed.emit(true);
      },
      () => {
        this.submited = false;
        this.pending = !this.pending;
        this.loggingService.showSuccess('PROFILE.LIST_PROFILE.PROFILE_PREVIEW.MODIFICATION_ERROR');
      },
    );
  }

  resetForm(profileDescription: ProfileDescription) {
    this.form.reset(profileDescription, { emitEvent: false });
  }

  isProfilAttached(inputProfile: ProfileDescription): boolean {
    return !!((inputProfile.controlSchema && inputProfile.controlSchema.length !== 2) || inputProfile.path);
  }
}
