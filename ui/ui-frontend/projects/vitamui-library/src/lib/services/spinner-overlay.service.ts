import { Component, Injectable, NgModule } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  template: `<mat-spinner class="vitamui-spinner x-large my-3"></mat-spinner>`,
  styles: [
    `
      ::ng-deep .spinner-overlay .mat-dialog-container {
        background: transparent;
        box-shadow: none;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    `,
  ],
})
class SpinnerComponent {}

@NgModule({
  declarations: [SpinnerComponent],
  imports: [MatProgressSpinnerModule],
  exports: [],
})
export class SpinnerModule {}

@Injectable({
  providedIn: 'root',
})
export class SpinnerOverlayService {
  private spinnerOverlayRef: MatDialogRef<any>;

  constructor(private dialog: MatDialog) {}

  public open() {
    this.spinnerOverlayRef = this.dialog.open(SpinnerComponent, {
      disableClose: true,
      height: '100%',
      width: '100%',
      panelClass: 'spinner-overlay',
    });
    // We prevent usage of "TAB" while the overlay is opened in order to prevent being able to edit forms in the background.
    const preventTab = (event: KeyboardEvent) => {
      if (event.key === 'Tab') {
        event.preventDefault();
      }
    };
    document.addEventListener('keydown', preventTab);
    this.spinnerOverlayRef.afterClosed().subscribe(() => {
      document.removeEventListener('keydown', preventTab);
    });
  }

  public close() {
    this.spinnerOverlayRef?.close();
  }
}
