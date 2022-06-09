import { Pipe, PipeTransform } from '@angular/core';
import { ProfileDescription } from '../../../models/profile-description.model';

@Pipe({name: 'filterByType'})
export class FilterByTypePipe implements PipeTransform {
  transform(listOfProfiles: ProfileDescription[], typeToFilter: string): ProfileDescription[] {
    if (!listOfProfiles) { return null; }
    if (!typeToFilter) { return listOfProfiles; }
    if (typeToFilter === 'ALL') { return listOfProfiles; }
    return listOfProfiles.filter(profile => profile.type === typeToFilter);
  }
}