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

import { Component, ElementRef, forwardRef, Input, OnInit, ViewChild } from '@angular/core';
import { ControlValueAccessor, FormControl, NG_VALUE_ACCESSOR } from '@angular/forms';
import { switchMap, tap } from 'rxjs/operators';
import { Application, ApplicationApiService, Option, Profile, ProfileService, VitamUIAutocompleteComponent } from 'ui-frontend-common';

import { HttpParams } from '@angular/common/http';
import { OptionTree } from './option-tree.interface';

export const PROFILES_FORM_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => ProfilesFormComponent),
  multi: true
};

@Component({
  selector: 'app-profiles-form',
  templateUrl: './profiles-form.component.html',
  styleUrls: ['./profiles-form.component.scss'],
  providers: [PROFILES_FORM_VALUE_ACCESSOR]
})
export class ProfilesFormComponent implements ControlValueAccessor, OnInit {

  profiles: Profile[] = [];
  profileIds: string[] = [];
  applicationsDetails: Application[] = [];

  public loading = true;

  @Input()
  showLevel = false;

  @Input() tenantIdentifier: number;

  @Input() applicationNameExclude: string [];
  @Input()
  set level(level: string) {
    this._level = level;
    this.getProfiles();
  }

  get level(): string {
    return this._level;
  }
  private _level: string;

  appSelect = new FormControl();
  tenantSelect = new FormControl();
  profileSelect = new FormControl();

  applications: OptionTree[] = [];
  filteredTenants: OptionTree[] = [];
  filteredProfiles: Option[] = [];

  @ViewChild('tenantInput', { static: false }) tenantInput: VitamUIAutocompleteComponent;
  @ViewChild('profileInput', { static: true }) profileInput: VitamUIAutocompleteComponent;
  @ViewChild('addButton', { static: true }) addButton: ElementRef;

  constructor(private rngProfileService: ProfileService, private appApiService: ApplicationApiService) {

  }

  ngOnInit(): void {
    this.getProfiles();
    this.appSelect.valueChanges
      .subscribe(() => {
        this.filterTenants();
        if (this.filteredTenants.length === 1) {
          this.tenantSelect.setValue(this.filteredTenants[0].key);
        } else {
          this.tenantSelect.setValue(null);
          if (!this.tenantIdentifier) {
            setTimeout(() => this.tenantInput.focus(), 0);
          }
        }
        this.toggleSelects();
      });
    this.tenantSelect.valueChanges.subscribe(() => {
      this.filterProfiles();
      if (this.filteredProfiles.length === 1) {
        this.profileSelect.setValue(this.filteredProfiles[0].key);
      } else {
        this.profileSelect.setValue(null);
      }
      this.toggleSelects();
      if (this.filteredProfiles.length === 1) {
        setTimeout(() => this.addButton.nativeElement.focus(), 0);
      } else {
        setTimeout(() => this.profileInput.focus(), 0);
      }
    });
  }

  getProfiles() {
    const params = new HttpParams().set('filterApp', 'false');
    this.appApiService.getAllByParams(params).pipe(
      tap((applications) => this.applicationsDetails = applications.APPLICATION_CONFIGURATION),
      switchMap(( ) => this.rngProfileService.list(this.level, this.tenantIdentifier, this.applicationNameExclude))
    ).subscribe((profiles) => {
      this.profiles = profiles;
      this.profileIds = this.profileIds.sort(byApplicationName(this.profiles, this.applicationsDetails));
      this.updateApplicationTree();
      this.loading = false;
    });
  }

  onChange = (_: any) => {};
  onTouched = () => {};

  writeValue(value: string[]) {
    this.profileIds = value || [];
    this.updateApplicationTree();
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  add() {
    if (!this.profileSelect.value) { return; }
    if (this.profileIds.includes(this.profileSelect.value)) { return; }
    this.profileIds.push(this.profileSelect.value);
    this.profileIds = this.profileIds.sort(byApplicationName(this.profiles, this.applicationsDetails));
    this.updateApplicationTree();
    this.onChange(this.profileIds);
  }

  remove(index: number) {
    this.profileIds = this.profileIds.filter((_, i) => i !== index);
    this.updateApplicationTree();
    this.onChange(this.profileIds);
  }

  getProfileFromId(id: string) {
    return this.profiles.find((profile) => profile.id === id);
  }

  getApplicationFromId(id: string) {
    const profileToDisplay = this.profiles.find((profile) => profile.id === id);
    if (profileToDisplay) {
      return this.applicationsDetails.find((app) => app.identifier === profileToDisplay.applicationName);
    } else {
      return { name: 'Non défini'};
    }
  }

  get canAddProfile() {
    return this.getProfileFromId(this.profileSelect.value) && !this.profileIds.includes(this.profileSelect.value);
  }

  private updateApplicationTree() {
    this.applications = [];
    const selectedProfiles = this.getSelectedProfiles();
    this.profiles
      .filter((profile) => profile.applicationName)
      .filter((profile) => {
        const foundProfile = selectedProfiles.find((p) => p.applicationName === profile.applicationName
        && p.tenantIdentifier === profile.tenantIdentifier);

        return !foundProfile;
      })
      .filter((profile) => !this.profileIds.includes(profile.id))
      .forEach((profile) => {
        const application = this.applications.find((a) => a.key === profile.applicationName);
        if (application) {
          const tenant = application.children.find((t) => t.key === profile.tenantIdentifier.toString());
          if (tenant) {
            if (!tenant.children.find((p) => p.key === profile.id)) {
              tenant.children.push({ key: profile.id, label: this.getProfileLabel(profile), info: profile.description });
            }
          } else {
            application.children.push(this.buildTenantOption(profile));
          }
        } else {
          this.applications.push(this.buildApplicationOption(profile));
        }
      });
    this.filterTenants();
    this.filterProfiles();
    this.toggleSelects();
  }

  getSelectedProfiles(): Profile[] {
    return this.profiles.filter((p) => this.profileIds.includes(p.id));
  }

  private toggleSelects() {
    if (this.filteredTenants.length > 0) {
      this.tenantSelect.enable({ emitEvent: false });
    } else {
      this.tenantSelect.disable({ emitEvent: false });
    }
    if (this.filteredProfiles.length > 0 ) {
      this.profileSelect.enable({ emitEvent: false });
    } else {
      this.profileSelect.disable({ emitEvent: false });
    }
  }

  private buildApplicationOption(profile: Profile): OptionTree {
    const application = this.applicationsDetails.find((app) => app.identifier === profile.applicationName);
    let appLabel = '';
    if (application) { appLabel = this.applicationsDetails.find((app) => app.identifier === application.identifier).name; }

    return {
      key: profile.applicationName,
      label: appLabel,
      children: [
        {
          key: profile.tenantIdentifier.toString(),
          label: profile.tenantName,
          children: [{ key: profile.id, label: this.getProfileLabel(profile), info: profile.description }]
        },
      ]
    };
  }

  private getProfileLabel(profile: Profile): string {
    let label = profile.name;
    if (this.showLevel && profile.level) {
      label = label.concat(' Niveau '.concat(profile.level));
    }

    return label;
  }

  private buildTenantOption(profile: Profile): OptionTree {

    return {
      key: profile.tenantIdentifier.toString(),
      label: profile.tenantName,
      children: [{ key: profile.id, label: this.getProfileLabel(profile), info: profile.description }]
    };
  }

  private filterTenants() {
    const application = this.applications.find((app) => app.key === this.appSelect.value);
    this.filteredTenants = application ? application.children : [];
  }

  private filterProfiles() {
    const tenant = this.filteredTenants.find((t) => t.key === this.tenantSelect.value);
    this.filteredProfiles = tenant ? tenant.children : [];
  }

  resetTree() {
    this.appSelect.setValue(null);
  }

}

function byApplicationName(profiles: Profile[], applications: Application[]): (idA: string, idB: string) => number {
  return (idA, idB) => {
    const nameA = getApplicationName(profiles, idA, applications);
    const nameB = getApplicationName(profiles, idB, applications);

    if (nameA.toLowerCase() > nameB.toLowerCase()) {
      return 1;
    }
    if (nameA.toLowerCase() < nameB.toLowerCase()) {
      return -1;
    }

    return 0;
  };
}

function getApplicationName(profiles: Profile[], profileId: string, applications: Application[]): string {
  const profile = profiles.find((p) => p.id === profileId);

  if (!profile) {
    return '';
  }

  const application = applications.find((app) => app.identifier === profile.applicationName);
  return application ? application.name : '';
}
