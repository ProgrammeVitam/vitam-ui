import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
    selector: 'app-homepage-message-translation',
    templateUrl: './homepage-message-translation.html',
    styleUrls: ['./homepage-message-translation.scss']
})
export class HomepageMessageTranslationComponent implements OnInit {

    public form: FormGroup;

    @Input()
    public messageTranslation: {id: number, index: number, language: string, title: string, description: string };

    @Output()
    public formChange = new EventEmitter<{form: FormGroup}>();

    @Output()
    public formRemove = new EventEmitter<{form: FormGroup}>();

    constructor(private formBuilder: FormBuilder) { }

    ngOnInit() {

        this.form = this.formBuilder.group({
            id: [null],
            index: [null],
            language: ['', Validators.required],
            portalTitle: ['', [Validators.required]],
            portalMessage: ['', [Validators.required, Validators.maxLength(500)]],
        });

        this.form.get('id').setValue(this.messageTranslation.id);
        this.form.get('index').setValue(this.messageTranslation.index);
        this.form.get('language').setValue(this.messageTranslation.language);
        this.form.get('portalTitle').setValue(this.messageTranslation.title);
        this.form.get('portalMessage').setValue(this.messageTranslation.description);

        this.formChange.emit({form: this.form});

        this.form.valueChanges.subscribe(() => {
            this.formChange.emit({form: this.form});
          });
    }

    public onRemove(): void {
        this.formRemove.emit({form: this.form});
    }
}
