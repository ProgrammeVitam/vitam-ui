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
