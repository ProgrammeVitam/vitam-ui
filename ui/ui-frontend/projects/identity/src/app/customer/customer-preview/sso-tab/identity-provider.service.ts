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
import { Observable, Subject } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { Criterion, CriteriaSearchQuery, IdentityProvider, Operators, VitamUISnackBarService } from 'vitamui-library';

import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { ProviderApiService } from './provider-api.service';

@Injectable({
  providedIn: 'root',
})
export class IdentityProviderService {
  updated = new Subject<IdentityProvider>();

  constructor(
    private providerApi: ProviderApiService,
    private snackBarService: VitamUISnackBarService,
  ) {}

  create(idp: IdentityProvider): Observable<IdentityProvider> {
    return this.providerApi.create(idp).pipe(
      map((newIDP: IdentityProvider) => this.addMetadataUrl(newIDP)),
      map((updatedIdp: IdentityProvider) => this.addSpMetadataUrl(updatedIdp)),
      tap(
        (newIDP: IdentityProvider) => {
          let messageKey = newIDP.internal ? 'SHARED.SNACKBAR.PROVIDER_CREATE' : 'SHARED.SNACKBAR.PROVIDER_CREATE_RESTART_NEED';

          this.snackBarService.open({
            message: messageKey,
            translateParams: {
              param1: newIDP.name,
            },
          });
        },
        (error) => {
          this.snackBarService.open({ message: error.error.message, translate: false });
        },
      ),
    );
  }

  patch(idp: { id: string; [key: string]: any }): Observable<IdentityProvider> {
    return this.providerApi.patch(idp).pipe(
      map((updatedIdp: IdentityProvider) => this.addMetadataUrl(updatedIdp)),
      map((updatedIdp: IdentityProvider) => this.addSpMetadataUrl(updatedIdp)),
      tap((updatedIdp: IdentityProvider) => this.updated.next(updatedIdp)),
      tap(
        (updatedIdp: IdentityProvider) => {
          let messageKey = updatedIdp.internal ? 'SHARED.SNACKBAR.PROVIDER_UPDATE' : 'SHARED.SNACKBAR.PROVIDER_UPDATE_RESTART_NEED';
          this.snackBarService.open({
            message: messageKey,
            translateParams: {
              param1: updatedIdp.name,
            },
          });
        },
        (error) => {
          this.snackBarService.open({ message: error.error.message, translate: false });
        },
      ),
    );
  }

  updateMetadataFile(id: string, idpMetadata: File): Observable<IdentityProvider> {
    return this.providerApi.patchProviderIdpMetadata(id, idpMetadata).pipe(
      map((updatedIdp: IdentityProvider) => this.addMetadataUrl(updatedIdp)),
      map((updatedIdp: IdentityProvider) => this.addSpMetadataUrl(updatedIdp)),
      tap((updatedIdp: IdentityProvider) => this.updated.next(updatedIdp)),
      tap(
        (updatedIdp: IdentityProvider) => {
          let messageKey = updatedIdp.internal ? 'SHARED.SNACKBAR.PROVIDER_UPDATE' : 'SHARED.SNACKBAR.PROVIDER_UPDATE_RESTART_NEED';
          this.snackBarService.open({
            message: messageKey,
            translateParams: {
              param1: updatedIdp.name,
            },
          });
        },
        (error) => {
          this.snackBarService.open({ message: error.error.message, translate: false });
        },
      ),
    );
  }

  updateKeystore(id: string, file: File, password: string): Observable<IdentityProvider> {
    return this.providerApi.patchProviderKeystore(id, file, password).pipe(
      map((updatedIdp: IdentityProvider) => this.addMetadataUrl(updatedIdp)),
      tap((updatedIdp: IdentityProvider) => this.updated.next(updatedIdp)),
      tap(
        (updatedIdp: IdentityProvider) => {
          let messageKey = updatedIdp.internal ? 'SHARED.SNACKBAR.PROVIDER_UPDATE' : 'SHARED.SNACKBAR.PROVIDER_UPDATE_RESTART_NEED';
          this.snackBarService.open({
            message: messageKey,
            translateParams: {
              param1: updatedIdp.name,
            },
          });
        },
        (error) => {
          this.snackBarService.open({ message: error.error.message, translate: false });
        },
      ),
    );
  }
  getAll(customerId?: string): Observable<IdentityProvider[]> {
    const criterionArray: Criterion[] = [];
    if (customerId) {
      criterionArray.push({ key: 'customerId', value: customerId, operator: Operators.equals });
    }

    const query: CriteriaSearchQuery = { criteria: criterionArray };

    const httpParams = new HttpParams().set('criteria', JSON.stringify(query));

    return this.providerApi.getAll(httpParams).pipe(
      map((identityProviders) => {
        return identityProviders.map((identityProvider) => this.addMetadataUrl(identityProvider));
      }),
      map((identityProviders) => {
        return identityProviders.map((identityProvider) => this.addSpMetadataUrl(identityProvider));
      }),
    );
  }

  getDomainByCustomerId(customerId: string): Observable<string[]> {
    return this.getAll(customerId).pipe(
      map((identityProviders: IdentityProvider[]) => {
        return identityProviders.reduce((acc, idp) => acc.concat(idp.patterns.map((p) => p.replace('.*@', ''))), []);
      }),
    );
  }

  private addMetadataUrl(identityProvider: IdentityProvider): IdentityProvider {
    identityProvider.idpMetadataUrl = this.providerApi.buildMetadataUrl(identityProvider.id);
    return identityProvider;
  }

  private addSpMetadataUrl(identityProvider: IdentityProvider): IdentityProvider {
    identityProvider.spMetadataUrl = this.providerApi.buildSpMetadataUrl(identityProvider.id);
    return identityProvider;
  }
}
