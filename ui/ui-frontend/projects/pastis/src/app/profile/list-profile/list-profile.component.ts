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
import {Component, Input, OnDestroy, OnInit, Pipe, PipeTransform, ViewChild} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {ProfileService} from '../../core/services/profile.service';
import {ProfileDescription} from '../../models/profile-description.model';
import {ActivatedRoute, Router} from '@angular/router';
import {FileUploader} from 'ng2-file-upload';
import {MetadataHeaders} from '../../models/models';
import {BreadcrumbDataTop} from '../../models/breadcrumb';
import {Direction, GlobalEventService, SidenavPage, StartupService} from 'ui-frontend-common';
import {Subscription} from 'rxjs';
import {environment} from '../../../environments/environment';
import {PastisConfiguration} from "../../core/classes/pastis-configuration";
import {ProfileResponse} from "../../models/profile-response";
import {PastisDialogData} from "../../shared/pastis-dialog/classes/pastis-dialog-data";
import {DataGeneriquePopupService} from "../../shared/data-generique-popup.service";
import {CreateProfileComponent} from "../create-profile/create-profile.component";
import {LangChangeEvent, TranslateService} from "@ngx-translate/core";
import {MatDialog} from "@angular/material/dialog";
import {ProfileInformationTabComponent} from "../profile-preview/profile-information-tab/profile-information-tab/profile-information-tab.component";
import {Profile} from '../../models/profile';
import {NoticeService} from '../../core/services/notice.service';
import {ArchivalProfileUnit} from '../../models/archival-profile-unit';
import {ToggleSidenavService} from '../../core/services/toggle-sidenav.service';

const POPUP_CREATION_PATH = 'PROFILE.POP_UP_CREATION';

function constantToTranslate() {
  this.popupCreationCancelLabel = this.translated('.POPUP_CREATION_CANCEL_LABEL');
  this.popupCreationTitleDialog = this.translated('.POPUP_CREATION_TITLE_DIALOG');
  this.popupCreationSubTitleDialog = this.translated('.POPUP_CREATION_SUBTITLE_DIALOG');
  this.popupCreationOkLabel = this.translated('.POPUP_CREATION_OK_LABEL');
}
@Component({
    selector: 'pastis-list-profile',
    templateUrl: './list-profile.component.html',
    styleUrls: ['./list-profile.component.scss']
})
export class ListProfileComponent extends SidenavPage<ProfileDescription> implements OnInit,OnDestroy {

  @ViewChild(ProfileInformationTabComponent, {static: true}) profileInformationTabComponent: ProfileInformationTabComponent;

  @Input()
  uploader: FileUploader = new FileUploader({url: ""});

  displayedColumns: string[] = ['type', "id", "baseName", "lastModified"]

  retrievedProfiles: ProfileDescription[] = [];

  matDataSource: MatTableDataSource<ProfileDescription>;

  numPA: number;

  numPUA: number;

  totalProfileNum: number;

  profileToLoad: any;

  hoveredElementId: number;

  buttonIsClicked: boolean;

  search: string;

  numProfilesFiltered: ProfileDescription[];

  profilModel: ProfileDescription

  filterType: string;

  isStandalone: boolean = environment.standalone;

  direction = Direction.ASCENDANT;

  orderBy = "identifier";

  sedaUrl: string = this.pastisConfig.pastisPathPrefix + (this.isStandalone ? '' : this.startupService.getTenantIdentifier()) + this.pastisConfig.sedaUrl;

  newProfileUrl: string = this.pastisConfig.pastisPathPrefix + (this.isStandalone ? '' : this.startupService.getTenantIdentifier() )+ this.pastisConfig.pastisNewProfile;

  docPath = this.isStandalone ? 'assets/doc/Standalone - Documentation APP - PASTIS.pdf' : 'assets/doc/VITAM UI - Documentation APP - PASTIS.pdf';

  subscription1$: Subscription;
  subscription2$: Subscription;
  _uploadProfileSub: Subscription;
  subscriptions: Subscription[] = [];

  donnees:string[];

  promise : Promise<any>

  expanded: boolean;

  pending: boolean;

  pendingSub: Subscription;

  public breadcrumbDataTop: Array<BreadcrumbDataTop>;

  popupCreationCancelLabel: string;
  popupCreationTitleDialog: string;
  popupCreationSubTitleDialog: string;
  popupCreationOkLabel: string;
  profilesChargees: boolean = false;

  constructor(private profileService: ProfileService, private noticeService: NoticeService,private sideNavService : ToggleSidenavService,
    private router:Router, private dialog: MatDialog,
    private startupService: StartupService, private pastisConfig: PastisConfiguration, route: ActivatedRoute, globalEventService: GlobalEventService,
              private dataGeneriquePopupService: DataGeneriquePopupService, private translateService: TranslateService,
              private toggleService : ToggleSidenavService) {
    super(route, globalEventService);
    this.expanded = false;
    this.pendingSub = this.sideNavService.isPending.subscribe(status=>{
      this.pending = status;
    })
  }

  ngOnInit() {
    if(!this.isStandalone){
      constantToTranslate.call(this);
      this.translatedOnChange();
    }
    else if(this.isStandalone)
    {
      this.popupCreationCancelLabel = "Annuler"
      this.popupCreationTitleDialog = "Choix du type de profil"
      this.popupCreationSubTitleDialog = "Création d'un profil"
      this.popupCreationOkLabel = "TERMINER"
    }
    this.dataGeneriquePopupService.currentDonnee.subscribe(donnees => this.donnees = donnees);
    this.breadcrumbDataTop = [{ label: "PROFILE.EDIT_PROFILE.BREADCRUMB.PORTAIL", url: this.startupService.getPortalUrl(), external: true},{ label: "PROFILE.EDIT_PROFILE.BREADCRUMB.CREER_ET_GERER_PROFIL", url: '/'}];

    this.subscription1$ =
      this.refreshListProfiles();
    this.subscriptions.push(this.subscription1$);
  }

  private refreshListProfiles() {
    this.toggleService.showPending();
    this.profileService.refreshListProfiles();
    return this.profileService.retrievedProfiles.subscribe((profileList: ProfileDescription[]) => {
      if (profileList) {
        this.retrievedProfiles = profileList;
        console.log("Profiles: ", this.retrievedProfiles);
        this.profilesChargees = true;
        this.toggleService.hidePending();
      }
      this.matDataSource = new MatTableDataSource<ProfileDescription>(this.retrievedProfiles);
      this.numPA = this.retrievePAorPUA("PA", false);
      this.numPUA = this.retrievePAorPUA("PUA", false);
      this.totalProfileNum = this.retrievedProfiles ? this.retrievedProfiles.length : 0;
    });
  }

  translatedOnChange(): void {
    this.translateService.onLangChange
      .subscribe((event: LangChangeEvent) => {
        constantToTranslate.call(this);
        console.log(event.lang);
      });
  }

  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(POPUP_CREATION_PATH + nameOfFieldToTranslate);
  }

  retrievePAorPUA(term: string, filter: boolean): number {
    let profiles: ProfileDescription[] = filter == false ? this.retrievedProfiles : this.numProfilesFiltered;
    let profileNum = profiles.filter(p => p.type === term).length
    return profileNum ? profileNum : 0;
  }

  navigate(d: BreadcrumbDataTop){
    if (d.external){
      window.location.assign(d.url);
    } else {
      this.router.navigate([d.url],{skipLocationChange: false});
    }
  }

  editProfile(element: ProfileDescription) {
    this.router.navigate([this.pastisConfig.pastisPathPrefix + (this.isStandalone ? '' : this.startupService.getTenantIdentifier()) + this.pastisConfig.pastisEditPage, element.id], {state: element, skipLocationChange: false});
  }

  uploadProfile(files: File[]):void {
    let fileToUpload: File = files[0];

    if (fileToUpload) {
      const formData = new FormData();
      formData.append('file', fileToUpload, fileToUpload.name);
      this._uploadProfileSub = this.profileService.uploadProfile(formData).subscribe( (response: any) => {
        if (response) {
          console.log('File submited! Reponse is : ', response);

          this.router.navigateByUrl(this.pastisConfig.pastisPathPrefix + (this.isStandalone ? '' : this.startupService.getTenantIdentifier() )+ this.pastisConfig.pastisNewProfile, { state: response });
        }
      });
      this.subscriptions.push(this._uploadProfileSub)
    }
  }

  async createProfile() {
    let dataToSendToPopUp = <PastisDialogData>{};
    dataToSendToPopUp.titleDialog = this.popupCreationTitleDialog;
    dataToSendToPopUp.subTitleDialog = this.popupCreationSubTitleDialog;
    dataToSendToPopUp.width = '800px';
    dataToSendToPopUp.height = '800px';
    dataToSendToPopUp.okLabel = this.popupCreationOkLabel;
    dataToSendToPopUp.cancelLabel = this.popupCreationCancelLabel;
    const dialogRef = this.dialog.open(CreateProfileComponent, {
        width: '800px',
        panelClass: 'pastis-popup-modal-box',
        data: dataToSendToPopUp
      }
    );
    this.subscription2$ =dialogRef.afterClosed().subscribe((result) => {
      if (result.success){
        console.log(result.action + " PA ou PUA ?")
        if(result.action ==='PA' || result.action ==='PUA'){
          this.profileService.createProfile(this.pastisConfig.createProfileByTypeUrl, result.action).subscribe((response: ProfileResponse) => {
            if (response) {
              console.log('File submited! Reponse is : ', response);
              this.router.navigateByUrl(this.pastisConfig.pastisPathPrefix + (this.isStandalone ? '' : this.startupService.getTenantIdentifier() )+ this.pastisConfig.pastisNewProfile, {state: response});
            }
          })
        }
      }
      });
    this.subscriptions.push(this.subscription2$);
  }

  public onSearchSubmit(search: string): void {
      this.search = search;
  }

  isRowHovered(elementId: number) {
      return this.hoveredElementId === elementId;
  }

  onMouseOver(row: MetadataHeaders) {
      this.buttonIsClicked = false;
      this.hoveredElementId = row.id;
  }

  onMouseLeave() {
      if (!this.buttonIsClicked) {
          this.hoveredElementId = 0;
      }
  }

  changeType(type: string){
    if(type != undefined){
      this.filterType = type;
    }
  }

  ngOnDestroy(){
    this.profileService.retrievedProfiles.next([]);
    this.subscriptions.forEach((subscriptions) => subscriptions.unsubscribe())
    if(this.pendingSub) this.pendingSub.unsubscribe();
  }


  showProfile(element: ProfileDescription) {
    if(!this.isStandalone){
      this.openPanel(element)
    }
  }

  updateProfileNotice(profileDescription: ProfileDescription, files: File[]){
    let fileToUpload: File = files[0];
    let profile: Profile;
    let archivalProfileUnit: ArchivalProfileUnit;
    if(profileDescription.type === "PA"){
      profile = this.noticeService.profileDescriptionToPaProfile(profileDescription)
      this.profileService.updateProfileFilePa(profile,  fileToUpload).subscribe(() => {
        this.expanded = !this.expanded;
        this.refreshListProfiles();
      })
    }
    if(profileDescription.type === "PUA"){
      if (fileToUpload) {
        let jsonObj: ProfileDescription;
        var fileReader = new FileReader();
        fileReader.readAsText(fileToUpload, 'UTF-8');
        fileReader.onload = () => {
          //console.log(fileReader.result.toString());
          jsonObj=(JSON.parse(fileReader.result.toString()));
          console.log(jsonObj['controlSchema'])
          profileDescription.controlSchema = jsonObj['controlSchema'];
          archivalProfileUnit = this.noticeService.profileDescriptionToPuaProfile(profileDescription)
          this.profileService.updateProfilePua(archivalProfileUnit).subscribe(() => {
            this.expanded = !this.expanded;
            this.refreshListProfiles();
          })
        }
        fileReader.onerror = (error) => {
        console.log(error);
        }
      }
    }

  }

  changeExpand(element: ProfileDescription){
    if(element.type == 'PA' && !element.path && element.status === 'ACTIVE'){
      this.expanded = !this.expanded;
    }
    if(element.type == 'PUA' && element.status == 'ACTIVE'
    && (element.controlSchema == "{}" || !element.controlSchema)){
      this.expanded = !this.expanded;
    }
  }
}

@Pipe({name: 'filterByType'})
export class FilterByTypePipe implements PipeTransform {
  transform(listOfProfiles: ProfileDescription[], typeToFilter: string): ProfileDescription[] {
    if(!listOfProfiles) return null;
    if(!typeToFilter) return listOfProfiles;
    if(typeToFilter == "ALL") return listOfProfiles;
    return listOfProfiles.filter(profile => profile.type == typeToFilter);
  }
}

@Pipe({name: 'filterByStringName'})
export class FilterByStringNamePipe implements PipeTransform {
  constructor(){}
  private listOfProfiles: ProfileDescription[]
  transform(listOfProfiles: ProfileDescription[], nameToFilter: string): ProfileDescription[] {
    if(!listOfProfiles) return null;
    if(!nameToFilter) return listOfProfiles;
    this.listOfProfiles = listOfProfiles.filter(profile => profile.identifier.toLowerCase().indexOf(nameToFilter.toLowerCase()) >= 0);
    return this.listOfProfiles;
  }
}
