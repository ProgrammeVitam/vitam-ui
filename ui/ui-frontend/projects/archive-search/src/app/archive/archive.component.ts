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
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  AccessContract,
  Collection,
  ExternalParameters,
  ExternalParametersService,
  GlobalEventService,
  Logger,
  SchemaService,
  SidenavPage,
  Unit,
} from 'ui-frontend-common';
import { ArchiveSharedDataService } from '../core/archive-shared-data.service';
import { ManagementRulesSharedDataService } from '../core/management-rules-shared-data.service';
import { ArchiveService } from './archive.service';

@Component({
  selector: 'app-archive',
  templateUrl: './archive.component.html',
  styleUrls: ['./archive.component.scss'],
})
export class ArchiveComponent extends SidenavPage<any> implements OnInit, OnDestroy {
  show = true;
  tenantIdentifier: string;
  foundAccessContract = false;
  accessContract: string;
  bulkOperationsThreshold: number;
  accessContractSub: Subscription;
  errorMessageSub: Subscription;
  isLPExtended = false;
  accessContractAllowUpdating = false;
  accessContractUpdatingRestrictedDesc = false;
  hasUpdateDescriptiveUnitMetadataRole = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    globalEventService: GlobalEventService,
    public dialog: MatDialog,
    private archiveSharedDataService: ArchiveSharedDataService,
    private externalParameterService: ExternalParametersService,
    private translateService: TranslateService,
    private snackBar: MatSnackBar,
    private managementRulesSharedDataService: ManagementRulesSharedDataService,
    private archiveService: ArchiveService,
    private loggerService: Logger,
    private schemaService: SchemaService,
  ) {
    super(route, globalEventService);
    this.schemaService.getSchema(Collection.ARCHIVE_UNIT);
  }

  ngOnInit() {
    this.route.params.subscribe((params) => {
      this.tenantIdentifier = params.tenantIdentifier;
    });

    this.archiveSharedDataService.getToggle().subscribe((hidden) => {
      this.show = hidden;
    });

    this.fetchUserExternalParameters();
    this.hasUpdateUnitDescriptiveMetadataPermission();
  }

  public hasUpdateUnitDescriptiveMetadataPermission() {
    this.archiveService.hasArchiveSearchRole('ROLE_UPDATE_UNIT_DESC_METADATA', Number(this.tenantIdentifier)).subscribe((result) => {
      this.hasUpdateDescriptiveUnitMetadataRole = result;
    });
  }

  ngOnDestroy() {
    this.accessContractSub.unsubscribe();
    if (this.errorMessageSub) {
      this.errorMessageSub.unsubscribe();
    }
  }

  fetchUserExternalParameters() {
    this.accessContractSub = this.externalParameterService.getUserExternalParameters().subscribe((parameters) => {
      const accessConctractId: string = parameters.get(ExternalParameters.PARAM_ACCESS_CONTRACT);

      if (accessConctractId && accessConctractId.length > 0) {
        this.accessContract = accessConctractId;
        this.foundAccessContract = true;
        this.managementRulesSharedDataService.emitAccessContract(accessConctractId);
        this.fetchVitamAccessContract();
      } else {
        this.errorMessageSub = this.translateService
          .get('ARCHIVE_SEARCH.ACCESS_CONTRACT_NOT_FOUND')
          .pipe(
            map((message) => {
              this.snackBar.open(message, null, {
                panelClass: 'vitamui-snack-bar',
                duration: 10000,
              });
            }),
          )
          .subscribe();
      }

      const threshold = Number(parameters.get(ExternalParameters.PARAM_BULK_OPERATIONS_THRESHOLD) || -1);

      this.bulkOperationsThreshold = threshold;
      this.managementRulesSharedDataService.emitBulkOperationsThreshold(threshold);
    });
  }

  fetchVitamAccessContract() {
    this.archiveService.getAccessContractById(this.accessContract).subscribe(
      (ac: AccessContract) => {
        this.accessContractAllowUpdating = ac.writingPermission;
        this.accessContractUpdatingRestrictedDesc = ac.writingRestrictedDesc;
      },
      (error: any) => {
        this.loggerService.error('error message', error);
        const message = this.translateService.instant('ARCHIVE_SEARCH.ACCESS_CONTRACT_NOT_FOUND_IN_VITAM');
        this.snackBar.open(message + ': ' + this.accessContract, null, {
          panelClass: 'vitamui-snack-bar',
          duration: 10000,
        });
      },
    );
  }

  hiddenTreeBlock(hidden: boolean): void {
    this.show = !hidden;
  }

  changeTenant(tenantIdentifier: number) {
    this.router.navigate(['..', tenantIdentifier], { relativeTo: this.route });
  }

  showPreviewArchiveUnit(item: Unit) {
    this.openPanel(item);
  }
  showExtendedLateralPanel() {
    this.isLPExtended = true;
  }
  backToNormalLateralPanel() {
    this.isLPExtended = false;
  }
}
