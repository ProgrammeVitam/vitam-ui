import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { VitamUICommonModule } from 'ui-frontend-common';
import { ArkSearchRoutingModule } from './ark-search-routing.module';
import { ArkSearchComponent } from './ark-search.component';
import { ErrorResponseModalComponent } from './error-response-modal/error-response-modal.component';


@NgModule({
  imports: [
    CommonModule,
    ArkSearchRoutingModule,
    VitamUICommonModule,
  ],
  declarations: [
    ArkSearchComponent,
    ErrorResponseModalComponent
  ],
})
export class ArkSearchModule {}
