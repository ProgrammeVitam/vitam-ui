import { Component, OnInit } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';

@Component({
  selector: 'starter-kit-inputs',
  templateUrl: './inputs.component.html',
  styleUrls: ['./inputs.component.scss']
})
export class InputsComponent implements OnInit {

  public streetEmpty = new FormControl('', [Validators.maxLength(3)]);
  public street = new FormControl('azerty', [Validators.maxLength(3)]);

  constructor() { }

  ngOnInit() {
  }

}
