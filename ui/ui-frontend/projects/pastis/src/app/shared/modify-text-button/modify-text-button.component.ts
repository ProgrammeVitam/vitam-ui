import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import { FileNode } from '../../models/file-node';

@Component({
  selector: 'modify-text-button',
  templateUrl: './modify-text-button.component.html',
  styleUrls: ['./modify-text-button.component.css']
})
export class ModifyTextButtonComponent implements OnInit {

  @Input()
  node: FileNode;

  @Output()
  textEdit: EventEmitter<string> = new EventEmitter();


  editmode = false;
  editText = '';
  constructor() {}

  ngOnInit(): void {}

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