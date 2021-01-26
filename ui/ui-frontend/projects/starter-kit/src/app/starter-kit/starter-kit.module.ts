import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MarkdownModule } from 'ngx-markdown';
import { StarterKitComponent } from './starter-kit.component';

@NgModule({
  declarations: [
    StarterKitComponent
  ],
  imports: [
    CommonModule,
    MarkdownModule.forRoot(),
  ]
})
export class StarterKitModule { }
