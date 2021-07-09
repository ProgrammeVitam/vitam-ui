import { Pipe, PipeTransform } from '@angular/core';
import {AccessionRegisterStatus} from "../../../../../vitamui-library/src/lib/models/accession-registers-status";

@Pipe({
  name: 'accessionStatus'
})
export class AccessionStatusPipe implements PipeTransform {

  transform(value: AccessionRegisterStatus): string {
    switch (value) {
      case AccessionRegisterStatus.STORED_AND_UPDATED:
        return 'Partiellement éliminée';
      case AccessionRegisterStatus.STORED_AND_COMPLETED:
        return 'En stock et complète';
      case AccessionRegisterStatus.UNSTORED:
        return 'Totalement éliminée';
      default:
        return value;
    }
  }
}
