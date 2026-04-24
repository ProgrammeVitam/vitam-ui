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
import { AfterViewInit, Component, EventEmitter, Input, OnDestroy, OnInit, Output, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { Subject } from 'rxjs';
import { take, takeUntil } from 'rxjs/operators';
import { Customer, LanguageService, Option, StartupService } from 'vitamui-library';

@Component({
  selector: 'app-homepage-message',
  templateUrl: './homepage-message.component.html',
  styleUrls: ['./homepage-message.component.scss'],
  standalone: false,
})
export class HomepageMessageComponent implements OnInit, OnDestroy, AfterViewInit {
  dialogRef = inject<MatDialogRef<HomepageMessageComponent>>(MatDialogRef);
  private formBuilder = inject(FormBuilder);
  private startupService = inject(StartupService);
  private languageService = inject(LanguageService);

  @Input() homepageMessageForm: FormGroup;
  @Input() customer: Customer;

  @Output()
  public formToSend = new EventEmitter<{
    form: FormGroup;
    portalTitles: {
      [language: string]: string;
    };
    portalMessages: {
      [language: string]: string;
    };
  }>();

  public languages: Option[];
  public defaultForm: FormGroup;
  public customerForm: FormGroup;

  private language: string;
  private portalTitles: { [language: string]: string } = {};
  private portalMessages: { [language: string]: string } = {};
  private destroy = new Subject<void>();

  ngOnInit() {
    this.homepageMessageForm = this.formBuilder.group({
      id: null,
      portalTitle: ['', [Validators.required]],
      portalMessage: ['', [Validators.required, Validators.maxLength(500)]],
      translations: this.formBuilder.array([]),
    });
  }

  ngAfterViewInit() {
    this.languageService
      .getAvailableLanguagesOptions()
      .pipe(take(1))
      .subscribe((options: Option[]) => {
        this.languages = options;
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

  public setMessages(): void {
    let idCustomer = null;

    const title = this.startupService.getConfigStringValue('PORTAL_TITLE');
    const message = this.startupService.getConfigStringValue('PORTAL_MESSAGE');

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

    const defaultTitle = this.portalTitles && this.portalTitles[this.language] ? this.portalTitles[this.language] : title;
    const defaultMessage = this.portalMessages && this.portalMessages[this.language] ? this.portalMessages[this.language] : message;

    this.homepageMessageForm.get('id').patchValue(idCustomer);
    this.homepageMessageForm.get('portalTitle').patchValue(defaultTitle);
    this.homepageMessageForm.get('portalMessage').patchValue(defaultMessage);

    this.languages.forEach((l) => {
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

  private sendForm(): void {
    this.getTranslations();
    this.formToSend.emit({ form: this.homepageMessageForm, portalTitles: this.portalTitles, portalMessages: this.portalMessages });
  }

  private getTranslations(): void {
    const titles: { [language: string]: any } = {};
    const messages: { [language: string]: any } = {};

    titles[this.language] = this.homepageMessageForm.get('portalTitle').value;
    messages[this.language] = this.homepageMessageForm.get('portalMessage').value;

    const forms = this.homepageMessageForm.get('translations').value;

    forms.forEach((form: FormGroup) => {
      const language = form.get('language').value;
      const title = form.get('portalTitle').value;
      const message = form.get('portalMessage').value;

      titles[language] = title;
      messages[language] = message;
    });

    this.portalTitles = titles;
    this.portalMessages = messages;
  }

  public getLanguages(index: number): Option[] {
    const forms = [...this.homepageMessageForm.get('translations').value];
    forms.splice(index, 1);

    const values = forms.map((x: FormGroup) => x.get('language').value);
    return this.languages.filter((x) => {
      return x.key !== this.language && values.indexOf(x.key) < 0;
    });
  }

  public isLanguageSet(): boolean {
    const forms = this.homepageMessageForm.get('translations').value;
    let isValid = true;
    forms.forEach((f: FormGroup) => {
      if (!f.get('language').valid) {
        isValid = false;
      }
    });
    return isValid;
  }
}
