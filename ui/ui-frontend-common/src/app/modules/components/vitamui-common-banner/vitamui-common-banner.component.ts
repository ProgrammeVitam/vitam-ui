import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

declare interface MoreButtonAction {
  identifier: string;
  label: string;
  icon?: string;
}
@Component({
  // tslint:disable-next-line: component-selector
  selector: 'vitamui-common-banner',
  templateUrl: './vitamui-common-banner.component.html',
  styleUrls: ['./vitamui-common-banner.component.scss']
})
export class VitamuiCommonBannerComponent implements OnInit {

  @Input() searchbarPlaceholder: string;
  @Input() actionsList: MoreButtonAction[];
  @Input() disableSearchBar = false;

  @Output() action = new EventEmitter<string>();
  @Output() search = new EventEmitter<string>();

  constructor() { }

  ngOnInit() {
  }

}
