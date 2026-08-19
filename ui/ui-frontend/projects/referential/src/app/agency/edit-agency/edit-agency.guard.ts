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
import { SecurityService, TenantSelectionService } from 'vitamui-library';
import { ApplicationId, Role } from 'vitamui-library';
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ROUTES, ERROR_MESSAGES } from './edit-agency.constants';

const accessDenied = (router: Router): Observable<boolean> => {
  router.navigateByUrl(ROUTES.ACCESS_DENIED);
  return of(false);
};

export const EditAgencyGuard: CanActivateFn = (route, _state): Observable<boolean> => {
  const securityService = inject(SecurityService);
  const tenantSelectionService = inject(TenantSelectionService);
  const router = inject(Router);

  const tenantIdentifier = +route.paramMap.get('tenantIdentifier') || tenantSelectionService.getSelectedTenant()?.identifier;
  const agencyIdentifier = route.paramMap.get('agencyIdentifier');

  if (!tenantIdentifier) console.error(ERROR_MESSAGES.MISSING_TENANT);
  if (!agencyIdentifier) console.error(ERROR_MESSAGES.MISSING_AGENCY);
  if (!tenantIdentifier || !agencyIdentifier) {
    return accessDenied(router);
  }

  // Vérification des permissions
  return securityService.hasRole$(ApplicationId.AGENCIES_APP, Role.ROLE_UPDATE_AGENCIES, tenantIdentifier).pipe(
    map((hasPermission) => {
      if (!hasPermission) router.navigateByUrl(ROUTES.AGENCY_DETAILS(tenantIdentifier, agencyIdentifier)).then();
      return hasPermission;
    }),
    catchError((error) => {
      console.error(ERROR_MESSAGES.PERMISSION_CHECK, error);
      return accessDenied(router);
    }),
  );
};
