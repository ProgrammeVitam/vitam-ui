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
import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { Logger } from '../logger/logger';
import { DisplayObjectService, DisplayRule } from './models';

@Component({
  selector: 'vitamui-common-object-viewer',
  templateUrl: './object-viewer.component.html',
  styleUrls: ['./object-viewer.component.scss'],
})
export class ObjectViewerComponent implements OnInit, OnChanges {
  @Input() data!: any;
  @Input() template!: DisplayRule[];
  @Input() mode = 'template-driven';

  constructor(
    private logger: Logger,
    public displayObjectService: DisplayObjectService,
  ) {}

  ngOnInit(): void {
    this.displayObjectService.setMode(this.mode);
    if (!this.data) {
      this.logger.warn(this, 'No data provided at init');
    }
    if (!this.template) {
      this.logger.warn(this, 'No template provided at init');
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    const { data, template, mode } = changes;

    if (mode) {
      this.displayObjectService.setMode(mode.currentValue);
    }
    if (data) {
      this.displayObjectService.setData(data.currentValue);
    }
    if (template) {
      this.displayObjectService.setTemplate(template.currentValue);
    }
  }
}
