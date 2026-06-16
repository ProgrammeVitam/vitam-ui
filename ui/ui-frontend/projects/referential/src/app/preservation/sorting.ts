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
import { Direction } from 'vitamui-library';
/**
 * Sort by key
 * @param key to sort on.
 * @param factor 1 asc sort, -1 desc sort
 */
export const sortByKey =
  <T>(key: keyof T, factor: 1 | -1) =>
  (a: T, b: T) => {
    const av = a[key];
    const bv = b[key];

    const normalize = (v: any) => (v instanceof Date ? v.getTime() : v);

    const A = normalize(av);
    const B = normalize(bv);

    let result: number;

    if (typeof A === 'string' && typeof B === 'string') {
      result = A.localeCompare(B);
    } else if (typeof A === 'number' && typeof B === 'number') {
      result = A - B;
    } else {
      result = String(A).localeCompare(String(B));
    }

    return result * factor;
  };

export const factorOf = (direction: Direction) => {
  return direction === Direction.DESCENDANT ? -1 : 1;
};
