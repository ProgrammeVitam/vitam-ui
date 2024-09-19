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
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { AccessContract, Option, VitamuiAutocompleteMultiselectOptions } from 'vitamui-library';
import { AgencyService } from '../../../agency/agency.service';
import { AccessRightType, accessRightTypeOf } from '../../../../../../vitamui-library/src/lib/models/access-contract.interface';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { AccessContractAuthorizationsUpdateComponent } from './access-contract-authorizations-update/access-contract-authorizations-update.component';

@Component({
  selector: 'app-access-contract-authorizations-tab',
  templateUrl: './access-contract-authorizations-tab.component.html',
  styleUrls: ['./access-contract-authorizations-tab.component.scss'],
})
export class AccessContractAuthorizationsTabComponent implements OnInit {
  AccessRightType = AccessRightType;

  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() isFormValid: EventEmitter<boolean> = new EventEmitter<boolean>();

  public form: FormGroup;
  public isLoading = false;
  public originatingAgenciesOptions: VitamuiAutocompleteMultiselectOptions;
  public _accessContract: AccessContract;

  get accessContract(): AccessContract {
    return this._accessContract;
  }

  @Input() set accessContract(accessContract: AccessContract) {
    if (!accessContract.originatingAgencies) {
      accessContract.originatingAgencies = [];
    }
    this._accessContract = {
      ...accessContract,
      accessRightType: accessRightTypeOf(accessContract),
      originatingAgencies: accessContract.originatingAgencies?.sort(),
    };
  }

  constructor(
    private agencyService: AgencyService,
    private dialog: MatDialog,
  ) {}

  ngOnInit(): void {
    this.agencyService.getOriginatingAgenciesAsOptions().subscribe((options: Option[]) => (this.originatingAgenciesOptions = { options }));
  }

  public openModalCreateAccessContractStep2Only() {
    const dialogRef = this.dialog.open(AccessContractAuthorizationsUpdateComponent, {
      panelClass: 'vitamui-modal',
      disableClose: true,
      data: {
        accessContract: this._accessContract,
      },
    });
    dialogRef.afterClosed().subscribe((accessContract: any) => {
      if (accessContract) {
        this.accessContract = accessContract;
      }
    });
  }

  originatingAgencyName(originatingAgency: string): string {
    return this.originatingAgenciesOptions?.options.filter((opt) => opt.key === originatingAgency).at(0).label;
  }
}
