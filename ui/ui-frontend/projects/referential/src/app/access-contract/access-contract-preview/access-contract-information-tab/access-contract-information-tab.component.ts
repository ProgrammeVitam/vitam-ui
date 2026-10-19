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
import { DatePipe } from '@angular/common';
import { Component, inject, linkedSignal, model, OnChanges, output, signal, SimpleChanges } from '@angular/core';
import { disabled, form, FormField, FormRoot, maxLength, minLength, required } from '@angular/forms/signals';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import { AccessContract, AccessContractService, DateTimePipe, diff, InputComponent, Option, SlideToggleComponent } from 'vitamui-library';
import { RULE_TYPES } from '../../../rule/rules.constants';
import { AccessContractCreateValidators } from '../../access-contract-create/access-contract-create.validators';
import { TranslatePipe } from '@ngx-translate/core';

interface AccessContractInformationForm {
  status: string;
  name: string;
  description: string;
  accessLog: string;
  ruleCategoryToFilter: string[];
  creationDate: string;
}

const FORM_KEYS: (keyof AccessContractInformationForm)[] = [
  'status',
  'name',
  'description',
  'accessLog',
  'ruleCategoryToFilter',
  'creationDate',
];

@Component({
  selector: 'app-access-contract-information-tab',
  templateUrl: './access-contract-information-tab.component.html',
  styleUrls: ['./access-contract-information-tab.component.scss'],
  imports: [FormRoot, FormField, SlideToggleComponent, InputComponent, TranslatePipe],
  providers: [DatePipe],
})
export class AccessContractInformationTabComponent implements OnChanges {
  private readonly accessContractService = inject(AccessContractService);
  private readonly accessContractCreateValidators = inject(AccessContractCreateValidators);
  private readonly dateTimePipe = new DateTimePipe(inject(DatePipe));

  readonly accessContract = model<AccessContract>(undefined);

  readonly updatedAccessContract = output<AccessContract>();

  readonly form = form(
    signal<AccessContractInformationForm>({
      status: 'ACTIVE',
      name: '',
      description: null,
      accessLog: 'ACTIVE',
      ruleCategoryToFilter: [],
      creationDate: null,
    }),
    (path) => {
      required(path.name);
      minLength(path.name, 2);
      maxLength(path.name, 100);
      this.accessContractCreateValidators.uniqueNameWhileEdit(path.name, () => (this.accessContract() ? this.previousValue() : undefined));
    },
    {
      submission: {
        action: async () => {
          this.onSubmit();
        },
        ignoreValidators: 'all',
      },
    },
  );

  // Read-only dates, displayed in disabled inputs
  readonly datesForm = form(
    linkedSignal(() => {
      const accessContract = this.accessContract();
      return {
        creationDate: this.formatDate(accessContract?.creationDate),
        lastUpdate: this.formatDate(accessContract?.lastUpdate),
        activationDate:
          accessContract?.status === 'ACTIVE' && accessContract?.activationDate ? this.formatDate(accessContract.activationDate) : '-',
        deactivationDate: accessContract?.deactivationDate ? this.formatDate(accessContract.deactivationDate) : '-',
      };
    }),
    (path) => disabled(path),
  );

  public submitted = false;
  public rules: Option[] = RULE_TYPES;

  previousValue = (): any => {
    return {
      status: this.accessContract().status,
      name: this.accessContract().name,
      description: this.accessContract().description,
      accessLog: this.accessContract().accessLog,
      ruleCategoryToFilter: this.accessContract().ruleCategoryToFilter,
      creationDate: this.accessContract().creationDate,
    };
  };

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['accessContract']) {
      this.setAccessContract(this.accessContract());
      this.resetForm(this.accessContract());
    }
  }

  public onSubmit() {
    this.submitted = true;
    if (this.isInvalid()) {
      return;
    }
    this.prepareSubmit().subscribe(
      () => {
        this.accessContractService.get(this.accessContract().identifier).subscribe((response) => {
          this.submitted = false;
          this.setAccessContract(response);
          this.resetForm(this.accessContract());
          this.updatedAccessContract.emit(response);
        });
      },
      () => {
        this.submitted = false;
      },
    );
  }

  public get unChanged(): boolean {
    const { status, accessLog } = this.form().value();
    return (
      JSON.stringify(diff(this.form().value(), this.previousValue())) === '{}' &&
      (status === 'ACTIVE' ? 'ACTIVE' : 'INACTIVE') === this.previousValue().status &&
      (accessLog === 'ACTIVE' ? 'ACTIVE' : 'INACTIVE') === this.previousValue().accessLog
    );
  }

  public isInvalid(): boolean {
    return [this.form.name, this.form.description, this.form.status, this.form.accessLog].some(
      (field) => field().invalid() || field().pending(),
    );
  }

  prepareSubmit(): Observable<AccessContract> {
    return of(diff(this.form().value(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.accessContract().id, identifier: this.accessContract().identifier }, formData)),
      switchMap((formData: { id: string; [key: string]: any }) => {
        if (formData['status']) {
          if (formData['status'] === 'ACTIVE') {
            formData['activationDate'] = new Date();
          } else {
            formData['status'] = 'INACTIVE';
            formData['deactivationDate'] = new Date();
          }
        }
        return this.accessContractService.patch(formData).pipe(catchError(() => of(null)));
      }),
    );
  }

  resetForm(accessContract: AccessContract) {
    this.form().reset(
      FORM_KEYS.reduce((acc: any, key) => {
        acc[key] = (accessContract as any)[key] ?? null;
        return acc;
      }, {}) as AccessContractInformationForm,
    );
  }

  onStatusChange(active: boolean) {
    this.form.status().value.set(active ? 'ACTIVE' : 'INACTIVE');
  }

  onAccessLogChange(active: boolean) {
    this.form.accessLog().value.set(active ? 'ACTIVE' : 'INACTIVE');
  }

  private formatDate(date: string): string {
    return this.dateTimePipe.transform(date, 'dd/MM/yyyy');
  }

  private setAccessContract(accessContract: AccessContract): void {
    this.accessContract.set(accessContract);

    if (!this.accessContract().ruleCategoryToFilter) {
      this.accessContract().ruleCategoryToFilter = [];
    }

    if (!this.accessContract().description) {
      this.accessContract().description = '';
    }
  }
}
