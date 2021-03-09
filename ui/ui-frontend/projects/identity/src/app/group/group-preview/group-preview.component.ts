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
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Subscription } from 'rxjs';
import { AuthService, Group, isLevelAllowed, StartupService } from 'ui-frontend-common';


import { GroupService } from '../group.service';

@Component({
  selector: 'app-group-preview',
  templateUrl: './group-preview.component.html',
  styleUrls: ['./group-preview.component.scss']
})
export class GroupPreviewComponent implements OnInit, OnDestroy {

  @Input() isPopup: boolean;

  @Input()
  set group(group: Group) {
    this._group = group;
    this.groupService.get(group.id).subscribe((groupRetrieved) => this.groupService.updated.next(groupRetrieved));
  }
  get group(): Group { return this._group; }
  private _group: Group;

  @Output() previewClose = new EventEmitter();

  private groupUpdateSub: Subscription;

  constructor(
    private groupService: GroupService, private authService: AuthService,
    private startupService: StartupService
  ) {}

  ngOnInit(): void {
    this.groupUpdateSub = this.groupService.updated.subscribe((updatedGroup) => {
      if (updatedGroup) {
        this._group = updatedGroup;
      }
    });
  }

  openPopup() {
    window.open(this.startupService.getConfigStringValue('UI_URL')
    + '/group/' + this._group.id, 'detailPopup', 'width=584, height=713, resizable=no, location=no');
    this.emitClose();
  }

  levelNotAllowed(): boolean {
    if (this._group) {
      return !isLevelAllowed(this.authService.user, this._group.level);
    }
  }

  emitClose() {
    this.previewClose.emit();
  }

  ngOnDestroy(): void {
    this.groupUpdateSub.unsubscribe();
  }

  filterEvents(event: any): boolean {
    return event.outDetail && (
      event.outDetail.includes('EXT_VITAMUI_CREATE_GROUP') ||
      event.outDetail.includes('EXT_VITAMUI_UPDATE_GROUP')
    );
  }
}
