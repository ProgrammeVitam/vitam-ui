/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { Clipboard } from '@angular/cdk/clipboard';
import { ChangeDetectorRef, Component, inject, signal } from '@angular/core';
import { form, FormField, FormRoot, maxLength, minLength, required } from '@angular/forms/signals';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import {
  AccessContractService,
  DslQueryType,
  InputComponent,
  Option,
  SelectComponent,
  SnackBarService,
  VitamuiTitleBreadcrumbComponent,
} from 'vitamui-library';
import { AdminDslService } from './admin-dsl.service';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { MatSidenav, MatSidenavContainer, MatSidenavContent } from '@angular/material/sidenav';

interface AdminDslForm {
  dslQueryType: DslQueryType;
  id: string;
  accessContract: string;
  dsl: string;
  response: string;
}

@Component({
  selector: 'app-admin-dsl',
  templateUrl: './admin-dsl.component.html',
  styleUrls: ['./admin-dsl.component.scss'],
  imports: [
    MatSidenavContainer,
    MatSidenav,
    MatSidenavContent,
    VitamuiTitleBreadcrumbComponent,
    FormRoot,
    FormField,
    SelectComponent,
    InputComponent,
    TranslatePipe,
  ],
})
export class AdminDslComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly adminDslService = inject(AdminDslService);
  private readonly snackBarService = inject(SnackBarService);
  private readonly accessContractService = inject(AccessContractService);
  private readonly clipboard = inject(Clipboard);
  private readonly translateService = inject(TranslateService);

  tenantId: number;
  readonly form = form(
    signal<AdminDslForm>({ dslQueryType: null, id: null, accessContract: null, dsl: null, response: null }),
    (path) => {
      required(path.dslQueryType);
      required(path.id, { when: () => this.isUnitIdRequired() });
      minLength(path.id, 36);
      maxLength(path.id, 36);
      required(path.accessContract);
      required(path.dsl);
    },
    {
      submission: {
        action: async () => {
          this.search();
        },
        ignoreValidators: 'all',
      },
    },
  );
  accessContracts: Option[] = [];
  dslQueryTypeOptions = Object.values(DslQueryType).map((queryType) => ({
    key: queryType,
    label: this.translateService.instant(`DSL.HOME.${queryType}`),
  }));

  constructor() {
    this.route.params.subscribe((params) => {
      if (params['tenantIdentifier']) {
        this.tenantId = params['tenantIdentifier'];
        this.accessContractService.getAllForTenant('' + this.tenantId).subscribe((accessContracts) => {
          this.accessContracts = accessContracts.map((accessContract) => ({
            key: accessContract.identifier,
            label: accessContract.name,
          }));
          this.cdr.markForCheck();
        });
      }
    });
  }

  search() {
    try {
      const value = this.form().value();
      const searchObservable: Observable<any> =
        value.dslQueryType === DslQueryType.ARCHIVE_UNIT
          ? this.adminDslService.getByDsl(value.id, JSON.parse(value.dsl), value.accessContract)
          : this.adminDslService.getUnitObjectsByDsl(value.id, JSON.parse(value.dsl), value.accessContract);

      searchObservable.subscribe(
        (response: any) => {
          if (response.httpCode === 400) {
            this.form.response().value.set(response.description);
          } else {
            this.form.response().value.set(JSON.stringify(response, null, 2));
          }
        },
        (error: any) => {
          console.log(error);
        },
      );
    } catch (syntaxError) {
      this.snackBarService.open({ message: 'SNACKBAR.INVALID_DSL_REQUEST_FORMAT' });
    }
  }

  checkDsl() {
    const dsl = this.form.dsl().value();
    try {
      return dsl.length > 1 && !!JSON.parse(dsl);
    } catch (syntaxError) {
      this.snackBarService.open({
        message: 'SNACKBAR.FORMAT_INVALID',
      });
      return false;
    }
  }

  copyToClipboard(value: string) {
    this.clipboard.copy(value);
  }

  clear() {
    this.form.response().reset(null);
  }

  /**
   * Check if the unit id is required according to the selected dsl query type
   */
  isUnitIdRequired(): boolean {
    return this.form.dslQueryType().value() === DslQueryType.TECHNICAL_OBJECT_GROUP;
  }
}
