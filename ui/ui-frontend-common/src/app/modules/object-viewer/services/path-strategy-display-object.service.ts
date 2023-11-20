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
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Logger } from '../../logger/logger';
import { DisplayObject, DisplayObjectService, DisplayRule } from '../models';
import { DisplayObjectHelperService } from './display-object-helper.service';

enum Mode {
  DATE_DRIVEN = 'data-driven',
  TEMPLATE_DRIVEN = 'template-driven',
  MIXED_DRIVEN = 'mixed-driven',
}

const NO_MODE_MESSAGE = 'Mode is not set';
const DATA_MODE_MESSAGE = 'The data mode is enabled, the computed display object will follow object structure';
const TEMPLATE_MODE_MESSAGE = 'The template mode is enabled, the computed display object will follow the provided template structure';
const MIXED_MODE_MESSAGE =
  'The mixed mode is enabled, the computed display object will follow the provided template structure and data structure otherwise';

@Injectable()
export class PathStrategyDisplayObjectService implements DisplayObjectService {
  private readonly configuration = {
    displayEmptyValues: false,
  };
  private displayObject = new BehaviorSubject<DisplayObject>(null);
  private data = new BehaviorSubject<any>(null);
  private template = new BehaviorSubject<DisplayRule[]>([]);
  private mode = new BehaviorSubject<Mode>(Mode.DATE_DRIVEN);

  displayObject$: Observable<DisplayObject> = this.displayObject.asObservable();

  constructor(private logger: Logger, private displayObjectHelper: DisplayObjectHelperService) {
    const handleDisplayObjectComputing = (): void => {
      if (!this.data.value) {
        return;
      }

      let message = NO_MODE_MESSAGE;
      let displayObject = this.displayObjectHelper.toDisplayObject({});

      if (this.mode.value === Mode.DATE_DRIVEN) {
        message = DATA_MODE_MESSAGE;
        displayObject = this.displayObjectHelper.dataDrivenDisplayObject(this.data.value, this.configuration);
      }

      if (this.mode.value === Mode.TEMPLATE_DRIVEN) {
        message = TEMPLATE_MODE_MESSAGE;
        displayObject = this.displayObjectHelper.templateDrivenDisplayObject(this.data.value, this.template.value, this.configuration);
      }

      if (this.mode.value === Mode.MIXED_DRIVEN) {
        message = MIXED_MODE_MESSAGE;
        displayObject = this.displayObjectHelper.mixedDrivenDisplayObject(this.data.value, this.template.value, this.configuration);
      }

      this.logger.info(this, message);

      this.displayObject.next(displayObject);
    };

    this.data.subscribe(handleDisplayObjectComputing);
    this.template.subscribe(handleDisplayObjectComputing);
    this.mode.subscribe(handleDisplayObjectComputing);
  }

  setData(object: any): void {
    this.data.next(object);
  }

  setTemplate(template: DisplayRule[]): void {
    this.template.next(template);
  }

  setMode(mode: string): void {
    this.mode.next(mode as Mode);
  }
}
