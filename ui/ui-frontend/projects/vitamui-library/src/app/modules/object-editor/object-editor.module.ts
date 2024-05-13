import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslateModule } from '@ngx-translate/core';
import { AccordionModule } from '../components/accordion/accordion.module';
import { DatepickerModule } from '../components/datepicker/datepicker.module';
import { VitamUICommonInputModule } from '../components/vitamui-input/vitamui-common-input.module';
import { VitamUIListInputModule } from '../components/vitamui-list-input';
import { VitamuiMenuButtonModule } from '../components/vitamui-menu-button/vitamui-menu-button.module';
import { VitamuiRepeatableInputModule } from '../components/vitamui-repeatable-input/vitamui-repeatable-input.module';
import { DisplayObjectService } from '../object-viewer/models';
import { PathStrategyDisplayObjectService } from '../object-viewer/services/path-strategy-display-object.service';
import { PipesModule } from '../pipes/pipes.module';
import { GroupEditorComponent } from './components/group-editor/group-editor.component';
import { ListEditorComponent } from './components/list-editor/list-editor.component';
import { PrimitiveEditorComponent } from './components/primitive-editor/primitive-editor.component';
import { ObjectEditorComponent } from './object-editor.component';
import { AppendStarPipe } from './required.pipe';

@NgModule({
  declarations: [ObjectEditorComponent, GroupEditorComponent, ListEditorComponent, PrimitiveEditorComponent, AppendStarPipe],
  providers: [{ provide: DisplayObjectService, useClass: PathStrategyDisplayObjectService }],
  imports: [
    CommonModule,
    TranslateModule,
    PipesModule,
    FormsModule,
    ReactiveFormsModule,
    VitamUICommonInputModule,
    VitamUIListInputModule,
    AccordionModule,
    VitamuiMenuButtonModule,
    DatepickerModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    VitamuiRepeatableInputModule,
  ],
  exports: [ObjectEditorComponent, GroupEditorComponent, ListEditorComponent, PrimitiveEditorComponent],
})
export class ObjectEditorModule {}
