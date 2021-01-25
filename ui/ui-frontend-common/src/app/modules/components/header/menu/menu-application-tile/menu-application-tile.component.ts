import { Component, Input, OnInit } from '@angular/core';
import { Application } from '../../../../models/application';

@Component({
  selector: 'vitamui-common-menu-application-tile',
  templateUrl: './menu-application-tile.component.html',
  styleUrls: ['./menu-application-tile.component.scss']
})
export class MenuApplicationTileComponent implements OnInit {

  @Input()
  public application: Application;

  @Input()
  public hlCriteria?: string;

  constructor() { }

  ngOnInit() {
  }

}
