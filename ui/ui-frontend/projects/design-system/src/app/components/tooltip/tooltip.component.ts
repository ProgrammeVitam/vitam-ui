import { Component } from '@angular/core';
import { CommonTooltipDirective } from 'vitamui-library';

@Component({
  selector: 'design-system-tooltip',
  templateUrl: './tooltip.component.html',
  styleUrls: ['./tooltip.component.scss'],
  standalone: true,
  imports: [CommonTooltipDirective],
})
export class TooltipComponent {
  public progressValue: number;
}
