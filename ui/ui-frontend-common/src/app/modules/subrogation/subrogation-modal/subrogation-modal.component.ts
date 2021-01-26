/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

import { AuthService } from '../../auth.service';
import { Subrogation } from '../../models';
import { NotificationSnackBarComponent } from '../notification-snack-bar/notification-snack-bar.component';
import { NotificationType } from '../notification-type.enum';
import { SubrogationService } from '../subrogation.service';

const PROGRESS_BAR_MULTIPLICATOR = 100;

@Component({
  selector: 'vitamui-common-subrogation-modal',
  templateUrl: './subrogation-modal.component.html',
  styleUrls: ['./subrogation-modal.component.scss'],
})
export class SubrogationModalComponent implements OnInit {

  public stepIndex = 0;
  public stepCount = 2;
  public domains: string[];
  public user: { email: string, firstname: string, lastname: string };
  public form: FormGroup;
  public pending: boolean;

  private subrogation: Subrogation;

  constructor(
    public dialogRef: MatDialogRef<SubrogationModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public builder: FormBuilder,
    private authService: AuthService,
    private matSnackBar: MatSnackBar,
    private subrogationService: SubrogationService
  ) {
    this.form = this.builder.group({
      emailFirstPart: [null, Validators.required],
      domain: null
    });

  }

  ngOnInit() {
    this.domains = this.data.domains;
    this.user = this.data.user;
    if (this.user) {
      this.stepIndex = 1;
      this.retrieveOrCreateSubrogation(this.user.email);
    } else {
      this.form.get('domain').setValue(this.domains[0], { emitEvent: false });
    }
  }

  get stepProgress() {
    return ((this.stepIndex + 1) / this.stepCount) * PROGRESS_BAR_MULTIPLICATOR;
  }

  // check if a subrogation exists and was already created with the same information
  // a. the subrogation exists, we retrieve it
  // b. we asks for a subrogation
  retrieveOrCreateSubrogation(surrogateEmail: string) {
    this.pending = true;
    this.subrogationService.checkCurrentUserIsInSubrogation().subscribe((response: Subrogation) => {
      if (response.id) {
        if (response.surrogate === surrogateEmail) {
          // tslint:disable-next-line:no-magic-numbers
          this.stepIndex = 1;
          // TODO subscribe to the modal close event and cancel the subrogation
          this.pending = true;
          this.subrogation = response;
          if (this.subrogation && this.subrogation.status === 'ACCEPTED') {
            this.logoutAndLaunchSubrogation(this.subrogation);
          } else {
            this.subrogationService.checkSubrogationStatus(this.subrogation, this.dialogRef).subscribe(() => {
              this.logoutAndLaunchSubrogation(this.subrogation);
            });
          }
        } else {
          this.dialogRef.close();
          this.matSnackBar.openFromComponent(NotificationSnackBarComponent, {
            panelClass: 'vitamui-snack-bar',
            data: { type: NotificationType.SUBRO_ALREADY_RUNNING_WITH_OTHER_USERS, email : response.surrogate },
            duration: 10000
          });
        }

      } else {
        this.createSubrogation(surrogateEmail);
      }
    });
  }

  askForSubrogation() {
    const surrogateEmail = this.form.get('emailFirstPart').value + '@' + this.form.get('domain').value;
    this.user = { email: surrogateEmail, firstname: undefined, lastname: undefined };
    this.retrieveOrCreateSubrogation(surrogateEmail);
  }

  createSubrogation(surrogateEmail: string) {
    // TODO subscribe to the modal close event and cancel the subrogation
    this.pending = true;
    const currentUserEmail = this.authService.user.email;
    this.subrogation = {
      id: null,
      status: 'CREATED',
      date: null,
      surrogate: surrogateEmail,
      superUser: currentUserEmail,
    };
    this.subrogationService.createSubrogation(this.subrogation).subscribe(
      (subrogation) => {
        // tslint:disable-next-line:no-magic-numbers
        this.stepIndex = 1;
        this.subrogation = subrogation;
        this.subrogationService.checkSubrogationStatus(this.subrogation, this.dialogRef).subscribe(() => {
          this.logoutAndLaunchSubrogation(this.subrogation);
        });
      },
      () => {
        this.handleSubrogationError();
      });
  }

  handleSubrogationError() {
    this.dialogRef.close();
    this.matSnackBar.openFromComponent(NotificationSnackBarComponent, {
      panelClass: 'vitamui-snack-bar',
      data: { type: NotificationType.SUBRO_UNAVAILABLE },
      duration: 10000
    });
  }

  logoutAndLaunchSubrogation(subrogation: Subrogation) {
    this.dialogRef.close();
    this.authService.logoutForSubrogation(subrogation.superUser, subrogation.surrogate);
  }

  onCancel() {
    this.dialogRef.close();
  }

  cancelSubrogation() {
    this.subrogationService.cancelSubrogation(this.subrogation).subscribe(() => {
      this.dialogRef.close();
    });
  }

}
