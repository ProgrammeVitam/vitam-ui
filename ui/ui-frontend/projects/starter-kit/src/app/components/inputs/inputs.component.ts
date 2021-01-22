import { Component, OnInit } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';

@Component({
  selector: 'starter-kit-inputs',
  templateUrl: './inputs.component.html',
  styleUrls: ['./inputs.component.scss']
})
export class InputsComponent implements OnInit {

  public control = new FormControl();
  
  public streetEmpty = new FormControl('', [Validators.maxLength(3)]);
  public streetInvalid = new FormControl('azerty', [Validators.maxLength(3)]);
  public streetDisable = new FormControl('azerty', [Validators.maxLength(6)]);
  public emailFirstPart = new FormControl('azerty', [Validators.maxLength(25)]);
  public email = new FormControl('azerty@test.fr', [Validators.maxLength(25)]);
  public domain = new FormControl('test.fr', [Validators.maxLength(10)]);
  public emails = new FormControl(['azerty@test.fr' , 'azerty@test2.com'], [Validators.maxLength(30)]);
  public list = new FormControl(['azerty1' , 'azerty2'], [Validators.maxLength(30)]);
  public country = new FormControl('FR', [Validators.maxLength(10)]);
  public textarea = new FormControl('name\naddress\ncity', [Validators.maxLength((25))]);
  public level = new FormControl('LEVEL', [Validators.maxLength((10))]);
  public toggle = new FormControl('Value 3');
  public duration = new FormControl( {days: 5, hours: 10, minutes: 5});
  public file = new FormControl( new File(['test'], 'test', {type: 'text/plain'}));

  constructor() { }

  onChange = (_: any) => {};
  onTouched = () => {};


  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  ngOnInit() { }

}
