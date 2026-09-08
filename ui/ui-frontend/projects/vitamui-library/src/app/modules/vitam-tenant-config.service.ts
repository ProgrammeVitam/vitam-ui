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
import { computed, inject, Injectable, signal } from '@angular/core';
import { Logger } from './logger/logger';
import { ConfigurationsApiService, TenantConfiguration } from './services/configurations-api.service';
import { lastValueFrom, Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class VitamTenantConfigService {
  private readonly logger = inject(Logger);
  private readonly configurationsApi = inject(ConfigurationsApiService);

  private readonly _tenantConfig = signal<TenantConfiguration | null>(null);
  private readonly _lastLoaded = signal<number | null>(null);

  private readonly TTL_MS = 60_000;

  readonly tenantConfig = this._tenantConfig.asReadonly();
  readonly isLoaded = computed(() => this._tenantConfig() !== null);

  loadAsPromise(): Promise<TenantConfiguration | null> {
    return lastValueFrom(this.load());
  }

  load(): Observable<TenantConfiguration | null> {
    if (this.isCacheValid()) {
      return of(this._tenantConfig());
    }

    return this.configurationsApi.getConfiguration().pipe(
      tap((config) => {
        this._tenantConfig.set(config);
        this._lastLoaded.set(Date.now());
      }),
      catchError((error) => {
        this.logger.error(this, error);
        return of(null);
      }),
    );
  }

  get(): TenantConfiguration | null {
    return this._tenantConfig();
  }

  private isCacheValid(): boolean {
    const lastLoaded = this._lastLoaded();
    return lastLoaded !== null && Date.now() - lastLoaded < this.TTL_MS;
  }
}
