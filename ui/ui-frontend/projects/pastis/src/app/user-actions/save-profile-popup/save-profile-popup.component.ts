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
import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ProfileType } from '../../models/profile-type.enum';
import { ApplicationService, MiscValidators, Option, VitamUICommonModule, VitamUILibraryModule } from 'vitamui-library';
import { Notice } from '../../models/notice.model';
import { ProfileService } from '../../core/services/profile.service';
import { Subscription } from 'rxjs';
import { ProfileDescription } from '../../models/profile-description.model';

import { PastisGenericPopupComponent } from '../../shared/pastis-generic-popup/pastis-generic-popup.component';

import { Router } from '@angular/router';
import { FileService } from '../../core/services/file.service';
import { IdentifierExistsValidator } from '../../validators/IdentifierExistsValidator';

@Component({
  imports: [
    VitamUILibraryModule,
    ReactiveFormsModule,
    FormsModule,
    VitamUICommonModule,
    PastisGenericPopupComponent,
    TranslatePipe,
    MatDialogModule,
  ],
  selector: 'app-save-profile-popup',
  templateUrl: './save-profile-popup.component.html',
})
export class SaveProfilePopupComponent implements OnInit, OnDestroy {
  private dialogRef = inject<MatDialogRef<SaveProfilePopupComponent>>(MatDialogRef);
  private fb = inject(FormBuilder);
  private translateService = inject(TranslateService);
  private profileService = inject(ProfileService);
  private applicationService = inject(ApplicationService);
  private fileService = inject(FileService);
  private router = inject(Router);
  private identifierValidator = inject(IdentifierExistsValidator);

  profileOptions: Option[];
  notice: Notice;
  selectedProfile: ProfileDescription;
  statusOptions: Option[];
  controlSchema: any;
  noticeForm: FormGroup;
  stepIndex = 0;
  firstChoiceEnregistrement: string;
  secondChoiceEnregistrement: string;
  firstChoiceGestionNotice: string;
  secondChoiceGestionNotice: string;
  titleGestionNotice: string;
  externalIdentifierEnabled: boolean;

  okLabel: string;
  editProfile: boolean;
  valueSelected = signal(false);
  gestionNotice = signal(true);
  userValidation = signal(false);
  modePUA: boolean;
  subscriptions = new Subscription();

  constructor() {
    this.editProfile = this.router.url.substring(this.router.url.lastIndexOf('/') - 4, this.router.url.lastIndexOf('/')) === 'edit';
  }

  ngOnInit(): void {
    this.modePUA = this.profileService.profileType === ProfileType.PUA;
    this.firstChoiceEnregistrement = this.translateService.instant('PROFILE.POP_UP_SAVE.CHOICE.FIRST_CHOICE_ENREGISTREMENT');
    this.secondChoiceEnregistrement = this.translateService.instant('PROFILE.POP_UP_SAVE.CHOICE.SECOND_CHOICE_ENREGISTREMENT');
    this.firstChoiceGestionNotice = this.translateService.instant('PROFILE.POP_UP_SAVE.CHOICE.FIRST_CHOICE_GESTION_NOTICE');
    this.secondChoiceGestionNotice = this.translateService.instant('PROFILE.POP_UP_SAVE.CHOICE.SECOND_CHOICE_GESTION_NOTICE');
    this.titleGestionNotice = this.translateService.instant('PROFILE.POP_UP_SAVE.CHOICE.TITLE_GESTION_NOTICE');

    if (this.editProfile) {
      this.subscriptions.add(
        this.fileService.noticeEditable.subscribe((value: Notice) => {
          this.notice = value;
        }),
      );
    } else {
      this.notice = {
        description: '',
        name: '',
        status: 'ACTIVE',
        identifier: '',
      };
    }
    this.statusOptions = [
      { key: 'ACTIVE', label: this.translateService.instant('PROFILE.POP_UP_CREATION_NOTICE.CHOICE.PROFIL_ACTIF') },
      { key: 'INACTIVE', label: this.translateService.instant('PROFILE.POP_UP_CREATION_NOTICE.CHOICE.PROFIL_INACTIF') },
    ];

    if (this.profileService.profileType === ProfileType.PUA) this.controlSchema = JSON.parse(this.profileService.controlSchema.getValue());

    this.noticeForm = this.fb.group({
      identifier: [
        { value: this.notice.identifier, disabled: this.editProfile },
        [Validators.required, Validators.minLength(2), Validators.maxLength(100), MiscValidators.allowedIdentifier],
        !this.editProfile ? [this.identifierValidator.checkIdentifierExists(this.modePUA)] : [],
      ],
      name: [this.notice.name, Validators.required],
      status: [this.notice.status],
      description: [this.notice.description],
      additionalProperties: [this.controlSchema?.additionalProperties],
    });

    this.setUpIdentifierValidator();
    this.getProfileOptions();
  }

  changeStatus($event: string) {
    this.valueSelected.set($event === this.secondChoiceEnregistrement);
  }

  changeStatusGestionNoticeProfil($event: string) {
    this.gestionNotice.set($event === this.firstChoiceGestionNotice);
  }

  validate() {
    if (!this.valueSelected()) {
      this.dialogRef.close({ success: true, action: 'local' });
      return;
    }

    const action = this.gestionNotice() ? 'creation' : 'rattachement';
    if (this.gestionNotice()) {
      if (!this.externalIdentifierEnabled && !this.editProfile) {
        this.noticeForm.controls['identifier'].setValue(this.noticeForm.controls['name'].value);
      }
      if (this.noticeForm.invalid) {
        return;
      }

      if (this.editProfile) {
        this.fileService.noticeEditable.next(this.noticeForm.getRawValue());
        this.fileService.setNotice(true);
      }
      this.updateControlSchema();
      this.dialogRef.close({
        success: true,
        action,
        data: this.noticeForm.getRawValue(),
      });
      return;
    }
    this.dialogRef.close({
      success: true,
      action,
      data: this.selectedProfile,
    });
  }

  moveToNextStep() {
    this.stepIndex = this.stepIndex + 1;
  }

  isFormValid(): boolean {
    return this.noticeForm.valid;
  }

  onClose() {
    this.dialogRef.close(true);
  }

  setUserValidation(bool: boolean) {
    this.userValidation.set(bool);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private updateControlSchema(): void {
    const additionalProps = this.noticeForm.controls['additionalProperties'].value;

    const updatedPatternProperties = {
      ...this.controlSchema?.patternProperties,
      '#management': {
        ...this.controlSchema?.patternProperties?.['#management'],
        additionalProperties: additionalProps,
      },
    };

    this.controlSchema = this.controlSchema
      ? {
          ...this.controlSchema,
          additionalProperties: additionalProps,
          patternProperties: updatedPatternProperties,
        }
      : {
          ...this.controlSchema,
          additionalProperties: additionalProps,
        };

    this.profileService.controlSchema.next(JSON.stringify(this.controlSchema));
  }

  private getProfileOptions() {
    const mapProfileDescriptionsToOptions = (profileListPUA: ProfileDescription[]) =>
      (this.profileOptions = profileListPUA.map((profile: ProfileDescription) => ({
        key: profile,
        label: `${profile.identifier} - ${profile.name}`,
      })));
    if (this.profileService.profileType === ProfileType.PUA) {
      this.profileService.getAllProfilesPUA(this.profileService.profileVersion).subscribe(mapProfileDescriptionsToOptions);
    } else if (this.profileService.profileType === ProfileType.PA) {
      this.profileService.getAllProfilesPA(this.profileService.profileVersion).subscribe(mapProfileDescriptionsToOptions);
    }
  }

  private setUpIdentifierValidator() {
    this.applicationService
      .isApplicationExternalIdentifierEnabled(this.profileService.profileType === ProfileType.PUA ? 'ARCHIVE_UNIT_PROFILE' : 'PROFILE')
      .subscribe((value) => {
        const identifierCtrl = this.noticeForm.controls['identifier'];

        this.externalIdentifierEnabled = value;

        if (!this.externalIdentifierEnabled || this.editProfile) {
          identifierCtrl.clearValidators();
        } else {
          identifierCtrl.setValidators([
            Validators.required,
            Validators.minLength(2),
            Validators.maxLength(100),
            MiscValidators.allowedIdentifier,
          ]);
        }

        identifierCtrl.updateValueAndValidity();
      });
  }
}
