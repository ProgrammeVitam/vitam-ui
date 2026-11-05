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
import { Routes } from '@angular/router';
import { AnalyticsResolver, AppGuard, AuthGuard, VitamuiFeatureConfig, vitamuiFeatureRoutes } from 'vitamui-library';
import { IngestFeatureComponent } from './ingest-feature.component';
import { provideIngestServices } from './ingest.providers';

/** Pages of the ingest feature, relative to its root ('/ingest/' as a separate application, its route in the single one). */
export const INGEST_ROUTES: Routes = [
  // =====================================================
  //                      Ingests
  // =====================================================
  {
    path: 'ingest',
    loadChildren: () => import('./ingest/ingest.module').then((m) => m.IngestModule),
    canActivate: [AuthGuard, AppGuard],
    resolve: { userAnalytics: AnalyticsResolver },
    data: { appId: 'INGEST_MANAGEMENT_APP' },
  },

  // =====================================================
  //                      TREES PLANS API
  // =====================================================
  {
    path: 'holding-filling-scheme',
    loadChildren: () => import('./holding-filling-scheme/holding-filling-scheme.module').then((m) => m.HoldingFillingSchemeModule),
    canActivate: [AuthGuard, AppGuard],
    resolve: { userAnalytics: AnalyticsResolver },
    data: { appId: 'HOLDING_FILLING_SCHEME_APP' },
  },
];

/** Routes of the ingest feature in the single application, with its own API prefix and services. */
export function ingestRoutes(config: VitamuiFeatureConfig): Routes {
  return vitamuiFeatureRoutes({ ...config, providers: [provideIngestServices()], component: IngestFeatureComponent }, [
    { path: '', pathMatch: 'full', redirectTo: '/' },
    ...INGEST_ROUTES,
  ]);
}
