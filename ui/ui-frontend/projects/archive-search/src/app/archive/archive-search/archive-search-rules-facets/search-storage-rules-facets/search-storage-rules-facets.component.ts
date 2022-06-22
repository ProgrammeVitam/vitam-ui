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

import { DatePipe } from '@angular/common';
import { Component, Input, OnChanges, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Colors } from 'ui-frontend-common';
import { FacetDetails } from 'ui-frontend-common/app/modules/models/operation/facet-details.interface';
import { RuleFacets } from '../../../models/search.criteria';

@Component({
  selector: 'app-search-storage-rules-facets',
  templateUrl: './search-storage-rules-facets.component.html',
  styleUrls: ['./search-storage-rules-facets.component.scss'],
})
export class SearchStorageRulesFacetsComponent implements OnInit, OnChanges {
  constructor(private translateService: TranslateService, private datePipe: DatePipe) {}

  @Input()
  storageRuleFacets: RuleFacets;

  @Input()
  totalResults: number;

  dateFilterValue: string;
  rulesFacetDetails: FacetDetails[] = [];
  expiredRulesFacetDetails: FacetDetails[] = [];
  finalActionFacetDetails: FacetDetails[] = [];
  archiveUnitsCountFacetDetails: FacetDetails[] = [];
  finalActionsFacetsValues: Map<string, number>;

  ngOnInit(): void {}
  ngOnChanges(): void {
    this.finalActionsFacetsValues = new Map();
    this.rulesFacetDetails = [];
    this.expiredRulesFacetDetails = [];
    this.finalActionFacetDetails = [];
    this.archiveUnitsCountFacetDetails = [];
    if (this.storageRuleFacets) {
      this.handleWaitingToRecalculateRulesFacets();
      this.handleFinalActionsRulesFacets();
      this.handleRulesDisctinctsFacets();
      this.handleRulesExpirationFacets();
    }
  }

  private handleRulesExpirationFacets() {
    let unexpiredRulesNb = 0;
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

    if (this.storageRuleFacets.unexpiredRulesListFacets && this.storageRuleFacets.unexpiredRulesListFacets.length > 0) {
      this.storageRuleFacets.unexpiredRulesListFacets.forEach((elt) => {
        if (elt && elt.node && elt.node.length > 10) {
          unexpiredRulesNb = elt.count ? elt.count : 0;
        }
      });
    }

    this.expiredRulesFacetDetails.push({
      title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.ACCESS_RULE.EXPIRED'),
      totalResults: expiredRulesNb,
      clickable: false,
      color: Colors.GRAY,
      filter: '>' + this.dateFilterValue,
      backgroundColor: Colors.DISABLED,
    });

    this.expiredRulesFacetDetails.push({
      title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.ACCESS_RULE.NOT_EXPIRED'),
      totalResults: unexpiredRulesNb,
      clickable: false,
      color: Colors.GRAY,
      filter: '<' + this.dateFilterValue,
      backgroundColor: Colors.DISABLED,
    });
  }

  private handleRulesDisctinctsFacets() {
    if (this.storageRuleFacets.rulesListFacets && this.storageRuleFacets.rulesListFacets.length > 0) {
      this.rulesFacetDetails.push({
        title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.STORAGE_RULE.DISTINCT_RULES'),
        totalResults: this.storageRuleFacets.rulesListFacets.length,
        clickable: false,
        color: Colors.GRAY,
        backgroundColor: Colors.DISABLED,
      });
    } else {
      this.rulesFacetDetails.push({
        title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.STORAGE_RULE.DISTINCT_RULES'),
        totalResults: 0,
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
      title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.STORAGE_RULE.COPY'),
      totalResults: this.finalActionsFacetsValues.get('COPY'),
      clickable: false,
      color: Colors.GRAY,
      backgroundColor: Colors.DISABLED,
    });

    this.finalActionFacetDetails.push({
      title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.STORAGE_RULE.TRANSFER'),
      totalResults: this.finalActionsFacetsValues.get('TRANSFER'),
      clickable: false,
      color: Colors.GRAY,
      backgroundColor: Colors.DISABLED,
    });

    this.finalActionFacetDetails.push({
      title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.STORAGE_RULE.RESTRICT_ACCESS'),
      totalResults: this.finalActionsFacetsValues.get('RESTRICTACCESS'),
      clickable: false,
      color: Colors.GRAY,
      backgroundColor: Colors.DISABLED,
    });
  }

  private handleWaitingToRecalculateRulesFacets() {
    if (
      this.storageRuleFacets.waitingToRecalculateRulesListFacets &&
      this.storageRuleFacets.waitingToRecalculateRulesListFacets.length > 0
    ) {
      this.storageRuleFacets.waitingToRecalculateRulesListFacets
        .filter((elt) => elt.node === 'true')
        .forEach((elt) => {
          let calculatedCount = elt.count ? elt.count : 0;
          let notCalculatedCount = this.totalResults - calculatedCount;
          this.archiveUnitsCountFacetDetails.push({
            title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.STORAGE_RULE.WAITING_TO_RECALCULATE'),
            totalResults: notCalculatedCount,
            clickable: false,
            color: notCalculatedCount === 0 ? Colors.GRAY : 'orange',
            backgroundColor: Colors.DISABLED,
          });
          this.archiveUnitsCountFacetDetails.push({
            title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.STORAGE_RULE.CALCULATED'),
            totalResults: calculatedCount,
            clickable: false,
            color: Colors.GRAY,
            backgroundColor: Colors.DISABLED,
          });
        });
    } else {
      this.archiveUnitsCountFacetDetails.push({
        title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.STORAGE_RULE.WAITING_TO_RECALCULATE'),
        totalResults: this.totalResults,
        clickable: false,
        color: this.totalResults === 0 ? Colors.GRAY : Colors.ORANGE,
        backgroundColor: Colors.DISABLED,
      });
      this.archiveUnitsCountFacetDetails.push({
        title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.STORAGE_RULE.CALCULATED'),
        totalResults: 0,
        clickable: false,
        color: Colors.GRAY,
        backgroundColor: Colors.DISABLED,
      });
    }

    if (this.storageRuleFacets.noRulesFacets && this.storageRuleFacets.noRulesFacets.length > 0) {
      this.storageRuleFacets.noRulesFacets.forEach((elt) => {
        this.archiveUnitsCountFacetDetails.push({
          title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.STORAGE_RULE.WITHOUT_RULES'),
          totalResults: elt.count ? elt.count : 0,
          clickable: false,
          color: Colors.GRAY,
          backgroundColor: Colors.DISABLED,
        });
      });
    } else {
      this.archiveUnitsCountFacetDetails.push({
        title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.STORAGE_RULE.WITHOUT_RULES'),
        totalResults: 0,
        clickable: false,
        color: Colors.GRAY,
        backgroundColor: Colors.DISABLED,
      });
    }
  }
}
