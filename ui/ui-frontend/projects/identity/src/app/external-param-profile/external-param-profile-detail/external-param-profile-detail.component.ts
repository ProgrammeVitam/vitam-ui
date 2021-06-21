import {Component,EventEmitter,Input,OnDestroy,OnInit,Output} from '@angular/core';
import {Subscription} from 'rxjs';
import {Event,ExternalParamProfile} from 'ui-frontend-common';
import {ExternalParamProfileService} from '../external-param-profile.service';
import {SharedService} from '../shared.service';

@Component({
  selector: 'app-external-param-profile-detail',
  templateUrl: './external-param-profile-detail.component.html',
  styleUrls: ['./external-param-profile-detail.component.css'],
})
export class ExternalParamProfileDetailComponent implements OnInit,OnDestroy {
  @Input() externalParamProfile: ExternalParamProfile;
  @Input() tenantIdentifier: string;
  @Input() isPopup: boolean;
  @Output() externalParamProfileClose=new EventEmitter();
  readOnly: boolean;
  externalParamProfileUpdateSub: Subscription;

  constructor(private sharedService: SharedService,private externalParamProfileServiceService: ExternalParamProfileService) {
    this.sharedService.getReadOnly().subscribe((readOnly) => {
      this.readOnly=readOnly;
    });
  }

  ngOnInit(): void {
    this.externalParamProfileUpdateSub=this.externalParamProfileServiceService.updated.subscribe((externalParamProfile) => {
      if(externalParamProfile) {
        this.externalParamProfileServiceService.getOne(externalParamProfile.idProfile).subscribe((newExternalParamProfile) => {
          this.externalParamProfile=newExternalParamProfile;
        });
      }
    });
  }

  emitClose() {
    this.externalParamProfileClose.emit();
  }

  ngOnDestroy(): void {
    this.externalParamProfileUpdateSub.unsubscribe();
  }

  filterEvents(event: Event): boolean {
    return (
      event.outDetail&&
      (event.outDetail.includes('EXT_VITAMUI_CREATE_EXTERNAL_PARAM_PROFILE')||
        event.outDetail.includes('EXT_VITAMUI_UPDATE_EXTERNAL_PARAM_PROFILE'))
    );
  }
}
