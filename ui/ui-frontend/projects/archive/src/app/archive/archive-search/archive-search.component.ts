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
  searchCriterias: SearchCriteria[] = [];
  constructor(private formBuilder: FormBuilder,) { }




  ngOnInit() {
    this.searchCriterias = [];
    for(let i=0; i< 10; i++){
      let searchValues = [];
      for(let j=0; j< i%2; j++){

        let searchValue: SearchCriteriaValue = {
            value: 'value'+i+j,
            valueShown: true
        };
        searchValues.push(searchValue);
      }
      let criteria:SearchCriteria = {
        key:'BeginDt',
        label:'Begin Dt',
        values: searchValues
      }
      this.searchCriterias.push(criteria);
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

}
