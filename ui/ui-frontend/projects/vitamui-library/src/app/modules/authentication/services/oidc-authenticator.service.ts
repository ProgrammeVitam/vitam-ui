/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { OAuthService } from 'angular-oauth2-oidc';
import { from, Observable } from 'rxjs';
import { AuthenticatorService } from './authenticator.service';
import { filter, first, map, tap } from 'rxjs/operators';
import { ConfigService } from '../../config.service';
import { OidcUrlCleaner } from './oidc-url-cleaner';
import { inject, Injectable } from '@angular/core';
import { WINDOW_LOCATION } from '../../injection-tokens';

interface IdentityClaims {
  state?: string;
}

@Injectable({
  providedIn: 'root',
})
export class OidcAuthenticatorService implements AuthenticatorService {
  private oAuthService = inject(OAuthService);
  private configService = inject(ConfigService);
  private urlCleaner = inject(OidcUrlCleaner);
  private location: any = inject(WINDOW_LOCATION); // Native Location object

  constructor() {
    this.configService.config$.pipe(first((config) => !!config?.OIDC_CONFIG)).subscribe((config) => {
      const oidcConfig = { ...config.OIDC_CONFIG };
      // Initialize deep linking support
      oidcConfig.redirectUri = this.urlCleaner.resolveValidRedirectUri(oidcConfig.redirectUri, this.location.href);
      oidcConfig.postLogoutRedirectUri = this.urlCleaner.resolveValidRedirectUri(oidcConfig.postLogoutRedirectUri, this.location.href);
      this.oAuthService.configure(oidcConfig);
    });
  }

  public login(): Observable<boolean> {
    const url = new URL(this.location.href);
    const returnUrl = this.urlCleaner.getCleanedPath(this.location.pathname + this.location.search, this.location.origin);

    // Ensure redirectUri is up-to-date with current URL (important for subrogation and deep links)
    this.oAuthService.redirectUri = this.urlCleaner.resolveValidRedirectUri(this.oAuthService.redirectUri, this.location.href);

    const isSubrogation = url.searchParams.get('isSubrogation');
    const username = url.searchParams.get('username');

    if (isSubrogation) {
      return from(this.startSubrogationFlow(url, returnUrl));
    }

    if (username) {
      return from(this.startLoginWithUsername(url, returnUrl));
    }

    return this.startStandardLogin(returnUrl);
  }

  private async startSubrogationFlow(url: URL, returnUrl: string): Promise<boolean> {
    await this.oAuthService.loadDiscoveryDocument();
    this.oAuthService.initCodeFlow(returnUrl, {
      superUserEmail: url.searchParams.get('superUserEmail'),
      superUserCustomerId: url.searchParams.get('superUserCustomerId'),
      surrogateEmail: url.searchParams.get('surrogateEmail'),
      surrogateCustomerId: url.searchParams.get('surrogateCustomerId'),
    });
    return true;
  }

  private async startLoginWithUsername(url: URL, returnUrl: string): Promise<boolean> {
    await this.oAuthService.loadDiscoveryDocument();
    this.oAuthService.initCodeFlow(returnUrl, { username: url.searchParams.get('username') });
    return true;
  }

  private startStandardLogin(returnUrl: string): Observable<boolean> {
    if (this.oAuthService.hasValidAccessToken()) {
      return from(this.oAuthService.loadDiscoveryDocument()).pipe(map(() => true));
    }

    return from(this.oAuthService.loadDiscoveryDocumentAndLogin({ state: returnUrl })).pipe(
      filter((authenticated) => authenticated),
      tap(() => this.cleanUrlAfterLogin()),
    );
  }

  public logout(): void {
    this.oAuthService.revokeTokenAndLogout().then();
  }

  public logoutSubrogationAndRedirectToLoginPage(username: string): void {
    this.updatePostLogoutRedirectUri({ username });
    this.logout();
  }

  public initSubrogationFlow(superUser: string, superUserCustomerId: string, surrogate: string, surrogateCustomerId: string): void {
    this.updatePostLogoutRedirectUri({
      isSubrogation: 'true',
      superUser,
      superUserCustomerId,
      surrogate,
      surrogateCustomerId,
    });
    this.logout();
  }

  public redirectToLoginPage(): void {
    this.logout();
  }

  /** Update postLogoutRedirectUri by adding search parameters */
  private updatePostLogoutRedirectUri(params: Record<string, string>): void {
    const url = new URL(this.oAuthService.postLogoutRedirectUri, this.location.origin);
    Object.entries(params).forEach(([key, value]) => url.searchParams.set(key, value));
    this.oAuthService.postLogoutRedirectUri = url.toString();
  }

  /** Clean address bar URL after authentication */
  private cleanUrlAfterLogin(): void {
    const cleanedPath = this.urlCleaner.getCleanedPath(this.location.pathname + this.location.search, this.location.origin);

    history.replaceState(null, '', cleanedPath);

    const claims = this.oAuthService.getIdentityClaims() as IdentityClaims | null;
    const [, encodedRedirectPath] = claims?.state?.split(';') ?? [];

    if (!encodedRedirectPath) {
      return;
    }

    const redirectPath = decodeURIComponent(encodedRedirectPath);

    if (redirectPath === '/' || !redirectPath.startsWith('/')) {
      return;
    }

    this.location.assign(this.location.origin + redirectPath);
  }
}
