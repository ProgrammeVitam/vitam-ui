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
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Customer, Logo } from 'ui-frontend-common';
import { CustomerService } from '../../../../core/customer.service';
import { LogosSafeResourceUrl } from './../logos-safe-resource-url.interface';

@Component({
  selector: 'app-graphic-identity-update',
  templateUrl: './graphic-identity-update.component.html',
  styleUrls: ['./graphic-identity-update.component.scss']
})
export class GraphicIdentityUpdateComponent implements OnInit, OnDestroy {

  private destroy = new Subject();
  private _customForm: FormGroup;
  public get customForm(): FormGroup { return this._customForm; }
  public set customForm(form: FormGroup) {
    this._customForm = form;
    this.disabled = !(this._customForm && this._customForm.valid);
  }

  public logos: Logo[];
  public customerLogosUrl: LogosSafeResourceUrl;

  public disabled = true;

  constructor(
    public dialogRef: MatDialogRef<GraphicIdentityUpdateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { customer: Customer, logos: LogosSafeResourceUrl },
    private customerService: CustomerService,
  ) {}

  ngOnDestroy(): void {
    this.destroy.next();
  }

  ngOnInit() {
    this.customerLogosUrl = this.data.logos;
  }

  public onCancel(): void {
    this.dialogRef.close();
  }

  public updateGraphicIdentity(): void {

    if (this.customForm.valid) {
      this.customerService.patch(this.customForm.value, this.logos)
      .pipe(takeUntil(this.destroy))
      .subscribe(
        () => {
          this.dialogRef.close(true);
        },
        (error) => {
          this.dialogRef.close(false);
          console.error(error);
        });
    }
  }
}
