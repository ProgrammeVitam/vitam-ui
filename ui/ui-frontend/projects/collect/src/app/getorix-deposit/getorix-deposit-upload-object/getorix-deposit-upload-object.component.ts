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
import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatMenuTrigger } from '@angular/material/menu';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Observable, Subject, Subscription, merge } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import {
  ArchiveSearchResultFacets,
  BreadCrumbData,
  CriteriaDataType,
  CriteriaOperator,
  Direction,
  FilingHoldingSchemeNode,
  GlobalEventService,
  Logger,
  ORPHANS_NODE_ID,
  PagedResult,
  SearchCriteria,
  SearchCriteriaEltDto,
  SearchCriteriaMgtRuleEnum,
  SearchCriteriaTypeEnum,
  SidenavPage,
  Unit,
} from 'ui-frontend-common';
import { isEmpty } from 'underscore';
import { ArchiveCollectService } from '../../collect/archive-search-collect/archive-collect.service';
import { ArchiveFacetsService } from '../../collect/archive-search-collect/archive-search-criteria/services/archive-facets.service';
import { ArchiveSearchHelperService } from '../../collect/archive-search-collect/archive-search-criteria/services/archive-search-helper.service';
import { CollectUploadFile, CollectZippedUploadFile } from '../../collect/shared/collect-upload/collect-upload-file';
import { CollectUploadService } from '../../collect/shared/collect-upload/collect-upload.service';
import { GetorixDeposit } from '../core/model/getorix-deposit.interface';
import { GetorixUnitFullPath } from '../core/model/getorix-unit-full-path.interface';
import { GetorixDepositService } from '../getorix-deposit.service';
import { GetorixDepositSharedDataService } from '../services/getorix-deposit-shared-data.service';

const FILTER_DEBOUNCE_TIME_MS = 400;
const UNIT_UPS = '#unitups';

const PAGE_SIZE = 10;
@Component({
  selector: 'getorix-deposit-upload-object',
  templateUrl: './getorix-deposit-upload-object.component.html',
  styleUrls: ['./getorix-deposit-upload-object.component.scss'],
})
export class GetorixDepositUploadObjectComponent extends SidenavPage<any> implements OnInit, OnDestroy {
  operationIdentifierSubscription: Subscription;
  operationId: string;
  dataBreadcrumb: BreadCrumbData[];
  getorixDepositDetails: GetorixDeposit;
  isIndeterminate: boolean;
  isAllChecked: boolean;
  numberOfSelectedElements = 0;
  itemNotSelected = 0;

  // upload
  uploadFiles$: Observable<CollectUploadFile[]>;
  zippedFile$: Observable<CollectZippedUploadFile>;
  @ViewChild('fileSearch', { static: false }) fileSearch: any;
  hasDropZoneOver = false;
  isShowUploadComponent = false;

  // search units

  totalResults = 0;
  orderBy = '#approximate_creation_date';
  direction = Direction.DESCENDANT;
  searchHasResults = false;
  pageNumbers = 0;
  canLoadMore = false;
  archiveUnits: Unit[];
  pending = true;
  submited = false;
  currentPage = 0;
  criteriaSearchList: SearchCriteriaEltDto[] = [];
  selectedItemsList: string[] = [];
  selectedItemsListOver: string[] = [];

  show = true;
  getorixDepositDetailsFound = false;

  private readonly orderChange = new Subject<string>();
  subscriptions: Subscription = new Subscription();

  archiveSearchResultFacets: ArchiveSearchResultFacets = new ArchiveSearchResultFacets();

  searchCriterias: Map<string, SearchCriteria>;

  nodeParents: { id: string; title: string }[];

  nodeParentsFullPath: GetorixUnitFullPath[] = [];

  @ViewChild('childrenMenuMenuTrigger', { read: MatMenuTrigger, static: false })
  childrenMenuMenuTrigger: MatMenuTrigger;

  isLoadUnitPath = false;

  constructor(
    private route: ActivatedRoute,
    globalEventService: GlobalEventService,
    private getorixDepositService: GetorixDepositService,
    private router: Router,
    private translateService: TranslateService,
    private loggerService: Logger,
    private uploadService: CollectUploadService,
    private snackBar: MatSnackBar,
    private translationService: TranslateService,
    private archiveUnitCollectService: ArchiveCollectService,
    private getorixDepositSharedDateService: GetorixDepositSharedDataService,
    private archiveFacetsService: ArchiveFacetsService,
    private archiveHelperService: ArchiveSearchHelperService
  ) {
    super(route, globalEventService);
  }

  ngOnInit(): void {
    this.isIndeterminate = false;
    this.itemNotSelected = 0;
    this.isAllChecked = false;

    this.searchCriterias = new Map();

    this.uploadFiles$ = this.uploadService.getUploadingFiles();
    this.zippedFile$ = this.uploadService.getZipFile();

    this.operationIdentifierSubscription = this.route.params.subscribe((params) => {
      this.operationId = params.operationIdentifier;
      this.dataBreadcrumb = [
        {
          redirectUrl: this.router.url.replace('/create', '').replace('upload-object', '').replace(this.operationId, ''),
          label: this.translateService.instant('GETORIX_DEPOSIT.BREAD_CRUMB.ARCHIVAL_SPACE'),
        },
        {
          label: this.translateService.instant('GETORIX_DEPOSIT.BREAD_CRUMB.NEW_PROJECT'),
          redirectUrl: this.router.url.replace('/upload-object/', '').replace(this.operationId, ''),
        },
        { label: this.translateService.instant('GETORIX_DEPOSIT.BREAD_CRUMB.UPLOAD_ARCHIVES') },
      ];
      this.subscriptions.add(
        this.getorixDepositService.getGetorixDepositById(params.operationIdentifier).subscribe(
          (data: GetorixDeposit) => {
            this.getorixDepositDetails = data;
            this.searchUnits(true);
            this.getorixDepositDetailsFound = true;
            this.getorixDepositSharedDateService.emitTransactionId(this.getorixDepositDetails.transactionId);
          },
          (error) => {
            this.loggerService.error('error while searching for this operation', error);
            this.router.navigate([this.router.url.replace('/create', '').replace('upload-object', '').replace(this.operationId, '')]);
          }
        )
      );
    });

    this.subscriptions.add(
      merge(this.orderChange)
        .pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS))
        .subscribe(() => {
          this.searchSubmit();
        })
    );

    this.subscriptions.add(
      this.getorixDepositSharedDateService.getToggle().subscribe((hidden) => {
        this.show = hidden;
      })
    );
  }

  goToUpdateOperation() {
    this.router.navigate([this.router.url.replace('/upload-object/', '').replace(this.operationId, '')], {
      queryParams: { operationId: this.operationId },
    });
  }

  showComments() {
    console.log('show comments');
  }

  ngOnDestroy() {
    this.operationIdentifierSubscription?.unsubscribe();
    this.subscriptions?.unsubscribe();
  }

  uploadNewObject() {
    this.isShowUploadComponent = !this.isShowUploadComponent;
  }

  editUnits() {
    console.log('edit units');
  }

  deleteUnits() {
    console.log('delete units');
  }

  reclassifyUnits() {
    console.log('reclassify units');
  }

  notifyArchivist() {
    console.log('send notification to the archivist');
  }

  checkParentBoxChange(event: any) {
    const { checked } = event.target;

    this.isAllChecked = checked;
    this.numberOfSelectedElements = checked ? this.totalResults : 0;
    this.archiveUnits.forEach((unit) => {
      this.selectedItemsList.push(unit['#id']);
      this.selectedItemsListOver.push(unit['#id']);
    });
    if (!checked) {
      this.isIndeterminate = false;
      this.selectedItemsList = [];
      this.selectedItemsListOver = [];
    }
  }

  checkParentBoxChangeJ(event: any) {
    const { checked } = event.target;

    this.isAllChecked = checked;
    this.numberOfSelectedElements = checked ? this.totalResults : 0;
    if (!checked) {
      this.isIndeterminate = false;
    }
  }

  checkChildrenBoxChange(id: string, event: any) {
    console.log('id', id);
    const action = event.target.checked;

    if (this.isAllChecked && !action) {
      this.isIndeterminate = true;

      if (this.numberOfSelectedElements > 0) {
        this.numberOfSelectedElements--;
        this.itemNotSelected++;
      }
    } else {
      this.itemNotSelected = 0;
      if (action) {
        this.numberOfSelectedElements++;
        if (this.numberOfSelectedElements === this.totalResults) {
          this.isIndeterminate = false;
        }
      } else {
        if (this.numberOfSelectedElements > 0) {
          this.numberOfSelectedElements--;
        }
      }
    }
  }

  onDragOver(event: any) {
    event.preventDefault();
    this.hasDropZoneOver = true;
  }

  onDragLeave(event: any) {
    event.preventDefault();
    this.hasDropZoneOver = false;
  }

  async onDropped(event: any) {
    this.hasDropZoneOver = false;
    event.preventDefault();
    const items = event.dataTransfer.items;
    const exists = this.uploadService.directoryExistInZipFile(items, true);
    if (exists) {
      this.snackBar.open(this.translationService.instant('GETORIX_DEPOSIT.UPLOAD_ARCHIVES.UPLOAD_FILE_ALREADY_IMPORTED'), null, {
        duration: 3000,
      });
      return;
    }
    await this.uploadService.handleDragAndDropUpload(items).then(() => {
      return this.sendFilesToVitam();
    });
  }

  async handleFile(event: any) {
    event.preventDefault();
    const items = event.target.files;
    const exists = this.uploadService.directoryExistInZipFile(items, false);
    if (exists) {
      this.snackBar.open(this.translationService.instant('GETORIX_DEPOSIT.UPLOAD_ARCHIVES.UPLOAD_FILE_ALREADY_IMPORTED'), null, {
        duration: 3000,
      });
      return;
    }
    await this.uploadService
      .handleUpload(items)

      .then(() => {
        return this.sendFilesToVitam();
      });
  }

  addFolder() {
    this.fileSearch.nativeElement.click();
  }

  removeFolder(file: CollectUploadFile) {
    this.uploadService.removeFolder(file);
  }

  deleteAllFiles() {
    this.subscriptions.add(
      this.uploadFiles$.subscribe((data) => {
        data.forEach((file) => {
          this.removeFolder(file);
        });
      })
    );
  }

  async sendFilesToVitam() {
    return this.uploadService
      .uploadZip(this.getorixDepositDetails.tenantIdentifier, this.getorixDepositDetails.transactionId)
      .then((uploadOperation) => {
        uploadOperation.subscribe(
          () => {},
          (error: any) => {
            this.logger.error('Error while uploading files to Vitam', error);
          },
          () => {
            this.isShowUploadComponent = false;
            this.snackBar.open(this.translationService.instant('GETORIX_DEPOSIT.UPLOAD_ARCHIVES.TERMINATED'), null, {
              panelClass: 'vitamui-snack-bar',
              duration: 10000,
            });
            setTimeout(() => {
              const node = { id: 'ORPHANS_NODE' } as FilingHoldingSchemeNode;
              this.searchUnitsOfNode(node, true);
            }, 3000);
          }
        );
      })
      .catch((error) => {
        this.logger.error('Error while uploading files to Vitam', error);
      });
  }

  searchSubmit() {
    this.submited = true;
    this.archiveHelperService.buildNodesListForQUery(this.searchCriterias, this.criteriaSearchList);
    this.archiveHelperService.buildFieldsCriteriaListForQUery(this.searchCriterias, this.criteriaSearchList);
    for (const mgtRuleType in SearchCriteriaMgtRuleEnum) {
      this.archiveHelperService.buildManagementRulesCriteriaListForQuery(mgtRuleType, this.searchCriterias, this.criteriaSearchList);
    }
    if (this.criteriaSearchList && this.criteriaSearchList.length > 0) {
      this.searchUnits(false);
    }
  }

  searchUnits(isLoadTree: boolean) {
    this.pending = true;
    this.isLoadUnitPath = true;
    const sortingCriteria = { criteria: this.orderBy, sorting: this.direction };
    const searchCriteria = {
      criteriaList: this.criteriaSearchList,
      pageNumber: this.currentPage,
      size: PAGE_SIZE,
      sortingCriteria,
      trackTotalHits: false,
    };

    if (isLoadTree) {
      this.getorixDepositSharedDateService.emitSearchCriterias(searchCriteria);
    }

    this.subscriptions.add(
      this.archiveUnitCollectService.searchArchiveUnitsByCriteria(searchCriteria, this.getorixDepositDetails.transactionId).subscribe(
        (pagedResult: PagedResult) => {
          if (this.currentPage === 0) {
            this.archiveUnits = pagedResult.results;
            this.searchHasResults = !isEmpty(pagedResult.results);
            if (isLoadTree) {
              this.getorixDepositSharedDateService.emitHasResult(!isEmpty(pagedResult.results));
            }
            this.totalResults = pagedResult.totalResults;
            this.archiveSearchResultFacets.nodesFacets = this.archiveFacetsService.extractNodesFacetsResults(pagedResult.facets);
            this.totalResults = pagedResult.totalResults;
            if (isLoadTree) {
              this.getorixDepositSharedDateService.emitTotalResults(this.totalResults);
              this.getorixDepositSharedDateService.emitFacets(this.archiveSearchResultFacets.nodesFacets);
            }
          } else if (pagedResult.results) {
            pagedResult.results.forEach((elt) => this.archiveUnits.push(elt));
            if (this.isAllChecked) {
              pagedResult.results.forEach((unit) => {
                this.selectedItemsList.push(unit['#id']);
                this.selectedItemsListOver.push(unit['#id']);
              });
            }
          }
          this.pageNumbers = pagedResult.pageNumbers;
          if (this.isAllChecked) {
            this.numberOfSelectedElements = this.totalResults - this.itemNotSelected;
          }
          this.canLoadMore = this.currentPage < this.pageNumbers - 1;

          this.pending = false;
          this.isLoadUnitPath = false;
        },
        (error: HttpErrorResponse) => {
          this.logger.error('Error message :', error.message);
          this.canLoadMore = false;
          this.pending = false;
          this.isLoadUnitPath = false;
        }
      )
    );
  }

  loadMore() {
    if (this.pending) {
      return;
    }
    this.canLoadMore = this.currentPage < this.pageNumbers - 1;
    if (!this.canLoadMore) {
      return;
    }
    this.submited = true;
    this.currentPage = this.currentPage + 1;

    this.searchUnits(false);
  }

  getArchiveUnitType(archiveUnit: Unit) {
    if (archiveUnit) {
      return archiveUnit['#unitType'];
    }
  }

  isItemSelected(archiveUnit: Unit): boolean {
    return this.selectedItemsList.filter((element) => element === archiveUnit['#id']).length > 0;
  }

  selectArchiveUnit(archiveUnit: Unit) {
    let action: boolean = !this.isItemSelected(archiveUnit);

    if (action) {
      this.selectedItemsList.push(archiveUnit['#id']);
      this.selectedItemsListOver.push(archiveUnit['#id']);
      this.numberOfSelectedElements++;
      this.isIndeterminate = !(this.numberOfSelectedElements === this.totalResults);
      if (this.numberOfSelectedElements === this.totalResults) {
        this.isAllChecked = true;
      }
    } else {
      this.selectedItemsList = this.selectedItemsList.filter((element) => element != archiveUnit['#id']);
      this.selectedItemsListOver = this.selectedItemsListOver.filter((element) => element != archiveUnit['#id']);

      this.numberOfSelectedElements--;
      if (this.isAllChecked) {
        this.itemNotSelected++;
        this.isIndeterminate = true;
      }

      if (this.numberOfSelectedElements === 0) {
        this.isIndeterminate = false;
        this.isAllChecked = false;
      }
    }
  }

  emitOrderChange() {
    this.orderChange.next();
  }

  hideTreeBlock(hidden: boolean) {
    this.show = !hidden;
  }

  onMouseOverOnUnitRow(unit: Unit) {
    if (!this.isItemSelected(unit)) {
      this.selectedItemsListOver.push(unit['#id']);
    }
  }

  onMouseLeaveOnUnitRow(archiveUnit: Unit) {
    if (!this.isItemSelected(archiveUnit)) {
      this.selectedItemsListOver = this.selectedItemsListOver.filter((element) => element != archiveUnit['#id']);
    }
  }

  isItemSelectedOver(archiveUnit: Unit) {
    return this.selectedItemsListOver.filter((element) => element === archiveUnit['#id']).length > 0;
  }

  searchUnitsOfNode(selectedUnitFolder: FilingHoldingSchemeNode, isLoadTree?: boolean) {
    this.isAllChecked = false;
    this.numberOfSelectedElements = 0;
    this.currentPage = 0;
    this.selectedItemsListOver = [];
    this.selectedItemsList = [];
    this.archiveUnits = [];
    this.criteriaSearchList = [];
    if (selectedUnitFolder.id !== ORPHANS_NODE_ID) {
      this.criteriaSearchList.push({
        criteria: UNIT_UPS,
        operator: CriteriaOperator.IN,
        category: SearchCriteriaTypeEnum.FIELDS,
        values: [{ id: selectedUnitFolder.id, value: selectedUnitFolder.id }],
        dataType: CriteriaDataType.STRING,
      });
    }
    this.findParents(selectedUnitFolder);
    if (isLoadTree) {
      this.searchUnits(true);
    } else {
      this.searchUnits(false);
    }
  }

  getUnitsOfNode(unitFullPath: GetorixUnitFullPath) {
    const node = { id: unitFullPath.id } as FilingHoldingSchemeNode;
    this.getorixDepositSharedDateService.emitSelectedNode(node);

    this.nodeParentsFullPath.splice(this.nodeParentsFullPath.indexOf(unitFullPath) + 1);
    this.searchUnitsOfNode(node);
  }

  // function to find parents
  findParents(selectedUnitFolder: FilingHoldingSchemeNode) {
    this.nodeParents = [];
    this.nodeParentsFullPath = [];
    this.isLoadUnitPath = true;

    if (selectedUnitFolder.id !== ORPHANS_NODE_ID) {
      this.subscriptions.add(
        this.getorixDepositService.getUnitFullPath(selectedUnitFolder.id).subscribe((data) => {
          this.nodeParentsFullPath = data;
          this.isLoadUnitPath = false;
        })
      );
    }
  }

  openNodeChildrenMenu() {
    this.childrenMenuMenuTrigger.openMenu();
  }
}
