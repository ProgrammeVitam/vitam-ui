import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'allow-additional-properties',
  templateUrl: './allow-additional-properties.component.html',
  styleUrls: ['./allow-additional-properties.component.scss']
})
export class AllowAdditionalPropertiesComponent implements OnInit {

  @Output() stateToggleButton = new EventEmitter<boolean>();

  @Input()
  checked: boolean;
  text : string;
  text1: string;

  constructor() { }

  ngOnInit(): void {
    this.text1='Métadonnées supplémentaires'
    this.text=' non autorisées'

  }
  // tslint:disable-next-line:member-ordering


  changed(){
    if(this.checked){
      this.text1='Métadonnées supplémentaires';
      this.text='autorisées';
    }
    else{
      this.text1='Métadonnées supplémentaires';
      this.text = 'non autorisées';
    }
    this.stateToggleButton.emit(this.checked);
  }
}


