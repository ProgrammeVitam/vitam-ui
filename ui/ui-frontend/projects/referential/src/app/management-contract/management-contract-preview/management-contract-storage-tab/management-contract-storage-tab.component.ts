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
import { Component, OnDestroy, inject, signal, output, model, OnChanges, SimpleChanges } from '@angular/core';
import { form, FormField, FormRoot, required } from '@angular/forms/signals';
import { Observable, of, Subscription } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import { diff, InputComponent, ManagementContract, StorageStrategy } from 'vitamui-library';
import { ManagementContractService } from '../../management-contract.service';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-management-contract-storage-tab',
  templateUrl: './management-contract-storage-tab.component.html',
  styleUrls: ['./management-contract-storage-tab.component.scss'],
  imports: [FormRoot, FormField, InputComponent, MatProgressSpinner, TranslatePipe],
})
export class ManagementContractStorageTabComponent implements OnChanges, OnDestroy {
  private readonly managementContractService = inject(ManagementContractService);

  readonly updated = output<boolean>();
  readonly form = form(
    signal<StorageStrategy>({ unitStrategy: null, objectGroupStrategy: null, objectStrategy: null }),
    (path) => {
      required(path.unitStrategy);
      required(path.objectGroupStrategy);
      required(path.objectStrategy);
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
  submited = signal(false);
  showSpinner = signal(false);
  storageStrategy: StorageStrategy = {
    unitStrategy: '',
    objectGroupStrategy: '',
    objectStrategy: '',
  };

  statusControlValueChangesSubscribe: Subscription;

  readonly inputManagementContract = model<ManagementContract>(undefined);

  previousValue = (): StorageStrategy => {
    return this.inputManagementContract()?.storage;
  };

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['inputManagementContract']) {
      this.setInputManagementContract(this.inputManagementContract());
    }
  }

  unchanged(): boolean {
    // if (this.previousValue() !== undefined) {
    const unchanged = JSON.stringify(diff(this.form().value(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
    // }
  }

  prepareSubmit(): Observable<ManagementContract> {
    return of(diff(this.form().value(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.inputManagementContract().id, identifier: this.inputManagementContract().identifier }, formData)),
      switchMap((formData: { id: string; [key: string]: any }) => {
        return this.managementContractService.patch(formData).pipe(catchError(() => of(null)));
      }),
    );
  }

  onSubmit() {
    this.submited.set(true);
    this.showSpinner.set(true);
    this.prepareSubmit().subscribe(
      () => {
        this.managementContractService.get(this.inputManagementContract().identifier).subscribe((response) => {
          this.submited.set(false);
          this.showSpinner.set(false);
          this.setInputManagementContract(response);
        });
      },
      () => {
        this.submited.set(false);
        this.showSpinner.set(false);
      },
    );
  }

  private setInputManagementContract(managementContract: ManagementContract) {
    if (managementContract.storage && !managementContract.storage.unitStrategy) {
      managementContract.storage.unitStrategy = '';
    }
    if (managementContract.storage && !managementContract.storage.objectGroupStrategy) {
      managementContract.storage.objectGroupStrategy = '';
    }
    if (managementContract.storage && !managementContract.storage.objectStrategy) {
      managementContract.storage.objectStrategy = '';
    }

    if (managementContract.storage === undefined) {
      managementContract.storage = this.storageStrategy;
    }

    this.inputManagementContract.set(managementContract);

    this.resetForm(this.inputManagementContract());
    this.updated.emit(false);
  }

  resetForm(managementContract: ManagementContract) {
    const storage = managementContract.storage;
    this.form().reset({
      unitStrategy: storage?.unitStrategy ?? null,
      objectGroupStrategy: storage?.objectGroupStrategy ?? null,
      objectStrategy: storage?.objectStrategy ?? null,
    });
  }

  ngOnDestroy() {
    this.statusControlValueChangesSubscribe?.unsubscribe();
  }
}
