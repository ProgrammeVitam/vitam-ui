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

import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatMenuItem } from '@angular/material/menu';
import {
  ApplicationId,
  download,
  GriffinsService,
  PreservationScenariosService,
  Role,
  SecurityService,
  VitamUICommonModule,
} from 'vitamui-library';
import { PreservationGroupComponent } from './preservation-group/preservation-group.component';
import { MatDialog } from '@angular/material/dialog';
import { ImportScenarioDialogComponent } from './import-scenario-dialog/import-scenario-dialog.component';
import { ImportGriffinDialogComponent } from './import-griffin-dialog/import-griffin-dialog.component';
import { take } from 'rxjs/operators';

@Component({
  selector: 'app-preservation',
  templateUrl: './preservation.component.html',
  styleUrl: './preservation.component.scss',
  imports: [CommonModule, MatSidenavModule, TranslateModule, VitamUICommonModule, PreservationGroupComponent, MatMenuItem],
})
export class PreservationComponent {
  search = '';

  private readonly preservationScenariosService = inject(PreservationScenariosService);
  private readonly griffinsService = inject(GriffinsService);
  private readonly securityService = inject(SecurityService);
  private readonly dialog = inject(MatDialog);

  readonly hasUpdateRole = this.securityService.hasRole(ApplicationId.PRESERVATION_APP, Role.ROLE_UPDATE_GRIFFINS);

  onSearchSubmit(search: string) {
    this.search = search?.trim() || '';
  }

  protected importScenarios() {
    this.dialog.open(ImportScenarioDialogComponent);
  }

  protected exportScenarios() {
    this.preservationScenariosService
      .list()
      .pipe(take(1))
      .subscribe((scenarios) => {
        const blob = new Blob([JSON.stringify(scenarios, null, 2)], { type: 'octet/stream' });
        download(blob, 'scenarios.json');
      });
  }

  protected importGriffins() {
    this.dialog.open(ImportGriffinDialogComponent);
  }

  protected exportGriffins() {
    this.griffinsService
      .list()
      .pipe(take(1))
      .subscribe((griffins) => {
        const griffinsToExport = griffins.map(({ Identifier, Name, ExecutableName, ExecutableVersion, Description }) => ({
          Identifier,
          Name,
          ExecutableName,
          ExecutableVersion,
          Description,
        }));
        const blob = new Blob([JSON.stringify(griffinsToExport, null, 2)], { type: 'octet/stream' });
        download(blob, 'griffins.json');
      });
  }
}
