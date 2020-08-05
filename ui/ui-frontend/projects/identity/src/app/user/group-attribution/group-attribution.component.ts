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
/* tslint:disable: no-use-before-declare */

import { CollapseDirective, Group, User } from 'ui-frontend-common';

import { animate, state, style, transition, trigger } from '@angular/animations';
import { Component, forwardRef, Inject, OnInit } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { GroupSelection } from '../group-selection.interface';
import { UserService } from '../user.service';

export const GROUP_ATTRIBUTION_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => GroupAttributionComponent),
  multi: true
};

@Component({
  selector: 'app-group-attribution',
  templateUrl: './group-attribution.component.html',
  styleUrls: ['./group-attribution.component.scss'],
  providers: [GROUP_ATTRIBUTION_VALUE_ACCESSOR],
  animations: [
    trigger('expansion', [
      state('collapsed', style({ height: '0px', visibility: 'hidden' })),
      state('expanded', style({ height: '*', visibility: 'visible' })),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4,0.0,0.2,1)')),
    ]),
  ]
})
export class GroupAttributionComponent implements OnInit {


  user: User;
  activeGroups: GroupSelection[];
  selectedGroupName: string;
  CUSTOMER_ACTIVE_PROFILE_GROUPS_INDEX = 2;

  public profileGroupChange(event: Group): void {
    this.user.groupId = event.id;
    this.selectedGroupName = event.name;
  }

  constructor(
    private userService: UserService,
    public dialogRef: MatDialogRef<GroupAttributionComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any) { }

  public ngOnInit(): void {
    this.user = this.data[0];
    this.resetActiveGroups();
  }

  public resetActiveGroups(): void {
    this.activeGroups = this.data[this.CUSTOMER_ACTIVE_PROFILE_GROUPS_INDEX];
    this.activeGroups.sort((a, b) => a.name.toUpperCase() < b.name.toUpperCase() ? -1 : 1);
    if (this.data[1]) {
      this.selectedGroupName = this.data[1].name;
      const selectedGroup = this.activeGroups.find((group) => group.id === this.data[1].id);
      if (selectedGroup) {
        selectedGroup.selected = true;
      }
    }
  }

  public removeGroup(): void {
    this.selectedGroupName = null;
    this.user.groupId = null;
    this.unselectAllProfileGroups();
  }

  public updateGroup(groupId: string, groupName: string, groupLevel: string, collapseDirective: CollapseDirective): void {
    this.selectedGroupName = groupName;
    this.user.groupId = groupId;
    this.user.level = groupLevel;
    this.unselectAllProfileGroups();
    const selectedGroup = this.activeGroups.find((group) => group.id === groupId);
    if (selectedGroup) {
      selectedGroup.selected = true;
      collapseDirective.collapse();
    }
  }

  public unselectAllProfileGroups(): void {
    this.activeGroups.forEach((group) => group.selected = false);
  }

  public saveUserUpdate(): void {
    this.userService.patch({ id: this.user.id, groupId: this.user.groupId })
      .subscribe(
        () => this.dialogRef.close(true),
        (error) => {
          console.error(error);
        });
  }

  public onCancel(): void {
    this.unselectAllProfileGroups();
    this.dialogRef.close();
  }

  public onSearch(text?: string): void {
    this.resetActiveGroups();
    if (text !== null && text.length > 0) {
      this.activeGroups = this.activeGroups.filter((group) => group.name.includes(text) || group.description.includes(text));
    }
  }
}
