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
import { HttpHeaders } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, input, model, OnChanges, SimpleChanges } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import {
  ExternalParameters,
  ExternalParametersService,
  IngestContract,
  SearchUnitApiService,
  SnackBarService,
  VitamuiHttpHeaders,
} from 'vitamui-library';
import { IngestContractNodeUpdateComponent } from './ingest-contract-nodes-update/ingest-contract-node-update.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-ingest-contract-attachment-tab',
  templateUrl: './ingest-contract-attachment-tab.component.html',
  styleUrls: ['./ingest-contract-attachment-tab.component.scss'],
  imports: [TranslatePipe],
})
export class IngestContractAttachmentTabComponent implements OnChanges {
  private readonly unitService = inject(SearchUnitApiService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly externalParameterService = inject(ExternalParametersService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBarService = inject(SnackBarService);

  readonly tenantIdentifier = input<number>(undefined);
  readonly readOnly = input<boolean>(undefined);

  readonly ingestContract = model<IngestContract>(undefined);

  accessContractId: string;
  linkParentIdTitle: string;
  checkParentIdTitles: string[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['ingestContract']) {
      this.setIngestContract(this.ingestContract());
    }
  }

  private initSearchAccessContractIdAndTitles(): void {
    this.externalParameterService.getUserExternalParameters().subscribe((parameters) => {
      const accessContractId: string = parameters.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
      if (accessContractId && accessContractId.length > 0) {
        this.accessContractId = accessContractId;
        this.initTitles();
        this.cdr.markForCheck();
      } else {
        this.snackBarService.open({ message: 'SNACKBAR.NO_ACCESS_CONTRACT_LINKED' });
      }
    });
  }

  initTitles() {
    let headers = new HttpHeaders()
      .set('Content-Type', 'application/json')
      .set(VitamuiHttpHeaders.X_ACCESS_CONTRACT_ID, this.accessContractId);
    this.unitService.getByDsl(null, this.getDslForRootNodes(), headers).subscribe((response) => {
      if (response.httpCode === 200) {
        this.checkParentIdTitles = [];
        response.$results.forEach((result: any) => {
          if (this.ingestContract().checkParentId?.includes(result['#id'])) {
            this.checkParentIdTitles.push(result.Title);
          }
          if (this.ingestContract().linkParentId === result['#id']) {
            this.linkParentIdTitle = result.Title;
          }
        });
        this.checkParentIdTitles.sort();
        this.cdr.markForCheck();
      }
    });
  }

  getDslForRootNodes(): any {
    const linkParentIdAsLinst: string[] = this.ingestContract().linkParentId ? [this.ingestContract().linkParentId] : [];
    const checkParentId: string[] = this.ingestContract().checkParentId ? this.ingestContract().checkParentId : [];

    return {
      $roots: [],
      $query: [
        {
          $and: [
            {
              $in: {
                '#id': [...checkParentId, ...linkParentIdAsLinst],
              },
            },
          ],
        },
      ],
      $projection: {
        $fields: {
          '#id': 1,
          Title: 1,
        },
      },
    };
  }

  openUpdateSelectedNodes() {
    if (!this.accessContractId) {
      return;
    }

    this.dialog
      .open(IngestContractNodeUpdateComponent, {
        disableClose: true,
        data: {
          ingestContract: this.ingestContract(),
          accessContractId: this.accessContractId,
          tenantIdentifier: this.tenantIdentifier(),
        },
      })
      .afterClosed()
      .subscribe((updatedIngestContract: IngestContract) => {
        if (updatedIngestContract) {
          this.setIngestContract(updatedIngestContract);
          this.cdr.markForCheck();
        }
      });
  }

  private setIngestContract(ingestContract: IngestContract) {
    this.ingestContract.set(ingestContract);
    this.accessContractId = '';
    this.linkParentIdTitle = '';
    this.checkParentIdTitles = [];
    this.initSearchAccessContractIdAndTitles();
  }
}
