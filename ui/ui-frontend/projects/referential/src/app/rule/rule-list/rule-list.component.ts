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
import { TranslateService } from '@ngx-translate/core';
import { Subject, merge } from 'rxjs';
import { debounceTime, filter } from 'rxjs/operators';
import {
  AdminUserProfile,
  ApplicationId,
  AuthService,
  ConfirmActionComponent,
  DEFAULT_PAGE_SIZE,
  Direction,
  InfiniteScrollTable,
  PageRequest,
  Role,
  Rule,
  RuleService,
  VitamUISnackBarService,
} from 'vitamui-library';
import { RULE_MEASUREMENTS, RULE_TYPES } from '../rules.constants';

const FILTER_DEBOUNCE_TIME_MS = 400;

@Component({
  selector: 'app-rule-list',
  templateUrl: './rule-list.component.html',
  styleUrls: ['./rule-list.component.scss'],
})
export class RuleListComponent extends InfiniteScrollTable<Rule> implements OnDestroy, OnInit {
  // eslint-disable-next-line @angular-eslint/no-input-rename
  @Input('search')
  set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }

  // eslint-disable-next-line @typescript-eslint/naming-convention,no-underscore-dangle,id-blacklist,id-match
  private _searchText: string;

  ruleTypes = RULE_TYPES;

  filterMap: { [key: string]: any[] } = {
    ruleType: this.ruleTypes.map((value) => value.label),
  };

  // eslint-disable-next-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match
  private _filters: string;

  ruleMeasurements = RULE_MEASUREMENTS;

  @Output() ruleClick = new EventEmitter<Rule>();

  @ViewChild('filterTemplate', { static: false }) filterTemplate: TemplateRef<RuleListComponent>;
  @ViewChild('filterButton', { static: false }) filterButton: ElementRef;

  overridePendingChange: true;
  loaded = false;
  orderBy = 'RuleId';
  direction = Direction.ASCENDANT;
  genericUserRole: Readonly<{ appId: ApplicationId; tenantIdentifier: number; roles: Role[] }>;

  private readonly filterChange = new Subject<{ [key: string]: any[] }>();
  private readonly searchChange = new Subject<string>();
  private readonly orderChange = new Subject<void>();

  @Input()
  get connectedUserInfo(): AdminUserProfile {
    return this._connectedUserInfo;
  }

  set connectedUserInfo(userInfo: AdminUserProfile) {
    this._connectedUserInfo = userInfo;
  }

  private _connectedUserInfo: AdminUserProfile;

  constructor(
    public ruleService: RuleService,
    private authService: AuthService,
    private matDialog: MatDialog,
    private translateService: TranslateService,
    private snackBarService: VitamUISnackBarService,
  ) {
    super(ruleService);
    this.genericUserRole = {
      appId: ApplicationId.USERS_APP,
      tenantIdentifier: +this.authService.user.proofTenantIdentifier,
      roles: [Role.ROLE_GENERIC_USERS],
    };
  }

  ngOnInit() {
    this.ruleService.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT)).subscribe((data: Rule[]) => {
      this.dataSource = data;
    });

    const searchCriteriaChange = merge(this.searchChange, this.orderChange, this.filterChange).pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));

    searchCriteriaChange.subscribe(() => {
      const query: any = this.buildRuleCriteriaFromSearch();
      const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
      this.search(pageRequest);
    });
  }

  buildRuleCriteriaFromSearch() {
    const criteria: any = {};
    if (this._searchText.length > 0) {
      criteria.RuleValue = this._searchText;
      criteria.RuleId = this._searchText;
    }

    if (this._filters?.length > 0) {
      criteria.RuleType = this._filters;
    }

    return criteria;
  }

  ngOnDestroy() {
    this.updatedData.unsubscribe();
  }

  searchRuleOrdered() {
    this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT));
  }

  emitOrderChange() {
    this.orderChange.next();
  }

  getRuleType(input: string) {
    if (input) {
      const result = this.ruleTypes.find((x) => x.key.toLowerCase() === input.toLowerCase());
      return result ? result.label : input;
    } else {
      return '';
    }
  }

  getRuleMeasurement(input: string) {
    if (input) {
      const result = this.ruleMeasurements.find((x) => x.key.toLowerCase() === input.toLowerCase());
      return result ? result.label : input;
    } else {
      return '';
    }
  }

  deleteRuleDialog(rule: Rule) {
    const dialog = this.matDialog.open(ConfirmActionComponent, { panelClass: 'vitamui-confirm-dialog' });

    dialog.componentInstance.objectType = this.translateService.instant('RULES_APP.HOME.RULE_MANAGEMENT');
    dialog.componentInstance.objectName = rule.ruleId;

    dialog
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.snackBarService.open({
          message: 'SNACKBAR.RULE_DELETION_START',
          translateParams: { name: rule.ruleId },
        });

        this.ruleService.delete(rule).subscribe(() => this.searchRuleOrdered());
      });
  }

  onFilterChange(key: string, values: string[]) {
    this.filterMap[key] = values;
    // TODO: remplacer les checkbox par des radio button ou gérer plusieurs ruleTypes?
    this._filters = values[0];
    this.filterChange.next(this.filterMap);
  }
}
