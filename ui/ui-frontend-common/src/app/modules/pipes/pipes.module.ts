import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { BytesPipe } from './bytes.pipe';
import { EmptyPipe } from './empty.pipe';
import { HighlightPipe } from './highlight.pipe';
import { SafeStylePipe } from './safe-style.pipe';
import { StrongifyPipe } from './strongify.pipe';
import { TruncatePipe } from './truncate.pipe';

@NgModule({
  declarations: [
    BytesPipe,
    HighlightPipe,
    StrongifyPipe,
    TruncatePipe,
    SafeStylePipe,
    EmptyPipe,
  ],
  imports: [
    CommonModule
  ],
  exports: [
    BytesPipe,
    HighlightPipe,
    StrongifyPipe,
    TruncatePipe,
    SafeStylePipe,
    EmptyPipe,
  ]
})
export class PipesModule { }
