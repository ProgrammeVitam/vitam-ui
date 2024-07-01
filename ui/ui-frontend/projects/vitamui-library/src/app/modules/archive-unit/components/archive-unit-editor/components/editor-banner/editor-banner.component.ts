import { Component, Input } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'vitamui-common-editor-banner',
  templateUrl: './editor-banner.component.html',
  styleUrls: ['./editor-banner.component.scss'],
  standalone: true,
  imports: [TranslateModule],
})
export class EditorBannerComponent {
  @Input() title!: string;
}
