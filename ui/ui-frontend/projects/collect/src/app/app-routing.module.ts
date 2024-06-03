import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { QuicklinkStrategy } from 'ngx-quicklink';
import { AccountComponent, ActiveTenantGuard, AnalyticsResolver, AppGuard, AuthGuard } from 'vitamui-library';
import { AppComponent } from './app.component';

const routes: Routes = [
  {
    path: '',
    component: AppComponent,
    canActivate: [AuthGuard, AppGuard],
    resolve: { userAnalytics: AnalyticsResolver },
    data: { appId: 'PORTAL_APP' },
  },
  {
    path: 'account',
    component: AccountComponent,
    canActivate: [AuthGuard, AppGuard],
    resolve: { userAnalytics: AnalyticsResolver },
    data: { appId: 'ACCOUNTS_APP' },
  },
  {
    path: 'collect',
    loadChildren: () => import('./collect/collect.module').then((m) => m.CollectModule),
    canActivate: [AuthGuard, AppGuard],
    resolve: { userAnalytics: AnalyticsResolver },
    data: { appId: 'COLLECT_APP' },
  },
  { path: '**', redirectTo: '' },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, {
      preloadingStrategy: QuicklinkStrategy,
      relativeLinkResolution: 'legacy',
    }),
  ],
  exports: [RouterModule],
  providers: [ActiveTenantGuard, AuthGuard],
})
export class AppRoutingModule {}
