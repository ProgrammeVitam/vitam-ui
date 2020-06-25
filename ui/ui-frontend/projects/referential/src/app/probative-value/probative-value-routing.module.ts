import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {Route, RouterModule} from '@angular/router';
import {ActiveTenantGuard, TenantSelectionGuard, VitamUITenantSelectComponent} from 'ui-frontend-common';

import {ProbativeValueComponent} from './probative-value.component';

const routes: Route[] = [
  {
    path: '',
    redirectTo: 'tenant',
    pathMatch: 'full'
  }, {
    path: 'tenant',
    component: VitamUITenantSelectComponent,
    canActivate: [TenantSelectionGuard]
  },
  {
    path: 'tenant/:tenantIdentifier',
    component: ProbativeValueComponent,
    canActivate: [ActiveTenantGuard]
  }
];

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
  ]
})
export class ProbativeValueRoutingModule {
}
