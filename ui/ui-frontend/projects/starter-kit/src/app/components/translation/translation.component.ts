import { Component, OnInit } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';

const TRANSLATE_GET_PATH = 'TRANSLATION.TRANSLATE_GET';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'starter-kit-translation',
  templateUrl: './translation.component.html',
  styleUrls: ['./translation.component.scss'],
})
export class TranslationComponent implements OnInit {
  public nbApplesTextMap: { [k: string]: string } = {
    '=': 'TRANSLATION.TRANSLATE_NUMBER.ZERO', // In case of no value
    '=0': 'TRANSLATION.TRANSLATE_NUMBER.ZERO',
    '=1': 'TRANSLATION.TRANSLATE_NUMBER.SINGULAR',
    other: 'TRANSLATION.TRANSLATE_NUMBER.PLURAL',
  };

  public firstInput = new FormControl('Test1', [Validators.maxLength(10), Validators.required]);
  public secondInput = new FormControl('Test2', [Validators.maxLength(10), Validators.required]);
  public nbApples = new FormControl('0', [Validators.maxLength(3), Validators.required]);

  public myInstantText: string;
  public myGetTexts: string[];

  constructor(private translateService: TranslateService) {}

  ngOnInit(): void {
    // Will not work because it is too early in ngOnInit
    this.myInstantText = this.translateService.instant('TRANSLATION.TRANSLATE_INSTANT');

    this.translateService.get(TRANSLATE_GET_PATH).subscribe((translatedTexts: { [key: string]: string }) => {
      this.myGetTexts = [translatedTexts.TRANSLATE_GET_1, translatedTexts.TRANSLATE_GET_2];
    });
  }

  getMyInstantTrad(): string {
    return this.translateService.instant('TRANSLATION.TRANSLATE_INSTANT');
  }
}
