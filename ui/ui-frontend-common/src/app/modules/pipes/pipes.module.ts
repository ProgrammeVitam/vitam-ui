import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { BytesPipe } from './bytes.pipe';
import { HighlightPipe } from './highlight.pipe';
import { StrongifyPipe } from './strongify.pipe';
import { TruncatePipe } from './truncate.pipe';

@NgModule({
  declarations: [
    BytesPipe,
    HighlightPipe,
    StrongifyPipe,
    TruncatePipe
  ],
  imports: [
    CommonModule
  ],
  exports: [
    BytesPipe,
    HighlightPipe,
    StrongifyPipe,
    TruncatePipe
  ]
})
export class PipesModule { }
