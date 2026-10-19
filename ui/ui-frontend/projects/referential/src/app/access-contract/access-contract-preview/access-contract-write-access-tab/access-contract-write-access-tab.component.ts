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
import { Component, effect, inject, signal, model, OnChanges, SimpleChanges, untracked } from '@angular/core';
import { form, FormField, FormRoot } from '@angular/forms/signals';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import {
  AccessContract,
  AccessContractService,
  diff,
  Option,
  requiredValue,
  SelectComponent,
  SlideToggleComponent,
  TooltipDirective,
} from 'vitamui-library';
import { MatButtonToggle, MatButtonToggleChange, MatButtonToggleGroup } from '@angular/material/button-toggle';
import { TranslatePipe } from '@ngx-translate/core';

interface AccessContractWriteAccessForm {
  writingPermission: boolean;
  writingAuthorizedDesc: boolean;
  downloadChoose: string;
  everyDataObjectVersion: boolean;
  dataObjectVersion: string[];
}

@Component({
  selector: 'app-access-contract-write-access-tab',
  templateUrl: './access-contract-write-access-tab.component.html',
  styleUrls: ['./access-contract-write-access-tab.component.scss'],
  imports: [
    FormRoot,
    FormField,
    SlideToggleComponent,
    TooltipDirective,
    MatButtonToggleGroup,
    MatButtonToggle,
    SelectComponent,
    TranslatePipe,
  ],
})
export class AccessContractWriteAccessTabComponent implements OnChanges {
  private readonly accessContractService = inject(AccessContractService);

  public usages: Option[] = [
    { key: 'BinaryMaster', label: 'Archives numériques originales', info: '' },
    { key: 'Dissemination', label: 'Copies de diffusion', info: '' },
    { key: 'Thumbnail', label: 'Vignettes', info: '' },
    { key: 'TextContent', label: 'Contenu textuel', info: '' },
    { key: 'PhysicalMaster', label: 'Archives physiques', info: '' },
  ];

  readonly accessContract = model<AccessContract>(undefined);

  readonly form = form(
    signal<AccessContractWriteAccessForm>({
      writingPermission: false,
      writingAuthorizedDesc: false,
      downloadChoose: 'ALL',
      everyDataObjectVersion: true,
      dataObjectVersion: [],
    }),
    (path) => {
      requiredValue(path.dataObjectVersion, { when: ({ valueOf }) => valueOf(path.downloadChoose) === 'SELECTION' });
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
  public submitted = signal(false);

  previousValue = (): any => {
    return {
      writingPermission: this.accessContract().writingPermission,
      writingRestrictedDesc: this.accessContract().writingRestrictedDesc,
      everyDataObjectVersion: this.accessContract().everyDataObjectVersion,
      dataObjectVersion: this.accessContract().dataObjectVersion.sort(),
    };
  };

  constructor() {
    this.onWritingRestrictedDescChanges();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['accessContract']) {
      this.setAccessContract(this.accessContract());
    }
  }

  onDownloadChooseChange({ value }: MatButtonToggleChange): void {
    this.form.everyDataObjectVersion().value.set(value === 'ALL');
  }

  // Only user changes (dirty fields) are propagated, not the values set when resetting the form
  private onWritingRestrictedDescChanges(): void {
    effect(() => {
      const writingAuthorizedDesc = this.form.writingAuthorizedDesc();
      if (writingAuthorizedDesc.dirty() && writingAuthorizedDesc.value()) {
        untracked(() => this.form.writingPermission().value.set(true));
      }
    });

    effect(() => {
      const writingPermission = this.form.writingPermission();
      if (writingPermission.dirty() && !writingPermission.value()) {
        untracked(() => this.form.writingAuthorizedDesc().value.set(false));
      }
    });
  }

  public get unChanged(): boolean {
    return JSON.stringify(diff(this.formDataValue(), this.previousValue())) === '{}';
  }

  public onSubmit(): void {
    this.submitted.set(true);

    this.prepareSubmit().subscribe(
      () => {
        this.accessContractService.get(this.accessContract().identifier).subscribe((response) => {
          this.submitted.set(false);
          this.setAccessContract(response);
        });
      },
      () => {
        this.submitted.set(false);
      },
    );
  }

  private formDataValue(): AccessContract {
    const formValue = this.form().value();
    const accessContractValue: any = {
      ...formValue,
      writingRestrictedDesc: !formValue.writingAuthorizedDesc,
      dataObjectVersion: formValue.downloadChoose === 'SELECTION' ? formValue.dataObjectVersion : [],
    };
    delete accessContractValue.writingAuthorizedDesc;
    delete accessContractValue.downloadChoose;
    return accessContractValue;
  }

  prepareSubmit(): Observable<AccessContract> {
    return of(diff(this.formDataValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.accessContract().id, identifier: this.accessContract().identifier }, formData)),
      switchMap((formData: { id: string; [key: string]: any }) =>
        this.accessContractService.patch(formData).pipe(catchError(() => of(null))),
      ),
    );
  }

  private setAccessContract(accessContract: AccessContract) {
    accessContract.dataObjectVersion = accessContract.dataObjectVersion || [];
    this.accessContract.set(accessContract);

    this.resetForm(this.accessContract());
  }

  resetForm(accessContract: AccessContract): void {
    const downloadChoose = accessContract.everyDataObjectVersion
      ? 'ALL'
      : accessContract.dataObjectVersion?.length > 0
        ? 'SELECTION'
        : 'NONE';

    this.form().reset({
      writingPermission: accessContract.writingPermission ?? null,
      writingAuthorizedDesc: !accessContract.writingRestrictedDesc,
      downloadChoose: downloadChoose,
      everyDataObjectVersion: accessContract.everyDataObjectVersion ?? null,
      dataObjectVersion: accessContract.dataObjectVersion ?? null,
    });
  }
}
