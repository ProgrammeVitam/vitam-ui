import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { AuthenticatorService } from 'vitamui-library';

@Injectable()
export class NoAuthenticatorService implements AuthenticatorService {
  login(): Observable<boolean> {
    const authenticated = true;

    return of(authenticated);
  }

  logout(): void {}

  logoutSubrogationAndRedirectToLoginPage(_username: string): void {}

  initSubrogationFlow(_superUser: string, _superUserCustomerId: string, _surrogate: string, _surrogateCustomerId: string): void {}

  redirectToLoginPage(): void {}
}
