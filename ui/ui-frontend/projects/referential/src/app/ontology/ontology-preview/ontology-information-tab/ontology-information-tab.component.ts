import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { Ontology, Option, diff } from 'vitamui-library';
import { setTypeDetailAndStringSize } from '../../../../../../vitamui-library/src/app/modules/models/ontology/ontology.utils';
import { RULE_TYPES } from '../../../rule/rules.constants';
import { OntologyService } from '../../ontology.service';

@Component({
  selector: 'app-ontology-information-tab',
  templateUrl: './ontology-information-tab.component.html',
  styleUrls: ['./ontology-information-tab.component.scss'],
})
export class OntologyInformationTabComponent implements OnInit {
  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();
  form: FormGroup;

  isInternal = true;

  submited = false;

  sizeFieldVisible = false;

  // FIXME: Get list from common var ?
  types: Option[] = [
    { key: 'DATE', label: 'Date', info: '' },
    { key: 'TEXT', label: 'Texte', info: '' },
    { key: 'KEYWORD', label: 'Mot clé', info: '' },
    { key: 'BOOLEAN', label: 'Boolean', info: '' },
    { key: 'LONG', label: 'Long', info: '' },
    { key: 'DOUBLE', label: 'Double', info: '' },
    { key: 'ENUM', label: 'Énumérer', info: '' },
    { key: 'GEO_POINT', label: 'Point Géographique', info: '' },
  ];

  // FIXME: Get list from common var ?
  collections: Option[] = [
    { key: 'Unit', label: 'Unité Archivistique', info: '' },
    { key: 'ObjectGroup', label: "Groupe d'objet", info: '' },
  ];

  sizes: Option[] = [
    { key: 'SHORT', label: 'Court', info: '' },
    { key: 'MEDIUM', label: 'Moyen', info: '' },
    { key: 'LARGE', label: 'Long', info: '' },
  ];

  @Input()
  set inputOntology(ontology: Ontology) {
    this._inputOntology = ontology;

    this.isInternal = ontology.origin === 'INTERNAL';

    if (!ontology.description) {
      this._inputOntology.description = '';
    }

    if (!ontology.collections) {
      this._inputOntology.collections = [];
    }

    this.sizeFieldVisible = this._inputOntology.type === 'TEXT';

    this.resetForm(this.inputOntology);
    this.updated.emit(false);
  }

  get inputOntology(): Ontology {
    return this._inputOntology;
  }

  private _inputOntology: Ontology;

  @Input()
  set readOnly(readOnly: boolean) {
    if (readOnly && this.form.enabled) {
      this.form.disable({ emitEvent: false });
    } else if (this.form.disabled) {
      this.form.enable({ emitEvent: false });
      this.form.get('identifier').disable({ emitEvent: false });
    }
  }

  previousValue = (): Ontology => {
    return this._inputOntology;
  };

  constructor(
    private formBuilder: FormBuilder,
    private ontologyService: OntologyService,
  ) {
    this.form = this.formBuilder.group({
      identifier: [{ disabled: true }, Validators.required],
      shortName: [{ disabled: true }, Validators.required],
      type: [{ value: null, disabled: true }, Validators.required],
      typeDetail: [{ value: null, disabled: true }],
      stringSize: [{ value: null, disabled: true }],
      collections: [{ value: null, disabled: true }, Validators.required],
      description: [{ value: null, disabled: true }],
      creationDate: [{ value: null, disabled: true }],
    });
  }

  ngOnInit(): void {
    if (this._inputOntology.origin === 'EXTERNAL') {
      this.form.enable({ emitEvent: false });
    }
    this.form.controls.identifier.disable({ emitEvent: true });
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  onIndexingModeChange(key: string) {
    if (!this.isInternal) {
      this.sizeFieldVisible = key === 'TEXT';
    }

    setTypeDetailAndStringSize(key, this.form);
  }

  prepareSubmit(): Observable<Ontology> {
    const payload = {
      id: this.previousValue().id,
      identifier: this.previousValue().identifier,
      ...this.form.value,
    };

    return of(payload).pipe(switchMap((formData: any) => this.ontologyService.patch(formData).pipe(catchError(() => of(null)))));
  }

  onSubmit() {
    this.submited = true;
    this.prepareSubmit().subscribe(
      () => {
        this.ontologyService.get(this._inputOntology.identifier).subscribe((response) => {
          this.submited = false;
          this.inputOntology = response;
          this.ontologyService.updated.next(this.inputOntology);
        });
      },
      () => {
        this.submited = false;
      },
    );
  }

  resetForm(ontology: Ontology) {
    this.form.reset(ontology, { emitEvent: false });
  }

  protected readonly RULE_TYPES = RULE_TYPES;
}
