import { Component, Input } from '@angular/core';

@Component({
  selector: 'vitamui-common-editor-banner',
  templateUrl: './editor-banner.component.html',
  styleUrls: ['./editor-banner.component.scss'],
})
export class EditorBannerComponent {
  @Input() title!: string;
}
