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
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Rule} from 'projects/vitamui-library/src/lib/models/rule';
import {Observable, Subject} from 'rxjs';
import {tap} from 'rxjs/operators';
import {SearchService, VitamUISnackBar} from 'ui-frontend-common';
import {RuleApiService} from '../core/api/rule-api.service';
import {VitamUISnackBarComponent} from '../shared/vitamui-snack-bar';

@Injectable({
  providedIn: 'root'
})

export class RuleService extends SearchService<Rule> {

  updated = new Subject<Rule>();

  constructor(
    private ruleApiService: RuleApiService,
    private snackBar: VitamUISnackBar,
    http: HttpClient) {
    super(http, ruleApiService, 'ALL');
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

  existsProperties(properties: {name?: string, ruleId?: string}): Observable<any> {
    const rule: any = {};
    if (properties.ruleId) {
      rule.ruleId = properties.ruleId;
    }

    const objectRule = rule as Rule;
    return this.ruleApiService.check(objectRule, this.headers);
  }

  create(rule: Rule) {
    return this.ruleApiService.create(rule, this.headers)
      .pipe(
        tap(
          () => {
            this.snackBar.openFromComponent(VitamUISnackBarComponent, {
              panelClass: 'vitamui-snack-bar',
              data: {type: 'ruleCreate', name: rule.ruleId},
              duration: 10000
            });
          },
          (error: any) => {
            this.snackBar.open(error.error.message, null, {
              panelClass: 'vitamui-snack-bar',
              duration: 10000
            });
          }
        )
      );
  }

  patch(data: {id: string, [key: string]: any}): Observable<Rule> {
    console.log(data);
    return this.ruleApiService.patch(data)
      .pipe(
        tap((response) => this.updated.next(response)),
        tap(
          () => {
            this.snackBar.openFromComponent(VitamUISnackBarComponent, {
              panelClass: 'vitamui-snack-bar',
              duration: 10000,
              data: {type: 'ruleUpdate', name: data.ruleId}
            });
          },
          (error) => {
            this.snackBar.open(error.error.message, null, {
              panelClass: 'vitamui-snack-bar',
              duration: 10000
            });
          }
        )
      );
  }

  delete(rule: Rule): Observable<any> {
    return this.ruleApiService.delete(rule.ruleId).pipe(
      tap(() => {
        this.snackBar.openFromComponent(VitamUISnackBarComponent, {
          panelClass: 'vitamui-snack-bar',
          duration: 10000,
          data: {type: 'ruleDelete', name: rule.ruleId}
        });
      },
        (error) => {
          this.snackBar.open(error.error.message, null, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000
          });
        })
    );
  }

  export() {
    this.snackBar.openFromComponent(VitamUISnackBarComponent, {
      panelClass: 'vitamui-snack-bar',
      duration: 10000,
      data: {type: 'ruleExportAll'}
    });

    this.ruleApiService.export().subscribe(
      (response) => {
        const a = document.createElement('a');
        document.body.appendChild(a);
        a.style.display = 'none';

        const blob = new Blob([response], {type: 'octet/stream'});
        const url = window.URL.createObjectURL(blob);
        a.href = url;
        a.download = 'rules.csv';
        a.click();
        window.URL.revokeObjectURL(url);
      }, (error) => {
        this.snackBar.open(error.error.message, null, {
          panelClass: 'vitamui-snack-bar',
          duration: 10000
        });
      }
    );
  }

}
