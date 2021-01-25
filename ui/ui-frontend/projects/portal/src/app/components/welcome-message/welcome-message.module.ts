import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { WelcomeMessageComponent } from './welcome-message.component';

@NgModule({
  declarations:  [WelcomeMessageComponent ],
  imports: [
    CommonModule
  ],
  exports: [ WelcomeMessageComponent ]
})
export class WelcomeMessageModule { }
