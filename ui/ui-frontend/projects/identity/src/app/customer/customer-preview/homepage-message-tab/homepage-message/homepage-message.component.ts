import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Customer, Theme, ThemeService } from 'ui-frontend-common';

@Component({
  selector: 'app-homepage-message',
  templateUrl: './homepage-message.component.html',
  styleUrls: ['./homepage-message.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomepageMessageComponent implements OnInit, OnDestroy {
  private destroy = new Subject();

  @Input()
  public homepageMessageForm: FormGroup;

  @Input()
  public customer?: Customer;

  public defaultForm: FormGroup;
  public customerForm: FormGroup;

  @Output()
  public formToSend = new EventEmitter<{ form: FormGroup }>();

  private portalMessage: string;
  private portalTitle: string;

  private defaultTheme: Theme = this.themeService.defaultTheme;

  constructor(
    public dialogRef: MatDialogRef<HomepageMessageComponent>,
    private formBuilder: FormBuilder,
    private themeService: ThemeService
  ) { }

  ngOnInit() {

    let idCustomer = null;
    if (this.customer && this.customer.id) {
      idCustomer = this.customer.id;
    }

    this.portalMessage =
      this.customer && this.customer.portalMessage
        ? this.customer.portalMessage
        : this.defaultTheme.portalMessage;
    this.portalTitle =
      this.customer && this.customer.portalTitle
        ? this.customer.portalTitle
        : this.defaultTheme.portalTitle;

    this.homepageMessageForm = this.formBuilder.group({
      id: idCustomer,
      portalTitle: [this.portalTitle, [Validators.required]],
      portalMessage: [this.portalMessage, [Validators.required, Validators.maxLength(500)]],
      translations: this.formBuilder.array([])
    });

    this.formToSend.emit({ form: this.homepageMessageForm });

    this.homepageMessageForm.valueChanges.pipe(takeUntil(this.destroy)).subscribe(() => {
      this.formToSend.emit({ form: this.homepageMessageForm });
    });
  }

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }

  public onAdd(): void {
    const emptyTranslation = this.formBuilder.group({
      language: ['', Validators.required],
      portalTitle: ['', [Validators.required]],
      portalMessage: ['', [Validators.required, Validators.maxLength(500)]],
  });

    this.homepageMessageForm.get('translations').value.push(emptyTranslation);
    this.formToSend.emit({ form: this.homepageMessageForm });
  }

  public update(): void {
    this.formToSend.emit({ form: this.homepageMessageForm });
  }

  public remove(index: number): void {

    this.homepageMessageForm.get('translations').value.splice(index, 1);
    this.formToSend.emit({ form: this.homepageMessageForm });
  }
}
