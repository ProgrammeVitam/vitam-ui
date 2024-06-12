import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatLegacyButtonModule as MatButtonModule } from '@angular/material/legacy-button';
import { MatLegacyMenuModule as MatMenuModule } from '@angular/material/legacy-menu';
import { VitamuiMenuButtonComponent } from './vitamui-menu-button.component';

@NgModule({
  declarations: [VitamuiMenuButtonComponent],
  imports: [CommonModule, MatButtonModule, MatMenuModule],
  exports: [VitamuiMenuButtonComponent, MatButtonModule, MatMenuModule],
})
export class VitamuiMenuButtonModule {}
