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
import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { SafeUrl } from '@angular/platform-browser';
import { Customer } from 'ui-frontend-common';
import { CustomerService } from '../../../../core/customer.service';

const IMAGE_TYPE_PREFIX = 'image';

@Component({
  selector: 'app-graphic-identity-update',
  templateUrl: './graphic-identity-update.component.html',
  styleUrls: ['./graphic-identity-update.component.scss']
})
export class GraphicIdentityUpdateComponent implements OnInit {

  customer: Customer;
  logoUrl: SafeUrl;
  graphicIdentityForm: FormGroup;
  hasCustomGraphicIdentity: boolean;
  hasDropZoneOver = false;
  imageToUpload: File = null;
  lastImageUploaded: File = null;
  imageUrl: any;
  lastUploadedImageUrl: any;
  hasError = true;
  message: string;

  @ViewChild('fileSearch', { static: false }) fileSearch: any;

  constructor(
    public dialogRef: MatDialogRef<GraphicIdentityUpdateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { customer: Customer, logo: SafeUrl },
    private formBuilder: FormBuilder,
    private customerService: CustomerService
  ) {
    this.customer = this.data.customer;
    this.logoUrl = this.data.logo;
    this.graphicIdentityForm = this.formBuilder.group({
      hasCustomGraphicIdentity: null,
      themeColors: null
    });
  }

  ngOnInit() {
    this.graphicIdentityForm.get('hasCustomGraphicIdentity').setValue(this.customer.hasCustomGraphicIdentity);
    this.hasCustomGraphicIdentity = this.graphicIdentityForm.get('hasCustomGraphicIdentity').value;

    this.graphicIdentityForm.get('themeColors').setValue(this.getThemeColors(this.customer.themeColors));

    this.graphicIdentityForm.get('hasCustomGraphicIdentity').valueChanges.subscribe(() => {
      this.hasCustomGraphicIdentity = this.graphicIdentityForm.get('hasCustomGraphicIdentity').value;
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
  }

  onCancel() {
    this.dialogRef.close();
  }

  onImageDropped(files: FileList) {
    this.hasDropZoneOver = false;
    this.handleImage(files);
  }

  handleImage(files: FileList) {
    this.hasError = false;
    this.lastImageUploaded = this.imageToUpload;
    this.message = null;
    this.imageToUpload = files.item(0);
    if (this.imageToUpload.type.split('/')[0] !== IMAGE_TYPE_PREFIX) {
      this.message = 'Le fichier que vous essayez de déposer n\'est pas une image';
      this.hasError = true;
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
          this.hasError = true;
        } else {
          this.imageUrl = logoUrl;
          this.hasCustomGraphicIdentity = true;
          this.graphicIdentityForm.get('hasCustomGraphicIdentity').setValue(true, { emitEvent: false });
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

  updateGraphicIdentity() {
    if (!this.isGraphicIdentityFormValid) { return; }
    const formData = {
      id : this.customer.id,
      hasCustomGraphicIdentity: this.graphicIdentityForm.get('hasCustomGraphicIdentity').value,
      themeColors: this.updateForCustomerModel(this.graphicIdentityForm.get('themeColors').value)
    };
    this.customerService.patch(formData, this.imageToUpload)
      .subscribe(
        () => {
          this.dialogRef.close(true);
        },
        (error) => {
          this.dialogRef.close(false);
          console.error(error);
        });
  }

  getThemeColors(themeColors: any): any[] {
    const formData: any[] = [];

    for (const color in themeColors) {
      if (themeColors.hasOwnProperty(color)) {
        const colorToUpdate = { colorName: color, colorValue: themeColors[color]};
        formData.push(colorToUpdate);
      }
    }

    return formData;
  }

  updateForCustomerModel(formData: any): any {
    const themeColors: any = {};
    for (const color of formData) {
      themeColors[color.colorName] = color.colorValue;
    }

    return themeColors;
  }

  isGraphicIdentityFormValid() {
    return this.graphicIdentityForm.get('hasCustomGraphicIdentity').value === false ||
      (this.graphicIdentityForm.get('hasCustomGraphicIdentity').value === true && this.imageUrl);
    // TODO: Update me to add themeColors !
    // FIXME: What if we dont want to update imageURL ? Can we test image OR themeColors updates
  }

}
