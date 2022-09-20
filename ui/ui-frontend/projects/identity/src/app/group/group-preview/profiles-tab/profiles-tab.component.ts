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
import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import { Application, ApplicationService, Group } from 'ui-frontend-common';
import { GroupService } from '../../group.service';
import { ProfilesEditComponent } from './profiles-edit/profiles-edit.component';

@Component({
  selector: 'app-profiles-tab',
  templateUrl: './profiles-tab.component.html',
  styleUrls: ['./profiles-tab.component.scss']
})
export class ProfilesTabComponent implements OnInit, OnDestroy {

  @Input()
  set group(group: Group) {
    this._group = group;
  }
  get group(): Group { return this._group; }
  private _group: Group;

  @Input() readOnly: boolean;

  profilesDisplay: any [];
  updatedGroup: Subscription;

  constructor(private dialog: MatDialog, private groupService: GroupService, private applicationService: ApplicationService) { }

  ngOnInit() {
    this.initializeProfilesList(this.group);
    this.updatedGroup = this.groupService.updated.subscribe((updatedGroup: Group) => {
      this.group = updatedGroup;
      this.initializeProfilesList(this.group);

    });
  }

  initializeProfilesList(group: Group) {
    this.profilesDisplay = [];
    group.profiles.forEach((profile) =>
      this.profilesDisplay.push({
        id: profile.id,
        appName: this.findApplicationName(profile.applicationName),
        tenantName: profile.tenantName,
        profileName: profile.name,
        description: profile.description
      })
    );

    this.profilesDisplay.sort((first, second) => {
      if (first.appName > second.appName) {
        return 1;
      }
      if (second.appName > first.appName) {
        return -1;
      }

      return 0;
    });

  }

  openEditProfilesDialog() {
    this.dialog.open(ProfilesEditComponent, {
      data: {
        group: this.group,
      },
      autoFocus: false,
      disableClose: true,
      panelClass: 'vitamui-modal'
    });
  }

  findApplicationName(appId: string): string {
    const apps: Application[] = this.applicationService.applications;
    if (appId && apps && apps.length > 0) {
      const applicationsMatch = apps.filter((application) => application.identifier === appId);
      if (applicationsMatch && applicationsMatch.length > 0) {
        return applicationsMatch[0].name;
      }
    }
    return appId;
  }

  ngOnDestroy() {
    this.updatedGroup.unsubscribe();
  }

}
