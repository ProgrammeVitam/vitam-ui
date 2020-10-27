import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { SearchCriteria, SearchCriteriaValue } from '../../core/search.criteria';

@Component({
  selector: 'app-archive-search',
  templateUrl: './archive-search.component.html',
  styleUrls: ['./archive-search.component.scss']
})
export class ArchiveSearchComponent implements OnInit {
  form: FormGroup;
  searchCriterias: Map<string, SearchCriteria>;



  constructor(private formBuilder: FormBuilder,) { }


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


    this.form = this.formBuilder.group({
      title: ['', []],
      description: ['', []],
      guid: ['', []],
      beginDt: ['', []],
      endDt: ['', []],
      serviceProdLabel: ['', []],
      serviceProdCode: ['', []],
      otherCriteria: ['', []]
    });
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

}
