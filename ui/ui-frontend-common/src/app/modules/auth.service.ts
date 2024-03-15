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
import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { filter, skipWhile, switchMap, take } from 'rxjs/operators';
import { ApplicationId } from './application-id.enum';
import { AuthenticatorService } from './authentication/services/authenticator.service';
import { AuthUser, Tenant, UserInfo } from './models';
@Injectable({
  providedIn: 'root',
})
export class AuthService implements OnDestroy {
  get userInfo(): UserInfo {
    return this._userInfo;
  }

  set userInfo(userInfo: UserInfo) {
    this._userInfo = userInfo;
    this.userInfo$.next(userInfo);
  }

  set user(user: AuthUser) {
    this._user = user;
    this.user$.next(user);
  }

  get user(): AuthUser {
    return this._user;
  }

  public loginUrl: string;
  public logoutUrl: string;
  public logoutRedirectUiUrl: string;
  public user$ = new BehaviorSubject<AuthUser>(null);

  private authenticatorService: AuthenticatorService;

  // tslint:disable-next-line:variable-name
  private _user: AuthUser;
  // tslint:disable-next-line:variable-name
  private _userInfo: UserInfo;
  private userInfo$ = new BehaviorSubject<UserInfo>(null);

  private gatewayEnabled = false;

  private isAuthenticatorConfigReady$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  constructor() {}

  ngOnDestroy(): void {
    this.isAuthenticatorConfigReady$.complete();
  }

  public configure(gatewayEnabled: boolean, authenticatorService: AuthenticatorService) {
    this.gatewayEnabled = gatewayEnabled;
    this.authenticatorService = authenticatorService;
    this.isAuthenticatorConfigReady$.next(true);
  }

  public login(): Observable<boolean> {
    return this.isAuthenticatorConfigReady$.pipe(
      skipWhile((ready) => !ready),
      switchMap(() => this.authenticatorService.login()),
    );
  }

  public logout(): void {
    this.user = null;
    this.authenticatorService.logout();
  }

  public logoutForSubrogation(superUser: string, superUserCustomerId: string, surrogate: string, surrogateCustomerId: string) {
    this.user = null;
    this.authenticatorService.initSubrogationFlow(superUser, superUserCustomerId, surrogate, surrogateCustomerId);
  }

  public logoutAndRedirectToUiForUser(superUser: string) {
    this.user = null;
    this.authenticatorService.logoutSubrogationAndRedirectToLoginPage(superUser);
  }

  public redirectToLoginPage(): void {
    this.user = null;
    this.authenticatorService.redirectToLoginPage();
  }

  public getAnyTenantIdentifier(): string {
    // retrieve any tenantIdentifier fro a profile
    let tenantIdentifier = null;
    if (this._user) {
      tenantIdentifier = this._user.profileGroup.profiles[0].tenantIdentifier.toString();
    }

    return tenantIdentifier;
  }

  public getTenantByIdentifier(identifier: number): Tenant {
    let tenant;
    if (this._user) {
      const tenants = this._user.tenantsByApp.reduce((acc, x) => acc.concat(x.tenants), []);
      tenant = tenants.find((t) => t.identifier === identifier);
    }

    return tenant;
  }

  public getTenantByAppAndIdentifier(appId: ApplicationId, tenantIdentifier: number): Tenant {
    if (!this._user) {
      console.error(`AuthService Error: user is null`);

      return null;
    }

    const app = this._user.tenantsByApp.find((appTenants) => appTenants.name === appId);
    if (!app) {
      console.error(`AuthService Error: can\'t find application with id "${appId}"`);
      return null;
    }

    const tenant = app.tenants.find((t) => t.identifier === tenantIdentifier);
    if (!tenant) {
      console.error(`AuthService Error: can\'t find tenant with id "${tenantIdentifier}"`);
      return null;
    }
    return tenant;
  }

  public getUser$(): Observable<AuthUser> {
    return this.user$.pipe(
      filter((user: AuthUser) => !!user),
      take(1),
    );
  }

  public getUserInfo$(): Observable<UserInfo> {
    return this.userInfo$.pipe(
      filter((userInfo: UserInfo) => !!userInfo),
      take(1),
    );
  }
}
