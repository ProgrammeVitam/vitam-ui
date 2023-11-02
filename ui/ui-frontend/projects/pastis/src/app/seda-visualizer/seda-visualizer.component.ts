/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2020)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { PastisApiService } from '../core/api/api.pastis.service';
import { SedaData } from '../models/seda-data';


const d3 = require('d3');


@Component({
  // tslint:disable-next-line:component-selector
  selector: 'pastis-seda-visualizer',
  templateUrl: './seda-visualizer.component.html',
  styleUrls: ['./seda-visualizer.component.scss']
})
export class SedaVisualizerComponent implements OnInit {

  @ViewChild('myDiv', {static: true}) myDiv: ElementRef;

  sedaData: SedaData;

  private getSedaUrl = './assets/seda_lower.json';

  constructor(private pastisService: PastisApiService) {
  }

  ngOnInit() {
   this.pastisService.getLocally(this.getSedaUrl).subscribe(sedaRules => {
      this.sedaData = sedaRules;

      const margin = {
        top: 20, right: 120, bottom: 0, left: 120
      };
      const width = 1800 - margin.right - margin.left;
      const height = 850 - margin.top - margin.bottom;

      let i = 0;
      const duration = 550;

      let root: any;

      const tree = d3.layout.tree()
      .size([height, width]);

      const diagonal = d3.svg.diagonal()
      .projection((d: any)=> { return [d.y, d.x]; });

      const svg = d3.select('div').append('svg')
      .attr('width', width + margin.right + margin.left)
      .attr('height', height + margin.top + margin.bottom)
      .append('g')
      .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');


      root = this.sedaData[0];
      root.x0 = height / 2;
      root.y0 = 0;
      update(root);

      d3.select(self.frameElement).style('height', '500px');

      function update(source: any) {

      // Compute the new tree layout.
      const nodes = tree.nodes(root);
      const links = tree.links(nodes);


      // Normalize for fixed-depth.
      nodes.forEach((d: any)=> { d.y = d.depth * 230; });

      // Update the nodes…
      const node = svg.selectAll('g.node')
        .data(nodes, (d: any)=> { return d.id || (d.id = ++i); });

      // Enter any new nodes at the parent's previous position.
      const nodeEnter = node.enter().append('g')
        .attr('class', 'node')
        .attr('text', 'A')
        .attr('transform', ()=> { return 'translate(' + source.y0 + ',' + source.x0 + ')'; })
        .style('cursor', 'pointer')
        .on('click', click).
        on('mouseover', function(d: any) {
          const g = d3.select(this); // The node
          // The class is used to remove the additional text later
          g.append('text')
             .classed('info', true)
             .attr('x', 20)
             .attr('y', -10)
             .text(d.Definition)
             .style('font', '12px sans-serif')
             .style('font-style', 'italic');
          })
      .on('mouseout', function() {
          // Remove the info text on mouse out.
          d3.select(this).select('text.info').remove();
        });

        // Adda circle instead of a image
      nodeEnter.append('circle')
        .attr('r', 1e-6)
        .style('stroke', '#604379')
        .style('stroke-width', '2px')
        .style('fill', (d: any)=> { return d.children ? '#604379' : '#fff'; });

      nodeEnter.append('text')
        .attr('x', (d: any)=> { return d.children || d._children ? -16 : 13; })
        .attr('dy', '.35em')
        .attr('text-anchor', (d: any)=> { return d.children || d._children ? 'end' : 'start'; })
        .text((d: any)=> { return d.Name; })
        .style('fill-opacity', 1e-6)
        .style('font', '12px sans-serif')
        .style('font-weight', 'bold');


        // Letters inside circle
      nodeEnter.append('text')
        .attr('x', (d: any)=> { return d.children || d._children ? 4 : -4; })
        .attr('text-anchor', (d: any)=> { return d.children || d._children ? 'end' : 'start'; })
        .attr('dy', '.35em')
        .attr('stroke', '#65B2E4')
        .attr('stroke-width', '1px')
        .text((d: any)=> {
          if (d.Element === 'Simple') { return 'S'; }
          if (d.Element === 'Complex') { return 'C'; }
          if (d.Element === 'Attribute') { return 'A'; } })
        .style('fill-opacity', 1e-6)
        .style('font', '12px sans-serif');

      // Transition nodes to their new position.
      const nodeUpdate = node.transition()
        .duration(duration)
        .attr('transform', (d: any)=> { return 'translate(' + d.y + ',' + d.x + ')'; });

      nodeUpdate.select('circle')
        .attr('r', 12)
        .style('fill', (d: any)=> { return d.children ? '#604379' : '#fff'; });

      nodeUpdate.select('text')
        .style('fill-opacity', 1);

      // Transition exiting nodes to the parent's new position.
      const nodeExit = node.exit().transition()
        .duration(duration)
        .attr('transform', ()=> { return 'translate(' + source.y + ',' + source.x + ')'; })
        .remove();

      nodeExit.select('circle')
        .attr('r', 1e-6);

      nodeExit.select('text')
        .style('fill-opacity', 1e-6);

      // Update the links…
      const link = svg.selectAll('path.link')
        .data(links, (d: any) => { return d.target.id; });


      // Enter any new links at the parent's previous position.
      link.enter().insert('path', 'g')
        .style('fill', 'none')
        .style('stroke', (d: any)=> {
          if (d.target.cardinality === '1-N') { return '#2A9DF4'; }
          if (d.target.cardinality === '1') { return '#1167B1'; }
          if (d.target.cardinality === '0-1') { return '#555555'; }
          if (d.target.cardinality === '0-N') { return '#adb7bd'; } else { return 'black'; }}
          )
        .style('stroke-width', '2.5px')
        .attr('class', 'link')
        .attr('d', ()=> {
        const o = {x: source.x0, y: source.y0};
        return diagonal({source: o, target: o});
        });

      // Transition links to their new position.
      link.transition()
        .duration(duration)
        .attr('d', diagonal);

      // Transition exiting nodes to the parent's new position.
      link.exit().transition()
        .duration(duration)
        .attr('d', () => {
        const o = {x: source.x, y: source.y};
        return diagonal({source: o, target: o});
        })
        .remove();

      // Legend
      // select the svg area
      // tslint:disable-next-line:variable-name
      const svg_legend = d3.select('#seda_legend');
      // Nodes
      svg_legend.append('circle').attr('cx', 20).attr('cy', 30).attr('r', 6).attr('r', 12).style('stroke', '#604379').style('stroke-width', '2px').style('fill', '#fff' );
      svg_legend.append('text').attr('x', '15').attr('dy', '35').attr('stroke', '#65B2E4').text('C').style('fill-opacity', 1e-6).style('font', '12px sans-serif');
      // Simple element circle and text
      svg_legend.append('circle').attr('cx', 180).attr('cy', 30).attr('r', 6).attr('r', 12).style('stroke', '#604379').style('stroke-width', '2px').style('fill', '#fff' );
      svg_legend.append('text').attr('x', '176').attr('dy', '35').attr('stroke', '#65B2E4').text('S').style('fill-opacity', 1e-6).style('font', '12px sans-serif');
      // Attribute circle and text
      svg_legend.append('circle').attr('cx', 330).attr('cy', 30).attr('r', 6).attr('r', 12).style('stroke', '#604379').style('stroke-width', '2px').style('fill', '#fff' );
      svg_legend.append('text').attr('x', '326').attr('dy', '35').attr('stroke', '#65B2E4').text('A').style('fill-opacity', 1e-6).style('font', '12px sans-serif');

      // Cardinalities
      // 1
      svg_legend.append('line').attr('x1', 50).attr('y1', 70).attr('x2', 90).attr('y2', 70).style('stroke', '#1167B1').style('stroke-width', '2.5');
      svg_legend.append('text').attr('x', '100').attr('dy', '70').text('1').style('font-size', '15px').attr('alignment-baseline', 'middle');
      // 1-N
      svg_legend.append('line').attr('x1', 140).attr('y1', 70).attr('x2', 180).attr('y2', 70).style('stroke', '#2A9DF4').style('stroke-width', '2.5');
      // tslint:disable-next-line:max-line-length
      svg_legend.append('text').attr('x', '190').attr('dy', '70').text('1-N').style('font-size', '15px').attr('alignment-baseline', 'middle');

      svg_legend.append('line').attr('x1', 230).attr('y1', 70).attr('x2', 270).attr('y2', 70).style('stroke', '#555555').style('stroke-width', '2.5');
      // tslint:disable-next-line:max-line-length
      svg_legend.append('text').attr('x', '280').attr('dy', '70').text('0-1').style('font-size', '15px').attr('alignment-baseline', 'middle');

      svg_legend.append('line').attr('x1', 310).attr('y1', 70).attr('x2', 350).attr('y2', 70).style('stroke', '#adb7bd').style('stroke-width', '2.5');
      // tslint:disable-next-line:max-line-length
      svg_legend.append('text').attr('x', '360').attr('dy', '70').text('0-N').style('font-size', '15px').attr('alignment-baseline', 'middle');

      // Legend text
      // Nodes
      svg_legend.append('text').attr('x', 40).attr('y', 30).text('Complex Element').style('font-size', '15px').attr('alignment-baseline', 'middle');
      svg_legend.append('text').attr('x', 200).attr('y', 30).text('Simple Element').style('font-size', '15px').attr('alignment-baseline', 'middle');
      svg_legend.append('text').attr('x', 350).attr('y', 30).text('Attribute').style('font-size', '15px').attr('alignment-baseline', 'middle');

      // Cardinalities

      // Stash the old positions for transition.
      nodes.forEach((d: any)=> {
      d.x0 = d.x;
      d.y0 = d.y;
      });
    }

      // Toggle Children on click.
      function click(d: any) {
        if (d.children) {
          d._children = d.children;
          d.children = null;
        } else {
          d.children = d._children;
          d._children = null;
        }
        update(d);
      }

    });
  }


}
