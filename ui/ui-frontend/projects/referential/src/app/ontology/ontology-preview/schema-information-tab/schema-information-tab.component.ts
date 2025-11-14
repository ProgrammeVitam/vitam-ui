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
import { Component, Input } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MiscValidators, Option, SchemaElement, SchemaService, VitamUICommonModule, VitamUILibraryModule } from 'vitamui-library';
import { sizes, types } from '../../ontology-form-options';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-schema-information-tab',
  templateUrl: './schema-information-tab.component.html',
  styleUrl: './schema-information-tab.component.scss',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, TranslateModule, VitamUICommonModule, VitamUILibraryModule],
})
export class SchemaInformationTabComponent {
  form: FormGroup;
  isExternal = false;
  showCustomSearchTypes = false;
  stringSizeVisible = false;
  sizes = sizes;

  types = [...types, { key: 'OBJECT', label: 'Objet', info: '' }];

  dataTypes: Option[] = [
    { key: 'STRING', label: 'String', info: '' },
    { key: 'ENUM', label: 'Énuméré', info: '' },
    { key: 'DATETIME', label: 'Date', info: '' },
    { key: 'BOOLEAN', label: 'Boolean', info: '' },
    { key: 'LONG', label: 'Long', info: '' },
    { key: 'DOUBLE', label: 'Décimal', info: '' },
  ];

  collections: Option[] = [
    { key: 'ARCHIVE_UNIT', label: 'Unité Archivistique', info: '' },
    { key: 'OBJECT_GROUP', label: "Groupe d'objet", info: '' },
  ];

  origins: Option[] = [
    { key: 'INTERNAL', label: 'Internal', info: '' },
    { key: 'EXTERNAL', label: 'External', info: '' },
  ];

  categories: Option[] = [
    { key: 'MANAGEMENT', label: 'MANAGEMENT', info: '' },
    { key: 'DESCRIPTION', label: 'DESCRIPTION', info: '' },
    { key: 'OTHER', label: 'OTHER', info: '' },
  ];

  cardinalities: Option[] = [
    { key: 'ONE', label: 'ONE', info: '' },
    { key: 'MANY', label: 'MANY', info: '' },
    { key: 'ONE_REQUIRED', label: 'ONE_REQUIRED', info: '' },
    { key: 'MANY_REQUIRED', label: 'MANY_REQUIRED', info: '' },
  ];

  private _inputSchema: SchemaElement;

  @Input()
  set inputSchema(schema: SchemaElement) {
    if (!schema || !this.form) return;

    this._inputSchema = schema;
    this.form.patchValue(schema);
    this.isExternal = schema?.Origin === 'EXTERNAL';
    this.stringSizeVisible = ['TEXT', 'KEYWORD'].includes(schema?.Type);
    this.showCustomSearchTypes = !['OBJECT'].includes(schema?.Type) && schema?.CustomSearchTypes?.length > 0;
  }
  get inputSchema(): SchemaElement {
    return this._inputSchema;
  }

  constructor(
    private formBuilder: FormBuilder,
    public schemaService: SchemaService,
  ) {
    this.form = this.formBuilder.group({
      Path: [{ value: '', disabled: true }, [MiscValidators.requiredNotBlank]],
      ApiPath: [{ value: '', disabled: true }, [MiscValidators.requiredNotBlank]],
      ShortName: [{ value: '', disabled: true }],
      FieldName: [{ value: '', disabled: true }, [MiscValidators.requiredNotBlank]],
      SedaField: [{ value: '', disabled: true }],
      ApiField: [{ value: '', disabled: true }],
      Description: [{ value: '', disabled: true }, [MiscValidators.requiredNotBlank]],
      Type: [{ value: '', disabled: true }, [Validators.required]],
      DataType: [{ value: '', disabled: true }, [Validators.required]],
      Collection: [{ value: '', disabled: true }, [Validators.required]],
      Origin: [{ value: '', disabled: true }],
      Category: [{ value: '', disabled: true }, [Validators.required]],
      Cardinality: [{ value: '', disabled: true }, [Validators.required]],
      SedaVersions: [{ value: '', disabled: true }],
      StringSize: [{ value: '', disabled: true }],
      CustomSearchTypes: [{ value: '', disabled: true }],
      CreationDate: [{ value: null, disabled: true }],
      LastUpdate: [{ value: null, disabled: true }],
    });
  }
}
