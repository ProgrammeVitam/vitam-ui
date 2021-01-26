import {registerLocaleData} from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import {LOCALE_ID, NgModule} from '@angular/core';
import {MatNativeDateModule} from '@angular/material/core';
import {BrowserModule, Title} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NgxFilesizeModule} from 'ngx-filesize';
import {VitamUICommonModule, WINDOW_LOCATION} from 'ui-frontend-common';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {CoreModule} from './core/core.module';
import {SharedModule} from './shared/shared.module';

registerLocaleData(localeFr, 'fr');

@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    CoreModule,
    BrowserAnimationsModule,
    BrowserModule,
    VitamUICommonModule,
    AppRoutingModule,
    MatNativeDateModule,
    NgxFilesizeModule,
    SharedModule
  ],
  providers: [
    Title,
    {provide: LOCALE_ID, useValue: 'fr'},
    {provide: WINDOW_LOCATION, useValue: window.location}
  ],
  bootstrap: [AppComponent],
})
export class AppModule {
}
