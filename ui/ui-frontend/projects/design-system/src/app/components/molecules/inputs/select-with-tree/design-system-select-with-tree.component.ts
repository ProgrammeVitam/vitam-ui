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
import { AfterViewInit, Component, ElementRef, inject, OnInit, signal, afterNextRender, Injector, viewChildren } from '@angular/core';
import { disabled, FieldTree, form, FormField, validate } from '@angular/forms/signals';
import {
  ItemNode,
  MockSchemaService,
  requiredValue,
  SchemaElement,
  SchemaService,
  SelectWithTreeComponent,
  VitamUICommonModule,
} from 'vitamui-library';
import { TranslatePipe } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { AsyncPipe } from '@angular/common';

type SelectWithTreeValue = SchemaElement | SchemaElement[] | null;

@Component({
  imports: [FormField, TranslatePipe, AsyncPipe, SelectWithTreeComponent, VitamUICommonModule],
  templateUrl: './design-system-select-with-tree.component.html',
  styleUrl: './design-system-select-with-tree.component.scss',
  providers: [{ provide: SchemaService, useClass: MockSchemaService }],
})
export class DesignSystemSelectWithTreeComponent implements OnInit, AfterViewInit {
  private readonly schemaService = inject(SchemaService);

  private readonly injector = inject(Injector);

  readonly configs = signal<
    {
      name: string;
      multiple?: boolean;
      entries: { type: string; states: { id: string; field: FieldTree<SelectWithTreeValue> }[] }[];
    }[]
  >(undefined);

  readonly displayOptions = form(signal({ enableSearch: true, enableDisplaySelected: true }));

  schemaOptions$: Observable<ItemNode<SchemaElement>[]>;

  readonly components = viewChildren(SelectWithTreeComponent, { read: ElementRef });

  ngOnInit() {
    this.initSchemaOptions();
  }

  ngAfterViewInit() {
    this.schemaOptions$.subscribe((options) => {
      const selectedOptionValue = options[1].item;
      this.configs.set([
        {
          name: 'Simple select',
          multiple: false,
          entries: this.getEntries(selectedOptionValue),
        },
        {
          name: 'Multiple select',
          multiple: true,
          entries: this.getEntries([selectedOptionValue]),
        },
      ]);
      // Focus the "Active" selects once they are rendered
      afterNextRender(
        () => {
          this.components().forEach((component) => {
            const nativeElement = component.nativeElement as HTMLElement;
            const isActive = nativeElement.getAttribute('data-active') === 'true';
            if (isActive) nativeElement.querySelector('mat-select').dispatchEvent(new Event('focus'));
          });
        },
        { injector: this.injector },
      );
    });
  }

  private getEntries(selectedOptionValue: SchemaElement | SchemaElement[]) {
    return [
      {
        type: 'empty',
        states: [
          { id: 'Default', field: this.createField() },
          { id: 'Active', field: this.createField() },
          { id: 'Disabled', field: this.createField({ disabled: true }) },
          { id: 'Error', field: this.createField({ error: true }) },
        ],
      },
      {
        type: 'full',
        states: [
          { id: 'Default', field: this.createField({ value: selectedOptionValue }) },
          { id: 'Active', field: this.createField({ value: selectedOptionValue }) },
          { id: 'Disabled', field: this.createField({ disabled: true, value: selectedOptionValue }) },
          { id: 'Error', field: this.createField({ error: true, value: selectedOptionValue }) },
        ],
      },
    ];
  }

  private createField(config?: { disabled?: boolean; error?: boolean; value?: SelectWithTreeValue }): FieldTree<SelectWithTreeValue> {
    const field = form(
      signal<SelectWithTreeValue>(config?.value ?? null),
      (path) => {
        if (config?.error || config?.disabled) {
          requiredValue(path);
        }
        if (config?.error) {
          validate(path, ({ value }) => {
            const values = value() ? (Array.isArray(value()) ? (value() as SchemaElement[]) : [value() as SchemaElement]) : [];
            return values.length && !values.some((v) => v.Path === 'TextContent') ? { kind: 'CONTAINS_TextContent' } : undefined;
          });
        }
        if (config?.disabled) {
          disabled(path);
        }
      },
      { injector: this.injector },
    );
    if (config?.error) field().markAsTouched();
    return field;
  }

  getSchemaElementDisplayValue = (element: SchemaElement) =>
    `${element.Origin === 'EXTERNAL' ? 'EXT-' : ''}${element.ShortName} - ${element.FieldName}`;

  private initSchemaOptions(): void {
    this.schemaOptions$ = this.schemaService.getDescriptiveSchemaTree();
  }
}
