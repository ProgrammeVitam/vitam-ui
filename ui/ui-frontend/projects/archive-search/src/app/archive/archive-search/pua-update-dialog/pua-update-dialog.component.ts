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
import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatRadioModule } from '@angular/material/radio';
import { finalize, Observable } from 'rxjs';
import { AsyncPipe, I18nPluralPipe } from '@angular/common';
import {
  ArchiveUnitProfilesService,
  Logger,
  SearchCriteriaEltDto,
  SelectComponent,
  StartupService,
  VitamuiSelectOptions,
} from 'vitamui-library';
import { map } from 'rxjs/operators';
import { ArchiveService } from '../../archive.service';
import { RuleActions, RuleSearchCriteriaDto } from '../../models/ruleAction.interface';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

export interface PuaUpdateDialogComponentData {
  tenantIdentifier: number;
  listOfUACriteriaSearch: SearchCriteriaEltDto[];
  selectedItemCount: number;
}

@Component({
  standalone: true,
  imports: [FormsModule, MatRadioModule, ReactiveFormsModule, MatDialogModule, AsyncPipe, SelectComponent, TranslateModule, I18nPluralPipe],
  templateUrl: './pua-update-dialog.component.html',
  styleUrl: './pua-update-dialog.component.scss',
})
export class PuaUpdateDialogComponent {
  form: FormGroup;
  puas$: Observable<VitamuiSelectOptions>;

  subtitle: { [k: string]: string } = {
    '=1': 'ARCHIVE_SEARCH.OTHER_ACTIONS.PUA_UPDATE.SUBTITLE.SINGULAR',
    other: 'ARCHIVE_SEARCH.OTHER_ACTIONS.PUA_UPDATE.SUBTITLE.PLURAL',
  };
  updating: boolean;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: PuaUpdateDialogComponentData,
    fb: FormBuilder,
    archiveUnitProfilesService: ArchiveUnitProfilesService,
    private dialogRef: MatDialogRef<PuaUpdateDialogComponent>,
    private archiveService: ArchiveService,
    private startupService: StartupService,
    private translateService: TranslateService,
    private logger: Logger,
  ) {
    this.puas$ = archiveUnitProfilesService
      .getAll()
      .pipe(map((puas) => ({ options: puas.map((pua) => ({ key: pua.identifier, label: `${pua.identifier} - ${pua.name}` })) })));

    const actionControl = new FormControl(null, Validators.required);
    this.form = fb.group({
      action: actionControl,
    });

    actionControl.valueChanges.subscribe((value) => {
      if ((value as unknown as string) === 'update') {
        this.form.addControl('pua', new FormControl(null, Validators.required));
      } else {
        this.form.removeControl('pua');
      }
    });
  }

  submit() {
    let actions: Partial<RuleActions>;
    if (this.form.value.action === 'update') {
      actions = {
        addOrUpdateMetadata: {
          archiveUnitProfile: this.form.value.pua,
        },
      };
    }
    if (this.form.value.action === 'delete') {
      actions = {
        deleteMetadata: {
          archiveUnitProfile: 'NOT_EMPTY', // Any value will do
        },
      };
    }

    const criteriaSearchDSLQueryToSend = {
      criteriaList: this.data.listOfUACriteriaSearch,
      pageNumber: 0, // Whatever the value, it won't be interpreted by the backend for updating the rules
      size: 0, // Whatever the value, it won't be interpreted by the backend for updating the rules
      language: this.translateService.currentLang,
    };

    if (actions) {
      const ruleSearchCriteriaDto: RuleSearchCriteriaDto = {
        searchCriteriaDto: criteriaSearchDSLQueryToSend,
        ruleActions: actions,
      };
      this.updating = true;
      this.archiveService
        .updateUnitsRules(ruleSearchCriteriaDto)
        .pipe(finalize(() => (this.updating = false)))
        .subscribe({
          next: (response) => {
            const serviceUrl = `${this.startupService.getReferentialUrl()}/logbook-operation/tenant/${this.data.tenantIdentifier}?guid=${response}`;

            this.dialogRef.close();
            this.archiveService.openSnackBarForWorkflow(
              this.translateService.instant('ARCHIVE_SEARCH.OTHER_ACTIONS.PUA_UPDATE.SUCCESS_MESSAGE'),
              serviceUrl,
            );
          },
          error: (error: any) => {
            this.logger.error('Error message :', error);
          },
        });
    }
  }
}
