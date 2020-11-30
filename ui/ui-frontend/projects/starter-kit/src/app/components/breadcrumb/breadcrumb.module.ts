import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { VitamUICommonModule } from 'ui-frontend-common';
import { BreadcrumbComponent } from './breadcrumb.component';



@NgModule({
  declarations: [BreadcrumbComponent],
  imports: [
    CommonModule,
    VitamUICommonModule
  ],
  exports: [BreadcrumbComponent]
})
export class BreadcrumbModule { }
