import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { AttachmentType, Logo, ThemeService } from 'ui-frontend-common';

@Component({
  selector: 'app-graphic-identity-form',
  templateUrl: './graphic-identity-form.component.html',
  styleUrls: ['./graphic-identity-form.component.scss']
})
export class GraphicIdentityFormComponent implements OnInit {

  @Input()
  public graphicIdentityForm: FormGroup;

  @Input()
  public disabled = false;

  @Output()
  public formChange = new EventEmitter<{form: FormGroup, logos: Logo[]}>();

  public ATTACHMENT_TYPE = AttachmentType;
  public logosSize = {width: 280, height: 100};

  private logos: Logo[] = [];

  constructor(private themeService: ThemeService) { }

  ngOnInit() {

    this.applyTheme();

    this.graphicIdentityForm.valueChanges.subscribe(() => {
      this.formChange.emit({form: this.graphicIdentityForm, logos: this.logos});
    });

    this.graphicIdentityForm.get('themeColors').valueChanges.subscribe(() => {
      this.graphicIdentityForm.updateValueAndValidity();
      this.applyTheme();
      this.formChange.emit({form: this.graphicIdentityForm, logos: this.logos});
    });
  }

  private applyTheme(): void {
    if (this.graphicIdentityForm.valid) {
      const colors = this.themeService.getThemeColors(this.graphicIdentityForm.get('themeColors').value);
      this.themeService.overloadLocalTheme(colors, '#toOverride');
    }
  }

  public addOrReplaceLogo(type: AttachmentType, data: File): void {
    const logo: Logo = { attr: type, file: data };
    const index = this.logos.findIndex((e: Logo) => e.attr === logo.attr);
    if (index === -1) {
        this.logos.push(logo);
    } else {
        this.logos[index] = logo;
    }
    this.formChange.emit({form: this.graphicIdentityForm, logos: this.logos});
  }
}
