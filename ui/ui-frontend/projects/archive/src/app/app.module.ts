import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ArchiveComponent } from './archive/archive.component';
import { ArchiveService } from './archive.service';
import { HttpClientModule } from '@angular/common/http';
import { VitamUICommonModule } from 'ui-frontend-common';

@NgModule({
  declarations: [
    AppComponent,

  ],
  imports: [
    CoreModule,
    BrowserAnimationsModule,
    VitamUICommonModule,
    HttpClientModule,
    BrowserModule,
    AppRoutingModule,


  ],
  providers: [
    Title,
    { provide: LOCALE_ID, useValue: 'fr' },
    { provide: WINDOW_LOCATION, useValue: window.location },
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
