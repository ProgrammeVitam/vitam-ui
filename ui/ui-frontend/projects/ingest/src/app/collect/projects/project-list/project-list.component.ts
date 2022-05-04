/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
import {Component, OnDestroy, OnInit} from '@angular/core';
import {Colors, DEFAULT_PAGE_SIZE, Direction, InfiniteScrollTable, PageRequest, Project} from 'ui-frontend-common';
import {ProjectsService} from '../projects.service';
import {BehaviorSubject} from 'rxjs';
import {FacetDetails} from "ui-frontend-common/app/modules/models/operation/facet-details.interface";

@Component({
  selector: 'app-project-list',
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.css'],
})
export class ProjectListComponent extends InfiniteScrollTable<Project> implements OnDestroy, OnInit {
  direction = Direction.DESCENDANT;
  orderBy = 'archivalAgreement';
  orderChange = new BehaviorSubject<string>(this.orderBy);
  facetDetails: FacetDetails[] = [
    {
      title: 'En cours',
      totalResults: 18,
      clickable: false,
      color: Colors.BLACK,
      backgroundColor: Colors.DISABLED,
    },
    {
      title: 'En attente de validation',
      totalResults: 12,
      clickable: false,
      color: Colors.BLACK,
      backgroundColor: Colors.DISABLED,
    },
    {
      title: 'Commentaires',
      totalResults: 24,
      clickable: false,
      color: Colors.BLACK,
      backgroundColor: Colors.DISABLED,
    },
    {
      title: 'Terminés',
      totalResults: 76,
      clickable: false,
      color: Colors.BLACK,
      backgroundColor: Colors.DISABLED,
    },
    {
      title: 'Refusés',
      totalResults: 2,
      clickable: false,
      color: Colors.BLACK,
      backgroundColor: Colors.DISABLED,
    }
  ]

  constructor(public projectsService: ProjectsService) {
    super(projectsService);
  }

  ngOnInit(): void {
    this.searchProject();
  }

  ngOnDestroy(): void {}

  searchProject() {
    const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction);
    super.search(pageRequest);
  }

  onScroll() {
    this.loadMore();
  }

  emitOrderChange(event: string) {
    this.orderChange.next(event);
  }

}
