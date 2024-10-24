import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { Ontology, diff, setTypeDetailAndStringSize } from 'vitamui-library';
import { RULE_TYPES } from '../../../rule/rules.constants';
import { OntologyService } from '../../ontology.service';
import { collections, types, sizes } from '../../ontology-form-options';

@Component({
  selector: 'app-ontology-information-tab',
  templateUrl: './ontology-information-tab.component.html',
  styleUrls: ['./ontology-information-tab.component.scss'],
})
export class OntologyInformationTabComponent implements OnInit {
  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();
  form: FormGroup;
  isInternal = true;
  submitted = false;
  sizeFieldVisible = false;
  types = types;
  collections = collections;
  sizes = sizes;

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

    this.sizeFieldVisible = ['TEXT', 'GEO_POINT', 'KEYWORD'].includes(this._inputOntology.type);

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
      shortName: [{ value: null, disabled: true }, Validators.required],
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
      this.sizeFieldVisible = ['TEXT', 'GEO_POINT', 'KEYWORD'].includes(key);
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
    this.submitted = true;
    this.prepareSubmit().subscribe(
      () => {
        this.ontologyService.get(this._inputOntology.identifier).subscribe((response) => {
          this.submitted = false;
          this.inputOntology = response;
          this.ontologyService.updated.next(this.inputOntology);
        });
      },
      () => {
        this.submitted = false;
      },
    );
  }

  resetForm(ontology: Ontology) {
    this.form.reset(ontology, { emitEvent: false });
  }

  protected readonly RULE_TYPES = RULE_TYPES;
}
