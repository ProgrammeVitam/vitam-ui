import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FileNode } from '../../models/file-node';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'modify-text-button',
  templateUrl: './modify-text-button.component.html',
  styleUrls: ['./modify-text-button.component.css'],
})
export class ModifyTextButtonComponent {
  @Input()
  node: FileNode;

  @Output()
  textEdit: EventEmitter<string> = new EventEmitter();

  editmode = false;
  editText = '';
  constructor() {}

  edit() {
    this.editmode = true;
    this.editText = this.node.editName ? this.node.editName : this.node.name;
  }

  save() {
    this.editmode = false;
    this.node.editName = this.editText;
    this.textEdit.emit(this.editText);
  }

  cancel() {
    this.editmode = false;
    this.editText = '';
  }
}
