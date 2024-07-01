/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { Component, Input, OnInit } from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ObjectQualifierType, VersionWithQualifierDto } from '../../../models';
import { BytesPipe } from '../../../pipes/bytes.pipe';
import { NgClass, NgFor, NgIf } from '@angular/common';

interface Measurement {
  name: string;
  value: number;
  unit: string;
}

interface DisplayValue {
  display: {
    key: string;
    value: string;
  };
  originalKey: string;
  originalValue: any;
}

interface Section {
  name: string;
  rows: DisplayValue[][];
}

type MeasurementDisplayMode = 'SYMBOL' | 'NAME';

@Component({
  selector: 'vitamui-common-physical-archive-viewer',
  templateUrl: './physical-archive-viewer.component.html',
  styleUrls: ['./physical-archive-viewer.component.scss'],
  standalone: true,
  imports: [NgIf, NgClass, NgFor, TranslateModule, BytesPipe],
})
export class PhysicalArchiveViewerComponent implements OnInit {
  @Input() archive: VersionWithQualifierDto;

  // Component configuration
  private measurementDisplayMode: MeasurementDisplayMode = 'NAME';
  private displayAll = false;
  private name = 'PhysicalDimensions';
  private items = ['Width', 'Height', 'Depth', 'Shape', 'Diameter', 'Length', 'Thickness', 'Weight', 'NumberOfPage'];
  private columns = 2;

  isPhysical = false;

  section: Section = {
    name: 'ARCHIVE_SEARCH.ARCHIVE_UNIT_PREVIEW.FIELDS.PhysicalDimensions',
    rows: [],
  };

  constructor(private translateService: TranslateService) {}

  ngOnInit(): void {
    if (!this.archive) {
      console.warn('No archive passed as parameter');

      return;
    }

    this.isPhysical = this.archive.qualifier === ObjectQualifierType.PHYSICALMASTER;

    if (!this.isPhysical) {
      console.warn('Non physical archive passed as parameter');

      return;
    }

    this.computeSection();
  }

  toggleArchiveMetadataDisplay(): void {
    this.archive.opened = !this.archive.opened;
  }

  private isAllowedDisplayValue(displayValue: DisplayValue): boolean {
    return this.displayAll || this.items.includes(displayValue.originalKey);
  }

  private translateMeasurementDisplayValue(displayValue: DisplayValue): DisplayValue {
    const { originalValue } = displayValue;

    if (this.isMeasurement(originalValue)) {
      const plural = (this.measurementDisplayMode === 'NAME' && originalValue.value) > 1 ? 's' : '';
      const translatedKey = this.translateService.instant(`SEDA.MEASUREMENT_TYPE.${originalValue.name}`);
      const translatedUnit = this.translateService.instant(`SEDA.MEASUREMENT_UNIT.${originalValue.unit}.${this.measurementDisplayMode}`);
      const translatedValue = `${originalValue.value} ${translatedUnit}${plural}`;

      return {
        ...displayValue,
        display: {
          key: translatedKey,
          value: translatedValue,
        },
      };
    }

    return displayValue;
  }

  private translateBasicDisplayValue(displayValue: DisplayValue): DisplayValue {
    const { originalKey, originalValue } = displayValue;

    if (typeof originalValue !== 'object') {
      const translatedKey = this.translateService.instant(`SEDA.MEASUREMENT_TYPE.${originalKey}`);

      return {
        ...displayValue,
        display: {
          key: translatedKey,
          value: originalValue,
        },
      };
    }

    return displayValue;
  }

  private computeDisplayValues(): DisplayValue[] {
    const section = (this.archive as any)[this.name];
    const sectionKeys = Object.keys(section);

    return sectionKeys.map((key: string): DisplayValue => {
      const value = section[key];

      if (typeof value === 'object') {
        const { dvalue, unit } = value;
        const measurement = {
          name: key,
          value: dvalue,
          unit,
        };
        const displayValue = this.measurementToDisplayValue(measurement);

        if (displayValue) {
          return displayValue;
        }
      }

      return {
        display: {
          key,
          value,
        },
        originalKey: key,
        originalValue: value,
      };
    });
  }

  private computeSectionRows(displayValues: DisplayValue[]): DisplayValue[][] {
    const rowCount = Math.ceil(this.items.length / this.columns);
    const columnCount = this.columns;
    const rows = [];

    for (let row = 0; row < rowCount; row++) {
      const columns: DisplayValue[] = [];

      for (let column = 0; column < columnCount; column++) {
        const index = row * this.columns + column;

        if (index < displayValues.length) {
          columns.push(displayValues[index]);
        }
      }

      rows.push(columns);
    }

    return rows;
  }

  private computeSection(): void {
    const displayValues = this.computeDisplayValues();
    const filteredDisplayValues = displayValues.filter((displayValue) => this.isAllowedDisplayValue(displayValue));
    const translatedDisplayValues = filteredDisplayValues
      .map((displayValue) => this.translateBasicDisplayValue(displayValue))
      .map((displayValue) => this.translateMeasurementDisplayValue(displayValue));

    this.section.rows = this.computeSectionRows(translatedDisplayValues);
  }

  private isMeasurement(value: any) {
    return typeof value === 'object' && value.value && value.unit;
  }

  private measurementToDisplayValue(measurement: Measurement): DisplayValue {
    if (!this.isMeasurement(measurement)) {
      console.warn('Data passed as parameter is not a measurement');

      return null;
    }

    return {
      display: {
        key: measurement.name,
        value: `${measurement.value} ${measurement.unit}`,
      },
      originalKey: measurement.name,
      originalValue: measurement,
    };
  }
}
