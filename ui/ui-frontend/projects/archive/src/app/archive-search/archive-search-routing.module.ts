import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { Route, RouterModule } from '@angular/router';
import { ArchiveSearchComponent } from './archive-search.component';


const routes: Route[] = [
  {
    path: '',
    redirectTo: 'search',
    pathMatch: 'full'
  }, {
    path: 'search',
    component: ArchiveSearchComponent,
    canActivate: []
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
export class ArchiveSearchRoutingModule { }
