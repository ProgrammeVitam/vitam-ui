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

import { Component, effect, inject, input } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActionType, Option, PreservationScenario } from 'vitamui-library';
import { VitamUICommonModule, VitamUILibraryModule } from 'vitamui-library';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-preservation-scenario-information-tab',
  imports: [FormsModule, ReactiveFormsModule, VitamUICommonModule, VitamUILibraryModule, TranslatePipe],
  templateUrl: './preservation-scenario-information-tab.component.html',
})
export class PreservationScenarioInformationTabComponent {
  private readonly translateService = inject(TranslateService);

  private readonly formBuilder = inject(FormBuilder);

  possibleActions: Option[] = [
    { key: 'GENERATE', label: this.translateService.instant('PRESERVATION.SCENARIO.TABLE.HEADER.ACTIONS.GENERATE') },
    { key: 'IDENTIFY', label: this.translateService.instant('PRESERVATION.SCENARIO.TABLE.HEADER.ACTIONS.IDENTIFY') },
    { key: 'ANALYSE', label: this.translateService.instant('PRESERVATION.SCENARIO.TABLE.HEADER.ACTIONS.ANALYSE') },
    { key: 'EXTRACT', label: this.translateService.instant('PRESERVATION.SCENARIO.TABLE.HEADER.ACTIONS.EXTRACT') },
    { key: 'EXTRACT_AU', label: this.translateService.instant('PRESERVATION.SCENARIO.TABLE.HEADER.ACTIONS.EXTRACT_AU') },
  ];

  inputPreservationScenario = input.required<PreservationScenario>();

  readonly form = this.formBuilder.nonNullable.group({
    Identifier: [{ value: '', disabled: true }, Validators.required],
    Name: [{ value: '', disabled: true }, Validators.required],
    ActionList: [{ value: [] as ActionType[], disabled: true }, Validators.required],
    Description: [{ value: '', disabled: true }],
    CreationDate: [{ value: null as Date, disabled: true }],
    LastUpdate: [{ value: null as Date, disabled: true }],
  });

  constructor() {
    effect(() => {
      Object.entries(this.form.controls).forEach(([, control]) => control.disable());

      this.form.reset(
        { ...this.inputPreservationScenario(), Description: this.inputPreservationScenario().Description ?? '' },
        { emitEvent: false },
      );
    });
  }
}
