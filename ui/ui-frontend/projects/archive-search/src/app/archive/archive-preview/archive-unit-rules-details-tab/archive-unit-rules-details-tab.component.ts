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

import { animate, AUTO_STYLE, state, style, transition, trigger } from '@angular/animations';
import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { CriteriaDataType, CriteriaOperator, SearchCriteriaEltDto, SearchCriteriaTypeEnum, Unit } from 'ui-frontend-common';
import { ArchiveService } from '../../archive.service';

const PAGE_SIZE = 10;
const CURRENT_PAGE = 0;

@Component({
  selector: 'app-archive-unit-rules-details-tab',
  templateUrl: './archive-unit-rules-details-tab.component.html',
  styleUrls: ['./archive-unit-rules-details-tab.component.css'],
  animations: [
    trigger('collapse', [
      state('false', style({ height: AUTO_STYLE, visibility: AUTO_STYLE })),
      state('true', style({ height: '0', visibility: 'hidden' })),
      transition('false => true', animate(300 + 'ms ease-in')),
      transition('true => false', animate(300 + 'ms ease-out')),
    ]),
  ],
})
export class ArchiveUnitRulesDetailsTabComponent implements OnInit, OnChanges, OnDestroy {
  @Input() archiveUnit: Unit;
  archiveUnitRules: Unit;
  selectUnitWithInheritedRulesSubscription: Subscription;

  listOfCriteriaSearch: SearchCriteriaEltDto[] = [];

  constructor(
    private archiveSearchService: ArchiveService,
    private translateService: TranslateService,
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.archiveUnit) {
      this.selectUnitWithInheritedRules(changes.archiveUnit.currentValue);
    }
  }

  ngOnInit(): void {}

  ngOnDestroy() {
    this.selectUnitWithInheritedRulesSubscription?.unsubscribe();
  }

  selectUnitWithInheritedRules(archiveUnit: Unit) {
    this.listOfCriteriaSearch.push({
      criteria: 'GUID',
      values: [{ value: archiveUnit['#id'], id: archiveUnit['#id'] }],
      operator: CriteriaOperator.EQ,
      category: SearchCriteriaTypeEnum.FIELDS,
      dataType: CriteriaDataType.STRING,
    });
    const inheritedRulesCriteriaSearch = {
      criteriaList: this.listOfCriteriaSearch,
      pageNumber: CURRENT_PAGE,
      size: PAGE_SIZE,
      language: this.translateService.currentLang,
    };
    this.selectUnitWithInheritedRulesSubscription = this.archiveSearchService
      .selectUnitWithInheritedRules(inheritedRulesCriteriaSearch)
      .subscribe((response) => {
        this.archiveUnitRules = response;
        this.listOfCriteriaSearch = [];
      });
  }
}
