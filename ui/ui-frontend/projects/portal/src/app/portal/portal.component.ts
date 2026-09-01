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
import { Component, OnDestroy, OnInit, computed, effect, inject, signal } from '@angular/core';
import { SafeResourceUrl, Title } from '@angular/platform-browser';
import { TranslateService } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import {
  Application,
  ApplicationId,
  ApplicationService,
  AuthService,
  Category,
  GlobalEventService,
  LanguageService,
  MinLangString,
  StartupService,
  ThemeDataType,
  ThemeService,
} from 'vitamui-library';
import { ContentTypeEnum } from '../components/content-list/content.enum';
import { Content } from '../components/content-list/content.interface';

import { MatMenuModule } from '@angular/material/menu';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { WelcomeMessageComponent } from '../components/welcome-message/welcome-message.component';
import { ContentListComponent } from '../components/content-list/content-list.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

const APPLICATION_TRANSLATE_PATH = 'APPLICATION';

@Component({
  selector: 'app-portal',
  templateUrl: './portal.component.html',
  styleUrls: ['./portal.component.scss'],
  imports: [MatMenuModule, ReactiveFormsModule, RouterModule, WelcomeMessageComponent, ContentListComponent, MatProgressSpinnerModule],
})
export class PortalComponent implements OnInit, OnDestroy {
  private translateService = inject(TranslateService);
  private applicationService = inject(ApplicationService);
  private startupService = inject(StartupService);
  private authService = inject(AuthService);
  private themeService = inject(ThemeService);
  private languageService = inject(LanguageService);
  private titleService = inject(Title);
  private globalEventService = inject(GlobalEventService);

  public content = signal<Map<Category, Content>>(new Map());
  public portalLogoUrl = signal<SafeResourceUrl>(null);
  public loading = signal(true);

  private currentMinLang = computed(() => (this.translateService.currentLang() as MinLangString) ?? MinLangString.FR);
  private currentFullLang = computed(() => this.languageService.getFullLangString(this.currentMinLang()));

  public welcomeTitle = computed(() => {
    const fullLang = this.currentFullLang();
    const customer = this.authService.user?.basicCustomer;
    if (customer?.portalTitles?.[fullLang]) return customer.portalTitles[fullLang];
    return this.startupService.getConfigStringValue('PORTAL_TITLE') ?? '';
  });

  public welcomeMessage = computed(() => {
    const fullLang = this.currentFullLang();
    const customer = this.authService.user?.basicCustomer;
    if (customer?.portalMessages?.[fullLang]) return customer.portalMessages[fullLang];
    return this.startupService.getConfigStringValue('PORTAL_MESSAGE') ?? '';
  });

  private destroyer$ = new Subject<void>();

  constructor() {
    effect(() => {
      const translatedAppName = this.translateService.instant(APPLICATION_TRANSLATE_PATH + '.' + ApplicationId.PORTAL_APP + '.NAME');
      this.titleService.setTitle(translatedAppName);
    });
  }

  ngOnInit() {
    this.applicationService
      .getActiveTenantAppsMap()
      .pipe(takeUntil(this.destroyer$))
      .subscribe((appMap) => {
        this.content.set(this.convertAppMapToContentMap(appMap));
        this.loading.set(false);
      });

    this.themeService
      .getData$(this.authService.user, ThemeDataType.PORTAL_LOGO)
      .subscribe((portalLogoUrl: SafeResourceUrl) => this.portalLogoUrl.set(portalLogoUrl));

    this.globalEventService.pageEvent.next(ApplicationId.PORTAL_APP);
  }

  ngOnDestroy() {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  private convertAppMapToContentMap(appMap: Map<Category, Application[]>): Map<Category, Content> {
    const contentMap: Map<Category, Content> = new Map();

    // Set applications
    for (const [category, apps] of appMap.entries()) {
      const content: Content = { type: ContentTypeEnum.APPLICATION, data: apps };
      contentMap.set(category, content);
    }

    return contentMap;
  }
}
