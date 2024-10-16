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
import { Component, OnInit } from '@angular/core';
import { MatLegacyDialog as MatDialog, MatLegacyDialogConfig as MatDialogConfig } from '@angular/material/legacy-dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { GlobalEventService, SidenavPage } from 'vitamui-library';
import { IngestType } from '../core/common/ingest-type.enum';
import { UploadComponent } from '../core/common/upload.component';

@Component({
  selector: 'app-holding-filling-scheme',
  templateUrl: './holding-filling-scheme.component.html',
  styleUrls: ['./holding-filling-scheme.component.scss'],
})
export class HoldingFillingSchemeComponent extends SidenavPage<any> implements OnInit {
  IngestType = IngestType;

  tenantIdentifier: string;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    globalEventService: GlobalEventService,
    public dialog: MatDialog,
  ) {
    super(route, globalEventService);
  }

  ngOnInit() {
    this.route.params.subscribe((params) => {
      this.tenantIdentifier = params.tenantIdentifier;
    });
  }

  openImportTreePlanPopup(type: IngestType) {
    const dialogConfig = new MatDialogConfig();

    dialogConfig.panelClass = 'vitamui-modal';
    dialogConfig.disableClose = false;

    dialogConfig.data = {
      tenantIdentifier: this.tenantIdentifier,
      givenContextId: type,
    };

    const dialogRef = this.dialog.open(UploadComponent, dialogConfig);

    dialogRef.afterClosed().subscribe();
  }

  changeTenant(tenantIdentifier: number) {
    this.router.navigate(['..', tenantIdentifier], { relativeTo: this.route });
  }
}
