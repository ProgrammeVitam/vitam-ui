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
import { combineLatest, Observable, Subject } from 'rxjs';
import { switchMap, take, tap } from 'rxjs/operators';
import { ApplicationApiService } from './api/application-api.service';
import { BaseUserInfoApiService } from './api/base-user-info-api.service';
import { SecurityApiService } from './api/security-api.service';
import { ApplicationId } from './application-id.enum';
import { ApplicationService } from './application.service';
import { AuthService } from './auth.service';
import { ConfigService } from './config.service';
import { WINDOW_LOCATION } from './injection-tokens';
import { Logger } from './logger/logger';
import { AppConfiguration, AttachmentType, AuthUser, UserInfo } from './models';
import { ThemeService } from './theme.service';

const WARNING_DURATION = 2000;

@Injectable({
  providedIn: 'root',
})
export class StartupService {
  public userRefresh = new Subject<any>();
  public CURRENT_APP_ID: ApplicationId = ApplicationId.PORTAL_APP;

  private configurationData: AppConfiguration = null;
  private CURRENT_TENANT_IDENTIFIER: string;

  constructor(
    private configService: ConfigService,
    private logger: Logger,
    private authService: AuthService,
    private securityApi: SecurityApiService,
    private applicationApi: ApplicationApiService,
    private themeService: ThemeService,
    private applicationService: ApplicationService,
    private userInfoApiService: BaseUserInfoApiService,
    @Inject(WINDOW_LOCATION) private location: any,
  ) {}

  load(): Observable<AuthUser> {
    this.configurationData = this.configService.config;
    this.authService.loginUrl = this.configurationData.CAS_URL;
    this.authService.logoutUrl = this.configurationData.CAS_LOGOUT_URL;
    this.authService.logoutRedirectUiUrl = this.configurationData.LOGOUT_REDIRECT_UI_URL;

    return this.refreshUser().pipe(
      tap((_) => {
        this.userInfoApiService
          .getMyUserInfo()
          .pipe(
            tap((userInfo: UserInfo) => (this.authService.userInfo = userInfo)),
            take(1),
          )
          .subscribe();
      }),
      tap((_) => {
        this.applicationService.list().pipe(take(1)).subscribe();
      }),
      switchMap((_) => {
        let logosObservable: Observable<any>;
        if (this.configurationData.GATEWAY_ENABLED) {
          logosObservable = combineLatest([
            this.applicationApi.getLocalAsset(this.configurationData.HEADER_LOGO),
            this.applicationApi.getLocalAsset(this.configurationData.FOOTER_LOGO),
            this.applicationApi.getLocalAsset(this.configurationData.PORTAL_LOGO),
            this.applicationApi.getLocalAsset(this.configurationData.USER_LOGO),
          ]).pipe(
            tap(([header, footer, portal, user]) => {
              // We change the LOGO variables to put base64 image instead of .png reference for the themeService.init()
              this.configurationData.HEADER_LOGO = header;
              this.configurationData.FOOTER_LOGO = footer;
              this.configurationData.PORTAL_LOGO = portal;
              this.configurationData.LOGO = portal;
              this.configurationData.USER_LOGO = user;
            }),
            take(1),
          );
        } else {
          logosObservable = this.applicationApi
            .getAsset([AttachmentType.Header, AttachmentType.Footer, AttachmentType.User, AttachmentType.Portal])
            .pipe(
              tap((data) => {
                this.configurationData.HEADER_LOGO = data[AttachmentType.Header];
                this.configurationData.FOOTER_LOGO = data[AttachmentType.Footer];
                this.configurationData.PORTAL_LOGO = data[AttachmentType.Portal];
                this.configurationData.USER_LOGO = data[AttachmentType.User];
                this.configurationData.LOGO = data[AttachmentType.Portal];
              }),
              take(1),
            );
        }
        return logosObservable;
      }),
      tap((_) => {
        const graphicIdentity = this.authService.user.basicCustomer.graphicIdentity;
        const customerColorMap = graphicIdentity.hasCustomGraphicIdentity ? graphicIdentity.themeColors : null;
        this.themeService.init(this.configurationData, customerColorMap);
      }),
    );
  }

  /**
   * @deprecated: use tenant-selection.service
   */
  setTenantIdentifier(tenantIdentifier?: string) {
    if (tenantIdentifier) {
      this.CURRENT_TENANT_IDENTIFIER = tenantIdentifier;
    }
  }

  /**
   * return the tenant ID saved in CURRENT_TENANT_IDENTIFIER or the user's proof tenant.
   * @deprecated: use tenant-selection.service
   */
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
      }),
    );
  }

  configurationLoaded(): boolean {
    return this.configurationData != null && this.configurationData.PORTAL_URL != null;
  }

  printConfiguration(): void {
    if (this.configurationLoaded()) {
      this.logger.log(this, 'startup data exists.', this.configurationData);
    } else {
      this.logger.log(this, 'startup data does not exists');
    }
  }

  getLogo(): string {
    if (this.configurationLoaded()) {
      if (this.configurationData.APP_LOGO) {
        return this.configurationData.APP_LOGO;
      } else {
        return this.configurationData.LOGO;
      }
    }
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

  getPortalUrl(): string {
    if (this.configurationLoaded()) {
      return this.configurationData.PORTAL_URL;
    }

    return null;
  }

  getLoginUrl(): string {
    if (this.configurationLoaded()) {
      return this.configurationData.CAS_URL;
    }

    return null;
  }

  getLogoutUrl(): string {
    if (this.configurationLoaded()) {
      return this.configurationData.CAS_LOGOUT_URL;
    }

    return null;
  }

  getCasUrl(): string {
    if (this.configurationLoaded()) {
      return this.configurationData.CAS_URL;
    }

    return null;
  }

  getSearchUrl(): string {
    if (this.configurationLoaded()) {
      return this.configurationData.SEARCH_URL;
    }

    return null;
  }

  getArchivesSearchUrl(): string {
    if (this.configurationLoaded()) {
      return this.configurationData.ARCHIVES_SEARCH_URL;
    }

    return null;
  }

  getReferentialUrl(): string {
    if (this.configurationLoaded()) {
      return this.configurationData.REFERENTIAL_URL;
    }
    return null;
  }

  getPastisUrl(): string {
    if (this.configurationLoaded()) {
      return this.configurationData.PASTIS_URL;
    }
    return null;
  }

  getCollectUrl(): string {
    if (this.configurationLoaded()) {
      return this.configurationData.COLLECT_URL;
    }

    return null;
  }

  getHasSiteSelection(): boolean {
    if (this.configurationLoaded()) {
      return this.configurationData.UI?.hasSiteSelection;
    }
  }

  getConfigStringValue(key: string): string {
    if (this.configurationLoaded() && this.configurationData.hasOwnProperty(key)) {
      return this.configurationData[key];
    }

    return null;
  }

  getConfigNumberValue(key: string): number {
    return +this.getConfigStringValue(key);
  }

  getConfigObjectValue(key: string): any {
    if (this.configurationLoaded() && this.configurationData.hasOwnProperty(key)) {
      return this.configurationData[key];
    }

    return null;
  }

  /**
   * Navigate to given url or to the portal otherwise.
   * @param url URL to be redirected to.
   */
  redirect(url?: string) {
    setTimeout(() => (this.location.href = url ? url : this.getPortalUrl()), WARNING_DURATION);
  }

  getPlatformName(): string {
    if (this.configurationLoaded()) {
      return this.configurationData.PLATFORM_NAME;
    }

    return null;
  }

  public getCustomer(): string {
    if (this.configurationLoaded()) {
      return this.configurationData.CUSTOMER;
    }
  }

  isVitamEnabled(): boolean {
    if (this.configurationLoaded()) {
      return this.configurationData.VITAM?.enabled ?? true;
    }
    return true;
  }
}
