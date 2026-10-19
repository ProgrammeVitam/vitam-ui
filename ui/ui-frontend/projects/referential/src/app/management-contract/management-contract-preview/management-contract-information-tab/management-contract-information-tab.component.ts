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
import { Component, inject, output, model, OnChanges, signal, SimpleChanges } from '@angular/core';
import { disabled, form, FormField, FormRoot, required } from '@angular/forms/signals';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import { diff, InputComponent, ManagementContract, PipesModule, SlideToggleComponent } from 'vitamui-library';
import { ManagementContractService } from '../../management-contract.service';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { TranslatePipe } from '@ngx-translate/core';

interface ManagementContractInformationForm {
  identifier: string;
  name: string;
  description: string;
  status: boolean;
}

@Component({
  selector: 'app-management-contract-information-tab',
  templateUrl: './management-contract-information-tab.component.html',
  styleUrls: ['./management-contract-information-tab.component.scss'],
  imports: [FormRoot, FormField, SlideToggleComponent, InputComponent, MatProgressSpinner, PipesModule, TranslatePipe],
})
export class ManagementContractInformationTabComponent implements OnChanges {
  private readonly managementContractService = inject(ManagementContractService);

  readonly updated = output<boolean>();

  readonly form = form(
    signal<ManagementContractInformationForm>({ identifier: null, name: null, description: '', status: null }),
    (path) => {
      required(path.identifier);
      disabled(path.identifier);
      required(path.name);
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
  submitting = false;

  readonly inputManagementContract = model<ManagementContract>(undefined);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['inputManagementContract']) {
      this.setInputManagementContract(this.inputManagementContract());
    }
  }

  previousValue = (): any => {
    return {
      id: this.inputManagementContract().id,
      identifier: this.inputManagementContract().identifier,
      name: this.inputManagementContract().name,
      description: this.inputManagementContract().description,
      status: this.inputManagementContract().status === 'ACTIVE',
    };
  };

  unchanged(): boolean {
    const differences = diff(this.form().value(), this.previousValue());
    const unchanged = JSON.stringify(differences) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  prepareSubmit(): Observable<ManagementContract> {
    return of(diff(this.form().value(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.previousValue().id, identifier: this.previousValue().identifier }, formData)),
      switchMap((formData: { id: string; [key: string]: any }) => {
        // Update the activation and deactivation dates if the contract status has changed before sending the data
        const partialManagementContract = { ...formData };
        const date = new Date().toISOString();
        const statusIsConsistent = formData['status'] !== undefined && formData['status'] !== null;

        if (statusIsConsistent && formData['status']) {
          partialManagementContract['activationDate'] = date;
          partialManagementContract['deactivationDate'] = null;
          partialManagementContract['status'] = 'ACTIVE';
        }

        if (statusIsConsistent && !formData['status']) {
          partialManagementContract['activationDate'] = null;
          partialManagementContract['deactivationDate'] = date;
          partialManagementContract['status'] = 'INACTIVE';
        }

        return this.managementContractService.patch(partialManagementContract).pipe(catchError(() => of(null)));
      }),
    );
  }

  onSubmit() {
    this.submitting = true;
    this.prepareSubmit().subscribe(
      () => {
        this.managementContractService.get(this.inputManagementContract().identifier).subscribe((response) => {
          this.submitting = false;
          this.setInputManagementContract(response);
        });
      },
      () => {
        this.submitting = false;
      },
    );
  }

  private setInputManagementContract(managementContract: ManagementContract) {
    this.inputManagementContract.set(managementContract);

    if (!managementContract.description) {
      this.inputManagementContract().description = '';
    }

    this.resetForm(this.inputManagementContract());
    this.updated.emit(false);
  }

  resetForm(managementContract: ManagementContract) {
    this.form().reset({
      identifier: managementContract.identifier ?? null,
      name: managementContract.name ?? null,
      description: managementContract.description ?? null,
      status: managementContract.status === 'ACTIVE',
    });
  }
}
