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
import {
  ChangeDetectorRef,
  AfterViewInit,
  Component,
  HostListener,
  inject,
  OnChanges,
  SimpleChanges,
  output,
  viewChild,
  model,
} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTab, MatTabGroup, MatTabHeader, MatTabsModule } from '@angular/material/tabs';
import { Observable } from 'rxjs';
import { ConfirmActionComponent, Ontology, SchemaElement, SchemaService, VitamUICommonModule, VitamUILibraryModule } from 'vitamui-library';
import { OntologyService } from '../ontology.service';
import { OntologyInformationTabComponent } from './ontology-information-tab/ontology-information-tab.component';

import { SchemaInformationTabComponent } from './schema-information-tab/schema-information-tab.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-ontology-preview',
  templateUrl: './ontology-preview.component.html',
  styleUrls: ['./ontology-preview.component.scss'],
  imports: [
    MatTabsModule,
    OntologyInformationTabComponent,
    SchemaInformationTabComponent,
    VitamUICommonModule,
    VitamUILibraryModule,
    TranslatePipe,
  ],
})
export class OntologyPreviewComponent implements AfterViewInit, OnChanges {
  private readonly matDialog = inject(MatDialog);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly ontologyService = inject(OntologyService);
  schemaService = inject(SchemaService);

  readonly previewClose = output<any>();

  title: string;
  identifier: string;

  readonly selectedElement = model<Ontology | SchemaElement>(undefined);
  // tab indexes: info = 0; history = 2;
  tabUpdated: boolean[] = [false, false];
  readonly tabs = viewChild<MatTabGroup>('tabs');

  tabLinks: Array<OntologyInformationTabComponent> = [];
  readonly infoTab = viewChild<OntologyInformationTabComponent>('infoTab');

  @HostListener('window:beforeunload', ['$event'])
  beforeunloadHandler(event: BeforeUnloadEvent): string | void {
    if (this.tabUpdated?.[this.tabs()?.selectedIndex] && this.isOntology(this.selectedElement())) {
      event.preventDefault();
      this.checkBeforeExit();
      event.returnValue = '';
      return '';
    }
  }

  ngAfterViewInit() {
    this.tabLinks[0] = this.infoTab();
  }

  ngOnChanges(changes: SimpleChanges): void {
    const selectedElement = this.selectedElement();
    if (changes['selectedElement'] && selectedElement) {
      if (this.isOntology(selectedElement)) {
        this.title = selectedElement.shortName;
        this.identifier = selectedElement.identifier;
      } else {
        this.title = selectedElement.ShortName;
        this.identifier = selectedElement.FieldName;
      }
    }
  }

  updatedChange(updated: boolean, index: number) {
    this.tabUpdated[index] = updated;
  }

  async checkBeforeExit() {
    if (await this.confirmAction()) {
      const submitOntologyUpdate: Observable<Ontology> = this.tabLinks[this.tabs().selectedIndex].prepareSubmit();

      submitOntologyUpdate.subscribe(() => {
        this.ontologyService.get((this.selectedElement() as Ontology)?.identifier).subscribe((response) => {
          this.selectedElement.set(response);
          this.cdr.markForCheck();
        });
      });
    } else {
      this.tabLinks[this.tabs().selectedIndex].resetForm(this.selectedElement() as Ontology);
    }
  }

  async interceptTabChange(tab: MatTab, tabHeader: MatTabHeader, idx: number) {
    if (this.tabUpdated[this.tabs().selectedIndex]) {
      await this.checkBeforeExit();
    }

    const args = [tab, tabHeader, idx];
    return MatTabGroup.prototype._handleClick.apply(this.tabs(), args);
  }

  async confirmAction(): Promise<boolean> {
    const dialog = this.matDialog.open(ConfirmActionComponent, { panelClass: 'small' });
    dialog.componentRef.setInput('dialogType', 'changeTab');
    return await dialog.afterClosed().toPromise();
  }

  emitClose() {
    this.previewClose.emit(undefined);
    this.tabUpdated = [false, false];
    this.ontologyService.selectedId$.next(null);
    this.schemaService.selectedPath$.next(null);
  }

  isOntology(item: Ontology | SchemaElement): item is Ontology {
    return item && 'tenant' in item && 'identifier' in item;
  }
}
