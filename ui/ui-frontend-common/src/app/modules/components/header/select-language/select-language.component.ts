import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AccountApiService } from '../../../account/account-api.service';
import { AuthService } from '../../../auth.service';
import { FullLangString, LanguageService, MinLangString } from '../../../language.service';
import { SessionStorageService } from '../../../services';

@Component({
  selector: 'vitamui-common-select-language',
  templateUrl: './select-language.component.html',
  styleUrls: ['./select-language.component.scss']
})
export class SelectLanguageComponent implements OnInit, OnDestroy {

  /**
   * This component have two display mode :
   * select : displays a select box with the current selected lang as text.
   * button : displays a circle button with the current selected lang as an image.
   */
  @Input() displayMode: 'select' | 'button' = 'button';
  @Input() hasLangSelection;
  @Input() disabled = false;

  public currentLang = '';
  public minLangString = MinLangString;

  private destroyer$ = new Subject();

  constructor(
    private translateService: TranslateService,
    private accountApiService: AccountApiService,
    private authService: AuthService,
    private languageService: LanguageService,
    private sessionStorageService: SessionStorageService
  ) { }

  ngOnInit() {
    if (this.authService.user && this.authService.user.language) {
      this.translateService.use(this.getInitLanguage(this.translateConverter(this.authService.user.language as FullLangString)));
      this.currentLang = this.translateService.currentLang;
    } else {
      this.currentLang = this.translateService.defaultLang;
    }

    this.translateService.onLangChange
      .pipe(takeUntil(this.destroyer$))
      .subscribe((lang: LangChangeEvent) => (this.currentLang = lang.lang));
  }

  ngOnDestroy() {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  public use(lang: MinLangString): void {
    this.accountApiService.patchMe({language: this.languageService.getFullLangString(lang)}).subscribe(() => {
      this.sessionStorageService.language = lang;
      this.translateService.use(lang);
    });
  }

  private getInitLanguage(language: string): string {
    return this.sessionStorageService.language ? this.sessionStorageService.language : language;
  }

  private translateConverter(lang: FullLangString): string {
    return this.languageService.getShortLangString(lang);
  }

}
