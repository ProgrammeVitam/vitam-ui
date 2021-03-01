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
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ValidatorFn, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { Subject } from 'rxjs';
import {Customer, Logo, Theme, ThemeColorType, ThemeService} from 'ui-frontend-common';
import { LogosSafeResourceUrl } from './../logos-safe-resource-url.interface';

@Component({
  selector: 'app-graphic-identity',
  templateUrl: './graphic-identity.component.html',
  styleUrls: ['./graphic-identity.component.scss']
})
export class GraphicIdentityComponent implements OnInit, OnDestroy {

  private hexValidator: ValidatorFn = Validators.pattern(/#([0-9A-Fa-f]{6})/);

  private destroy = new Subject();
  public graphicIdentityForm: FormGroup;

  @Input()
  public customer?: Customer;

  @Input()
  public customerLogosUrl?: LogosSafeResourceUrl;

  public defaultForm: FormGroup;
  public customerForm: FormGroup;

  @Output()
  public formToSend = new EventEmitter<{form: FormGroup, logos: Logo[]}>();

  private customerTheme: Theme = {
    colors: null,
    headerUrl: '',
    footerUrl: '',
    portalUrl: '',
    portalMessage: '',
    portalTitle: ''
  };

  private defaultTheme: Theme = this.themeService.defaultTheme;

  public displayCustomGraphicIdentity = new FormControl(false);

  constructor(
    public dialogRef: MatDialogRef<GraphicIdentityComponent>,
    private formBuilder: FormBuilder,
    private themeService: ThemeService,
  ) {}

  ngOnDestroy(): void {
    this.destroy.next();
  }

  ngOnInit() {

    this.graphicIdentityForm = this.formBuilder.group({
      id : null,
      hasCustomGraphicIdentity: false,
      themeColors: this.formBuilder.group({
        [ThemeColorType.VITAMUI_PRIMARY]: new FormControl('', [this.hexValidator, Validators.required]),
        [ThemeColorType.VITAMUI_SECONDARY]: new FormControl('', [this.hexValidator, Validators.required]),
        [ThemeColorType.VITAMUI_TERTIARY]: new FormControl('', this.hexValidator),
        [ThemeColorType.VITAMUI_HEADER_FOOTER]: new FormControl('', [this.hexValidator, Validators.required]),
        [ThemeColorType.VITAMUI_BACKGROUND]: new FormControl('', [this.hexValidator, Validators.required]),
      }),
      portalTitle: ['', [Validators.required]],
      portalMessage: ['', [Validators.required, Validators.maxLength(500)]],
      headerUrl: '',
      footerUrl: '',
      portalUrl: '',
    });

    this.customerTheme = {
      colors: this.customer && this.customer.themeColors
        ? this.themeService.getThemeColors(this.customer.themeColors)
        : this.defaultTheme.colors,
      headerUrl: this.customerLogosUrl ? this.customerLogosUrl.headerUrl : this.defaultTheme.headerUrl,
      footerUrl: this.customerLogosUrl ? this.customerLogosUrl.footerUrl : this.defaultTheme.footerUrl,
      portalUrl: this.customerLogosUrl ? this.customerLogosUrl.portalUrl : this.defaultTheme.portalUrl,
      portalMessage: this.customer && this.customer.portalMessage ? this.customer.portalMessage : this.defaultTheme.portalMessage,
      portalTitle: this.customer && this.customer.portalTitle ? this.customer.portalTitle : this.defaultTheme.portalTitle
    };

    if (this.customer) {

      if (this.customer.id) {
        this.graphicIdentityForm.get('id').setValue(this.customer.id);
      }

      if (this.customer.hasCustomGraphicIdentity) {
        this.displayCustomGraphicIdentity.patchValue(true);
      }
    }

    this.defaultForm = this.setTheme(this.defaultTheme);
    this.customerForm = this.setTheme(this.customerTheme);

    if (this.displayCustomGraphicIdentity.value === true) {
      this.customerForm.get('hasCustomGraphicIdentity').patchValue(true);
      this.formToSend.emit({form: this.customerForm, logos: null});
    } else {
      this.formToSend.emit({form: this.formBuilder.group({
        id: this.graphicIdentityForm.get('id').value,
        hasCustomGraphicIdentity: false
      }), logos: null});
    }

    this.displayCustomGraphicIdentity.valueChanges.subscribe((hasGraphicIdentity: boolean) => {
      if (!hasGraphicIdentity) {
        this.formToSend.emit({
          form: this.formBuilder.group({
              id: this.graphicIdentityForm.get('id').value,
              hasCustomGraphicIdentity: false
            }
          ),
          logos: null});
      } else {
        this.customerForm.get('hasCustomGraphicIdentity').patchValue(true);
        this.formToSend.emit({form: this.customerForm, logos: null});
      }
    });

  }

  public sendForm(data: {form: FormGroup, logos: Logo[]}): void {
    this.customerForm = data.form;
    this.formToSend.emit(data);
  }

  private setTheme(theme: Theme): FormGroup {

    let newTheme = this.formBuilder.group(this.graphicIdentityForm.get('themeColors').value);

    if (theme.colors) {
      newTheme = new FormGroup({
        [ThemeColorType.VITAMUI_PRIMARY]: new FormControl(
          theme.colors[ThemeColorType.VITAMUI_PRIMARY], [this.hexValidator, Validators.required]),
        [ThemeColorType.VITAMUI_SECONDARY]: new FormControl(
          theme.colors[ThemeColorType.VITAMUI_SECONDARY], [this.hexValidator, Validators.required]),
        [ThemeColorType.VITAMUI_TERTIARY]: new FormControl(
          theme.colors[ThemeColorType.VITAMUI_TERTIARY], this.hexValidator),
        [ThemeColorType.VITAMUI_HEADER_FOOTER]: new FormControl(
          theme.colors[ThemeColorType.VITAMUI_HEADER_FOOTER], [this.hexValidator, Validators.required]),
        [ThemeColorType.VITAMUI_BACKGROUND]: new FormControl(
          theme.colors[ThemeColorType.VITAMUI_BACKGROUND], [this.hexValidator, Validators.required]),
      });
    }

    this.graphicIdentityForm.get('headerUrl').setValue(theme.headerUrl);
    this.graphicIdentityForm.get('footerUrl').setValue(theme.footerUrl);
    this.graphicIdentityForm.get('portalUrl').setValue(theme.portalUrl);

    this.graphicIdentityForm.get('portalTitle').setValue(theme.portalTitle);
    this.graphicIdentityForm.get('portalMessage').setValue(theme.portalMessage);

    const newForm = this.formBuilder.group(this.graphicIdentityForm.value);
    newForm.controls.themeColors = newTheme;
    newForm.get('portalTitle').validator = this.graphicIdentityForm.get('portalTitle').validator;
    newForm.get('portalMessage').validator = this.graphicIdentityForm.get('portalMessage').validator;
    return newForm;
  }
}
