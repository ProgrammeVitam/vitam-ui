import { Component } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'design-system-elevations',
  templateUrl: './elevations.component.html',
  styleUrls: ['./elevations.component.scss'],
  standalone: true,
  imports: [TranslateModule],
})
export class ElevationComponent {}
