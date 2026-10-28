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
import { Component, inject, signal } from '@angular/core';
import { form, FormField, FormRoot } from '@angular/forms/signals';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef } from '@angular/material/dialog';
import {
  DialogHeaderComponent,
  FilingPlanComponent,
  FilingPlanMode,
  IngestContract,
  NextStepComponent,
  PreviousStepComponent,
  StepperComponent,
  TooltipDirective,
} from 'vitamui-library';
import { IngestContractService } from '../../../ingest-contract.service';
import { CdkStep } from '@angular/cdk/stepper';
import { MatButtonToggle, MatButtonToggleGroup } from '@angular/material/button-toggle';
import { TranslatePipe } from '@ngx-translate/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { MatTreeModule } from '@angular/material/tree';
import { CommonModule } from '@angular/common';
import { MatCheckboxModule } from '@angular/material/checkbox';

interface FilingPlanSelection {
  included: string[];
  excluded: string[];
}

interface SelectNodesForm {
  linkParentId: FilingPlanSelection;
  checkParentId: FilingPlanSelection;
  checkParentLink: string;
}

@Component({
  selector: 'app-ingest-contract-node-update',
  templateUrl: './ingest-contract-node-update.component.html',
  styleUrls: ['./ingest-contract-node-update.component.scss'],
  imports: [
    DialogHeaderComponent,
    FormRoot,
    FormField,
    StepperComponent,
    CdkStep,
    MatDialogContent,
    MatDialogActions,
    NextStepComponent,
    MatButtonToggleGroup,
    MatButtonToggle,
    TooltipDirective,
    PreviousStepComponent,
    TranslatePipe,
    CommonModule,
    FilingPlanComponent,
    MatButtonModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatTreeModule,
  ],
})
export class IngestContractNodeUpdateComponent {
  dialogRef = inject<MatDialogRef<IngestContractNodeUpdateComponent>>(MatDialogRef);
  data = inject<{
    ingestContract: IngestContract;
    accessContractId: string;
    tenantIdentifier: number;
  }>(MAT_DIALOG_DATA);
  private readonly ingestContractService = inject(IngestContractService);

  ingestContract: IngestContract;
  accessContractId: string;
  tenantIdentifier: number;
  readonly selectNodesForm = form(
    signal<SelectNodesForm>({
      linkParentId: { included: [], excluded: [] },
      checkParentId: { included: [], excluded: [] },
      checkParentLink: 'AUTHORIZED',
    }),
    {
      submission: {
        action: async () => {
          this.updateIngestContractNodes();
        },
        ignoreValidators: 'all',
      },
    },
  );

  hasError = true;
  message: string;

  FILING_PLAN_MODE_SOLO = FilingPlanMode.SOLO;
  FILING_PLAN_MODE_INC = FilingPlanMode.INCLUDE_ONLY;

  constructor() {
    this.accessContractId = this.data.accessContractId;
    this.ingestContract = this.data.ingestContract;
    this.tenantIdentifier = this.data.tenantIdentifier;
    this.selectNodesForm().value.set({
      linkParentId: this.ingestContract.linkParentId
        ? { included: [this.ingestContract.linkParentId], excluded: [] }
        : { included: [], excluded: [] },
      checkParentId: this.ingestContract.checkParentId
        ? { included: this.ingestContract.checkParentId, excluded: [] }
        : { included: [], excluded: [] },
      checkParentLink: this.ingestContract.checkParentLink ? this.ingestContract.checkParentLink : 'AUTHORIZED',
    });
  }

  onCancel() {
    this.dialogRef.close();
  }

  updateIngestContractNodes() {
    const { linkParentId, checkParentId, checkParentLink } = this.selectNodesForm().value();
    const formData = {
      id: this.ingestContract.id,
      identifier: this.ingestContract.identifier,
      checkParentId: checkParentId.included,
      linkParentId: linkParentId.included.length > 0 ? linkParentId.included[0] : '',
      checkParentLink,
    };

    this.ingestContractService.patch(formData).subscribe((updatedIngestContract) => {
      this.dialogRef.close(updatedIngestContract);
    });
  }
}
