import { Component, OnInit } from '@angular/core';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { AccountApiService } from '../../../account/account-api.service';
import { AuthService } from '../../../auth.service';
import { SessionStorageService } from '../../../services/session-storage.service';

@Component({
  selector: 'vitamui-common-select-language',
  templateUrl: './select-language.component.html',
  styleUrls: ['./select-language.component.scss']
})
export class SelectLanguageComponent implements OnInit {

  public currentLang = '';

  constructor(
    private translateService: TranslateService,
    private accountApiService: AccountApiService,
    private authService: AuthService,
    private sessionStorageService: SessionStorageService
  ) { }

  ngOnInit() {
    if (this.authService.user.language) {
      this.translateService.use(this.getInitLanguage(this.translateConverter(this.authService.user.language)));
      this.currentLang = this.translateService.currentLang;
    } else {
      this.currentLang = this.translateService.defaultLang;
    }
    this.translateService.onLangChange.subscribe((lang: LangChangeEvent) => this.currentLang = lang.lang);
  }

  public use(lang: string): void {
    this.accountApiService.patchMe({language: lang === 'fr' ? 'FRENCH' : 'ENGLISH'}).subscribe(() => {
      this.sessionStorageService.language = lang;
      this.translateService.use(lang);
    });
  }

  private getInitLanguage(language: string): string {
    return this.sessionStorageService.language ? this.sessionStorageService.language : language;
  }

  private translateConverter(lang: string): string {
    return (lang === 'FRENCH' || lang === 'fr') ? 'fr' : 'en';
  }
}
