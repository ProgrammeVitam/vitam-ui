/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { Component, OnInit, Input, OnChanges, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { PagedResult, SearchCriteria, SearchCriteriaEltDto, SearchCriteriaStatusEnum} from '../models/search.criteria';
import { merge,  Subject,  Subscription } from 'rxjs';
import { debounceTime, filter, map } from 'rxjs/operators';
import { diff, Direction } from 'ui-frontend-common';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { DatePipe } from '@angular/common';
import { ArchiveSharedDataServiceService } from '../../core/archive-shared-data-service.service';
import { ArchiveService } from '../archive.service';

const UPDATE_DEBOUNCE_TIME = 200;
const BUTTON_MAX_TEXT = 40;
const DESCRIPTION_MAX_TEXT = 60;
const PAGE_SIZE = 10;
const FILTER_DEBOUNCE_TIME_MS = 400;

@Component({
  selector: 'app-archive-search',
  templateUrl: './archive-search.component.html',
  styleUrls: ['./archive-search.component.scss']
})
export class ArchiveSearchComponent implements OnInit, OnChanges {

  private readonly orderChange = new Subject<string>();
  orderBy = 'Title';
  direction = Direction.ASCENDANT;

  @Input()
  accessContract: string;
  nbQueryCriteria: number = 0;
  subscriptionNodes: Subscription;
  currentPage: number = 0;
  pageNumbers: number = 0;
  totalResults: number = 0;
  pending: boolean = false;
  canLoadMore: boolean = false;
  accessContractIdSelected: string = 'ContratTNR';
  accessContracts: string[];
  tenantIdentifier: string;
  form: FormGroup;
  submited: boolean = false;
  searchCriterias: Map<string, SearchCriteria>;
  otherCriteriaValueEnabled: boolean = false;
  otherCriteriaValueType: string = 'DATE';
  showCriteriaPanel: boolean = true;
  selectedValueOntolonogy: any;
  archiveUnits: any[];
  ontologies: any;
  filterMapType: { [key: string]: string[] } = {
    status: ['Folder', 'Document']
  };
  private readonly filterChange = new Subject<{ [key: string]: any[] }>();

  previousValue: {
    archiveCriteria: '',
    title: '',
    identifier: '',
    description: '',
    guid: '',
    uaid: '',
    beginDt: '',
    endDt: '',
    serviceProdLabel: '',
    serviceProdCode: '',
    serviceProdCommunicability: '',
    serviceProdCommunicabilityDt: '',
    otherCriteria: '',
    otherCriteriaValue: ''
  };
emptyForm = {
  archiveCriteria: '',
  title: '',
  identifier: '',
  description: '',
  guid: '',
  uaid: '',
  beginDt: '',
  endDt: '',
  serviceProdLabel: '',
  serviceProdCode: '',
  serviceProdCommunicability: '',
  serviceProdCommunicabilityDt: '',
  otherCriteria: '',
  otherCriteriaValue: ''}

  show = true;

  constructor(private formBuilder: FormBuilder, private httpClient: HttpClient, private archiveService: ArchiveService,
              private route: ActivatedRoute, private nodesService: ArchiveSharedDataServiceService, private datePipe: DatePipe) {
      private route: ActivatedRoute, private nodesService: ArchiveSharedDataServiceService, private datePipe:DatePipe ) {
    this.subscriptionNodes = this.nodesService.getNodes().subscribe(node => {
      if(node.checked){
        this.addCriteria("NODE", "Noeud", node.id, node.title);

      }else {
        node.count = null;
        this.removeCriteria("NODE", node.id);
      }
    });

    this.getOntologiesFromJson().subscribe((data:any) =>{
      this.ontologies= data;
      this.ontologies.sort(function (a: any, b: any) {
        var shortNameA = a.ShortName;
        var shortNameB = b.ShortName;
        return (shortNameA < shortNameB) ? -1 : (shortNameA > shortNameB) ? 1 : 0;
      });
    })

    this.previousValue = {
    archiveCriteria: "",
    title: "",
    identifier: "",
    description: "",
    guid: "",
    uaid: "",
    beginDt: "",
    endDt: "",
    serviceProdLabel: "",
    serviceProdCode: "",
    serviceProdCommunicability: "",
    serviceProdCommunicabilityDt: "",
    otherCriteria: "",
    otherCriteriaValue: ""};


    this.form = this.formBuilder.group({
      archiveCriteria: ['', []],
      title: ['', []],
      description: ['', []],
      guid: ['', []],
      uaid: ['', []],
      beginDt: ['', []],
      endDt: ['', []],
      serviceProdLabel: ['', []],
      serviceProdCode: ['', []],
      serviceProdCommunicability: ['', []],
      serviceProdCommunicabilityDt: ['', []],
      otherCriteria: ['', []],
      otherCriteriaValue: ['', []]
    });
    merge(this.form.statusChanges, this.form.valueChanges)
    .pipe(
      debounceTime(UPDATE_DEBOUNCE_TIME),
      filter(() => this.form.valid),
      map(() => this.form.value),
      map(() => diff(this.form.value, this.previousValue)),
      filter((formData) => this.isEmpty(formData)),
    )
    .subscribe(() => {
      this.resetForm();
    }
    );
   }

   getOntologiesFromJson() {
    return this.httpClient.get("assets/ontologies/ontologies.json")
    .pipe(map(resp => resp));
  }


   isEmpty(formData: any): boolean{
     if(formData){
       if(formData.archiveCriteria){
         this.addCriteria("titleAndDescription", "Titre ou Description", formData.archiveCriteria.trim(), formData.archiveCriteria.trim());
       return true;
     }else
      if(formData.title){
        this.addCriteria("Title", "Titre", formData.title.trim(), formData.title.trim());
       return true;
     }else  if(formData.description){
        this.addCriteria("Description", "Description", formData.description.trim(), formData.description.trim());
       return true;
     } else if(formData.beginDt) {
        this.addCriteria("StartDate", "Date début", this.form.value.beginDt, this.datePipe.transform(this.form.value.beginDt, 'dd/MM/yyyy'));
       return true;
     }else if(formData.endDt){
        this.addCriteria("EndDate", "Date de fin", this.form.value.endDt, this.datePipe.transform(this.form.value.endDt, 'dd/MM/yyyy'));
       return true;
      }   else if(formData.serviceProdCode){
        this.addCriteria("#originating_agency", "Code Service Prod ", formData.serviceProdCode.trim(), formData.serviceProdCode.trim());
       return true;
      }else if(formData.serviceProdLabel){
        this.addCriteria("originating_agency_label", "Nom Service Prod ", formData.serviceProdLabel.trim(), formData.serviceProdLabel.trim());
       return true;
     }else if(formData.uaid){
        this.addCriteria("#id", "ID", formData.uaid, formData.uaid);
       return true;
      } else if(formData.guid) {
        this.addCriteria("#opi", "GUID", formData.guid, formData.guid);
        return true;
      }else if(formData.serviceProdCommunicability){
        this.addCriteria("serviceProdCommunicability", "Service Prod Comm", formData.serviceProdCommunicability, formData.serviceProdCommunicability);
       return true;
     }else if(formData.serviceProdCommunicabilityDt){
        this.addCriteria("serviceProdCommunicabilityDt", "Service Prod Comm DT", formData.serviceProdCommunicabilityDt, formData.serviceProdCommunicabilityDt);
       return true;
     }else if(formData.otherCriteriaValue){
        const ontologyElt = this.ontologies.find((ontoElt: any) => ontoElt.Value === formData.otherCriteria);
        if(this.otherCriteriaValueType === 'DATE'){
          this.addCriteria(ontologyElt.Value, ontologyElt.ShortName, this.form.value.otherCriteriaValue, this.datePipe.transform(this.form.value.otherCriteriaValue, 'dd/MM/yyyy') );
        }else {
          this.addCriteria(ontologyElt.Value, ontologyElt.ShortName, formData.otherCriteriaValue.trim(), formData.otherCriteriaValue.trim());
        }
       return true;
     }else{
      return false;
     }
     } else {
       return false;
     }
   }

   private resetForm() {
     this.form.reset(this.emptyForm);
  }

  ngOnInit() {
    this.tenantIdentifier  = this.route.snapshot.paramMap.get('tenantIdentifier');
    this.searchCriterias = new Map();
    //this.accessContractService.getAllForTenant(this.tenantIdentifier).subscribe(accessContracts => this.accessContracts = accessContracts);
    this.accessContracts= [];
    this.accessContracts.push("contract_with_field_EveryDataObjectVersion");
    this.accessContracts.push("contrat_producteur1");
    this.accessContracts.push("contrat_tous_producteur");
    this.accessContracts.push("ContratTNR");
    const searchCriteriaChange = merge(this.orderChange)
    this.filterMapType['Type'] = ['Folder','Document'];
    const searchCriteriaChange = merge(this.orderChange, this.filterChange)
      .pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));


    searchCriteriaChange.subscribe(() => {
      this.submit();
    });
  };

  ngOnChanges(changes: SimpleChanges): void {
    if(changes.accessContract) {
      this.show = true;
      this.nodesService.emitToggle(this.show);
    }
  }

  onFilterChange(key: string, values: any[]) {
    this.filterMapType[key] = values;
    this.filterChange.next(this.filterMapType);
  }

  showHidePanel(){
    this.showCriteriaPanel = !this.showCriteriaPanel;
  }


  emitOrderChange(){
    this.orderChange.next();
  }


  removeCriteria(keyElt: string, valueElt: string){

    if(this.searchCriterias && this.searchCriterias.size > 0){
      this.searchCriterias.forEach((val, key) => {
        if(key === keyElt){
          let values = val.values;
          values = values.filter(item => item.value !== valueElt);
          if(values.length === 0 ){
            this.searchCriterias.delete(keyElt);
          }else {
            val.values = values;
            this.searchCriterias.set(keyElt, val);
        }

        this.nbQueryCriteria--;

        }
        if(key === 'NODE'){
          this.nodesService.emitNodeTarget(valueElt);
        }
      });
    }
    if(this.searchCriterias && this.searchCriterias.size === 0) {
      this.submited = false;
      this.showCriteriaPanel = true;
      this.archiveUnits = [];
    }
  }


  onSelectOtherCriteria() {
    this.form.get('otherCriteria').valueChanges
    .subscribe(selectedcriteria => {

        if (selectedcriteria === '') {
            this.otherCriteriaValueEnabled = false;
            this.selectedValueOntolonogy = null;
        }
        else {
          this.form.controls.otherCriteriaValue.setValue('');
          this.otherCriteriaValueEnabled = true;
          let selectedValueOntolonogyValue = this.form.get('otherCriteria').value;
          const selectedValueOntolonogyElt = this.ontologies.find((ontoElt: any) => ontoElt.Value === selectedValueOntolonogyValue);
          if(selectedValueOntolonogyElt){
            this.selectedValueOntolonogy =  selectedValueOntolonogyElt.ShortName;
            this.otherCriteriaValueType = selectedValueOntolonogyElt.Type;
          }
        }
    });
}

  addCriteria(keyElt: string, keyLabel: string, valueElt: string, labelElt: string){
    if(keyElt && valueElt){
     if(this.searchCriterias){
      this.nbQueryCriteria++;
      let criteria: SearchCriteria;
      if(this.searchCriterias.has(keyElt)){
        criteria = this.searchCriterias.get(keyElt);
        let values = criteria.values;
        if(!values || values.length === 0){
          values = [];
        }
        values.push({ value: valueElt, label: labelElt, valueShown: true, status: SearchCriteriaStatusEnum.NOT_INCLUDED} );
        criteria.values = values;
        this.searchCriterias.set(keyElt, criteria);
      }else {
        let values = [];
        values.push({ value: valueElt, label: labelElt, valueShown: true, status: SearchCriteriaStatusEnum.NOT_INCLUDED} );
        let criteria = {key: keyElt, label: keyLabel, values : values };
        this.searchCriterias.set(keyElt, criteria);
      }
    }}
  }


  submit(){
    this.pending = true;
    this.submited = true;
    this.showCriteriaPanel = false;
    this.currentPage = 0;
    this.archiveUnits = [];
    this.callVitamApiService(this.buildNodesListForQUery(), this.buildCriteriaListForQUery());

  }

  buildNodesListForQUery(): String[] {
    let nodesIdList: String[] = [];
    this.searchCriterias.forEach((criteria: SearchCriteria) => {
      if(criteria.key === 'NODE') {
        criteria.values.forEach((elt) => {
          nodesIdList.push(elt.value);
        });
      }
    });
    return nodesIdList;
  }


  buildCriteriaListForQUery():SearchCriteriaEltDto[]{
    let criteriaList: SearchCriteriaEltDto[] = [];
    this.searchCriterias.forEach((criteria: SearchCriteria) => {

      let strValues: string[] = [];
      this.updateCriteriaStatus(SearchCriteriaStatusEnum.NOT_INCLUDED, SearchCriteriaStatusEnum.IN_PROGRESS);
      if(criteria.key !== 'NODE') {
        criteria.values.forEach((elt) => {
          strValues.push(elt.value);
        });
          criteriaList.push({ "criteria": criteria.key, "values": strValues })
      }
    });

    let typesFilterValues: string[] = [];
    this.filterMapType['Type'].forEach(
      (filter) => {
        if(filter === 'Folder'){
          typesFilterValues.push('RecordGrp');
        }
       if(filter === 'Document') {
          typesFilterValues.push('File');
          typesFilterValues.push('Item');
        }
      }
    );
    if(typesFilterValues.length > 0){
      criteriaList.push({ 'criteria': 'DescriptionLevel', "values": typesFilterValues })
    }
    return criteriaList;
  }

  private callVitamApiService(nodesIds: String[], criteriaList: SearchCriteriaEltDto[])  {
    this.pending = true;
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
     headers = headers.append('X-Access-Contract-Id', this.accessContractIdSelected);

    let sortingCriteria = { criteria: this.orderBy , sorting: this.direction}
    this.archiveService.searchArchiveUnitsByCriteria({ "nodes": nodesIds, "criteriaList": criteriaList, "pageNumber": this.currentPage, size: PAGE_SIZE, 'sortingCriteria': sortingCriteria }, headers).subscribe(
      (pagedResult: PagedResult) => {


        if(this.currentPage === 0 ){
          this.archiveUnits = pagedResult.results;
          this.nodesService.emitFacets(pagedResult.facets);
        } else {
          if (pagedResult.results) {
            pagedResult.results.forEach(elt => this.archiveUnits.push(elt));
          }
        }
        this.pageNumbers = pagedResult.pageNumbers;
        this.totalResults = pagedResult.totalResults;
        this.canLoadMore = (this.currentPage < this.pageNumbers - 1);
        this.nodesService.emitFacets(pagedResult.facets);
        this.updateCriteriaStatus(SearchCriteriaStatusEnum.IN_PROGRESS, SearchCriteriaStatusEnum.INCLUDED);
        this.pending = false;
      },
      (error: HttpErrorResponse) => {
        this.canLoadMore = false;
        this.pending = false;
        console.log("Errors : ", error.message);
        this.nodesService.emitFacets([]);
        this.updateCriteriaStatus(SearchCriteriaStatusEnum.IN_PROGRESS, SearchCriteriaStatusEnum.NOT_INCLUDED);
      })
  }


  updateCriteriaStatus(oldStatusFilter: SearchCriteriaStatusEnum, newStatus: SearchCriteriaStatusEnum){
    this.searchCriterias.forEach((value: SearchCriteria) => {
      value.values.forEach((elt) => {
        if(elt.status === oldStatusFilter) {
          elt.status = newStatus;
        }
      });
    });
  }



  getButtonSubText(originText: string): string{
    return this.getSubText(originText, BUTTON_MAX_TEXT);
  }

  getDescriptionSubText(originText: string): string {
    return this.getSubText(originText, DESCRIPTION_MAX_TEXT);
  }

  getSubText(originText: string, limit: number): string {
    let subText = originText;
    if(originText && originText.length > limit) {
      subText = originText.substring(0, limit) + '...';
    }
    return subText;
  }

  initContractSelected(event: any) {
    this.accessContractIdSelected = event.value;
  }

  loadMore(){
    if(this.canLoadMore) {
      this.submited = true;
      this.currentPage = this.currentPage + 1 ;
      this.callVitamApiService(this.buildNodesListForQUery(), this.buildCriteriaListForQUery());
    }
  }

  hiddenTreeBlock(hidden: boolean): void {
    this.show = !hidden;
    this.nodesService.emitToggle(this.show);
  }

  ngOnDestroy() {
    // unsubscribe to ensure no memory leaks
    this.subscriptionNodes.unsubscribe();
  }

  get uaid() { return this.form.controls.uaid }
  get archiveCriteria() { return this.form.controls.archiveCriteria }
  get title() { return this.form.controls.title }
  get description() { return this.form.controls.description }
  get guid() { return this.form.controls.guid }
  get beginDt() { return this.form.controls.beginDt }
  get endDt() { return this.form.controls.endDt }
  get serviceProdLabel() { return this.form.controls.serviceProdLabel }
  get serviceProdCommunicability() { return this.form.controls.serviceProdCommunicability }
  get serviceProdCode() { return this.form.controls.serviceProdCode }
  get serviceProdCommunicabilityDt() { return this.form.controls.serviceProdCommunicabilityDt }
  get otherCriteria() { return this.form.controls.otherCriteria }
  get otherCriteriaValue() { return this.form.controls.otherCriteriaValue }

}
