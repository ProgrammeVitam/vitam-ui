/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { Component, OnDestroy, OnInit } from '@angular/core';
import { SafeResourceUrl, Title } from '@angular/platform-browser';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Application, ApplicationService, Category } from 'ui-frontend-common';
import {
  ApplicationId,
  AuthService,
  BasicCustomer,
  FullLangString,
  GlobalEventService,
  LanguageService,
  MinLangString,
  StartupService,
  ThemeDataType,
  ThemeService,
} from 'ui-frontend-common';

const APPLICATION_TRANSLATE_PATH = 'APPLICATION';

@Component({
  selector: 'app-portal',
  templateUrl: './portal.component.html',
  styleUrls: ['./portal.component.scss'],
})
export class PortalComponent implements OnInit, OnDestroy {
  public applications: Map<Category, Application[]>;
  public welcomeTitle: string;
  public welcomeMessage: string;
  public customerLogoUrl: SafeResourceUrl;
  public loading = true;

  private destroyer$ = new Subject();

  constructor(
    private translateService: TranslateService,
    private applicationService: ApplicationService,
    private startupService: StartupService,
    private authService: AuthService,
    private themeService: ThemeService,
    private langagueService: LanguageService,
    private titleService: Title,
    private globalEventService: GlobalEventService,
  ) {}

  ngOnInit() {
    this.applicationService
      .getActiveTenantAppsMap()
      .pipe(takeUntil(this.destroyer$))
      .subscribe((appMap) => {
        this.applications = appMap;
        this.loading = false;
      });

    this.translateService.onLangChange.pipe(takeUntil(this.destroyer$)).subscribe((event: LangChangeEvent) => {
      this.initPortalTitleAndMessage(this.langagueService.getFullLangString(event.lang as MinLangString));
    });

    this.customerLogoUrl = this.themeService.getData(this.authService.user, ThemeDataType.PORTAL_LOGO);
    this.globalEventService.pageEvent.next(ApplicationId.PORTAL_APP);
  }

  ngOnDestroy() {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  private initPortalTitleAndMessage(lang: FullLangString): void {
    const translatedAppName = this.translateService.instant(APPLICATION_TRANSLATE_PATH + '.' + ApplicationId.PORTAL_APP + '.NAME');
    this.titleService.setTitle(translatedAppName);

    const customer: BasicCustomer = this.authService.user.basicCustomer;
    if (customer) {
      if (customer.portalTitles && customer.portalTitles[lang]) {
        this.welcomeTitle = customer.portalTitles[lang];
      } else {
        this.welcomeTitle = this.startupService.getDefaultPortalTitle();
      }

      if (customer.portalMessages && customer.portalMessages[lang]) {
        this.welcomeMessage = customer.portalMessages[lang];
      } else {
        this.welcomeMessage = this.startupService.getDefaultPortalMessage();
      }
    }
  }
}
