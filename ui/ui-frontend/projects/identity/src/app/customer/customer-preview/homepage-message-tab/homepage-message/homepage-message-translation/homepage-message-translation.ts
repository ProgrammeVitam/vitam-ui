import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Subscription } from 'rxjs';
import { Option } from 'vitamui-library';

@Component({
  selector: 'app-homepage-message-translation',
  templateUrl: './homepage-message-translation.html',
  styleUrls: ['./homepage-message-translation.scss'],
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
