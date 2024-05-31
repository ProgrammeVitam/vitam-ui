import { APP_INITIALIZER, NgModule } from '@angular/core';
import { AuthService, AuthenticatorService } from 'ui-frontend-common';
import { NoAuthenticatorService } from './no-authenticator.service';

export function initializeApp() {
  return (): Promise<any> => Promise.resolve(true);
}

@NgModule({
  providers: [
    { provide: AuthenticatorService, useClass: NoAuthenticatorService },
    {
      provide: APP_INITIALIZER,
      useFactory: initializeApp,
      multi: true,
    },
  ],
})
export class NoAuthenticationModule {
  private gatewayEnabled = false;

  constructor(
    private authenticationService: AuthService,
    private authenticatorService: AuthenticatorService,
  ) {
    this.authenticationService.configure(this.gatewayEnabled, this.authenticatorService);
  }
}
