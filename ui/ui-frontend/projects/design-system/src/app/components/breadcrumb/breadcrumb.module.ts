import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { VitamUICommonModule } from 'vitamui-library';
import { BreadcrumbComponent } from './breadcrumb.component';

@NgModule({
  declarations: [BreadcrumbComponent],
  imports: [CommonModule, VitamUICommonModule],
  exports: [BreadcrumbComponent],
})
export class BreadcrumbModule {}
