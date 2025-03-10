/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
import { Component, Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  template: `<mat-spinner class="vitamui-spinner x-large my-3"></mat-spinner>`,
  styleUrls: ['spinner-component.scss'],
  imports: [MatProgressSpinnerModule],
})
class SpinnerComponent {}

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
