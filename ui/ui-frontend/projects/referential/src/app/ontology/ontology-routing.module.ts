import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {Route, RouterModule} from '@angular/router';
import {AppGuard, AuthGuard} from 'ui-frontend-common';
import {OntologyComponent} from './ontology.component';

const routes: Route[] = [
  {
    path: '',
    component: OntologyComponent,
    canActivate: [AuthGuard, AppGuard],
    data: {appId: 'ONTOLOGY_APP'}
  }
];


@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
  ]
})
export class OntologyRoutingModule {
}
