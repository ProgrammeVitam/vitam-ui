import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Route, RouterModule } from '@angular/router';
import { VitamUITenantSelectComponent, TenantSelectionGuard, ActiveTenantGuard } from 'ui-frontend-common';
import { TreesPlansComponent } from './trees-plans.component';



const routes: Route[] = [
  {
    path: '',
    redirectTo: 'tenant',
    pathMatch: 'full'
  },
  {
    path: 'tenant',
    component: VitamUITenantSelectComponent,
    pathMatch: 'full',
    canActivate: [TenantSelectionGuard]
  },
  {
    path: 'tenant/:tenantIdentifier',
    component: TreesPlansComponent,
    canActivate: [ActiveTenantGuard]
  }
];


@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ],
  exports: [
    RouterModule
  ]
})
export class TreesPlansRoutingModule { }
