/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2020)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/
import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { Router } from '@angular/router';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { StartupService, VitamUISnackBarComponent } from 'vitamui-library';
import { environment } from '../../../environments/environment';
import { FileService } from '../../core/services/file.service';
import { NoticeService } from '../../core/services/notice.service';
import { NotificationService } from '../../core/services/notification.service';
import { PopupService } from '../../core/services/popup.service';
import { ProfileService } from '../../core/services/profile.service';
import { ToggleSidenavService } from '../../core/services/toggle-sidenav.service';
import { ArchivalProfileUnit } from '../../models/archival-profile-unit';
import { FileNode } from '../../models/file-node';
import { Profile } from '../../models/profile';
import { ProfileDescription } from '../../models/profile-description.model';
import { ProfileType } from '../../models/profile-type.enum';
import { VitamUIExceptionResponse } from '../../models/vitam-ui-exception-response.model';
import { DataGeneriquePopupService } from '../../shared/data-generique-popup.service';
import { PastisDialogData } from '../../shared/pastis-dialog/classes/pastis-dialog-data';
import { CreateNoticeComponent } from '../create-notice/create-notice.component';
import { SaveProfileOptionsComponent } from '../save-profile-options/save-profile-options.component';
import { SelectNoticeComponent } from '../select-notice/select-notice.component';

export interface PastisDialogDataCreate {
  height: string;
  titleDialog: string;
  subTitleDialog: string;
  okLabel: string;
  cancelLabel: string;
  profileMode?: ProfileType;
  isSlaveMode?: boolean;
}

const POPUP_SAVE_PATH = 'PROFILE.POP_UP_SAVE';

function constantToTranslate(edit: boolean) {
  if (edit) {
    this.popupSaveCreateNoticeTitleDialog = this.translated('.SAVE_PROFILE.POPUP_CREATE_NOTICE_TITLE_DIALOG_EDIT');
    this.popupSaveCreateNoticeSubTitleDialog = this.translated('.SAVE_PROFILE.POPUP_CREATE_NOTICE_SUBTITLE_DIALOG_EDIT');
  } else {
    this.popupSaveCreateNoticeTitleDialog = this.translated('.SAVE_PROFILE.POPUP_CREATE_NOTICE_TITLE_DIALOG');
    this.popupSaveCreateNoticeSubTitleDialog = this.translated('.SAVE_PROFILE.POPUP_CREATE_NOTICE_SUBTITLE_DIALOG');
  }
  this.popupSaveCancelLabel = this.translated('.SAVE_PROFILE.POPUP_SAVE_CANCEL_LABEL');
  this.popupSaveTitleDialog = this.translated('.SAVE_PROFILE.POPUP_SAVE_TITLE_DIALOG');
  this.popupSaveSubTitleDialog = this.translated('.SAVE_PROFILE.POPUP_SAVE_SUBTITLE_DIALOG');
  this.popupSaveOkLabel = this.translated('.SAVE_PROFILE.POPUP_SAVE_OK_LABEL');
  this.popupSaveCreateNoticeCancelLabel = this.translated('.SAVE_PROFILE.POPUP_CREATE_NOTICE_CANCEL_LABEL');
  this.popupSaveCreateNoticeOkLabel = this.translated('.SAVE_PROFILE.POPUP_CREATE_NOTICE_OK_LABEL');
  this.popupSaveSelectNoticeTitleDialog = this.translated('.SAVE_PROFILE.POPUP_SELECT_NOTICE_TITLE_DIALOG');
}

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'pastis-user-action-save-profile',
  templateUrl: './save-profile.component.html',
  styleUrls: ['./save-profile.component.scss'],
})
export class UserActionSaveProfileComponent implements OnInit, OnDestroy {
  popupSaveCancelLabel: string;
  popupSaveTitleDialog: string;
  popupSaveSubTitleDialog: string;
  popupSaveOkLabel: string;

  popupSaveCreateNoticeCancelLabel: string;
  popupSaveCreateNoticeTitleDialog: string;
  popupSaveCreateNoticeSubTitleDialog: string;
  popupSaveCreateNoticeOkLabel: string;

  popupSaveSelectNoticeTitleDialog: string;

  isStandalone: boolean = environment.standalone;
  editProfile: boolean;

  data: FileNode[] = [];
  donnees: string[];

  subscriptions: Subscription = new Subscription();

  archivalProfileUnit: ArchivalProfileUnit;
  profile: Profile;

  profileDescription: ProfileDescription;
  isSlaveMode: boolean;
  fileRng: File;

  // eslint-disable-next-line @angular-eslint/no-output-native
  @Output() close = new EventEmitter();

  constructor(
    private profileService: ProfileService,
    private popupService: PopupService,
    private fileService: FileService,
    private startupService: StartupService,
    private snackBar: MatSnackBar,
    private dataGeneriquePopupService: DataGeneriquePopupService,
    private noticeService: NoticeService,
    private translateService: TranslateService,
    public dialog: MatDialog,
    private router: Router,
    private notificationService: NotificationService,
    private toggleService: ToggleSidenavService,
  ) {
    this.editProfile = this.router.url.substring(this.router.url.lastIndexOf('/') - 4, this.router.url.lastIndexOf('/')) === 'edit';
  }

  ngOnInit() {
    if (!this.isStandalone) {
      constantToTranslate.call(this, this.editProfile);
      this.translatedOnChange();
    } else if (this.isStandalone) {
      this.popupSaveCancelLabel = 'ANNULER';
      this.popupSaveTitleDialog = 'Sélectionner les options de votre enregistrement';
      this.popupSaveSubTitleDialog = 'Enregistrement';
      this.popupSaveOkLabel = 'VALIDER';
      this.popupSaveCreateNoticeCancelLabel = 'PRECEDENT';
      this.popupSaveCreateNoticeTitleDialog = 'Rédiger la notice de profil';
      this.popupSaveCreateNoticeSubTitleDialog = 'Enregistrement';
      this.popupSaveCreateNoticeOkLabel = 'TERMINER';
    }
    this.subscriptions.add(this.dataGeneriquePopupService.currentDonnee.subscribe((donnees) => (this.donnees = donnees)));
    this.isSlaveMode = this.popupService.externalIdentifierEnabled;
  }

  translatedOnChange(): void {
    this.subscriptions.add(
      this.translateService.onLangChange.subscribe((_: LangChangeEvent) => {
        constantToTranslate.call(this);
      }),
    );
  }

  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(POPUP_SAVE_PATH + nameOfFieldToTranslate);
  }

  saveProfileToFile() {
    // Retrieve the current file tree data as a JSON
    this.data = this.fileService.allData.getValue();
    if (this.isStandalone) {
      this.downloadProfiles(true);
      return;
    }

    const donnees = ['Local', 'SAE', "Où souhaitez-vous l'enregistrer ?"];
    this.dataGeneriquePopupService.changeDonnees(donnees);

    const pastisDialogData = {} as PastisDialogData;
    pastisDialogData.titleDialog = this.popupSaveTitleDialog;
    pastisDialogData.subTitleDialog = this.popupSaveSubTitleDialog;
    pastisDialogData.width = '800px';
    pastisDialogData.height = '800px';
    pastisDialogData.okLabel = this.popupSaveOkLabel;
    pastisDialogData.cancelLabel = this.popupSaveCancelLabel;
    const dialogRef = this.dialog.open(SaveProfileOptionsComponent, {
      width: '800px',
      panelClass: 'pastis-popup-modal-box',
      data: pastisDialogData,
    });
    this.subscriptions.add(
      dialogRef.afterClosed().subscribe((result) => {
        if (!result.success) {
          return;
        }
        if (result.action === 'local') {
          this.downloadProfiles(true);
        } else if (result.action === 'creation') {
          const modeProfile = this.profileService.profileMode;
          const createNoticeData = {} as PastisDialogDataCreate;
          createNoticeData.titleDialog = this.popupSaveCreateNoticeTitleDialog;
          createNoticeData.subTitleDialog = this.popupSaveCreateNoticeSubTitleDialog;
          createNoticeData.okLabel = this.popupSaveCreateNoticeOkLabel;
          createNoticeData.cancelLabel = this.popupSaveCreateNoticeCancelLabel;
          createNoticeData.profileMode = modeProfile;
          createNoticeData.isSlaveMode = this.isSlaveMode;
          this.dialog
            .open(CreateNoticeComponent, {
              width: '800px',
              panelClass: 'pastis-popup-modal-box',
              data: createNoticeData,
            })
            .afterClosed()
            .subscribe((createNoticeComponentResult) => {
              this.toggleService.showPending();
              let retour;
              if (createNoticeComponentResult.success) {
                retour = createNoticeComponentResult.data;
                if (createNoticeComponentResult.mode === ProfileType.PUA) {
                  if (!this.editProfile) {
                    this.profileDescription = Object.assign(
                      this.noticeService.profileFromNotice(retour, this.editProfile, true),
                      this.profileDescription,
                    );
                  } else {
                    this.subscriptions.add(
                      this.fileService.notice.subscribe((value: ProfileDescription) => {
                        this.profileDescription = value;
                      }),
                    );
                  }
                  this.subscriptions.add(
                    this.profileService
                      .uploadFile(this.data, this.profileDescription, createNoticeComponentResult.mode)
                      .subscribe((retrievedData) => {
                        retrievedData.text().then((result) => {
                          const jsonObject = JSON.parse(result);
                          this.archivalProfileUnit = jsonObject as unknown as ArchivalProfileUnit;
                          // Create ro update existing PUA
                          if (!this.editProfile) {
                            this.profileService.createArchivalUnitProfile(this.archivalProfileUnit).subscribe(() => {
                              this.toggleService.hidePending();
                              this.success('La création du profil a bien été effectué');
                            });
                          } else {
                            this.profileService.updateProfilePua(this.archivalProfileUnit).subscribe(
                              () => {
                                this.toggleService.hidePending();
                                this.success('La modification du profil a bien été effectué');
                              },
                              (error) => {
                                this.toggleService.hidePending();
                                const vitamUIException: VitamUIExceptionResponse = error.error;
                                this.displaySnackBar(vitamUIException);
                              },
                            );
                          }
                        });
                      }),
                  );
                } else if (createNoticeComponentResult.mode === ProfileType.PA) {
                  const profile: Profile = this.noticeService.paNotice(retour, true);
                  if (!this.editProfile) {
                    // CREER NOTICE PUIS ASSIGNER LE PROFIL A LA NOTICE
                    this.profile = Object.assign(profile, this.profile);
                    this.profileDescription = Object.assign(
                      this.noticeService.profileFromNotice(retour, this.editProfile, false),
                      this.profileDescription,
                    );
                  } else {
                    this.subscriptions.add(
                      this.fileService.notice.subscribe((value: ProfileDescription) => {
                        this.profile = Object.assign(profile, value);
                        this.profileDescription = value;
                      }),
                    );
                  }
                  // STEP 1 : Create or update Notice
                  this.savePA();
                }
              }
            });
        } else if (result.action === 'rattachement') {
          // eslint-disable-next-line @typescript-eslint/no-shadow
          const dataToSendToPopUp = {} as PastisDialogDataCreate;
          dataToSendToPopUp.titleDialog = this.popupSaveSelectNoticeTitleDialog;
          dataToSendToPopUp.subTitleDialog = this.popupSaveCreateNoticeSubTitleDialog;
          dataToSendToPopUp.okLabel = this.popupSaveCreateNoticeOkLabel;
          dataToSendToPopUp.cancelLabel = this.popupSaveCreateNoticeCancelLabel;
          dataToSendToPopUp.profileMode = this.profileService.profileMode;
          // eslint-disable-next-line @typescript-eslint/no-shadow
          const selectNoticeDialog = this.dialog.open(SelectNoticeComponent, {
            width: '800px',
            panelClass: 'pastis-popup-modal-box',
            data: dataToSendToPopUp,
          });
          // eslint-disable-next-line @typescript-eslint/no-shadow
          this.subscriptions.add(
            selectNoticeDialog.afterClosed().subscribe((selectNoticeResult) => {
              this.toggleService.showPending();
              let profileDescription: ProfileDescription;
              if (selectNoticeResult.success) {
                profileDescription = selectNoticeResult.data;
                if (selectNoticeResult.mode === ProfileType.PUA) {
                  this.subscriptions.add(
                    this.profileService.uploadFile(this.data, profileDescription, selectNoticeResult.mode).subscribe((retrievedData) => {
                      // eslint-disable-next-line @typescript-eslint/no-shadow
                      retrievedData.text().then((result) => {
                        const jsonObject = JSON.parse(result);
                        this.archivalProfileUnit = jsonObject as unknown as ArchivalProfileUnit;
                        //  update existing PUA
                        this.subscriptions.add(
                          this.profileService.updateProfilePua(this.archivalProfileUnit).subscribe(
                            () => {
                              this.toggleService.hidePending();
                              this.success('La modification du profil a bien été effectué');
                            },
                            (_) => {
                              this.toggleService.hidePending();
                            },
                          ),
                        );
                      });
                    }),
                  );
                } else if (selectNoticeResult.mode === ProfileType.PA) {
                  this.profileService.uploadFile(this.data, profileDescription, selectNoticeResult.mode).subscribe((retrievedData) => {
                    const myFile = this.blobToFile(retrievedData, 'file');
                    this.profileService
                      .updateProfileFilePa(this.noticeService.paNotice(profileDescription, false), myFile)
                      .subscribe(() => {
                        this.toggleService.hidePending();
                        this.success('La modification du profil a bien été effectué');
                      });
                  });
                }
              }
            }),
          );
        }
      }),
    );
  }

  displaySnackBar(vitamUIException: VitamUIExceptionResponse) {
    if (!vitamUIException || !vitamUIException.args || vitamUIException.args.length < 1) {
      return;
    }
    this.snackBar.openFromComponent(VitamUISnackBarComponent, {
      panelClass: 'vitamui-snack-bar',
      data: {
        type: 'otherType',
        messageKey: 'SNACKBAR.ERROR_WITH_LOGBOOK_OPERATION_ID',
        buttonAction: () => this.goToOperation(vitamUIException.args[0]),
        buttonMessageKey: 'SNACKBAR.ERROR_WITH_LOGBOOK_OPERATION_ID_BUTTON',
      },
    });
  }

  goToOperation(operationId: string) {
    window.location.href =
      this.startupService.getReferentialUrl() +
      '/logbook-operation/tenant/' +
      this.startupService.getTenantIdentifier() +
      '?guid=' +
      operationId;
  }

  savePA() {
    if (!this.editProfile) {
      this.profileService.createProfilePa(this.profile).subscribe((createdProfile) => {
        if (createdProfile) {
          // STEP 2 : ASSIGNER LE PROFIL A LA NOTICE
          this.profileService.uploadFile(this.data, this.profileDescription, ProfileType.PA).subscribe((retrievedData) => {
            const myFile = this.blobToFile(retrievedData, 'file');
            this.profileService.updateProfileFilePa(createdProfile, myFile).subscribe(() => {
              this.toggleService.hidePending();
              this.success('La création du profil a bien été effectué');
            });
          });
        }
      });
    } else {
      this.profileService.updateProfilePa(this.profile).subscribe(() => {
        // STEP 2 : ASSIGNER LE PROFIL A LA NOTICE
        this.profileService.uploadFile(this.data, this.profileDescription, this.profileService.profileMode).subscribe((retrievedData) => {
          const myFile = this.blobToFile(retrievedData, 'file');
          this.profileService.updateProfileFilePa(this.noticeService.paNotice(this.profileDescription, false), myFile).subscribe(() => {
            this.toggleService.hidePending();
            this.success('La modification du profil a bien été effectué');
          });
        });
      });
    }
  }

  success(msg: string) {
    this.notificationService.showSuccess(msg);
    // sleep 3 sec before return pastishome
    setTimeout(() => {
      this.router.navigate(['pastis']);
    }, 3000);
  }

  public blobToFile = (theBlob: Blob, fileName: string): File => {
    const b: any = theBlob;
    b.lastModifiedDate = new Date();
    b.name = fileName;
    // Cast to a File() type
    return theBlob as File;
  };

  downloadFile(dataFile: any, notice: boolean): void {
    let typeFile;
    let download;
    if (notice) {
      typeFile = 'application/json';
      download = 'pastis.json';
    } else {
      typeFile = this.profileService.profileMode === ProfileType.PA ? 'application/xml' : 'application/json';
      download = this.profileService.profileMode === ProfileType.PA ? 'pastis_profile.rng' : 'pastis.json';
    }
    const newBlob = new Blob([dataFile], { type: typeFile });
    const data = window.URL.createObjectURL(newBlob);
    const link = document.createElement('a');
    link.href = data;
    link.download = download;
    // this is necessary as link.click() does not work on the latest firefox
    link.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, view: window }));
    setTimeout(() => {
      // For Firefox it is necessary to delay revoking the ObjectURL
      window.URL.revokeObjectURL(data);
      link.remove();
    }, 100);
  }

  downloadProfiles(local: boolean): void {
    if (this.data) {
      // Get Notice changement
      let notice: any;
      if (this.profileService.profileMode === ProfileType.PUA) {
        this.fileService.notice.subscribe((value: any) => {
          notice = value;
        });
      }
      if (local && this.profileService.profileMode === ProfileType.PA && this.editProfile) {
        this.subscriptions.add(
          this.fileService.notice.subscribe((value: ProfileDescription) => {
            this.downloadFile(JSON.stringify(value), true);
          }),
        );
      }

      // Send the retrieved JSON data to profile service
      this.subscriptions.add(
        this.profileService.uploadFile(this.data, notice, this.profileService.profileMode).subscribe((retrievedData) => {
          this.downloadFile(retrievedData, false);
        }),
      );
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
