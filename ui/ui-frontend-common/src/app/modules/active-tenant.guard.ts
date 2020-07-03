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
import { Observable } from 'rxjs';

import { Inject, Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, CanActivateChild, Router } from '@angular/router';

import { ApplicationService } from './application.service';
import { AuthService } from './auth.service';
import { GlobalEventService } from './global-event.service';
import { WINDOW_LOCATION } from './injection-tokens';
import { Tenant } from './models';
import { TenantsByApplication } from './models/user/tenants-by-application.interface';
import { StartupService } from './startup.service';

@Injectable({
  providedIn: 'root'
})
export class ActiveTenantGuard implements CanActivate, CanActivateChild {

  constructor(
    private authService: AuthService,
    private appService: ApplicationService,
    private startupService: StartupService,
    private globalEventService: GlobalEventService,
    private router: Router,
    @Inject(WINDOW_LOCATION) private location: any,
  ) { }

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean> | boolean {
    return this.checkTenants(route);
  }

  canActivateChild(route: ActivatedRouteSnapshot): Observable<boolean> | boolean {
    return this.canActivate(route);
  }

  checkTenants(route: ActivatedRouteSnapshot): Observable<boolean> | boolean {
    const tenantIdentifier = route.paramMap.get('tenantIdentifier');
    const tenantsByApp: TenantsByApplication = this.authService.user.tenantsByApp.find((element) => element.name === route.data.appId);
    if (tenantsByApp) {
      const result = tenantsByApp.tenants.find((tenant: Tenant) => tenant.identifier === +tenantIdentifier);
      if (result) {
        // set tenant Identifier whenever a tenant is selected
        this.startupService.setTenantIdentifier(tenantIdentifier);
        // emit tenant change event
        this.globalEventService.tenantEvent.next(tenantIdentifier);

        return true;
      }
    }
    // redirect user to the tenant selection page
    const application = this.appService.applications.find((appFromService) => appFromService.identifier === route.data.appId);
    this.router.navigate(route.pathFromRoot.map(r => r.url.toString()).concat(['tenant']));

    return false;
  }

}
