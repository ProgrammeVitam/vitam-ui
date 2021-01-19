import {
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

  public messageTranslations: {
    id: number;
    language: string;
    title: string;
    description: string;
    isValid: boolean;
  }[] = [];

  public messageTranslationValid = true;

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
      messageTranslationValid: [true]
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
    this.homepageMessageForm.get('messageTranslations').setValue([]);

    if (this.customer && this.customer.id) {
      this.homepageMessageForm.get('id').setValue(this.customer.id);
    }

    this.homepageMessageForm.valueChanges.subscribe(() => {
      this.formToSend.emit({ form: this.homepageMessageForm });
    });
  }

  public onAdd(): void {
    console.log('add');
    this.messageTranslations = this.homepageMessageForm.get('messageTranslations').value;

    const emptyTranslation = {
      id: this.messageTranslations?.length + 1,
      language: '',
      title: '',
      description: '',
      isValid : false,
    };

    this.messageTranslations.push(emptyTranslation);
    this.messageTranslationValid = false;

    this.homepageMessageForm.get('messageTranslations').setValue(this.messageTranslations);
    this.homepageMessageForm.get('messageTranslationValid').setValue(this.messageTranslationValid);
  }


  public updateTranslation(data: { form: FormGroup }): void {
    console.log('update');
  
    const idValue =  data.form.get('id')?.value;
    const oldTranslations = [...this.homepageMessageForm.get('messageTranslations').value];

    console.log(idValue);
    console.log(oldTranslations);

    const newTranslation = {
      id: idValue,
      language: data.form.get('language')?.value,
      title: data.form.get('portalTitle')?.value,
      description: data.form.get('portalMessage')?.value,
      isValid : data.form.valid
    };

    const i = oldTranslations.findIndex((item: { id: any; }) => item.id === idValue);
    console.log('index');
    console.log(idValue);
    if (i > -1) { oldTranslations[i] = newTranslation; } else { oldTranslations.push(newTranslation); }

    console.log(oldTranslations);
    
    this.homepageMessageForm.get('messageTranslations').setValue(oldTranslations);
    this.homepageMessageForm.get('messageTranslationValid').setValue(data.form.valid);

    console.log(this.homepageMessageForm.get('messageTranslations').value);
  }

  public removeTranslation(data: { form: FormGroup }): void {

    console.log('form translation');
    const idValue =  data.form.get('id')?.value;
    let oldTranslations = [...this.homepageMessageForm.get('messageTranslations').value];

    let isTranslationValid = true;

    console.log(idValue);
    console.log(oldTranslations);

    oldTranslations = oldTranslations.filter(f => f.id !== idValue).map((t, index) => {
        t.id = index + 1;
        if (!t.isValid) {isTranslationValid = false; }
        return t;
      });

    this.messageTranslations = oldTranslations;
    this.messageTranslationValid = isTranslationValid;
    this.homepageMessageForm.get('messageTranslations').setValue(oldTranslations);
    this.homepageMessageForm.get('messageTranslationValid').setValue(isTranslationValid);

    console.log('new translations');
    console.log(oldTranslations);
    console.log('form translation');
    console.log(this.homepageMessageForm.get('messageTranslations').value);
  }
}
