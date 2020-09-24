import {NgModule} from '@angular/core';

import {UpdatedApplicationSelectContentModule} from './components/application-select-content/application-select-content.module';
import {CardGroupModule} from './components/card-group/card-group.module';
import {CardSelectModule} from './components/card-select/card-select.module';
import {CardModule} from './components/card/card.module';
import {ConfirmActionModule} from './components/confirm-action/confirm-action.module';
import {FilingPlanModule} from './components/filing-plan/filing-plan.module';
import {VitamUIInputModule} from './components/vitamui-input/vitamui-input.module';
import {UpdatedVitamUIMenuTileModule} from './components/vitamui-menu-tile/vitamui-menu-tile.module';
import {VitamUIRadioGroupModule} from './components/vitamui-radio-group/vitamui-radio-group.module';
import {VitamUIRadioModule} from './components/vitamui-radio/vitamui-radio.module';
import {VitamUISelectAllOptionModule} from './components/vitamui-select-all-option/vitamui-select-all-option.module';

@NgModule({
  declarations: [],
  imports: [
    CardGroupModule,
    CardModule,
    CardSelectModule,
    ConfirmActionModule,
    FilingPlanModule,
    UpdatedApplicationSelectContentModule,
    UpdatedVitamUIMenuTileModule,
    VitamUIInputModule,
    VitamUIRadioGroupModule,
    VitamUIRadioModule,
    VitamUISelectAllOptionModule
  ],
  exports: [
    CardGroupModule,
    CardModule,
    CardSelectModule,
    ConfirmActionModule,
    FilingPlanModule,
    UpdatedApplicationSelectContentModule,
    UpdatedVitamUIMenuTileModule,
    VitamUIInputModule,
    VitamUIRadioGroupModule,
    VitamUIRadioModule,
    VitamUISelectAllOptionModule
  ]
})
export class VitamUILibraryModule {
}
