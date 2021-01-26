import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';

@Component({
  selector: 'starter-kit-buttons',
  templateUrl: './buttons.component.html',
  styleUrls: ['./buttons.component.scss']
})
export class ButtonsComponent implements OnInit {

  public control = new FormControl();

  constructor() { }

  ngOnInit() {
  }

  public onClick(): void {
    console.log('[onClick]');
  }

}
