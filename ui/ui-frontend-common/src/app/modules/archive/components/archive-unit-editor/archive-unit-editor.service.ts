import { Injectable } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { BehaviorSubject, combineLatest } from 'rxjs';
import { Logger } from '../../../logger/logger';
import { Collection, Schema } from '../../../models';
import { EditObject } from '../../../object-editor/models/edit-object.model';
import { EditObjectService } from '../../../object-editor/services/edit-object.service';
import { SchemaService as SchemaUtils } from '../../../object-editor/services/schema.service';
import { TemplateService } from '../../../object-editor/services/template.service';
import { DisplayRule, SchemaElement } from '../../../object-viewer/models';
import { DisplayObjectHelperService } from '../../../object-viewer/services/display-object-helper.service';
import { DisplayRuleHelperService } from '../../../object-viewer/services/display-rule-helper.service';
import { SchemaElementToDisplayRuleService } from '../../../object-viewer/services/schema-element-to-display-rule.service';
import { DisplayObjectType, SedaVersion } from '../../../object-viewer/types';
import { SchemaService } from '../../../schema';
import { diff } from '../../../utils';
import { ArchiveUnit } from '../../models/archive-unit';
import { JsonPatch, JsonPatchDto } from '../../models/json-patch';

@Injectable()
export class ArchiveUnitEditorService {
  private collection = new BehaviorSubject<Collection>(Collection.ARCHIVE_UNIT);
  private sedaVersions = new BehaviorSubject<SedaVersion[]>(['INTERNE', '2.3']);
  private category = new BehaviorSubject<SchemaElement['Category']>('DESCRIPTION');
  private data = new BehaviorSubject<any>(null);
  private editObject = new BehaviorSubject<EditObject>(null);
  private customTemplate = new BehaviorSubject<DisplayRule[]>([]);
  private template = new BehaviorSubject<DisplayRule[]>([]);
  private schema = new BehaviorSubject<Schema>([]);

  editObject$ = this.editObject.asObservable();

  private readonly OriginInternal = 'internal';
  private readonly OriginExternal = 'external';
  private readonly PathOtherMetadata = 'OtherMetadata';

  constructor(
    private logger: Logger,
    private schemaService: SchemaService,
    private displayObjectHelper: DisplayObjectHelperService,
    private displayRuleHelper: DisplayRuleHelperService,
    private componentMapperService: SchemaElementToDisplayRuleService,
    private templateService: TemplateService,
    private editObjectService: EditObjectService,
    private schemaUtils: SchemaUtils,
  ) {
    combineLatest([this.data, this.customTemplate, this.schemaService.getSchema(this.collection.value)]).subscribe(
      ([data, customTemplate, schema]) => {
        if (data === null) {
          this.editObject.next(null);

          return;
        }

        const subschema: Schema = schema
          .filter((element) => element.Category === this.category.value)
          .filter((element) => element.SedaVersions.some((version) => this.sedaVersions.value.includes(version)))
          .filter((element) => {
            const toRemove = [
              'Title.keyword', // FIXME: Remove from database or handle it properly
              'Signature.Masterdata', // FIXME: What should we do with free extension points
              'SigningInformation.Extended', // FIXME: What should we do with free extension points
              'OriginatingAgency.OrganizationDescriptiveMetadata', // FIXME: What should we do with free extension points
              'SubmissionAgency.OrganizationDescriptiveMetadata', // FIXME: What should we do with free extension points
            ];

            return !toRemove.includes(element.Path);
          });

        // To check consistency of subschema
        this.schemaUtils.validate(subschema);

        const editObject: EditObject = this.computeEditObject(data, customTemplate, subschema);

        this.editObject.next(editObject);
      },
      (error) => {
        this.logger.error(this, 'Error in observable', error);
      },
    );
  }

  public setData(data: any): void {
    this.data.next(data);
  }

  public setTemplate(template: DisplayRule[]): void {
    if (template) this.customTemplate.next(template);
  }

  private groupSchemaByOrigin(schema: SchemaElement[]): Record<string, SchemaElement[]> {
    const groupedByOrigin: Record<string, SchemaElement[]> = {};

    schema
      .filter((schemaElement) => {
        if (!schemaElement.Origin) {
          this.logger.warn(this, `Path ${schemaElement.Path} have not origin, it will be skipped...`, { schemaElement });
        }

        return schemaElement.Origin;
      })
      .forEach((schemaElement) => {
        const originToLocaleLowerCase = schemaElement.Origin.toLocaleLowerCase();

        if (!groupedByOrigin[originToLocaleLowerCase]) {
          groupedByOrigin[originToLocaleLowerCase] = [];
        }

        groupedByOrigin[originToLocaleLowerCase].push(schemaElement);
      });

    return groupedByOrigin;
  }

  private computeEditObject(originalData: any, customTemplate: DisplayRule[], originalSchema: Schema): EditObject {
    const fullTemplate = this.computeTemplate(originalData, customTemplate, originalSchema);
    this.template.next(fullTemplate);

    const projectedData = this.templateService.toProjected(originalData, fullTemplate);
    const subschema = originalSchema.filter((element) => element.Category === 'DESCRIPTION');

    const templatedSchema = this.editObjectService.createaTemplateSchema(fullTemplate, subschema);
    this.schema.next(templatedSchema);

    const editObject: EditObject = this.editObjectService.editObject('', projectedData, fullTemplate, templatedSchema);

    this.setMissingTypes(editObject); // TODO: Idealement utiliser soit type ou kind pour les ngIf des composants
    this.setMissingDisplayRules(editObject);
    this.setLabels(editObject, templatedSchema);
    this.displayAll(editObject);
    this.collapseAll(editObject);
    this.expand('', editObject);
    this.expand('Generalities', editObject);

    return editObject;
  }

  computeTemplate(data: any, customTemplate: DisplayRule[], schema: Schema): DisplayRule[] {
    const schemaByOrigin = this.groupSchemaByOrigin(schema);
    const internalSchema = schemaByOrigin[this.OriginInternal] || [];
    const externalSchema = schemaByOrigin[this.OriginExternal] || [];
    const defaultTemplate = this.getDefaultTemplate(data);
    const internalSchemaTemplate = this.componentMapperService.mapSchemaToDisplayRules(internalSchema);
    const externalSchemaTemplate = this.componentMapperService.mapSchemaToDisplayRules(externalSchema);
    const otherMetadataTemplate = this.getOtherMetadataTemplate(externalSchema);
    // Attention, le template de schéma se base sur un mapping interne vers externe pour les chemins, or la donnée reçue côté frontend est déjà au format externe.
    const backToFrontSchemaTemplate = internalSchemaTemplate.concat(externalSchemaTemplate);
    const frontToFrontSchemaTemplate = backToFrontSchemaTemplate.map((rule) => ({ ...rule, Path: rule.ui.Path }));
    const finalSchemaTemplate = this.displayRuleHelper.mergeDisplayRulesByPath(defaultTemplate, frontToFrontSchemaTemplate);
    const finalCustomTemplate = this.displayRuleHelper.prioritizeAndMergeDisplayRules(finalSchemaTemplate, customTemplate);
    const finalOtherMetadataTemplate = this.displayRuleHelper.prioritizeAndMergeDisplayRules(finalSchemaTemplate, otherMetadataTemplate);
    const finalNotConsumedRulesByCustomTemplate = this.displayRuleHelper.getUniqueDisplayRules(finalSchemaTemplate, finalCustomTemplate);
    const finalNotConsumedRulesByOtherMetadataTemplate = this.displayRuleHelper.getUniqueDisplayRules(
      finalSchemaTemplate,
      finalOtherMetadataTemplate,
    );
    const finalNotConsumedTemplate = this.displayRuleHelper.getCommonDisplayRules(
      finalNotConsumedRulesByCustomTemplate,
      finalNotConsumedRulesByOtherMetadataTemplate,
    );

    return finalCustomTemplate.concat(finalNotConsumedTemplate).concat(finalOtherMetadataTemplate);
  }

  private setLabels(editObject: EditObject, schema: Schema): void {
    const { displayRule } = editObject;
    const path = displayRule?.ui?.Path;

    if (displayRule?.ui?.Path) {
      const element = schema.find((e) => e.ApiPath === path);

      if (element) editObject.displayRule.ui.label = element.ShortName;
    }

    editObject.children.forEach((c) => this.setLabels(c, schema));
  }

  private setMissingTypes(editObject: EditObject): void {
    const { kind } = editObject;

    switch (kind) {
      case 'object':
        editObject.type = DisplayObjectType.GROUP;
        break;
      case 'object-array':
      case 'primitive-array':
        editObject.type = DisplayObjectType.LIST;
        break;
      case 'primitive':
      default:
        editObject.type = DisplayObjectType.PRIMITIVE;
    }

    editObject.children.forEach((child) => this.setMissingTypes(child as EditObject));
  }

  private setMissingDisplayRules(editObject: EditObject): void {
    const { displayRule } = editObject;

    if (!displayRule) {
      this.logger.info(this, `EditObject with path '${editObject.path}' have no DisplayRule`);
      editObject.displayRule = this.displayRuleHelper.toDisplayRule(editObject.value);
    }

    editObject.children.forEach((child) => this.setMissingDisplayRules(child as EditObject));
  }

  private displayAll(node: EditObject) {
    if (!node) {
      this.logger.log(this, 'Node is null');

      return;
    }

    if (!node.displayRule) {
      this.logger.log(this, `Node ${JSON.stringify({ ...node, control: null, children: null })} less displayRule`);

      return;
    }

    node.visible = true;
    node.displayRule = { ...node.displayRule, ui: { ...node.displayRule.ui, display: true } };
    node.children.forEach((child) => this.displayAll(child as EditObject));
  }

  private collapseAll(editObject: EditObject): void {
    editObject?.children?.forEach((child) => this.collapseAll(child));

    editObject.open = false;
  }

  private expand(path: string, editObject: EditObject): void {
    editObject?.children?.forEach((child) => this.expand(path, child));

    if (editObject.path === path) editObject.open = true;
  }

  private getDefaultTemplate(data: any): DisplayRule[] {
    return this.displayObjectHelper.extractDisplayRules(this.displayObjectHelper.dataDrivenDisplayObject(data));
  }

  private getOtherMetadataTemplate(schema: SchemaElement[]): DisplayRule[] {
    const otherMetadataDisplayRule: DisplayRule = {
      Path: null,
      ui: {
        Path: this.PathOtherMetadata,
        component: 'group',
        label: 'DISPLAY_RULE.OTHER_METADATA.LABEL',
        open: false,
        display: true,
        layout: {
          columns: 2,
          size: 'medium',
        },
      },
    };
    const externalSchemaMovedInOtherMetadataTemplate = this.componentMapperService.mapSchemaToDisplayRules(schema).map(
      (displayRule): DisplayRule => ({
        ...displayRule,
        ui: {
          ...displayRule.ui,
          Path: `${this.PathOtherMetadata}.${displayRule.ui.Path}`,
        },
      }),
    );

    return [otherMetadataDisplayRule].concat(externalSchemaMovedInOtherMetadataTemplate);
  }

  public getValue(): ArchiveUnit {
    const projectedValue = (this.editObject.value.control as FormGroup).getRawValue();
    const updatedValue = this.templateService.toOriginal(projectedValue, this.template.value);

    return updatedValue;
  }

  public getOriginalValue(): ArchiveUnit {
    const projectedValue = this.templateService.toProjected(this.data.value, this.template.value);
    const originalValue = this.templateService.toOriginal(projectedValue, this.template.value);

    return originalValue;
  }

  public toJsonPatch(): JsonPatch {
    const originalValue = this.getOriginalValue();
    const updatedValue = this.getValue();
    const criterias = [undefined, null, [], {}, ''];
    const consistentOriginalValue = this.filterByCriteria(originalValue, criterias);
    const consistentUpdatedValue = this.filterByCriteria(updatedValue, criterias);

    const changes = diff(consistentUpdatedValue, consistentOriginalValue);
    const replaceEntries = Object.entries(changes);
    const addEntries = Object.entries(consistentUpdatedValue).filter(([key]) => !Object.keys(consistentOriginalValue).includes(key));
    const removeEntries = Object.entries(consistentOriginalValue).filter(([key]) => !Object.keys(consistentUpdatedValue).includes(key));
    const jsonPatch: JsonPatch = [];

    removeEntries.forEach(([key, value]) => {
      jsonPatch.push({ op: 'remove', path: key, value });
    });
    addEntries.forEach(([key, value]) => {
      jsonPatch.push({ op: 'add', path: key, value });
    });
    replaceEntries.forEach(([key]) => {
      jsonPatch.push({ op: 'replace', path: key, value: consistentUpdatedValue[key] });
    });

    return jsonPatch;
  }

  public toJsonPatchDto(): JsonPatchDto {
    return {
      id: this.data.value['#id'],
      jsonPatch: this.toJsonPatch(),
    };
  }

  private filterByCriteria(input: any, criterias: any[]): any {
    if (Array.isArray(input)) {
      // Si c'est un tableau
      return input
        .map((item) => this.filterByCriteria(item, criterias)) // Appel récursif pour chaque élément
        .filter((item) => !criterias.some((criteria) => JSON.stringify(criteria) === JSON.stringify(item))); // Filtrer les éléments selon les critères fournis
    } else if (typeof input === 'object' && input !== null) {
      // Si c'est un objet
      const filteredObj = {};
      Object.entries(input).forEach(([key, value]) => {
        const filteredValue = this.filterByCriteria(value, criterias);
        if (!criterias.some((criteria) => JSON.stringify(criteria) === JSON.stringify(filteredValue))) {
          filteredObj[key] = filteredValue;
        }
      });
      return filteredObj;
    }
    // Pour tout autre type de valeur, renvoyer directement la valeur
    return input;
  }
}
