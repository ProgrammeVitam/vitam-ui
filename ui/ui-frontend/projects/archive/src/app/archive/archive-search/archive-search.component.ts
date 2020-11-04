import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { OntologySubModel, SearchCriteria, SearchCriteriaStatusEnum } from '../../core/search.criteria';
import { merge } from 'rxjs';
import { debounceTime, filter, map } from 'rxjs/operators';
import { diff } from 'ui-frontend-common';
import { HttpClient } from '@angular/common/http';


const UPDATE_DEBOUNCE_TIME = 200;
const BUTTON_MAX_TEXT = 40;


@Component({
  selector: 'app-archive-search',
  templateUrl: './archive-search.component.html',
  styleUrls: ['./archive-search.component.scss']
})
export class ArchiveSearchComponent implements OnInit {
  form: FormGroup;
  searchCriterias: Map<string, SearchCriteria>;
  otherCriteriaValueEnabled: boolean = false;
  showCriteriaPanel: boolean = true;
  selectedValueOntolonogy: any;
  ontologies: any;
  previousValue: {
    archiveCriteria: '',
    title: '',
    identifier: '',
    description: '',
    guid: '',
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
  beginDt: '',
  endDt: '',
  serviceProdLabel: '',
  serviceProdCode: '',
  serviceProdCommunicability: '',
  serviceProdCommunicabilityDt: '',
  otherCriteria: '',
  otherCriteriaValue: ''}

  constructor(private formBuilder: FormBuilder,private httpClient: HttpClient) {
    this.getOntologiesFromJson().subscribe((data:any) =>{
      this.ontologies= data;
      console.log(this.ontologies)
    })
    this.previousValue = {
    archiveCriteria: "",
    title: "",
    identifier: "",
    description: "",
    guid: "",
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
      //filter(() => this.form.valid),
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
      if(formData.title){
      this.addCriteria("title", "Titre",formData.title);
       return true;
     }else  if(formData.beginDt){
      this.addCriteria("beginDt", "Date début",formData.beginDt);
       return true;
     }else if(formData.description){
      this.addCriteria("description", "Description",formData.description);
       return true;
     }else if(formData.endDt){
      this.addCriteria("endDt", "Date de fin",formData.endDt);
       return true;
     }else if(formData.serviceProdLabel){
      this.addCriteria("serviceProdLabel", "Service Prod",formData.serviceProdLabel);
       return true;
     }else if(formData.serviceProdCode){
      this.addCriteria("serviceProdCode", "Service Prod Code",formData.serviceProdCode);
       return true;
     }else if(formData.guid){
      this.addCriteria("guid", "Guid",formData.guid);
       return true;
     }else if(formData.serviceProdCommunicability){
      this.addCriteria("serviceProdCommunicability","Service Prod Comm", formData.serviceProdCommunicability);
       return true;
     }else if(formData.serviceProdCommunicabilityDt){
      this.addCriteria("serviceProdCommunicabilityDt", "Service Prod Comm DT",  formData.serviceProdCommunicabilityDt);
       return true;
     }else if(formData.otherCriteriaValue){
         const ontologyElt = this.ontologies.find((ontoElt: any) => ontoElt.Identifier === formData.otherCriteria);
         this.addCriteria(formData.otherCriteria, ontologyElt.ShortName ,formData.otherCriteriaValue);
       return true;
     }else{
      return false;
     }
   }
   filterHere(): boolean {
     return false;
   }

   private resetForm() {
     this.form.reset(this.emptyForm);
  }

  ngOnInit() {
    this.searchCriterias = new Map();
  }

  showHidePanel(){
    this.showCriteriaPanel = !this.showCriteriaPanel;
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
        }
      });
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
          this.otherCriteriaValueEnabled = true;
          let selectedValueOntolonogyValue = this.form.get('otherCriteria').value;
          const selectedValueOntolonogyElt = this.ontologies.find((ontoElt: any) => ontoElt.Identifier === selectedValueOntolonogyValue);
          if(selectedValueOntolonogyElt){
            this.selectedValueOntolonogy =  selectedValueOntolonogyElt.ShortName;
          }
        }
    });
}

  addCriteria(keyElt: string, keyLabel: string, valueElt: string){
    if(keyElt && valueElt){
     if(this.searchCriterias){
      let criteria: SearchCriteria;
      if(this.searchCriterias.has(keyElt)){
        criteria = this.searchCriterias.get(keyElt);
        let values = criteria.values;
        if(!values || values.length === 0){
          values = [];
        }
        values.push({value: valueElt, valueShown: true, status: SearchCriteriaStatusEnum.NOT_INCLUDED} );
        criteria.values = values;
        this.searchCriterias.set(keyElt, criteria);
      }else {
        let values = [];
        values.push({value: valueElt, valueShown: true, status: SearchCriteriaStatusEnum.NOT_INCLUDED} );
        let criteria = {key: keyElt, label: keyLabel, values : values };
        this.searchCriterias.set(keyElt, criteria);
      }
    }}
  }


  submit(){
    console.log("Submited")
    if(this.searchCriterias){
    setTimeout(() =>
    {
      console.log("pass to Progress")

        this.searchCriterias.forEach((value: SearchCriteria) => {
          value.values.forEach((elt)=> {
            if(elt.status === SearchCriteriaStatusEnum.NOT_INCLUDED){
              elt.status = SearchCriteriaStatusEnum.IN_PROGRESS;
            }
          });
      });

    },
    5000);

    setTimeout(() =>
    {
      console.log("pass to progress")
      this.searchCriterias.forEach((value: SearchCriteria) => {
        value.values.forEach((elt)=> {
          if(elt.status === SearchCriteriaStatusEnum.IN_PROGRESS){
            elt.status = SearchCriteriaStatusEnum.INCLUDED;
          }
        });
    });
    },
    7000);

  }
}




  get title() {
    return this.form.get('title');
  }

  getButtonLabel(originText: string): string{
    let subText = originText;
    if(originText && originText.length > BUTTON_MAX_TEXT){
      subText = originText.substring(0, BUTTON_MAX_TEXT) +  '...';
    }
    return subText;
  }
}
