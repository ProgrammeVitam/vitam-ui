import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { LangChangeEvent, TranslateModule, TranslateService } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../../auth.service';
import { FullLangString, LanguageService, MinLangString } from '../../../language.service';
import { BaseUserInfoApiService } from './../../../api/base-user-info-api.service';
import { ItemSelectComponent } from '../item-select/item-select.component';
import { MatLegacyMenuModule } from '@angular/material/legacy-menu';
import { MatLegacyButtonModule } from '@angular/material/legacy-button';
import { NgIf } from '@angular/common';

@Component({
  selector: 'vitamui-common-select-language',
  templateUrl: './select-language.component.html',
  styleUrls: ['./select-language.component.scss'],
  standalone: true,
  imports: [NgIf, MatLegacyButtonModule, MatLegacyMenuModule, ItemSelectComponent, TranslateModule],
})
export class SelectLanguageComponent implements OnInit, OnDestroy {
  /**
   * This component have two display mode :
   * select : displays a select box with the current selected lang as text.
   * button : displays a circle button with the current selected lang as an image.
   */
  @Input() displayMode: 'select' | 'button' = 'button';

  public currentLang = '';
  public minLangString = MinLangString;

  private destroyer$ = new Subject<void>();

  constructor(
    private translateService: TranslateService,
    private languageService: LanguageService,
    private userInfoApiService: BaseUserInfoApiService,
    private authService: AuthService,
  ) {}

  ngOnInit() {
    this.authService.getUserInfo$().subscribe((userInfo) => {
      this.currentLang = this.languageService.getShortLangString(userInfo.language as FullLangString);
      this.translateService.use(this.currentLang);
    });

    this.translateService.onLangChange.pipe(takeUntil(this.destroyer$)).subscribe((langEvent: LangChangeEvent) => {
      this.currentLang = langEvent.lang;
    });
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
}
