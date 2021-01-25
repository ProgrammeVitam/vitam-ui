import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, RoutesRecognized } from '@angular/router';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AccountApiService } from '../../../account/account-api.service';
import { ApplicationId } from '../../../application-id.enum';
import { AuthService } from '../../../auth.service';
import { SessionStorageService } from '../../../services/session-storage.service';

@Component({
  selector: 'vitamui-common-select-language',
  templateUrl: './select-language.component.html',
  styleUrls: ['./select-language.component.scss']
})
export class SelectLanguageComponent implements OnInit, OnDestroy {

  public currentLang = '';
  public hasLangSelection = false;

  private destroyer$ = new Subject();

  constructor(
    private translateService: TranslateService,
    private accountApiService: AccountApiService,
    private authService: AuthService,
    private sessionStorageService: SessionStorageService,
    private router: Router
  ) { }

  ngOnInit() {
    if (this.authService.user && this.authService.user.language) {
      this.translateService.use(this.getInitLanguage(this.translateConverter(this.authService.user.language)));
      this.currentLang = this.translateService.currentLang;
    } else {
      this.currentLang = this.translateService.defaultLang;
    }

    this.translateService.onLangChange.pipe(takeUntil(this.destroyer$))
      .subscribe((lang: LangChangeEvent) => this.currentLang = lang.lang);

    this.router.events.pipe(takeUntil(this.destroyer$)).subscribe((data) => {
      if (data instanceof RoutesRecognized) {
        const appId = data.state.root.firstChild.data.appId;
        this.hasLangSelection = this.hasLanguageSelection(appId);
      }
    });
  }

  ngOnDestroy() {
    this.destroyer$.next();
    this.destroyer$.complete();
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

  private hasLanguageSelection(appId: string): boolean {
    if (this.authService.user.readonly) {
      return false;
    } else {
      switch (appId) {
        case ApplicationId.PORTAL_APP:
          return true;
        default:
          return false;
      }
    }
  }
}
