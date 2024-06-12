/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { Component, ElementRef, EventEmitter, Input, OnDestroy, OnInit, Output, TemplateRef, ViewChild } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { ActivatedRoute } from '@angular/router';
import { Subject, merge } from 'rxjs';
import { debounceTime, distinctUntilChanged, filter, map, takeUntil, tap } from 'rxjs/operators';
import {
  // AccessContract,
  AdminUserProfile,
  Agency,
  ApplicationId,
  AuthService,
  DEFAULT_PAGE_SIZE,
  Direction,
  InfiniteScrollTable,
  PageRequest,
  Role,
  SecurityService,
} from 'vitamui-library';
import { AgencyService } from '../agency.service';

const FILTER_DEBOUNCE_TIME_MS = 400;

@Component({
  selector: 'app-agency-list',
  templateUrl: './agency-list.component.html',
  styleUrls: ['./agency-list.component.scss'],
})
export class AgencyListComponent extends InfiniteScrollTable<Agency> implements OnDestroy, OnInit {
  // tslint:disable-next-line:no-input-rename
  @Input('search') set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }

  // tslint:disable-next-line:variable-name
  private _searchText: string;

  @Output() agencyClick = new EventEmitter<Agency>();

  @ViewChild('filterTemplate', { static: false }) filterTemplate: TemplateRef<AgencyListComponent>;
  @ViewChild('filterButton', { static: false }) filterButton: ElementRef;
  @ViewChild('confirmDeleteDialog', { static: true }) confirmDeleteDialog: TemplateRef<AgencyListComponent>;

  agencyToDelete: Agency;
  hasDeleteRole = false;
  orderBy = 'Identifier';
  direction = Direction.ASCENDANT;
  genericUserRole: Readonly<{ appId: ApplicationId; tenantIdentifier: number; roles: Role[] }> = {
    appId: ApplicationId.USERS_APP,
    tenantIdentifier: +this.authService.user.proofTenantIdentifier,
    roles: [Role.ROLE_GENERIC_USERS],
  };

  readonly orderChange = new Subject<void>();
  private readonly filterChange = new Subject<{ [key: string]: any[] }>();
  private readonly searchChange = new Subject<string>();

  @Input() get connectedUserInfo(): AdminUserProfile {
    return this._connectedUserInfo;
  }

  set connectedUserInfo(userInfo: AdminUserProfile) {
    this._connectedUserInfo = userInfo;
  }

  // tslint:disable-next-line:variable-name
  private _connectedUserInfo: AdminUserProfile;
  private readonly destroyer$ = new Subject<void>();

  constructor(
    public agencyService: AgencyService,
    private route: ActivatedRoute,
    private authService: AuthService,
    private matDialog: MatDialog,
    private securityService: SecurityService,
  ) {
    super(agencyService);
  }

  ngOnInit() {
    const tenantChange = this.route.paramMap.pipe(
      filter((paramMap) => !!paramMap.get('tenantIdentifier')),
      map((paramMap) => +paramMap.get('tenantIdentifier')),
      distinctUntilChanged(),
      tap((tenantIdentifier) => {
        this.agencyService.setTenantId(tenantIdentifier);
        this.securityService
          .hasRole(ApplicationId.AGENCIES_APP, tenantIdentifier, Role.ROLE_CREATE_AGENCIES)
          .subscribe((value: boolean) => (this.hasDeleteRole = value));
      }),
    );

    merge(tenantChange, this.searchChange, this.filterChange, this.orderChange)
      .pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS))
      .subscribe(() => {
        const query: any = this.buildAgencyCriteriaFromSearch();
        const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
        this.search(pageRequest);
      });

    this.replaceUpdatedAgency();
  }

  public searchAgencyOrdered(): void {
    this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT));
  }

  public deleteAgencyDialog(agency: Agency): void {
    this.agencyToDelete = agency;
    this.matDialog
      .open(this.confirmDeleteDialog, { panelClass: 'vitamui-confirm-dialog' })
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.agencyToDelete = null;
        this.agencyService
          .delete(agency)
          .subscribe(() => this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT)));
      });
  }

  private buildAgencyCriteriaFromSearch() {
    const criteria: any = {};
    if (this._searchText.length > 0) {
      criteria.Name = this._searchText;
      criteria.Identifier = this._searchText;
    }

    return criteria;
  }

  private replaceUpdatedAgency(): void {
    this.agencyService.updated.pipe(takeUntil(this.destroyer$)).subscribe((updatedAgency: Agency) => {
      const index = this.dataSource.findIndex((item: Agency) => item.id === updatedAgency.id);
      if (index !== -1) {
        this.dataSource[index] = updatedAgency;
      }
    });
  }

  ngOnDestroy() {
    this.updatedData.unsubscribe();
    this.destroyer$.next();
    this.destroyer$.complete();
  }
}
