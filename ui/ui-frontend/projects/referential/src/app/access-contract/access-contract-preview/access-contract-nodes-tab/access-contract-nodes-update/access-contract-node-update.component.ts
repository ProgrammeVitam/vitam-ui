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
import { Component, inject, signal, viewChild } from '@angular/core';
import { form, FormField, FormRoot } from '@angular/forms/signals';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef } from '@angular/material/dialog';
import {
  AccessContract,
  AccessContractService,
  DialogHeaderComponent,
  FilingPlanComponent,
  FilingPlanMode,
  SlideToggleComponent,
  TooltipDirective,
} from 'vitamui-library';
import { TranslatePipe } from '@ngx-translate/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { MatTreeModule } from '@angular/material/tree';
import { CommonModule } from '@angular/common';
import { MatCheckboxModule } from '@angular/material/checkbox';

interface SelectedNodes {
  included: string[];
  excluded: string[];
}

@Component({
  selector: 'app-access-contract-node-update',
  templateUrl: './access-contract-node-update.component.html',
  styleUrls: ['./access-contract-node-update.component.scss'],
  imports: [
    DialogHeaderComponent,
    FormRoot,
    FormField,
    MatDialogContent,
    SlideToggleComponent,
    TooltipDirective,
    MatDialogActions,
    TranslatePipe,
    CommonModule,
    FilingPlanComponent,
    MatButtonModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatTreeModule,
  ],
})
export class AccessContractNodeUpdateComponent {
  dialogRef = inject<MatDialogRef<AccessContractNodeUpdateComponent>>(MatDialogRef);
  data = inject<{
    accessContract: AccessContract;
    searchAccessContractId: string;
    tenantIdentifier: number;
  }>(MAT_DIALOG_DATA);
  private readonly accessContractService = inject(AccessContractService);

  accessContract: AccessContract;
  tenantIdentifier: number;
  readonly selectNodesForm = form(
    signal<{ allRootUnits: boolean; selectedRoots: SelectedNodes }>({
      allRootUnits: false,
      selectedRoots: { included: [], excluded: [] },
    }),
    {
      submission: {
        action: async () => {
          this.updateAccessContractNodes();
        },
        ignoreValidators: 'all',
      },
    },
  );

  hasError = true;
  message: string;

  FILLING_PLAN_MODE_BOTH = FilingPlanMode.BOTH;

  readonly fileSearch = viewChild<any>('fileSearch');

  constructor() {
    this.tenantIdentifier = this.data.tenantIdentifier;
    this.accessContract = this.data.accessContract;

    const rootUnits: string[] = this.accessContract.rootUnits || [];
    this.selectNodesForm().value.set({
      allRootUnits: rootUnits.length === 0,
      selectedRoots: { included: rootUnits, excluded: this.accessContract.excludedRootUnits || [] },
    });
  }

  onCancel() {
    this.dialogRef.close();
  }

  isInvalid(): boolean {
    const { allRootUnits, selectedRoots } = this.selectNodesForm().value();
    return allRootUnits === false && (!selectedRoots?.included || selectedRoots.included.length === 0);
  }

  updateAccessContractNodes() {
    const { allRootUnits, selectedRoots } = this.selectNodesForm().value();
    const rootUnits: string[] = allRootUnits ? [] : selectedRoots?.included;
    const excludedRootUnits: string[] = allRootUnits ? [] : selectedRoots?.excluded;

    const formData = {
      id: this.accessContract.id,
      identifier: this.accessContract.identifier,
      rootUnits,
      excludedRootUnits,
    };

    this.accessContractService.patch(formData).subscribe((updatedAccessContract) => {
      this.dialogRef.close(updatedAccessContract);
    });
  }
}
