import { registerLocaleData } from '@angular/common';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HttpClientModule } from '@angular/common/http';
import { LOCALE_ID, NgModule } from '@angular/core';
import { default as localeFr } from '@angular/common/locales/fr';
import { BrowserModule, Title } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { VitamUICommonModule, WINDOW_LOCATION } from 'ui-frontend-common';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CoreModule } from './core/core.module';
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
