import { Component, Input } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { EditorHintComponent } from './editor-hint.component';
import { FormErrorDisplayComponent } from '../../components/form-error-display/form-error-display.component';
import { TranslateModule } from '@ngx-translate/core';
import { PipesModule } from '../../pipes/pipes.module';
import { AppendStarPipe } from '../required.pipe';
import { MatLegacyFormFieldModule } from '@angular/material/legacy-form-field';
import { MatLegacySelectModule } from '@angular/material/legacy-select';

@Component({
  selector: 'vitamui-editor-select',
  template: `
    <mat-form-field class="mb-4 w-100 vitamui-mat-select">
      <mat-label>{{ label | translate | empty }}</mat-label>
      <mat-select [formControl]="control" [multiple]="multiple" panelClass="vitamui-mat-select">
        @for (option of options; track option) {
          <mat-option [value]="option">
            {{ option }}
          </mat-option>
        }
      </mat-select>
      <div class="select-arrow">
        <i class="material-icons">keyboard_arrow_up</i>
        <i class="material-icons">keyboard_arrow_down</i>
      </div>
      <vitamui-editor-hint [control]="control" [hint]="hint"></vitamui-editor-hint>
      <vitamui-form-error-display [control]="control"></vitamui-form-error-display>
    </mat-form-field>
  `,
  standalone: true,
  imports: [
    ReactiveFormsModule,
    EditorHintComponent,
    FormErrorDisplayComponent,
    TranslateModule,
    PipesModule,
    AppendStarPipe,
    MatLegacyFormFieldModule,
    MatLegacySelectModule,
  ],
})
export class EditorSelectComponent {
  @Input({ required: true }) control!: FormControl;
  @Input() options: string[] = [];
  @Input() multiple: boolean = false;
  @Input() required: boolean = false;
  @Input() label?: string;
  @Input() hint?: string;
}
