import { Component, OnInit } from '@angular/core';
import { DomSanitizer, SafeResourceUrl, SafeUrl } from '@angular/platform-browser';
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
  public customerTechnicalReferentEmail: string;
  public sanitizedCustomerWebsiteUrl: SafeUrl;
  public customerWebsiteUrlTitle: string;
  private customerWebsiteUrl: string;
  public footerLogoUrl: SafeResourceUrl;

  constructor(
    private startupService: StartupService,
    private sanitizer: DomSanitizer,
    private authService: AuthService,
    private themeService: ThemeService,
  ) { }

  ngOnInit() {
    this.customerTechnicalReferentEmail = this.startupService.getCustomerTechnicalReferentEmail();
    this.customerWebsiteUrl = this.startupService.getCustomerWebsiteUrl();
    this.sanitizedCustomerWebsiteUrl = this.sanitizer.bypassSecurityTrustUrl(this.customerWebsiteUrl);
    this.customerWebsiteUrlTitle = this.getCustomerWebSiteTitle();
    this.footerLogoUrl = this.themeService.getData(this.authService.user, ThemeDataType.FOOTER_LOGO);
  }

  private getCustomerWebSiteTitle(): string {
    if (this.customerWebsiteUrl) {
      return new URL(this.customerWebsiteUrl).hostname;
    }
  }
}
