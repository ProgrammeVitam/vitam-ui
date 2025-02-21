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
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { ApplicationService, Option } from 'vitamui-library';
import { environment } from '../../../environments/environment';
import { FileService } from '../../core/services/file.service';
import { PopupService } from '../../core/services/popup.service';
import { ProfileService } from '../../core/services/profile.service';
import { ArchivalProfileUnit } from '../../models/archival-profile-unit';
import { Notice } from '../../models/notice.model';
import { Profile } from '../../models/profile';
import { ProfileType } from '../../models/profile-type.enum';
import { PastisDialogDataCreate } from '../save-profile/save-profile.component';
import { ProfileVersion } from '../../models/profile-version.enum';

const POPUP_CREATION_CHOICE_PATH = 'PROFILE.POP_UP_CREATION_NOTICE.CHOICE';

function constantToTranslate() {
  this.profilActif = this.translated('.PROFIL_ACTIF');
  this.profilInactif = this.translated('.PROFIL_INACTIF');
}

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'create-notice',
  templateUrl: './create-notice.component.html',
  styleUrls: ['./create-notice.component.scss'],
})
export class CreateNoticeComponent implements OnInit, OnDestroy {
  form: FormGroup;
  btnIsDisabled: boolean;
  notice: Notice;
  // edit or new notice
  editNotice: boolean;
  titleDialog: string;
  subTitleDialog: string;
  okLabel: string;
  cancelLabel: string;
  statusOptions: Option[];
  profileType?: ProfileType;
  profileVersion?: ProfileVersion;
  modePUA: boolean;
  information: string;
  presenceNonDeclareMetadonneesPUAControl = new FormControl(false);
  profilActif: string;
  profilInactif: string;
  validate: boolean;

  isStandalone: boolean = environment.standalone;

  subscriptions = new Subscription();
  externalIdentifierEnabled: boolean;

  constructor(
    public dialogRef: MatDialogRef<CreateNoticeComponent>,
    @Inject(MAT_DIALOG_DATA) public data: PastisDialogDataCreate,
    private formBuilder: FormBuilder,
    private translateService: TranslateService,
    private popupService: PopupService,
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
    this.applicationService
      .isApplicationExternalIdentifierEnabled(this.profileType === ProfileType.PUA ? 'ARCHIVE_UNIT_PROFILE' : 'PROFILE')
      .subscribe((value) => {
        this.externalIdentifierEnabled = value;
      });
    this.editNotice = this.router.url.substring(this.router.url.lastIndexOf('/') - 4, this.router.url.lastIndexOf('/')) === 'edit';
    if (this.editNotice) {
      this.validate = true;
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

    if (!this.isStandalone) {
      constantToTranslate.call(this);
      this.translatedOnChange();
    } else if (this.isStandalone) {
      this.profilActif = 'Profil actif';
      this.profilInactif = 'Profil inactif';
    }
    this.statusOptions = [
      { key: 'INACTIVE', label: this.profilInactif },
      { key: 'ACTIVE', label: this.profilActif },
    ];
    this.information = "texte d'information";
    this.form = this.formBuilder.group({
      identifier: [null, Validators.required],
      intitule: [null, Validators.required],
      selectedStatus: [null],
      description: [null],
      autoriserPresenceMetadonnees: false,
    });

    this.subscriptions.add(
      this.presenceNonDeclareMetadonneesPUAControl.valueChanges.subscribe((value) => {
        this.form.controls.autoriserPresenceMetadonnees.setValue(value);
      }),
    );

    // Subscribe observer to button status and
    // set the inital state of the ok button to disabled

    this.subscriptions.add(
      this.popupService.btnYesShoudBeDisabled.subscribe((status) => {
        this.btnIsDisabled = status;
      }),
    );
  }

  translatedOnChange(): void {
    this.subscriptions.add(
      this.translateService.onLangChange.subscribe((_: LangChangeEvent) => {
        constantToTranslate.call(this);
      }),
    );
  }

  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(POPUP_CREATION_CHOICE_PATH + nameOfFieldToTranslate);
  }

  onCancel() {
    this.dialogRef.close();
  }

  upateButtonStatusAndDataToSend() {
    this.popupService.setPopUpDataOnClose('test');
    this.popupService.disableYesButton(true);
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  // eslint-disable-next-line @angular-eslint/use-lifecycle-interface
  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  checkIdentifier(modePUA: boolean) {
    if (this.notice.identifier.length < 1) {
      this.validate = false;
      return;
    }
    if (modePUA) {
      const archivalProfileUnit = {} as ArchivalProfileUnit;
      archivalProfileUnit.identifier = this.notice.identifier;
      this.subscriptions.add(
        this.profileService.checkPuaProfile(archivalProfileUnit).subscribe((response: boolean) => {
          if (response) {
            alert('Identifier already exists use another identifier');
            this.validate = false;
          } else {
            this.validate = true;
            this.checkIntitule();
          }
        }),
      );
    } else {
      const profile = {} as Profile;
      profile.identifier = this.notice.identifier;
      this.subscriptions.add(
        this.profileService.checkPaProfile(profile).subscribe((response: boolean) => {
          if (response) {
            alert('Identifier already exists use another identifier');
            this.validate = false;
          } else {
            this.validate = true;
            this.checkIntitule();
          }
        }),
      );
    }
  }

  checkIntitule() {
    this.validate = this.notice.name.length !== 0;
  }

  onSubmit() {
    if (!this.externalIdentifierEnabled) {
      this.form.controls.identifier.setValue(this.form.controls.intitule.value);
    }
    if (this.form.invalid) {
      return;
    }
    if (this.editNotice) {
      this.fileService.noticeEditable.next(this.notice);
      this.fileService.setNotice(true);
    }
    this.dialogRef.close({
      success: true,
      action: 'none',
      data: this.form.value,
      profileType: this.profileType,
      profileVersion: this.profileVersion,
    });
  }
}
