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
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { APP_INITIALIZER, Inject, ModuleWithProviders, NgModule } from '@angular/core';
import { OAuthModule, OAuthService } from 'angular-oauth2-oidc';
import { first, tap } from 'rxjs/operators';
import { AuthService } from '../auth.service';
import { ConfigService } from '../config.service';
import { WINDOW_LOCATION } from '../injection-tokens';
import { AuthenticationInterceptor } from './authentication-interceptor';
import { AuthenticatorService } from './services/authenticator.service';
import { CasAuthenticatorService } from './services/cas-authenticator.service';
import { OidcAuthenticatorService } from './services/oidc-authenticator.service';

@NgModule({
  declarations: [],
  imports: [],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: AuthenticationModule.loadModule,
      multi: true,
    },
  ],
})
export class AuthenticationModule {
  private static loading: Promise<any> = null;

  private static loadModule() {
    // tslint:disable-next-line: whitespace semicolon
    return () => this.loading;
  }

  constructor(
    private configService: ConfigService,
    private oAuthService: OAuthService,
    @Inject(WINDOW_LOCATION) private location: any,
    authService: AuthService,
  ) {
    AuthenticationModule.loading = this.configService.config$
      .pipe(
        first((config) => !!config),
        tap(() => {
          let authenticatorService: AuthenticatorService;
          const config = this.configService.config;
          if (config.GATEWAY_ENABLED) {
            const oidcConfig = config.OIDC_CONFIG;
            oidcConfig.redirectUri = this.parseUri(oidcConfig.redirectUri);
            oidcConfig.postLogoutRedirectUri = this.parseUri(oidcConfig.postLogoutRedirectUri);
            this.oAuthService.configure(oidcConfig);
            authenticatorService = new OidcAuthenticatorService(this.oAuthService, this.location);
          } else {
            authenticatorService = new CasAuthenticatorService(
              this.location,
              config.CAS_URL,
              config.CAS_LOGOUT_URL,
              config.LOGOUT_REDIRECT_UI_URL,
            );
          }
          authService.configure(config.GATEWAY_ENABLED, authenticatorService);
        }),
      )
      .toPromise();
  }

  static forRoot(): ModuleWithProviders<AuthenticationModule> {
    return {
      ngModule: AuthenticationModule,
      providers: [{ provide: HTTP_INTERCEPTORS, useClass: AuthenticationInterceptor, multi: true }, OAuthModule.forRoot().providers],
    };
  }

  private parseUri(uri: string): string {
    if (this.isValidUrl(uri)) {
      return uri;
    }
    // to manage a bug in the OIDC lib. It is fixed in the new version. To remove after updating angular version + oidc lib.
    if (uri) {
      return this.location.href + uri;
    }
    return this.location.href;
  }

  private isValidUrl(url: string) {
    try {
      return Boolean(new URL(url));
    } catch (error) {
      return false;
    }
  }
}
