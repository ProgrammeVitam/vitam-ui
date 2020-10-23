import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ActiveTenantGuard, AppGuard, AuthGuard, AccountComponent } from 'ui-frontend-common';
import { AppComponent } from './app.component';
import { ArchiveComponent } from './archive/archive.component';


const routes: Routes = [

  {
    path: '',
    component: AppComponent,
    canActivate: [AuthGuard, AppGuard],
    data: { appId: 'PORTAL_APP' }
  },
  {
    path: 'account',
    component: AccountComponent,
    canActivate: [AuthGuard, AppGuard],
    data: { appId: 'ACCOUNTS_APP' }
  },
  {
    path: 'archive',
    // loadChildren: () => import('./archive/archive.module').then(m => m.ArchiveModule),
    // canActivate: [AuthGuard, AppGuard],
    component: ArchiveComponent,
    data: { appId: 'ARCHIVE_MANAGEMENT_APP' }
  },

  { path: '**', redirectTo: '' }


];

@NgModule({
  imports: [
    RouterModule.forRoot(routes)],
  exports: [RouterModule],
  providers: [
    ActiveTenantGuard,
    AuthGuard
  ]
})
export class AppRoutingModule { }
