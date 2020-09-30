import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatSelectModule} from '@angular/material/select';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {VitamUILibraryModule} from 'projects/vitamui-library/src/public-api';
import {VitamUICommonModule} from 'ui-frontend-common';

import {SharedModule} from '../../shared/shared.module';
import {ProbativeValueCreateComponent} from './probative-value-create.component';

@NgModule({
  declarations: [ProbativeValueCreateComponent],
  imports: [
    CommonModule,
    SharedModule,
    MatButtonToggleModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    MatSnackBarModule,
    ReactiveFormsModule,
    VitamUICommonModule,
    VitamUILibraryModule
  ],
  entryComponents: [ProbativeValueCreateComponent]
})
export class ProbativeValueCreateModule {
}
