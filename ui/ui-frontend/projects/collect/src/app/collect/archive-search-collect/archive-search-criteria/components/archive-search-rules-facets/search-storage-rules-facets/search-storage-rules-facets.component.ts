/*Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
and the signatories of the "VITAM - Accord du Contributeur" agreement.

contact@programmevitam.fr

This software is a computer program whose purpose is to implement
implement a digital archiving front-office system for the secure and
efficient high volumetry VITAM solution.

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/

import { DatePipe } from '@angular/common';
import { Component, Input, OnChanges } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Colors, FacetDetails, RuleFacets } from 'vitamui-library';
import { ArchiveSearchConstsEnum } from '../../../models/archive-search-consts-enum';
import { ArchiveFacetsService } from '../../../services/archive-facets.service';

@Component({
  selector: 'app-search-storage-rules-facets',
  templateUrl: './search-storage-rules-facets.component.html',
  styleUrls: ['./search-storage-rules-facets.component.scss'],
})
export class SearchStorageRulesFacetsComponent implements OnChanges {
  constructor(
    private facetsService: ArchiveFacetsService,
    private translateService: TranslateService,
    private datePipe: DatePipe,
  ) {}

  @Input()
  storageRuleFacets: RuleFacets;

  @Input()
  totalResults: number;

  @Input()
  exactCount: boolean;

  dateFilterValue: string;
  rulesFacetDetails: FacetDetails[] = [];
  expiredRulesFacetDetails: FacetDetails[] = [];
  finalActionFacetDetails: FacetDetails[] = [];
  archiveUnitsCountFacetDetails: FacetDetails[] = [];
  finalActionsFacetsValues: Map<string, number>;

  ngOnChanges(): void {
    this.finalActionsFacetsValues = new Map();
    this.rulesFacetDetails = [];
    this.expiredRulesFacetDetails = [];
    this.finalActionFacetDetails = [];
    this.archiveUnitsCountFacetDetails = [];
    let archiveUnitWithRules = 0;
    if (this.storageRuleFacets) {
      archiveUnitWithRules = this.handleWaitingToRecalculateRulesFacets();
      this.handleFinalActionsRulesFacets();
      this.handleRulesDisctinctsFacets();
      this.handleRulesExpirationFacets(archiveUnitWithRules);
    }
  }

  private handleRulesExpirationFacets(archiveUnitWithRules: number) {
    let expiredRulesNb = 0;
    this.dateFilterValue = this.datePipe.transform(new Date(), 'dd-MM-yyyy');

    if (this.storageRuleFacets.expiredRulesListFacets && this.storageRuleFacets.expiredRulesListFacets.length > 0) {
      this.storageRuleFacets.expiredRulesListFacets.forEach((elt) => {
        if (elt && elt.node && elt.node.length > 10) {
          this.dateFilterValue = ' ' + elt.node.substring(elt.node.length - 10, elt.node.length);
          expiredRulesNb = elt.count ? elt.count : 0;
        }
      });
    }

    this.expiredRulesFacetDetails.push({
      title: this.translateService.instant('COLLECT.FACETS.STORAGE_RULE.EXPIRED'),
      totalResults: this.facetsService.getFacetTextByExactCountFlag(expiredRulesNb, this.exactCount, this.totalResults),
      clickable: false,
      color: Colors.GRAY,
      filter: '>' + this.dateFilterValue,
      backgroundColor: Colors.DISABLED,
    });

    this.expiredRulesFacetDetails.push({
      title: this.translateService.instant('COLLECT.FACETS.STORAGE_RULE.NOT_EXPIRED'),
      totalResults: this.facetsService.getFacetTextByExactCountFlag(
        archiveUnitWithRules - expiredRulesNb,
        this.exactCount,
        this.totalResults,
      ),
      clickable: false,
      color: Colors.GRAY,
      filter: '<' + this.dateFilterValue,
      backgroundColor: Colors.DISABLED,
    });
  }

  private handleRulesDisctinctsFacets() {
    if (this.storageRuleFacets.rulesListFacets && this.storageRuleFacets.rulesListFacets.length > 0) {
      this.rulesFacetDetails.push({
        title: this.translateService.instant('COLLECT.FACETS.STORAGE_RULE.DISTINCT_RULES'),
        totalResults: this.facetsService.getFacetTextByExactCountFlag(
          this.storageRuleFacets.rulesListFacets.length,
          this.exactCount,
          this.totalResults,
        ),
        clickable: false,
        color: Colors.GRAY,
        backgroundColor: Colors.DISABLED,
      });
    } else {
      this.rulesFacetDetails.push({
        title: this.translateService.instant('COLLECT.FACETS.STORAGE_RULE.DISTINCT_RULES'),
        totalResults: this.facetsService.getFacetTextByExactCountFlag(0, this.exactCount, this.totalResults),
        clickable: false,
        color: Colors.GRAY,
        backgroundColor: Colors.DISABLED,
      });
    }
  }

  private handleFinalActionsRulesFacets() {
    this.finalActionsFacetsValues.set('COPY', 0);
    this.finalActionsFacetsValues.set('TRANSFER', 0);
    this.finalActionsFacetsValues.set('RESTRICTACCESS', 0);

    if (this.storageRuleFacets.finalActionsFacets) {
      this.storageRuleFacets.finalActionsFacets.forEach((elt) => {
        this.finalActionsFacetsValues.set(elt.node.toUpperCase(), elt.count ? elt.count : 0);
      });
    }

    this.finalActionFacetDetails.push({
      title: this.translateService.instant('COLLECT.FACETS.STORAGE_RULE.COPY'),
      totalResults: this.facetsService.getFacetTextByExactCountFlag(
        this.finalActionsFacetsValues.get('COPY'),
        this.exactCount,
        this.totalResults,
      ),
      clickable: false,
      color: Colors.GRAY,
      backgroundColor: Colors.DISABLED,
    });

    this.finalActionFacetDetails.push({
      title: this.translateService.instant('COLLECT.FACETS.STORAGE_RULE.TRANSFER'),
      totalResults: this.facetsService.getFacetTextByExactCountFlag(
        this.finalActionsFacetsValues.get('TRANSFER'),
        this.exactCount,
        this.totalResults,
      ),
      clickable: false,
      color: Colors.GRAY,
      backgroundColor: Colors.DISABLED,
    });

    this.finalActionFacetDetails.push({
      title: this.translateService.instant('COLLECT.FACETS.STORAGE_RULE.RESTRICT_ACCESS'),
      totalResults: this.facetsService.getFacetTextByExactCountFlag(
        this.finalActionsFacetsValues.get('RESTRICTACCESS'),
        this.exactCount,
        this.totalResults,
      ),
      clickable: false,
      color: Colors.GRAY,
      backgroundColor: Colors.DISABLED,
    });
  }

  private handleWaitingToRecalculateRulesFacets(): number {
    let archiveUnitWithRules = 0;
    if (
      this.storageRuleFacets.waitingToRecalculateRulesListFacets &&
      this.storageRuleFacets.waitingToRecalculateRulesListFacets.length > 0
    ) {
      const facetComputedUnits = this.storageRuleFacets.waitingToRecalculateRulesListFacets.filter((elt) => elt.node === 'true');
      let computedCount = 0;

      if (facetComputedUnits.length > 0) {
        computedCount = facetComputedUnits[0].count ? facetComputedUnits[0].count : 0;
      } else {
        computedCount = 0;
      }
      let notComputedCount = '';
      if (
        !this.exactCount &&
        (this.totalResults >= ArchiveSearchConstsEnum.RESULTS_MAX_NUMBER || computedCount >= ArchiveSearchConstsEnum.RESULTS_MAX_NUMBER)
      ) {
        notComputedCount = ArchiveSearchConstsEnum.BIG_RESULTS_FACETS_DEFAULT_TEXT;
      } else {
        notComputedCount = (this.totalResults - computedCount).toString();
      }

      this.archiveUnitsCountFacetDetails.push({
        title: this.translateService.instant('COLLECT.FACETS.STORAGE_RULE.WAITING_TO_RECALCULATE'),
        totalResults: notComputedCount,
        clickable: false,
        color: notComputedCount === '0' ? Colors.GRAY : Colors.ORANGE,
        backgroundColor: Colors.DISABLED,
      });
      this.archiveUnitsCountFacetDetails.push({
        title: this.translateService.instant('COLLECT.FACETS.STORAGE_RULE.CALCULATED'),
        totalResults: this.facetsService.getFacetTextByExactCountFlag(computedCount, this.exactCount, this.totalResults),
        clickable: false,
        color: Colors.GRAY,
        backgroundColor: Colors.DISABLED,
      });
      archiveUnitWithRules = computedCount;
    } else {
      this.archiveUnitsCountFacetDetails.push({
        title: this.translateService.instant('COLLECT.FACETS.STORAGE_RULE.WAITING_TO_RECALCULATE'),
        totalResults: this.facetsService.getFacetTextByExactCountFlag(this.totalResults, this.exactCount, this.totalResults),
        clickable: false,
        color: this.totalResults === 0 ? Colors.GRAY : Colors.ORANGE,
        backgroundColor: Colors.DISABLED,
      });
      this.archiveUnitsCountFacetDetails.push({
        title: this.translateService.instant('COLLECT.FACETS.STORAGE_RULE.CALCULATED'),
        totalResults: this.facetsService.getFacetTextByExactCountFlag(0, this.exactCount, this.totalResults),
        clickable: false,
        color: Colors.GRAY,
        backgroundColor: Colors.DISABLED,
      });
    }

    if (this.storageRuleFacets.noRulesFacets && this.storageRuleFacets.noRulesFacets.length > 0) {
      this.storageRuleFacets.noRulesFacets.forEach((elt) => {
        this.archiveUnitsCountFacetDetails.push({
          title: this.translateService.instant('COLLECT.FACETS.STORAGE_RULE.WITHOUT_RULES'),
          totalResults: this.facetsService.getFacetTextByExactCountFlag(elt.count, this.exactCount, this.totalResults),
          clickable: false,
          color: Colors.GRAY,
          backgroundColor: Colors.DISABLED,
        });
        archiveUnitWithRules -= elt.count ? elt.count : 0;
      });
    } else {
      this.archiveUnitsCountFacetDetails.push({
        title: this.translateService.instant('COLLECT.FACETS.STORAGE_RULE.WITHOUT_RULES'),
        totalResults: this.facetsService.getFacetTextByExactCountFlag(0, this.exactCount, this.totalResults),
        clickable: false,
        color: Colors.GRAY,
        backgroundColor: Colors.DISABLED,
      });
    }
    return archiveUnitWithRules;
  }
}
