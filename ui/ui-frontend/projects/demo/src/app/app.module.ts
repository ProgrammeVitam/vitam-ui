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
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';

import { AccountComponent, AppGuard, BASE_URL, ENVIRONMENT, LoggerModule, VitamUICommonModule, WINDOW_LOCATION } from 'ui-frontend-common';
import { environment } from '../environments/environment';
import { AppComponent } from './app.component';
import { AppGuardDemoComponent } from './demo/app-guard-demo/app-guard-demo.component';
import { AppGuardDemoModule } from './demo/app-guard-demo/app-guard-demo.module';
import { GuardedPageComponent } from './demo/app-guard-demo/guarded-page.component';
import { ComponentDemoComponent } from './demo/component-demo/component-demo.component';
import { ComponentDemoModule } from './demo/component-demo/component-demo.module';
import { ComponentsComponent } from './demo/component-demo/components/components.component';
import { CssComponent } from './demo/component-demo/css/css.component';
import { IconDemoComponent } from './demo/icon-demo/icon-demo.component';
import { IconDemoModule } from './demo/icon-demo/icon-demo.module';
import { SubrogationDemoComponent } from './demo/subrogation-demo/subrogation-demo.component';
import { SubrogationDemoModule } from './demo/subrogation-demo/subrogation-demo.module';

@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    BrowserAnimationsModule,
    BrowserModule,
    MatSidenavModule,
    MatListModule,
    MatButtonModule,
    FormsModule,
    VitamUICommonModule,
    LoggerModule.forRoot(),
    SubrogationDemoModule,
    ComponentDemoModule,
    IconDemoModule,
    AppGuardDemoModule,
    LoggerModule.forRoot(),
    RouterModule.forRoot([
      { path: 'subrogation-demo', component: SubrogationDemoComponent },
      {
        path: 'components-demo',
        component: ComponentDemoComponent,
        children: [
          { path: 'angular', component: ComponentsComponent },
          { path: 'angular/tenant/:tenantIdentifier', component: ComponentsComponent },
          { path: 'angular/customer/:customerId', component: ComponentsComponent },
          { path: 'css', component: CssComponent },
          { path: '', redirectTo: 'angular', pathMatch: 'full' },
        ]
      },
      { path: 'icons-demo', component: IconDemoComponent },
      {
        path: 'app-guard-demo',
        component: AppGuardDemoComponent,
        children: [
          { path: 'account', component: GuardedPageComponent, canActivate: [AppGuard], data: { appId: 'ACCOUNTS_APP' } },
          { path: 'subrogation', component: GuardedPageComponent, canActivate: [AppGuard], data: { appId: 'SUBROGATIONS_APP' } },
          { path: 'customers', component: GuardedPageComponent, canActivate: [AppGuard], data: { appId: 'CUSTOMERS_APP' } },
          { path: 'fake-app', component: GuardedPageComponent, canActivate: [AppGuard], data: { appId: 'DOES_NOT_EXIST_APP' } },
        ]
      },
      { path: 'account', component: AccountComponent, canActivate: [AppGuard], data: { appId: 'ACCOUNTS_APP' } },
      { path: '', redirectTo: 'components-demo', pathMatch: 'full' },
      { path: '**', redirectTo: '' },
    ]),
  ],
  providers: [
    { provide: BASE_URL, useValue: '/identity-api' },
    { provide: ENVIRONMENT, useValue: environment },
    { provide: WINDOW_LOCATION, useValue: window.location },
    // { provide: ErrorHandler, useClass: GlobalErrorHandler },
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
