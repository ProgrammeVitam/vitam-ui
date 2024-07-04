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

import { DisplayObjectService } from '../object-viewer/models';
import { PathStrategyDisplayObjectService } from '../object-viewer/services/path-strategy-display-object.service';
import { PipesModule } from '../pipes/pipes.module';
import { GroupEditorComponent } from './components/group-editor/group-editor.component';
import { ListEditorComponent } from './components/list-editor/list-editor.component';
import { PrimitiveEditorComponent } from './components/primitive-editor/primitive-editor.component';
import { ObjectEditorComponent } from './object-editor.component';
import { AppendStarPipe } from './required.pipe';

@NgModule({
  providers: [{ provide: DisplayObjectService, useClass: PathStrategyDisplayObjectService }],
  imports: [
    CommonModule,
    TranslateModule,
    PipesModule,
    FormsModule,
    ReactiveFormsModule,
    AccordionModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    ObjectEditorComponent,
    GroupEditorComponent,
    ListEditorComponent,
    PrimitiveEditorComponent,
    AppendStarPipe,
  ],
  exports: [ObjectEditorComponent, GroupEditorComponent, ListEditorComponent, PrimitiveEditorComponent],
})
export class ObjectEditorModule {}
