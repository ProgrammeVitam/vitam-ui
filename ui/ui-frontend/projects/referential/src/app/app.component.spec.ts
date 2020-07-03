import {Component} from '@angular/core';
import {async, TestBed} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {AppComponent} from './app.component';

// tslint:disable-next-line:component-selector
@Component({selector: 'vitamui-common-subrogation-banner', template: ''})
class SubrogationBannerStubComponent {
}

describe('AppComponent', () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule
      ],
      declarations: [
        SubrogationBannerStubComponent,
        AppComponent
      ],
    }).compileComponents();
  }));

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  });


  it(`should have as title 'Referential App'`, async(() => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    console.log('Title App: ', app);
    expect(app.title).toEqual('Referential App');
  }));

});
