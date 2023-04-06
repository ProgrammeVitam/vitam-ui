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
/* tslint:disable:object-literal-key-quotes quotemark */
import { HttpHeaders } from '@angular/common/http';
import { Component, Input, OnInit } from '@angular/core';
import '@angular/localize/init';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SearchUnitApiService } from 'projects/vitamui-library/src/public-api';
import { AccessContract, ExternalParameters, ExternalParametersService, IngestContract } from 'ui-frontend-common';
import '@angular/localize/init';
import { ExternalParameters, ExternalParametersService } from 'ui-frontend-common';
import { IngestContractNodeUpdateComponent } from './ingest-contract-nodes-update/ingest-contract-node-update.component';

@Component({
  selector: 'app-ingest-contract-attachment-tab',
  templateUrl: './ingest-contract-attachment-tab.component.html',
  styleUrls: ['./ingest-contract-attachment-tab.component.scss'],
})
export class IngestContractAttachmentTabComponent implements OnInit {
  submited = false;

  @Input()
  tenantIdentifier: number;

  @Input()
  readOnly: boolean;

  accessContractId: string;

  accessContracts: AccessContract[];
  titles: any = {};

  // tslint:disable-next-line:variable-name
  private _ingestContract: IngestContract;

  previousValue = (): IngestContract => {
    return this._ingestContract;
  };

  @Input()
  set ingestContract(ingestContract: IngestContract) {
    this._ingestContract = ingestContract;
  }

  get ingestContract(): IngestContract {
    return this._ingestContract;
  }

  constructor(
    private unitService: SearchUnitApiService,
    private externalParameterService: ExternalParametersService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.externalParameterService.getUserExternalParameters().subscribe((parameters) => {
      const accessContratId: string = parameters.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
      if (accessContratId && accessContratId.length > 0) {
        this.accessContractId = accessContratId;
        this.initTitles();
      } else {
        this.snackBar.open(
          $localize`:access contrat not set message@@accessContratNotSetErrorMessage:Aucun contrat d'accès n'est associé à l'utilisateur`,
          null,
          {
            panelClass: 'vitamui-snack-bar',
            duration: 10000,
          }
        );
      }
    });
  }

  initTitles() {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    headers = headers.append('X-Access-Contract-Id', this.accessContractId);
    this.unitService.getByDsl(null, this.getDslForRootNodes(), headers).subscribe((response) => {
      if (response.httpCode === 200) {
        this.titles = {};
        response.$results.forEach((result: any) => {
          this.titles[result['#id']] = result.Title;
        });
      }
    });
  }

  getDslForRootNodes(): any {
    const linkParentIdAsLinst: string[] = this.ingestContract.linkParentId ? [this.ingestContract.linkParentId] : [];
    const checkParentId: string[] = this.ingestContract.checkParentId ? this.ingestContract.checkParentId : [];

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
    this.dialog.open(IngestContractNodeUpdateComponent, {
      panelClass: 'vitamui-modal',
      disableClose: true,
      data: {
        ingestContract: this.ingestContract,
        accessContractId: this.accessContractId,
        tenantIdentifier: this.tenantIdentifier,
      },
    });
  }

  getTitle(id: string) {
    return this.titles[id] || id;
  }
}
