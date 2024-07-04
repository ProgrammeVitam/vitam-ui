import { Component } from '@angular/core';
import { ScrollTopComponent } from '../scroll-top/scroll-top.component';

@Component({
  selector: 'vitamui-common-body',
  templateUrl: './vitamui-body.component.html',
  styleUrls: ['./vitamui-body.component.scss'],
  standalone: true,
  imports: [ScrollTopComponent],
})
export class VitamuiBodyComponent {
  constructor() {}
}
