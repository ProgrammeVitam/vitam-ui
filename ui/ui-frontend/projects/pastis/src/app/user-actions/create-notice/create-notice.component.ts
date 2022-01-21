import {Component, Inject, OnInit} from '@angular/core';
import {PastisDialogData} from "../../shared/pastis-dialog/classes/pastis-dialog-data";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {PopupService} from "../../core/services/popup.service";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {PastisDialogDataCreate} from "../save-profile/save-profile.component";
import {LangChangeEvent, TranslateService} from "@ngx-translate/core";
import {environment} from "../../../environments/environment";
import { Notice } from '../../models/notice.model';
import { Router } from '@angular/router';
import { FileService } from '../../core/services/file.service';
import { ProfileService } from '../../core/services/profile.service';


interface Status {
  value: string;
  viewValue: string;
}

const POPUP_CREATION_CHOICE_PATH = 'PROFILE.POP_UP_CREATION_NOTICE.CHOICE';

function constantToTranslate() {
  this.profilActif = this.translated('.PROFIL_ACTIF');
  this.profilInactif = this.translated('.PROFIL_INACTIF');
}

@Component({
  selector: 'create-notice',
  templateUrl: './create-notice.component.html',
  styleUrls: ['./create-notice.component.scss']
})
export class CreateNoticeComponent implements OnInit {
  form: FormGroup;
  stepIndex = 0;
  btnIsDisabled: boolean;
  dialogData: PastisDialogData;
  isDisabledButton = false;
  notice: Notice;
  //edit or new notice
  editNotice: boolean;
  titleDialog: string;
  subTitleDialog: string;
  okLabel:string;
  cancelLabel:string;
  arrayStatus: Status[] ;
  typeProfile?: string;
  modePUA: boolean;
  information: string;
  presenceNonDeclareMetadonneesPUAControl = new FormControl(false);
  createNotice: boolean;
  profilActif :string;
  profilInactif:string;
  validate: boolean;

  isStandalone: boolean = environment.standalone;

  constructor(public dialogRef: MatDialogRef<CreateNoticeComponent>,
              @Inject(MAT_DIALOG_DATA) public data: PastisDialogDataCreate,
              private formBuilder: FormBuilder,
              private translateService: TranslateService,
              private popUpService: PopupService,
              private fileService: FileService,
              private router: Router,
              private profileService: ProfileService) {

  }

  ngOnInit() {

    this.editNotice = this.router.url.substring(this.router.url.lastIndexOf('/')-4, this.router.url.lastIndexOf('/')) === "edit";
    if(this.editNotice){
      this.validate = true;
      // Subscribe observer to notice
      this.fileService.noticeEditable.subscribe((value: Notice) => {
        this.notice = value;
      })
    }// load new notice
    else{
      this.notice = {
        description: '',
        name: '',
        status: 'ACTIVE',
        identifier: ''
       }
    }

    if(!this.isStandalone){
      constantToTranslate.call(this);
      this.translatedOnChange();
    }
    else if(this.isStandalone)
    {
      this.profilActif = "Profil actif"
      this.profilInactif = "Profil inactif"
    }
    this.arrayStatus= [
      {value: 'INACTIVE', viewValue:this.profilInactif},
      {value: 'ACTIVE', viewValue:  this.profilActif}
    ];
    this.typeProfile = this.data.modeProfile;
    if(this.typeProfile ==="PUA")
      this.modePUA=true;
    this.information = "texte d'information"
    let identifierForm = this.modePUA ? [null, Validators.required] : [null];
    this.form = this.formBuilder.group({
      identifier: identifierForm,
      intitule: [null, Validators.required],
      selectedStatus: [null],
      description: [null],
      autoriserPresenceMetadonnees : false
      //TODO à implémenter
      // this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
    });


    this.presenceNonDeclareMetadonneesPUAControl.valueChanges.subscribe((value) => {
      this.form.controls.autoriserPresenceMetadonnees.setValue(value);
    });


    // Subscribe observer to button status and
    // set the inital state of the ok button to disabled
    this.popUpService.btnYesShoudBeDisabled.subscribe(status=>{
      this.btnIsDisabled = status;
    })
  }

  translatedOnChange(): void {
    this.translateService.onLangChange
      .subscribe((event: LangChangeEvent) => {
        constantToTranslate.call(this);
        console.log(event.lang);
      });
  }

  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(POPUP_CREATION_CHOICE_PATH + nameOfFieldToTranslate);
  }

  onCancel() {
/*    if (this.form.dirty) {
      this.popUpService.confirmBeforeClosing(this.dialogRef);
    } else {*/
      this.dialogRef.close();
    // }
  }




  upateButtonStatusAndDataToSend(){
    this.popUpService.setPopUpDataOnClose("test");
    this.popUpService.disableYesButton(true)
  }


  onNoClick(): void {
    this.dialogRef.close();
  }


  ngOnDestroy(): void {

  }

  checkIdentifier(){

    if(this.notice.identifier.length !== 0){
      this.profileService.getPuaProfile(this.notice.identifier).subscribe(
        () => {
          alert('Identifier already exists use another identifier')
          this.validate = false;
        },() => {
          this.validate = true;
          this.checkIntitule();
        }
      )
    }else{
      this.validate = false;
    }    
  }

  checkIntitule(){
    if(this.notice.name.length !== 0) this.validate = true;
    else{
      this.validate = false;
    }  
  }

  onSubmit() {
    if (this.form.invalid) {
      this.isDisabledButton = true;
      return;
    }
    this.isDisabledButton = true;
    console.log(this.form.value)
    if(this.editNotice){
      this.fileService.noticeEditable.next(this.notice);
      this.fileService.setNotice(true);
    }
    this.dialogRef.close({ success: true, action: 'none', data:this.form.value, mode:this.typeProfile });
  }
}


