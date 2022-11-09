import { Component, Input, OnInit } from '@angular/core';
import { AppraisalRuleFacets } from '../../models/search.criteria';

@Component({
  selector: 'app-archive-search-rules-facets',
  templateUrl: './archive-search-rules-facets.component.html',
  styleUrls: ['./archive-search-rules-facets.component.css'],
})
export class ArchiveSearchRulesFacetsComponent implements OnInit {
  @Input()
  totalResults: number;

  @Input()
  appraisalRuleFacets: AppraisalRuleFacets;

  @Input()
  exactCount: boolean;

  @Input()
  tenantIdentifier: number;

  facetsVisibles = true;

  showHideFacets(show: boolean) {
    this.facetsVisibles = show;
  }

  constructor() {}

  ngOnInit(): void {}
}
