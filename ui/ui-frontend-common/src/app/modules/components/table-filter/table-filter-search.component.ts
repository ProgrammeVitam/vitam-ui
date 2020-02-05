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
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'vitamui-common-table-filter-search',
  templateUrl: './table-filter-search.component.html',
  styleUrls: ['./table-filter-search.component.scss']
})
export class TableFilterSearchComponent implements OnInit {

  @Input() filter: any[];
  @Input()
  set options(options: Array<{ label: string, value: any }>) {
    this._options = options;
    this.optionDisplayMap = this._options.map(() => true);
  }
  // tslint:disable-next-line:variable-name
  _options: Array<{ label: string, value: any }>;
  @Input() emptyValueOption: string;

  @Output() readonly filterChange = new EventEmitter<any[]>();
  @Output() readonly filterClose = new EventEmitter();

  optionDisplayMap: Array<boolean>;
  hideEmptyValueOption = false;

  constructor() { }

  ngOnInit() {
  }

  searchOptions(search: string) {
    if (!search) {
      this.optionDisplayMap = this._options.map(() => true);
      this.hideEmptyValueOption = false;

      return;
    }

    this.optionDisplayMap = this._options.map((option) => option.label.toLowerCase().includes(search.toLowerCase()));

    this.hideEmptyValueOption = !this.emptyValueOption || !this.emptyValueOption.toLowerCase().includes(search.toLowerCase());
  }

  resetOptionList() {
    this.optionDisplayMap = this._options.map(() => true);
  }

}
