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
import { Component, OnInit, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { ProfileService } from '../../core/services/profile.service';
import { ProfileDescription } from '../../models/profile-description.model';
import { ProfileType } from '../../models/profile-type.enum';
import { PastisDialogDataCreate } from '../save-profile/save-profile.component';
import { Option } from '../../../../../vitamui-library/src/app/modules/components/autocomplete/utils/option.interface';

const POPUP_CREATION_CHOICE_PATH = 'PROFILE.POP_UP_CREATION_NOTICE.CHOICE';

function constantToTranslate() {
  this.profilActif = this.translated('.PROFIL_ACTIF');
  this.profilInactif = this.translated('.PROFIL_INACTIF');
}

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'select-notice',
  templateUrl: './select-notice.component.html',
  styleUrls: ['./select-notice.component.scss'],
  standalone: false,
})
export class SelectNoticeComponent implements OnInit {
  dialogRef = inject<MatDialogRef<SelectNoticeComponent>>(MatDialogRef);
  data = inject<PastisDialogDataCreate>(MAT_DIALOG_DATA);
  private translateService = inject(TranslateService);
  private profilService = inject(ProfileService);

  profileOptions: Option[];
  selectedProfile: ProfileDescription;
  userValidation = false;

  ngOnInit(): void {
    const mapProfileDescriptionsToOptions = (profileListPUA: ProfileDescription[]) =>
      (this.profileOptions = profileListPUA.map((profile: ProfileDescription) => ({
        key: profile,
        label: `${profile.identifier} - ${profile.name}`,
      })));
    if (this.data.profileType === ProfileType.PUA) {
      this.profilService.getAllProfilesPUA(this.data.profileVersion).subscribe(mapProfileDescriptionsToOptions);
    } else if (this.data.profileType === ProfileType.PA) {
      this.profilService.getAllProfilesPA(this.data.profileVersion).subscribe(mapProfileDescriptionsToOptions);
    }
  }

  translatedOnChange(): void {
    this.translateService.onLangChange.subscribe((_: LangChangeEvent) => {
      constantToTranslate.call(this);
    });
  }

  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(POPUP_CREATION_CHOICE_PATH + nameOfFieldToTranslate);
  }

  onSubmit() {
    this.dialogRef.close({
      success: true,
      action: 'none',
      data: this.selectedProfile,
      profileType: this.data.profileType,
      profileVersion: this.data.profileVersion,
    });
  }

  onCancel() {
    this.dialogRef.close();
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  setUserValidation(bool: boolean) {
    this.userValidation = bool;
  }
}
