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
import * as d3 from 'd3';
import { map, tap } from 'rxjs/operators';
import { PastisApiService } from '../core/api/api.pastis.service';
import { SedaData } from '../models/seda-data';

interface ExtendedHierachyNode<T> extends d3.HierarchyNode<T> {
  x0: number;
  y0: number;
  _children?: SedaData[];
}

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'pastis-seda-visualizer',
  templateUrl: './seda-visualizer.component.html',
  styleUrls: ['./seda-visualizer.component.scss'],
  standalone: true,
})
export class SedaVisualizerComponent implements OnInit {
  @ViewChild('myDiv', { static: true }) myDiv: ElementRef;

  private getSedaUrl = './assets/seda_lower.json';

  constructor(private pastisService: PastisApiService) {}

  // @See https://observablehq.com/@d3/collapsible-tree?intent=fork

  ngOnInit() {
    this.pastisService
      .getLocally(this.getSedaUrl)
      .pipe(
        tap(() => this.initLegend()),
        map((rules: SedaData[]) => rules[0]),
        tap((data: SedaData) => {
          const setChildren = (n: any) => {
            n.children = n.children || n._children;
            n.children.forEach((c: any) => setChildren(c));
          };
          setChildren(data);
        }),
      )
      .subscribe((data) => this.chart(data));
  }

  chart = (data: SedaData) => {
    // Specify the charts’ dimensions. The height is variable, depending on the layout.
    const marginTop = 60;
    const marginRight = 160;
    const marginBottom = 60;
    const marginLeft = 160;
    const width = 1960 - marginLeft - marginRight;

    // Rows are separated by dx pixels, columns by dy pixels. These names can be counter-intuitive
    // (dx is a height, and dy a width). This because the tree must be viewed with the root at the
    // “bottom”, in the data domain. The width of a column is based on the tree’s height.

    const base = d3.hierarchy(data);
    const dx = 35; // Spacing between same level circle centers
    const dy = (width * 1.5 - marginRight - marginLeft) / (1 + base.height); // Spacing between circle levels
    const root: ExtendedHierachyNode<SedaData> = Object.assign(base, { x0: dy / 2, y0: 0 });

    // Define the tree layout and the shape for links.
    const tree = d3.tree().nodeSize([dx, dy]);
    const diagonal = d3
      .linkHorizontal()
      .x((d: any) => d.y)
      .y((d: any) => d.x);

    // Create the SVG container, a layer for the links and a layer for the nodes.
    const svg = d3
      .select('div')
      .append('svg')
      .attr('width', width)
      .attr('height', dx)
      .attr('viewBox', [-marginLeft, -marginTop, width, dx].join(' '))
      .attr('style', 'max-width: 100%; height: auto; font: 10px sans-serif; user-select: none;');

    const gLink = svg.append('g').attr('fill', 'none').attr('stroke', '#555').attr('stroke-opacity', 0.4).attr('stroke-width', 2);
    const gNode = svg.append('g').attr('cursor', 'pointer').attr('pointer-events', 'all');

    const cardinalityColor = (data: SedaData): string => {
      if (data.cardinality === '1-N') return '#2A9DF4';
      if (data.cardinality === '1') return '#1167B1';
      if (data.cardinality === '0-1') return '#555555';
      if (data.cardinality === '0-N') return '#adb7bd';

      return 'black';
    };

    function update(event: any, source: any) {
      const duration = event?.altKey ? 2500 : 250; // hold the alt key to slow down the transition
      const nodes = root.descendants().reverse();
      const links = root.links();

      // Compute the new tree layout.
      tree(root);

      let left = root;
      let right = root;
      root.eachBefore((node) => {
        if (node.x < left.x) left = node;
        if (node.x > right.x) right = node;
      });

      const height = right.x - left.x + marginTop + marginBottom;

      const transition = svg
        .transition()
        .duration(duration)
        .attr('height', height)
        .attr('viewBox', [-marginLeft, left.x - marginTop, width, height].join(' '))
        .tween('resize', window.ResizeObserver ? null : () => () => svg.dispatch('toggle'));

      // Update the nodes…
      const node = gNode.selectAll('g').data(nodes, (d: any) => d.id);

      // Enter any new nodes at the parent's previous position.
      const nodeEnter = node
        .enter()
        .append('g')
        .attr('transform', (_d) => `translate(${source.y0},${source.x0})`)
        .attr('fill-opacity', 0)
        .attr('stroke-opacity', 0)
        .on('click', (event: any, d: any) => {
          d.children = d.children ? null : d._children;
          update(event, d);
        })
        .on('mouseover', function (_event: any, d: any) {
          const g = d3.select(this); // The node

          // The class is used to remove the additional text later
          g.append('text')
            .classed('info', true)
            .attr('x', 20)
            .attr('y', -10)
            .text(d.data.Definition || 'No definition')
            .style('font', '12px sans-serif')
            .style('font-style', 'italic');
        })
        .on('mouseout', function () {
          // Remove the info text on mouse out.
          d3.select(this).select('text.info').remove();
        });

      const fontSize = 12;
      const circleRadius = fontSize + 1;
      const textMargin = circleRadius + 0.5 * circleRadius;

      nodeEnter
        .append('circle')
        .attr('r', circleRadius)
        .style('stroke', '#604379')
        .style('stroke-width', '2px')
        .style('fill', (d: any) => (d.children || d._children ? '#604379' : '#fff'));

      nodeEnter
        .append('text') // Setup text near the circle
        .attr('dy', '0.31em')
        .attr('x', (d: any) => (d._children ? -textMargin : textMargin))
        .attr('text-anchor', (d: any) => (d._children ? 'end' : 'start'))
        .text((d) => d.data.Name)
        .attr('stroke-linejoin', 'round')
        .attr('stroke-width', 3)
        .attr('stroke', 'white')
        .attr('paint-order', 'stroke')
        .attr('stroke', '#65B2E4')
        .attr('stroke-width', '1px')
        .style('font-size', `${fontSize}px`);

      nodeEnter
        .append('text') // Setup letter inside circle
        .attr('x', (d: any) => (d.children || d._children ? 4 : -4))
        .attr('text-anchor', (d: any) => (d.children || d._children ? 'end' : 'start'))
        .attr('dy', '.35em')
        .attr('dx', '.05em')
        .attr('stroke', (d: any) => (d.children || d._children ? '#fff' : '#65B2E4'))
        .attr('stroke-width', '1px')
        .style('font-size', `${fontSize}px`)
        .text((d: any) => {
          if (d.data.Element === 'Simple') return 'S';
          if (d.data.Element === 'Complex') return 'C';
          if (d.data.Element === 'Attribute') 'A';

          return 'A';
        });

      // Transition nodes to their new position.
      node
        .merge(nodeEnter)
        .transition(transition)
        .attr('transform', (d) => `translate(${d.y},${d.x})`)
        .attr('fill-opacity', 1)
        .attr('stroke-opacity', 1);

      // Transition exiting nodes to the parent's new position.
      node
        .exit()
        .transition(transition)
        .remove()
        .attr('transform', (_d) => `translate(${source.y},${source.x})`)
        .attr('fill-opacity', 0)
        .attr('stroke-opacity', 0);

      // Update the links…
      const link = gLink
        .selectAll('path')
        .data(links, (d: any) => d.target.id)
        .style('stroke', (d: any) => cardinalityColor(d.target.data));

      // Enter any new links at the parent's previous position.
      const linkEnter = link
        .enter()
        .append('path')
        .attr('d', (_d) => {
          const o = { x: source.x0, y: source.y0 };
          return diagonal({ source: [o.x, o.y], target: [o.x, o.y] });
        })
        .style('stroke', (d: any) => cardinalityColor(d.target.data));

      // Transition links to their new position.
      link
        .merge(linkEnter)
        .transition(transition)
        .attr('d', diagonal as any);

      // Transition exiting nodes to the parent's new position.
      link
        .exit()
        .transition(transition)
        .remove()
        .attr('d', (_d) => {
          const o = { x: source.x, y: source.y };
          return diagonal({ source: [o.x, o.y], target: [o.x, o.y] });
        });

      // Stash the old positions for transition.
      root.eachBefore((d: any) => {
        d.x0 = d.x;
        d.y0 = d.y;
      });
    }

    // Do the first update to the initial configuration of the tree — where a number of nodes
    // are open (arbitrarily selected as the root, plus nodes with 7 letters).
    root.x0 = dy / 2;
    root.y0 = 0;
    root.descendants().forEach((d: any, i) => {
      d.id = i;
      d._children = d.children;
      if (d.depth && d.data.Name.length !== 7) d.children = null;
    });

    update(null, root);

    return svg.node();
  };

  initLegend(): void {
    // Legend
    // select the svg area
    const svg_legend = d3.select('#seda_legend');
    // Nodes
    svg_legend
      .append('circle')
      .attr('cx', 20)
      .attr('cy', 30)
      .attr('r', 6)
      .attr('r', 12)
      .style('stroke', '#604379')
      .style('stroke-width', '2px')
      .style('fill', '#fff');
    svg_legend
      .append('text')
      .attr('x', '15')
      .attr('dy', '35')
      .attr('stroke', '#65B2E4')
      .text('C')
      .style('fill-opacity', 1e-6)
      .style('font', '12px sans-serif');
    // Simple element circle and text
    svg_legend
      .append('circle')
      .attr('cx', 180)
      .attr('cy', 30)
      .attr('r', 6)
      .attr('r', 12)
      .style('stroke', '#604379')
      .style('stroke-width', '2px')
      .style('fill', '#fff');
    svg_legend
      .append('text')
      .attr('x', '176')
      .attr('dy', '35')
      .attr('stroke', '#65B2E4')
      .text('S')
      .style('fill-opacity', 1e-6)
      .style('font', '12px sans-serif');
    // Attribute circle and text
    svg_legend
      .append('circle')
      .attr('cx', 330)
      .attr('cy', 30)
      .attr('r', 6)
      .attr('r', 12)
      .style('stroke', '#604379')
      .style('stroke-width', '2px')
      .style('fill', '#fff');
    svg_legend
      .append('text')
      .attr('x', '326')
      .attr('dy', '35')
      .attr('stroke', '#65B2E4')
      .text('A')
      .style('fill-opacity', 1e-6)
      .style('font', '12px sans-serif');

    // Cardinalities
    // 1
    svg_legend
      .append('line')
      .attr('x1', 50)
      .attr('y1', 70)
      .attr('x2', 90)
      .attr('y2', 70)
      .style('stroke', '#1167B1')
      .style('stroke-width', '2.5');
    svg_legend.append('text').attr('x', '100').attr('dy', '70').text('1').style('font-size', '15px').attr('alignment-baseline', 'middle');
    // 1-N
    svg_legend
      .append('line')
      .attr('x1', 140)
      .attr('y1', 70)
      .attr('x2', 180)
      .attr('y2', 70)
      .style('stroke', '#2A9DF4')
      .style('stroke-width', '2.5');
    // eslint-disable-next-line max-len
    svg_legend.append('text').attr('x', '190').attr('dy', '70').text('1-N').style('font-size', '15px').attr('alignment-baseline', 'middle');

    svg_legend
      .append('line')
      .attr('x1', 230)
      .attr('y1', 70)
      .attr('x2', 270)
      .attr('y2', 70)
      .style('stroke', '#555555')
      .style('stroke-width', '2.5');
    // eslint-disable-next-line max-len
    svg_legend.append('text').attr('x', '280').attr('dy', '70').text('0-1').style('font-size', '15px').attr('alignment-baseline', 'middle');

    svg_legend
      .append('line')
      .attr('x1', 310)
      .attr('y1', 70)
      .attr('x2', 350)
      .attr('y2', 70)
      .style('stroke', '#adb7bd')
      .style('stroke-width', '2.5');
    // eslint-disable-next-line max-len
    svg_legend.append('text').attr('x', '360').attr('dy', '70').text('0-N').style('font-size', '15px').attr('alignment-baseline', 'middle');

    // Legend text
    // Nodes
    svg_legend
      .append('text')
      .attr('x', 40)
      .attr('y', 30)
      .text('Complex Element')
      .style('font-size', '15px')
      .attr('alignment-baseline', 'middle');
    svg_legend
      .append('text')
      .attr('x', 200)
      .attr('y', 30)
      .text('Simple Element')
      .style('font-size', '15px')
      .attr('alignment-baseline', 'middle');
    svg_legend
      .append('text')
      .attr('x', 350)
      .attr('y', 30)
      .text('Attribute')
      .style('font-size', '15px')
      .attr('alignment-baseline', 'middle');
  }
}
