import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { Subject } from 'rxjs';
import { Customer, Theme, ThemeService } from 'ui-frontend-common';

@Component({
  selector: 'app-homepage-message',
  templateUrl: './homepage-message.component.html',
  styleUrls: ['./homepage-message.component.scss']
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
  public formToSend = new EventEmitter<{form: FormGroup}>();

  private portalMessage: string;
  private portalTitle: string;

  private defaultTheme: Theme = this.themeService.defaultTheme;

  constructor(
    public dialogRef: MatDialogRef<HomepageMessageComponent>,
    private formBuilder: FormBuilder,
    private themeService: ThemeService
  ) {}

  ngOnDestroy(): void {
    this.destroy.next();
  }

  ngOnInit() {
    this.homepageMessageForm = this.formBuilder.group({
      id : null,
      portalTitle: ['', [Validators.required]],
      portalMessage: ['', [Validators.required, Validators.maxLength(500)]],
    });

    this.portalMessage = this.customer && this.customer.portalMessage ? this.customer.portalMessage : this.defaultTheme.portalMessage;
    this.portalTitle =  this.customer && this.customer.portalTitle ? this.customer.portalTitle : this.defaultTheme.portalTitle;

    this.homepageMessageForm.get('portalTitle').setValue(this.portalTitle);
    this.homepageMessageForm.get('portalMessage').setValue(this.portalMessage);

    if (this.customer && this.customer.id) {
      this.homepageMessageForm.get('id').setValue(this.customer.id);
    }

    this.formToSend.emit({form: this.homepageMessageForm});

    this.homepageMessageForm.valueChanges.subscribe(() => {
      this.formToSend.emit({form: this.homepageMessageForm});
    });
  }

  public onClick(): void {
    console.log('[onClick]');
  }
}

