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
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { AuthService } from '../auth.service';
import { AuthUser } from '../models/user/auth-user.interface';
import { TenantSelectionService } from '../tenant-selection.service';

@Injectable({
  providedIn: 'root',
})
export class SecurityService {
  private authService = inject(AuthService);
  private tenantSelectionService = inject(TenantSelectionService);

  /**
   * Returns true if the logged user has any of the specified roles and false otherwise.
   */
  hasAnyRole(appId: string, tenantIdentifier = this.tenantSelectionService.getSelectedTenant().identifier, ...roles: string[]): boolean {
    const user = this.authService.user;
    return this.userHasAnyRole(user, appId, tenantIdentifier, roles);
  }

  /**
   * @Deprecated: use hasAnyRole() instead
   * Returns true if the logged user has any of the specified roles and false otherwise.
   */
  hasAnyRole$(
    appId: string,
    tenantIdentifier = this.tenantSelectionService.getSelectedTenant().identifier,
    ...roles: string[]
  ): Observable<boolean> {
    return this.authService.user$.pipe(
      map((user: AuthUser) => {
        return this.userHasAnyRole(user, appId, tenantIdentifier, roles);
      }),
    );
  }

  /**
   * Returns true if the logged user has the specified role and false otherwise.
   */
  hasRole(appId: string, role: string, tenantIdentifier?: number): boolean {
    return this.hasAnyRole(appId, tenantIdentifier, role);
  }

  /**
   * @Deprecated: use hasRole() instead
   * Returns true if the logged user has the specified role and false otherwise.
   */
  hasRole$(appId: string, role: string, tenantIdentifier?: number): Observable<boolean> {
    return this.hasAnyRole$(appId, tenantIdentifier, role);
  }

  private userHasAnyRole(user: AuthUser, appId: string, tenantIdentifier: number, roles: string[]) {
    if (user?.profileGroup?.profiles) {
      const appProfiles = user.profileGroup.profiles.filter((profile) => profile.applicationName === appId);
      const tenantProfiles = appProfiles.filter((profile) => profile.tenantIdentifier === tenantIdentifier);
      const rolesByTenant = tenantProfiles.map((profile) => profile.roles);
      // Flatten the rolesByTenant (which is an array of arrays) into a simple list of roles
      const userRoles: Array<{ name: string }> = [].concat.apply([], rolesByTenant);

      const userRolesNames = userRoles.map((role) => role.name);

      return roles.some((r) => userRolesNames.includes(r));
    }
    return false;
  }
}
