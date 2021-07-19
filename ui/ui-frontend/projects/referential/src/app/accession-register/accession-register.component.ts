import { Component, OnInit } from '@angular/core';
import { AccessionRegisterSummary } from '../../../../vitamui-library/src/lib/models/accession-registe-summary';
import { AccessionRegistersService } from './accession-register.service';
import { SidenavPage } from 'ui-frontend-common';
import { AccessionRegisterDetail } from '../../../../vitamui-library/src/lib/models/accession-registers-detail';
import { ActivatedRoute } from '@angular/router';
import { AccessionRegisterBusiness } from './accession-register.business';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-accession-register',
  templateUrl: './accession-register.component.html',
  styleUrls: ['./accession-register.component.scss'],
})
export class AccessionRegisterComponent extends SidenavPage<AccessionRegisterDetail> implements OnInit {
  accessionRegisterSummary: AccessionRegisterSummary[] = [];
  advancedSearchPanelOpenState$: Observable<boolean>;
  public search: string;

  constructor(
    accessionRegisterService: AccessionRegistersService,
    route: ActivatedRoute,
    private accessionRegisterBusiness: AccessionRegisterBusiness
  ) {
    super(route, accessionRegisterService);
  }

  ngOnInit(): void {
    this.advancedSearchPanelOpenState$ = this.accessionRegisterBusiness.isOpenAdvancedSearchPanel();
  }

  onSearchSubmit(search: string) {
    this.search = search;
  }

  openAdvancedSearchPanel(value: boolean) {
    console.log(value);
    this.accessionRegisterBusiness.toggleOpenAdvancedSearchPanel();
  }
}
