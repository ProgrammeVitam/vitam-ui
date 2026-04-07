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
import { map, tap } from 'rxjs/operators';

const OIDC_PARAMS = ['code', 'state', 'id_token', 'access_token', 'token_type', 'session_state', 'nonce'];

export class OidcAuthenticatorService implements AuthenticatorService {
  constructor(
    private oAuthService: OAuthService,
    private location: any,
  ) {}

  public login(): Observable<boolean> {
    const url = new URL(this.location.href);
    const returnUrl = this.cleanOidcParams(this.location.pathname + this.location.search);

    if (url.searchParams.get('isSubrogation')) {
      return from(this.startSubrogationFlow(url, returnUrl));
    }

    if (url.searchParams.get('username')) {
      return from(this.startLoginWithUsername(url, returnUrl));
    }

    return this.startStandardLogin(returnUrl);
  }

  private async startSubrogationFlow(url: URL, returnUrl: string): Promise<boolean> {
    await this.oAuthService.loadDiscoveryDocument();
    this.oAuthService.redirectUri = this.buildAbsoluteRedirectUri();
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
    this.oAuthService.redirectUri = this.buildAbsoluteRedirectUri();
    this.oAuthService.initCodeFlow(returnUrl, { username: url.searchParams.get('username') });
    return true;
  }

  private startStandardLogin(returnUrl: string): Observable<boolean> {
    if (this.oAuthService.hasValidAccessToken()) {
      return from(this.oAuthService.loadDiscoveryDocument()).pipe(map(() => true));
    }

    return from(this.oAuthService.loadDiscoveryDocumentAndLogin({ state: returnUrl })).pipe(
      tap((authenticated) => {
        if (authenticated) {
          this.cleanUrlAfterLogin();
          const claims = this.oAuthService.getIdentityClaims() as any;
          if (claims?.state) {
            const parts = claims.state.split(';');
            // The state format is `<nonce>;<encoded_url>` — we take parts[1].
            const redirectPath = parts.length > 1 ? decodeURIComponent(parts[1]) : null;
            if (redirectPath && redirectPath !== '/') {
              this.location.href = this.location.origin + redirectPath;
            }
          }
        }
      }),
    );
  }

  public logout(): void {
    this.oAuthService.revokeTokenAndLogout();
  }

  public logoutSubrogationAndRedirectToLoginPage(username: string): void {
    this.oAuthService.postLogoutRedirectUri += this.getUrlSeparator(this.oAuthService.postLogoutRedirectUri) + 'username=' + username;
    this.oAuthService.revokeTokenAndLogout();
  }

  public initSubrogationFlow(superUser: string, superUserCustomerId: string, surrogate: string, surrogateCustomerId: string): void {
    const sep = this.getUrlSeparator(this.oAuthService.postLogoutRedirectUri);
    this.oAuthService.postLogoutRedirectUri +=
      sep +
      `isSubrogation=true&superUserEmail=${superUser}&superUserCustomerId=${superUserCustomerId}&surrogateEmail=${surrogate}&surrogateCustomerId=${surrogateCustomerId}`;
    this.oAuthService.revokeTokenAndLogout();
  }

  public redirectToLoginPage(): void {
    this.oAuthService.revokeTokenAndLogout();
  }

  private cleanOidcParams(path: string): string {
    const u = new URL(path, this.location.origin);
    OIDC_PARAMS.forEach((p) => u.searchParams.delete(p));
    return u.pathname + (u.search || '');
  }

  private cleanUrlAfterLogin(): void {
    history.replaceState(null, '', this.cleanOidcParams(this.location.pathname + this.location.search));
  }

  private getUrlSeparator(url: string): string {
    const idx = url.indexOf('?');
    if (idx === -1) return '?';
    if (idx === url.length - 1) return '';
    return '&';
  }

  private buildAbsoluteRedirectUri(): string {
    const postLogoutUri = this.oAuthService.postLogoutRedirectUri;

    try {
      const u = new URL(postLogoutUri);
      const forbiddenParams = ['code', 'state', 'id_token', 'access_token', 'token_type', 'session_state'];
      forbiddenParams.forEach((p) => u.searchParams.delete(p));
      return u.toString();
    } catch {
      return this.location.origin + postLogoutUri;
    }
  }
}
