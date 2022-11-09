import { DatePipe } from '@angular/common';
import { Component, Input, OnChanges, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Colors } from 'ui-frontend-common';
import { FacetDetails } from 'ui-frontend-common/app/modules/models/operation/facet-details.interface';
import { AppraisalRuleFacets } from '../../../models/search.criteria';

const MAX_RESULTS_WITHOUT_TRACK_TOTAL_HITS = 10000;
const MAX_RESULTS_FACET_TEXT = '-';

@Component({
  selector: 'app-search-appraisal-rules-facets',
  templateUrl: './search-appraisal-rules-facets.component.html',
  styleUrls: ['./search-appraisal-rules-facets.component.scss'],
})
export class SearchAppraisalRulesFacetsComponent implements OnInit, OnChanges {
  constructor(private translateService: TranslateService, private datePipe: DatePipe) {}

  @Input()
  appraisalRuleFacets: AppraisalRuleFacets;

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

  ngOnInit(): void {}
  ngOnChanges(): void {
    this.finalActionsFacetsValues = new Map();
    this.rulesFacetDetails = [];
    this.expiredRulesFacetDetails = [];
    this.finalActionFacetDetails = [];
    this.archiveUnitsCountFacetDetails = [];
    let archiveUnitWithRules = 0;
    if (this.appraisalRuleFacets) {
      archiveUnitWithRules = this.handleWaitingToRecalculateRulesFacets();
      this.handleFinalActionsRulesFacets();
      this.handleRulesDisctinctsFacets();
      this.handleRulesExpirationFacets(archiveUnitWithRules);
    }
  }

  getFacetTextByExactCountFlag(count: number, exactCount: boolean): string {
    let facetContentValue = count.toString();
    if (!exactCount && this.totalResults >= MAX_RESULTS_WITHOUT_TRACK_TOTAL_HITS) {
      facetContentValue = MAX_RESULTS_FACET_TEXT;
    }
    return facetContentValue;
  }
  private handleRulesExpirationFacets(archiveUnitWithRules: number) {
    let expiredRulesNb = 0;
    this.dateFilterValue = this.datePipe.transform(new Date(), 'dd-MM-yyyy');

    if (this.appraisalRuleFacets.expiredRulesListFacets && this.appraisalRuleFacets.expiredRulesListFacets.length > 0) {
      this.appraisalRuleFacets.expiredRulesListFacets.forEach((elt) => {
        if (elt && elt.node && elt.node.length > 10) {
          this.dateFilterValue = ' ' + elt.node.substring(elt.node.length - 10, elt.node.length);
          expiredRulesNb = elt.count ? elt.count : 0;
        }
      });
    }

    this.expiredRulesFacetDetails.push({
      title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.APPRAISAL_RULE.EXPIRED'),
      totalResults: this.getFacetTextByExactCountFlag(expiredRulesNb, this.exactCount),
      clickable: false,
      color: 'gray',
      filter: '>' + this.dateFilterValue,
      backgroundColor: Colors.DISABLED,
    });

    this.expiredRulesFacetDetails.push({
      title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.APPRAISAL_RULE.NOT_EXPIRED'),
      totalResults: this.getFacetTextByExactCountFlag(archiveUnitWithRules - expiredRulesNb, this.exactCount),
      clickable: false,
      color: 'gray',
      filter: '<' + this.dateFilterValue,
      backgroundColor: Colors.DISABLED,
    });
  }

  private handleRulesDisctinctsFacets() {
    if (this.appraisalRuleFacets.rulesListFacets && this.appraisalRuleFacets.rulesListFacets.length > 0) {
      this.rulesFacetDetails.push({
        title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.APPRAISAL_RULE.DISTINCT_RULES'),
        totalResults: this.getFacetTextByExactCountFlag(this.appraisalRuleFacets.rulesListFacets.length, this.exactCount),
        clickable: false,
        color: 'gray',
        backgroundColor: Colors.DISABLED,
      });
    } else {
      this.rulesFacetDetails.push({
        title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.APPRAISAL_RULE.DISTINCT_RULES'),
        totalResults: this.getFacetTextByExactCountFlag(0, this.exactCount),
        clickable: false,
        color: 'gray',
        backgroundColor: Colors.DISABLED,
      });
    }
  }

  private handleFinalActionsRulesFacets() {
    this.finalActionsFacetsValues.set('KEEP', 0);
    this.finalActionsFacetsValues.set('DESTROY', 0);
    this.finalActionsFacetsValues.set('CONFLICT', 0);

    if (this.appraisalRuleFacets.finalActionsFacets) {
      this.appraisalRuleFacets.finalActionsFacets.forEach((elt) => {
        this.finalActionsFacetsValues.set(elt.node.toUpperCase(), elt.count ? elt.count : 0);
      });
    }

    this.finalActionFacetDetails.push({
      title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.APPRAISAL_RULE.KEEP'),
      totalResults: this.getFacetTextByExactCountFlag(this.finalActionsFacetsValues.get('KEEP'), this.exactCount),
      clickable: false,
      color: 'green',
      filter: 'KEEP',
      backgroundColor: Colors.DISABLED,
    });

    this.finalActionFacetDetails.push({
      title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.APPRAISAL_RULE.DESTROY'),
      totalResults: this.getFacetTextByExactCountFlag(this.finalActionsFacetsValues.get('DESTROY'), this.exactCount),
      clickable: false,
      color: 'orange',
      filter: 'DESTROY',
      backgroundColor: Colors.DISABLED,
    });

    this.finalActionFacetDetails.push({
      title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.APPRAISAL_RULE.CONFLICT'),
      totalResults: this.getFacetTextByExactCountFlag(this.finalActionsFacetsValues.get('CONFLICT'), this.exactCount),
      clickable: false,
      color: 'red',
      filter: 'CONFLICT',
      backgroundColor: Colors.DISABLED,
    });
  }

  private handleWaitingToRecalculateRulesFacets() {
    let archiveUnitWithRules = 0;
    if (
      this.appraisalRuleFacets.waitingToRecalculateRulesListFacets &&
      this.appraisalRuleFacets.waitingToRecalculateRulesListFacets.length > 0
    ) {
      this.appraisalRuleFacets.waitingToRecalculateRulesListFacets
        .filter((elt) => elt.node === 'true')
        .forEach((elt) => {
          let calculatedCount = elt.count ? elt.count : 0;

          let notCalculatedCount = '';
          if (
            !this.exactCount &&
            (this.totalResults >= MAX_RESULTS_WITHOUT_TRACK_TOTAL_HITS || calculatedCount >= MAX_RESULTS_WITHOUT_TRACK_TOTAL_HITS)
          ) {
            notCalculatedCount = MAX_RESULTS_FACET_TEXT;
          } else {
            notCalculatedCount = (this.totalResults - calculatedCount).toString();
          }

          this.archiveUnitsCountFacetDetails.push({
            title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.APPRAISAL_RULE.WAITING_TO_RECALCULATE'),
            totalResults: notCalculatedCount,
            clickable: false,
            color: notCalculatedCount === '0' ? 'gray' : 'orange',
            backgroundColor: Colors.DISABLED,
          });

          this.archiveUnitsCountFacetDetails.push({
            title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.APPRAISAL_RULE.CALCULATED'),
            totalResults: this.getFacetTextByExactCountFlag(calculatedCount, this.exactCount),
            clickable: false,
            color: 'gray',
            backgroundColor: Colors.DISABLED,
          });
          archiveUnitWithRules = calculatedCount;
        });
    } else {
      this.archiveUnitsCountFacetDetails.push({
        title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.APPRAISAL_RULE.WAITING_TO_RECALCULATE'),
        totalResults: this.getFacetTextByExactCountFlag(this.totalResults, this.exactCount),
        clickable: false,
        color: this.totalResults === 0 ? 'gray' : 'orange',
        backgroundColor: Colors.DISABLED,
      });
      this.archiveUnitsCountFacetDetails.push({
        title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.APPRAISAL_RULE.CALCULATED'),
        totalResults: this.getFacetTextByExactCountFlag(0, this.exactCount),
        clickable: false,
        color: 'gray',
        backgroundColor: Colors.DISABLED,
      });
      archiveUnitWithRules = 0;
    }

    if (this.appraisalRuleFacets.noAppraisalRulesFacets && this.appraisalRuleFacets.noAppraisalRulesFacets.length > 0) {
      this.appraisalRuleFacets.noAppraisalRulesFacets.forEach((elt) => {
        this.archiveUnitsCountFacetDetails.push({
          title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.APPRAISAL_RULE.WITHOUT_RULES'),
          totalResults: this.getFacetTextByExactCountFlag(elt.count, this.exactCount),
          clickable: false,
          color: 'gray',
          backgroundColor: Colors.DISABLED,
        });
        archiveUnitWithRules -= elt.count ? elt.count : 0;
      });
    } else {
      this.archiveUnitsCountFacetDetails.push({
        title: this.translateService.instant('ARCHIVE_SEARCH.FACETS.APPRAISAL_RULE.WITHOUT_RULES'),
        totalResults: this.getFacetTextByExactCountFlag(0, this.exactCount),
        clickable: false,
        color: 'gray',
        backgroundColor: Colors.DISABLED,
      });
    }
    return archiveUnitWithRules;
  }
}
