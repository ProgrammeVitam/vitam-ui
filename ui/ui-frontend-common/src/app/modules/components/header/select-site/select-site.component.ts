import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { OAuthStorage } from 'angular-oauth2-oidc';
import { Observable } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { Operators } from '../../../vitamui-table';
import { SiteApiService } from './../../../api/site-api.service';
import { AuthService } from './../../../auth.service';

@Component({
  selector: 'vitamui-common-select-site',
  templateUrl: './select-site.component.html',
  styleUrls: ['./select-site.component.scss']
})
export class SelectSiteComponent implements OnInit {

  public selectedSite: any;
  public sites: any[];
  private sessionExpireAt: any;
  private defaultSiteCode: string;
  private siteSession: {code: string, sessionExpireAt: string};

  constructor(
    protected http: HttpClient,
    private authService: AuthService,
    private authStorage: OAuthStorage,
    private siteApiService: SiteApiService) { }

  ngOnInit(): void {
    this.sessionExpireAt =  this.authStorage.getItem('expires_at');
    this.siteSession = JSON.parse(this.authStorage.getItem('site'));

    if (this.siteSession?.sessionExpireAt === this.sessionExpireAt) {
      this.defaultSiteCode = this.siteSession.code;
    } else {
      this.defaultSiteCode = this.authService.user.siteCode;
    }

    this.loadSites(this.defaultSiteCode);
  }

  /**
   * Initializes list of sites and selected site from extrnal api data
   */
  private loadSites(siteCode: string): void {
    this.getSitesByCode(siteCode).pipe(
      map((sites) =>
        sites.length > 0 ? sites[0].region : null
      ),
      switchMap((region) => {
        return this.getSitesByRegion(region);
      })).subscribe(result => {
        this.sites = result;
        this.selectedSite = this.sites.find(site => site.code === this.defaultSiteCode);
      });
  }

  /**
   * get sites by code
   * @param siteCode site code
   */
  getSitesByCode(siteCode: string): Observable<any> {
    const params = new HttpParams().set('criteria',
      JSON.stringify({ criteria: [{ key: 'code', value: siteCode, operator: Operators.equals }]}));
    return this.siteApiService.getAllByParams(params);
  }

  /**
   * get sites by region
   * @param region region
   */
  getSitesByRegion(region: string): Observable<any> {
    const params = new HttpParams().set('criteria',
      JSON.stringify({ criteria: [{ key: 'region', value: region, operator: Operators.equals }]}));
    return this.siteApiService.getAllByParams(params);
  }

  /**
   * Handles the selection of an element
   * @param site The selected site
   */
  async onSelectSite(site: any) {
    this.selectedSite = site;
    this.siteSession = {
      code: site.code,
      sessionExpireAt: this.sessionExpireAt
    };
    this.authStorage.setItem('site', JSON.stringify(this.siteSession));
  }
}
