import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { Subject } from 'rxjs';
import { Customer, Theme, ThemeService } from 'ui-frontend-common';

@Component({
  selector: 'app-homepage-message',
  templateUrl: './homepage-message.component.html',
  styleUrls: ['./homepage-message.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomepageMessageComponent implements OnInit, OnDestroy {
  private destroy = new Subject();

  @Input()
  public homepageMessageForm: FormGroup;

  @Input()
  public customer?: Customer;

  public defaultForm: FormGroup;
  public customerForm: FormGroup;

  @Output()
  public formToSend = new EventEmitter<{ form: FormGroup }>();

  private portalMessage: string;
  private portalTitle: string;

  private defaultTheme: Theme = this.themeService.defaultTheme;

  private messageTranslations: {
    id: number;
    language: string;
    title: string;
    description: string;
  }[] = [];

  private validTranslations: {
    id: number;
    isValid: boolean;
  }[] = [];

  constructor(
    public dialogRef: MatDialogRef<HomepageMessageComponent>,
    private formBuilder: FormBuilder,
    private themeService: ThemeService
  ) { }

  ngOnDestroy(): void {
    this.destroy.next();
  }

  ngOnInit() {
    this.homepageMessageForm = this.formBuilder.group({
      id: null,
      portalTitle: ['', [Validators.required]],
      portalMessage: ['', [Validators.required, Validators.maxLength(500)]],
      messageTranslations: [null],
      isFormValid: null,
    });

    this.portalMessage =
      this.customer && this.customer.portalMessage
        ? this.customer.portalMessage
        : this.defaultTheme.portalMessage;
    this.portalTitle =
      this.customer && this.customer.portalTitle
        ? this.customer.portalTitle
        : this.defaultTheme.portalTitle;

    this.homepageMessageForm.get('portalTitle').setValue(this.portalTitle);
    this.homepageMessageForm.get('portalMessage').setValue(this.portalMessage);
    this.homepageMessageForm.get('messageTranslations').setValue(this.messageTranslations);
    this.homepageMessageForm.get('isFormValid').setValue(true);

    if (this.customer && this.customer.id) {
      this.homepageMessageForm.get('id').setValue(this.customer.id);
    }

    this.formToSend.emit({ form: this.homepageMessageForm });

    this.homepageMessageForm.valueChanges.subscribe(() => {
      this.formToSend.emit({ form: this.homepageMessageForm });
    });
  }

  public onAdd(): void {
    this.messageTranslations = this.homepageMessageForm.get('messageTranslations').value;

    let newId = 1;
    const itemsLength = this.messageTranslations?.length;

    if (itemsLength > 0) {
      newId = this.messageTranslations[itemsLength - 1].id + 1;
    }

    const emptyTranslation = {
      id: newId,
      language: '',
      title: '',
      description: ''
    };

    this.messageTranslations.push(emptyTranslation);
  }

  public updateTranslation(data: { form: FormGroup }): void {
    const idValue = data.form.get('id')?.value;

    this.messageTranslations = [...this.homepageMessageForm.get('messageTranslations').value];

    this.messageTranslations.filter(f => f.id === idValue).map(t => {
      t.language = data.form.get('language')?.value;
      t.title = data.form.get('portalTitle')?.value;
      t.description = data.form.get('portalMessage')?.value;
      return t;
    });

    const validationForm = {
      id: idValue,
      isValid: data.form.valid
    };

    const i = this.validTranslations.findIndex((item: { id: any; }) => item.id === idValue);
    if (i > -1) { this.validTranslations[i] = validationForm; } else { this.validTranslations.push(validationForm); }

    this.checkValidation();

    this.homepageMessageForm.get('messageTranslations').patchValue(this.messageTranslations);
  }

  public removeTranslation(data: { form: FormGroup }): void {
    const idValue = data.form.get('id')?.value;

    this.messageTranslations = [...this.homepageMessageForm.get('messageTranslations').value];

    this.messageTranslations = this.messageTranslations.filter(f => f.id !== idValue);
    this.validTranslations = [];
    this.validTranslations = this.validTranslations.filter(f => f.id !== idValue);

    this.homepageMessageForm.get('messageTranslations').patchValue(this.messageTranslations);
  }

  private checkValidation() {
    let valid = true;
    if (this.validTranslations.some((v) => !v.isValid)) {
      valid = false;
    }
    this.homepageMessageForm.get('isFormValid').patchValue(valid);
  }
}
