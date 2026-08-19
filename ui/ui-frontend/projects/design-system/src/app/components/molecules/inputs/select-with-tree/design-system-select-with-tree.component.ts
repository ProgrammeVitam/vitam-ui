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
import { AfterViewInit, Component, ElementRef, OnInit, QueryList, ViewChildren, inject } from '@angular/core';
import { AbstractControl, FormControl, FormsModule, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { ItemNode, MockSchemaService, SchemaElement, SchemaService } from 'vitamui-library';
import { SelectWithTreeComponent, VitamUICommonModule } from 'vitamui-library';
import { TranslatePipe } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { AsyncPipe } from '@angular/common';
import { delay } from 'rxjs/operators';

@Component({
  imports: [ReactiveFormsModule, TranslatePipe, AsyncPipe, SelectWithTreeComponent, FormsModule, VitamUICommonModule],
  templateUrl: './design-system-select-with-tree.component.html',
  styleUrl: './design-system-select-with-tree.component.scss',
  providers: [{ provide: SchemaService, useClass: MockSchemaService }],
})
export class DesignSystemSelectWithTreeComponent implements OnInit, AfterViewInit {
  private schemaService = inject(SchemaService);

  configs: {
    name: string;
    multiple?: boolean;
    entries: { type: string; states: { id: string; control: FormControl }[] }[];
  }[];

  enableSearch = true;
  enableDisplaySelected = true;

  schemaOptions$: Observable<ItemNode<SchemaElement>[]>;

  @ViewChildren(SelectWithTreeComponent, { read: ElementRef }) components: QueryList<ElementRef>;

  ngOnInit() {
    this.initSchemaOptions();
  }

  ngAfterViewInit() {
    this.schemaOptions$.subscribe((options) => {
      const selectedOptionValue = options[1].item;
      this.configs = [
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
      ];
    });

    this.schemaOptions$.pipe(delay(0)).subscribe(() => {
      this.components.forEach((component) => {
        const nativeElement = component.nativeElement as HTMLElement;
        const isActive = nativeElement.getAttribute('data-active') === 'true';
        if (isActive) nativeElement.querySelector('mat-select').dispatchEvent(new Event('focus'));
      });
    });
  }

  private getEntries(selectedOptionValue: any) {
    return [
      {
        type: 'empty',
        states: [
          { id: 'Default', control: this.createControl() },
          { id: 'Active', control: this.createControl() },
          { id: 'Disabled', control: this.createControl({ disabled: true }) },
          { id: 'Error', control: this.createControl({ error: true }) },
        ],
      },
      {
        type: 'full',
        states: [
          { id: 'Default', control: this.createControl({ value: selectedOptionValue }) },
          { id: 'Active', control: this.createControl({ value: selectedOptionValue }) },
          { id: 'Disabled', control: this.createControl({ disabled: true, value: selectedOptionValue }) },
          { id: 'Error', control: this.createControl({ error: true, value: selectedOptionValue }) },
        ],
      },
    ];
  }

  private createControl(config?: { disabled?: boolean; error?: boolean; value?: any }): FormControl {
    const validators = config?.error
      ? [
          Validators.required,
          (control: AbstractControl): ValidationErrors => {
            const value = control.value ? (Array.isArray(control.value) ? control.value : [control.value]) : [];
            if (value.length && !(value as SchemaElement[]).map((v) => v.Path).some((path) => path === 'TextContent')) {
              return { CONTAINS_TextContent: true };
            }
            return undefined;
          },
        ]
      : config?.disabled
        ? [Validators.required]
        : [];

    const fc = new FormControl(null, validators);
    if (config?.disabled) fc.disable();
    if (config?.error) fc.markAsTouched();
    if (config?.value) fc.setValue(config.value);

    return fc;
  }

  getSchemaElementDisplayValue = (element: SchemaElement) =>
    `${element.Origin === 'EXTERNAL' ? 'EXT-' : ''}${element.ShortName} - ${element.FieldName}`;

  private initSchemaOptions(): void {
    this.schemaOptions$ = this.schemaService.getDescriptiveSchemaTree();
  }
}
