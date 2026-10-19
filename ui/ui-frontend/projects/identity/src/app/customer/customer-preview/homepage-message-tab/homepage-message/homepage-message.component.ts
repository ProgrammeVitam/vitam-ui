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
import { AfterViewInit, Component, inject, input, output, signal, effect, untracked } from '@angular/core';
import { applyEach, FieldTree, form, FormField, maxLength, required } from '@angular/forms/signals';
import { MatDialogContent, MatDialogRef } from '@angular/material/dialog';
import { take } from 'rxjs/operators';
import { Customer, DialogHeaderComponent, InputComponent, LanguageService, Option, StartupService } from 'vitamui-library';
import { HomepageMessageTranslationComponent } from './homepage-message-translation/homepage-message-translation';
import { TranslatePipe } from '@ngx-translate/core';

export interface HomepageMessageTranslationModel {
  language: string;
  portalTitle: string;
  portalMessage: string;
}

export interface HomepageMessageModel {
  id: string;
  portalTitle: string;
  portalMessage: string;
  translations: HomepageMessageTranslationModel[];
}

export interface HomepageMessageFormEvent {
  form: FieldTree<HomepageMessageModel>;
  portalTitles: {
    [language: string]: string;
  };
  portalMessages: {
    [language: string]: string;
  };
}

@Component({
  selector: 'app-homepage-message',
  templateUrl: './homepage-message.component.html',
  styleUrls: ['./homepage-message.component.scss'],
  imports: [DialogHeaderComponent, MatDialogContent, FormField, InputComponent, HomepageMessageTranslationComponent, TranslatePipe],
})
export class HomepageMessageComponent implements AfterViewInit {
  dialogRef = inject<MatDialogRef<HomepageMessageComponent>>(MatDialogRef);
  private readonly startupService = inject(StartupService);
  private readonly languageService = inject(LanguageService);

  private readonly model = signal<HomepageMessageModel>({ id: null, portalTitle: '', portalMessage: '', translations: [] });
  readonly homepageMessageForm = form(this.model, (path) => {
    required(path.portalTitle);
    required(path.portalMessage);
    maxLength(path.portalMessage, 500);
    applyEach(path.translations, (translation) => {
      required(translation.language);
      required(translation.portalTitle);
      required(translation.portalMessage);
      maxLength(translation.portalMessage, 500);
    });
  });
  readonly customer = input<Customer>(undefined);

  public readonly formToSend = output<HomepageMessageFormEvent>();

  public languages: Option[];

  private language: string;
  private portalTitles: { [language: string]: string } = {};
  private portalMessages: { [language: string]: string } = {};
  // The form is sent to the parent on each change once the languages are loaded
  private readonly ready = signal(false);

  constructor() {
    effect(() => {
      if (this.ready()) {
        this.model();
        untracked(() => this.sendForm());
      }
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
        this.ready.set(true);
      });
  }

  public setMessages(): void {
    let idCustomer = null;

    const title = this.startupService.getConfigStringValue('PORTAL_TITLE');
    const message = this.startupService.getConfigStringValue('PORTAL_MESSAGE');

    const customer = this.customer();
    if (customer) {
      if (customer.id) {
        idCustomer = customer.id;
      }
      if (customer.language) {
        this.language = customer.language;
      }
      if (customer.portalMessages) {
        this.portalMessages = customer.portalMessages;
      }
      if (customer.portalTitles) {
        this.portalTitles = customer.portalTitles;
      }
    }

    const defaultTitle = this.portalTitles && this.portalTitles[this.language] ? this.portalTitles[this.language] : title;
    const defaultMessage = this.portalMessages && this.portalMessages[this.language] ? this.portalMessages[this.language] : message;

    const translations: HomepageMessageTranslationModel[] = this.languages
      .filter((l) => this.portalTitles[l.key] && this.portalMessages[l.key] && this.language !== l.key)
      .map((l) => ({ language: l.key, portalTitle: this.portalTitles[l.key], portalMessage: this.portalMessages[l.key] }));

    this.model.update((homepageMessage) => ({
      ...homepageMessage,
      id: idCustomer,
      portalTitle: defaultTitle,
      portalMessage: defaultMessage,
      translations: [...homepageMessage.translations, ...translations],
    }));
  }

  public onAdd(): void {
    this.model.update((homepageMessage) => ({
      ...homepageMessage,
      translations: [...homepageMessage.translations, { language: '', portalTitle: '', portalMessage: '' }],
    }));
  }

  public remove(index: number): void {
    this.model.update((homepageMessage) => ({
      ...homepageMessage,
      translations: homepageMessage.translations.filter((_, i) => i !== index),
    }));
  }

  private sendForm(): void {
    this.getTranslations();
    this.formToSend.emit({ form: this.homepageMessageForm, portalTitles: this.portalTitles, portalMessages: this.portalMessages });
  }

  private getTranslations(): void {
    const titles: { [language: string]: any } = {};
    const messages: { [language: string]: any } = {};
    const homepageMessage = this.model();

    titles[this.language] = homepageMessage.portalTitle;
    messages[this.language] = homepageMessage.portalMessage;

    homepageMessage.translations.forEach((translation) => {
      titles[translation.language] = translation.portalTitle;
      messages[translation.language] = translation.portalMessage;
    });

    this.portalTitles = titles;
    this.portalMessages = messages;
  }

  public getLanguages(index: number): Option[] {
    const values = this.model()
      .translations.filter((_, i) => i !== index)
      .map((translation) => translation.language);
    return this.languages.filter((x) => {
      return x.key !== this.language && values.indexOf(x.key) < 0;
    });
  }

  public isLanguageSet(): boolean {
    return this.model().translations.every((_, index) => this.homepageMessageForm.translations[index].language().valid());
  }
}
