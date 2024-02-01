/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { QuicklinkStrategy } from 'ngx-quicklink';
import { AccountComponent, ActiveTenantGuard, AnalyticsResolver, AppGuard, AuthGuard } from 'ui-frontend-common';
import { AppComponent } from './app.component';

const routes: Routes = [
  {
    // we use PORTAL_APP as our appId so that the AppGuard won't find a profile with this appId
    // and we'll be redirected to the Portal Application
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
  // =====================================================
  //                      ACCESS CONTRACT
  // =====================================================
  {
    path: 'access-contract',
    loadChildren: () => import('./access-contract/access-contract.module').then((m) => m.AccessContractModule),
    canActivate: [AuthGuard, AppGuard],
    resolve: { userAnalytics: AnalyticsResolver },
    data: { appId: 'ACCESS_APP' },
  },
  // =====================================================
  //                      INGEST CONTRACT
  // =====================================================
  {
    path: 'ingest-contract',
    loadChildren: () => import('./ingest-contract/ingest-contract.module').then((m) => m.IngestContractModule),
    canActivate: [AuthGuard, AppGuard],
    resolve: { userAnalytics: AnalyticsResolver },
    data: { appId: 'INGEST_APP' },
  },
  // =====================================================
  //                      AGENCY
  // =====================================================
  {
    path: 'agency',
    loadChildren: () => import('./agency/agency.module').then((m) => m.AgencyModule),
    canActivate: [AuthGuard, AppGuard],
    resolve: { userAnalytics: AnalyticsResolver },
    data: { appId: 'AGENCIES_APP' },
  },
  // =====================================================
  //                    FILE FORMAT
  // =====================================================
  {
    path: 'file-format',
    loadChildren: () => import('./file-format/file-format.module').then((m) => m.FileFormatModule),
    canActivate: [AuthGuard, AppGuard],
    resolve: { userAnalytics: AnalyticsResolver },
    data: { appId: 'FILE_FORMATS_APP' },
  },
  // =====================================================
  //                     CONTEXTS
  // =====================================================
  {
    path: 'context',
    loadChildren: () => import('./context/context.module').then((m) => m.ContextModule),
    canActivate: [AuthGuard, AppGuard],
    resolve: { userAnalytics: AnalyticsResolver },
    data: { appId: 'CONTEXTS_APP' },
  },
  // =====================================================
  //                SECURITY PROFILES
  // =====================================================
  {
    path: 'security-profile',
    loadChildren: () => import('./security-profile/security-profile.module').then((m) => m.SecurityProfileModule),
    canActivate: [AuthGuard, AppGuard],
    resolve: { userAnalytics: AnalyticsResolver },
    data: { appId: 'SECURITY_PROFILES_APP' },
  },
  // =====================================================
  //                    ONTOLOGY
  // =====================================================
  {
    path: 'ontology',
    loadChildren: () => import('./ontology/ontology.module').then((m) => m.OntologyModule),
    canActivate: [AuthGuard, AppGuard],
    resolve: { userAnalytics: AnalyticsResolver },
    data: { appId: 'ONTOLOGY_APP' },
  },
  // =====================================================
  //                    AUDITS
  // =====================================================
  {
    path: 'audit',
    loadChildren: () => import('./audit/audit.module').then((m) => m.AuditModule),
    canActivate: [AuthGuard, AppGuard],
    resolve: { userAnalytics: AnalyticsResolver },
    data: { appId: 'AUDIT_APP' },
  },
  // =====================================================
  //                    SECURISATION
  // =====================================================
  {
    path: 'securisation',
    loadChildren: () => import('./securisation/securisation.module').then((m) => m.SecurisationModule),
    canActivate: [AuthGuard, AppGuard],
    resolve: { userAnalytics: AnalyticsResolver },
    data: { appId: 'SECURE_APP' },
  },
  // =====================================================
  //                   PROBATIVE VALUE
  // =====================================================
  {
    path: 'probative-value',
    loadChildren: () => import('./probative-value/probative-value.module').then((m) => m.ProbativeValueModule),
    canActivate: [AuthGuard, AppGuard],
    resolve: { userAnalytics: AnalyticsResolver },
    data: { appId: 'PROBATIVE_VALUE_APP' },
  },
  // =====================================================
  //                      LOGBOOK OPERATION API
  // =====================================================
  {
    path: 'logbook-operation',
    loadChildren: () => import('./logbook-operation/logbook-operation.module').then((m) => m.LogbookOperationModule),
    canActivate: [AuthGuard, AppGuard],
    resolve: { userAnalytics: AnalyticsResolver },
    data: { appId: 'LOGBOOK_OPERATION_APP' },
  },
  // =====================================================
  //                       DSL
  // =====================================================
  {
    path: 'dsl',
    loadChildren: () => import('./admin-dsl/admin-dsl.module').then((m) => m.AdminDslModule),
    canActivate: [AuthGuard, AppGuard],
    resolve: { userAnalytics: AnalyticsResolver },
    data: { appId: 'DSL_APP' },
  },
  // =====================================================
  //                       RULES
  // =====================================================
  {
    path: 'rule',
    loadChildren: () => import('./rule/rule.module').then((m) => m.RuleModule),
    canActivate: [AuthGuard, AppGuard],
    resolve: { userAnalytics: AnalyticsResolver },
    data: { appId: 'RULES_APP' },
  },
  // =====================================================
  //              LOGBOOK MANAGEMENT OPERATION
  // =====================================================
  {
    path: 'logbook-management-operation',
    loadChildren: () =>
      import('./logbook-management-operation/logbook-management-operation.module').then(
        (module) => module.LogbookManagementOperationModule,
      ),
    canActivate: [AuthGuard, AppGuard],
    resolve: { userAnalytics: AnalyticsResolver },
    data: { appId: 'LOGBOOK_MANAGEMENT_OPERATION_APP' },
  },
  // =====================================================
  //                      ACCESSION REGISTER
  // =====================================================
  {
    path: 'accession-register',
    loadChildren: () => import('./accession-register/accession-register.module').then((m) => m.AccessionRegisterModule),
    canActivate: [AuthGuard, AppGuard],
    resolve: { userAnalytics: AnalyticsResolver },
    data: { appId: 'ACCESSION_REGISTER_APP' },
  },
  // =====================================================
  //                    MANAGEMENT_CONTRACT_APP
  // =====================================================
  {
    path: 'management-contract',
    loadChildren: () => import('./management-contract/management-contract.module').then((m) => m.ManagementContractModule),
    canActivate: [AuthGuard, AppGuard],
    resolve: { userAnalytics: AnalyticsResolver },
    data: { appId: 'MANAGEMENT_CONTRACT_APP' },
  },
  // =====================================================
  //                      unknown path
  // =====================================================
  { path: '**', redirectTo: '' },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, {
      preloadingStrategy: QuicklinkStrategy,
    }),
  ],
  exports: [RouterModule],
  providers: [ActiveTenantGuard, AuthGuard],
})
export class AppRoutingModule {}
