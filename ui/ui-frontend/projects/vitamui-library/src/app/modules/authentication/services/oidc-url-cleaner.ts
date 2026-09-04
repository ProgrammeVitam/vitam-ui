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
import { Injectable } from '@angular/core';

/**
 * Service dedicated to cleaning and validating URLs during OIDC authentication flows.
 */
@Injectable({
  providedIn: 'root',
})
export class OidcUrlCleaner {
  private readonly OIDC_PARAMS = [
    'code',
    'state',
    'id_token',
    'access_token',
    'token_type',
    'session_state',
    'nonce',
    'error',
    'error_description',
    'client_id',
    'isSubrogation',
    'superUserEmail',
    'superUserCustomerId',
    'surrogateEmail',
    'surrogateCustomerId',
  ];

  /** Removes all OIDC-related parameters from the provided URL object */
  public removeOidcParams(url: URL): void {
    this.OIDC_PARAMS.forEach((p) => url.searchParams.delete(p));
  }

  /** Returns a relative path (pathname + search) cleaned of OIDC parameters */
  public getCleanedPath(pathWithSearch: string, origin: string): string {
    const url = new URL(pathWithSearch, origin);
    this.removeOidcParams(url);
    return url.pathname + (url.search || '');
  }

  /**
   * Resolves a valid absolute redirect URI.
   * If the provided URI is not absolute or is invalid, it returns the current URL (cleaned).
   */
  public resolveValidRedirectUri(uri: string | undefined, currentHref: string): string {
    if (this.isAbsoluteUrl(uri)) {
      return uri!;
    }

    const url = new URL(currentHref);
    this.removeOidcParams(url);
    return url.origin + url.pathname + url.search;
  }

  private isAbsoluteUrl(url: string | undefined): boolean {
    if (!url) {
      return false;
    }
    try {
      const u = new URL(url);
      return !!u.host && !!u.protocol;
    } catch {
      return false;
    }
  }
}
