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
import { AfterViewInit, Component, EventEmitter, HostListener, Input, Output, ViewChild, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTab, MatTabGroup, MatTabHeader } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { ConfirmActionComponent } from '../../../../../vitamui-library/src/lib/components/confirm-action/confirm-action.component';
import { environment } from '../../../environments/environment';
import { PastisConfiguration } from '../../core/classes/pastis-configuration';
import { ProfileService } from '../../core/services/profile.service';
import { FileNode } from '../../models/file-node';
import { ProfileDescription } from '../../models/profile-description.model';
import { ProfileResponse } from '../../models/profile-response';
import { ProfileType } from '../../models/profile-type.enum';
import { ProfileInformationTabComponent } from './profile-information-tab/profile-information-tab/profile-information-tab.component';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'profile-preview',
  templateUrl: './profile-preview.component.html',
  styleUrls: ['./profile-preview.component.scss'],
  standalone: false,
})
export class ProfilePreviewComponent implements AfterViewInit {
  private matDialog = inject(MatDialog);
  private router = inject(Router);
  private pastisConfig = inject(PastisConfiguration);
  private profileService = inject(ProfileService);
  private route = inject(ActivatedRoute);

  @Output()
  previewClose: EventEmitter<any> = new EventEmitter();
  @Input()
  inputProfile: ProfileDescription;

  tabUpdated: boolean[] = [false, false];
  isClicked = false;
  isStandalone: boolean = environment.standalone;

  fileNode: FileNode[] = [];

  isPopup: boolean;
  @ViewChild('tabs', { static: false }) tabs: MatTabGroup;

  tabLinks: Array<ProfileInformationTabComponent> = [];
  @ViewChild('infoTab', { static: false }) infoTab: ProfileInformationTabComponent;

  @HostListener('window:beforeunload', ['$event'])
  beforeunloadHandler(event: any) {
    if (this.tabUpdated[this.tabs.selectedIndex]) {
      event.preventDefault();
      this.checkBeforeExit();
      return '';
    }
  }

  ngAfterViewInit() {
    this.tabs._handleClick = this.interceptTabChange.bind(this);
    this.tabLinks[0] = this.infoTab;
  }

  updatedChange(updated: boolean, index: number) {
    this.tabUpdated[index] = updated;
  }

  closeNotice(updated: boolean) {
    if (updated) {
      this.emitClose();
    }
  }

  async checkBeforeExit() {
    if (await this.confirmAction()) {
      const submitProfileUpdate: Observable<ProfileDescription> = this.tabLinks[this.tabs.selectedIndex].updateProfile(
        this.inputProfile,
      ) as Observable<ProfileDescription>;

      submitProfileUpdate.subscribe(() => {});
    } else {
      this.tabLinks[this.tabs.selectedIndex].resetForm(this.inputProfile);
    }
  }

  async interceptTabChange(tab: MatTab, tabHeader: MatTabHeader, idx: number) {
    if (this.tabUpdated[this.tabs.selectedIndex]) {
      await this.checkBeforeExit();
    }

    const args = [tab, tabHeader, idx];
    return MatTabGroup.prototype._handleClick.apply(this.tabs, args);
  }

  async confirmAction(): Promise<boolean> {
    const dialog = this.matDialog.open(ConfirmActionComponent, { panelClass: 'small' });
    dialog.componentInstance.dialogType = 'changeTab';
    return await dialog.afterClosed().toPromise();
  }

  async emitClose() {
    if (this.tabUpdated[this.tabs.selectedIndex]) {
      await this.checkBeforeExit();
    }
    this.previewClose.emit();
  }

  isProfilAttached() {
    if ((this.inputProfile.controlSchema && this.inputProfile.controlSchema.length !== 2) || this.inputProfile.path) {
      // console.log(this.inputProfile)
      return true;
    }
  }

  onButtonClicked() {
    this.isClicked = !this.isClicked;
  }

  editProfile(inputProfile: ProfileDescription) {
    this.router.navigate([this.pastisConfig.pastisEditPage, inputProfile.id], {
      state: inputProfile,
      relativeTo: this.route,
      skipLocationChange: false,
    });
  }

  downloadProfile(inputProfile: ProfileDescription) {
    if (inputProfile.type === ProfileType.PA) {
      this.profileService.downloadProfilePaVitam(inputProfile.identifier).subscribe((dataFile) => {
        if (dataFile) {
          this.downloadFile(dataFile, inputProfile.type, inputProfile);
        }
      });
    } else if (inputProfile.type === ProfileType.PUA) {
      // Send the retrieved JSON data to profile service
      this.profileService.getProfile(inputProfile).subscribe((retrievedData) => {
        const profileResponse = retrievedData as ProfileResponse;
        this.fileNode.push(profileResponse.profile);
        this.profileService
          .uploadFile(this.fileNode, profileResponse.notice, inputProfile.type, inputProfile.sedaVersion)
          .subscribe((data) => {
            this.downloadFile(data, inputProfile.type, inputProfile);
          });
      });
    }
  }

  downloadFile(dataFile: any, typeProfile: string, inputProfile?: ProfileDescription): void {
    const typeFile = typeProfile === ProfileType.PA ? 'application/xml' : 'application/json';
    const newBlob = new Blob([dataFile], { type: typeFile });
    const data = window.URL.createObjectURL(newBlob);
    const link = document.createElement('a');
    link.href = data;
    link.download = typeProfile === ProfileType.PA ? inputProfile.path : 'pastis_' + inputProfile.identifier + '.json';
    // this is necessary as link.click() does not work on the latest firefox
    link.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, view: window }));
    setTimeout(() => {
      // For Firefox it is necessary to delay revoking the ObjectURL
      window.URL.revokeObjectURL(data);
      link.remove();
    }, 100);
  }
}
