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

import { Component, computed, effect, ElementRef, inject, input, output, Signal, signal, viewChild } from '@angular/core';
import {
  download,
  Griffin,
  GriffinsService,
  PreservationScenario,
  PreservationScenariosService,
  VitamUICommonModule,
  VitamUILibraryModule,
} from 'vitamui-library';
import { MatTab, MatTabGroup } from '@angular/material/tabs';
import { GriffinInformationTabComponent } from './griffin-information-tab/griffin-information-tab.component';
import { TranslatePipe } from '@ngx-translate/core';
import { PreservationScenarioInformationTabComponent } from './preservation-scenario-information-tab/preservation-scenario-information-tab.component';

@Component({
  selector: 'app-preservation-preview',
  templateUrl: './preservation-preview.component.html',
  imports: [
    VitamUICommonModule,
    VitamUILibraryModule,
    MatTab,
    MatTabGroup,
    GriffinInformationTabComponent,
    TranslatePipe,
    PreservationScenarioInformationTabComponent,
  ],
})
export class PreservationPreviewComponent {
  previewClose = output();

  private readonly griffinService = inject(GriffinsService);
  private readonly preservationScenarioService = inject(PreservationScenariosService);

  selectedElement = input.required<Griffin | PreservationScenario>();
  editableElement = signal<Griffin | PreservationScenario | null>(null);
  tabUpdated: boolean[] = [false, false];

  title: string;

  isPreservationScenario: Signal<boolean> = computed(() => {
    return this.selectedElement() && 'ActionList' in this.selectedElement();
  });

  infoTab = viewChild<ElementRef<HTMLElement>>('infoTab');

  constructor() {
    effect(() => {
      this.editableElement.set(this.selectedElement());
    });
  }

  updateGriffin(griffin: Griffin) {
    this.editableElement.set(griffin);
  }

  downloadScenario() {
    const blob = new Blob([JSON.stringify([this.selectedElement()], null, 2)], { type: 'octet/stream' });
    download(blob, 'PreservationScenario.json');
  }

  emitClose() {
    this.previewClose.emit();
    this.tabUpdated = [false, false];
    this.griffinService.selectedId$.next(null);
    this.preservationScenarioService.selectedId$.next(null);
  }
}
