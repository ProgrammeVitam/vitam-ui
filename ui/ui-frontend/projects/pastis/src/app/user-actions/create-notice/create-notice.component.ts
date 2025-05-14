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
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { ApplicationService, Option } from 'vitamui-library';
import { FileService } from '../../core/services/file.service';
import { ProfileService } from '../../core/services/profile.service';
import { ArchivalProfileUnit } from '../../models/archival-profile-unit';
import { Notice } from '../../models/notice.model';
import { Profile } from '../../models/profile';
import { ProfileType } from '../../models/profile-type.enum';
import { PastisDialogDataCreate } from '../save-profile/save-profile.component';
import { ProfileVersion } from '../../models/profile-version.enum';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'create-notice',
  templateUrl: './create-notice.component.html',
  styleUrls: ['./create-notice.component.scss'],
  standalone: false,
})
export class CreateNoticeComponent implements OnInit, OnDestroy {
  form: FormGroup;
  notice: Notice;
  // edit or new notice
  editNotice = false;
  statusOptions: Option[];
  profileType: ProfileType;
  profileVersion: ProfileVersion;
  modePUA = false;
  controlSchema: any;

  subscriptions = new Subscription();
  externalIdentifierEnabled: boolean;
  private isIdentifierOK = true;

  constructor(
    public dialogRef: MatDialogRef<CreateNoticeComponent>,
    @Inject(MAT_DIALOG_DATA) public data: PastisDialogDataCreate,
    private formBuilder: FormBuilder,
    private translateService: TranslateService,
    private fileService: FileService,
    private router: Router,
    private profileService: ProfileService,
    private applicationService: ApplicationService,
  ) {}

  ngOnInit() {
    this.profileType = this.data.profileType;
    this.profileVersion = this.data.profileVersion;
    if (this.profileType === ProfileType.PUA) {
      this.modePUA = true;
    }
    this.editNotice = this.router.url.substring(this.router.url.lastIndexOf('/') - 4, this.router.url.lastIndexOf('/')) === 'edit';
    if (this.editNotice) {
      // Subscribe observer to notice
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

    this.controlSchema = JSON.parse(this.profileService.controlSchema.getValue());
    this.form = this.formBuilder.group({
      identifier: [{ value: this.notice.identifier, disabled: this.editNotice }, Validators.required],
      name: [this.notice.name, Validators.required],
      status: [this.notice.status],
      description: [this.notice.description],
      additionalProperties: [this.controlSchema?.additionalProperties],
    });

    this.applicationService
      .isApplicationExternalIdentifierEnabled(this.profileType === ProfileType.PUA ? 'ARCHIVE_UNIT_PROFILE' : 'PROFILE')
      .subscribe((value) => {
        this.externalIdentifierEnabled = value;
        if (this.editNotice || !this.externalIdentifierEnabled) {
          this.form.controls.identifier.clearValidators();
          this.form.controls.identifier.updateValueAndValidity();
        }
      });

    if (!this.editNotice) {
      this.subscriptions.add(
        this.form.controls.identifier.valueChanges
          .pipe(debounceTime(300), distinctUntilChanged())
          .subscribe((identifier: string) => this.checkIdentifier(identifier)),
      );
    }
  }

  onCancel() {
    this.dialogRef.close();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  checkIdentifier(identifier: string) {
    if (identifier.length === 0) {
      return;
    }
    if (this.modePUA) {
      const archivalProfileUnit = { identifier: identifier } as ArchivalProfileUnit;
      this.subscriptions.add(
        this.profileService.checkPuaProfile(archivalProfileUnit).subscribe((response: boolean) => {
          if (response) {
            this.isIdentifierOK = false;
            alert('Identifier already exists use another identifier');
          } else {
            this.isIdentifierOK = true;
          }
        }),
      );
    } else {
      const profile = { identifier: identifier } as Profile;
      this.subscriptions.add(
        this.profileService.checkPaProfile(profile).subscribe((response: boolean) => {
          if (response) {
            this.isIdentifierOK = false;
            alert('Identifier already exists use another identifier');
          } else {
            this.isIdentifierOK = true;
          }
        }),
      );
    }
  }

  isFormValid(): boolean {
    return this.form.valid && this.isIdentifierOK;
  }

  onSubmit() {
    if (!this.externalIdentifierEnabled && !this.editNotice) {
      this.form.controls.identifier.setValue(this.form.controls.name.value);
    }
    if (this.form.invalid) {
      return;
    }
    if (this.editNotice) {
      this.fileService.noticeEditable.next(this.form.getRawValue());
      this.fileService.setNotice(true);
    }
    this.updateControlSchema();
    this.dialogRef.close({
      success: true,
      action: 'none',
      data: this.form.getRawValue(),
      profileType: this.profileType,
      profileVersion: this.profileVersion,
    });
  }

  private updateControlSchema(): void {
    const additionalProps = this.form.controls.additionalProperties.value;

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
}
