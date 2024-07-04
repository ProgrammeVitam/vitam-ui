import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-welcome-message',
  templateUrl: './welcome-message.component.html',
  styleUrls: ['./welcome-message.component.scss'],
  standalone: true,
})
export class WelcomeMessageComponent {
  @Input() title: string;

  @Input() message: string;

  @Input() imgUrl: string;
}
