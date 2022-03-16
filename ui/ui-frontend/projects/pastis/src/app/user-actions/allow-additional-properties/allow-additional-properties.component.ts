import {Component, EventEmitter, OnInit, Output} from '@angular/core';

@Component({
  selector: 'allow-additional-properties',
  templateUrl: './allow-additional-properties.component.html',
  styleUrls: ['./allow-additional-properties.component.scss']
})
export class AllowAdditionalPropertiesComponent implements OnInit {
  constructor() { }

  ngOnInit(): void {
    this.text1="Métadonnées supplémentaires"
    this.text=" non autorisées"

  }
  @Output() stateToggleButton = new EventEmitter<boolean>();

  checked: false;
  text : string;
  text1: string;


  changed(){
    if(this.checked){
      this.text1="Métadonnées supplémentaires "
      this.text="autorisées"
    }
    else{
      this.text1="Métadonnées supplémentaires "
      this.text = "non autorisées"
    }
    this.stateToggleButton.emit(this.checked)
  }
}


