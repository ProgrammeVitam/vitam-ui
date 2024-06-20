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
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatLegacyProgressSpinnerModule as MatProgressSpinnerModule } from '@angular/material/legacy-progress-spinner';
import { MatLegacyTooltipModule as MatTooltipModule } from '@angular/material/legacy-tooltip';
import { TranslateModule } from '@ngx-translate/core';
import { AccordionModule } from '../components/accordion/accordion.module';
import { DataModule } from '../components/data/data.module';
import { PipesModule } from '../pipes/pipes.module';
import { GroupComponent } from './components/group/group.component';
import { ListComponent } from './components/list/list.component';
import { PrimitiveComponent } from './components/primitive/primitive.component';
import { DisplayObjectService } from './models';
import { ObjectViewerComponent } from './object-viewer.component';
import { DisplayObjectHelperService } from './services/display-object-helper.service';
import { DisplayRuleHelperService } from './services/display-rule-helper.service';
import { PathStrategyDisplayObjectService } from './services/path-strategy-display-object.service';
import { SchemaElementToDisplayRuleService } from './services/schema-element-to-display-rule.service';

@NgModule({
  imports: [CommonModule, TranslateModule, PipesModule, MatTooltipModule, MatProgressSpinnerModule, AccordionModule, DataModule],
  declarations: [ObjectViewerComponent, GroupComponent, ListComponent, PrimitiveComponent],
  providers: [
    DisplayObjectHelperService,
    DisplayRuleHelperService,
    SchemaElementToDisplayRuleService,
    { provide: DisplayObjectService, useClass: PathStrategyDisplayObjectService },
  ],
  exports: [ObjectViewerComponent, GroupComponent, ListComponent, PrimitiveComponent],
})
export class ObjectViewerModule {}
