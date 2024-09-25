import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatLegacyDialogModule as MatDialogModule } from '@angular/material/legacy-dialog';
import { MatLegacyFormFieldModule as MatFormFieldModule } from '@angular/material/legacy-form-field';
import { MatLegacyInputModule as MatInputModule } from '@angular/material/legacy-input';
import { MatLegacyProgressSpinnerModule as MatProgressSpinnerModule } from '@angular/material/legacy-progress-spinner';
import { MatLegacyTooltipModule as MatTooltipModule } from '@angular/material/legacy-tooltip';
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
import { MatLegacyOptionModule } from '@angular/material/legacy-core';
import { MatLegacySelectModule } from '@angular/material/legacy-select';
import { EditorListInputComponent } from './components/editor-list-input.component';
import { EditorListTextareaComponent } from './components/editor-list-textarea.component';
import { FormErrorDisplayComponent } from '../components/form-error-display/form-error-display.component';
import { EditorListSelectComponent } from './components/editor-list-select.component';
import { EditorHintComponent } from './components/editor-hint.component';
import { EditorInputComponent } from './components/editor-input.component';
import { EditorTextareaComponent } from './components/editor-textarea.component';
import { EditorSelectComponent } from './components/editor-select.component';
import { MultipleOptionsDatepickerModule } from '../components/multiple-options-datepicker/multiple-options-datepicker.module';
import { EditorListDateComponent } from './components/editor-list-date.component';

@NgModule({
  declarations: [ObjectEditorComponent, GroupEditorComponent, ListEditorComponent, PrimitiveEditorComponent],
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
    MatLegacyOptionModule,
    MatLegacySelectModule,
    EditorListInputComponent,
    EditorListTextareaComponent,
    FormErrorDisplayComponent,
    AppendStarPipe,
    EditorListSelectComponent,
    EditorHintComponent,
    EditorInputComponent,
    EditorTextareaComponent,
    EditorSelectComponent,
    EditorListDateComponent,
    MultipleOptionsDatepickerModule,
  ],
  exports: [ObjectEditorComponent, GroupEditorComponent, ListEditorComponent, PrimitiveEditorComponent],
})
export class ObjectEditorModule {}
