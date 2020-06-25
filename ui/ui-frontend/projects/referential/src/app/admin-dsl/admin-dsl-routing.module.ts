import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {Route, RouterModule} from '@angular/router';
import {ActiveTenantGuard, TenantSelectionGuard, VitamUITenantSelectComponent} from 'ui-frontend-common';

import {AdminDslComponent} from './admin-dsl.component';

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
    component: AdminDslComponent,
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
export class AdminDslRoutingModule {
}
