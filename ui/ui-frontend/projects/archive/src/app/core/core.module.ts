
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { BASE_URL, ENVIRONMENT, InjectorModule, LoggerModule, VitamUICommonModule, throwIfAlreadyLoaded } from 'ui-frontend-common';
import { environment } from '../../environments/environment';
import { NgModule, Optional, SkipSelf } from '@angular/core';



@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    HttpClientModule,
    VitamUICommonModule,
    InjectorModule,
    LoggerModule.forRoot()
  ],

  exports: [VitamUICommonModule],
  providers: [
    { provide: BASE_URL, useValue: './archive-api' },
    { provide: ENVIRONMENT, useValue: environment }
  ]
})
export class CoreModule {

  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    throwIfAlreadyLoaded(parentModule, 'CoreModule');
  }

}
