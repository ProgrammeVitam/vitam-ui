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
import { Pipe, PipeTransform } from '@angular/core';
import { Logger } from '../logger/logger';

const NUMBER_OF_BYTES_IN_ONE_KB = 1024;
const DEFAULT_PRECISION = 2;

@Pipe({ name: 'bytes' })
export class BytesPipe implements PipeTransform {

  constructor(private logger: Logger) { }

  transform(value: any, precision = DEFAULT_PRECISION): any {
    if (isNaN(parseFloat(value)) || !isFinite(value)) {
      this.logger.log(this, 'BytesPipe: value [' + value + '] is not a number');
      return '';
    }

    // TODO internationalization of units
    const units = ['bytes', 'kB', 'MB', 'GB', 'TB', 'PB'];
    let power = Math.floor(Math.log(value) / Math.log(NUMBER_OF_BYTES_IN_ONE_KB));
    if (power >= units.length) {
      power = units.length - 1;
    }

    return (value / Math.pow(NUMBER_OF_BYTES_IN_ONE_KB, power)).toFixed(precision) + ' ' + units[power];
  }
}
