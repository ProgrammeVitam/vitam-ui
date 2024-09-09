/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { EditObject } from '../models/edit-object.model';
import { FormControl, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { EditorTextareaComponent } from './editor-textarea.component';

@Component({
  selector: 'vitamui-editor-list-textarea',
  template: `
    <vitamui-editor-textarea
      [control]="control"
      [label]="editObject.displayRule?.ui?.label"
      [hint]="editObject.hint"
      [required]="editObject.required"
    ></vitamui-editor-textarea>
  `,
  imports: [EditorTextareaComponent],
  standalone: true,
})
export class EditorListTextareaComponent implements OnInit, OnDestroy {
  @Input({ required: true }) editObject!: EditObject;

  control: FormControl;

  private subscriptions = new Subscription();

  ngOnInit() {
    const values: any[] = [...this.editObject.control.value] as any[];
    const firstValue = values.shift();
    const validators = Object.keys(this.editObject).reduce((acc, key) => {
      if (key === 'required' && this.editObject[key]) acc.push(Validators.required);
      if (key === 'pattern' && this.editObject[key]) acc.push(Validators.pattern(this.editObject[key]));

      return acc;
    }, []);

    this.control = new FormControl(firstValue, validators);
    this.subscriptions.add(
      this.control.valueChanges.subscribe((value) => {
        this.editObject.control.setValue(value ? [value] : []);
        this.editObject.control.markAsDirty();
      }),
    );
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }
}
