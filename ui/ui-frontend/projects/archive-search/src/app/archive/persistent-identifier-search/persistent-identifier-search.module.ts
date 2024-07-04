import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { VitamUICommonModule } from 'vitamui-library';
import { FoundObjectModalComponent } from './found-object-modal/found-object-modal.component';
import { PersistentIdentifierSearchRoutingModule } from './persistent-identifier-search-routing.module';
import { PersistentIdentifierSearchComponent } from './persistent-identifier-search.component';
import { PurgedPersistentIdentifierModalComponent } from './purged-persistent-identifier-modal/purged-persistent-identifier-modal.component';

@NgModule({
  imports: [
    CommonModule,
    PersistentIdentifierSearchRoutingModule,
    VitamUICommonModule,
    PersistentIdentifierSearchComponent,
    PurgedPersistentIdentifierModalComponent,
    FoundObjectModalComponent,
  ],
})
export class PersistentIdentifierSearchModule {}
