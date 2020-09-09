import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AccountComponent, ActiveTenantGuard, AppGuard, AuthGuard} from 'ui-frontend-common';

import {AppComponent} from './app.component';


const routes: Routes = [
  {
    // we use PORTAL_APP as our appId so that the AppGuard won't find a profile with this appId
    // and we'll be redirected to the Portal Application
    path: '',
    component: AppComponent,
    canActivate: [AuthGuard, AppGuard],
    data: {appId: 'PORTAL_APP'}
  },
  {
    path: 'account',
    component: AccountComponent,
    canActivate: [AuthGuard, AppGuard],
    data: {appId: 'ACCOUNTS_APP'}
  },
  // =====================================================
  //                      ACCESS CONTRACT
  // =====================================================
  {
    path: 'access-contract',
    loadChildren: () => import('./access-contract/access-contract.module').then(m => m.AccessContractModule),
    canActivate: [AuthGuard, AppGuard],
    data: {appId: 'ACCESS_APP'}
  },
  // =====================================================
  //                      INGEST CONTRACT
  // =====================================================
  {
    path: 'ingest-contract',
    loadChildren: () => import('./ingest-contract/ingest-contract.module').then(m => m.IngestContractModule),
    canActivate: [AuthGuard, AppGuard],
    data: {appId: 'INGEST_APP'}
  },
  // =====================================================
  //                      AGENCY
  // =====================================================
  {
    path: 'agency',
    loadChildren: () => import('./agency/agency.module').then(m => m.AgencyModule),
    canActivate: [AuthGuard, AppGuard],
    data: {appId: 'AGENCIES_APP'}
  },
  // =====================================================
  //                    FILE FORMAT
  // =====================================================
  {
    path: 'file-format',
    loadChildren: () => import('./file-format/file-format.module').then(m => m.FileFormatModule),
    canActivate: [AuthGuard, AppGuard],
    data: {appId: 'FILE_FORMATS_APP'}
  },
  // =====================================================
  //                     CONTEXTS
  // =====================================================
  {
    path: 'context',
    loadChildren: () => import('./context/context.module').then(m => m.ContextModule),
    canActivate: [AuthGuard, AppGuard],
    data: {appId: 'CONTEXTS_APP'}
  },
  // =====================================================
  //                SECURITY PROFILES
  // =====================================================
  {
    path: 'security-profile',
    loadChildren: () => import('./security-profile/security-profile.module').then(m => m.SecurityProfileModule),
    canActivate: [AuthGuard, AppGuard],
    data: {appId: 'SECURITY_PROFILES_APP'}
  },
  // =====================================================
  //                    ONTOLOGY
  // =====================================================
  {
    path: 'ontology',
    loadChildren: () => import('./ontology/ontology.module').then(m => m.OntologyModule),
    canActivate: [AuthGuard, AppGuard],
    data: {appId: 'ONTOLOGY_APP'}
  },
  // =====================================================
  //                    AUDITS
  // =====================================================
  {
    path: 'audit',
    loadChildren: () => import('./audit/audit.module').then(m => m.AuditModule),
    canActivate: [AuthGuard, AppGuard],
    data: {appId: 'AUDIT_APP'}
  },
  // =====================================================
  //                    SECURISATION
  // =====================================================
  {
    path: 'securisation',
    loadChildren: () => import('./securisation/securisation.module').then(m => m.SecurisationModule),
    canActivate: [AuthGuard, AppGuard],
    data: {appId: 'SECURE_APP'}
  },
  // =====================================================
  //                   PROBATIVE VALUE
  // =====================================================
  {
    path: 'probative-value',
    loadChildren: () => import('./probative-value/probative-value.module').then(m => m.ProbativeValueModule),
    canActivate: [AuthGuard, AppGuard],
    data: {appId: 'PROBATIVE_VALUE_APP'}
  },
  // =====================================================
  //                      LOGBOOK OPERATION API
  // =====================================================
  {
    path: 'logbook-operation',
    loadChildren: () => import('./logbook-operation/logbook-operation.module').then(m => m.LogbookOperationModule),
    canActivate: [AuthGuard, AppGuard],
    data: { appId: 'LOGBOOK_OPERATION_APP' }
  },
  // =====================================================
  //                       DSL
  // =====================================================
  {
    path: 'dsl',
    loadChildren: () => import('./admin-dsl/admin-dsl.module').then(m => m.AdminDslModule),
    canActivate: [AuthGuard, AppGuard],
    data: {appId: 'DSL_APP'}
  },
  // =====================================================
  //                       RULES
  // =====================================================
  {
    path: 'rule',
    loadChildren: () => import('./rule/rule.module').then(m => m.RuleModule),
    canActivate: [AuthGuard, AppGuard],
    data: {appId: 'RULES_APP'}
  },
  // =====================================================
  //                      unknown path
  // =====================================================
  {path: '**', redirectTo: ''}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
  providers: [
    ActiveTenantGuard,
    AuthGuard
  ]
})
export class AppRoutingModule {
}
