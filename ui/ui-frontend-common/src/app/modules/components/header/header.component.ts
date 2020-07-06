import { Component, OnInit } from '@angular/core';
import { SafeUrl } from '@angular/platform-browser';
import { StartupService } from '../../startup.service';
import { AuthService } from '../../auth.service';
import { AuthUser } from '../../models';
import { ApplicationId } from './../../application-id.enum';
import { SubrogationService } from './../../subrogation/subrogation.service';

@Component({
  selector: 'vitamui-common-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit {

  public portalUrl: string;

  public currentUser: AuthUser;

  /** TODO : rooting /account in portal module => move to header module */
  public hasAccountProfile = false;

  public trustedInlineLogoUrl: SafeUrl;

  constructor(private subrogationService: SubrogationService, private startupService: StartupService,
              private authService: AuthService) { }

  ngOnInit() {
    if (this.authService.user) {
      this.currentUser = this.authService.user;
      this.hasAccountProfile =
        this.authService.user.profileGroup.profiles.some(
          (profile) => profile.applicationName === ApplicationId.ACCOUNTS_APP
        );
    }
    this.portalUrl = this.startupService.getPortalUrl();
  }

  public enabledSubrogation(): void {
    this.subrogationService.checkSubrogation();
  }

  public logout(): void {
    this.authService.logout();
  }
}
