import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VitamuiMultiInputsComponent } from './vitamui-multi-inputs.component';
import { EditableFieldModule } from '../editable-field/editable-field.module';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ReactiveFormsModule } from '@angular/forms';
import { OverlayModule } from '@angular/cdk/overlay';
import { PipesModule } from '../../pipes/pipes.module';

@NgModule({
  declarations: [VitamuiMultiInputsComponent],
  imports: [CommonModule, EditableFieldModule, MatProgressSpinnerModule, ReactiveFormsModule, OverlayModule, PipesModule],
  exports: [VitamuiMultiInputsComponent],
})
export class VitamuiMultiInputsModule {}
