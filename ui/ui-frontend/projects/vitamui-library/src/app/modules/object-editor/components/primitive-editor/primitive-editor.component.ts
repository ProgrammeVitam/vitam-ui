import { Component, Input, OnInit } from '@angular/core';
import { Logger } from '../../../logger/logger';
import { DateDisplayService } from '../../../object-viewer/services/date-display.service';
import { ComponentType } from '../../../object-viewer/types';
import { EditObject } from '../../models/edit-object.model';
import { AppendStarPipe } from '../../required.pipe';
import { EmptyPipe } from '../../../pipes/empty.pipe';
import { TranslateModule } from '@ngx-translate/core';
import { DatepickerComponent } from '../../../components/datepicker/datepicker.component';
import { VitamUITextareaComponent } from '../../../components/vitamui-input/vitamui-textarea.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { VitamUICommonInputComponent } from '../../../components/vitamui-input/vitamui-common-input.component';
import { NgIf, NgSwitch, NgSwitchCase } from '@angular/common';

@Component({
  selector: 'vitamui-common-primitive-editor',
  templateUrl: './primitive-editor.component.html',
  styleUrls: ['./primitive-editor.component.scss'],
  standalone: true,
  imports: [
    NgIf,
    NgSwitch,
    NgSwitchCase,
    VitamUICommonInputComponent,
    FormsModule,
    ReactiveFormsModule,
    VitamUITextareaComponent,
    DatepickerComponent,
    TranslateModule,
    EmptyPipe,
    AppendStarPipe,
  ],
})
export class PrimitiveEditorComponent implements OnInit {
  @Input() editObject: EditObject;

  uiComponent: ComponentType;
  dateFormat: string;

  constructor(
    private logger: Logger,
    private dateDisplayService: DateDisplayService,
  ) {}

  ngOnInit(): void {
    if (!this.editObject) return;
    if (!this.editObject.control) this.logger.warn(this, this.editObject.path, 'No control assigned !');

    this.uiComponent = this.editObject.displayRule?.ui?.component;
    this.dateFormat = this.dateDisplayService.getFormat(this.uiComponent);
  }
}
