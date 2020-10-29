import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { SearchCriteria, SearchCriteriaValue } from '../../core/search.criteria';
import { merge } from 'rxjs';
import { isEmpty } from 'underscore';

import { debounceTime, filter, map } from 'rxjs/operators';
import { diff } from 'ui-frontend-common';


const UPDATE_DEBOUNCE_TIME = 200;


@Component({
  selector: 'app-archive-search',
  templateUrl: './archive-search.component.html',
  styleUrls: ['./archive-search.component.scss']
})
export class ArchiveSearchComponent implements OnInit {
  form: FormGroup;
  searchCriterias: Map<string, SearchCriteria>;

  previousValue: {
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
    otherCriteria: ''
  };
emptyForm = {
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
  otherCriteria: ''}

  constructor(private formBuilder: FormBuilder,) {
    this.previousValue = {
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
    otherCriteria: ""}
    ;

    this.form = this.formBuilder.group({
      title: ['', []],
      description: ['', []],
      guid: ['', []],
      beginDt: ['', []],
      endDt: ['', []],
      serviceProdLabel: ['', []],
      serviceProdCode: ['', []],
      serviceProdCommunicability: ['', []],
      serviceProdCommunicabilityDt: ['', []],
      otherCriteria: ['', []]
    });
    merge(this.form.statusChanges, this.form.valueChanges)
    .pipe(
      debounceTime(UPDATE_DEBOUNCE_TIME),
      filter(() => this.form.valid),
      map(() => this.form.value),      
      map(() => diff(this.form.value, this.previousValue)),
      filter((formData) => this.isEmpty(formData)),
      //filter((formData) => !this.isEmpty(formData)),
      //filter((formData) => !this.filterHere(formData)),
      //map((formData) => extend({ id: this.customer.id }, formData)),     
     // map((formData) => this.submit(formData)),   
    )
    .subscribe(() => {       
      this.resetForm();
    }
    );
   }

   isEmpty(formData: any): boolean{
      if(formData.title){
      this.addCriteria("title", formData.title);
       return true;
     }else  if(formData.beginDt){
      this.addCriteria("beginDt", formData.beginDt);
       return true;
     }else if(formData.description){
 
      this.addCriteria("description", formData.description);
       return true;
     }else if(formData.endDt){
      this.addCriteria("endDt", formData.endDt);
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
    for(let i=0; i< 3; i++){
      let searchValues = [];
      for(let j=0; j< i*2 +1; j++){

        let searchValue: SearchCriteriaValue = {
            value: 'value'+i+j,
            valueShown: true
        };
        searchValues.push(searchValue);
      }
      let keyCriteria = 'BeginDt'+i;
      let criteria:SearchCriteria = {
        key:keyCriteria,
        label:keyCriteria,
        values: searchValues
      }
      this.searchCriterias.set(keyCriteria, criteria);
    }



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

  addCriteria(keyElt: string, valueElt: string){
    if(keyElt && valueElt){
     if(this.searchCriterias){ 
      let criteria: SearchCriteria;
      if(this.searchCriterias.has(keyElt)){
        criteria = this.searchCriterias.get(keyElt);
        let values = criteria.values;
        if(!values || values.length === 0){
          values = [];
        }
        values.push({value: valueElt, valueShown: true} );
        criteria.values = values;
        this.searchCriterias.set(keyElt, criteria);
      }else {
        let values = [];
        values.push({value: valueElt, valueShown: true} );
        let criteria = {key: keyElt, label: keyElt, values : values  };
        this.searchCriterias.set(keyElt, criteria);
      }
    }}
  }


  submit(formData: any){
console.log(formData)
  }


  get title() {
    return this.form.get('title');
  }
}
