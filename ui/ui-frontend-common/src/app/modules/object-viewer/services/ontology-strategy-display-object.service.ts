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
import { BehaviorSubject, combineLatest, Observable } from 'rxjs';
import { Logger } from '../../logger/logger';
import { OntologyService } from '../../ontology';
import { DisplayObject, DisplayObjectService, DisplayRule, ExtendedOntology } from '../models';
import { ComponentMapperService } from './component-mapper.service';
import { DisplayObjectHelperService } from './display-object-helper.service';
import { DisplayRuleHelperService } from './display-rule-helper.service';
import { TypeService } from './type.service';

enum Mode {
  DEFAULT = 'default',
}

@Injectable()
export class OntologyStrategyDisplayObjectService implements DisplayObjectService {
  private readonly configuration = {
    displayEmptyValues: false,
  };
  private displayObject = new BehaviorSubject<DisplayObject>(null);
  private data = new BehaviorSubject<any>(null);
  private template = new BehaviorSubject<DisplayRule[]>([]);
  private mode = new BehaviorSubject<Mode>(Mode.DEFAULT);

  displayObject$: Observable<DisplayObject> = this.displayObject.asObservable();

  private readonly OriginInternal = 'internal';
  private readonly OriginExternal = 'external';
  private readonly PathOtherMetadata = 'OtherMetadata';

  constructor(
    private logger: Logger,
    private ontologyService: OntologyService,
    private displayObjectHelper: DisplayObjectHelperService,
    private displayRuleHelper: DisplayRuleHelperService,
    private componentMapperService: ComponentMapperService,
    private typeService: TypeService
  ) {
    combineLatest([
      this.ontologyService.getInternalOntologyFieldsList() as Observable<ExtendedOntology[]>,
      this.data,
      this.template,
    ]).subscribe(
      ([ontologies, data, template]) => {
        if (data === null) {
          this.displayObject.next(null);

          return;
        }

        const ontologiesByOrigin = this.groupOntologiesByOrigin(ontologies);
        const internalOntologies = ontologiesByOrigin[this.OriginInternal];
        const externalOntologies = ontologiesByOrigin[this.OriginExternal];
        const defaultTemplate = this.getDefaultTemplate(data);
        const internalOntologyTemplate = this.componentMapperService.mapOntologiesToDisplayRules(internalOntologies);
        const externalOntologyTemplate = this.componentMapperService.mapOntologiesToDisplayRules(externalOntologies);
        const customTemplate = template;
        const otherMetadataTemplate = this.getOtherMetadataTemplate(externalOntologies);
        const kwargs = {
          ontologies,
          ontologiesByOrigin,
          defaultTemplate,
          internalOntologyTemplate,
          externalOntologyTemplate,
          customTemplate,
          otherMetadataTemplate,
        };

        this.displayObject.next(this.computeDisplayObject(kwargs));
      },
      (error) => {
        this.logger.error(this, 'Error in observable', error);
      }
    );
  }

  private groupOntologiesByOrigin(ontologies: ExtendedOntology[]): Record<string, ExtendedOntology[]> {
    const groupedByOrigin: Record<string, ExtendedOntology[]> = {};

    ontologies.forEach((ontology) => {
      const origin = ontology.Origin.toLocaleLowerCase();

      if (!groupedByOrigin[origin]) {
        groupedByOrigin[origin] = [];
      }

      groupedByOrigin[origin].push(ontology);
    });

    return groupedByOrigin;
  }

  public setData(data: any): void {
    this.data.next(data);
  }

  public setTemplate(template: DisplayRule[]): void {
    if (template) {
      this.template.next(template);
    }
  }

  public setMode(mode: string): void {
    if (mode) {
      this.mode.next(mode as Mode);
    }
  }

  private computeDisplayObject({
    ontologies,
    defaultTemplate,
    internalOntologyTemplate,
    externalOntologyTemplate,
    customTemplate,
    otherMetadataTemplate,
  }: {
    ontologies: ExtendedOntology[];
    defaultTemplate: DisplayRule[];
    internalOntologyTemplate: DisplayRule[];
    externalOntologyTemplate: DisplayRule[];
    customTemplate: DisplayRule[];
    otherMetadataTemplate: DisplayRule[];
  }): DisplayObject {
    if (this.mode.value !== Mode.DEFAULT) {
      throw new Error(`Mode ${this.mode.value} is not supported`);
    }

    const ontologyTemplate = internalOntologyTemplate.concat(externalOntologyTemplate);
    const finalOntologyTemplate = this.displayRuleHelper.mergeDisplayRulesByPath(ontologyTemplate, defaultTemplate);
    const finalCustomTemplate = this.displayRuleHelper.prioritizeAndMergeDisplayRules(finalOntologyTemplate, customTemplate);
    const finalOtherMetadataTemplate = this.displayRuleHelper.prioritizeAndMergeDisplayRules(finalOntologyTemplate, otherMetadataTemplate);
    const finalNotConsumedRulesByCustomTemplate = this.displayRuleHelper.getUniqueDisplayRules(finalOntologyTemplate, finalCustomTemplate);
    const finalNotConsumedRulesByOtherMetadataTemplate = this.displayRuleHelper.getUniqueDisplayRules(
      finalOntologyTemplate,
      finalOtherMetadataTemplate
    );
    const finalNotConsumedTemplate = this.displayRuleHelper.getCommonDisplayRules(
      finalNotConsumedRulesByCustomTemplate,
      finalNotConsumedRulesByOtherMetadataTemplate
    );

    const template = finalCustomTemplate.concat(finalNotConsumedTemplate).concat(finalOtherMetadataTemplate);

    this.logger.log(
      this,
      'computeDisplayObject',
      { internalOntologyTemplate, externalOntologyTemplate },
      {
        ontologyTemplate,
        finalOntologyTemplate,
        finalCustomTemplate,
        finalOtherMetadataTemplate,
        finalNotConsumedRulesByCustomTemplate,
        finalNotConsumedRulesByOtherMetadataTemplate,
        finalNotConsumedTemplate,
      }
    );

    const displayObject = this.displayObjectHelper.templateDrivenDisplayObject(this.data.value, template, this.configuration);

    this.logger.log(this, 'computeDisplayObject', {
      displayObject,
    });

    this.hideUnconcernedNodes(ontologies, displayObject);

    return displayObject;
  }

  private ontologyByPathFilter =
    (path: string) =>
    (ontology: ExtendedOntology): boolean => {
      const backendPath = ontology.path;
      const frontendPath = this.componentMapperService.getOntologyFrontendModelPath(ontology);

      return [backendPath, frontendPath].includes(path);
    };

  private hideUnconcernedNodes(ontologies: ExtendedOntology[], displayObject: DisplayObject): void {
    const { displayRule, value } = displayObject;
    const { path, ui } = displayRule;
    const ontology = ontologies.find(this.ontologyByPathFilter(path));
    const belongsToOntologies = Boolean(ontology);
    const isConsistent = this.typeService.isConsistent(value);
    const isVirtual = displayRule.path === null;
    const belongsToTemplate = this.template.value.some((rule) => rule.ui.path === path);
    const shouldDisplay = isConsistent && (belongsToOntologies || isVirtual || belongsToTemplate);

    displayObject.displayRule = { ...displayRule, ui: { ...ui, display: shouldDisplay } };

    if (displayObject.children) {
      displayObject.children.forEach((child) => this.hideUnconcernedNodes(ontologies, child));
    }
  }

  private getDefaultTemplate(data: any): DisplayRule[] {
    const defaultDisplayObject: DisplayObject = this.displayObjectHelper.dataDrivenDisplayObject(data, this.configuration);

    return this.displayObjectHelper.extractDisplayRules(defaultDisplayObject);
  }

  private getOtherMetadataTemplate(ontologies: ExtendedOntology[], origin = this.OriginExternal): DisplayRule[] {
    const otherMetadataDisplayRule: DisplayRule = {
      path: null,
      ui: {
        path: this.PathOtherMetadata,
        component: 'group',
        open: false,
        display: true,
        layout: {
          columns: 2,
          size: 'medium',
        },
      },
    };
    const externalOntologyMovedInOtherMetadataTemplate = this.componentMapperService
      .mapOntologiesToDisplayRules(ontologies)
      .map((displayRule) => ({
        ...displayRule,
        ui: {
          ...displayRule.ui,
          path: `${this.PathOtherMetadata}.${displayRule.ui.path}`,
        },
      }));

    return [otherMetadataDisplayRule].concat(externalOntologyMovedInOtherMetadataTemplate);
  }
}
