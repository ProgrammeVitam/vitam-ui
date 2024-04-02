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
import { OAuthService, OAuthSuccessEvent } from 'angular-oauth2-oidc';
import { from, Observable, zip } from 'rxjs';
import { AuthenticatorService } from './authenticator.service';
import { map, skipWhile, take, tap } from 'rxjs/operators';

export class OidcAuthenticatorService implements AuthenticatorService {
  constructor(
    private oAuthService: OAuthService,
    private location: any,
  ) {}

  public login(): Observable<boolean> {
    const url = new URL(this.location.href);
    const isSubrogation = !!url.searchParams.get('isSubrogation');
    if (isSubrogation) {
      return from(
        this.surrogateUser(
          url.searchParams.get('superUserEmail'),
          url.searchParams.get('superUserCustomerId'),
          url.searchParams.get('surrogateEmail'),
          url.searchParams.get('surrogateCustomerId'),
        ),
      );
    }
    const hasUsername = !!url.searchParams.get('username');
    if (hasUsername) {
      return from(this.initLoginWithUserName(url.searchParams.get('username')));
    }

    const urlCleaner = this.oAuthService.events.pipe(
      skipWhile((type) => !(type instanceof OAuthSuccessEvent)),
      take(1),
      tap(() => this.cleanUrlAfterLogin()),
      map(() => true),
    );
    return zip(from(this.oAuthService.loadDiscoveryDocumentAndLogin()), urlCleaner).pipe(
      map(([authenticated, urlCleaned]) => authenticated && urlCleaned),
    );
  }

  private surrogateUser(superUser: string, superUserCustomerId: string, surrogate: string, surrogateCustomerId: string): Promise<boolean> {
    return this.oAuthService.loadDiscoveryDocument().then(() => {
      const postLogoutUri = this.oAuthService.postLogoutRedirectUri;
      const redirectUri = postLogoutUri + this.getUrlSeparator(postLogoutUri);
      this.oAuthService.redirectUri = redirectUri;
      this.oAuthService.initCodeFlow('', {
        superUserEmail: superUser,
        superUserCustomerId,
        surrogateEmail: surrogate,
        surrogateCustomerId,
        redirect_uri: redirectUri,
      });
      return true;
    });
  }

  private initLoginWithUserName(username: string): Promise<boolean> {
    return this.oAuthService.loadDiscoveryDocument().then(() => {
      const postLogoutUri = this.oAuthService.postLogoutRedirectUri;
      const redirectUri = postLogoutUri + this.getUrlSeparator(postLogoutUri);
      this.oAuthService.redirectUri = redirectUri;
      this.oAuthService.initCodeFlow('', { username, redirect_uri: redirectUri });
      return true;
    });
  }

  public logout(): void {
    this.oAuthService.revokeTokenAndLogout();
  }

  public logoutSubrogationAndRedirectToLoginPage(username: string) {
    const oldPostLogoutRedirectUri = this.oAuthService.postLogoutRedirectUri;
    const separator = this.getUrlSeparator(oldPostLogoutRedirectUri);
    const usernamePayload = 'username=' + username;
    this.oAuthService.postLogoutRedirectUri = oldPostLogoutRedirectUri + separator + usernamePayload;
    this.oAuthService.revokeTokenAndLogout();
  }

  public initSubrogationFlow(superUser: string, superUserCustomerId: string, surrogate: string, surrogateCustomerId: string) {
    const oldPostLogoutRedirectUri = this.oAuthService.postLogoutRedirectUri;
    const separator = this.getUrlSeparator(oldPostLogoutRedirectUri);
    const subrogationPayload =
      'isSubrogation=true' +
      '&superUserEmail=' +
      superUser +
      '&superUserCustomerId=' +
      superUserCustomerId +
      '&surrogateEmail=' +
      surrogate +
      '&surrogateCustomerId=' +
      surrogateCustomerId;
    this.oAuthService.postLogoutRedirectUri = oldPostLogoutRedirectUri + separator + subrogationPayload;
    this.oAuthService.revokeTokenAndLogout();
  }

  public redirectToLoginPage(): void {
    this.oAuthService.logOut();
    this.oAuthService.initCodeFlow();
  }

  private getUrlSeparator(url: string): string {
    const questionMarkIndex = url.indexOf('?');
    if (url.length === questionMarkIndex + 1) {
      return '';
    }
    return questionMarkIndex > -1 ? '&' : '?';
  }

  private cleanUrlAfterLogin() {
    let url =
      this.location.origin +
      this.location.pathname +
      this.location.search
        .replace(/nonce=[^&\$]*/, '')
        .replace(/client_id=[^&\$]*/, '')
        .replace(/isSubrogation=[^&\$]*/, '')
        .replace(/^\?&/, '?')
        .replace(/&$/, '')
        .replace(/^\?$/, '')
        .replace(/&+/g, '&')
        .replace(/\?&/, '?')
        .replace(/\?$/, '');
    history.replaceState(null, window.name, url);
  }
}
