/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
import { Component, OnInit } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';

const TRANSLATE_GET_PATH = 'TRANSLATION.TRANSLATE_GET';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'design-system-translation',
  templateUrl: './translation.component.html',
  styleUrls: ['./translation.component.scss'],
  standalone: false,
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
