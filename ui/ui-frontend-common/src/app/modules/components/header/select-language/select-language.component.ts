import { Component, Input, OnDestroy, OnInit } from '@angular/core';
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

  /**
   * This component have two display mode :
   * select : displays a select box with the current selected lang as text.
   * button : displays a circle button with the current selected lang as an image.
   */
  @Input() displayMode: 'select' | 'button' = 'button';

  public hasLangSelection = false;
  public currentLang = '';

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
        case ApplicationId.STARTER_KIT_APP:
        case ApplicationId.CUSTOMERS_APP :
        case ApplicationId.USERS_APP :
        case ApplicationId.GROUPS_APP :
        case ApplicationId.PROFILES_APP :
        case ApplicationId.SUBROGATIONS_APP :
        case ApplicationId.ACCOUNTS_APP :
        case ApplicationId.HIERARCHY_PROFILE_APP :
        case ApplicationId.INGEST_APP :
        case ApplicationId.ARCHIVE_SEARCH_APP :
        case ApplicationId.RULES_APP :
        case ApplicationId.HOLDING_FILLING_SCHEME_APP :
        case ApplicationId.LOGBOOK_OPERATION_APP :
        case ApplicationId.PROBATIVE_VALUE_APP :
        case ApplicationId.DSL_APP :
        case ApplicationId.SECURE_APP :
        case ApplicationId.AUDIT_APP :
        case ApplicationId.ONTOLOGY_APP :
        case ApplicationId.SECURITY_PROFILES_APP :
        case ApplicationId.CONTEXTS_APP :
        case ApplicationId.FILE_FORMATS_APP :
        case ApplicationId.AGENCIES_APP :
        case ApplicationId.ACCESS_APP :
        case ApplicationId.INGEST_APP_REF :
          return true;
        default:
          return false;
      }
    }
  }
}
