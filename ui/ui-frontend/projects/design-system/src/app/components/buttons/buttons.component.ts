import { Component } from '@angular/core';
import { FormControl } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'design-system-buttons',
  templateUrl: './buttons.component.html',
  styleUrls: ['./buttons.component.scss'],
  standalone: true,
  imports: [TranslateModule],
})
export class ButtonsComponent {
  public control = new FormControl();

  public onClick(): void {
    console.log('[onClick]');
  }
}
