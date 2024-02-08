import { Component, Input } from '@angular/core';
import { DisplayObjectType } from '../object-viewer/types';
import { EditObject } from './models/edit-object.model';

@Component({
  selector: 'vitamui-common-object-editor',
  templateUrl: './object-editor.component.html',
  styles: [],
})
export class ObjectEditorComponent {
  @Input() editObject!: EditObject;

  readonly DisplayObjectType = DisplayObjectType;
}
