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

import { Component, EventEmitter, Inject, Input, OnInit, Output } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { collapseAnimation, rotateAnimation } from 'ui-frontend-common';
import { GroupSelection } from './../../group-selection.interface';

@Component({
  selector: 'app-group-list',
  templateUrl: './group-list.component.html',
  styleUrls: ['./group-list.component.scss'],
  animations: [
    collapseAnimation,
    rotateAnimation,
  ]
})
export class GroupListComponent implements OnInit {


  public groupName: string;

  @Input()
  public groups: GroupSelection[];

  @Input()
  public searchActiv = false;

  @Output()
  public selectedGroupEvent = new EventEmitter<GroupSelection>();

  CUSTOMER_ACTIVE_PROFILE_GROUPS_INDEX = 2;


  constructor( @Inject(MAT_DIALOG_DATA) public data: any) { }

  ngOnInit() {
  }


  updateGroup(groupId: string, groupName: string) {
    this.groupName = groupName;
    this.unselectAllGroups();
    const selectedGroup = this.groups.find((group) => group.id === groupId);
    selectedGroup.selected = true;
    this.selectedGroupEvent.emit(selectedGroup);
  }


  unselectAllGroups() {
    this.groups.forEach((group) => group.selected = false);
  }

  public findGroup(id: string): GroupSelection {
    return this.groups.find(value => value.id === id);
  }
  public onSearch(text?: string): void {
    this.resetgroups();
    if (text !== null && text.length > 0) {
      this.groups = this.groups.filter((group) => group.name.includes(text) || group.description.includes(text));
    }
  }


  public resetgroups(): void {
    this.groups = this.data[this.CUSTOMER_ACTIVE_PROFILE_GROUPS_INDEX];
    this.groups.sort((a, b) => a.name.toUpperCase() < b.name.toUpperCase() ? -1 : 1);
    if (this.data[1]) {
      const selectedGroup = this.groups.find((group) => group.id === this.data[1].id);
      if (selectedGroup) {
        selectedGroup.selected = true;
      }
    }
  }
}
