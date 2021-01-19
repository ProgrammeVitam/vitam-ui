import { Component, OnInit } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { StartupService } from '../../startup.service';

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

  constructor(private startupService: StartupService, private sanitizer: DomSanitizer) { }

  ngOnInit() {
    this.customerTechnicalReferentEmail = this.startupService.getCustomerTechnicalReferentEmail();
    this.customerWebsiteUrl = this.startupService.getCustomerWebsiteUrl();
    this.sanitizedCustomerWebsiteUrl = this.sanitizer.bypassSecurityTrustUrl(this.customerWebsiteUrl);
    this.customerWebsiteUrlTitle = this.getCustomerWebSiteTitle();
  }

  private getCustomerWebSiteTitle(): string {
    if (this.customerWebsiteUrl) {
      return new URL(this.customerWebsiteUrl).hostname;
    }
  }
}
