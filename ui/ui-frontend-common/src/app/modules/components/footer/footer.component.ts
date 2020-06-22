import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import * as _ from 'lodash';
import { AuthService } from '../../auth.service';
import { StartupService } from '../../startup.service';

@Component({
  selector: 'vitamui-common-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss'],
})
export class FooterComponent implements OnInit {
  logoUrl: SafeUrl;
  platformName: string;
  formGroup: FormGroup;

  constructor(
    public authService: AuthService,
    private startupService: StartupService,
    private domSanitizer: DomSanitizer,
    private formBuilder: FormBuilder
  ) {}

  ngOnInit() {
    this.createForm();
    this.logoUrl = this.getLogoUrl();
    this.platformName = this.startupService.getPlatformName();
  }

  private getLogoUrl(): SafeUrl {
    let logoUrl = this.startupService.getCustomerLogoURL();

    if (!this.isCustomerHasGraphicalIdentity() || !logoUrl) {
      logoUrl = this.startupService.getAppLogoURL();
    }

    return logoUrl ? this.sanitizeImageUrl(logoUrl) : null;
  }

  private isCustomerHasGraphicalIdentity(): boolean {
    return _.get(
      this.authService.user,
      'basicCustomer.graphicIdentity.hasCustomGraphicIdentity',
      false
    );
  }

  private sanitizeImageUrl(url: string): SafeUrl {
    return this.domSanitizer.bypassSecurityTrustUrl(
      'data:image/*;base64,' + url
    );
  }

  private createForm() {
    this.formGroup = this.formBuilder.group({
      email: [null],
      name: [null],
    });
  }
}
