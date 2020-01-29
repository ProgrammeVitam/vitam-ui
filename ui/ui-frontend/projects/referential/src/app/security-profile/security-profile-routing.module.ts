import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Route, RouterModule } from '@angular/router';
import { AuthGuard, AppGuard } from 'ui-frontend-common';
import { SecurityProfileComponent } from "./security-profile.component";

const routes: Route[] = [
  {
    path: '',
    component: SecurityProfileComponent,
    canActivate: [AuthGuard, AppGuard],
    data: { appId: 'SECURITY_PROFILES_APP' }
  }
];


@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
  ]
})
export class SecurityProfileRoutingModule { }
