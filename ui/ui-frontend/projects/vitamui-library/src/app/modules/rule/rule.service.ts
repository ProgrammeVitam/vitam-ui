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
import { HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { VitamUISnackBarService } from '../../modules/components/vitamui-snack-bar';
import { RuleApiService } from '../api/rule-api.service';
import { Rule } from '../models/rule/rule.interface';
import { SearchService } from '../vitamui-table';

const keySnackbar = 'APPLICATION.RULES_APP.MESSAGES.';

@Injectable({
  providedIn: 'root',
})
export class RuleService extends SearchService<Rule> {
  updated = new Subject<Rule>();

  constructor(
    private ruleApiService: RuleApiService,
    private snackBarService: VitamUISnackBarService,
  ) {
    super(ruleApiService, 'ALL');
  }

  get(ruleId: string): Observable<Rule> {
    return this.ruleApiService.getOne(encodeURI(ruleId));
  }

  getAllForTenant(tenantId: string): Observable<Rule[]> {
    // TODO: Check add of tenantId
    const params = new HttpParams().set('embedded', 'ALL');
    const headers = new HttpHeaders().append('X-Tenant-Id', tenantId);
    return this.ruleApiService.getAllByParams(params, headers);
  }

  existsProperties(properties: { name?: string; ruleId?: string; ruleType?: string }): Observable<any> {
    const rule: any = {};
    if (properties.ruleId) {
      rule.ruleId = properties.ruleId;
    }

    if (properties.ruleType) {
      rule.ruleType = properties.ruleType;
    }

    const ruleObject = rule as Rule;
    return this.ruleApiService.check(ruleObject, this.headers);
  }

  create(rule: Rule): Observable<boolean> {
    return this.ruleApiService.createRule(rule, this.headers).pipe(
      tap(
        (success) => {
          const message = keySnackbar + (success ? 'RULE_CREATION_SUCCESS' : 'RULE_CREATION_FAILED');
          this.snackBarService.open({ message, translateParams: { name: rule.ruleId } });
        },
        (error) => this.snackBarService.open({ message: error.error.message, translate: false }),
      ),
    );
  }

  patch(data: { id: string; [key: string]: any }): Observable<boolean> {
    return this.ruleApiService.patchRule(data).pipe(
      tap(
        (success) => {
          const message = keySnackbar + (success ? 'RULE_UPDATE_SUCCESS' : 'RULE_UPDATE_FAILED');
          this.snackBarService.open({ message, translateParams: { name: data.id } });
        },
        (error) => this.snackBarService.open({ message: error.error.message, translate: false }),
      ),
    );
  }

  delete(rule: Rule): Observable<boolean> {
    return this.ruleApiService.deleteRule(rule.ruleId).pipe(
      tap(
        (success) => {
          const message = keySnackbar + (success ? 'RULE_DELETION_SUCCESS' : 'RULE_DELETION_FAILED');
          this.snackBarService.open({ message, translateParams: { name: rule.ruleId } });
        },
        (error) => this.snackBarService.open({ message: error.error.message, translate: false }),
      ),
    );
  }

  export(): void {
    this.snackBarService.open({ message: `${keySnackbar}EXPORT_IN_PROGRESS` });

    this.ruleApiService.export().subscribe(
      (response) => {
        const a = document.createElement('a');
        document.body.appendChild(a);
        a.style.display = 'none';

        const blob = new Blob([response], { type: 'octet/stream' });
        const url = window.URL.createObjectURL(blob);
        a.href = url;
        a.download = 'rules.csv';
        a.click();
        window.URL.revokeObjectURL(url);
      },
      (error) => this.snackBarService.open({ message: error.error.message, translate: false }),
    );
  }
}