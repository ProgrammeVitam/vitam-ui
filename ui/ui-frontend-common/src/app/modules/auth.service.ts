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
import { BehaviorSubject } from 'rxjs';

import { ApplicationId } from './application-id.enum';
import { WINDOW_LOCATION } from './injection-tokens';
import { AuthUser, Tenant } from './models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  set user(user: AuthUser) {
    this._user = user;
    this.userLoaded.next(user);
  }
  get user(): AuthUser {
    return this._user;
  }
  // tslint:disable-next-line:variable-name
  private _user: AuthUser;

  loginUrl: string;
  logoutUrl: string;
  logoutRedirectUiUrl: string;
  userLoaded = new BehaviorSubject<AuthUser>(null);

  constructor(@Inject(WINDOW_LOCATION) private location: any) {}

  logout() {
    this.user = null;
    this.location.href = this.logoutUrl;
  }

  logoutForSubrogation(superUser: string, surrogate: string) {
    const email = surrogate + ',' + superUser;
    this.logoutAndRedirectToUiForUser(email);
  }

  logoutAndRedirectToUI() {
    this.user = null;
    this.location.href = this.logoutRedirectUiUrl;
  }

  logoutAndRedirectToUiForUser(email: string) {
    this.user = null;
    this.location.href = this.logoutRedirectUiUrl + '?cas_username=' + email;
  }

  logoutWithEmailWithOutRedirectToUi(email: string) {
    this.user = null;
    let casLogoutUrl = this.logoutUrl;
    if (casLogoutUrl && casLogoutUrl.endsWith('/')) {
      casLogoutUrl = casLogoutUrl.substr(0, casLogoutUrl.lastIndexOf('/') - 1);
    }
    this.location.href = casLogoutUrl + '?cas_username=' + email;
  }

  getAnyTenantIdentifier(): string {
    // retrieve any tenantIdentifier fro a profile
    let tenantIdentifier = null;
    if (this._user) {
      tenantIdentifier = this._user.profileGroup.profiles[0].tenantIdentifier.toString();
    }

    return tenantIdentifier;
  }

  getTenantByIdentifier(identifier: number): Tenant {
    let tenant;
    if (this._user) {
      const tenants = this._user.tenantsByApp.reduce((acc, x) => acc.concat(x.tenants), []);
      tenant = tenants.find((t) => t.identifier === identifier);
    }

    return tenant;
  }

  getTenantByAppAndIdentifier(appId: ApplicationId, tenantIdentifier: number): Tenant {
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

}
