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
import { Component, computed, effect, inject, OnDestroy, signal } from '@angular/core';
import { applyWhen, form, FormField, FormRoot, maxLength, minLength, required } from '@angular/forms/signals';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatRadioModule } from '@angular/material/radio';
import { allowedIdentifier, ApplicationService, Option, VitamUICommonModule, VitamUILibraryModule } from 'vitamui-library';
import { ProfileType } from '../../models/profile-type.enum';
import { ProfileVersion, ProfileVersionOptions } from '../../models/profile-version.enum';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ProfileService } from '../../core/services/profile.service';
import { Subscription } from 'rxjs';

import { Notice } from '../../models/notice.model';

import { PastisGenericPopupComponent } from '../../shared/pastis-generic-popup/pastis-generic-popup.component';

import { IdentifierExistsValidator } from '../../validators/IdentifierExistsValidator';

interface CreateProfilNoticeForm extends Notice {
  profileType: ProfileType;
  profileVersion: ProfileVersion | null;
  additionalProperties: boolean | null;
}

@Component({
  imports: [
    VitamUILibraryModule,
    FormRoot,
    FormField,
    VitamUICommonModule,
    PastisGenericPopupComponent,
    TranslatePipe,
    MatDialogModule,
    MatRadioModule,
  ],
  selector: 'app-create-profil-notice',
  templateUrl: './create-profil-notice.component.html',
})
export class CreateProfilNoticeComponent implements OnDestroy {
  private readonly profileService = inject(ProfileService);
  private readonly applicationService = inject(ApplicationService);
  private readonly translateService = inject(TranslateService);
  private readonly dialogRef = inject<MatDialogRef<CreateProfilNoticeComponent>>(MatDialogRef);
  private readonly identifierValidator = inject(IdentifierExistsValidator);

  readonly notice: Notice = {
    description: '',
    name: '',
    status: 'ACTIVE',
    identifier: '',
  };
  stepIndex = 0;
  readonly statusOptions: Option[] = [
    { key: 'ACTIVE', label: this.translateService.instant('PROFILE.POP_UP_CREATION_NOTICE.CHOICE.PROFIL_ACTIF') },
    { key: 'INACTIVE', label: this.translateService.instant('PROFILE.POP_UP_CREATION_NOTICE.CHOICE.PROFIL_INACTIF') },
  ];
  controlSchema: any;
  subscriptions = new Subscription();

  readonly externalIdentifierEnabledSignal = signal(false);

  readonly noticeForm = form(
    signal<CreateProfilNoticeForm>({
      profileType: ProfileType.PA,
      profileVersion: null,
      ...this.notice,
      additionalProperties: null,
    }),
    (path) => {
      required(path.profileType);
      required(path.profileVersion);
      applyWhen(
        path,
        () => this.externalIdentifierEnabledSignal(),
        (enabledPath) => {
          required(enabledPath.identifier);
          minLength(enabledPath.identifier, 2);
          maxLength(enabledPath.identifier, 100);
          allowedIdentifier(enabledPath.identifier);
        },
      );
      this.identifierValidator.identifierExists(path.identifier, () => this.modePUA());
      required(path.name);
    },
    {
      submission: {
        action: async () => {
          this.validate();
          return undefined;
        },
        ignoreValidators: 'all',
      },
    },
  );

  readonly modePUA = computed(() => this.noticeForm.profileType().value() === ProfileType.PUA);

  readonly ProfileType = ProfileType;
  readonly ProfileVersionOptions = ProfileVersionOptions;

  constructor() {
    // Checks whether the external identifier is enabled for the selected profile type
    effect((onCleanup) => {
      const subscription = this.applicationService
        .isApplicationExternalIdentifierEnabled(this.modePUA() ? 'ARCHIVE_UNIT_PROFILE' : 'PROFILE')
        .subscribe((enabled) => this.externalIdentifierEnabledSignal.set(enabled));
      onCleanup(() => subscription.unsubscribe());
    });
  }

  isStepOneValid(): boolean {
    return this.noticeForm.profileType().valid() && this.noticeForm.profileVersion().valid();
  }

  setProfileType(profileType: string) {
    this.noticeForm.profileType().value.set(profileType as ProfileType);
    if (profileType === ProfileType.PUA) {
      this.controlSchema = JSON.parse(this.profileService.controlSchema.getValue());
      this.noticeForm.additionalProperties().value.set(this.controlSchema?.additionalProperties ?? null);
    }
  }

  onClose() {
    this.dialogRef.close(true);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  validate() {
    if (!this.externalIdentifierEnabledSignal()) {
      this.noticeForm.identifier().value.set(this.noticeForm.name().value());
    }
    if (this.noticeForm().invalid()) {
      return;
    }
    this.updateControlSchema();
    const data = this.noticeForm().value();
    this.dialogRef.close({
      success: true,
      action: 'none',
      data: { ...data, additionalProperties: data.additionalProperties ?? undefined },
    });
  }

  private updateControlSchema(): void {
    const additionalProps = this.noticeForm.additionalProperties().value() ?? undefined;

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
    return this.noticeForm().valid();
  }
}
