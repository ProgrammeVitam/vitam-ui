import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {Route, RouterModule} from '@angular/router';
import {AppGuard, AuthGuard} from 'ui-frontend-common';
import {SecurityProfileComponent} from './security-profile.component';

const routes: Route[] = [
  {
    path: '',
    component: SecurityProfileComponent,
    canActivate: [AuthGuard, AppGuard],
    data: {appId: 'SECURITY_PROFILES_APP'}
  }
];


@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
  ]
})
export class SecurityProfileRoutingModule {
}
