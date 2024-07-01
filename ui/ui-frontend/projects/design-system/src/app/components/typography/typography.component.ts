import { Component } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { EllipsisDirective } from 'vitamui-library';

@Component({
  selector: 'design-system-typography',
  templateUrl: './typography.component.html',
  styleUrls: ['./typography.component.scss'],
  standalone: true,
  imports: [EllipsisDirective, TranslateModule],
})
export class TypographyComponent {}
