import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { Option, VitamUICommonInputComponent, VitamUIInputErrorComponent, VitamUITextareaComponent } from 'vitamui-library';
import { TranslateModule } from '@ngx-translate/core';
import { MatLegacyOptionModule } from '@angular/material/legacy-core';
import { MatLegacySelectModule } from '@angular/material/legacy-select';
import { MatLegacyFormFieldModule } from '@angular/material/legacy-form-field';
import { NgIf, NgFor } from '@angular/common';

@Component({
  selector: 'app-homepage-message-translation',
  templateUrl: './homepage-message-translation.html',
  styleUrls: ['./homepage-message-translation.scss'],
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgIf,
    VitamUICommonInputComponent,
    MatLegacyFormFieldModule,
    MatLegacySelectModule,
    NgFor,
    MatLegacyOptionModule,
    TranslateModule,
    VitamUIInputErrorComponent,
    VitamUITextareaComponent,
  ],
})
export class HomepageMessageTranslationComponent implements OnInit, OnDestroy {
  @Input()
  public form: FormGroup;

  @Input()
  public index: number;

  @Input()
  public languages: Option[];

  @Output()
  public formChange = new EventEmitter<{ form: FormGroup }>();

  @Output()
  public formRemove = new EventEmitter<{ form: FormGroup }>();

  private subscription: Subscription;

  constructor() {}

  ngOnInit() {
    this.subscription = this.form.valueChanges.subscribe(() => {
      this.formChange.emit({ form: this.form });
    });
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  public onRemove(): void {
    this.formRemove.emit({ form: this.form });
  }
}
