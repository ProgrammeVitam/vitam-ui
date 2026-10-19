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
import { HttpHeaders, HttpParams } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, input, output, model, OnChanges, SimpleChanges, linkedSignal, signal } from '@angular/core';
import { disabled, form, FormField, FormRoot, required } from '@angular/forms/signals';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import {
  DateTimePipe,
  diff,
  IngestContract,
  InputComponent,
  Option,
  PipesModule,
  SelectComponent,
  SlideToggleComponent,
  VitamuiHttpHeaders,
} from 'vitamui-library';

import { ArchiveProfileApiService } from '../../../core/api/archive-profile-api.service';
import { ManagementContractApiService } from '../../../core/api/management-contract-api.service';
import { IngestContractCreateValidators } from '../../ingest-contract-create/ingest-contract-create.validators';
import { IngestContractService } from '../../ingest-contract.service';
import { TranslatePipe } from '@ngx-translate/core';

interface IngestContractInformationForm {
  identifier: string;
  status: string;
  name: string;
  description: string;
  archiveProfiles: string[];
  managementContractId: string;
}

const FORM_KEYS: (keyof IngestContractInformationForm)[] = [
  'identifier',
  'status',
  'name',
  'description',
  'archiveProfiles',
  'managementContractId',
];

@Component({
  selector: 'app-ingest-contract-information-tab',
  templateUrl: './ingest-contract-information-tab.component.html',
  styleUrls: ['./ingest-contract-information-tab.component.scss'],
  imports: [FormRoot, FormField, SlideToggleComponent, InputComponent, SelectComponent, PipesModule, TranslatePipe],
  providers: [DatePipe, DateTimePipe],
})
export class IngestContractInformationTabComponent implements OnChanges, OnInit {
  private readonly ingestContractService = inject(IngestContractService);
  private readonly managementContractService = inject(ManagementContractApiService);
  private readonly archiveProfileService = inject(ArchiveProfileApiService);
  private readonly ingestContractCreateValidators = inject(IngestContractCreateValidators);
  private readonly dateTimePipe = inject(DateTimePipe);

  readonly updated = output<boolean>();
  readonly updatedIngestContract = output<IngestContract>();
  readonly isFormValid = output<boolean>();
  readonly tenantIdentifier = input<number>(undefined);

  readonly ingestContract = model<IngestContract>(undefined);

  readonly readOnly = input<boolean>(undefined);

  readonly form = form(
    signal<IngestContractInformationForm>(this.toFormValue({} as IngestContract)),
    (path) => {
      required(path.identifier);
      required(path.name);
      this.ingestContractCreateValidators.uniqueNameWhileEdit(path.name, () => this.ingestContract());
      required(path.description);
      disabled(path, () => !!this.readOnly());
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
  protected readonly datesForm = form(
    linkedSignal(() => {
      const ingestContract = this.ingestContract();
      return {
        creationDate: this.formatDate(ingestContract?.creationDate),
        lastUpdate: this.formatDate(ingestContract?.lastUpdate),
        activationDate: this.formatDate(ingestContract?.status === 'ACTIVE' ? ingestContract?.activationDate : null),
        deactivationDate: this.formatDate(ingestContract?.deactivationDate),
      };
    }),
    (path) => disabled(path),
  );

  submitted = false;

  managementContracts: Option[];
  archiveProfiles: Option[];

  previousValue = (): any => {
    return {
      identifier: this.ingestContract().identifier,
      status: this.ingestContract().status,
      name: this.ingestContract().name,
      description: this.ingestContract().description,
      archiveProfiles: this.ingestContract().archiveProfiles,
      managementContractId: this.ingestContract().managementContractId,
    };
  };

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['ingestContract']) {
      this.setIngestContract(this.ingestContract());
    }
  }

  private toFormValue(ingestContract: IngestContract): IngestContractInformationForm {
    return FORM_KEYS.reduce((acc: any, key) => {
      acc[key] = (ingestContract as any)[key] ?? null;
      return acc;
    }, {}) as IngestContractInformationForm;
  }

  private formatDate(date: string): string {
    return date ? this.dateTimePipe.transform(date, 'dd/MM/yyyy') : '-';
  }

  ngOnInit(): void {
    const params = new HttpParams().set('embedded', 'ALL');
    const headers = new HttpHeaders().set(VitamuiHttpHeaders.X_TENANT_ID, '' + this.tenantIdentifier());

    this.managementContractService.getAllByParams(params, headers).subscribe((managmentContracts) => {
      this.managementContracts = managmentContracts.map((managementContract) => ({
        key: managementContract.identifier,
        label: managementContract.name,
      }));
    });

    this.archiveProfileService.getAllByParams(params, headers).subscribe((archiveProfiles) => {
      this.archiveProfiles = archiveProfiles.map((archiveProfile) => ({
        key: archiveProfile.identifier,
        label: archiveProfile.name,
      }));
    });
  }

  unchanged(): boolean {
    const currentFormValue = {
      ...this.form().value(),
      managementContractId: this.form.managementContractId().value() || '',
    };
    const unchanged = JSON.stringify(diff(currentFormValue, this.previousValue())) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  isInvalid(): boolean {
    const isInvalid = [this.form.name, this.form.description, this.form.status, this.form.archiveProfiles].some(
      (field) => field().invalid() || field().pending(),
    );
    this.isFormValid.emit(!isInvalid);
    return isInvalid;
  }

  prepareSubmit(): Observable<IngestContract> {
    return of(diff(this.form().value(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.ingestContract().id, identifier: this.ingestContract().identifier }, formData)),
      switchMap((formData: { id: string; [key: string]: any }) => {
        // Update the activation and deactivation dates if the contract status has changed before sending the data
        if (formData['status']) {
          if (formData['status'] === 'ACTIVE') {
            formData['activationDate'] = new Date();
            formData['deactivationDate'] = null;
          } else {
            formData['status'] = 'INACTIVE';
            formData['activationDate'] = null;
            formData['deactivationDate'] = new Date();
          }
        }
        // Set management contract ID to null if undefined
        if (formData['managementContractId'] === undefined) {
          formData['managementContractId'] = null;
        }
        return this.ingestContractService.patch(formData).pipe(catchError(() => of(null)));
      }),
    );
  }

  onSubmit() {
    this.submitted = true;
    if (this.isInvalid()) {
      return;
    }
    this.prepareSubmit().subscribe(
      () => {
        this.ingestContractService.get(this.ingestContract().identifier).subscribe((response) => {
          this.submitted = false;
          this.setIngestContract(response);
          this.updatedIngestContract.emit(response);
        });
      },
      () => {
        this.submitted = false;
      },
    );
  }

  private setIngestContract(ingestContract: IngestContract) {
    if (!ingestContract.managementContractId) {
      ingestContract.managementContractId = '';
    }
    if (!ingestContract.name) {
      ingestContract.name = '';
    }
    if (!ingestContract.description) {
      ingestContract.description = '';
    }
    this.ingestContract.set(ingestContract);
    this.resetForm(this.ingestContract());
    this.updated.emit(false);
  }

  resetForm(ingestContract: IngestContract) {
    this.form().reset(this.toFormValue(ingestContract));
  }

  onStatusChange(active: boolean) {
    this.form.status().value.set(active ? 'ACTIVE' : 'INACTIVE');
  }
}
