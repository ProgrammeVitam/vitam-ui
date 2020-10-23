import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HttpClientModule } from '@angular/common/http';
import { VitamUICommonModule, WINDOW_LOCATION } from 'ui-frontend-common';
import { CoreModule } from './core/core.module';
import { LOCALE_ID, NgModule } from '@angular/core';
import { BrowserModule, Title } from '@angular/platform-browser';
import { registerLocaleData } from '@angular/common';
import { default as localeFr } from '@angular/common/locales/fr';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ArchiveComponent } from './archive/archive.component';
import { ArchiveService } from './archive.service';


registerLocaleData(localeFr, 'fr');

@NgModule({
  declarations: [
    AppComponent,
    ArchiveComponent
  ],
  imports: [
    CoreModule,
    VitamUICommonModule,
    HttpClientModule,
    BrowserModule,
    AppRoutingModule,


  ],
  providers: [ArchiveService,
    Title,
    { provide: LOCALE_ID, useValue: 'fr' },
    { provide: WINDOW_LOCATION, useValue: window.location },
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
