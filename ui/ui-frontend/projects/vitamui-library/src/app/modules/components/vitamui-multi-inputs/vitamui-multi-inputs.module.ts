import { OverlayModule } from '@angular/cdk/overlay';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatLegacyProgressSpinnerModule as MatProgressSpinnerModule } from '@angular/material/legacy-progress-spinner';
import { PipesModule } from '../../pipes/pipes.module';
import { EditableFieldModule } from '../editable-field/editable-field.module';
import { VitamuiMultiInputsComponent } from './vitamui-multi-inputs.component';

@NgModule({
  declarations: [VitamuiMultiInputsComponent],
  imports: [CommonModule, EditableFieldModule, MatProgressSpinnerModule, ReactiveFormsModule, OverlayModule, PipesModule],
  exports: [VitamuiMultiInputsComponent],
})
export class VitamuiMultiInputsModule {}
