import { Component, Input, OnInit } from '@angular/core';
import { ArchiveSearchResultFacets } from '../../models/search.criteria';

@Component({
  selector: 'app-archive-search-rules-facets',
  templateUrl: './archive-search-rules-facets.component.html',
  styleUrls: ['./archive-search-rules-facets.component.css'],
})
export class ArchiveSearchRulesFacetsComponent implements OnInit {
  @Input()
  totalResults: number;

  @Input()
  archiveSearchResultFacets: ArchiveSearchResultFacets[];

  @Input()
  tenantIdentifier: number;

  constructor() {}

  ngOnInit(): void {}
}
