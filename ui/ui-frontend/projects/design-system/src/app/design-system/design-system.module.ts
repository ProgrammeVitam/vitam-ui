import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MarkdownModule } from 'ngx-markdown';
import { DesignSystemComponent } from './design-system.component';

@NgModule({
  declarations: [DesignSystemComponent],
  imports: [CommonModule, MarkdownModule.forRoot()],
})
export class DesignSystemModule {}
