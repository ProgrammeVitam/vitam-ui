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
import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { Observable, Subscription } from 'rxjs';
import { tap } from 'rxjs/operators';

@Component({
  selector: 'vitamui-common-archive-unit-count',
  templateUrl: './archive-unit-count.component.html',
  styleUrls: ['./archive-unit-count.component.scss'],
})
export class ArchiveUnitCountComponent implements OnInit, OnChanges {
  @Input() search: Observable<number>;
  @Input() archiveUnitCount = 0;
  @Input() selectedArchiveUnitCount = 0;
  @Input() pending!: boolean;
  @Input() threshold!: number;
  @Output() archiveUnitCountChange = new EventEmitter<number>();
  @Output() pendingChange = new EventEmitter<boolean>();

  canLoadExactCount = false;
  exactCountLoaded = false;

  private subscriptions = new Subscription();

  constructor() {}

  ngOnInit(): void {}

  ngOnChanges(changes: SimpleChanges): void {
    const { archiveUnitCount, threshold } = changes;

    if (archiveUnitCount || threshold) {
      this.updateCanLoadExactCount();
    }
  }

  private isValidExactCount(exactCount: number): boolean {
    return exactCount >= 0;
  }

  private shouldChangeArchiveUnitCount(exactCount: number): boolean {
    return this.archiveUnitCount !== exactCount && this.isValidExactCount(exactCount);
  }

  private handleExactCount(exactCount: number) {
    this.exactCountLoaded = this.isValidExactCount(exactCount);
    this.updateCanLoadExactCount();
    if (this.shouldChangeArchiveUnitCount(exactCount)) {
      this.archiveUnitCount = exactCount;
    }
  }

  updateCanLoadExactCount() {
    this.canLoadExactCount = !this.exactCountLoaded && this.archiveUnitCount >= this.threshold;
  }

  loadExactCount() {
    const updatePending = (value: boolean) => {
      this.pending = value;
      this.pendingChange.emit(this.pending);
    };
    const notPending = () => {
      updatePending(false);
    };

    updatePending(true);
    this.subscriptions.add(
      this.search.pipe(tap(notPending, notPending)).subscribe(
        (exactCount: number) => {
          this.handleExactCount(exactCount);
        },
        (error: HttpErrorResponse) => {
          console.error(error.message);

          this.handleExactCount(-1);
        }
      )
    );
  }
}
