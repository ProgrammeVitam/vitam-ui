import { APP_INITIALIZER, NgModule } from '@angular/core';
import { AuthenticatorService, AuthService } from 'vitamui-library';
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
  constructor(
    private authenticationService: AuthService,
    private authenticatorService: AuthenticatorService,
  ) {
    this.authenticationService.configure(this.authenticatorService);
  }
}
