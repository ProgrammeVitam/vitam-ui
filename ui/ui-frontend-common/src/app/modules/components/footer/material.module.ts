import { NgModule } from '@angular/core';
import {
  MatButtonModule,
  MatInputModule
} from '@angular/material';

@NgModule({
  imports: [
    MatInputModule,
    MatButtonModule
  ],
  exports: [
    MatInputModule,
    MatButtonModule,
  ],
})
export class MaterialModule {}
