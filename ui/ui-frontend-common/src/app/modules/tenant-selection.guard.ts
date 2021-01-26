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
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRouteSnapshot, CanActivate, CanActivateChild, Router } from '@angular/router';
import { ApplicationService } from './application.service';
import { AuthService } from './auth.service';
import { WINDOW_LOCATION } from './injection-tokens';
import { TenantsByApplication } from './models/user/tenants-by-application.interface';
import { TENANT_SELECTION_URL_CONDITION } from './tenant-selection.service';
import { TenantSelectionService } from './tenant-selection.service';

@Injectable({
  providedIn: 'root'
})
export class TenantSelectionGuard implements CanActivate, CanActivateChild {

  constructor(
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private appService: ApplicationService,
    private router: Router,
    private tenantService: TenantSelectionService,
    @Inject(WINDOW_LOCATION) private location: any) {
  }

  canActivate(
    route: ActivatedRouteSnapshot
  ): boolean {
    if (route.params.tenantIdentifier) {
      return true;
    } else if (this.tenantService.getSelectedTenant()) {
      const application = this.appService.applications.find((appFromService) => appFromService.identifier === route.data.appId);
      this.location.href = application.url + TENANT_SELECTION_URL_CONDITION + this.tenantService.getSelectedTenant().identifier;
    }

    const tenantsByApp: TenantsByApplication = this.authService.user.tenantsByApp.find((element) => element.name === route.data.appId);
    if (tenantsByApp) {
      const tenants = tenantsByApp.tenants;
      if (tenants.length === 1) {
        // redirect user to the unique tenant page of the app
        const application = this.appService.applications.find((appFromService) => appFromService.identifier === route.data.appId);
        this.router.navigate(route.pathFromRoot.map(r => r.url.toString()).concat([tenants[0].identifier.toString()]));
        return true;
      } else {
        return true;
      }
    } else {
      this.snackBar.open('Liste des tenants introuvable pour :' + route.data.appId + '.', null, {
        duration: 4000,
      });
    }

    return false;
  }

  canActivateChild(route: ActivatedRouteSnapshot): boolean {
    return this.canActivate(route);
  }

}
