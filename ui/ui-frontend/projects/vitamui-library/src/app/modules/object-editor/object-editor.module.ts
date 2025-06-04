/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
import { AccordionModule } from '../components/accordion/accordion.module';
import { DatepickerModule } from '../components/datepicker/datepicker.module';
import { VitamUICommonInputModule } from '../components/vitamui-input/vitamui-common-input.module';
import { VitamUIListInputModule } from '../components/vitamui-list-input';
import { VitamuiMenuButtonModule } from '../components/vitamui-menu-button/vitamui-menu-button.module';
import { DisplayObjectService } from '../object-viewer/models';
import { PathStrategyDisplayObjectService } from '../object-viewer/services/path-strategy-display-object.service';
import { PipesModule } from '../pipes/pipes.module';
import { GroupEditorComponent } from './components/group-editor/group-editor.component';
import { ListEditorComponent } from './components/list-editor/list-editor.component';
import { PrimitiveEditorComponent } from './components/primitive-editor/primitive-editor.component';
import { ObjectEditorComponent } from './object-editor.component';
import { AppendStarPipe } from './required.pipe';
import { MatOptionModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { EditorListInputComponent } from './components/editor-list-input.component';
import { EditorListTextareaComponent } from './components/editor-list-textarea.component';
import { FormErrorDisplayComponent } from '../components/form-error-display/form-error-display.component';
import { EditorListSelectComponent } from './components/editor-list-select.component';
import { HintComponent } from '../components/hint/hint.component';
import { EditorInputComponent } from './components/editor-input.component';
import { EditorTextareaComponent } from './components/editor-textarea.component';
import { EditorSelectComponent } from './components/editor-select.component';
import { MultipleOptionsDatepickerModule } from '../components/multiple-options-datepicker/multiple-options-datepicker.module';
import { EditorListDateComponent } from './components/editor-list-date.component';
import { EditObjectService } from './services/edit-object.service';
import { PathService } from './services/path.service';
import { SchemaService } from './services/schema.service';
import { TemplateService } from './services/template.service';
import { DialogHeaderComponent } from '../../../lib/components/dialog/dialog-header/dialog-header.component';
import { VitamuiRepeatableInputComponent } from '../components/vitamui-repeatable-input/vitamui-repeatable-input.component';

@NgModule({
  declarations: [ObjectEditorComponent, GroupEditorComponent, ListEditorComponent, PrimitiveEditorComponent],
  providers: [
    { provide: DisplayObjectService, useClass: PathStrategyDisplayObjectService },
    EditObjectService,
    PathService,
    SchemaService,
    TemplateService,
  ],
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
    MatProgressSpinnerModule,
    MatDialogModule,
    VitamuiRepeatableInputComponent,
    MatOptionModule,
    MatSelectModule,
    EditorListInputComponent,
    EditorListTextareaComponent,
    FormErrorDisplayComponent,
    AppendStarPipe,
    EditorListSelectComponent,
    HintComponent,
    EditorInputComponent,
    EditorTextareaComponent,
    EditorSelectComponent,
    EditorListDateComponent,
    MultipleOptionsDatepickerModule,
    DialogHeaderComponent,
  ],
  exports: [ObjectEditorComponent, GroupEditorComponent, ListEditorComponent, PrimitiveEditorComponent],
})
export class ObjectEditorModule {}
