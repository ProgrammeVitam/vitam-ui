import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { User } from '../../../models/user/user.interface';

@Component({
  selector: 'vitamui-common-user-photo',
  templateUrl: './user-photo.component.html',
  styleUrls: ['./user-photo.component.scss']
})
export class UserPhotoComponent implements OnInit {

  @Input() photo: string;
  @Input() size = 40;
  @Input() hasStatus = false;
  @Input() statusCondition = false;

  @Output() photoClicked = new EventEmitter<any>();

  constructor() { }

  ngOnInit() {}

}
