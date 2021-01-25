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
import { LanguageService } from 'projects/identity/src/app/services/LaguageService.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService, Customer, Option, StartupService } from 'ui-frontend-common';

@Component({
  selector: 'app-homepage-message',
  templateUrl: './homepage-message.component.html',
  styleUrls: ['./homepage-message.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [LanguageService]
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
  public formToSend = new EventEmitter<{
    form: FormGroup,
    portalTitles: {
      [language: string]: string;
    },
    portalMessages: {
      [language: string]: string;
    }
  }>();

  private portalTitles: {
    [language: string]: string;
  } = {};

  private portalMessages: {
    [language: string]: string;
  } = {};

  public languages: Option[];

  private language = this.authService.user.language;

  constructor(
    public dialogRef: MatDialogRef<HomepageMessageComponent>,
    private formBuilder: FormBuilder,
    private startupService: StartupService,
    private authService: AuthService,
    private languageService: LanguageService
  ) { }

  ngOnInit() {

    this.homepageMessageForm = this.formBuilder.group({
      id: null,
      portalTitle: ['', [Validators.required]],
      portalMessage: ['', [Validators.required, Validators.maxLength(500)]],
      translations: this.formBuilder.array([])
    });

    console.log('init');
    this.languageService.getLanguages().then(languages => {
      this.languages = languages;
      this.setMessages();
      this.formToSend.emit({ form: this.homepageMessageForm, portalTitles: this.portalTitles, portalMessages: this.portalMessages });
      this.homepageMessageForm.valueChanges.pipe(takeUntil(this.destroy)).subscribe(() => {
        this.sendForm();
      });
    });
  }

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }

  public setMessages() {

    let idCustomer = null;
    const title = this.startupService.getDefaultPortalTitle();
    const message = this.startupService.getDefaultPortalMessage();

    if (this.customer) {
      if (this.customer.id) {
        idCustomer = this.customer.id;
      }
      if (this.customer.language) {
        this.language = this.customer.language;
      }
      if (this.customer.portalMessages) {
        this.portalMessages = this.customer.portalMessages;
      }
      if (this.customer.portalTitles) {
        this.portalTitles = this.customer.portalTitles;
      }
    }

    const defaultTitle = (this.portalTitles && this.portalTitles[this.language]) ? (this.portalTitles[this.language]) : title;
    const defaultMessage = (this.portalMessages && this.portalMessages[this.language]) ? this.portalMessages[this.language] : message;

    this.homepageMessageForm.get('id').patchValue(idCustomer);
    this.homepageMessageForm.get('portalTitle').patchValue(defaultTitle);
    this.homepageMessageForm.get('portalMessage').patchValue(defaultMessage);

    this.languages.forEach(l => {
      if (this.portalTitles[l.key] && this.portalMessages[l.key] && this.language !== l.key) {

        const translation = this.formBuilder.group({
          language: [l.key, Validators.required],
          portalTitle: [this.portalTitles[l.key], [Validators.required]],
          portalMessage: [this.portalMessages[l.key], [Validators.required, Validators.maxLength(500)]],
        });
        this.homepageMessageForm.get('translations').value.push(translation);
      }
    });
  }

  public onAdd(): void {
    const emptyTranslation = this.formBuilder.group({
      language: ['', Validators.required],
      portalTitle: ['', [Validators.required]],
      portalMessage: ['', [Validators.required, Validators.maxLength(500)]],
    });

    this.homepageMessageForm.get('translations').value.push(emptyTranslation);
    this.sendForm();
  }

  public update(): void {
    this.sendForm();
  }

  public remove(index: number): void {
    this.homepageMessageForm.get('translations').value.splice(index, 1);
    this.sendForm();
  }

  private sendForm() {
    this.getTranslations();
    this.formToSend.emit({ form: this.homepageMessageForm, portalTitles: this.portalTitles, portalMessages: this.portalMessages });
  }

  private getTranslations() {
    const titles: { [language: string]: any } = {};
    const messages: { [language: string]: any } = {};

    titles[this.language] = this.homepageMessageForm.get('portalTitle').value;
    messages[this.language] = this.homepageMessageForm.get('portalMessage').value;

    const forms = this.homepageMessageForm.get('translations').value;

    forms.forEach(((form: FormGroup) => {
      const language = form.get('language').value;
      const title = form.get('portalTitle').value;
      const message = form.get('portalMessage').value;

      titles[language] = title;
      messages[language] = message;
    }));

    this.portalTitles = titles;
    this.portalMessages = messages;
  }

  getLanguages(index: number) {
    const forms = [...this.homepageMessageForm.get('translations').value];
    forms.splice(index, 1);

    const values = forms.map((x: FormGroup) => x.get('language').value);

    return this.languages.filter(x => {
      return (x.key !== this.language && values.indexOf(x.key) < 0);
    });
  }


  isLanguageSet(): boolean {
    const forms = this.homepageMessageForm.get('translations').value;
    let isValid = true;
    forms.forEach(((f: FormGroup) => {
      if (!f.get('language').valid) { isValid = false; }
    }));
    return isValid;
  }
}
