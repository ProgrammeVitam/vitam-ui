import { TestBed, async } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { Router } from '@angular/router';
import { AuthService, StartupService } from 'ui-frontend-common';
import { of } from 'rxjs';
import { MatSidenavModule } from '@angular/material/sidenav';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Component } from '@angular/core';


@Component({ selector: 'router-outlet', template: '' })
class RouterOutletStubComponent {}

@Component({ selector: 'vitamui-common-subrogation-banner', template: '' })
class SubrogationBannerStubComponent {}

describe('AppComponent', () => {


  beforeEach(async(() => {
    const startupServiceStub = { configurationLoaded: () => true, printConfiguration: () => {} };
    TestBed.configureTestingModule({
      imports: [
        MatSidenavModule,
        NoopAnimationsModule,
      ],
      declarations: [
        AppComponent,
        SubrogationBannerStubComponent,
        RouterOutletStubComponent,
      ],
      providers: [
        { provide: StartupService, useValue: startupServiceStub },
        { provide: AuthService, useValue: { userLoaded: of(null) } },
        { provide: Router, useValue: { navigate: () => {} } },
      ]
    }).compileComponents();
  }));

  it('should create the app', async(() => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    console.log('Create App: ', app);
    expect(app).toBeTruthy();
  }));

  it(`should have as title 'Collect Application'`, async(() => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    console.log('Title App: ', app);
    expect(app.title).toEqual('Collect Application');
  }));


});
