import { DatePipe } from '@angular/common';
import { Component, Input, OnChanges, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Colors } from 'ui-frontend-common';
import { FacetDetails } from 'ui-frontend-common/app/modules/models/operation/facet-details.interface';
import { RuleFacets } from '../../../models/search.criteria';

@Component({
  selector: 'app-search-access-rules-facets',
  templateUrl: './search-access-rules-facets.component.html',
  styleUrls: ['./search-access-rules-facets.component.scss'],
})
export class SearchAccessRulesFacetsComponent implements OnInit, OnChanges {
  constructor(private translateService: TranslateService, private datePipe: DatePipe) {}

  @Input()
  accessRuleFacets: RuleFacets;

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
    if (this.accessRuleFacets) {
      this.handleWaitingToRecalculateRulesFacets();
      this.handleRulesDisctinctsFacets();
      this.handleRulesExpirationFacets();
    }
  }

  private handleRulesExpirationFacets() {
    let unexpiredRulesNb = 0;
    let expiredRulesNb = 0;
    this.dateFilterValue = this.datePipe.transform(new Date(), 'dd-MM-yyyy');

    if (this.accessRuleFacets.expiredRulesListFacets && this.accessRuleFacets.expiredRulesListFacets.length > 0) {
      this.accessRuleFacets.expiredRulesListFacets.forEach((elt) => {
        if (elt.node === 'UNEXPIRED') {
          unexpiredRulesNb = elt.count ? elt.count : 0;
        } else if (elt && elt.node && elt.node.length > 10) {
          this.dateFilterValue = ' ' + elt.node.substring(elt.node.length - 10, elt.node.length);
          expiredRulesNb = elt.count ? elt.count : 0;
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
    if (this.accessRuleFacets.rulesListFacets && this.accessRuleFacets.rulesListFacets.length > 0) {
      this.rulesFacetDetails.push({
        title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.ACCESS_RULE.DISTINCT_RULES'),
        totalResults: this.accessRuleFacets.rulesListFacets.length,
        clickable: false,
        color: Colors.GRAY,
        backgroundColor: Colors.DISABLED,
      });
    } else {
      this.rulesFacetDetails.push({
        title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.ACCESS_RULE.DISTINCT_RULES'),
        totalResults: 0,
        clickable: false,
        color: Colors.GRAY,
        backgroundColor: Colors.DISABLED,
      });
    }
  }

  private handleWaitingToRecalculateRulesFacets() {
    if (this.accessRuleFacets.waitingToRecalculateRulesListFacets && this.accessRuleFacets.waitingToRecalculateRulesListFacets.length > 0) {
      this.accessRuleFacets.waitingToRecalculateRulesListFacets
        .filter((elt) => elt.node === 'true')
        .forEach((elt) => {
          let calculatedCount = elt.count ? elt.count : 0;
          let notCalculatedCount = this.totalResults - calculatedCount;
          this.archiveUnitsCountFacetDetails.push({
            title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.ACCESS_RULE.WAITING_TO_RECALCULATE'),
            totalResults: notCalculatedCount,
            clickable: false,
            color: notCalculatedCount === 0 ? Colors.GRAY : Colors.ORANGE,
            backgroundColor: Colors.DISABLED,
          });
          this.archiveUnitsCountFacetDetails.push({
            title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.ACCESS_RULE.CALCULATED'),
            totalResults: calculatedCount,
            clickable: false,
            color: Colors.GRAY,
            backgroundColor: Colors.DISABLED,
          });
        });
    } else {
      this.archiveUnitsCountFacetDetails.push({
        title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.ACCESS_RULE.WAITING_TO_RECALCULATE'),
        totalResults: this.totalResults,
        clickable: false,
        color: this.totalResults === 0 ? Colors.GRAY : 'orange',
        backgroundColor: Colors.DISABLED,
      });
      this.archiveUnitsCountFacetDetails.push({
        title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.ACCESS_RULE.CALCULATED'),
        totalResults: 0,
        clickable: false,
        color: Colors.GRAY,
        backgroundColor: Colors.DISABLED,
      });
    }

    if (this.accessRuleFacets.noRulesFacets && this.accessRuleFacets.noRulesFacets.length > 0) {
      this.accessRuleFacets.noRulesFacets.forEach((elt) => {
        this.archiveUnitsCountFacetDetails.push({
          title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.ACCESS_RULE.WITHOUT_RULES'),
          totalResults: elt.count ? elt.count : 0,
          clickable: false,
          color: Colors.GRAY,
          backgroundColor: Colors.DISABLED,
        });
      });
    } else {
      this.archiveUnitsCountFacetDetails.push({
        title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.ACCESS_RULE.WITHOUT_RULES'),
        totalResults: 0,
        clickable: false,
        color: Colors.GRAY,
        backgroundColor: Colors.DISABLED,
      });
    }
  }
}
