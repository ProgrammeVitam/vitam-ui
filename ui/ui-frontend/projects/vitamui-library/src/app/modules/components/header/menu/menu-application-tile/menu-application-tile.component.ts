import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Application } from './../../../../models/application';
import { TranslateModule } from '@ngx-translate/core';
import { HighlightPipe } from '../../../../pipes/highlight.pipe';
import { NgClass } from '@angular/common';

@Component({
  selector: 'vitamui-common-menu-application-tile',
  templateUrl: './menu-application-tile.component.html',
  styleUrls: ['./menu-application-tile.component.scss'],
  standalone: true,
  imports: [NgClass, HighlightPipe, TranslateModule],
})
export class MenuApplicationTileComponent {
  @Input()
  public application: Application;

  @Input()
  public applicationUrl: string;

  @Input()
  public hlCriteria?: string;

  @Output() openApplication = new EventEmitter<Application>();

  constructor() {}
}
