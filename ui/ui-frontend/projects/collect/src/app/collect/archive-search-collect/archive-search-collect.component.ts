/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Subject, Subscription } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  AccessContract,
  Direction,
  ExternalParameters,
  ExternalParametersService,
  GlobalEventService,
  SidenavPage,
} from 'ui-frontend-common';
import { Unit } from 'vitamui-library/lib/models/unit.interface';
import { ArchiveCollectService } from './archive-collect.service';
import { CriteriaValue, PagedResult, SearchCriteria, SearchCriteriaCategory, SearchCriteriaEltDto } from '../core/models';

const PAGE_SIZE = 10;

@Component({
  selector: 'app-archive-search-collect',
  templateUrl: './archive-search-collect.component.html',
  styleUrls: ['./archive-search-collect.component.scss'],
})
export class ArchiveSearchCollectComponent extends SidenavPage<any> implements OnInit, OnDestroy {
  accessContract: string;
  accessContractSub: Subscription;
  accessContractSubscription: Subscription;
  errorMessageSub: Subscription;

  projectId: string;
  foundAccessContract = false;
  hasAccessContractManagementPermissions = false;
  hasUpdateDescriptiveUnitMetadataRole = false;
  isLPExtended = false;

  searchCriteriaKeys: string[];
  searchCriterias: Map<string, SearchCriteria>;
  criteriaSearchList: SearchCriteriaEltDto[] = [];
  additionalSearchCriteriaCategories: SearchCriteriaCategory[];
  included = false;
  showCriteriaPanel = true;
  showSearchCriteriaPanel = false;
  archiveUnits: Unit[];
  ontologies: any;

  listOfUAIdToInclude: CriteriaValue[] = [];
  listOfUAIdToExclude: CriteriaValue[] = [];
  listOfUACriteriaSearch: SearchCriteriaEltDto[] = [];

  // AU Search Properties
  pending = false;
  submited = false;
  currentPage = 0;
  itemSelected = 0;
  itemNotSelected = 0;
  isIndeterminate: boolean;
  isAllchecked: boolean;
  waitingToGetFixedCount = false;
  pendingGetFixedCount = false;
  totalResults = 0;
  orderBy = 'Title';
  direction = Direction.ASCENDANT;
  DEFAULT_RESULT_THRESHOLD = 10000;
  hasResults = false;
  pageNumbers = 0;
  canLoadMore = false;

  private readonly orderChange = new Subject<string>();

  constructor(
    private route: ActivatedRoute,
    globalEventService: GlobalEventService,
    private externalParameterService: ExternalParametersService,
    private translateService: TranslateService,
    private archiveUnitCollectService: ArchiveCollectService,
    private snackBar: MatSnackBar
  ) {
    super(route, globalEventService);
  }

  ngOnDestroy(): void {
    this.accessContractSub?.unsubscribe();
    this.accessContractSubscription?.unsubscribe();
  }

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.projectId = params.id;
    });

    this.searchCriteriaKeys = [];
    this.searchCriterias = new Map();

    this.fetchUserAccessContractFromExternalParameters();
    this.submit();
  }

  fetchUserAccessContractFromExternalParameters() {
    this.accessContractSub = this.externalParameterService.getUserExternalParameters().subscribe((parameters) => {
      const accessConctractId: string = parameters.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
      if (accessConctractId && accessConctractId.length > 0) {
        this.accessContract = accessConctractId;
        this.foundAccessContract = true;
        this.fetchVitamAccessContract();
        if (this.archiveUnits.length === 0) {
          this.callVitamApiService(false);
        }
      } else {
        this.errorMessageSub = this.translateService
          .get('ARCHIVE_SEARCH.ACCESS_CONTRACT_NOT_FOUND')
          .pipe(
            map((message) => {
              this.snackBar.open(message, null, {
                panelClass: 'vitamui-snack-bar',
                duration: 10000,
              });
            })
          )
          .subscribe();
      }
    });
  }

  fetchVitamAccessContract() {
    this.accessContractSubscription = this.archiveUnitCollectService.getAccessContractById(this.accessContract).subscribe(
      (ac: AccessContract) => {
        this.hasAccessContractManagementPermissions = this.archiveUnitCollectService.hasAccessContractManagementPermissions(ac);
      },
      (error: any) => {
        this.logger.error('AccessContract not found :', error.message);
        const message = this.translateService.instant('COLLECT.ACCESS_CONTRACT_NOT_FOUND_IN_VITAM');
        this.snackBar.open(message + ': ' + this.accessContract, null, {
          panelClass: 'vitamui-snack-bar',
          duration: 10000,
        });
      }
    );
  }

  submit() {
    this.initializeSelectionParams();
  }

  private initializeSelectionParams() {
    this.pending = true;
    this.submited = true;
    this.showCriteriaPanel = false;
    this.showSearchCriteriaPanel = false;
    this.currentPage = 0;
    this.archiveUnits = [];
    this.criteriaSearchList = [];
    this.itemSelected = 0;
    this.isIndeterminate = false;
    this.itemNotSelected = 0;
    this.isAllchecked = false;
  }

  loadMore() {
    this.canLoadMore = this.currentPage < this.pageNumbers - 1;
    if (this.canLoadMore && !this.pending) {
      this.submited = true;
      this.currentPage = this.currentPage + 1;
      this.criteriaSearchList = [];
      if (this.criteriaSearchList && this.criteriaSearchList.length > 0) {
        this.callVitamApiService(false);
      }
    }
  }

  private callVitamApiService(includeFacets: boolean) {
    this.pending = true;

    const sortingCriteria = { criteria: this.orderBy, sorting: this.direction };
    const searchCriteria = {
      criteriaList: this.criteriaSearchList,
      pageNumber: this.currentPage,
      size: PAGE_SIZE,
      sortingCriteria,
      trackTotalHits: false,
      computeFacets: includeFacets,
    };
    this.archiveUnitCollectService.searchArchiveUnitsByCriteria(searchCriteria, this.projectId, this.accessContract).subscribe(
      (pagedResult: PagedResult) => {
        if (this.currentPage === 0) {
          this.archiveUnits = pagedResult.results;
          this.hasResults = true;
          this.totalResults = pagedResult.totalResults;
        } else {
          if (pagedResult.results) {
            this.hasResults = true;
            pagedResult.results.forEach((elt) => this.archiveUnits.push(elt));
          }
        }

        this.pageNumbers = pagedResult.pageNumbers;

        this.waitingToGetFixedCount = this.totalResults === this.DEFAULT_RESULT_THRESHOLD;

        if (this.isAllchecked) {
          this.itemSelected = this.totalResults - this.itemNotSelected;
        }

        this.canLoadMore = this.currentPage < this.pageNumbers - 1;

        this.pending = false;
        this.included = true;
      },
      (error: HttpErrorResponse) => {
        this.logger.error('Error message :', error.message);
        this.canLoadMore = false;
        this.pending = false;
      }
    );
  }

  checkParentBoxChange(event: any) {
    const action = event.target.checked;
    if (action) {
      this.itemSelected = this.totalResults;
      this.isAllchecked = true;
    } else {
      this.isAllchecked = false;
      this.isIndeterminate = false;
      this.itemSelected = 0;
    }
    this.listOfUAIdToInclude = [];
    this.listOfUAIdToExclude = [];
    this.listOfUACriteriaSearch = [];
  }

  checkChildrenBoxChange(id: string, event: any) {
    const action = event.target.checked;

    if (this.isAllchecked && !action) {
      this.listOfUACriteriaSearch = [];
      this.isIndeterminate = true;
      this.listOfUAIdToExclude.push({ value: id, id });
      this.listOfUAIdToInclude = [];
      if (this.itemSelected > 0) {
        this.itemSelected--;
        this.itemNotSelected++;
      }
    } else {
      this.itemNotSelected = 0;
      if (action) {
        this.listOfUACriteriaSearch = [];
        this.itemSelected++;
        if (this.itemSelected === this.totalResults) {
          this.isIndeterminate = false;
        }
        this.listOfUAIdToInclude.push({ value: id, id });
        this.listOfUAIdToExclude.splice(0, this.listOfUAIdToExclude.length);
      } else {
        this.listOfUAIdToInclude = this.listOfUAIdToInclude.filter((element) => element.id !== id);
        if (this.itemSelected > 0) {
          this.itemSelected--;
        }
      }
    }
  }

  showExtendedLateralPanel() {
    this.isLPExtended = true;
  }

  backToNormalLateralPanel() {
    this.isLPExtended = false;
  }

  emitOrderChange() {
    this.orderChange.next();
  }
}
