import { NgModule } from '@angular/core';

import {
  UpdatedApplicationSelectContentModule,
} from './components/application-select-content/application-select-content.module';
import { CardGroupModule } from './components/card-group/card-group.module';
import { CardSelectModule } from './components/card-select/card-select.module';
import { CardModule } from './components/card/card.module';
import { VitamUIInputModule } from './components/vitamui-input/vitamui-input.module';
import { UpdatedVitamUIMenuTileModule } from './components/vitamui-menu-tile/vitamui-menu-tile.module';
import { VitamUIRadioGroupModule } from './components/vitamui-radio-group/vitamui-radio-group.module';
import { VitamUIRadioModule } from './components/vitamui-radio/vitamui-radio.module';
import { FilingPlanModule } from "./components/filing-plan/filing-plan.module";
import { ConfirmActionModule } from "./components/confirm-action/confirm-action.module";

@NgModule({
  declarations: [],
  imports: [
    CardModule,
    CardGroupModule,
    CardSelectModule,
    ConfirmActionModule,
    UpdatedApplicationSelectContentModule,
    UpdatedVitamUIMenuTileModule,
    VitamUIInputModule,
    VitamUIRadioModule,
    VitamUIRadioGroupModule,
    FilingPlanModule
  ],
  exports: [
    CardModule,
    CardGroupModule,
    CardSelectModule,
    ConfirmActionModule,
    UpdatedApplicationSelectContentModule,
    UpdatedVitamUIMenuTileModule,
    VitamUIInputModule,
    VitamUIRadioModule,
    VitamUIRadioGroupModule,
    FilingPlanModule
  ]
})
export class VitamUILibraryModule { }
