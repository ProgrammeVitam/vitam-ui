/* tslint:disable:object-literal-key-quotes quotemark */
import {HttpHeaders} from '@angular/common/http';
import {Component, Input, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AccessContract, IngestContract, SearchUnitApiService} from 'projects/vitamui-library/src/public-api';

import {IngestContractNodeUpdateComponent} from './ingest-contract-nodes-update/ingest-contract-node-update.component';
import {ExternalParametersService, ExternalParameters} from 'ui-frontend-common';
import '@angular/localize/init';

@Component({
  selector: 'app-ingest-contract-attachment-tab',
  templateUrl: './ingest-contract-attachment-tab.component.html',
  styleUrls: ['./ingest-contract-attachment-tab.component.scss']
})
export class IngestContractAttachmentTabComponent implements OnInit {

  submited = false;

  @Input()
  tenantIdentifier: number;

  accessContractId: string;

  accessContracts: AccessContract[];
  titles: any = {};

  // tslint:disable-next-line:variable-name
  private _ingestContract: IngestContract;

  previousValue = (): IngestContract => {
    return this._ingestContract;
  }

  @Input()
  set ingestContract(ingestContract: IngestContract) {
    this._ingestContract = ingestContract;
  }

  get ingestContract(): IngestContract {
    return this._ingestContract;
  }

  @Input()
  set readOnly(readOnly: boolean) {
    console.log('RO:', readOnly);
  }

  constructor(
    private unitService: SearchUnitApiService,
    private externalParameterService: ExternalParametersService,
    private dialog: MatDialog, 
    private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.externalParameterService.getUserExternalParameters().subscribe(parameters => {
      const accessContratId: string = parameters.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
      if (accessContratId && accessContratId.length > 0) {

        this.accessContractId = accessContratId;
        this.initTitles();
      } else {
        this.snackBar.open(
          $localize`:access contrat not set message@@accessContratNotSetErrorMessage:Aucun contrat d'accès n'est associé à l'utiisateur`, 
          null, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000
        });
      }
    });
  }

  initTitles() {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    headers = headers.append('X-Access-Contract-Id', this.accessContractId);
    this.unitService.getByDsl(null, this.getDslForRootNodes(), headers).subscribe(
      response => {
        if (response.httpCode === 200) {
          this.titles = {};
          response.$results.forEach((result: any) => {
            this.titles[result['#id']] = result.Title;
          });
        }

      }
    );
  }

  getDslForRootNodes(): any {
    const linkParentIdAsLinst: string[] = this.ingestContract.linkParentId ? [this.ingestContract.linkParentId] : [];
    const checkParentId: string[] = this.ingestContract.checkParentId ? this.ingestContract.checkParentId : [];

    return {
      "$roots": [],
      "$query": [
        {
          "$and": [
            {
              "$in": {
                "#id": [...checkParentId, ...linkParentIdAsLinst]
              }
            }
          ]
        }
      ],
      "$projection": {
        "$fields": {
          "#id": 1,
          "Title": 1
        }
      }
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
        tenantIdentifier: this.tenantIdentifier
      }
    });
  }

  getTitle(id: string) {
    return this.titles[id] || id;
  }
}
