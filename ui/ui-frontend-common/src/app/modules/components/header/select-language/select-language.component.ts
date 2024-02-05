import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../../auth.service';
import { FullLangString, LanguageService, MinLangString } from '../../../language.service';
import { BaseUserInfoApiService } from './../../../api/base-user-info-api.service';

@Component({
  selector: 'vitamui-common-select-language',
  templateUrl: './select-language.component.html',
  styleUrls: ['./select-language.component.scss'],
})
export class SelectLanguageComponent implements OnInit, OnDestroy {
  /**
   * This component have two display mode :
   * select : displays a select box with the current selected lang as text.
   * button : displays a circle button with the current selected lang as an image.
   */
  @Input() displayMode: 'select' | 'button' = 'button';
  @Input() hasLangSelection;

  public currentLang = '';
  public minLangString = MinLangString;

  private destroyer$ = new Subject();

  constructor(
    private translateService: TranslateService,
    private authService: AuthService,
    private languageService: LanguageService,
    private userInfoApiService: BaseUserInfoApiService,
  ) {}

  ngOnInit() {
    if (this.authService.user && this.authService.user.userInfoId) {
      this.userInfoApiService.getMyUserInfo().subscribe((result) => {
        this.translateService.use(this.translateConverter(result.language as FullLangString));
        this.currentLang = this.translateService.currentLang;
      });
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
    this.userInfoApiService.patchMyUserInfo({ language: this.languageService.getFullLangString(lang) }).subscribe(() => {
      this.translateService.use(lang);
    });
  }

  private translateConverter(lang: FullLangString): string {
    return this.languageService.getShortLangString(lang);
  }
}
