import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import {Observable, of} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {diff} from 'ui-frontend-common';
import {NotificationService} from '../../../../core/services/notification.service';
import {ProfileService} from '../../../../core/services/profile.service';
import {ArchivalProfileUnit} from '../../../../models/archival-profile-unit';
import {Profile} from '../../../../models/profile';
import {ProfileDescription} from '../../../../models/profile-description.model';

@Component({
  selector: 'profile-information-tab',
  templateUrl: './profile-information-tab.component.html',
  styleUrls: ['./profile-information-tab.component.scss']
})
export class ProfileInformationTabComponent implements OnInit {

  @Input()
  set inputProfile(profileDescription: ProfileDescription) {
    this._inputProfile = profileDescription;
    this.statusProfile.setValue(this.inputProfile.status === 'INACTIVE' ? false : true)
    this.resetForm(this.inputProfile);
    this.updated.emit(false);
  }

  get inputProfile(): ProfileDescription {
    return this._inputProfile;
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


  constructor(private formBuilder: FormBuilder,
              private profileService: ProfileService, private loggingService: NotificationService,
                private translateService: TranslateService) {
    this.form = this.formBuilder.group({
      identifier: [null, Validators.required],
      id: [null, Validators.required],
      type: [null],
      description: [null],
      name: [null, Validators.required],
      creationDate: [null],
      status: [null, Validators.required]
    });

    this.statusProfile.valueChanges.subscribe((value) => {
      this.form.controls.status.setValue(value = (value === false) ? 'INACTIVE' : 'ACTIVE');
    });
  }


  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() close: EventEmitter<boolean> = new EventEmitter<boolean>();
  form: FormGroup;

  statusProfile = new FormControl();

  submited = false;

  isProfileAttache: boolean;

  typeProfile: string;

  archivalProfileUnit: ArchivalProfileUnit;

  profile: Profile;


  private _inputProfile: ProfileDescription;
  pending = false;

  previousValue = (): ProfileDescription => {
    return this._inputProfile;
  }

  isInvalid(): boolean {
    return false;
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  prepareSubmit(inputProfile: ProfileDescription): Observable<ProfileDescription> {
    console.log(JSON.stringify(inputProfile) + ' inputProfile');

    console.log(this.form.getRawValue());

    // let diffValue = diff(this.form.getRawValue(), this.previousValue());


    if (inputProfile.type == 'PA') {
      this.profile = this.form.value;
      console.log(JSON.stringify(this.profile));
      return this.profileService.updateProfilePa(this.profile).pipe(catchError(() => of(null)));
    } else {
      this.archivalProfileUnit = this.form.value;
      return this.profileService.updateProfilePua(this.archivalProfileUnit).pipe(catchError(() => of(null)));
    }
  }
  onSubmit() {
    this.submited = true;
    this.prepareSubmit(this.inputProfile).subscribe((result) => {
      this.submited = false;
      this.pending = !this.pending;
      this.inputProfile = this._inputProfile;
      console.log(JSON.stringify(result));
      this.loggingService.showSuccess(this.translateService.instant('PROFILE.LIST_PROFILE.PROFILE_PREVIEW.MODIFICATION_SUCCESS'));
      this.profileService.refreshListProfiles();
      this.close.emit(true);

    }, () => {
      this.submited = false;
      this.pending = !this.pending;
      this.loggingService.showSuccess('PROFILE.LIST_PROFILE.PROFILE_PREVIEW.MODIFICATION_ERROR');
    });
  }

  resetForm(profileDescription: ProfileDescription) {
    this.form.reset(profileDescription, {emitEvent: false});
  }

  ngOnInit(): void {
  }

  isProfilAttached(inputProfile: ProfileDescription): boolean {
    return !!(inputProfile.controlSchema && inputProfile.controlSchema.length != 2 || inputProfile.path);
  }

  enregistrement() {
    this.pending = !this.pending;
  }
}
