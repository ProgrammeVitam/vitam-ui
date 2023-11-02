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
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { OAuthStorage } from 'angular-oauth2-oidc';
import { Observable } from 'rxjs';
import { catchError, first, switchMap } from 'rxjs/operators';
import { ConfigService } from '../config.service';
import { Logger } from '../logger/logger';

@Injectable()
export class AuthenticationInterceptor implements HttpInterceptor {
  constructor(private authStorage: OAuthStorage, private configService: ConfigService, private logger: Logger) {}

  private checkUrl(url: string): boolean {
    const found = this.configService.config.ALLOWED_URLS.find((u) => url.includes(u));
    return !!found;
  }

  public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    /**
     * Manage uri that are charged before the configuration is loaded
     * and should not be impacted by security.
     * Like configurations and assets.
     *
     * /!\ Http request that are not triggered may be due to this.
     * /!\ Check if the request is triggered in a module initialization.
     */
    const url = req.url.toLowerCase();
    if (url.endsWith('/conf') || url.includes('assets/shared-i18n/') || url.includes('assets/i18n/')) {
      return next.handle(req);
    }

    /**
     * Wait for the configuration to be loaded and add OAuth bearer token header.
     * Only if the url is in the list of allowed urls and Gateway is enabled.
     */
    return this.configService.config$.pipe(
      first((config) => !!config),
      switchMap((_) => {
        const sendAccessToken = this.configService.config?.GATEWAY_ENABLED;
        if (sendAccessToken) {
          if (!this.checkUrl(url)) {
            return next.handle(req);
          }
          const token = this.authStorage.getItem('access_token');
          const header = 'Bearer ' + token;
          const headers = req.headers.set('Authorization', header);
          req = req.clone({ headers });
        }

        return next.handle(req);
      }),
      catchError((err) => {
        this.logger.error(this, err);
        return next.handle(req);
      })
    );
  }
}
