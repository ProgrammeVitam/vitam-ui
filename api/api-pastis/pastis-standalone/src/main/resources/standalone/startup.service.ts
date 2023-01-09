/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { Inject, Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApplicationService } from './application.service';

import { ApplicationApiService } from './api/application-api.service';
import { SecurityApiService } from './api/security-api.service';
import { ApplicationId } from './application-id.enum';
import { AuthService } from './auth.service';
import { WINDOW_LOCATION } from './injection-tokens';
import { Logger } from './logger/logger';
import { AppConfiguration, AttachmentType, AuthUser, Color } from './models';
import {ThemeService} from './theme.service';

const WARNING_DURATION = 2000;
const CUSTOMER_TECHNICAL_REFERENT_KEY = 'technical-referent-email';
const CUSTOMER_WEBSITE_URL_KEY = 'website-url';

@Injectable({
  providedIn: 'root'
})
export class StartupService {

  private configurationData: AppConfiguration;

  userRefresh = new Subject<any>();

  CURRENT_APP_ID: ApplicationId = ApplicationId.PORTAL_APP;

  private CURRENT_TENANT_IDENTIFIER: string;


  constructor(
    private logger: Logger,
    private authService: AuthService,
    private securityApi: SecurityApiService,
    private themeService: ThemeService,
    @Inject(WINDOW_LOCATION) private location: any
  ) { }

  load(): any {
    this.configurationData = null;

    let appConf: AppConfiguration = {
      THEME_COLORS: {
        "vitamui-background" : "#F5F7FC",
        "vitamui-header-footer" : "#ffffff",
        "vitamui-primary" : "#702382",
        "vitamui-secondary" : "#2563A9",
        "vitamui-tertiary" : "#C22A40"
      }
    };
    this.configurationData = appConf;
    this.themeService.init(this.configurationData, this.configurationData.THEME_COLORS);
    return appConf;
  }

  setTenantIdentifier(tenantIdentifier?: string) {
    this.CURRENT_TENANT_IDENTIFIER = tenantIdentifier;
  }

  getTenantIdentifier() {
    let tenantIdentifier = this.CURRENT_TENANT_IDENTIFIER;
    if (!tenantIdentifier && this.authService.user) {
      tenantIdentifier = this.authService.user.proofTenantIdentifier;
    }

    return tenantIdentifier;
  }

  /**
   * No catchError should be set here, the security api must be called and verified before anything else.
   */
  refreshUser(): Observable<AuthUser> {
    return this.securityApi.getAuthenticated().pipe(
      tap((data) => {
        this.authService.user = data;
        this.userRefresh.next(data);
      })
    );
  }

  configurationLoaded(): boolean {
    return null;
  }

  printConfiguration(): void {
    if (this.configurationLoaded()) {
      this.logger.log(this, 'startup data exists.', this.configurationData);
    } else {
      this.logger.log(this, 'startup data does not exists');
    }
  }

  getLogo(): string {
    return null;
  }

  getAppLogoURL(): string {
    let trustedAppLogoUrl = null;
    const base64Logo = this.getLogo();

    if (base64Logo) {
      trustedAppLogoUrl = base64Logo;
    }

    return trustedAppLogoUrl;
  }

  getCustomerLogoURL(): string {
    let trustedInlineLogoUrl = null;

    if (this.authService.user) {
      const currentUser = this.authService.user;
      if (currentUser.basicCustomer) {
        trustedInlineLogoUrl = currentUser.basicCustomer.graphicIdentity.portalDataBase64;
      }
    }

    return trustedInlineLogoUrl;
  }

  getReferentialUrl(): string {
    return null;
  }
  getPortalUrl(): string {
    return null;
  }

  getLoginUrl(): string {
    return null;
  }

  getLogoutUrl(): string {

    return null;
  }

  getCasUrl(): string {

    return null;
  }

  getSearchUrl(): string {
    return null;
  }

  getConfigStringValue(key: string): string {

    return null;
  }

  getConfigNumberValue(key: string): number {
    return +this.getConfigStringValue(key);
  }

  /**
   * Navigate to given url or to the portal otherwise.
   * @param url URL to be redirected to.
   */
  redirect(url?: string) {
    setTimeout(() => this.location.href = url ? url : this.getPortalUrl(), WARNING_DURATION);
  }

  getPlatformName(): string {

    return null;
  }

  public getCustomer(): string {
    return null;
  }

  public getCustomerTechnicalReferentEmail(): string {
    const customer = this.getCustomer();
    if (customer) {
      return customer[CUSTOMER_TECHNICAL_REFERENT_KEY];
    }
  }

  public getCustomerWebsiteUrl(): string {
    const customer = this.getCustomer();
    if (customer) {
      return customer[CUSTOMER_WEBSITE_URL_KEY];
    }
  }

}
