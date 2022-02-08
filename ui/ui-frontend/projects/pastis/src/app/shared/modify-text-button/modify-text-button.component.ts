import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'modify-text-button',
  templateUrl: './modify-text-button.component.html',
  styleUrls: ['./modify-text-button.component.css']
})
export class ModifyTextButtonComponent implements OnInit {

  @Input()
  text: string;

  @Output()
  textEdit: EventEmitter<string> = new EventEmitter();


  editmode = false;
  editText = '';
  constructor() { }

  ngOnInit(): void {
  }
  edit() {
    this.editmode = true;
    this.editText = this.text;
  }

  save() {
    this.editmode = false;
    this.text = this.editText;
    this.textEdit.emit(this.editText);

  }

  cancel() {
    this.editmode = false;
    this.editText = '';
  }
}
