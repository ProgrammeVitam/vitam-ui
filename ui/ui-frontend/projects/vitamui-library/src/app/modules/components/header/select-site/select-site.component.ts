/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { OAuthStorage } from 'angular-oauth2-oidc';
import { Observable } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { Operators } from '../../../vitamui-table/operators.enum';
import { SiteApiService } from './../../../api/site-api.service';
import { AuthService } from './../../../auth.service';

@Component({
  selector: 'vitamui-common-select-site',
  templateUrl: './select-site.component.html',
  styleUrls: ['./select-site.component.scss'],
  standalone: false,
})
export class SelectSiteComponent implements OnInit {
  protected http = inject(HttpClient);
  private authService = inject(AuthService);
  private authStorage = inject(OAuthStorage);
  private siteApiService = inject(SiteApiService);

  public selectedSite: any;
  public sites: any[];
  private sessionExpireAt: any;
  private defaultSiteCode: string;
  private siteSession: { code: string; sessionExpireAt: string };

  ngOnInit(): void {
    this.sessionExpireAt = this.authStorage.getItem('expires_at');
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
    this.getSitesByCode(siteCode)
      .pipe(
        map((sites) => (sites.length > 0 ? sites[0].region : null)),
        switchMap((region) => {
          return this.getSitesByRegion(region);
        }),
      )
      .subscribe((result) => {
        this.sites = result;
        this.selectedSite = this.sites.find((site) => site.code === this.defaultSiteCode);
      });
  }

  /**
   * get sites by code
   * @param siteCode site code
   */
  getSitesByCode(siteCode: string): Observable<any> {
    const params = new HttpParams().set(
      'criteria',
      JSON.stringify({ criteria: [{ key: 'code', value: siteCode, operator: Operators.equals }] }),
    );
    return this.siteApiService.getAllByParams(params);
  }

  /**
   * get sites by region
   * @param region region
   */
  getSitesByRegion(region: string): Observable<any> {
    const params = new HttpParams().set(
      'criteria',
      JSON.stringify({ criteria: [{ key: 'region', value: region, operator: Operators.equals }] }),
    );
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
      sessionExpireAt: this.sessionExpireAt,
    };
    this.authStorage.setItem('site', JSON.stringify(this.siteSession));
  }
}
