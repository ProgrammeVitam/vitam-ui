import { Component, OnInit } from '@angular/core';
import { SafeResourceUrl } from '@angular/platform-browser';
import { AuthService } from '../../auth.service';
import { ThemeDataType } from '../../models';
import { StartupService } from '../../startup.service';
import { ThemeService } from '../../theme.service';

@Component({
  selector: 'vitamui-common-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss'],
})
export class FooterComponent implements OnInit {
  public footerLogoUrl: SafeResourceUrl;
  public version: string;

  constructor(
    private startupService: StartupService,
    private authService: AuthService,
    private themeService: ThemeService
  ) {}

  ngOnInit() {
    const versionRelease = this.startupService.getConfigStringValue( 'VERSION_RELEASE');
    if (versionRelease) {
      this.version = 'v' + versionRelease;
    }
    this.footerLogoUrl = this.themeService.getData(
      this.authService.user,
      ThemeDataType.FOOTER_LOGO
    );
  }
}
