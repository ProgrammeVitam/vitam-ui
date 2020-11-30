import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { User } from '../../../models/user/user.interface';

@Component({
  selector: 'vitamui-common-user-photo',
  templateUrl: './user-photo.component.html',
  styleUrls: ['./user-photo.component.scss']
})
export class UserPhotoComponent implements OnInit {

  @Input() user: User;

  @Input() size = 40;

  @Output() photoClicked = new EventEmitter<any>();

  constructor() { }

  ngOnInit() {}

}
