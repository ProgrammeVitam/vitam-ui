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

import { HttpHeaders } from '@angular/common/http';
import { Component, Input } from '@angular/core';
import '@angular/localize/init';
import { MatDialog } from '@angular/material/dialog';
import { SearchUnitApiService } from 'projects/vitamui-library/src/public-api';
import { AccessContract, ExternalParameters, ExternalParametersService, VitamUISnackBarService } from 'ui-frontend-common';
import { AccessContractNodeUpdateComponent } from './access-contract-nodes-update/access-contract-node-update.component';

@Component({
  selector: 'app-access-contract-nodes-tab',
  templateUrl: './access-contract-nodes-tab.component.html',
  styleUrls: ['./access-contract-nodes-tab.component.scss'],
})
export class AccessContractNodesTabComponent {
  @Input() tenantIdentifier: number;
  @Input() set accessContract(accessContract: AccessContract) {

    if(!accessContract.rootUnits){
      accessContract.rootUnits = [];
    }

    if(!accessContract.excludedRootUnits){
      accessContract.excludedRootUnits = [];
    }

    this._accessContract = accessContract;
    this.initSearchAccessContractIdAndTitles();
  }

  get accessContract(): AccessContract {
    return this._accessContract;
  }

  public searchAccessContractId: string;

  // tslint:disable-next-line:variable-name
  private _accessContract: AccessContract;
  rootUnitsTitles: string[] = [];
  excludedRootUnitsTitles: string[] = [];

  constructor(
    private unitService: SearchUnitApiService,
    private externalParameterService: ExternalParametersService,
    private dialog: MatDialog,
    private vitamUISnackBarService: VitamUISnackBarService
  ) {}

  get isAllRootUnitsSelected(): boolean {
    return !this._accessContract.rootUnits || this._accessContract.rootUnits.length === 0;
  }

  public openUpdateSelectedNodes() {
    if (!this.searchAccessContractId) {
      return;
    }

    this.dialog.open(AccessContractNodeUpdateComponent, {
      panelClass: 'vitamui-modal',
      disableClose: true,
      data: {
        accessContract: this._accessContract,
        searchAccessContractId: this.searchAccessContractId,
        tenantIdentifier: this.tenantIdentifier,
      },
    })
    .afterClosed()
    .subscribe((updatedAccessContract: AccessContract) => {
      if (updatedAccessContract) {
        this.accessContract = updatedAccessContract;
      }
    });;
  }

  private initSearchAccessContractIdAndTitles(): void {
    this.externalParameterService.getUserExternalParameters().subscribe((parameters) => {
      const accessContractId: string = parameters.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
      if (accessContractId && accessContractId.length > 0) {
        this.searchAccessContractId = accessContractId;
        this.initTitles();
      } else {
        this.vitamUISnackBarService.open({
          message: 'SNACKBAR.NO_ACCESS_CONTRACT_LINKED',
        });
      }
    });
  }

  private initTitles(): void {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    headers = headers.append('X-Access-Contract-Id', this.searchAccessContractId);
    this.unitService.getByDsl(null, this.getDslForRootNodes(), headers).subscribe((response) => {
      if (response.httpCode === 200) {
        this.rootUnitsTitles = [];
        this.excludedRootUnitsTitles = [];
        response.$results.forEach((result: any) => {
          if (this.accessContract.rootUnits.includes(result['#id'])) {
            this.rootUnitsTitles.push(result.Title);
          }
          if (this.accessContract.excludedRootUnits.includes(result['#id'])) {
            this.excludedRootUnitsTitles.push(result.Title);
          }
        });
        this.rootUnitsTitles.sort();
        this.excludedRootUnitsTitles.sort();
      }
    });
  }

  private getDslForRootNodes(): any {
    const excludedRoots: string[] = this._accessContract.excludedRootUnits ? this._accessContract.excludedRootUnits : [];
    const rootUnits: string[] = this._accessContract.rootUnits ? this._accessContract.rootUnits : [];

    return {
      $roots: [],
      $query: [
        {
          $and: [
            {
              $in: {
                '#id': [...rootUnits, ...excludedRoots],
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
}
