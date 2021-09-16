import { Component, Input, OnChanges, OnInit } from '@angular/core';
import { FacetDetails } from 'ui-frontend-common/app/modules/models/operation/facet-details.interface';
import { AppraisalRuleFacets } from '../../../models/search.criteria';

@Component({
  selector: 'app-search-appraisal-rules-facets',
  templateUrl: './search-appraisal-rules-facets.component.html',
  styleUrls: ['./search-appraisal-rules-facets.component.css'],
})
export class SearchAppraisalRulesFacetsComponent implements OnInit, OnChanges {
  constructor() {}

  @Input()
  appraisalRuleFacets: AppraisalRuleFacets;

  @Input()
  totalResults: number;

  rulesFacetDetails: FacetDetails[] = [];
  expiredRulesFacetDetails: FacetDetails[] = [];
  finalActionFacetDetails: FacetDetails[] = [];
  archiveUnitsCountFacetDetails: FacetDetails[] = [];

  ngOnInit(): void {}
  ngOnChanges(): void {
    this.rulesFacetDetails = [];
    this.expiredRulesFacetDetails = [];
    this.finalActionFacetDetails = [];
    this.archiveUnitsCountFacetDetails = [];
    if (this.appraisalRuleFacets) {
      this.handleHavingOrNotRulesFacets();
      this.handleWaitingToRecalculateRulesFacets();
      this.handleFinalActionsRulesFacets();
      this.handleRulesDisctinctsFacets();
      this.handleRulesExpirationFacets();
    }
  }

  private handleRulesExpirationFacets() {
    let unexpiredRulesNb = this.totalResults;
    if (this.appraisalRuleFacets.expiredRulesListFacets && this.appraisalRuleFacets.expiredRulesListFacets.length > 0) {
      this.appraisalRuleFacets.expiredRulesListFacets.forEach((elt) => {
        this.expiredRulesFacetDetails.push({
          title: 'expired',
          totalResults: elt.count,
          clickable: true,
          color: 'red',
          filter: 'filter',
        });
        unexpiredRulesNb = unexpiredRulesNb - elt.count;
      });
    } else {
      this.expiredRulesFacetDetails.push({
        title: 'expired',
        totalResults: 0,
        clickable: true,
        color: 'red',
        filter: 'filter',
      });
    }
    this.expiredRulesFacetDetails.push({
      title: 'unexpired',
      totalResults: unexpiredRulesNb,
      clickable: true,
      color: 'red',
      filter: 'filter',
    });
  }

  private handleRulesDisctinctsFacets() {
    if (this.appraisalRuleFacets.rulesListFacets && this.appraisalRuleFacets.rulesListFacets.length > 0) {
      this.rulesFacetDetails.push({
        title: 'DISTINCT_RULES',
        totalResults: this.appraisalRuleFacets.rulesListFacets.length,
        clickable: true,
        color: 'black',
        filter: 'filter',
      });
    } else {
      this.rulesFacetDetails.push({
        title: 'DISTINCT_RULES',
        totalResults: 0,
        clickable: true,
        color: 'black',
        filter: 'filter',
      });
    }
  }

  private handleFinalActionsRulesFacets() {
    let undefinedStatusNb = this.totalResults;
    if (this.appraisalRuleFacets.finalActionsFacets) {
      this.appraisalRuleFacets.finalActionsFacets.forEach((elt) => {
        this.finalActionFacetDetails.push({
          title: elt.node,
          totalResults: elt.count,
          clickable: true,
          color: 'green',
          filter: 'filter',
        });
        undefinedStatusNb = undefinedStatusNb - elt.count;
      });
    }
    this.finalActionFacetDetails.push({
      title: 'undefined',
      totalResults: undefinedStatusNb,
      clickable: true,
      color: 'green',
      filter: 'filter',
    });
  }

  private handleWaitingToRecalculateRulesFacets() {
    if (
      this.appraisalRuleFacets.waitingToRecalculateRulesListFacets &&
      this.appraisalRuleFacets.waitingToRecalculateRulesListFacets.length > 0
    ) {
      this.appraisalRuleFacets.waitingToRecalculateRulesListFacets
        .filter((elt) => elt.node !== 'true')
        .forEach((elt) => {
          this.archiveUnitsCountFacetDetails.push({
            title: 'WAITING',
            totalResults: elt.count,
            clickable: true,
            color: 'gray',
            filter: 'filter',
          });
        });
    } else {
      this.archiveUnitsCountFacetDetails.push({
        title: 'WAITING',
        totalResults: 0,
        clickable: true,
        color: 'gray',
        filter: 'filter',
      });
    }
  }

  private handleHavingOrNotRulesFacets() {
    if (this.appraisalRuleFacets.noAppraisalRulesFacets && this.appraisalRuleFacets.noAppraisalRulesFacets.length > 0) {
      this.appraisalRuleFacets.noAppraisalRulesFacets.forEach((elt) => {
        this.archiveUnitsCountFacetDetails.push({
          title: 'WITHOUT_RULES',
          totalResults: elt.count,
          clickable: true,
          color: 'red',
          filter: 'filter',
        });
      });
    } else {
      this.archiveUnitsCountFacetDetails.push({
        title: 'WITHOUT_RULES',
        totalResults: 0,
        clickable: true,
        color: 'red',
        filter: 'filter',
      });
    }
  }
}
