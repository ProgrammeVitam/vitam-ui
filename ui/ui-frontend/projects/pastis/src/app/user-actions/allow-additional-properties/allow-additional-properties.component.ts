import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'allow-additional-properties',
  templateUrl: './allow-additional-properties.component.html',
  styleUrls: ['./allow-additional-properties.component.scss'],
})
export class AllowAdditionalPropertiesComponent implements OnInit {
  constructor() {}
  @Output() stateToggleButton = new EventEmitter<boolean>();

  @Input()
  checked: boolean;

  text: string;
  text1: string;

  ngOnInit(): void {
    this.text1 = 'Métadonnées supplémentaires';
    this.text = ' non autorisées';
  }

  changed() {
    if (this.checked) {
      this.text1 = 'Métadonnées supplémentaires ';
      this.text = 'autorisées';
    } else {
      this.text1 = 'Métadonnées supplémentaires ';
      this.text = 'non autorisées';
    }
    this.stateToggleButton.emit(this.checked);
  }
}
