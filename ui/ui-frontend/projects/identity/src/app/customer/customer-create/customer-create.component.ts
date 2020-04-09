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
import { merge, Subscription } from 'rxjs';
import { ConfirmDialogService, OtpState } from 'ui-frontend-common';

import { Component, Inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { CustomerService } from '../../core/customer.service';
import { CustomerCreateValidators } from './customer-create.validators';

const PROGRESS_BAR_MULTIPLICATOR = 100;
const IMAGE_TYPE_PREFIX = 'image';

@Component({
  selector: 'app-customer-create',
  templateUrl: './customer-create.component.html',
  styleUrls: ['./customer-create.component.scss']
})
export class CustomerCreateComponent implements OnInit, OnDestroy {

  form: FormGroup;
  stepIndex = 0;
  customerInfo: { code: string, name: string, companyName: string } = { code: '', name: '', companyName: '' };
  hasCustomGraphicIdentity = false;
  hasDropZoneOver = false;
  imageToUpload: File = null;
  lastImageUploaded: File = null;
  imageUrl: any;
  lastUploadedImageUrl: any;
  hasError = true;
  message: string;
  creating = false;

  // stepCount is the total number of steps and is used to calculate the advancement of the progress bar.
  // We could get the number of steps using ViewChildren(StepComponent) but this triggers a
  // "Expression has changed after it was checked" error so we instead manually define the value.
  // Make sure to update this value whenever you add or remove a step from the  template.
  private stepCount = 4;
  private keyPressSubscription: Subscription;

  @ViewChild('fileSearch', { static: false }) fileSearch: any;

  constructor(
    public dialogRef: MatDialogRef<CustomerCreateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    private customerService: CustomerService,
    private customerCreateValidators: CustomerCreateValidators,
    private confirmDialogService: ConfirmDialogService
  ) {
  }

  ngOnInit() {
    this.form = this.formBuilder.group({
      enabled: [true, Validators.required],
      code: [
        null,
        [Validators.required, Validators.pattern(/^[0-9]{6,20}$/)],
        this.customerCreateValidators.uniqueCode(),
      ],
      name: [null, Validators.required],
      companyName: [null, Validators.required],
      passwordRevocationDelay: 6,
      otp: OtpState.OPTIONAL,
      address: this.formBuilder.group({
        street: [null, Validators.required],
        zipCode: [null, Validators.required],
        city: [null, Validators.required],
        country: ['FR', Validators.required],
      }),
      language: ['FRENCH', Validators.required],
      emailDomains: [null, Validators.required],
      defaultEmailDomain: [null, Validators.required],
      hasCustomGraphicIdentity: false,
      owners: this.formBuilder.array([
        this.formBuilder.control(null, Validators.required),
      ])
    });

    this.onChanges();
    this.form.get('hasCustomGraphicIdentity').valueChanges.subscribe(() => {
      this.hasCustomGraphicIdentity = this.form.get('hasCustomGraphicIdentity').value;
      this.message = null;
      if (this.hasCustomGraphicIdentity) {
        this.imageUrl = this.lastUploadedImageUrl;
        this.imageToUpload = this.lastImageUploaded;
      } else {
        this.lastImageUploaded = this.imageToUpload;
        this.lastUploadedImageUrl = this.imageUrl;
        this.imageToUpload = null;
        this.imageUrl = null;
      }
    });

    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  onChanges() {
    merge(
      this.form.get('code').valueChanges,
      this.form.get('name').valueChanges,
      this.form.get('companyName').valueChanges
    )
    .subscribe(() => {
      // reset object to trigger customerInfo input update in child component
      this.customerInfo = {
        code: this.form.get('code').value,
        name: this.form.get('name').value,
        companyName: this.form.get('companyName').value
      };
    });
  }

  onCancel() {
    if (this.form.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit() {
    if (this.form.invalid) { return; }
    this.creating = true;
    this.customerService.create(this.form.value, this.imageToUpload).subscribe(
      () => {
        this.dialogRef.close(true);
      },
      (error) => {
        this.creating = false;
        console.error(error);
      });
  }

  onImageDropped(files: FileList) {
    this.hasDropZoneOver = false;
    this.handleImage(files);
  }

  handleImage(files: FileList) {
    this.lastImageUploaded = this.imageToUpload;
    this.lastUploadedImageUrl = this.imageUrl;
    this.message = null;
    this.imageToUpload = files.item(0);
    if (this.imageToUpload.type.split('/')[0] !== IMAGE_TYPE_PREFIX) {
      this.message = 'Le fichier que vous essayez de déposer n\'est pas une image';
      return;
    }
    const reader = new FileReader();
    const logoImage = new Image();
    reader.onload = () => {
      const logoUrl: any = reader.result;
      logoImage.src = logoUrl;
      logoImage.onload = () => {
        if (logoImage.width > 280 || logoImage.height > 100) {
          this.imageToUpload = this.lastImageUploaded;
          this.message = 'Les dimensions du fichier que vous essayez de déposer sont supérieures à 280px * 100px';
        } else {
          this.imageUrl = logoUrl;
          this.hasCustomGraphicIdentity = true;
          this.form.get('hasCustomGraphicIdentity').setValue(true, { emitEvent: false });
        }
      };
    };
    reader.readAsDataURL(this.imageToUpload);
  }

  onImageDragOver(inDropZone: boolean) {
    this.hasDropZoneOver = inDropZone;
  }

  onImageDragLeave(inDropZone: boolean) {
    this.hasDropZoneOver = inDropZone;
  }

  addLogo() {
    this.fileSearch.nativeElement.click();
  }

  handleFileInput(files: FileList) {
    this.handleImage(files);
  }

  firstStepInvalid(): boolean {
    return this.form.get('code').invalid || this.form.get('code').pending ||
      this.form.get('name').invalid || this.form.get('name').pending ||
      this.form.get('companyName').invalid || this.form.get('companyName').pending ||
      this.form.get('address.street').invalid || this.form.get('address.street').pending ||
      this.form.get('address.zipCode').invalid || this.form.get('address.zipCode').pending ||
      this.form.get('address.city').invalid || this.form.get('address.city').pending ||
      this.form.get('address.country').invalid || this.form.get('address.country').pending;
  }

  secondStepInvalid(): boolean {
    return this.form.get('passwordRevocationDelay').invalid ||
      this.form.get('otp').invalid ||
      this.form.get('emailDomains').invalid ||
      this.form.get('defaultEmailDomain').invalid;
  }

  thirdStepValid(): boolean {
    return this.form.get('hasCustomGraphicIdentity').value === false ||
            (this.form.get('hasCustomGraphicIdentity').value === true && this.imageUrl);
  }

  get stepProgress() {
    return ((this.stepIndex + 1) / this.stepCount) * PROGRESS_BAR_MULTIPLICATOR;
  }

}
