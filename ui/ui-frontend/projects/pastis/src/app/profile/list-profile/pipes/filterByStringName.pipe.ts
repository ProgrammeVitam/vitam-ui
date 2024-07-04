import { Pipe, PipeTransform } from '@angular/core';
import { ProfileDescription } from '../../../models/profile-description.model';

@Pipe({
  name: 'filterByStringName',
  standalone: true,
})
export class FilterByStringNamePipe implements PipeTransform {
  constructor() {}
  private listOfProfiles: ProfileDescription[];
  transform(listOfProfiles: ProfileDescription[], nameToFilter: string): ProfileDescription[] {
    if (!listOfProfiles) {
      return null;
    }
    if (!nameToFilter) {
      return listOfProfiles;
    }

    this.listOfProfiles = listOfProfiles.filter(
      (profile) =>
        profile.identifier.toLowerCase().indexOf(nameToFilter.toLowerCase()) >= 0 ||
        profile.name.toLowerCase().indexOf(nameToFilter.toLowerCase()) >= 0,
    );
    return this.listOfProfiles;
  }
}
