import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'vitamui-common-banner',
  templateUrl: './vitamui-common-banner.component.html',
  styleUrls: ['./vitamui-common-banner.component.scss']
})
export class VitamuiCommonBannerComponent {

  @Input() searchbarPlaceholder: string;
  @Input() disableSearchBar = false;

  @Output() action = new EventEmitter<string>();
  @Output() search = new EventEmitter<string>();

  constructor() { }

}
