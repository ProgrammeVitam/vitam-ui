import { Component, EventEmitter, Input, Output } from '@angular/core';
import { SearchBarComponent } from '../search-bar/search-bar.component';

@Component({
  selector: 'vitamui-common-banner',
  templateUrl: './vitamui-common-banner.component.html',
  styleUrls: ['./vitamui-common-banner.component.scss'],
  standalone: true,
  imports: [SearchBarComponent],
})
export class VitamuiCommonBannerComponent {
  @Input() searchbarPlaceholder: string;
  @Input() disableSearchBar = false;

  @Output() action = new EventEmitter<string>();
  @Output() search = new EventEmitter<string>();

  constructor() {}
}
