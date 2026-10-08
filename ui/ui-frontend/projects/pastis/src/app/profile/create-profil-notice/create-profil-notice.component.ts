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
import { Component, computed, effect, inject, Injector, OnDestroy, OnInit, signal } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatRadioModule } from '@angular/material/radio';
import { ApplicationService, MiscValidators, Option, VitamUICommonModule, VitamUILibraryModule } from 'vitamui-library';
import { ProfileType } from '../../models/profile-type.enum';
import { ProfileVersionOptions } from '../../models/profile-version.enum';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ProfileService } from '../../core/services/profile.service';
import { Subscription } from 'rxjs';

import { Notice } from '../../models/notice.model';

import { PastisGenericPopupComponent } from '../../shared/pastis-generic-popup/pastis-generic-popup.component';

import { IdentifierExistsValidator } from '../../validators/IdentifierExistsValidator';

@Component({
  imports: [
    VitamUILibraryModule,
    ReactiveFormsModule,
    VitamUICommonModule,
    PastisGenericPopupComponent,
    TranslatePipe,
    MatDialogModule,
    MatRadioModule,
  ],
  selector: 'app-create-profil-notice',
  templateUrl: './create-profil-notice.component.html',
})
export class CreateProfilNoticeComponent implements OnInit, OnDestroy {
  private profileService = inject(ProfileService);
  private applicationService = inject(ApplicationService);
  private translateService = inject(TranslateService);
  private fb = inject(FormBuilder);
  private injector = inject(Injector);
  private dialogRef = inject<MatDialogRef<CreateProfilNoticeComponent>>(MatDialogRef);
  private identifierValidator = inject(IdentifierExistsValidator);

  notice: Notice;
  noticeForm: FormGroup;
  stepIndex = 0;
  statusOptions: Option[];
  profileType: ProfileType;
  controlSchema: any;
  subscriptions = new Subscription();

  profileTypeSignal = signal(ProfileType.PA);
  externalIdentifierEnabledSignal = signal(false);
  readonly modePUA = computed(() => this.profileTypeSignal() === ProfileType.PUA);

  readonly ProfileType = ProfileType;
  readonly ProfileVersionOptions = ProfileVersionOptions;
  identifierControl: FormControl;

  ngOnInit() {
    this.notice = {
      description: '',
      name: '',
      status: 'ACTIVE',
      identifier: '',
    };
    this.statusOptions = [
      { key: 'ACTIVE', label: this.translateService.instant('PROFILE.POP_UP_CREATION_NOTICE.CHOICE.PROFIL_ACTIF') },
      { key: 'INACTIVE', label: this.translateService.instant('PROFILE.POP_UP_CREATION_NOTICE.CHOICE.PROFIL_INACTIF') },
    ];

    this.noticeForm = this.fb.group({
      profileType: [ProfileType.PA, Validators.required],
      profileVersion: [null, Validators.required],
      identifier: [
        this.notice.identifier,
        [Validators.required, Validators.minLength(2), Validators.maxLength(100), MiscValidators.allowedIdentifier],
        [this.identifierValidator.checkIdentifierExists(() => this.modePUA())],
      ],
      name: [this.notice.name, Validators.required],
      status: [this.notice.status],
      description: [this.notice.description],
      additionalProperties: [this.controlSchema?.additionalProperties],
    });

    this.identifierControl = this.noticeForm.get('noticeDetails.identifier') as FormControl;

    this.setupIdentifierValidationEffect();
    this.handleProfileTypeChange();
  }

  isStepOneValid(): any {
    const profileType = this.noticeForm.controls['profileType'];
    const profileVersion = this.noticeForm.controls['profileVersion'];
    return profileType.valid && profileVersion.valid;
  }

  onClose() {
    this.dialogRef.close(true);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  validate() {
    if (!this.externalIdentifierEnabledSignal) {
      this.noticeForm.controls['identifier'].setValue(this.noticeForm.controls['name'].value);
    }
    if (this.noticeForm.invalid) {
      return;
    }
    this.updateControlSchema();
    this.dialogRef.close({
      success: true,
      action: 'none',
      data: this.noticeForm.getRawValue(),
    });
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

  isFormValid(): boolean {
    return this.noticeForm.valid;
  }

  private setupIdentifierValidationEffect(): void {
    // Track the changes in the profileType and externalIdentifierEnabled signals.
    effect(
      () => {
        const isPUA = this.modePUA();
        const isEnabled = this.externalIdentifierEnabledSignal();
        const identifierCtrl = this.noticeForm.controls['identifier'];
        const appType = isPUA ? 'ARCHIVE_UNIT_PROFILE' : 'PROFILE';

        // check for external identifier
        this.subscriptions.add(
          this.applicationService.isApplicationExternalIdentifierEnabled(appType).subscribe((enabled) => {
            this.externalIdentifierEnabledSignal.set(enabled);
          }),
        );

        if (isEnabled) {
          identifierCtrl.setValidators([
            Validators.required,
            Validators.minLength(2),
            Validators.maxLength(100),
            MiscValidators.allowedIdentifier,
          ]);
        } else {
          identifierCtrl.clearValidators();
        }

        identifierCtrl.updateValueAndValidity();
      },
      { injector: this.injector },
    );
  }

  private handleProfileTypeChange(): void {
    this.subscriptions.add(
      this.noticeForm.get('profileType')?.valueChanges.subscribe((value: ProfileType) => {
        this.profileTypeSignal.set(value);
        if (value === ProfileType.PUA) {
          this.controlSchema = JSON.parse(this.profileService.controlSchema.getValue());
          this.noticeForm.controls['additionalProperties'].setValue(this.controlSchema?.additionalProperties);
        }
      }),
    );
  }
}
