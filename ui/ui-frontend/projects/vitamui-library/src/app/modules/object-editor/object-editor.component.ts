import { Component, Input } from '@angular/core';
import { DisplayObjectType } from '../object-viewer/types';
import { EditObject } from './models/edit-object.model';
import { PrimitiveEditorComponent } from './components/primitive-editor/primitive-editor.component';
import { ListEditorComponent } from './components/list-editor/list-editor.component';
import { GroupEditorComponent } from './components/group-editor/group-editor.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';

@Component({
  selector: 'vitamui-common-object-editor',
  templateUrl: './object-editor.component.html',
  styles: [],
  standalone: true,
  imports: [NgIf, FormsModule, ReactiveFormsModule, GroupEditorComponent, ListEditorComponent, PrimitiveEditorComponent],
})
export class ObjectEditorComponent {
  @Input() editObject!: EditObject;

  readonly DisplayObjectType = DisplayObjectType;
}
