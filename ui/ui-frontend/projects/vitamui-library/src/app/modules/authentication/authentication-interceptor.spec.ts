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
import { HTTP_INTERCEPTORS, HttpClient, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { OAuthStorage } from 'angular-oauth2-oidc';
import { BehaviorSubject } from 'rxjs';
import { ConfigService } from '../config.service';
import { AppConfiguration } from '../models/app.configuration.interface';
import { AuthenticationInterceptor } from './authentication-interceptor';

describe('AuthenticationInterceptor', () => {
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    const config = { GATEWAY_ENABLED: true, ALLOWED_URLS: ['/identity-api'] } as AppConfiguration;
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: HTTP_INTERCEPTORS, useClass: AuthenticationInterceptor, multi: true },
        { provide: ConfigService, useValue: { config, config$: new BehaviorSubject(config) } },
        { provide: OAuthStorage, useValue: { getItem: () => 'token' } },
      ],
    });
    httpClient = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  const authorizationHeaderOf = (url: string): string | null => {
    httpClient.get(url).subscribe();
    const req = httpTestingController.expectOne(url);
    req.flush({});
    return req.request.headers.get('Authorization');
  };

  it('should add the bearer token to allowed urls of the application', () => {
    expect(authorizationHeaderOf('./identity-api/users')).toBe('Bearer token');
    expect(authorizationHeaderOf('/identity-api/users')).toBe('Bearer token');
    expect(authorizationHeaderOf(`${document.location.origin}/identity-api/users`)).toBe('Bearer token');
  });

  it('should not add the bearer token to urls that are not allowed', () => {
    expect(authorizationHeaderOf('/other-api/users')).toBeNull();
    expect(authorizationHeaderOf('/other-api/users?redirect=/identity-api')).toBeNull();
  });

  it('should not add the bearer token to urls of another origin', () => {
    expect(authorizationHeaderOf('https://other.example.com/identity-api/users')).toBeNull();
    expect(authorizationHeaderOf('https://other.example.com/?path=/identity-api')).toBeNull();
  });
});
