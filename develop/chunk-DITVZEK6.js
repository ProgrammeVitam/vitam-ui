import{$ as hd,$b as ti,$d as Ze,$e as tt,Aa as ee,Ab as Ho,Ae as Vd,B as Fe,Ba as _t,Bd as Od,Be as li,C as dd,Ca as er,Cc as Md,Ce as Nd,Da as U,Dd as ca,De as di,Ea as _e,Eb as Uo,Ec as wd,F as cd,Fa as zo,Fb as Wo,Fe as Ld,G as Zn,Gc as re,Ge as pn,H as Zr,Ha as S,Hb as B,Hc as Pn,Ia as X,Ib as v,Ic as sa,Ie as Zo,Ja as Xr,Jb as y,Jc as la,Je as Qo,Ke as Ht,L as $e,La as V,Lb as at,Le as yt,Ma as Lt,Mb as Be,Mc as O,Me as Bd,N as De,Nb as je,Nc as L,Nd as Td,Oa as pe,Ob as f,Oc as Bt,P as ud,Pa as bt,Pb as p,Pc as Qt,Qb as h,Ra as N,Rb as _,Rd as ua,Sa as On,Sb as le,Sc as Ed,T as Qn,Ta as Jr,Tb as he,Tc as W,Td as be,U as Ne,Ua as _d,Ub as Pe,Uc as k,Ud as qo,Va as bd,Vc as jt,Vd as zt,W as md,Wd as ot,We as ve,X as pd,Xb as ia,Xc as ii,Xd as Ue,Ya as ea,Yb as ze,Yc as kd,Yd as Go,Ye as jd,Zb as He,Zc as C,Zd as dn,_b as T,_c as St,_d as Xt,a as g,ac as m,ad as Ad,b as q,ba as fd,bc as Ee,ca as gd,cb as c,cc as Z,cd as Sd,ce as ma,d as Xi,da as Qr,dc as sn,dd as ne,eb as rt,ec as ke,ee as fe,fa as Le,fb as tr,fc as z,fd as Y,ff as hn,ga as an,gb as ta,gc as H,gd as vt,ge as $o,gf as pa,h as Bo,ha as ge,hb as we,hc as xd,hd as da,he as Fd,hf as zd,i as oe,ia as Xn,ib as ie,ic as ni,ie as Ko,if as ha,ja as Me,jb as na,jc as Tn,jd as Yo,jf as Hd,k as Kn,kc as Ke,kf as Ud,l as F,la as qe,lc as At,lf as Wd,m as od,ma as K,mb as Jn,mc as w,me as ai,n as jo,na as G,nf as Yd,oc as Ae,oe as Re,pa as M,pb as x,pc as P,pe as cn,qa as In,qb as $,qc as ce,qd as ri,qe as ir,ra as l,rb as E,rc as Te,re as Pd,s as sd,sb as vd,sc as Fn,sd as ln,t as Ie,tb as yd,tc as Dd,te as un,u as ld,ub as Cd,ue as Rn,v as Kr,va as Ji,vb as te,vc as ra,ve as xe,wa as on,wb as Oe,wc as aa,we as oi,xb as ei,xc as oa,xd as Id,xe as mn,ye as si,za as J,zb as nr,ze as Rd}from"./chunk-GRQUTHWA.js";function Vn(i){return i.buttons===0||i.detail===0}function Nn(i){let n=i.touches&&i.touches[0]||i.changedTouches&&i.changedTouches[0];return!!n&&n.identifier===-1&&(n.radiusX==null||n.radiusX===1)&&(n.radiusY==null||n.radiusY===1)}var rr;function qd(){if(rr==null&&typeof window<"u")try{window.addEventListener("test",null,Object.defineProperty({},"passive",{get:()=>rr=!0}))}finally{rr=rr||!1}return rr}function ci(i){return qd()?i:!!i.capture}var Gd=new M("cdk-input-modality-detector-options"),$d={ignoreKeys:[18,17,224,91,16]},Kd=650,Xo={passive:!0,capture:!0},Zd=(()=>{class i{_platform=l(be);_listenerCleanups;modalityDetected;modalityChanged;get mostRecentModality(){return this._modality.value}_mostRecentTarget=null;_modality=new od(null);_options;_lastTouchMs=0;_onKeydown=e=>{this._options?.ignoreKeys?.some(t=>t===e.keyCode)||(this._modality.next("keyboard"),this._mostRecentTarget=ot(e))};_onMousedown=e=>{Date.now()-this._lastTouchMs<Kd||(this._modality.next(Vn(e)?"keyboard":"mouse"),this._mostRecentTarget=ot(e))};_onTouchstart=e=>{if(Nn(e)){this._modality.next("keyboard");return}this._lastTouchMs=Date.now(),this._modality.next("touch"),this._mostRecentTarget=ot(e)};constructor(){let e=l(X),t=l(_e),r=l(Gd,{optional:!0});if(this._options=g(g({},$d),r),this.modalityDetected=this._modality.pipe(Qr(1)),this.modalityChanged=this.modalityDetected.pipe(md()),this._platform.isBrowser){let a=l(ta).createRenderer(null,null);this._listenerCleanups=e.runOutsideAngular(()=>[a.listen(t,"keydown",this._onKeydown,Xo),a.listen(t,"mousedown",this._onMousedown,Xo),a.listen(t,"touchstart",this._onTouchstart,Xo)])}}ngOnDestroy(){this._modality.complete(),this._listenerCleanups?.forEach(e=>e())}static \u0275fac=function(t){return new(t||i)};static \u0275prov=K({token:i,factory:i.\u0275fac,providedIn:"root"})}return i})(),ar=(function(i){return i[i.IMMEDIATE=0]="IMMEDIATE",i[i.EVENTUAL=1]="EVENTUAL",i})(ar||{}),Qd=new M("cdk-focus-monitor-default-options"),fa=ci({passive:!0,capture:!0}),It=(()=>{class i{_ngZone=l(X);_platform=l(be);_inputModalityDetector=l(Zd);_origin=null;_lastFocusOrigin=null;_windowFocused=!1;_windowFocusTimeoutId;_originTimeoutId;_originFromTouchInteraction=!1;_elementInfo=new Map;_monitoredElementCount=0;_rootNodeFocusListenerCount=new Map;_detectionMode;_windowFocusListener=()=>{this._windowFocused=!0,this._windowFocusTimeoutId=setTimeout(()=>this._windowFocused=!1)};_document=l(_e);_stopInputModalityDetector=new F;constructor(){let e=l(Qd,{optional:!0});this._detectionMode=e?.detectionMode||ar.IMMEDIATE}_rootNodeFocusAndBlurListener=e=>{let t=ot(e);for(let r=t;r;r=r.parentElement)e.type==="focus"?this._onFocus(e,r):this._onBlur(e,r)};monitor(e,t=!1){let r=Xt(e);if(!this._platform.isBrowser||r.nodeType!==1)return Ie();let a=qo(r)||this._document,o=this._elementInfo.get(r);if(o)return t&&(o.checkChildren=!0),o.subject;let s={checkChildren:t,subject:new F,rootNode:a};return this._elementInfo.set(r,s),this._registerGlobalListeners(s),s.subject}stopMonitoring(e){let t=Xt(e),r=this._elementInfo.get(t);r&&(r.subject.complete(),this._setClasses(t),this._elementInfo.delete(t),this._removeGlobalListeners(r))}focusVia(e,t,r){let a=Xt(e),o=this._document.activeElement;a===o?this._getClosestElementsInfo(a).forEach(([s,d])=>this._originChanged(s,t,d)):(this._setOrigin(t),typeof a.focus=="function"&&a.focus(r))}ngOnDestroy(){this._elementInfo.forEach((e,t)=>this.stopMonitoring(t))}_getWindow(){return this._document.defaultView||window}_getFocusOrigin(e){return this._origin?this._originFromTouchInteraction?this._shouldBeAttributedToTouch(e)?"touch":"program":this._origin:this._windowFocused&&this._lastFocusOrigin?this._lastFocusOrigin:e&&this._isLastInteractionFromInputLabel(e)?"mouse":"program"}_shouldBeAttributedToTouch(e){return this._detectionMode===ar.EVENTUAL||!!e?.contains(this._inputModalityDetector._mostRecentTarget)}_setClasses(e,t){e.classList.toggle("cdk-focused",!!t),e.classList.toggle("cdk-touch-focused",t==="touch"),e.classList.toggle("cdk-keyboard-focused",t==="keyboard"),e.classList.toggle("cdk-mouse-focused",t==="mouse"),e.classList.toggle("cdk-program-focused",t==="program")}_setOrigin(e,t=!1){this._ngZone.runOutsideAngular(()=>{if(this._origin=e,this._originFromTouchInteraction=e==="touch"&&t,this._detectionMode===ar.IMMEDIATE){clearTimeout(this._originTimeoutId);let r=this._originFromTouchInteraction?Kd:1;this._originTimeoutId=setTimeout(()=>this._origin=null,r)}})}_onFocus(e,t){let r=this._elementInfo.get(t),a=ot(e);!r||!r.checkChildren&&t!==a||this._originChanged(t,this._getFocusOrigin(a),r)}_onBlur(e,t){let r=this._elementInfo.get(t);!r||r.checkChildren&&e.relatedTarget instanceof Node&&t.contains(e.relatedTarget)||(this._setClasses(t),this._emitOrigin(r,null))}_emitOrigin(e,t){e.subject.observers.length&&this._ngZone.run(()=>e.subject.next(t))}_registerGlobalListeners(e){if(!this._platform.isBrowser)return;let t=e.rootNode,r=this._rootNodeFocusListenerCount.get(t)||0;r||this._ngZone.runOutsideAngular(()=>{t.addEventListener("focus",this._rootNodeFocusAndBlurListener,fa),t.addEventListener("blur",this._rootNodeFocusAndBlurListener,fa)}),this._rootNodeFocusListenerCount.set(t,r+1),++this._monitoredElementCount===1&&(this._ngZone.runOutsideAngular(()=>{this._getWindow().addEventListener("focus",this._windowFocusListener)}),this._inputModalityDetector.modalityDetected.pipe(ge(this._stopInputModalityDetector)).subscribe(a=>{this._setOrigin(a,!0)}))}_removeGlobalListeners(e){let t=e.rootNode;if(this._rootNodeFocusListenerCount.has(t)){let r=this._rootNodeFocusListenerCount.get(t);r>1?this._rootNodeFocusListenerCount.set(t,r-1):(t.removeEventListener("focus",this._rootNodeFocusAndBlurListener,fa),t.removeEventListener("blur",this._rootNodeFocusAndBlurListener,fa),this._rootNodeFocusListenerCount.delete(t))}--this._monitoredElementCount||(this._getWindow().removeEventListener("focus",this._windowFocusListener),this._stopInputModalityDetector.next(),clearTimeout(this._windowFocusTimeoutId),clearTimeout(this._originTimeoutId))}_originChanged(e,t,r){this._setClasses(e,t),this._emitOrigin(r,t),this._lastFocusOrigin=t}_getClosestElementsInfo(e){let t=[];return this._elementInfo.forEach((r,a)=>{(a===e||r.checkChildren&&a.contains(e))&&t.push([a,r])}),t}_isLastInteractionFromInputLabel(e){let{_mostRecentTarget:t,mostRecentModality:r}=this._inputModalityDetector;if(r!=="mouse"||!t||t===e||e.nodeName!=="INPUT"&&e.nodeName!=="TEXTAREA"||e.disabled)return!1;let a=e.labels;if(a){for(let o=0;o<a.length;o++)if(a[o].contains(t))return!0}return!1}static \u0275fac=function(t){return new(t||i)};static \u0275prov=K({token:i,factory:i.\u0275fac,providedIn:"root"})}return i})(),Jo=(()=>{class i{_elementRef=l(N);_focusMonitor=l(It);_monitorSubscription;_focusOrigin=null;cdkFocusChange=new S;constructor(){}get focusOrigin(){return this._focusOrigin}ngAfterViewInit(){let e=this._elementRef.nativeElement;this._monitorSubscription=this._focusMonitor.monitor(e,e.nodeType===1&&e.hasAttribute("cdkMonitorSubtreeFocus")).subscribe(t=>{this._focusOrigin=t,this.cdkFocusChange.emit(t)})}ngOnDestroy(){this._focusMonitor.stopMonitoring(this._elementRef),this._monitorSubscription?.unsubscribe()}static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,selectors:[["","cdkMonitorElementFocus",""],["","cdkMonitorSubtreeFocus",""]],outputs:{cdkFocusChange:"cdkFocusChange"},exportAs:["cdkMonitorFocus"]})}return i})();var Ut=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["ng-component"]],exportAs:["cdkVisuallyHidden"],decls:0,vars:0,template:function(t,r){},styles:[`.cdk-visually-hidden {
  border: 0;
  clip: rect(0 0 0 0);
  height: 1px;
  margin: -1px;
  overflow: hidden;
  padding: 0;
  position: absolute;
  width: 1px;
  white-space: nowrap;
  outline: 0;
  -webkit-appearance: none;
  -moz-appearance: none;
  left: 0;
}
[dir=rtl] .cdk-visually-hidden {
  left: auto;
  right: 0;
}
`],encapsulation:2,changeDetection:0})}return i})(),ga;function Nh(){if(ga===void 0&&(ga=null,typeof window<"u")){let i=window;i.trustedTypes!==void 0&&(ga=i.trustedTypes.createPolicy("angular#components",{createHTML:n=>n}))}return ga}function Ln(i){return Nh()?.createHTML(i)||i}function Xd(i,n,e){let t=e.sanitize(on.HTML,n);i.innerHTML=Ln(t||"")}var Jd=new Set,Bn,ui=(()=>{class i{_platform=l(be);_nonce=l(bd,{optional:!0});_matchMedia;constructor(){this._matchMedia=this._platform.isBrowser&&window.matchMedia?window.matchMedia.bind(window):Bh}matchMedia(e){return(this._platform.WEBKIT||this._platform.BLINK)&&Lh(e,this._nonce),this._matchMedia(e)}static \u0275fac=function(t){return new(t||i)};static \u0275prov=K({token:i,factory:i.\u0275fac,providedIn:"root"})}return i})();function Lh(i,n){if(!Jd.has(i))try{Bn||(Bn=document.createElement("style"),n&&Bn.setAttribute("nonce",n),Bn.setAttribute("type","text/css"),document.head.appendChild(Bn)),Bn.sheet&&(Bn.sheet.insertRule(`@media ${i} {body{ }}`,0),Jd.add(i))}catch(e){console.error(e)}}function Bh(i){return{matches:i==="all"||i==="",media:i,addListener:()=>{},removeListener:()=>{}}}var es=(()=>{class i{_mediaMatcher=l(ui);_zone=l(X);_queries=new Map;_destroySubject=new F;constructor(){}ngOnDestroy(){this._destroySubject.next(),this._destroySubject.complete()}isMatched(e){return ec(Go(e)).some(r=>this._registerQuery(r).mql.matches)}observe(e){let r=ec(Go(e)).map(o=>this._registerQuery(o).observable),a=dd(r);return a=cd(a.pipe(Ne(1)),a.pipe(Qr(1),Qn(0))),a.pipe(Fe(o=>{let s={matches:!1,breakpoints:{}};return o.forEach(({matches:d,query:u})=>{s.matches=s.matches||d,s.breakpoints[u]=d}),s}))}_registerQuery(e){if(this._queries.has(e))return this._queries.get(e);let t=this._mediaMatcher.matchMedia(e),a={observable:new Kn(o=>{let s=d=>this._zone.run(()=>o.next(d));return t.addListener(s),()=>{t.removeListener(s)}}).pipe(Le(t),Fe(({matches:o})=>({query:e,matches:o})),ge(this._destroySubject)),mql:t};return this._queries.set(e,a),a}static \u0275fac=function(t){return new(t||i)};static \u0275prov=K({token:i,factory:i.\u0275fac,providedIn:"root"})}return i})();function ec(i){return i.map(n=>n.split(",")).reduce((n,e)=>n.concat(e)).map(n=>n.trim())}function jh(i){if(i.type==="characterData"&&i.target instanceof Comment)return!0;if(i.type==="childList"){for(let n=0;n<i.addedNodes.length;n++)if(!(i.addedNodes[n]instanceof Comment))return!1;for(let n=0;n<i.removedNodes.length;n++)if(!(i.removedNodes[n]instanceof Comment))return!1;return!0}return!1}var tc=(()=>{class i{create(e){return typeof MutationObserver>"u"?null:new MutationObserver(e)}static \u0275fac=function(t){return new(t||i)};static \u0275prov=K({token:i,factory:i.\u0275fac,providedIn:"root"})}return i})(),nc=(()=>{class i{_mutationObserverFactory=l(tc);_observedElements=new Map;_ngZone=l(X);constructor(){}ngOnDestroy(){this._observedElements.forEach((e,t)=>this._cleanupObserver(t))}observe(e){let t=Xt(e);return new Kn(r=>{let o=this._observeElement(t).pipe(Fe(s=>s.filter(d=>!jh(d))),De(s=>!!s.length)).subscribe(s=>{this._ngZone.run(()=>{r.next(s)})});return()=>{o.unsubscribe(),this._unobserveElement(t)}})}_observeElement(e){return this._ngZone.runOutsideAngular(()=>{if(this._observedElements.has(e))this._observedElements.get(e).count++;else{let t=new F,r=this._mutationObserverFactory.create(a=>t.next(a));r&&r.observe(e,{characterData:!0,childList:!0,subtree:!0}),this._observedElements.set(e,{observer:r,stream:t,count:1})}return this._observedElements.get(e).stream})}_unobserveElement(e){this._observedElements.has(e)&&(this._observedElements.get(e).count--,this._observedElements.get(e).count||this._cleanupObserver(e))}_cleanupObserver(e){if(this._observedElements.has(e)){let{observer:t,stream:r}=this._observedElements.get(e);t&&t.disconnect(),r.complete(),this._observedElements.delete(e)}}static \u0275fac=function(t){return new(t||i)};static \u0275prov=K({token:i,factory:i.\u0275fac,providedIn:"root"})}return i})(),_x=(()=>{class i{_contentObserver=l(nc);_elementRef=l(N);event=new S;get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._disabled?this._unsubscribe():this._subscribe()}_disabled=!1;get debounce(){return this._debounce}set debounce(e){this._debounce=dn(e),this._subscribe()}_debounce;_currentSubscription=null;constructor(){}ngAfterContentInit(){!this._currentSubscription&&!this.disabled&&this._subscribe()}ngOnDestroy(){this._unsubscribe()}_subscribe(){this._unsubscribe();let e=this._contentObserver.observe(this._elementRef);this._currentSubscription=(this.debounce?e.pipe(Qn(this.debounce)):e).subscribe(this.event)}_unsubscribe(){this._currentSubscription?.unsubscribe()}static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,selectors:[["","cdkObserveContent",""]],inputs:{disabled:[2,"cdkObserveContentDisabled","disabled",Y],debounce:"debounce"},outputs:{event:"cdkObserveContent"},exportAs:["cdkObserveContent"]})}return i})(),_a=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275mod=$({type:i});static \u0275inj=G({providers:[tc]})}return i})();var va=(()=>{class i{_platform=l(be);constructor(){}isDisabled(e){return e.hasAttribute("disabled")}isVisible(e){return Hh(e)&&getComputedStyle(e).visibility==="visible"}isTabbable(e){if(!this._platform.isBrowser)return!1;let t=zh(Zh(e));if(t&&(ic(t)===-1||!this.isVisible(t)))return!1;let r=e.nodeName.toLowerCase(),a=ic(e);return e.hasAttribute("contenteditable")?a!==-1:r==="iframe"||r==="object"||this._platform.WEBKIT&&this._platform.IOS&&!$h(e)?!1:r==="audio"?e.hasAttribute("controls")?a!==-1:!1:r==="video"?a===-1?!1:a!==null?!0:this._platform.FIREFOX||e.hasAttribute("controls"):e.tabIndex>=0}isFocusable(e,t){return Kh(e)&&!this.isDisabled(e)&&(t?.ignoreVisibility||this.isVisible(e))}static \u0275fac=function(t){return new(t||i)};static \u0275prov=K({token:i,factory:i.\u0275fac,providedIn:"root"})}return i})();function zh(i){try{return i.frameElement}catch{return null}}function Hh(i){return!!(i.offsetWidth||i.offsetHeight||typeof i.getClientRects=="function"&&i.getClientRects().length)}function Uh(i){let n=i.nodeName.toLowerCase();return n==="input"||n==="select"||n==="button"||n==="textarea"}function Wh(i){return qh(i)&&i.type=="hidden"}function Yh(i){return Gh(i)&&i.hasAttribute("href")}function qh(i){return i.nodeName.toLowerCase()=="input"}function Gh(i){return i.nodeName.toLowerCase()=="a"}function oc(i){if(!i.hasAttribute("tabindex")||i.tabIndex===void 0)return!1;let n=i.getAttribute("tabindex");return!!(n&&!isNaN(parseInt(n,10)))}function ic(i){if(!oc(i))return null;let n=parseInt(i.getAttribute("tabindex")||"",10);return isNaN(n)?-1:n}function $h(i){let n=i.nodeName.toLowerCase(),e=n==="input"&&i.type;return e==="text"||e==="password"||n==="select"||n==="textarea"}function Kh(i){return Wh(i)?!1:Uh(i)||Yh(i)||i.hasAttribute("contenteditable")||oc(i)}function Zh(i){return i.ownerDocument&&i.ownerDocument.defaultView||window}var ba=class{_element;_checker;_ngZone;_document;_injector;_startAnchor=null;_endAnchor=null;_hasAttached=!1;startAnchorListener=()=>this.focusLastTabbableElement();endAnchorListener=()=>this.focusFirstTabbableElement();get enabled(){return this._enabled}set enabled(n){this._enabled=n,this._startAnchor&&this._endAnchor&&(this._toggleAnchorTabIndex(n,this._startAnchor),this._toggleAnchorTabIndex(n,this._endAnchor))}_enabled=!0;constructor(n,e,t,r,a=!1,o){this._element=n,this._checker=e,this._ngZone=t,this._document=r,this._injector=o,a||this.attachAnchors()}destroy(){let n=this._startAnchor,e=this._endAnchor;n&&(n.removeEventListener("focus",this.startAnchorListener),n.remove()),e&&(e.removeEventListener("focus",this.endAnchorListener),e.remove()),this._startAnchor=this._endAnchor=null,this._hasAttached=!1}attachAnchors(){return this._hasAttached?!0:(this._ngZone.runOutsideAngular(()=>{this._startAnchor||(this._startAnchor=this._createAnchor(),this._startAnchor.addEventListener("focus",this.startAnchorListener)),this._endAnchor||(this._endAnchor=this._createAnchor(),this._endAnchor.addEventListener("focus",this.endAnchorListener))}),this._element.parentNode&&(this._element.parentNode.insertBefore(this._startAnchor,this._element),this._element.parentNode.insertBefore(this._endAnchor,this._element.nextSibling),this._hasAttached=!0),this._hasAttached)}focusInitialElementWhenReady(n){return new Promise(e=>{this._executeOnStable(()=>e(this.focusInitialElement(n)))})}focusFirstTabbableElementWhenReady(n){return new Promise(e=>{this._executeOnStable(()=>e(this.focusFirstTabbableElement(n)))})}focusLastTabbableElementWhenReady(n){return new Promise(e=>{this._executeOnStable(()=>e(this.focusLastTabbableElement(n)))})}_getRegionBoundary(n){let e=this._element.querySelectorAll(`[cdk-focus-region-${n}], [cdkFocusRegion${n}], [cdk-focus-${n}]`);return n=="start"?e.length?e[0]:this._getFirstTabbableElement(this._element):e.length?e[e.length-1]:this._getLastTabbableElement(this._element)}focusInitialElement(n){let e=this._element.querySelector("[cdk-focus-initial], [cdkFocusInitial]");if(e){if(!this._checker.isFocusable(e)){let t=this._getFirstTabbableElement(e);return t?.focus(n),!!t}return e.focus(n),!0}return this.focusFirstTabbableElement(n)}focusFirstTabbableElement(n){let e=this._getRegionBoundary("start");return e&&e.focus(n),!!e}focusLastTabbableElement(n){let e=this._getRegionBoundary("end");return e&&e.focus(n),!!e}hasAttached(){return this._hasAttached}_getFirstTabbableElement(n){if(this._checker.isFocusable(n)&&this._checker.isTabbable(n))return n;let e=n.children;for(let t=0;t<e.length;t++){let r=e[t].nodeType===this._document.ELEMENT_NODE?this._getFirstTabbableElement(e[t]):null;if(r)return r}return null}_getLastTabbableElement(n){if(this._checker.isFocusable(n)&&this._checker.isTabbable(n))return n;let e=n.children;for(let t=e.length-1;t>=0;t--){let r=e[t].nodeType===this._document.ELEMENT_NODE?this._getLastTabbableElement(e[t]):null;if(r)return r}return null}_createAnchor(){let n=this._document.createElement("div");return this._toggleAnchorTabIndex(this._enabled,n),n.classList.add("cdk-visually-hidden"),n.classList.add("cdk-focus-trap-anchor"),n.setAttribute("aria-hidden","true"),n}_toggleAnchorTabIndex(n,e){n?e.setAttribute("tabindex","0"):e.removeAttribute("tabindex")}toggleAnchors(n){this._startAnchor&&this._endAnchor&&(this._toggleAnchorTabIndex(n,this._startAnchor),this._toggleAnchorTabIndex(n,this._endAnchor))}_executeOnStable(n){this._injector?rt(n,{injector:this._injector}):setTimeout(n)}},ya=(()=>{class i{_checker=l(va);_ngZone=l(X);_document=l(_e);_injector=l(U);constructor(){l(Ue).load(Ut)}create(e,t=!1){return new ba(e,this._checker,this._ngZone,this._document,t,this._injector)}static \u0275fac=function(t){return new(t||i)};static \u0275prov=K({token:i,factory:i.\u0275fac,providedIn:"root"})}return i})(),ns=(()=>{class i{_elementRef=l(N);_focusTrapFactory=l(ya);focusTrap=void 0;_previouslyFocusedElement=null;get enabled(){return this.focusTrap?.enabled||!1}set enabled(e){this.focusTrap&&(this.focusTrap.enabled=e)}autoCapture=!1;constructor(){l(be).isBrowser&&(this.focusTrap=this._focusTrapFactory.create(this._elementRef.nativeElement,!0))}ngOnDestroy(){this.focusTrap?.destroy(),this._previouslyFocusedElement&&(this._previouslyFocusedElement.focus(),this._previouslyFocusedElement=null)}ngAfterContentInit(){this.focusTrap?.attachAnchors(),this.autoCapture&&this._captureFocus()}ngDoCheck(){this.focusTrap&&!this.focusTrap.hasAttached()&&this.focusTrap.attachAnchors()}ngOnChanges(e){let t=e.autoCapture;t&&!t.firstChange&&this.autoCapture&&this.focusTrap?.hasAttached()&&this._captureFocus()}_captureFocus(){this._previouslyFocusedElement=zt(),this.focusTrap?.focusInitialElementWhenReady()}static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,selectors:[["","cdkTrapFocus",""]],inputs:{enabled:[2,"cdkTrapFocus","enabled",Y],autoCapture:[2,"cdkTrapFocusAutoCapture","autoCapture",Y]},exportAs:["cdkTrapFocus"],features:[pe]})}return i})(),sc=new M("liveAnnouncerElement",{providedIn:"root",factory:()=>null}),lc=new M("LIVE_ANNOUNCER_DEFAULT_OPTIONS"),Qh=0,is=(()=>{class i{_ngZone=l(X);_defaultOptions=l(lc,{optional:!0});_liveElement;_document=l(_e);_sanitizer=l(ua);_previousTimeout;_currentPromise;_currentResolve;constructor(){let e=l(sc,{optional:!0});this._liveElement=e||this._createLiveElement()}announce(e,...t){let r=this._defaultOptions,a,o;return t.length===1&&typeof t[0]=="number"?o=t[0]:[a,o]=t,this.clear(),clearTimeout(this._previousTimeout),a||(a=r&&r.politeness?r.politeness:"polite"),o==null&&r&&(o=r.duration),this._liveElement.setAttribute("aria-live",a),this._liveElement.id&&this._exposeAnnouncerToModals(this._liveElement.id),this._ngZone.runOutsideAngular(()=>(this._currentPromise||(this._currentPromise=new Promise(s=>this._currentResolve=s)),clearTimeout(this._previousTimeout),this._previousTimeout=setTimeout(()=>{!e||typeof e=="string"?this._liveElement.textContent=e:Xd(this._liveElement,e,this._sanitizer),typeof o=="number"&&(this._previousTimeout=setTimeout(()=>this.clear(),o)),this._currentResolve?.(),this._currentPromise=this._currentResolve=void 0},100),this._currentPromise))}clear(){this._liveElement&&(this._liveElement.textContent="")}ngOnDestroy(){clearTimeout(this._previousTimeout),this._liveElement?.remove(),this._liveElement=null,this._currentResolve?.(),this._currentPromise=this._currentResolve=void 0}_createLiveElement(){let e="cdk-live-announcer-element",t=this._document.getElementsByClassName(e),r=this._document.createElement("div");for(let a=0;a<t.length;a++)t[a].remove();return r.classList.add(e),r.classList.add("cdk-visually-hidden"),r.setAttribute("aria-atomic","true"),r.setAttribute("aria-live","polite"),r.id=`cdk-live-announcer-${Qh++}`,this._document.body.appendChild(r),r}_exposeAnnouncerToModals(e){let t=this._document.querySelectorAll('body > .cdk-overlay-container [aria-modal="true"]');for(let r=0;r<t.length;r++){let a=t[r],o=a.getAttribute("aria-owns");o?o.indexOf(e)===-1&&a.setAttribute("aria-owns",o+" "+e):a.setAttribute("aria-owns",e)}}static \u0275fac=function(t){return new(t||i)};static \u0275prov=K({token:i,factory:i.\u0275fac,providedIn:"root"})}return i})();var fn=(function(i){return i[i.NONE=0]="NONE",i[i.BLACK_ON_WHITE=1]="BLACK_ON_WHITE",i[i.WHITE_ON_BLACK=2]="WHITE_ON_BLACK",i})(fn||{}),rc="cdk-high-contrast-black-on-white",ac="cdk-high-contrast-white-on-black",ts="cdk-high-contrast-active",dc=(()=>{class i{_platform=l(be);_hasCheckedHighContrastMode=!1;_document=l(_e);_breakpointSubscription;constructor(){this._breakpointSubscription=l(es).observe("(forced-colors: active)").subscribe(()=>{this._hasCheckedHighContrastMode&&(this._hasCheckedHighContrastMode=!1,this._applyBodyHighContrastModeCssClasses())})}getHighContrastMode(){if(!this._platform.isBrowser)return fn.NONE;let e=this._document.createElement("div");e.style.backgroundColor="rgb(1,2,3)",e.style.position="absolute",this._document.body.appendChild(e);let t=this._document.defaultView||window,r=t&&t.getComputedStyle?t.getComputedStyle(e):null,a=(r&&r.backgroundColor||"").replace(/ /g,"");switch(e.remove(),a){case"rgb(0,0,0)":case"rgb(45,50,54)":case"rgb(32,32,32)":return fn.WHITE_ON_BLACK;case"rgb(255,255,255)":case"rgb(255,250,239)":return fn.BLACK_ON_WHITE}return fn.NONE}ngOnDestroy(){this._breakpointSubscription.unsubscribe()}_applyBodyHighContrastModeCssClasses(){if(!this._hasCheckedHighContrastMode&&this._platform.isBrowser&&this._document.body){let e=this._document.body.classList;e.remove(ts,rc,ac),this._hasCheckedHighContrastMode=!0;let t=this.getHighContrastMode();t===fn.BLACK_ON_WHITE?e.add(ts,rc):t===fn.WHITE_ON_BLACK&&e.add(ts,ac)}}static \u0275fac=function(t){return new(t||i)};static \u0275prov=K({token:i,factory:i.\u0275fac,providedIn:"root"})}return i})(),or=(()=>{class i{constructor(){l(dc)._applyBodyHighContrastModeCssClasses()}static \u0275fac=function(t){return new(t||i)};static \u0275mod=$({type:i});static \u0275inj=G({imports:[_a]})}return i})();function Xh(i,n){}var gn=class{viewContainerRef;injector;id;role="dialog";panelClass="";hasBackdrop=!0;backdropClass="";disableClose=!1;closePredicate;width="";height="";minWidth;minHeight;maxWidth;maxHeight;positionStrategy;data=null;direction;ariaDescribedBy=null;ariaLabelledBy=null;ariaLabel=null;ariaModal=!1;autoFocus="first-tabbable";restoreFocus=!0;scrollStrategy;closeOnNavigation=!0;closeOnDestroy=!0;closeOnOverlayDetachments=!0;disableAnimations=!1;providers;container;templateContext};var as=(()=>{class i extends Pd{_elementRef=l(N);_focusTrapFactory=l(ya);_config;_interactivityChecker=l(va);_ngZone=l(X);_focusMonitor=l(It);_renderer=l(we);_changeDetectorRef=l(ne);_injector=l(U);_platform=l(be);_document=l(_e);_portalOutlet;_focusTrapped=new F;_focusTrap=null;_elementFocusedBeforeDialogWasOpened=null;_closeInteractionType=null;_ariaLabelledByQueue=[];_isDestroyed=!1;constructor(){super(),this._config=l(gn,{optional:!0})||new gn,this._config.ariaLabelledBy&&this._ariaLabelledByQueue.push(this._config.ariaLabelledBy)}_addAriaLabelledBy(e){this._ariaLabelledByQueue.push(e),this._changeDetectorRef.markForCheck()}_removeAriaLabelledBy(e){let t=this._ariaLabelledByQueue.indexOf(e);t>-1&&(this._ariaLabelledByQueue.splice(t,1),this._changeDetectorRef.markForCheck())}_contentAttached(){this._initializeFocusTrap(),this._captureInitialFocus()}_captureInitialFocus(){this._trapFocus()}ngOnDestroy(){this._focusTrapped.complete(),this._isDestroyed=!0,this._restoreFocus()}attachComponentPortal(e){this._portalOutlet.hasAttached();let t=this._portalOutlet.attachComponentPortal(e);return this._contentAttached(),t}attachTemplatePortal(e){this._portalOutlet.hasAttached();let t=this._portalOutlet.attachTemplatePortal(e);return this._contentAttached(),t}attachDomPortal=e=>{this._portalOutlet.hasAttached();let t=this._portalOutlet.attachDomPortal(e);return this._contentAttached(),t};_recaptureFocus(){this._containsFocus()||this._trapFocus()}_forceFocus(e,t){this._interactivityChecker.isFocusable(e)||(e.tabIndex=-1,this._ngZone.runOutsideAngular(()=>{let r=()=>{a(),o(),e.removeAttribute("tabindex")},a=this._renderer.listen(e,"blur",r),o=this._renderer.listen(e,"mousedown",r)})),e.focus(t)}_focusByCssSelector(e,t){let r=this._elementRef.nativeElement.querySelector(e);r&&this._forceFocus(r,t)}_trapFocus(e){this._isDestroyed||rt(()=>{let t=this._elementRef.nativeElement;switch(this._config.autoFocus){case!1:case"dialog":this._containsFocus()||t.focus(e);break;case!0:case"first-tabbable":this._focusTrap?.focusInitialElement(e)||this._focusDialogContainer(e);break;case"first-heading":this._focusByCssSelector('h1, h2, h3, h4, h5, h6, [role="heading"]',e);break;default:this._focusByCssSelector(this._config.autoFocus,e);break}this._focusTrapped.next()},{injector:this._injector})}_restoreFocus(){let e=this._config.restoreFocus,t=null;if(typeof e=="string"?t=this._document.querySelector(e):typeof e=="boolean"?t=e?this._elementFocusedBeforeDialogWasOpened:null:e&&(t=e),this._config.restoreFocus&&t&&typeof t.focus=="function"){let r=zt(),a=this._elementRef.nativeElement;(!r||r===this._document.body||r===a||a.contains(r))&&(this._focusMonitor?(this._focusMonitor.focusVia(t,this._closeInteractionType),this._closeInteractionType=null):t.focus())}this._focusTrap&&this._focusTrap.destroy()}_focusDialogContainer(e){this._elementRef.nativeElement.focus?.(e)}_containsFocus(){let e=this._elementRef.nativeElement,t=zt();return e===t||e.contains(t)}_initializeFocusTrap(){this._platform.isBrowser&&(this._focusTrap=this._focusTrapFactory.create(this._elementRef.nativeElement),this._document&&(this._elementFocusedBeforeDialogWasOpened=zt()))}static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["cdk-dialog-container"]],viewQuery:function(t,r){if(t&1&&ke(un,7),t&2){let a;z(a=H())&&(r._portalOutlet=a.first)}},hostAttrs:["tabindex","-1",1,"cdk-dialog-container"],hostVars:6,hostBindings:function(t,r){t&2&&B("id",r._config.id||null)("role",r._config.role)("aria-modal",r._config.ariaModal)("aria-labelledby",r._config.ariaLabel?null:r._ariaLabelledByQueue[0])("aria-label",r._config.ariaLabel)("aria-describedby",r._config.ariaDescribedBy||null)},features:[te],decls:1,vars:0,consts:[["cdkPortalOutlet",""]],template:function(t,r){t&1&&Oe(0,Xh,0,0,"ng-template",0)},dependencies:[un],styles:[`.cdk-dialog-container {
  display: block;
  width: 100%;
  height: 100%;
  min-height: inherit;
  max-height: inherit;
}
`],encapsulation:2})}return i})(),sr=class{overlayRef;config;componentInstance=null;componentRef=null;containerInstance;disableClose;closed=new F;backdropClick;keydownEvents;outsidePointerEvents;id;_detachSubscription;constructor(n,e){this.overlayRef=n,this.config=e,this.disableClose=e.disableClose,this.backdropClick=n.backdropClick(),this.keydownEvents=n.keydownEvents(),this.outsidePointerEvents=n.outsidePointerEvents(),this.id=e.id,this.keydownEvents.subscribe(t=>{t.keyCode===27&&!this.disableClose&&!xe(t)&&(t.preventDefault(),this.close(void 0,{focusOrigin:"keyboard"}))}),this.backdropClick.subscribe(()=>{!this.disableClose&&this._canClose()?this.close(void 0,{focusOrigin:"mouse"}):this.containerInstance._recaptureFocus?.()}),this._detachSubscription=n.detachments().subscribe(()=>{e.closeOnOverlayDetachments!==!1&&this.close()})}close(n,e){if(this._canClose(n)){let t=this.closed;this.containerInstance._closeInteractionType=e?.focusOrigin||"program",this._detachSubscription.unsubscribe(),this.overlayRef.dispose(),t.next(n),t.complete(),this.componentInstance=this.containerInstance=null}}updatePosition(){return this.overlayRef.updatePosition(),this}updateSize(n="",e=""){return this.overlayRef.updateSize({width:n,height:e}),this}addPanelClass(n){return this.overlayRef.addPanelClass(n),this}removePanelClass(n){return this.overlayRef.removePanelClass(n),this}_canClose(n){let e=this.config;return!!this.containerInstance&&(!e.closePredicate||e.closePredicate(n,e,this.componentInstance))}},Jh=new M("DialogScrollStrategy",{providedIn:"root",factory:()=>{let i=l(U);return()=>oi(i)}}),ef=new M("DialogData"),tf=new M("DefaultDialogConfig");function nf(i){let n=V(i),e=new S;return{valueSignal:n,get value(){return n()},change:e,ngOnDestroy(){e.complete()}}}var os=(()=>{class i{_injector=l(U);_defaultOptions=l(tf,{optional:!0});_parentDialog=l(i,{optional:!0,skipSelf:!0});_overlayContainer=l(Rd);_idGenerator=l(Re);_openDialogsAtThisLevel=[];_afterAllClosedAtThisLevel=new F;_afterOpenedAtThisLevel=new F;_ariaHiddenElements=new Map;_scrollStrategy=l(Jh);get openDialogs(){return this._parentDialog?this._parentDialog.openDialogs:this._openDialogsAtThisLevel}get afterOpened(){return this._parentDialog?this._parentDialog.afterOpened:this._afterOpenedAtThisLevel}afterAllClosed=Zn(()=>this.openDialogs.length?this._getAfterAllClosed():this._getAfterAllClosed().pipe(Le(void 0)));constructor(){}open(e,t){let r=this._defaultOptions||new gn;t=g(g({},r),t),t.id=t.id||this._idGenerator.getId("cdk-dialog-"),t.id&&this.getDialogById(t.id);let a=this._getOverlayConfig(t),o=pn(this._injector,a),s=new sr(o,t),d=this._attachContainer(o,s,t);if(s.containerInstance=d,!this.openDialogs.length){let u=this._overlayContainer.getContainerElement();d._focusTrapped?d._focusTrapped.pipe(Ne(1)).subscribe(()=>{this._hideNonDialogContentFromAssistiveTechnology(u)}):this._hideNonDialogContentFromAssistiveTechnology(u)}return this._attachDialogContent(e,s,d,t),this.openDialogs.push(s),s.closed.subscribe(()=>this._removeOpenDialog(s,!0)),this.afterOpened.next(s),s}closeAll(){rs(this.openDialogs,e=>e.close())}getDialogById(e){return this.openDialogs.find(t=>t.id===e)}ngOnDestroy(){rs(this._openDialogsAtThisLevel,e=>{e.config.closeOnDestroy===!1&&this._removeOpenDialog(e,!1)}),rs(this._openDialogsAtThisLevel,e=>e.close()),this._afterAllClosedAtThisLevel.complete(),this._afterOpenedAtThisLevel.complete(),this._openDialogsAtThisLevel=[]}_getOverlayConfig(e){let t=new si({positionStrategy:e.positionStrategy||di().centerHorizontally().centerVertically(),scrollStrategy:e.scrollStrategy||this._scrollStrategy(),panelClass:e.panelClass,hasBackdrop:e.hasBackdrop,direction:e.direction,minWidth:e.minWidth,minHeight:e.minHeight,maxWidth:e.maxWidth,maxHeight:e.maxHeight,width:e.width,height:e.height,disposeOnNavigation:e.closeOnNavigation,disableAnimations:e.disableAnimations});return e.backdropClass&&(t.backdropClass=e.backdropClass),t}_attachContainer(e,t,r){let a=r.injector||r.viewContainerRef?.injector,o=[{provide:gn,useValue:r},{provide:sr,useValue:t},{provide:Vd,useValue:e}],s;r.container?typeof r.container=="function"?s=r.container:(s=r.container.type,o.push(...r.container.providers(r))):s=as;let d=new cn(s,r.viewContainerRef,U.create({parent:a||this._injector,providers:o}));return e.attach(d).instance}_attachDialogContent(e,t,r,a){if(e instanceof tr){let o=this._createInjector(a,t,r,void 0),s={$implicit:a.data,dialogRef:t};a.templateContext&&(s=g(g({},s),typeof a.templateContext=="function"?a.templateContext():a.templateContext)),r.attachTemplatePortal(new ir(e,null,s,o))}else{let o=this._createInjector(a,t,r,this._injector),s=r.attachComponentPortal(new cn(e,a.viewContainerRef,o));t.componentRef=s,t.componentInstance=s.instance}}_createInjector(e,t,r,a){let o=e.injector||e.viewContainerRef?.injector,s=[{provide:ef,useValue:e.data},{provide:sr,useValue:t}];return e.providers&&(typeof e.providers=="function"?s.push(...e.providers(t,e,r)):s.push(...e.providers)),e.direction&&(!o||!o.get(Ze,null,{optional:!0}))&&s.push({provide:Ze,useValue:nf(e.direction)}),U.create({parent:o||a,providers:s})}_removeOpenDialog(e,t){let r=this.openDialogs.indexOf(e);r>-1&&(this.openDialogs.splice(r,1),this.openDialogs.length||(this._ariaHiddenElements.forEach((a,o)=>{a?o.setAttribute("aria-hidden",a):o.removeAttribute("aria-hidden")}),this._ariaHiddenElements.clear(),t&&this._getAfterAllClosed().next()))}_hideNonDialogContentFromAssistiveTechnology(e){if(e.parentElement){let t=e.parentElement.children;for(let r=t.length-1;r>-1;r--){let a=t[r];a!==e&&a.nodeName!=="SCRIPT"&&a.nodeName!=="STYLE"&&!a.hasAttribute("aria-live")&&!a.hasAttribute("popover")&&(this._ariaHiddenElements.set(a,a.getAttribute("aria-hidden")),a.setAttribute("aria-hidden","true"))}}}_getAfterAllClosed(){let e=this._parentDialog;return e?e._getAfterAllClosed():this._afterAllClosedAtThisLevel}static \u0275fac=function(t){return new(t||i)};static \u0275prov=K({token:i,factory:i.\u0275fac,providedIn:"root"})}return i})();function rs(i,n){let e=i.length;for(;e--;)n(i[e])}var cc=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275mod=$({type:i});static \u0275inj=G({providers:[os],imports:[Ht,Rn,or,Rn]})}return i})();var Xx={XSmall:"(max-width: 599.98px)",Small:"(min-width: 600px) and (max-width: 959.98px)",Medium:"(min-width: 960px) and (max-width: 1279.98px)",Large:"(min-width: 1280px) and (max-width: 1919.98px)",XLarge:"(min-width: 1920px)",Handset:"(max-width: 599.98px) and (orientation: portrait), (max-width: 959.98px) and (orientation: landscape)",Tablet:"(min-width: 600px) and (max-width: 839.98px) and (orientation: portrait), (min-width: 960px) and (max-width: 1279.98px) and (orientation: landscape)",Web:"(min-width: 840px) and (orientation: portrait), (min-width: 1280px) and (orientation: landscape)",HandsetPortrait:"(max-width: 599.98px) and (orientation: portrait)",TabletPortrait:"(min-width: 600px) and (max-width: 839.98px) and (orientation: portrait)",WebPortrait:"(min-width: 840px) and (orientation: portrait)",HandsetLandscape:"(max-width: 959.98px) and (orientation: landscape)",TabletLandscape:"(min-width: 960px) and (max-width: 1279.98px) and (orientation: landscape)",WebLandscape:"(min-width: 1280px) and (orientation: landscape)"};var rf=new M("MATERIAL_ANIMATIONS"),uc=null;function lr(){return l(rf,{optional:!0})?.animationsDisabled||l(_d,{optional:!0})==="NoopAnimations"?"di-disabled":(uc??=l(ui).matchMedia("(prefers-reduced-motion)").matches,uc?"reduced-motion":"enabled")}function ye(){return lr()!=="enabled"}var af=200,mi=class{_letterKeyStream=new F;_items=[];_selectedItemIndex=-1;_pressedLetters=[];_skipPredicateFn;_selectedItem=new F;selectedItem=this._selectedItem;constructor(n,e){let t=typeof e?.debounceInterval=="number"?e.debounceInterval:af;e?.skipPredicate&&(this._skipPredicateFn=e.skipPredicate),this.setItems(n),this._setupKeyHandler(t)}destroy(){this._pressedLetters=[],this._letterKeyStream.complete(),this._selectedItem.complete()}setCurrentSelectedItemIndex(n){this._selectedItemIndex=n}setItems(n){this._items=n}handleKey(n){let e=n.keyCode;n.key&&n.key.length===1?this._letterKeyStream.next(n.key.toLocaleUpperCase()):(e>=65&&e<=90||e>=48&&e<=57)&&this._letterKeyStream.next(String.fromCharCode(e))}isTyping(){return this._pressedLetters.length>0}reset(){this._pressedLetters=[]}_setupKeyHandler(n){this._letterKeyStream.pipe(Xn(e=>this._pressedLetters.push(e)),Qn(n),De(()=>this._pressedLetters.length>0),Fe(()=>this._pressedLetters.join("").toLocaleUpperCase())).subscribe(e=>{for(let t=1;t<this._items.length+1;t++){let r=(this._selectedItemIndex+t)%this._items.length,a=this._items[r];if(!this._skipPredicateFn?.(a)&&a.getLabel?.().toLocaleUpperCase().trim().indexOf(e)===0){this._selectedItem.next(a);break}}this._pressedLetters=[]})}};var pi=class{_items;_activeItemIndex=V(-1);_activeItem=V(null);_wrap=!1;_typeaheadSubscription=oe.EMPTY;_itemChangesSubscription;_vertical=!0;_horizontal=null;_allowedModifierKeys=[];_homeAndEnd=!1;_pageUpAndDown={enabled:!1,delta:10};_effectRef;_typeahead;_skipPredicateFn=n=>n.disabled;constructor(n,e){this._items=n,n instanceof On?this._itemChangesSubscription=n.changes.subscribe(t=>this._itemsChanged(t.toArray())):nr(n)&&(this._effectRef=Lt(()=>this._itemsChanged(n()),{injector:e}))}tabOut=new F;change=new F;skipPredicate(n){return this._skipPredicateFn=n,this}withWrap(n=!0){return this._wrap=n,this}withVerticalOrientation(n=!0){return this._vertical=n,this}withHorizontalOrientation(n){return this._horizontal=n,this}withAllowedModifierKeys(n){return this._allowedModifierKeys=n,this}withTypeAhead(n=200){this._typeaheadSubscription.unsubscribe();let e=this._getItemsArray();return this._typeahead=new mi(e,{debounceInterval:typeof n=="number"?n:void 0,skipPredicate:t=>this._skipPredicateFn(t)}),this._typeaheadSubscription=this._typeahead.selectedItem.subscribe(t=>{this.setActiveItem(t)}),this}cancelTypeahead(){return this._typeahead?.reset(),this}withHomeAndEnd(n=!0){return this._homeAndEnd=n,this}withPageUpDown(n=!0,e=10){return this._pageUpAndDown={enabled:n,delta:e},this}setActiveItem(n){let e=this._activeItem();this.updateActiveItem(n),this._activeItem()!==e&&this.change.next(this._activeItemIndex())}onKeydown(n){let e=n.keyCode,r=["altKey","ctrlKey","metaKey","shiftKey"].every(a=>!n[a]||this._allowedModifierKeys.indexOf(a)>-1);switch(e){case 9:this.tabOut.next();return;case 40:if(this._vertical&&r){this.setNextItemActive();break}else return;case 38:if(this._vertical&&r){this.setPreviousItemActive();break}else return;case 39:if(this._horizontal&&r){this._horizontal==="rtl"?this.setPreviousItemActive():this.setNextItemActive();break}else return;case 37:if(this._horizontal&&r){this._horizontal==="rtl"?this.setNextItemActive():this.setPreviousItemActive();break}else return;case 36:if(this._homeAndEnd&&r){this.setFirstItemActive();break}else return;case 35:if(this._homeAndEnd&&r){this.setLastItemActive();break}else return;case 33:if(this._pageUpAndDown.enabled&&r){let a=this._activeItemIndex()-this._pageUpAndDown.delta;this._setActiveItemByIndex(a>0?a:0,1);break}else return;case 34:if(this._pageUpAndDown.enabled&&r){let a=this._activeItemIndex()+this._pageUpAndDown.delta,o=this._getItemsArray().length;this._setActiveItemByIndex(a<o?a:o-1,-1);break}else return;default:(r||xe(n,"shiftKey"))&&this._typeahead?.handleKey(n);return}this._typeahead?.reset(),n.preventDefault()}get activeItemIndex(){return this._activeItemIndex()}get activeItem(){return this._activeItem()}isTyping(){return!!this._typeahead&&this._typeahead.isTyping()}setFirstItemActive(){this._setActiveItemByIndex(0,1)}setLastItemActive(){this._setActiveItemByIndex(this._getItemsArray().length-1,-1)}setNextItemActive(){this._activeItemIndex()<0?this.setFirstItemActive():this._setActiveItemByDelta(1)}setPreviousItemActive(){this._activeItemIndex()<0&&this._wrap?this.setLastItemActive():this._setActiveItemByDelta(-1)}updateActiveItem(n){let e=this._getItemsArray(),t=typeof n=="number"?n:e.indexOf(n),r=e[t];this._activeItem.set(r??null),this._activeItemIndex.set(t),this._typeahead?.setCurrentSelectedItemIndex(t)}destroy(){this._typeaheadSubscription.unsubscribe(),this._itemChangesSubscription?.unsubscribe(),this._effectRef?.destroy(),this._typeahead?.destroy(),this.tabOut.complete(),this.change.complete()}_setActiveItemByDelta(n){this._wrap?this._setActiveInWrapMode(n):this._setActiveInDefaultMode(n)}_setActiveInWrapMode(n){let e=this._getItemsArray();for(let t=1;t<=e.length;t++){let r=(this._activeItemIndex()+n*t+e.length)%e.length,a=e[r];if(!this._skipPredicateFn(a)){this.setActiveItem(r);return}}}_setActiveInDefaultMode(n){this._setActiveItemByIndex(this._activeItemIndex()+n,n)}_setActiveItemByIndex(n,e){let t=this._getItemsArray();if(t[n]){for(;this._skipPredicateFn(t[n]);)if(n+=e,!t[n])return;this.setActiveItem(n)}}_getItemsArray(){return nr(this._items)?this._items():this._items instanceof On?this._items.toArray():this._items}_itemsChanged(n){this._typeahead?.setItems(n);let e=this._activeItem();if(e){let t=n.indexOf(e);t>-1&&t!==this._activeItemIndex()&&(this._activeItemIndex.set(t),this._typeahead?.setCurrentSelectedItemIndex(t))}}};var ur=class extends pi{setActiveItem(n){this.activeItem&&this.activeItem.setInactiveStyles(),super.setActiveItem(n),this.activeItem&&this.activeItem.setActiveStyles()}};var mr=class extends pi{_origin="program";setFocusOrigin(n){return this._origin=n,this}setActiveItem(n){super.setActiveItem(n),this.activeItem&&this.activeItem.focus(this._origin)}};function ss(i){return Kr(i)?i:Ie(i)}var ls=class{_activeItemIndex=-1;_activeItem=null;_shouldActivationFollowFocus=!1;_horizontalOrientation="ltr";_skipPredicateFn=n=>!1;_trackByFn=n=>n;_items=[];_typeahead;_typeaheadSubscription=oe.EMPTY;_hasInitialFocused=!1;_initializeFocus(){if(this._hasInitialFocused||this._items.length===0)return;let n=0;for(let t=0;t<this._items.length;t++)if(!this._skipPredicateFn(this._items[t])&&!this._isItemDisabled(this._items[t])){n=t;break}let e=this._items[n];e.makeFocusable?(this._activeItem?.unfocus(),this._activeItemIndex=n,this._activeItem=e,this._typeahead?.setCurrentSelectedItemIndex(n),e.makeFocusable()):this.focusItem(n),this._hasInitialFocused=!0}constructor(n,e){n instanceof On?(this._items=n.toArray(),n.changes.subscribe(t=>this._itemsChanged(t.toArray()))):Kr(n)?n.subscribe(t=>this._itemsChanged(t)):(this._items=n,this._initializeFocus()),typeof e.shouldActivationFollowFocus=="boolean"&&(this._shouldActivationFollowFocus=e.shouldActivationFollowFocus),e.horizontalOrientation&&(this._horizontalOrientation=e.horizontalOrientation),e.skipPredicate&&(this._skipPredicateFn=e.skipPredicate),e.trackBy&&(this._trackByFn=e.trackBy),typeof e.typeAheadDebounceInterval<"u"&&this._setTypeAhead(e.typeAheadDebounceInterval)}change=new F;destroy(){this._typeaheadSubscription.unsubscribe(),this._typeahead?.destroy(),this.change.complete()}onKeydown(n){switch(n.key){case"Tab":return;case"ArrowDown":this._focusNextItem();break;case"ArrowUp":this._focusPreviousItem();break;case"ArrowRight":this._horizontalOrientation==="rtl"?this._collapseCurrentItem():this._expandCurrentItem();break;case"ArrowLeft":this._horizontalOrientation==="rtl"?this._expandCurrentItem():this._collapseCurrentItem();break;case"Home":this._focusFirstItem();break;case"End":this._focusLastItem();break;case"Enter":case" ":this._activateCurrentItem();break;default:if(n.key==="*"){this._expandAllItemsAtCurrentItemLevel();break}this._typeahead?.handleKey(n);return}this._typeahead?.reset(),n.preventDefault()}getActiveItemIndex(){return this._activeItemIndex}getActiveItem(){return this._activeItem}_itemsChanged(n){this._hasInitialFocused&&this._activeItem&&!n.includes(this._activeItem)&&(this._activeItem=null,this._hasInitialFocused=!1),this._items=n,this._typeahead?.setItems(this._items),this._updateActiveItemIndex(this._items),this._initializeFocus()}_focusFirstItem(){this.focusItem(this._findNextAvailableItemIndex(-1))}_focusLastItem(){this.focusItem(this._findPreviousAvailableItemIndex(this._items.length))}_focusNextItem(){this.focusItem(this._findNextAvailableItemIndex(this._activeItemIndex))}_focusPreviousItem(){this.focusItem(this._findPreviousAvailableItemIndex(this._activeItemIndex))}focusItem(n,e={}){e.emitChangeEvent??=!0;let t=typeof n=="number"?n:this._items.findIndex(o=>this._trackByFn(o)===this._trackByFn(n));if(t<0||t>=this._items.length)return;let r=this._items[t];if(this._activeItem!==null&&this._trackByFn(r)===this._trackByFn(this._activeItem))return;let a=this._activeItem;this._activeItem=r??null,this._activeItemIndex=t,this._typeahead?.setCurrentSelectedItemIndex(t),this._activeItem?.focus(),a?.unfocus(),e.emitChangeEvent&&this.change.next(this._activeItem),this._shouldActivationFollowFocus&&this._activateCurrentItem()}_updateActiveItemIndex(n){let e=this._activeItem;if(!e)return;let t=n.findIndex(r=>this._trackByFn(r)===this._trackByFn(e));t>-1&&t!==this._activeItemIndex&&(this._activeItemIndex=t,this._typeahead?.setCurrentSelectedItemIndex(t))}_setTypeAhead(n){this._typeahead=new mi(this._items,{debounceInterval:typeof n=="number"?n:void 0,skipPredicate:e=>this._skipPredicateFn(e)}),this._typeaheadSubscription=this._typeahead.selectedItem.subscribe(e=>{this.focusItem(e)})}_findNextAvailableItemIndex(n){for(let e=n+1;e<this._items.length;e++)if(!this._skipPredicateFn(this._items[e]))return e;return n}_findPreviousAvailableItemIndex(n){for(let e=n-1;e>=0;e--)if(!this._skipPredicateFn(this._items[e]))return e;return n}_collapseCurrentItem(){if(this._activeItem)if(this._isCurrentItemExpanded())this._activeItem.collapse();else{let n=this._activeItem.getParent();if(!n||this._skipPredicateFn(n))return;this.focusItem(n)}}_expandCurrentItem(){this._activeItem&&(this._isCurrentItemExpanded()?ss(this._activeItem.getChildren()).pipe(Ne(1)).subscribe(n=>{let e=n.find(t=>!this._skipPredicateFn(t));e&&this.focusItem(e)}):this._activeItem.expand())}_isCurrentItemExpanded(){return this._activeItem?typeof this._activeItem.isExpanded=="boolean"?this._activeItem.isExpanded:this._activeItem.isExpanded():!1}_isItemDisabled(n){return typeof n.isDisabled=="boolean"?n.isDisabled:n.isDisabled?.()}_expandAllItemsAtCurrentItemLevel(){if(!this._activeItem)return;let n=this._activeItem.getParent(),e;n?e=ss(n.getChildren()):e=Ie(this._items.filter(t=>t.getParent()===null)),e.pipe(Ne(1)).subscribe(t=>{for(let r of t)r.expand()})}_activateCurrentItem(){this._activeItem?.activate()}},kD=new M("tree-key-manager",{providedIn:"root",factory:()=>(i,n)=>new ls(i,n)});var hc=" ";function us(i,n,e){let t=Ma(i,n);e=e.trim(),!t.some(r=>r.trim()===e)&&(t.push(e),i.setAttribute(n,t.join(hc)))}function wa(i,n,e){let t=Ma(i,n);e=e.trim();let r=t.filter(a=>a!==e);r.length?i.setAttribute(n,r.join(hc)):i.removeAttribute(n)}function Ma(i,n){return i.getAttribute(n)?.match(/\S+/g)??[]}var fc="cdk-describedby-message",Da="cdk-describedby-host",cs=0,gc=(()=>{class i{_platform=l(be);_document=l(_e);_messageRegistry=new Map;_messagesContainer=null;_id=`${cs++}`;constructor(){l(Ue).load(Ut),this._id=l(Jr)+"-"+cs++}describe(e,t,r){if(!this._canBeDescribed(e,t))return;let a=ds(t,r);typeof t!="string"?(pc(t,this._id),this._messageRegistry.set(a,{messageElement:t,referenceCount:0})):this._messageRegistry.has(a)||this._createMessageElement(t,r),this._isElementDescribedByMessage(e,a)||this._addMessageReference(e,a)}removeDescription(e,t,r){if(!t||!this._isElementNode(e))return;let a=ds(t,r);if(this._isElementDescribedByMessage(e,a)&&this._removeMessageReference(e,a),typeof t=="string"){let o=this._messageRegistry.get(a);o&&o.referenceCount===0&&this._deleteMessageElement(a)}this._messagesContainer?.childNodes.length===0&&(this._messagesContainer.remove(),this._messagesContainer=null)}ngOnDestroy(){let e=this._document.querySelectorAll(`[${Da}="${this._id}"]`);for(let t=0;t<e.length;t++)this._removeCdkDescribedByReferenceIds(e[t]),e[t].removeAttribute(Da);this._messagesContainer?.remove(),this._messagesContainer=null,this._messageRegistry.clear()}_createMessageElement(e,t){let r=this._document.createElement("div");pc(r,this._id),r.textContent=e,t&&r.setAttribute("role",t),this._createMessagesContainer(),this._messagesContainer.appendChild(r),this._messageRegistry.set(ds(e,t),{messageElement:r,referenceCount:0})}_deleteMessageElement(e){this._messageRegistry.get(e)?.messageElement?.remove(),this._messageRegistry.delete(e)}_createMessagesContainer(){if(this._messagesContainer)return;let e="cdk-describedby-message-container",t=this._document.querySelectorAll(`.${e}[platform="server"]`);for(let a=0;a<t.length;a++)t[a].remove();let r=this._document.createElement("div");r.style.visibility="hidden",r.classList.add(e),r.classList.add("cdk-visually-hidden"),this._platform.isBrowser||r.setAttribute("platform","server"),this._document.body.appendChild(r),this._messagesContainer=r}_removeCdkDescribedByReferenceIds(e){let t=Ma(e,"aria-describedby").filter(r=>r.indexOf(fc)!=0);e.setAttribute("aria-describedby",t.join(" "))}_addMessageReference(e,t){let r=this._messageRegistry.get(t);us(e,"aria-describedby",r.messageElement.id),e.setAttribute(Da,this._id),r.referenceCount++}_removeMessageReference(e,t){let r=this._messageRegistry.get(t);r.referenceCount--,wa(e,"aria-describedby",r.messageElement.id),e.removeAttribute(Da)}_isElementDescribedByMessage(e,t){let r=Ma(e,"aria-describedby"),a=this._messageRegistry.get(t),o=a&&a.messageElement.id;return!!o&&r.indexOf(o)!=-1}_canBeDescribed(e,t){if(!this._isElementNode(e))return!1;if(t&&typeof t=="object")return!0;let r=t==null?"":`${t}`.trim(),a=e.getAttribute("aria-label");return r?!a||a.trim()!==r:!1}_isElementNode(e){return e.nodeType===this._document.ELEMENT_NODE}static \u0275fac=function(t){return new(t||i)};static \u0275prov=K({token:i,factory:i.\u0275fac,providedIn:"root"})}return i})();function ds(i,n){return typeof i=="string"?`${n||""}/${i}`:i}function pc(i,n){i.id||(i.id=`${fc}-${n}-${cs++}`)}function of(i,n){}var ka=class{viewContainerRef;injector;id;role="dialog";panelClass="";hasBackdrop=!0;backdropClass="";disableClose=!1;closePredicate;width="";height="";minWidth;minHeight;maxWidth;maxHeight;position;data=null;direction;ariaDescribedBy=null;ariaLabelledBy=null;ariaLabel=null;ariaModal=!1;autoFocus="first-tabbable";restoreFocus=!0;delayFocusTrap=!0;scrollStrategy;closeOnNavigation=!0;enterAnimationDuration;exitAnimationDuration},ms="mdc-dialog--open",_c="mdc-dialog--opening",bc="mdc-dialog--closing",sf=150,lf=75,df=(()=>{class i extends as{_animationStateChanged=new S;_animationsEnabled=!ye();_actionSectionCount=0;_hostElement=this._elementRef.nativeElement;_enterAnimationDuration=this._animationsEnabled?yc(this._config.enterAnimationDuration)??sf:0;_exitAnimationDuration=this._animationsEnabled?yc(this._config.exitAnimationDuration)??lf:0;_animationTimer=null;_contentAttached(){super._contentAttached(),this._startOpenAnimation()}_startOpenAnimation(){this._animationStateChanged.emit({state:"opening",totalTime:this._enterAnimationDuration}),this._animationsEnabled?(this._hostElement.style.setProperty(vc,`${this._enterAnimationDuration}ms`),this._requestAnimationFrame(()=>this._hostElement.classList.add(_c,ms)),this._waitForAnimationToComplete(this._enterAnimationDuration,this._finishDialogOpen)):(this._hostElement.classList.add(ms),Promise.resolve().then(()=>this._finishDialogOpen()))}_startExitAnimation(){this._animationStateChanged.emit({state:"closing",totalTime:this._exitAnimationDuration}),this._hostElement.classList.remove(ms),this._animationsEnabled?(this._hostElement.style.setProperty(vc,`${this._exitAnimationDuration}ms`),this._requestAnimationFrame(()=>this._hostElement.classList.add(bc)),this._waitForAnimationToComplete(this._exitAnimationDuration,this._finishDialogClose)):Promise.resolve().then(()=>this._finishDialogClose())}_updateActionSectionCount(e){this._actionSectionCount+=e,this._changeDetectorRef.markForCheck()}_finishDialogOpen=()=>{this._clearAnimationClasses(),this._openAnimationDone(this._enterAnimationDuration)};_finishDialogClose=()=>{this._clearAnimationClasses(),this._animationStateChanged.emit({state:"closed",totalTime:this._exitAnimationDuration})};_clearAnimationClasses(){this._hostElement.classList.remove(_c,bc)}_waitForAnimationToComplete(e,t){this._animationTimer!==null&&clearTimeout(this._animationTimer),this._animationTimer=setTimeout(t,e)}_requestAnimationFrame(e){this._ngZone.runOutsideAngular(()=>{typeof requestAnimationFrame=="function"?requestAnimationFrame(e):e()})}_captureInitialFocus(){this._config.delayFocusTrap||this._trapFocus()}_openAnimationDone(e){this._config.delayFocusTrap&&this._trapFocus(),this._animationStateChanged.next({state:"opened",totalTime:e})}ngOnDestroy(){super.ngOnDestroy(),this._animationTimer!==null&&clearTimeout(this._animationTimer)}attachComponentPortal(e){let t=super.attachComponentPortal(e);return t.location.nativeElement.classList.add("mat-mdc-dialog-component-host"),t}static \u0275fac=(()=>{let e;return function(r){return(e||(e=bt(i)))(r||i)}})();static \u0275cmp=x({type:i,selectors:[["mat-dialog-container"]],hostAttrs:["tabindex","-1",1,"mat-mdc-dialog-container","mdc-dialog"],hostVars:10,hostBindings:function(t,r){t&2&&(He("id",r._config.id),B("aria-modal",r._config.ariaModal)("role",r._config.role)("aria-labelledby",r._config.ariaLabel?null:r._ariaLabelledByQueue[0])("aria-label",r._config.ariaLabel)("aria-describedby",r._config.ariaDescribedBy||null),w("_mat-animation-noopable",!r._animationsEnabled)("mat-mdc-dialog-container-with-actions",r._actionSectionCount>0))},features:[te],decls:3,vars:0,consts:[[1,"mat-mdc-dialog-inner-container","mdc-dialog__container"],[1,"mat-mdc-dialog-surface","mdc-dialog__surface"],["cdkPortalOutlet",""]],template:function(t,r){t&1&&(p(0,"div",0)(1,"div",1),Oe(2,of,0,0,"ng-template",2),h()())},dependencies:[un],styles:[`.mat-mdc-dialog-container {
  width: 100%;
  height: 100%;
  display: block;
  box-sizing: border-box;
  max-height: inherit;
  min-height: inherit;
  min-width: inherit;
  max-width: inherit;
  outline: 0;
}

.cdk-overlay-pane.mat-mdc-dialog-panel {
  max-width: var(--mat-dialog-container-max-width, 560px);
  min-width: var(--mat-dialog-container-min-width, 280px);
}
@media (max-width: 599px) {
  .cdk-overlay-pane.mat-mdc-dialog-panel {
    max-width: var(--mat-dialog-container-small-max-width, calc(100vw - 32px));
  }
}

.mat-mdc-dialog-inner-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-around;
  box-sizing: border-box;
  height: 100%;
  opacity: 0;
  transition: opacity linear var(--mat-dialog-transition-duration, 0ms);
  max-height: inherit;
  min-height: inherit;
  min-width: inherit;
  max-width: inherit;
}
.mdc-dialog--closing .mat-mdc-dialog-inner-container {
  transition: opacity 75ms linear;
  transform: none;
}
.mdc-dialog--open .mat-mdc-dialog-inner-container {
  opacity: 1;
}
._mat-animation-noopable .mat-mdc-dialog-inner-container {
  transition: none;
}

.mat-mdc-dialog-surface {
  display: flex;
  flex-direction: column;
  flex-grow: 0;
  flex-shrink: 0;
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  position: relative;
  overflow-y: auto;
  outline: 0;
  transform: scale(0.8);
  transition: transform var(--mat-dialog-transition-duration, 0ms) cubic-bezier(0, 0, 0.2, 1);
  max-height: inherit;
  min-height: inherit;
  min-width: inherit;
  max-width: inherit;
  box-shadow: var(--mat-dialog-container-elevation-shadow, none);
  border-radius: var(--mat-dialog-container-shape, var(--mat-sys-corner-extra-large, 4px));
  background-color: var(--mat-dialog-container-color, var(--mat-sys-surface, white));
}
[dir=rtl] .mat-mdc-dialog-surface {
  text-align: right;
}
.mdc-dialog--open .mat-mdc-dialog-surface, .mdc-dialog--closing .mat-mdc-dialog-surface {
  transform: none;
}
._mat-animation-noopable .mat-mdc-dialog-surface {
  transition: none;
}
.mat-mdc-dialog-surface::before {
  position: absolute;
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  border: 2px solid transparent;
  border-radius: inherit;
  content: "";
  pointer-events: none;
}

.mat-mdc-dialog-title {
  display: block;
  position: relative;
  flex-shrink: 0;
  box-sizing: border-box;
  margin: 0 0 1px;
  padding: var(--mat-dialog-headline-padding, 6px 24px 13px);
}
.mat-mdc-dialog-title::before {
  display: inline-block;
  width: 0;
  height: 40px;
  content: "";
  vertical-align: 0;
}
[dir=rtl] .mat-mdc-dialog-title {
  text-align: right;
}
.mat-mdc-dialog-container .mat-mdc-dialog-title {
  color: var(--mat-dialog-subhead-color, var(--mat-sys-on-surface, rgba(0, 0, 0, 0.87)));
  font-family: var(--mat-dialog-subhead-font, var(--mat-sys-headline-small-font, inherit));
  line-height: var(--mat-dialog-subhead-line-height, var(--mat-sys-headline-small-line-height, 1.5rem));
  font-size: var(--mat-dialog-subhead-size, var(--mat-sys-headline-small-size, 1rem));
  font-weight: var(--mat-dialog-subhead-weight, var(--mat-sys-headline-small-weight, 400));
  letter-spacing: var(--mat-dialog-subhead-tracking, var(--mat-sys-headline-small-tracking, 0.03125em));
}

.mat-mdc-dialog-content {
  display: block;
  flex-grow: 1;
  box-sizing: border-box;
  margin: 0;
  overflow: auto;
  max-height: 65vh;
}
.mat-mdc-dialog-content > :first-child {
  margin-top: 0;
}
.mat-mdc-dialog-content > :last-child {
  margin-bottom: 0;
}
.mat-mdc-dialog-container .mat-mdc-dialog-content {
  color: var(--mat-dialog-supporting-text-color, var(--mat-sys-on-surface-variant, rgba(0, 0, 0, 0.6)));
  font-family: var(--mat-dialog-supporting-text-font, var(--mat-sys-body-medium-font, inherit));
  line-height: var(--mat-dialog-supporting-text-line-height, var(--mat-sys-body-medium-line-height, 1.5rem));
  font-size: var(--mat-dialog-supporting-text-size, var(--mat-sys-body-medium-size, 1rem));
  font-weight: var(--mat-dialog-supporting-text-weight, var(--mat-sys-body-medium-weight, 400));
  letter-spacing: var(--mat-dialog-supporting-text-tracking, var(--mat-sys-body-medium-tracking, 0.03125em));
}
.mat-mdc-dialog-container .mat-mdc-dialog-content {
  padding: var(--mat-dialog-content-padding, 20px 24px);
}
.mat-mdc-dialog-container-with-actions .mat-mdc-dialog-content {
  padding: var(--mat-dialog-with-actions-content-padding, 20px 24px 0);
}
.mat-mdc-dialog-container .mat-mdc-dialog-title + .mat-mdc-dialog-content {
  padding-top: 0;
}

.mat-mdc-dialog-actions {
  display: flex;
  position: relative;
  flex-shrink: 0;
  flex-wrap: wrap;
  align-items: center;
  box-sizing: border-box;
  min-height: 52px;
  margin: 0;
  border-top: 1px solid transparent;
  padding: var(--mat-dialog-actions-padding, 16px 24px);
  justify-content: var(--mat-dialog-actions-alignment, flex-end);
}
@media (forced-colors: active) {
  .mat-mdc-dialog-actions {
    border-top-color: CanvasText;
  }
}
.mat-mdc-dialog-actions.mat-mdc-dialog-actions-align-start, .mat-mdc-dialog-actions[align=start] {
  justify-content: start;
}
.mat-mdc-dialog-actions.mat-mdc-dialog-actions-align-center, .mat-mdc-dialog-actions[align=center] {
  justify-content: center;
}
.mat-mdc-dialog-actions.mat-mdc-dialog-actions-align-end, .mat-mdc-dialog-actions[align=end] {
  justify-content: flex-end;
}
.mat-mdc-dialog-actions .mat-button-base + .mat-button-base,
.mat-mdc-dialog-actions .mat-mdc-button-base + .mat-mdc-button-base {
  margin-left: 8px;
}
[dir=rtl] .mat-mdc-dialog-actions .mat-button-base + .mat-button-base,
[dir=rtl] .mat-mdc-dialog-actions .mat-mdc-button-base + .mat-mdc-button-base {
  margin-left: 0;
  margin-right: 8px;
}

.mat-mdc-dialog-component-host {
  display: contents;
}
`],encapsulation:2})}return i})(),vc="--mat-dialog-transition-duration";function yc(i){return i==null?null:typeof i=="number"?i:i.endsWith("ms")?dn(i.substring(0,i.length-2)):i.endsWith("s")?dn(i.substring(0,i.length-1))*1e3:i==="0"?0:null}var Ea=(function(i){return i[i.OPEN=0]="OPEN",i[i.CLOSING=1]="CLOSING",i[i.CLOSED=2]="CLOSED",i})(Ea||{}),pr=class{_ref;_config;_containerInstance;componentInstance;componentRef=null;disableClose;id;_afterOpened=new jo(1);_beforeClosed=new jo(1);_result;_closeFallbackTimeout;_state=Ea.OPEN;_closeInteractionType;constructor(n,e,t){this._ref=n,this._config=e,this._containerInstance=t,this.disableClose=e.disableClose,this.id=n.id,n.addPanelClass("mat-mdc-dialog-panel"),t._animationStateChanged.pipe(De(r=>r.state==="opened"),Ne(1)).subscribe(()=>{this._afterOpened.next(),this._afterOpened.complete()}),t._animationStateChanged.pipe(De(r=>r.state==="closed"),Ne(1)).subscribe(()=>{clearTimeout(this._closeFallbackTimeout),this._finishDialogClose()}),n.overlayRef.detachments().subscribe(()=>{this._beforeClosed.next(this._result),this._beforeClosed.complete(),this._finishDialogClose()}),$e(this.backdropClick(),this.keydownEvents().pipe(De(r=>r.keyCode===27&&!this.disableClose&&!xe(r)))).subscribe(r=>{this.disableClose||(r.preventDefault(),Cc(this,r.type==="keydown"?"keyboard":"mouse"))})}close(n){let e=this._config.closePredicate;e&&!e(n,this._config,this.componentInstance)||(this._result=n,this._containerInstance._animationStateChanged.pipe(De(t=>t.state==="closing"),Ne(1)).subscribe(t=>{this._beforeClosed.next(n),this._beforeClosed.complete(),this._ref.overlayRef.detachBackdrop(),this._closeFallbackTimeout=setTimeout(()=>this._finishDialogClose(),t.totalTime+100)}),this._state=Ea.CLOSING,this._containerInstance._startExitAnimation())}afterOpened(){return this._afterOpened}afterClosed(){return this._ref.closed}beforeClosed(){return this._beforeClosed}backdropClick(){return this._ref.backdropClick}keydownEvents(){return this._ref.keydownEvents}updatePosition(n){let e=this._ref.config.positionStrategy;return n&&(n.left||n.right)?n.left?e.left(n.left):e.right(n.right):e.centerHorizontally(),n&&(n.top||n.bottom)?n.top?e.top(n.top):e.bottom(n.bottom):e.centerVertically(),this._ref.updatePosition(),this}updateSize(n="",e=""){return this._ref.updateSize(n,e),this}addPanelClass(n){return this._ref.addPanelClass(n),this}removePanelClass(n){return this._ref.removePanelClass(n),this}getState(){return this._state}_finishDialogClose(){this._state=Ea.CLOSED,this._ref.close(this._result,{focusOrigin:this._closeInteractionType}),this.componentInstance=null}};function Cc(i,n,e){return i._closeInteractionType=n,i.close(e)}var cf=new M("MatMdcDialogData"),uf=new M("mat-mdc-dialog-default-options"),mf=new M("mat-mdc-dialog-scroll-strategy",{providedIn:"root",factory:()=>{let i=l(U);return()=>oi(i)}}),hr=(()=>{class i{_defaultOptions=l(uf,{optional:!0});_scrollStrategy=l(mf);_parentDialog=l(i,{optional:!0,skipSelf:!0});_idGenerator=l(Re);_injector=l(U);_dialog=l(os);_animationsDisabled=ye();_openDialogsAtThisLevel=[];_afterAllClosedAtThisLevel=new F;_afterOpenedAtThisLevel=new F;dialogConfigClass=ka;_dialogRefConstructor;_dialogContainerType;_dialogDataToken;get openDialogs(){return this._parentDialog?this._parentDialog.openDialogs:this._openDialogsAtThisLevel}get afterOpened(){return this._parentDialog?this._parentDialog.afterOpened:this._afterOpenedAtThisLevel}_getAfterAllClosed(){let e=this._parentDialog;return e?e._getAfterAllClosed():this._afterAllClosedAtThisLevel}afterAllClosed=Zn(()=>this.openDialogs.length?this._getAfterAllClosed():this._getAfterAllClosed().pipe(Le(void 0)));constructor(){this._dialogRefConstructor=pr,this._dialogContainerType=df,this._dialogDataToken=cf}open(e,t){let r;t=g(g({},this._defaultOptions||new ka),t),t.id=t.id||this._idGenerator.getId("mat-mdc-dialog-"),t.scrollStrategy=t.scrollStrategy||this._scrollStrategy();let a=this._dialog.open(e,q(g({},t),{positionStrategy:di(this._injector).centerHorizontally().centerVertically(),disableClose:!0,closePredicate:void 0,closeOnDestroy:!1,closeOnOverlayDetachments:!1,disableAnimations:this._animationsDisabled||t.enterAnimationDuration?.toLocaleString()==="0"||t.exitAnimationDuration?.toString()==="0",container:{type:this._dialogContainerType,providers:()=>[{provide:this.dialogConfigClass,useValue:t},{provide:gn,useValue:t}]},templateContext:()=>({dialogRef:r}),providers:(o,s,d)=>(r=new this._dialogRefConstructor(o,t,d),r.updatePosition(t?.position),[{provide:this._dialogContainerType,useValue:d},{provide:this._dialogDataToken,useValue:s.data},{provide:this._dialogRefConstructor,useValue:r}])}));return r.componentRef=a.componentRef,r.componentInstance=a.componentInstance,this.openDialogs.push(r),this.afterOpened.next(r),r.afterClosed().subscribe(()=>{let o=this.openDialogs.indexOf(r);o>-1&&(this.openDialogs.splice(o,1),this.openDialogs.length||this._getAfterAllClosed().next())}),r}closeAll(){this._closeDialogs(this.openDialogs)}getDialogById(e){return this.openDialogs.find(t=>t.id===e)}ngOnDestroy(){this._closeDialogs(this._openDialogsAtThisLevel),this._afterAllClosedAtThisLevel.complete(),this._afterOpenedAtThisLevel.complete()}_closeDialogs(e){let t=e.length;for(;t--;)e[t].close()}static \u0275fac=function(t){return new(t||i)};static \u0275prov=K({token:i,factory:i.\u0275fac,providedIn:"root"})}return i})(),xc=(()=>{class i{dialogRef=l(pr,{optional:!0});_elementRef=l(N);_dialog=l(hr);ariaLabel;type="button";dialogResult;_matDialogClose;constructor(){}ngOnInit(){this.dialogRef||(this.dialogRef=Ec(this._elementRef,this._dialog.openDialogs))}ngOnChanges(e){let t=e._matDialogClose||e._matDialogCloseResult;t&&(this.dialogResult=t.currentValue)}_onButtonClick(e){Cc(this.dialogRef,e.screenX===0&&e.screenY===0?"keyboard":"mouse",this.dialogResult)}static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,selectors:[["","mat-dialog-close",""],["","matDialogClose",""]],hostVars:2,hostBindings:function(t,r){t&1&&T("click",function(o){return r._onButtonClick(o)}),t&2&&B("aria-label",r.ariaLabel||null)("type",r.type)},inputs:{ariaLabel:[0,"aria-label","ariaLabel"],type:"type",dialogResult:[0,"mat-dialog-close","dialogResult"],_matDialogClose:[0,"matDialogClose","_matDialogClose"]},exportAs:["matDialogClose"],features:[pe]})}return i})(),Dc=(()=>{class i{_dialogRef=l(pr,{optional:!0});_elementRef=l(N);_dialog=l(hr);constructor(){}ngOnInit(){this._dialogRef||(this._dialogRef=Ec(this._elementRef,this._dialog.openDialogs)),this._dialogRef&&Promise.resolve().then(()=>{this._onAdd()})}ngOnDestroy(){this._dialogRef?._containerInstance&&Promise.resolve().then(()=>{this._onRemove()})}static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i})}return i})(),Mc=(()=>{class i extends Dc{id=l(Re).getId("mat-mdc-dialog-title-");_onAdd(){this._dialogRef._containerInstance?._addAriaLabelledBy?.(this.id)}_onRemove(){this._dialogRef?._containerInstance?._removeAriaLabelledBy?.(this.id)}static \u0275fac=(()=>{let e;return function(r){return(e||(e=bt(i)))(r||i)}})();static \u0275dir=E({type:i,selectors:[["","mat-dialog-title",""],["","matDialogTitle",""]],hostAttrs:[1,"mat-mdc-dialog-title","mdc-dialog__title"],hostVars:1,hostBindings:function(t,r){t&2&&He("id",r.id)},inputs:{id:"id"},exportAs:["matDialogTitle"],features:[te]})}return i})(),dM=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,selectors:[["","mat-dialog-content",""],["mat-dialog-content"],["","matDialogContent",""]],hostAttrs:[1,"mat-mdc-dialog-content","mdc-dialog__content"],features:[Cd([Fd])]})}return i})(),wc=(()=>{class i extends Dc{align;_onAdd(){this._dialogRef._containerInstance?._updateActionSectionCount?.(1)}_onRemove(){this._dialogRef._containerInstance?._updateActionSectionCount?.(-1)}static \u0275fac=(()=>{let e;return function(r){return(e||(e=bt(i)))(r||i)}})();static \u0275dir=E({type:i,selectors:[["","mat-dialog-actions",""],["mat-dialog-actions"],["","matDialogActions",""]],hostAttrs:[1,"mat-mdc-dialog-actions","mdc-dialog__actions"],hostVars:6,hostBindings:function(t,r){t&2&&w("mat-mdc-dialog-actions-align-start",r.align==="start")("mat-mdc-dialog-actions-align-center",r.align==="center")("mat-mdc-dialog-actions-align-end",r.align==="end")},inputs:{align:"align"},features:[te]})}return i})();function Ec(i,n){let e=i.nativeElement.parentElement;for(;e&&!e.classList.contains("mat-mdc-dialog-container");)e=e.parentElement;return e?n.find(t=>t.id===e.id):null}var kc=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275mod=$({type:i});static \u0275inj=G({providers:[hr],imports:[cc,Ht,Rn,fe]})}return i})();var Ct=(function(i){return i[i.FADING_IN=0]="FADING_IN",i[i.VISIBLE=1]="VISIBLE",i[i.FADING_OUT=2]="FADING_OUT",i[i.HIDDEN=3]="HIDDEN",i})(Ct||{}),ps=class{_renderer;element;config;_animationForciblyDisabledThroughCss;state=Ct.HIDDEN;constructor(n,e,t,r=!1){this._renderer=n,this.element=e,this.config=t,this._animationForciblyDisabledThroughCss=r}fadeOut(){this._renderer.fadeOutRipple(this)}},Ac=ci({passive:!0,capture:!0}),hs=class{_events=new Map;addHandler(n,e,t,r){let a=this._events.get(e);if(a){let o=a.get(t);o?o.add(r):a.set(t,new Set([r]))}else this._events.set(e,new Map([[t,new Set([r])]])),n.runOutsideAngular(()=>{document.addEventListener(e,this._delegateEventHandler,Ac)})}removeHandler(n,e,t){let r=this._events.get(n);if(!r)return;let a=r.get(e);a&&(a.delete(t),a.size===0&&r.delete(e),r.size===0&&(this._events.delete(n),document.removeEventListener(n,this._delegateEventHandler,Ac)))}_delegateEventHandler=n=>{let e=ot(n);e&&this._events.get(n.type)?.forEach((t,r)=>{(r===e||r.contains(e))&&t.forEach(a=>a.handleEvent(n))})}},fr={enterDuration:225,exitDuration:150},hf=800,Sc=ci({passive:!0,capture:!0}),Ic=["mousedown","touchstart"],Oc=["mouseup","mouseleave","touchend","touchcancel"],ff=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["ng-component"]],hostAttrs:["mat-ripple-style-loader",""],decls:0,vars:0,template:function(t,r){},styles:[`.mat-ripple {
  overflow: hidden;
  position: relative;
}
.mat-ripple:not(:empty) {
  transform: translateZ(0);
}

.mat-ripple.mat-ripple-unbounded {
  overflow: visible;
}

.mat-ripple-element {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  transition: opacity, transform 0ms cubic-bezier(0, 0, 0.2, 1);
  transform: scale3d(0, 0, 0);
  background-color: var(--mat-ripple-color, color-mix(in srgb, var(--mat-sys-on-surface) 10%, transparent));
}
@media (forced-colors: active) {
  .mat-ripple-element {
    display: none;
  }
}
.cdk-drag-preview .mat-ripple-element, .cdk-drag-placeholder .mat-ripple-element {
  display: none;
}
`],encapsulation:2,changeDetection:0})}return i})(),gr=class i{_target;_ngZone;_platform;_containerElement;_triggerElement=null;_isPointerDown=!1;_activeRipples=new Map;_mostRecentTransientRipple=null;_lastTouchStartEvent;_pointerUpEventsRegistered=!1;_containerRect=null;static _eventManager=new hs;constructor(n,e,t,r,a){this._target=n,this._ngZone=e,this._platform=r,r.isBrowser&&(this._containerElement=Xt(t)),a&&a.get(Ue).load(ff)}fadeInRipple(n,e,t={}){let r=this._containerRect=this._containerRect||this._containerElement.getBoundingClientRect(),a=g(g({},fr),t.animation);t.centered&&(n=r.left+r.width/2,e=r.top+r.height/2);let o=t.radius||gf(n,e,r),s=n-r.left,d=e-r.top,u=a.enterDuration,b=document.createElement("div");b.classList.add("mat-ripple-element"),b.style.left=`${s-o}px`,b.style.top=`${d-o}px`,b.style.height=`${o*2}px`,b.style.width=`${o*2}px`,t.color!=null&&(b.style.backgroundColor=t.color),b.style.transitionDuration=`${u}ms`,this._containerElement.appendChild(b);let A=window.getComputedStyle(b),Q=A.transitionProperty,I=A.transitionDuration,me=Q==="none"||I==="0s"||I==="0s, 0s"||r.width===0&&r.height===0,Se=new ps(this,b,t,me);b.style.transform="scale3d(1, 1, 1)",Se.state=Ct.FADING_IN,t.persistent||(this._mostRecentTransientRipple=Se);let Je=null;return!me&&(u||a.exitDuration)&&this._ngZone.runOutsideAngular(()=>{let Kt=()=>{Je&&(Je.fallbackTimer=null),clearTimeout(Ki),this._finishRippleTransition(Se)},Zt=()=>this._destroyRipple(Se),Ki=setTimeout(Zt,u+100);b.addEventListener("transitionend",Kt),b.addEventListener("transitioncancel",Zt),Je={onTransitionEnd:Kt,onTransitionCancel:Zt,fallbackTimer:Ki}}),this._activeRipples.set(Se,Je),(me||!u)&&this._finishRippleTransition(Se),Se}fadeOutRipple(n){if(n.state===Ct.FADING_OUT||n.state===Ct.HIDDEN)return;let e=n.element,t=g(g({},fr),n.config.animation);e.style.transitionDuration=`${t.exitDuration}ms`,e.style.opacity="0",n.state=Ct.FADING_OUT,(n._animationForciblyDisabledThroughCss||!t.exitDuration)&&this._finishRippleTransition(n)}fadeOutAll(){this._getActiveRipples().forEach(n=>n.fadeOut())}fadeOutAllNonPersistent(){this._getActiveRipples().forEach(n=>{n.config.persistent||n.fadeOut()})}setupTriggerEvents(n){let e=Xt(n);!this._platform.isBrowser||!e||e===this._triggerElement||(this._removeTriggerEvents(),this._triggerElement=e,Ic.forEach(t=>{i._eventManager.addHandler(this._ngZone,t,e,this)}))}handleEvent(n){n.type==="mousedown"?this._onMousedown(n):n.type==="touchstart"?this._onTouchStart(n):this._onPointerUp(),this._pointerUpEventsRegistered||(this._ngZone.runOutsideAngular(()=>{Oc.forEach(e=>{this._triggerElement.addEventListener(e,this,Sc)})}),this._pointerUpEventsRegistered=!0)}_finishRippleTransition(n){n.state===Ct.FADING_IN?this._startFadeOutTransition(n):n.state===Ct.FADING_OUT&&this._destroyRipple(n)}_startFadeOutTransition(n){let e=n===this._mostRecentTransientRipple,{persistent:t}=n.config;n.state=Ct.VISIBLE,!t&&(!e||!this._isPointerDown)&&n.fadeOut()}_destroyRipple(n){let e=this._activeRipples.get(n)??null;this._activeRipples.delete(n),this._activeRipples.size||(this._containerRect=null),n===this._mostRecentTransientRipple&&(this._mostRecentTransientRipple=null),n.state=Ct.HIDDEN,e!==null&&(n.element.removeEventListener("transitionend",e.onTransitionEnd),n.element.removeEventListener("transitioncancel",e.onTransitionCancel),e.fallbackTimer!==null&&clearTimeout(e.fallbackTimer)),n.element.remove()}_onMousedown(n){let e=Vn(n),t=this._lastTouchStartEvent&&Date.now()<this._lastTouchStartEvent+hf;!this._target.rippleDisabled&&!e&&!t&&(this._isPointerDown=!0,this.fadeInRipple(n.clientX,n.clientY,this._target.rippleConfig))}_onTouchStart(n){if(!this._target.rippleDisabled&&!Nn(n)){this._lastTouchStartEvent=Date.now(),this._isPointerDown=!0;let e=n.changedTouches;if(e)for(let t=0;t<e.length;t++)this.fadeInRipple(e[t].clientX,e[t].clientY,this._target.rippleConfig)}}_onPointerUp(){this._isPointerDown&&(this._isPointerDown=!1,this._getActiveRipples().forEach(n=>{let e=n.state===Ct.VISIBLE||n.config.terminateOnPointerUp&&n.state===Ct.FADING_IN;!n.config.persistent&&e&&n.fadeOut()}))}_getActiveRipples(){return Array.from(this._activeRipples.keys())}_removeTriggerEvents(){let n=this._triggerElement;n&&(Ic.forEach(e=>i._eventManager.removeHandler(e,n,this)),this._pointerUpEventsRegistered&&(Oc.forEach(e=>n.removeEventListener(e,this,Sc)),this._pointerUpEventsRegistered=!1))}};function gf(i,n,e){let t=Math.max(Math.abs(i-e.left),Math.abs(i-e.right)),r=Math.max(Math.abs(n-e.top),Math.abs(n-e.bottom));return Math.sqrt(t*t+r*r)}var fs=new M("mat-ripple-global-options"),Aa=(()=>{class i{_elementRef=l(N);_animationsDisabled=ye();color;unbounded=!1;centered=!1;radius=0;animation;get disabled(){return this._disabled}set disabled(e){e&&this.fadeOutAllNonPersistent(),this._disabled=e,this._setupTriggerEventsIfEnabled()}_disabled=!1;get trigger(){return this._trigger||this._elementRef.nativeElement}set trigger(e){this._trigger=e,this._setupTriggerEventsIfEnabled()}_trigger;_rippleRenderer;_globalOptions;_isInitialized=!1;constructor(){let e=l(X),t=l(be),r=l(fs,{optional:!0}),a=l(U);this._globalOptions=r||{},this._rippleRenderer=new gr(this,e,this._elementRef,t,a)}ngOnInit(){this._isInitialized=!0,this._setupTriggerEventsIfEnabled()}ngOnDestroy(){this._rippleRenderer._removeTriggerEvents()}fadeOutAll(){this._rippleRenderer.fadeOutAll()}fadeOutAllNonPersistent(){this._rippleRenderer.fadeOutAllNonPersistent()}get rippleConfig(){return{centered:this.centered,radius:this.radius,color:this.color,animation:g(g(g({},this._globalOptions.animation),this._animationsDisabled?{enterDuration:0,exitDuration:0}:{}),this.animation),terminateOnPointerUp:this._globalOptions.terminateOnPointerUp}}get rippleDisabled(){return this.disabled||!!this._globalOptions.disabled}_setupTriggerEventsIfEnabled(){!this.disabled&&this._isInitialized&&this._rippleRenderer.setupTriggerEvents(this.trigger)}launch(e,t=0,r){return typeof e=="number"?this._rippleRenderer.fadeInRipple(e,t,g(g({},this.rippleConfig),r)):this._rippleRenderer.fadeInRipple(0,0,g(g({},this.rippleConfig),e))}static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,selectors:[["","mat-ripple",""],["","matRipple",""]],hostAttrs:[1,"mat-ripple"],hostVars:2,hostBindings:function(t,r){t&2&&w("mat-ripple-unbounded",r.unbounded)},inputs:{color:[0,"matRippleColor","color"],unbounded:[0,"matRippleUnbounded","unbounded"],centered:[0,"matRippleCentered","centered"],radius:[0,"matRippleRadius","radius"],animation:[0,"matRippleAnimation","animation"],disabled:[0,"matRippleDisabled","disabled"],trigger:[0,"matRippleTrigger","trigger"]},exportAs:["matRipple"]})}return i})();var _f={capture:!0},bf=["focus","mousedown","mouseenter","touchstart"],gs="mat-ripple-loader-uninitialized",_s="mat-ripple-loader-class-name",Tc="mat-ripple-loader-centered",Sa="mat-ripple-loader-disabled",Fc=(()=>{class i{_document=l(_e);_animationsDisabled=ye();_globalRippleOptions=l(fs,{optional:!0});_platform=l(be);_ngZone=l(X);_injector=l(U);_eventCleanups;_hosts=new Map;constructor(){let e=l(ta).createRenderer(null,null);this._eventCleanups=this._ngZone.runOutsideAngular(()=>bf.map(t=>e.listen(this._document,t,this._onInteraction,_f)))}ngOnDestroy(){let e=this._hosts.keys();for(let t of e)this.destroyRipple(t);this._eventCleanups.forEach(t=>t())}configureRipple(e,t){e.setAttribute(gs,this._globalRippleOptions?.namespace??""),(t.className||!e.hasAttribute(_s))&&e.setAttribute(_s,t.className||""),t.centered&&e.setAttribute(Tc,""),t.disabled&&e.setAttribute(Sa,"")}setDisabled(e,t){let r=this._hosts.get(e);r?(r.target.rippleDisabled=t,!t&&!r.hasSetUpEvents&&(r.hasSetUpEvents=!0,r.renderer.setupTriggerEvents(e))):t?e.setAttribute(Sa,""):e.removeAttribute(Sa)}_onInteraction=e=>{let t=ot(e);if(t instanceof HTMLElement){let r=t.closest(`[${gs}="${this._globalRippleOptions?.namespace??""}"]`);r&&this._createRipple(r)}};_createRipple(e){if(!this._document||this._hosts.has(e))return;e.querySelector(".mat-ripple")?.remove();let t=this._document.createElement("span");t.classList.add("mat-ripple",e.getAttribute(_s)),e.append(t);let r=this._globalRippleOptions,a=this._animationsDisabled?0:r?.animation?.enterDuration??fr.enterDuration,o=this._animationsDisabled?0:r?.animation?.exitDuration??fr.exitDuration,s={rippleDisabled:this._animationsDisabled||r?.disabled||e.hasAttribute(Sa),rippleConfig:{centered:e.hasAttribute(Tc),terminateOnPointerUp:r?.terminateOnPointerUp,animation:{enterDuration:a,exitDuration:o}}},d=new gr(s,this._ngZone,t,this._platform,this._injector),u=!s.rippleDisabled;u&&d.setupTriggerEvents(e),this._hosts.set(e,{target:s,renderer:d,hasSetUpEvents:u}),e.removeAttribute(gs)}destroyRipple(e){let t=this._hosts.get(e);t&&(t.renderer._removeTriggerEvents(),this._hosts.delete(e))}static \u0275fac=function(t){return new(t||i)};static \u0275prov=K({token:i,factory:i.\u0275fac,providedIn:"root"})}return i})();var _n=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["structural-styles"]],decls:0,vars:0,template:function(t,r){},styles:[`.mat-focus-indicator {
  position: relative;
}
.mat-focus-indicator::before {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  box-sizing: border-box;
  pointer-events: none;
  display: var(--mat-focus-indicator-display, none);
  border-width: var(--mat-focus-indicator-border-width, 3px);
  border-style: var(--mat-focus-indicator-border-style, solid);
  border-color: var(--mat-focus-indicator-border-color, transparent);
  border-radius: var(--mat-focus-indicator-border-radius, 4px);
}
.mat-focus-indicator:focus-visible::before {
  content: "";
}

@media (forced-colors: active) {
  html {
    --mat-focus-indicator-display: block;
  }
}
`],encapsulation:2,changeDetection:0})}return i})();var vf=["mat-icon-button",""],yf=["*"],Cf=new M("MAT_BUTTON_CONFIG");function Pc(i){return i==null?void 0:vt(i)}var Ia=(()=>{class i{_elementRef=l(N);_ngZone=l(X);_animationsDisabled=ye();_config=l(Cf,{optional:!0});_focusMonitor=l(It);_cleanupClick;_renderer=l(we);_rippleLoader=l(Fc);_isAnchor;_isFab=!1;color;get disableRipple(){return this._disableRipple}set disableRipple(e){this._disableRipple=e,this._updateRippleDisabled()}_disableRipple=!1;get disabled(){return this._disabled}set disabled(e){this._disabled=e,this._updateRippleDisabled()}_disabled=!1;ariaDisabled;disabledInteractive;tabIndex;set _tabindex(e){this.tabIndex=e}constructor(){l(Ue).load(_n);let e=this._elementRef.nativeElement;this._isAnchor=e.tagName==="A",this.disabledInteractive=this._config?.disabledInteractive??!1,this.color=this._config?.color??null,this._rippleLoader?.configureRipple(e,{className:"mat-mdc-button-ripple"})}ngAfterViewInit(){this._focusMonitor.monitor(this._elementRef,!0),this._isAnchor&&this._setupAsAnchor()}ngOnDestroy(){this._cleanupClick?.(),this._focusMonitor.stopMonitoring(this._elementRef),this._rippleLoader?.destroyRipple(this._elementRef.nativeElement)}focus(e="program",t){e?this._focusMonitor.focusVia(this._elementRef.nativeElement,e,t):this._elementRef.nativeElement.focus(t)}_getAriaDisabled(){return this.ariaDisabled!=null?this.ariaDisabled:this._isAnchor?this.disabled||null:this.disabled&&this.disabledInteractive?!0:null}_getDisabledAttribute(){return this.disabledInteractive||!this.disabled?null:!0}_updateRippleDisabled(){this._rippleLoader?.setDisabled(this._elementRef.nativeElement,this.disableRipple||this.disabled)}_getTabIndex(){return this._isAnchor?this.disabled&&!this.disabledInteractive?-1:this.tabIndex:this.tabIndex}_setupAsAnchor(){this._cleanupClick=this._ngZone.runOutsideAngular(()=>this._renderer.listen(this._elementRef.nativeElement,"click",e=>{this.disabled&&(e.preventDefault(),e.stopImmediatePropagation())}))}static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,hostAttrs:[1,"mat-mdc-button-base"],hostVars:13,hostBindings:function(t,r){t&2&&(B("disabled",r._getDisabledAttribute())("aria-disabled",r._getAriaDisabled())("tabindex",r._getTabIndex()),Ae(r.color?"mat-"+r.color:""),w("mat-mdc-button-disabled",r.disabled)("mat-mdc-button-disabled-interactive",r.disabledInteractive)("mat-unthemed",!r.color)("_mat-animation-noopable",r._animationsDisabled))},inputs:{color:"color",disableRipple:[2,"disableRipple","disableRipple",Y],disabled:[2,"disabled","disabled",Y],ariaDisabled:[2,"aria-disabled","ariaDisabled",Y],disabledInteractive:[2,"disabledInteractive","disabledInteractive",Y],tabIndex:[2,"tabIndex","tabIndex",Pc],_tabindex:[2,"tabindex","_tabindex",Pc]}})}return i})(),Oa=(()=>{class i extends Ia{constructor(){super(),this._rippleLoader.configureRipple(this._elementRef.nativeElement,{centered:!0})}static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["button","mat-icon-button",""],["a","mat-icon-button",""],["button","matIconButton",""],["a","matIconButton",""]],hostAttrs:[1,"mdc-icon-button","mat-mdc-icon-button"],exportAs:["matButton","matAnchor"],features:[te],attrs:vf,ngContentSelectors:yf,decls:4,vars:0,consts:[[1,"mat-mdc-button-persistent-ripple","mdc-icon-button__ripple"],[1,"mat-focus-indicator"],[1,"mat-mdc-button-touch-target"]],template:function(t,r){t&1&&(Ee(),Pe(0,"span",0),Z(1),Pe(2,"span",1)(3,"span",2))},styles:[`.mat-mdc-icon-button {
  -webkit-user-select: none;
  user-select: none;
  display: inline-block;
  position: relative;
  box-sizing: border-box;
  border: none;
  outline: none;
  background-color: transparent;
  fill: currentColor;
  text-decoration: none;
  cursor: pointer;
  z-index: 0;
  overflow: visible;
  border-radius: var(--mat-icon-button-container-shape, var(--mat-sys-corner-full, 50%));
  flex-shrink: 0;
  text-align: center;
  width: var(--mat-icon-button-state-layer-size, 40px);
  height: var(--mat-icon-button-state-layer-size, 40px);
  padding: calc(calc(var(--mat-icon-button-state-layer-size, 40px) - var(--mat-icon-button-icon-size, 24px)) / 2);
  font-size: var(--mat-icon-button-icon-size, 24px);
  color: var(--mat-icon-button-icon-color, var(--mat-sys-on-surface-variant));
  -webkit-tap-highlight-color: transparent;
}
.mat-mdc-icon-button .mat-mdc-button-ripple,
.mat-mdc-icon-button .mat-mdc-button-persistent-ripple,
.mat-mdc-icon-button .mat-mdc-button-persistent-ripple::before {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  pointer-events: none;
  border-radius: inherit;
}
.mat-mdc-icon-button .mat-mdc-button-ripple {
  overflow: hidden;
}
.mat-mdc-icon-button .mat-mdc-button-persistent-ripple::before {
  content: "";
  opacity: 0;
}
.mat-mdc-icon-button .mdc-button__label,
.mat-mdc-icon-button .mat-icon {
  z-index: 1;
  position: relative;
}
.mat-mdc-icon-button .mat-focus-indicator {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  border-radius: inherit;
}
.mat-mdc-icon-button:focus-visible > .mat-focus-indicator::before {
  content: "";
  border-radius: inherit;
}
.mat-mdc-icon-button .mat-ripple-element {
  background-color: var(--mat-icon-button-ripple-color, color-mix(in srgb, var(--mat-sys-on-surface-variant) calc(var(--mat-sys-pressed-state-layer-opacity) * 100%), transparent));
}
.mat-mdc-icon-button .mat-mdc-button-persistent-ripple::before {
  background-color: var(--mat-icon-button-state-layer-color, var(--mat-sys-on-surface-variant));
}
.mat-mdc-icon-button.mat-mdc-button-disabled .mat-mdc-button-persistent-ripple::before {
  background-color: var(--mat-icon-button-disabled-state-layer-color, var(--mat-sys-on-surface-variant));
}
.mat-mdc-icon-button:hover > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-icon-button-hover-state-layer-opacity, var(--mat-sys-hover-state-layer-opacity));
}
.mat-mdc-icon-button.cdk-program-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-icon-button.cdk-keyboard-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-icon-button.mat-mdc-button-disabled-interactive:focus > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-icon-button-focus-state-layer-opacity, var(--mat-sys-focus-state-layer-opacity));
}
.mat-mdc-icon-button:active > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-icon-button-pressed-state-layer-opacity, var(--mat-sys-pressed-state-layer-opacity));
}
.mat-mdc-icon-button .mat-mdc-button-touch-target {
  position: absolute;
  top: 50%;
  height: var(--mat-icon-button-touch-target-size, 48px);
  display: var(--mat-icon-button-touch-target-display, block);
  left: 50%;
  width: var(--mat-icon-button-touch-target-size, 48px);
  transform: translate(-50%, -50%);
}
.mat-mdc-icon-button._mat-animation-noopable {
  transition: none !important;
  animation: none !important;
}
.mat-mdc-icon-button[disabled], .mat-mdc-icon-button.mat-mdc-button-disabled {
  cursor: default;
  pointer-events: none;
  color: var(--mat-icon-button-disabled-icon-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}
.mat-mdc-icon-button.mat-mdc-button-disabled-interactive {
  pointer-events: auto;
}
.mat-mdc-icon-button img,
.mat-mdc-icon-button svg {
  width: var(--mat-icon-button-icon-size, 24px);
  height: var(--mat-icon-button-icon-size, 24px);
  vertical-align: baseline;
}
.mat-mdc-icon-button .mat-mdc-button-persistent-ripple {
  border-radius: var(--mat-icon-button-container-shape, var(--mat-sys-corner-full, 50%));
}
.mat-mdc-icon-button[hidden] {
  display: none;
}
.mat-mdc-icon-button.mat-unthemed:not(.mdc-ripple-upgraded):focus::before, .mat-mdc-icon-button.mat-primary:not(.mdc-ripple-upgraded):focus::before, .mat-mdc-icon-button.mat-accent:not(.mdc-ripple-upgraded):focus::before, .mat-mdc-icon-button.mat-warn:not(.mdc-ripple-upgraded):focus::before {
  background: transparent;
  opacity: 1;
}
`,`@media (forced-colors: active) {
  .mat-mdc-button:not(.mdc-button--outlined),
  .mat-mdc-unelevated-button:not(.mdc-button--outlined),
  .mat-mdc-raised-button:not(.mdc-button--outlined),
  .mat-mdc-outlined-button:not(.mdc-button--outlined),
  .mat-mdc-button-base.mat-tonal-button,
  .mat-mdc-icon-button.mat-mdc-icon-button,
  .mat-mdc-outlined-button .mdc-button__ripple {
    outline: solid 1px;
  }
}
`],encapsulation:2,changeDetection:0})}return i})();var hi=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275mod=$({type:i});static \u0275inj=G({imports:[fe]})}return i})();var xf=["matButton",""],Vc=[[["",8,"material-icons",3,"iconPositionEnd",""],["mat-icon",3,"iconPositionEnd",""],["","matButtonIcon","",3,"iconPositionEnd",""]],"*",[["","iconPositionEnd","",8,"material-icons"],["mat-icon","iconPositionEnd",""],["","matButtonIcon","","iconPositionEnd",""]]],Nc=[".material-icons:not([iconPositionEnd]), mat-icon:not([iconPositionEnd]), [matButtonIcon]:not([iconPositionEnd])","*",".material-icons[iconPositionEnd], mat-icon[iconPositionEnd], [matButtonIcon][iconPositionEnd]"];var Df=["mat-mini-fab",""],Mf=`.mat-mdc-fab-base {
  -webkit-user-select: none;
  user-select: none;
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  width: 56px;
  height: 56px;
  padding: 0;
  border: none;
  fill: currentColor;
  text-decoration: none;
  cursor: pointer;
  -moz-appearance: none;
  -webkit-appearance: none;
  overflow: visible;
  transition: box-shadow 280ms cubic-bezier(0.4, 0, 0.2, 1), opacity 15ms linear 30ms, transform 270ms 0ms cubic-bezier(0, 0, 0.2, 1);
  flex-shrink: 0;
  -webkit-tap-highlight-color: transparent;
}
.mat-mdc-fab-base .mat-mdc-button-ripple,
.mat-mdc-fab-base .mat-mdc-button-persistent-ripple,
.mat-mdc-fab-base .mat-mdc-button-persistent-ripple::before {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  pointer-events: none;
  border-radius: inherit;
}
.mat-mdc-fab-base .mat-mdc-button-ripple {
  overflow: hidden;
}
.mat-mdc-fab-base .mat-mdc-button-persistent-ripple::before {
  content: "";
  opacity: 0;
}
.mat-mdc-fab-base .mdc-button__label,
.mat-mdc-fab-base .mat-icon {
  z-index: 1;
  position: relative;
}
.mat-mdc-fab-base .mat-focus-indicator {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
}
.mat-mdc-fab-base:focus-visible > .mat-focus-indicator::before {
  content: "";
}
.mat-mdc-fab-base._mat-animation-noopable {
  transition: none !important;
  animation: none !important;
}
.mat-mdc-fab-base::before {
  position: absolute;
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  border: 1px solid transparent;
  border-radius: inherit;
  content: "";
  pointer-events: none;
}
.mat-mdc-fab-base[hidden] {
  display: none;
}
.mat-mdc-fab-base::-moz-focus-inner {
  padding: 0;
  border: 0;
}
.mat-mdc-fab-base:active, .mat-mdc-fab-base:focus {
  outline: none;
}
.mat-mdc-fab-base:hover {
  cursor: pointer;
}
.mat-mdc-fab-base > svg {
  width: 100%;
}
.mat-mdc-fab-base .mat-icon, .mat-mdc-fab-base .material-icons {
  transition: transform 180ms 90ms cubic-bezier(0, 0, 0.2, 1);
  fill: currentColor;
  will-change: transform;
}
.mat-mdc-fab-base .mat-focus-indicator::before {
  margin: calc(calc(var(--mat-focus-indicator-border-width, 3px) + 2px) * -1);
}
.mat-mdc-fab-base[disabled], .mat-mdc-fab-base.mat-mdc-button-disabled {
  cursor: default;
  pointer-events: none;
}
.mat-mdc-fab-base[disabled], .mat-mdc-fab-base[disabled]:focus, .mat-mdc-fab-base.mat-mdc-button-disabled, .mat-mdc-fab-base.mat-mdc-button-disabled:focus {
  box-shadow: none;
}
.mat-mdc-fab-base.mat-mdc-button-disabled-interactive {
  pointer-events: auto;
}

.mat-mdc-fab {
  background-color: var(--mat-fab-container-color, var(--mat-sys-primary-container));
  border-radius: var(--mat-fab-container-shape, var(--mat-sys-corner-large));
  color: var(--mat-fab-foreground-color, var(--mat-sys-on-primary-container, inherit));
  box-shadow: var(--mat-fab-container-elevation-shadow, var(--mat-sys-level3));
}
@media (hover: hover) {
  .mat-mdc-fab:hover {
    box-shadow: var(--mat-fab-hover-container-elevation-shadow, var(--mat-sys-level4));
  }
}
.mat-mdc-fab:focus {
  box-shadow: var(--mat-fab-focus-container-elevation-shadow, var(--mat-sys-level3));
}
.mat-mdc-fab:active, .mat-mdc-fab:focus:active {
  box-shadow: var(--mat-fab-pressed-container-elevation-shadow, var(--mat-sys-level3));
}
.mat-mdc-fab[disabled], .mat-mdc-fab.mat-mdc-button-disabled {
  cursor: default;
  pointer-events: none;
  color: var(--mat-fab-disabled-state-foreground-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
  background-color: var(--mat-fab-disabled-state-container-color, color-mix(in srgb, var(--mat-sys-on-surface) 12%, transparent));
}
.mat-mdc-fab.mat-mdc-button-disabled-interactive {
  pointer-events: auto;
}
.mat-mdc-fab .mat-mdc-button-touch-target {
  position: absolute;
  top: 50%;
  height: var(--mat-fab-touch-target-size, 48px);
  display: var(--mat-fab-touch-target-display, block);
  left: 50%;
  width: var(--mat-fab-touch-target-size, 48px);
  transform: translate(-50%, -50%);
}
.mat-mdc-fab .mat-ripple-element {
  background-color: var(--mat-fab-ripple-color, color-mix(in srgb, var(--mat-sys-on-primary-container) calc(var(--mat-sys-pressed-state-layer-opacity) * 100%), transparent));
}
.mat-mdc-fab .mat-mdc-button-persistent-ripple::before {
  background-color: var(--mat-fab-state-layer-color, var(--mat-sys-on-primary-container));
}
.mat-mdc-fab.mat-mdc-button-disabled .mat-mdc-button-persistent-ripple::before {
  background-color: var(--mat-fab-disabled-state-layer-color);
}
.mat-mdc-fab:hover > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-fab-hover-state-layer-opacity, var(--mat-sys-hover-state-layer-opacity));
}
.mat-mdc-fab.cdk-program-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-fab.cdk-keyboard-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-fab.mat-mdc-button-disabled-interactive:focus > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-fab-focus-state-layer-opacity, var(--mat-sys-focus-state-layer-opacity));
}
.mat-mdc-fab:active > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-fab-pressed-state-layer-opacity, var(--mat-sys-pressed-state-layer-opacity));
}

.mat-mdc-mini-fab {
  width: 40px;
  height: 40px;
  background-color: var(--mat-fab-small-container-color, var(--mat-sys-primary-container));
  border-radius: var(--mat-fab-small-container-shape, var(--mat-sys-corner-medium));
  color: var(--mat-fab-small-foreground-color, var(--mat-sys-on-primary-container, inherit));
  box-shadow: var(--mat-fab-small-container-elevation-shadow, var(--mat-sys-level3));
}
@media (hover: hover) {
  .mat-mdc-mini-fab:hover {
    box-shadow: var(--mat-fab-small-hover-container-elevation-shadow, var(--mat-sys-level4));
  }
}
.mat-mdc-mini-fab:focus {
  box-shadow: var(--mat-fab-small-focus-container-elevation-shadow, var(--mat-sys-level3));
}
.mat-mdc-mini-fab:active, .mat-mdc-mini-fab:focus:active {
  box-shadow: var(--mat-fab-small-pressed-container-elevation-shadow, var(--mat-sys-level3));
}
.mat-mdc-mini-fab[disabled], .mat-mdc-mini-fab.mat-mdc-button-disabled {
  cursor: default;
  pointer-events: none;
  color: var(--mat-fab-small-disabled-state-foreground-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
  background-color: var(--mat-fab-small-disabled-state-container-color, color-mix(in srgb, var(--mat-sys-on-surface) 12%, transparent));
}
.mat-mdc-mini-fab.mat-mdc-button-disabled-interactive {
  pointer-events: auto;
}
.mat-mdc-mini-fab .mat-mdc-button-touch-target {
  position: absolute;
  top: 50%;
  height: var(--mat-fab-small-touch-target-size, 48px);
  display: var(--mat-fab-small-touch-target-display);
  left: 50%;
  width: var(--mat-fab-small-touch-target-size, 48px);
  transform: translate(-50%, -50%);
}
.mat-mdc-mini-fab .mat-ripple-element {
  background-color: var(--mat-fab-small-ripple-color, color-mix(in srgb, var(--mat-sys-on-primary-container) calc(var(--mat-sys-pressed-state-layer-opacity) * 100%), transparent));
}
.mat-mdc-mini-fab .mat-mdc-button-persistent-ripple::before {
  background-color: var(--mat-fab-small-state-layer-color, var(--mat-sys-on-primary-container));
}
.mat-mdc-mini-fab.mat-mdc-button-disabled .mat-mdc-button-persistent-ripple::before {
  background-color: var(--mat-fab-small-disabled-state-layer-color);
}
.mat-mdc-mini-fab:hover > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-fab-small-hover-state-layer-opacity, var(--mat-sys-hover-state-layer-opacity));
}
.mat-mdc-mini-fab.cdk-program-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-mini-fab.cdk-keyboard-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-mini-fab.mat-mdc-button-disabled-interactive:focus > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-fab-small-focus-state-layer-opacity, var(--mat-sys-focus-state-layer-opacity));
}
.mat-mdc-mini-fab:active > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-fab-small-pressed-state-layer-opacity, var(--mat-sys-pressed-state-layer-opacity));
}

.mat-mdc-extended-fab {
  -moz-osx-font-smoothing: grayscale;
  -webkit-font-smoothing: antialiased;
  padding-left: 20px;
  padding-right: 20px;
  width: auto;
  max-width: 100%;
  line-height: normal;
  box-shadow: var(--mat-fab-extended-container-elevation-shadow, var(--mat-sys-level3));
  height: var(--mat-fab-extended-container-height, 56px);
  border-radius: var(--mat-fab-extended-container-shape, var(--mat-sys-corner-large));
  font-family: var(--mat-fab-extended-label-text-font, var(--mat-sys-label-large-font));
  font-size: var(--mat-fab-extended-label-text-size, var(--mat-sys-label-large-size));
  font-weight: var(--mat-fab-extended-label-text-weight, var(--mat-sys-label-large-weight));
  letter-spacing: var(--mat-fab-extended-label-text-tracking, var(--mat-sys-label-large-tracking));
}
@media (hover: hover) {
  .mat-mdc-extended-fab:hover {
    box-shadow: var(--mat-fab-extended-hover-container-elevation-shadow, var(--mat-sys-level4));
  }
}
.mat-mdc-extended-fab:focus {
  box-shadow: var(--mat-fab-extended-focus-container-elevation-shadow, var(--mat-sys-level3));
}
.mat-mdc-extended-fab:active, .mat-mdc-extended-fab:focus:active {
  box-shadow: var(--mat-fab-extended-pressed-container-elevation-shadow, var(--mat-sys-level3));
}
.mat-mdc-extended-fab[disabled], .mat-mdc-extended-fab.mat-mdc-button-disabled {
  cursor: default;
  pointer-events: none;
}
.mat-mdc-extended-fab[disabled], .mat-mdc-extended-fab[disabled]:focus, .mat-mdc-extended-fab.mat-mdc-button-disabled, .mat-mdc-extended-fab.mat-mdc-button-disabled:focus {
  box-shadow: none;
}
.mat-mdc-extended-fab.mat-mdc-button-disabled-interactive {
  pointer-events: auto;
}
[dir=rtl] .mat-mdc-extended-fab .mdc-button__label + .mat-icon, [dir=rtl] .mat-mdc-extended-fab .mdc-button__label + .material-icons,
.mat-mdc-extended-fab > .mat-icon,
.mat-mdc-extended-fab > .material-icons {
  margin-left: -8px;
  margin-right: 12px;
}
.mat-mdc-extended-fab .mdc-button__label + .mat-icon,
.mat-mdc-extended-fab .mdc-button__label + .material-icons, [dir=rtl] .mat-mdc-extended-fab > .mat-icon, [dir=rtl] .mat-mdc-extended-fab > .material-icons {
  margin-left: 12px;
  margin-right: -8px;
}
.mat-mdc-extended-fab .mat-mdc-button-touch-target {
  width: 100%;
}
`,Rc=new Map([["text",["mat-mdc-button"]],["filled",["mdc-button--unelevated","mat-mdc-unelevated-button"]],["elevated",["mdc-button--raised","mat-mdc-raised-button"]],["outlined",["mdc-button--outlined","mat-mdc-outlined-button"]],["tonal",["mat-tonal-button"]]]),vs=(()=>{class i extends Ia{get appearance(){return this._appearance}set appearance(e){this.setAppearance(e||this._config?.defaultAppearance||"text")}_appearance=null;constructor(){super();let e=wf(this._elementRef.nativeElement);e&&this.setAppearance(e)}setAppearance(e){if(e===this._appearance)return;let t=this._elementRef.nativeElement.classList,r=this._appearance?Rc.get(this._appearance):null,a=Rc.get(e);r&&t.remove(...r),t.add(...a),this._appearance=e}static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["button","matButton",""],["a","matButton",""],["button","mat-button",""],["button","mat-raised-button",""],["button","mat-flat-button",""],["button","mat-stroked-button",""],["a","mat-button",""],["a","mat-raised-button",""],["a","mat-flat-button",""],["a","mat-stroked-button",""]],hostAttrs:[1,"mdc-button"],inputs:{appearance:[0,"matButton","appearance"]},exportAs:["matButton","matAnchor"],features:[te],attrs:xf,ngContentSelectors:Nc,decls:7,vars:4,consts:[[1,"mat-mdc-button-persistent-ripple"],[1,"mdc-button__label"],[1,"mat-focus-indicator"],[1,"mat-mdc-button-touch-target"]],template:function(t,r){t&1&&(Ee(Vc),Pe(0,"span",0),Z(1),le(2,"span",1),Z(3,1),he(),Z(4,2),Pe(5,"span",2)(6,"span",3)),t&2&&w("mdc-button__ripple",!r._isFab)("mdc-fab__ripple",r._isFab)},styles:[`.mat-mdc-button-base {
  text-decoration: none;
}
.mat-mdc-button-base .mat-icon {
  min-height: fit-content;
  flex-shrink: 0;
}
@media (hover: none) {
  .mat-mdc-button-base:hover > span.mat-mdc-button-persistent-ripple::before {
    opacity: 0;
  }
}

.mdc-button {
  -webkit-user-select: none;
  user-select: none;
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  min-width: 64px;
  border: none;
  outline: none;
  line-height: inherit;
  -webkit-appearance: none;
  overflow: visible;
  vertical-align: middle;
  background: transparent;
  padding: 0 8px;
}
.mdc-button::-moz-focus-inner {
  padding: 0;
  border: 0;
}
.mdc-button:active {
  outline: none;
}
.mdc-button:hover {
  cursor: pointer;
}
.mdc-button:disabled {
  cursor: default;
  pointer-events: none;
}
.mdc-button[hidden] {
  display: none;
}
.mdc-button .mdc-button__label {
  position: relative;
}

.mat-mdc-button {
  padding: 0 var(--mat-button-text-horizontal-padding, 12px);
  height: var(--mat-button-text-container-height, 40px);
  font-family: var(--mat-button-text-label-text-font, var(--mat-sys-label-large-font));
  font-size: var(--mat-button-text-label-text-size, var(--mat-sys-label-large-size));
  letter-spacing: var(--mat-button-text-label-text-tracking, var(--mat-sys-label-large-tracking));
  text-transform: var(--mat-button-text-label-text-transform);
  font-weight: var(--mat-button-text-label-text-weight, var(--mat-sys-label-large-weight));
}
.mat-mdc-button, .mat-mdc-button .mdc-button__ripple {
  border-radius: var(--mat-button-text-container-shape, var(--mat-sys-corner-full));
}
.mat-mdc-button:not(:disabled) {
  color: var(--mat-button-text-label-text-color, var(--mat-sys-primary));
}
.mat-mdc-button[disabled], .mat-mdc-button.mat-mdc-button-disabled {
  cursor: default;
  pointer-events: none;
  color: var(--mat-button-text-disabled-label-text-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}
.mat-mdc-button.mat-mdc-button-disabled-interactive {
  pointer-events: auto;
}
.mat-mdc-button:has(.material-icons, mat-icon, [matButtonIcon]) {
  padding: 0 var(--mat-button-text-with-icon-horizontal-padding, 16px);
}
.mat-mdc-button > .mat-icon {
  margin-right: var(--mat-button-text-icon-spacing, 8px);
  margin-left: var(--mat-button-text-icon-offset, -4px);
}
[dir=rtl] .mat-mdc-button > .mat-icon {
  margin-right: var(--mat-button-text-icon-offset, -4px);
  margin-left: var(--mat-button-text-icon-spacing, 8px);
}
.mat-mdc-button .mdc-button__label + .mat-icon {
  margin-right: var(--mat-button-text-icon-offset, -4px);
  margin-left: var(--mat-button-text-icon-spacing, 8px);
}
[dir=rtl] .mat-mdc-button .mdc-button__label + .mat-icon {
  margin-right: var(--mat-button-text-icon-spacing, 8px);
  margin-left: var(--mat-button-text-icon-offset, -4px);
}
.mat-mdc-button .mat-ripple-element {
  background-color: var(--mat-button-text-ripple-color, color-mix(in srgb, var(--mat-sys-primary) calc(var(--mat-sys-pressed-state-layer-opacity) * 100%), transparent));
}
.mat-mdc-button .mat-mdc-button-persistent-ripple::before {
  background-color: var(--mat-button-text-state-layer-color, var(--mat-sys-primary));
}
.mat-mdc-button.mat-mdc-button-disabled .mat-mdc-button-persistent-ripple::before {
  background-color: var(--mat-button-text-disabled-state-layer-color, var(--mat-sys-on-surface-variant));
}
.mat-mdc-button:hover > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-button-text-hover-state-layer-opacity, var(--mat-sys-hover-state-layer-opacity));
}
.mat-mdc-button.cdk-program-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-button.cdk-keyboard-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-button.mat-mdc-button-disabled-interactive:focus > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-button-text-focus-state-layer-opacity, var(--mat-sys-focus-state-layer-opacity));
}
.mat-mdc-button:active > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-button-text-pressed-state-layer-opacity, var(--mat-sys-pressed-state-layer-opacity));
}
.mat-mdc-button .mat-mdc-button-touch-target {
  position: absolute;
  top: 50%;
  height: var(--mat-button-text-touch-target-size, 48px);
  display: var(--mat-button-text-touch-target-display, block);
  left: 0;
  right: 0;
  transform: translateY(-50%);
}

.mat-mdc-unelevated-button {
  transition: box-shadow 280ms cubic-bezier(0.4, 0, 0.2, 1);
  height: var(--mat-button-filled-container-height, 40px);
  font-family: var(--mat-button-filled-label-text-font, var(--mat-sys-label-large-font));
  font-size: var(--mat-button-filled-label-text-size, var(--mat-sys-label-large-size));
  letter-spacing: var(--mat-button-filled-label-text-tracking, var(--mat-sys-label-large-tracking));
  text-transform: var(--mat-button-filled-label-text-transform);
  font-weight: var(--mat-button-filled-label-text-weight, var(--mat-sys-label-large-weight));
  padding: 0 var(--mat-button-filled-horizontal-padding, 24px);
}
.mat-mdc-unelevated-button > .mat-icon {
  margin-right: var(--mat-button-filled-icon-spacing, 8px);
  margin-left: var(--mat-button-filled-icon-offset, -8px);
}
[dir=rtl] .mat-mdc-unelevated-button > .mat-icon {
  margin-right: var(--mat-button-filled-icon-offset, -8px);
  margin-left: var(--mat-button-filled-icon-spacing, 8px);
}
.mat-mdc-unelevated-button .mdc-button__label + .mat-icon {
  margin-right: var(--mat-button-filled-icon-offset, -8px);
  margin-left: var(--mat-button-filled-icon-spacing, 8px);
}
[dir=rtl] .mat-mdc-unelevated-button .mdc-button__label + .mat-icon {
  margin-right: var(--mat-button-filled-icon-spacing, 8px);
  margin-left: var(--mat-button-filled-icon-offset, -8px);
}
.mat-mdc-unelevated-button .mat-ripple-element {
  background-color: var(--mat-button-filled-ripple-color, color-mix(in srgb, var(--mat-sys-on-primary) calc(var(--mat-sys-pressed-state-layer-opacity) * 100%), transparent));
}
.mat-mdc-unelevated-button .mat-mdc-button-persistent-ripple::before {
  background-color: var(--mat-button-filled-state-layer-color, var(--mat-sys-on-primary));
}
.mat-mdc-unelevated-button.mat-mdc-button-disabled .mat-mdc-button-persistent-ripple::before {
  background-color: var(--mat-button-filled-disabled-state-layer-color, var(--mat-sys-on-surface-variant));
}
.mat-mdc-unelevated-button:hover > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-button-filled-hover-state-layer-opacity, var(--mat-sys-hover-state-layer-opacity));
}
.mat-mdc-unelevated-button.cdk-program-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-unelevated-button.cdk-keyboard-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-unelevated-button.mat-mdc-button-disabled-interactive:focus > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-button-filled-focus-state-layer-opacity, var(--mat-sys-focus-state-layer-opacity));
}
.mat-mdc-unelevated-button:active > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-button-filled-pressed-state-layer-opacity, var(--mat-sys-pressed-state-layer-opacity));
}
.mat-mdc-unelevated-button .mat-mdc-button-touch-target {
  position: absolute;
  top: 50%;
  height: var(--mat-button-filled-touch-target-size, 48px);
  display: var(--mat-button-filled-touch-target-display, block);
  left: 0;
  right: 0;
  transform: translateY(-50%);
}
.mat-mdc-unelevated-button:not(:disabled) {
  color: var(--mat-button-filled-label-text-color, var(--mat-sys-on-primary));
  background-color: var(--mat-button-filled-container-color, var(--mat-sys-primary));
}
.mat-mdc-unelevated-button, .mat-mdc-unelevated-button .mdc-button__ripple {
  border-radius: var(--mat-button-filled-container-shape, var(--mat-sys-corner-full));
}
.mat-mdc-unelevated-button[disabled], .mat-mdc-unelevated-button.mat-mdc-button-disabled {
  cursor: default;
  pointer-events: none;
  color: var(--mat-button-filled-disabled-label-text-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
  background-color: var(--mat-button-filled-disabled-container-color, color-mix(in srgb, var(--mat-sys-on-surface) 12%, transparent));
}
.mat-mdc-unelevated-button.mat-mdc-button-disabled-interactive {
  pointer-events: auto;
}

.mat-mdc-raised-button {
  transition: box-shadow 280ms cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--mat-button-protected-container-elevation-shadow, var(--mat-sys-level1));
  height: var(--mat-button-protected-container-height, 40px);
  font-family: var(--mat-button-protected-label-text-font, var(--mat-sys-label-large-font));
  font-size: var(--mat-button-protected-label-text-size, var(--mat-sys-label-large-size));
  letter-spacing: var(--mat-button-protected-label-text-tracking, var(--mat-sys-label-large-tracking));
  text-transform: var(--mat-button-protected-label-text-transform);
  font-weight: var(--mat-button-protected-label-text-weight, var(--mat-sys-label-large-weight));
  padding: 0 var(--mat-button-protected-horizontal-padding, 24px);
}
.mat-mdc-raised-button > .mat-icon {
  margin-right: var(--mat-button-protected-icon-spacing, 8px);
  margin-left: var(--mat-button-protected-icon-offset, -8px);
}
[dir=rtl] .mat-mdc-raised-button > .mat-icon {
  margin-right: var(--mat-button-protected-icon-offset, -8px);
  margin-left: var(--mat-button-protected-icon-spacing, 8px);
}
.mat-mdc-raised-button .mdc-button__label + .mat-icon {
  margin-right: var(--mat-button-protected-icon-offset, -8px);
  margin-left: var(--mat-button-protected-icon-spacing, 8px);
}
[dir=rtl] .mat-mdc-raised-button .mdc-button__label + .mat-icon {
  margin-right: var(--mat-button-protected-icon-spacing, 8px);
  margin-left: var(--mat-button-protected-icon-offset, -8px);
}
.mat-mdc-raised-button .mat-ripple-element {
  background-color: var(--mat-button-protected-ripple-color, color-mix(in srgb, var(--mat-sys-primary) calc(var(--mat-sys-pressed-state-layer-opacity) * 100%), transparent));
}
.mat-mdc-raised-button .mat-mdc-button-persistent-ripple::before {
  background-color: var(--mat-button-protected-state-layer-color, var(--mat-sys-primary));
}
.mat-mdc-raised-button.mat-mdc-button-disabled .mat-mdc-button-persistent-ripple::before {
  background-color: var(--mat-button-protected-disabled-state-layer-color, var(--mat-sys-on-surface-variant));
}
.mat-mdc-raised-button:hover > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-button-protected-hover-state-layer-opacity, var(--mat-sys-hover-state-layer-opacity));
}
.mat-mdc-raised-button.cdk-program-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-raised-button.cdk-keyboard-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-raised-button.mat-mdc-button-disabled-interactive:focus > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-button-protected-focus-state-layer-opacity, var(--mat-sys-focus-state-layer-opacity));
}
.mat-mdc-raised-button:active > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-button-protected-pressed-state-layer-opacity, var(--mat-sys-pressed-state-layer-opacity));
}
.mat-mdc-raised-button .mat-mdc-button-touch-target {
  position: absolute;
  top: 50%;
  height: var(--mat-button-protected-touch-target-size, 48px);
  display: var(--mat-button-protected-touch-target-display, block);
  left: 0;
  right: 0;
  transform: translateY(-50%);
}
.mat-mdc-raised-button:not(:disabled) {
  color: var(--mat-button-protected-label-text-color, var(--mat-sys-primary));
  background-color: var(--mat-button-protected-container-color, var(--mat-sys-surface));
}
.mat-mdc-raised-button, .mat-mdc-raised-button .mdc-button__ripple {
  border-radius: var(--mat-button-protected-container-shape, var(--mat-sys-corner-full));
}
@media (hover: hover) {
  .mat-mdc-raised-button:hover {
    box-shadow: var(--mat-button-protected-hover-container-elevation-shadow, var(--mat-sys-level2));
  }
}
.mat-mdc-raised-button:focus {
  box-shadow: var(--mat-button-protected-focus-container-elevation-shadow, var(--mat-sys-level1));
}
.mat-mdc-raised-button:active, .mat-mdc-raised-button:focus:active {
  box-shadow: var(--mat-button-protected-pressed-container-elevation-shadow, var(--mat-sys-level1));
}
.mat-mdc-raised-button[disabled], .mat-mdc-raised-button.mat-mdc-button-disabled {
  cursor: default;
  pointer-events: none;
  color: var(--mat-button-protected-disabled-label-text-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
  background-color: var(--mat-button-protected-disabled-container-color, color-mix(in srgb, var(--mat-sys-on-surface) 12%, transparent));
}
.mat-mdc-raised-button[disabled].mat-mdc-button-disabled, .mat-mdc-raised-button.mat-mdc-button-disabled.mat-mdc-button-disabled {
  box-shadow: var(--mat-button-protected-disabled-container-elevation-shadow, var(--mat-sys-level0));
}
.mat-mdc-raised-button.mat-mdc-button-disabled-interactive {
  pointer-events: auto;
}

.mat-mdc-outlined-button {
  border-style: solid;
  transition: border 280ms cubic-bezier(0.4, 0, 0.2, 1);
  height: var(--mat-button-outlined-container-height, 40px);
  font-family: var(--mat-button-outlined-label-text-font, var(--mat-sys-label-large-font));
  font-size: var(--mat-button-outlined-label-text-size, var(--mat-sys-label-large-size));
  letter-spacing: var(--mat-button-outlined-label-text-tracking, var(--mat-sys-label-large-tracking));
  text-transform: var(--mat-button-outlined-label-text-transform);
  font-weight: var(--mat-button-outlined-label-text-weight, var(--mat-sys-label-large-weight));
  border-radius: var(--mat-button-outlined-container-shape, var(--mat-sys-corner-full));
  border-width: var(--mat-button-outlined-outline-width, 1px);
  padding: 0 var(--mat-button-outlined-horizontal-padding, 24px);
}
.mat-mdc-outlined-button > .mat-icon {
  margin-right: var(--mat-button-outlined-icon-spacing, 8px);
  margin-left: var(--mat-button-outlined-icon-offset, -8px);
}
[dir=rtl] .mat-mdc-outlined-button > .mat-icon {
  margin-right: var(--mat-button-outlined-icon-offset, -8px);
  margin-left: var(--mat-button-outlined-icon-spacing, 8px);
}
.mat-mdc-outlined-button .mdc-button__label + .mat-icon {
  margin-right: var(--mat-button-outlined-icon-offset, -8px);
  margin-left: var(--mat-button-outlined-icon-spacing, 8px);
}
[dir=rtl] .mat-mdc-outlined-button .mdc-button__label + .mat-icon {
  margin-right: var(--mat-button-outlined-icon-spacing, 8px);
  margin-left: var(--mat-button-outlined-icon-offset, -8px);
}
.mat-mdc-outlined-button .mat-ripple-element {
  background-color: var(--mat-button-outlined-ripple-color, color-mix(in srgb, var(--mat-sys-primary) calc(var(--mat-sys-pressed-state-layer-opacity) * 100%), transparent));
}
.mat-mdc-outlined-button .mat-mdc-button-persistent-ripple::before {
  background-color: var(--mat-button-outlined-state-layer-color, var(--mat-sys-primary));
}
.mat-mdc-outlined-button.mat-mdc-button-disabled .mat-mdc-button-persistent-ripple::before {
  background-color: var(--mat-button-outlined-disabled-state-layer-color, var(--mat-sys-on-surface-variant));
}
.mat-mdc-outlined-button:hover > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-button-outlined-hover-state-layer-opacity, var(--mat-sys-hover-state-layer-opacity));
}
.mat-mdc-outlined-button.cdk-program-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-outlined-button.cdk-keyboard-focused > .mat-mdc-button-persistent-ripple::before, .mat-mdc-outlined-button.mat-mdc-button-disabled-interactive:focus > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-button-outlined-focus-state-layer-opacity, var(--mat-sys-focus-state-layer-opacity));
}
.mat-mdc-outlined-button:active > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-button-outlined-pressed-state-layer-opacity, var(--mat-sys-pressed-state-layer-opacity));
}
.mat-mdc-outlined-button .mat-mdc-button-touch-target {
  position: absolute;
  top: 50%;
  height: var(--mat-button-outlined-touch-target-size, 48px);
  display: var(--mat-button-outlined-touch-target-display, block);
  left: 0;
  right: 0;
  transform: translateY(-50%);
}
.mat-mdc-outlined-button:not(:disabled) {
  color: var(--mat-button-outlined-label-text-color, var(--mat-sys-primary));
  border-color: var(--mat-button-outlined-outline-color, var(--mat-sys-outline));
}
.mat-mdc-outlined-button[disabled], .mat-mdc-outlined-button.mat-mdc-button-disabled {
  cursor: default;
  pointer-events: none;
  color: var(--mat-button-outlined-disabled-label-text-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
  border-color: var(--mat-button-outlined-disabled-outline-color, color-mix(in srgb, var(--mat-sys-on-surface) 12%, transparent));
}
.mat-mdc-outlined-button.mat-mdc-button-disabled-interactive {
  pointer-events: auto;
}

.mat-tonal-button {
  transition: box-shadow 280ms cubic-bezier(0.4, 0, 0.2, 1);
  height: var(--mat-button-tonal-container-height, 40px);
  font-family: var(--mat-button-tonal-label-text-font, var(--mat-sys-label-large-font));
  font-size: var(--mat-button-tonal-label-text-size, var(--mat-sys-label-large-size));
  letter-spacing: var(--mat-button-tonal-label-text-tracking, var(--mat-sys-label-large-tracking));
  text-transform: var(--mat-button-tonal-label-text-transform);
  font-weight: var(--mat-button-tonal-label-text-weight, var(--mat-sys-label-large-weight));
  padding: 0 var(--mat-button-tonal-horizontal-padding, 24px);
}
.mat-tonal-button:not(:disabled) {
  color: var(--mat-button-tonal-label-text-color, var(--mat-sys-on-secondary-container));
  background-color: var(--mat-button-tonal-container-color, var(--mat-sys-secondary-container));
}
.mat-tonal-button, .mat-tonal-button .mdc-button__ripple {
  border-radius: var(--mat-button-tonal-container-shape, var(--mat-sys-corner-full));
}
.mat-tonal-button[disabled], .mat-tonal-button.mat-mdc-button-disabled {
  cursor: default;
  pointer-events: none;
  color: var(--mat-button-tonal-disabled-label-text-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
  background-color: var(--mat-button-tonal-disabled-container-color, color-mix(in srgb, var(--mat-sys-on-surface) 12%, transparent));
}
.mat-tonal-button.mat-mdc-button-disabled-interactive {
  pointer-events: auto;
}
.mat-tonal-button > .mat-icon {
  margin-right: var(--mat-button-tonal-icon-spacing, 8px);
  margin-left: var(--mat-button-tonal-icon-offset, -8px);
}
[dir=rtl] .mat-tonal-button > .mat-icon {
  margin-right: var(--mat-button-tonal-icon-offset, -8px);
  margin-left: var(--mat-button-tonal-icon-spacing, 8px);
}
.mat-tonal-button .mdc-button__label + .mat-icon {
  margin-right: var(--mat-button-tonal-icon-offset, -8px);
  margin-left: var(--mat-button-tonal-icon-spacing, 8px);
}
[dir=rtl] .mat-tonal-button .mdc-button__label + .mat-icon {
  margin-right: var(--mat-button-tonal-icon-spacing, 8px);
  margin-left: var(--mat-button-tonal-icon-offset, -8px);
}
.mat-tonal-button .mat-ripple-element {
  background-color: var(--mat-button-tonal-ripple-color, color-mix(in srgb, var(--mat-sys-on-secondary-container) calc(var(--mat-sys-pressed-state-layer-opacity) * 100%), transparent));
}
.mat-tonal-button .mat-mdc-button-persistent-ripple::before {
  background-color: var(--mat-button-tonal-state-layer-color, var(--mat-sys-on-secondary-container));
}
.mat-tonal-button.mat-mdc-button-disabled .mat-mdc-button-persistent-ripple::before {
  background-color: var(--mat-button-tonal-disabled-state-layer-color, var(--mat-sys-on-surface-variant));
}
.mat-tonal-button:hover > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-button-tonal-hover-state-layer-opacity, var(--mat-sys-hover-state-layer-opacity));
}
.mat-tonal-button.cdk-program-focused > .mat-mdc-button-persistent-ripple::before, .mat-tonal-button.cdk-keyboard-focused > .mat-mdc-button-persistent-ripple::before, .mat-tonal-button.mat-mdc-button-disabled-interactive:focus > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-button-tonal-focus-state-layer-opacity, var(--mat-sys-focus-state-layer-opacity));
}
.mat-tonal-button:active > .mat-mdc-button-persistent-ripple::before {
  opacity: var(--mat-button-tonal-pressed-state-layer-opacity, var(--mat-sys-pressed-state-layer-opacity));
}
.mat-tonal-button .mat-mdc-button-touch-target {
  position: absolute;
  top: 50%;
  height: var(--mat-button-tonal-touch-target-size, 48px);
  display: var(--mat-button-tonal-touch-target-display, block);
  left: 0;
  right: 0;
  transform: translateY(-50%);
}

.mat-mdc-button,
.mat-mdc-unelevated-button,
.mat-mdc-raised-button,
.mat-mdc-outlined-button,
.mat-tonal-button {
  -webkit-tap-highlight-color: transparent;
}
.mat-mdc-button .mat-mdc-button-ripple,
.mat-mdc-button .mat-mdc-button-persistent-ripple,
.mat-mdc-button .mat-mdc-button-persistent-ripple::before,
.mat-mdc-unelevated-button .mat-mdc-button-ripple,
.mat-mdc-unelevated-button .mat-mdc-button-persistent-ripple,
.mat-mdc-unelevated-button .mat-mdc-button-persistent-ripple::before,
.mat-mdc-raised-button .mat-mdc-button-ripple,
.mat-mdc-raised-button .mat-mdc-button-persistent-ripple,
.mat-mdc-raised-button .mat-mdc-button-persistent-ripple::before,
.mat-mdc-outlined-button .mat-mdc-button-ripple,
.mat-mdc-outlined-button .mat-mdc-button-persistent-ripple,
.mat-mdc-outlined-button .mat-mdc-button-persistent-ripple::before,
.mat-tonal-button .mat-mdc-button-ripple,
.mat-tonal-button .mat-mdc-button-persistent-ripple,
.mat-tonal-button .mat-mdc-button-persistent-ripple::before {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  pointer-events: none;
  border-radius: inherit;
}
.mat-mdc-button .mat-mdc-button-ripple,
.mat-mdc-unelevated-button .mat-mdc-button-ripple,
.mat-mdc-raised-button .mat-mdc-button-ripple,
.mat-mdc-outlined-button .mat-mdc-button-ripple,
.mat-tonal-button .mat-mdc-button-ripple {
  overflow: hidden;
}
.mat-mdc-button .mat-mdc-button-persistent-ripple::before,
.mat-mdc-unelevated-button .mat-mdc-button-persistent-ripple::before,
.mat-mdc-raised-button .mat-mdc-button-persistent-ripple::before,
.mat-mdc-outlined-button .mat-mdc-button-persistent-ripple::before,
.mat-tonal-button .mat-mdc-button-persistent-ripple::before {
  content: "";
  opacity: 0;
}
.mat-mdc-button .mdc-button__label,
.mat-mdc-button .mat-icon,
.mat-mdc-unelevated-button .mdc-button__label,
.mat-mdc-unelevated-button .mat-icon,
.mat-mdc-raised-button .mdc-button__label,
.mat-mdc-raised-button .mat-icon,
.mat-mdc-outlined-button .mdc-button__label,
.mat-mdc-outlined-button .mat-icon,
.mat-tonal-button .mdc-button__label,
.mat-tonal-button .mat-icon {
  z-index: 1;
  position: relative;
}
.mat-mdc-button .mat-focus-indicator,
.mat-mdc-unelevated-button .mat-focus-indicator,
.mat-mdc-raised-button .mat-focus-indicator,
.mat-mdc-outlined-button .mat-focus-indicator,
.mat-tonal-button .mat-focus-indicator {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  border-radius: inherit;
}
.mat-mdc-button:focus-visible > .mat-focus-indicator::before,
.mat-mdc-unelevated-button:focus-visible > .mat-focus-indicator::before,
.mat-mdc-raised-button:focus-visible > .mat-focus-indicator::before,
.mat-mdc-outlined-button:focus-visible > .mat-focus-indicator::before,
.mat-tonal-button:focus-visible > .mat-focus-indicator::before {
  content: "";
  border-radius: inherit;
}
.mat-mdc-button._mat-animation-noopable,
.mat-mdc-unelevated-button._mat-animation-noopable,
.mat-mdc-raised-button._mat-animation-noopable,
.mat-mdc-outlined-button._mat-animation-noopable,
.mat-tonal-button._mat-animation-noopable {
  transition: none !important;
  animation: none !important;
}
.mat-mdc-button > .mat-icon,
.mat-mdc-unelevated-button > .mat-icon,
.mat-mdc-raised-button > .mat-icon,
.mat-mdc-outlined-button > .mat-icon,
.mat-tonal-button > .mat-icon {
  display: inline-block;
  position: relative;
  vertical-align: top;
  font-size: 1.125rem;
  height: 1.125rem;
  width: 1.125rem;
}

.mat-mdc-outlined-button .mat-mdc-button-ripple,
.mat-mdc-outlined-button .mdc-button__ripple {
  top: -1px;
  left: -1px;
  bottom: -1px;
  right: -1px;
}

.mat-mdc-unelevated-button .mat-focus-indicator::before,
.mat-tonal-button .mat-focus-indicator::before,
.mat-mdc-raised-button .mat-focus-indicator::before {
  margin: calc(calc(var(--mat-focus-indicator-border-width, 3px) + 2px) * -1);
}

.mat-mdc-outlined-button .mat-focus-indicator::before {
  margin: calc(calc(var(--mat-focus-indicator-border-width, 3px) + 3px) * -1);
}
`,`@media (forced-colors: active) {
  .mat-mdc-button:not(.mdc-button--outlined),
  .mat-mdc-unelevated-button:not(.mdc-button--outlined),
  .mat-mdc-raised-button:not(.mdc-button--outlined),
  .mat-mdc-outlined-button:not(.mdc-button--outlined),
  .mat-mdc-button-base.mat-tonal-button,
  .mat-mdc-icon-button.mat-mdc-icon-button,
  .mat-mdc-outlined-button .mdc-button__ripple {
    outline: solid 1px;
  }
}
`],encapsulation:2,changeDetection:0})}return i})();function wf(i){return i.hasAttribute("mat-raised-button")?"elevated":i.hasAttribute("mat-stroked-button")?"outlined":i.hasAttribute("mat-flat-button")?"filled":i.hasAttribute("mat-button")?"text":null}var Ef=new M("mat-mdc-fab-default-options",{providedIn:"root",factory:()=>bs}),bs={color:"accent"};var aw=(()=>{class i extends Ia{_options=l(Ef,{optional:!0});_isFab=!0;constructor(){super(),this._options=this._options||bs,this.color=this._options.color||bs.color}static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["button","mat-mini-fab",""],["a","mat-mini-fab",""],["button","matMiniFab",""],["a","matMiniFab",""]],hostAttrs:[1,"mdc-fab","mat-mdc-fab-base","mdc-fab--mini","mat-mdc-mini-fab"],exportAs:["matButton","matAnchor"],features:[te],attrs:Df,ngContentSelectors:Nc,decls:7,vars:4,consts:[[1,"mat-mdc-button-persistent-ripple"],[1,"mdc-button__label"],[1,"mat-focus-indicator"],[1,"mat-mdc-button-touch-target"]],template:function(t,r){t&1&&(Ee(Vc),Pe(0,"span",0),Z(1),le(2,"span",1),Z(3,1),he(),Z(4,2),Pe(5,"span",2)(6,"span",3)),t&2&&w("mdc-button__ripple",!r._isFab)("mdc-fab__ripple",r._isFab)},styles:[Mf],encapsulation:2,changeDetection:0})}return i})();var Ta=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275mod=$({type:i});static \u0275inj=G({imports:[hi,fe]})}return i})();var Af=["mat-menu-item",""],Sf=[[["mat-icon"],["","matMenuItemIcon",""]],"*"],If=["mat-icon, [matMenuItemIcon]","*"];function Of(i,n){i&1&&(_t(),p(0,"svg",2),_(1,"polygon",3),h())}var Tf=["*"];function Ff(i,n){if(i&1){let e=ze();le(0,"div",0),ti("click",function(){J(e);let r=m();return ee(r.closed.emit("click"))})("animationstart",function(r){J(e);let a=m();return ee(a._onAnimationStart(r.animationName))})("animationend",function(r){J(e);let a=m();return ee(a._onAnimationDone(r.animationName))})("animationcancel",function(r){J(e);let a=m();return ee(a._onAnimationDone(r.animationName))}),le(1,"div",1),Z(2),he()()}if(i&2){let e=m();Ae(e._classList),w("mat-menu-panel-animations-disabled",e._animationsDisabled)("mat-menu-panel-exit-animation",e._panelAnimationState==="void")("mat-menu-panel-animating",e._isAnimating()),He("id",e.panelId),B("aria-label",e.ariaLabel||null)("aria-labelledby",e.ariaLabelledby||null)("aria-describedby",e.ariaDescribedby||null)}}var Cs=new M("MAT_MENU_PANEL"),jn=(()=>{class i{_elementRef=l(N);_document=l(_e);_focusMonitor=l(It);_parentMenu=l(Cs,{optional:!0});_changeDetectorRef=l(ne);role="menuitem";disabled=!1;disableRipple=!1;_hovered=new F;_focused=new F;_highlighted=!1;_triggersSubmenu=!1;constructor(){l(Ue).load(_n),this._parentMenu?.addItem?.(this)}focus(e,t){this._focusMonitor&&e?this._focusMonitor.focusVia(this._getHostElement(),e,t):this._getHostElement().focus(t),this._focused.next(this)}ngAfterViewInit(){this._focusMonitor&&this._focusMonitor.monitor(this._elementRef,!1)}ngOnDestroy(){this._focusMonitor&&this._focusMonitor.stopMonitoring(this._elementRef),this._parentMenu&&this._parentMenu.removeItem&&this._parentMenu.removeItem(this),this._hovered.complete(),this._focused.complete()}_getTabIndex(){return this.disabled?"-1":"0"}_getHostElement(){return this._elementRef.nativeElement}_checkDisabled(e){this.disabled&&(e.preventDefault(),e.stopPropagation())}_handleMouseEnter(){this._hovered.next(this)}getLabel(){let e=this._elementRef.nativeElement.cloneNode(!0),t=e.querySelectorAll("mat-icon, .material-icons");for(let r=0;r<t.length;r++)t[r].remove();return e.textContent?.trim()||""}_setHighlighted(e){this._highlighted=e,this._changeDetectorRef.markForCheck()}_setTriggersSubmenu(e){this._triggersSubmenu=e,this._changeDetectorRef.markForCheck()}_hasFocus(){return this._document&&this._document.activeElement===this._getHostElement()}static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["","mat-menu-item",""]],hostAttrs:[1,"mat-mdc-menu-item","mat-focus-indicator"],hostVars:8,hostBindings:function(t,r){t&1&&T("click",function(o){return r._checkDisabled(o)})("mouseenter",function(){return r._handleMouseEnter()}),t&2&&(B("role",r.role)("tabindex",r._getTabIndex())("aria-disabled",r.disabled)("disabled",r.disabled||null),w("mat-mdc-menu-item-highlighted",r._highlighted)("mat-mdc-menu-item-submenu-trigger",r._triggersSubmenu))},inputs:{role:"role",disabled:[2,"disabled","disabled",Y],disableRipple:[2,"disableRipple","disableRipple",Y]},exportAs:["matMenuItem"],attrs:Af,ngContentSelectors:If,decls:5,vars:3,consts:[[1,"mat-mdc-menu-item-text"],["matRipple","",1,"mat-mdc-menu-ripple",3,"matRippleDisabled","matRippleTrigger"],["viewBox","0 0 5 10","focusable","false","aria-hidden","true",1,"mat-mdc-menu-submenu-icon"],["points","0,0 5,5 0,10"]],template:function(t,r){t&1&&(Ee(Sf),Z(0),p(1,"span",0),Z(2,1),h(),_(3,"div",1),v(4,Of,2,0,":svg:svg",2)),t&2&&(c(3),f("matRippleDisabled",r.disableRipple||r.disabled)("matRippleTrigger",r._getHostElement()),c(),y(r._triggersSubmenu?4:-1))},dependencies:[Aa],encapsulation:2,changeDetection:0})}return i})();var Pf=new M("MatMenuContent");var Rf=new M("mat-menu-default-options",{providedIn:"root",factory:()=>({overlapTrigger:!1,xPosition:"after",yPosition:"below",backdropClass:"cdk-overlay-transparent-backdrop"})}),ys="_mat-menu-enter",Fa="_mat-menu-exit",gi=(()=>{class i{_elementRef=l(N);_changeDetectorRef=l(ne);_injector=l(U);_keyManager;_xPosition;_yPosition;_firstItemFocusRef;_exitFallbackTimeout;_animationsDisabled=ye();_allItems;_directDescendantItems=new On;_classList={};_panelAnimationState="void";_animationDone=new F;_isAnimating=V(!1);parentMenu;direction;overlayPanelClass;backdropClass;ariaLabel;ariaLabelledby;ariaDescribedby;get xPosition(){return this._xPosition}set xPosition(e){this._xPosition=e,this.setPositionClasses()}get yPosition(){return this._yPosition}set yPosition(e){this._yPosition=e,this.setPositionClasses()}templateRef;items;lazyContent;overlapTrigger=!1;hasBackdrop;set panelClass(e){let t=this._previousPanelClass,r=g({},this._classList);t&&t.length&&t.split(" ").forEach(a=>{r[a]=!1}),this._previousPanelClass=e,e&&e.length&&(e.split(" ").forEach(a=>{r[a]=!0}),this._elementRef.nativeElement.className=""),this._classList=r}_previousPanelClass;get classList(){return this.panelClass}set classList(e){this.panelClass=e}closed=new S;close=this.closed;panelId=l(Re).getId("mat-menu-panel-");constructor(){let e=l(Rf);this.overlayPanelClass=e.overlayPanelClass||"",this._xPosition=e.xPosition,this._yPosition=e.yPosition,this.backdropClass=e.backdropClass,this.overlapTrigger=e.overlapTrigger,this.hasBackdrop=e.hasBackdrop}ngOnInit(){this.setPositionClasses()}ngAfterContentInit(){this._updateDirectDescendants(),this._keyManager=new mr(this._directDescendantItems).withWrap().withTypeAhead().withHomeAndEnd(),this._keyManager.tabOut.subscribe(()=>this.closed.emit("tab")),this._directDescendantItems.changes.pipe(Le(this._directDescendantItems),an(e=>$e(...e.map(t=>t._focused)))).subscribe(e=>this._keyManager.updateActiveItem(e)),this._directDescendantItems.changes.subscribe(e=>{let t=this._keyManager;if(this._panelAnimationState==="enter"&&t.activeItem?._hasFocus()){let r=e.toArray(),a=Math.max(0,Math.min(r.length-1,t.activeItemIndex||0));r[a]&&!r[a].disabled?t.setActiveItem(a):t.setNextItemActive()}})}ngOnDestroy(){this._keyManager?.destroy(),this._directDescendantItems.destroy(),this.closed.complete(),this._firstItemFocusRef?.destroy(),clearTimeout(this._exitFallbackTimeout)}_hovered(){return this._directDescendantItems.changes.pipe(Le(this._directDescendantItems),an(t=>$e(...t.map(r=>r._hovered))))}addItem(e){}removeItem(e){}_handleKeydown(e){let t=e.keyCode,r=this._keyManager;switch(t){case 27:xe(e)||(e.preventDefault(),this.closed.emit("keydown"));break;case 37:this.parentMenu&&this.direction==="ltr"&&this.closed.emit("keydown");break;case 39:this.parentMenu&&this.direction==="rtl"&&this.closed.emit("keydown");break;default:(t===38||t===40)&&r.setFocusOrigin("keyboard"),r.onKeydown(e);return}}focusFirstItem(e="program"){this._firstItemFocusRef?.destroy(),this._firstItemFocusRef=rt(()=>{let t=this._resolvePanel();if(!t||!t.contains(document.activeElement)){let r=this._keyManager;r.setFocusOrigin(e).setFirstItemActive(),!r.activeItem&&t&&t.focus()}},{injector:this._injector})}resetActiveItem(){this._keyManager.setActiveItem(-1)}setElevation(e){}setPositionClasses(e=this.xPosition,t=this.yPosition){this._classList=q(g({},this._classList),{"mat-menu-before":e==="before","mat-menu-after":e==="after","mat-menu-above":t==="above","mat-menu-below":t==="below"}),this._changeDetectorRef.markForCheck()}_onAnimationDone(e){let t=e===Fa;(t||e===ys)&&(t&&(clearTimeout(this._exitFallbackTimeout),this._exitFallbackTimeout=void 0),this._animationDone.next(t?"void":"enter"),this._isAnimating.set(!1))}_onAnimationStart(e){(e===ys||e===Fa)&&this._isAnimating.set(!0)}_setIsOpen(e){if(this._panelAnimationState=e?"enter":"void",e){if(this._keyManager.activeItemIndex===0){let t=this._resolvePanel();t&&(t.scrollTop=0)}}else this._animationsDisabled||(this._exitFallbackTimeout=setTimeout(()=>this._onAnimationDone(Fa),200));this._animationsDisabled&&setTimeout(()=>{this._onAnimationDone(e?ys:Fa)}),this._changeDetectorRef.markForCheck()}_updateDirectDescendants(){this._allItems.changes.pipe(Le(this._allItems)).subscribe(e=>{this._directDescendantItems.reset(e.filter(t=>t._parentMenu===this)),this._directDescendantItems.notifyOnChanges()})}_resolvePanel(){let e=null;return this._directDescendantItems.length&&(e=this._directDescendantItems.first._getHostElement().closest('[role="menu"]')),e}static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["mat-menu"]],contentQueries:function(t,r,a){if(t&1&&sn(a,Pf,5)(a,jn,5)(a,jn,4),t&2){let o;z(o=H())&&(r.lazyContent=o.first),z(o=H())&&(r._allItems=o),z(o=H())&&(r.items=o)}},viewQuery:function(t,r){if(t&1&&ke(tr,5),t&2){let a;z(a=H())&&(r.templateRef=a.first)}},hostVars:3,hostBindings:function(t,r){t&2&&B("aria-label",null)("aria-labelledby",null)("aria-describedby",null)},inputs:{backdropClass:"backdropClass",ariaLabel:[0,"aria-label","ariaLabel"],ariaLabelledby:[0,"aria-labelledby","ariaLabelledby"],ariaDescribedby:[0,"aria-describedby","ariaDescribedby"],xPosition:"xPosition",yPosition:"yPosition",overlapTrigger:[2,"overlapTrigger","overlapTrigger",Y],hasBackdrop:[2,"hasBackdrop","hasBackdrop",e=>e==null?null:Y(e)],panelClass:[0,"class","panelClass"],classList:"classList"},outputs:{closed:"closed",close:"close"},exportAs:["matMenu"],features:[re([{provide:Cs,useExisting:i}])],ngContentSelectors:Tf,decls:1,vars:0,consts:[["tabindex","-1","role","menu",1,"mat-mdc-menu-panel",3,"click","animationstart","animationend","animationcancel","id"],[1,"mat-mdc-menu-content"]],template:function(t,r){t&1&&(Ee(),ei(0,Ff,3,12,"ng-template"))},styles:[`mat-menu {
  display: none;
}

.mat-mdc-menu-content {
  margin: 0;
  padding: 8px 0;
  outline: 0;
}
.mat-mdc-menu-content,
.mat-mdc-menu-content .mat-mdc-menu-item .mat-mdc-menu-item-text {
  -moz-osx-font-smoothing: grayscale;
  -webkit-font-smoothing: antialiased;
  flex: 1;
  white-space: normal;
  font-family: var(--mat-menu-item-label-text-font, var(--mat-sys-label-large-font));
  line-height: var(--mat-menu-item-label-text-line-height, var(--mat-sys-label-large-line-height));
  font-size: var(--mat-menu-item-label-text-size, var(--mat-sys-label-large-size));
  letter-spacing: var(--mat-menu-item-label-text-tracking, var(--mat-sys-label-large-tracking));
  font-weight: var(--mat-menu-item-label-text-weight, var(--mat-sys-label-large-weight));
}

@keyframes _mat-menu-enter {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: none;
  }
}
@keyframes _mat-menu-exit {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}
.mat-mdc-menu-panel {
  min-width: 112px;
  max-width: 280px;
  overflow: auto;
  box-sizing: border-box;
  outline: 0;
  animation: _mat-menu-enter 120ms cubic-bezier(0, 0, 0.2, 1);
  border-radius: var(--mat-menu-container-shape, var(--mat-sys-corner-extra-small));
  background-color: var(--mat-menu-container-color, var(--mat-sys-surface-container));
  box-shadow: var(--mat-menu-container-elevation-shadow, 0px 3px 1px -2px rgba(0, 0, 0, 0.2), 0px 2px 2px 0px rgba(0, 0, 0, 0.14), 0px 1px 5px 0px rgba(0, 0, 0, 0.12));
  will-change: transform, opacity;
}
.mat-mdc-menu-panel.mat-menu-panel-exit-animation {
  animation: _mat-menu-exit 100ms 25ms linear forwards;
}
.mat-mdc-menu-panel.mat-menu-panel-animations-disabled {
  animation: none;
}
.mat-mdc-menu-panel.mat-menu-panel-animating {
  pointer-events: none;
}
.mat-mdc-menu-panel.mat-menu-panel-animating:has(.mat-mdc-menu-content:empty) {
  display: none;
}
@media (forced-colors: active) {
  .mat-mdc-menu-panel {
    outline: solid 1px;
  }
}
.mat-mdc-menu-panel .mat-divider {
  border-top-color: var(--mat-menu-divider-color, var(--mat-sys-surface-variant));
  margin-bottom: var(--mat-menu-divider-bottom-spacing, 8px);
  margin-top: var(--mat-menu-divider-top-spacing, 8px);
}

.mat-mdc-menu-item {
  display: flex;
  position: relative;
  align-items: center;
  justify-content: flex-start;
  overflow: hidden;
  padding: 0;
  cursor: pointer;
  width: 100%;
  text-align: left;
  box-sizing: border-box;
  color: inherit;
  font-size: inherit;
  background: none;
  text-decoration: none;
  margin: 0;
  min-height: 48px;
  padding-left: var(--mat-menu-item-leading-spacing, 12px);
  padding-right: var(--mat-menu-item-trailing-spacing, 12px);
  -webkit-user-select: none;
  user-select: none;
  cursor: pointer;
  outline: none;
  border: none;
  -webkit-tap-highlight-color: transparent;
}
.mat-mdc-menu-item::-moz-focus-inner {
  border: 0;
}
[dir=rtl] .mat-mdc-menu-item {
  padding-left: var(--mat-menu-item-trailing-spacing, 12px);
  padding-right: var(--mat-menu-item-leading-spacing, 12px);
}
.mat-mdc-menu-item:has(.material-icons, mat-icon, [matButtonIcon]) {
  padding-left: var(--mat-menu-item-with-icon-leading-spacing, 12px);
  padding-right: var(--mat-menu-item-with-icon-trailing-spacing, 12px);
}
[dir=rtl] .mat-mdc-menu-item:has(.material-icons, mat-icon, [matButtonIcon]) {
  padding-left: var(--mat-menu-item-with-icon-trailing-spacing, 12px);
  padding-right: var(--mat-menu-item-with-icon-leading-spacing, 12px);
}
.mat-mdc-menu-item, .mat-mdc-menu-item:visited, .mat-mdc-menu-item:link {
  color: var(--mat-menu-item-label-text-color, var(--mat-sys-on-surface));
}
.mat-mdc-menu-item .mat-icon-no-color,
.mat-mdc-menu-item .mat-mdc-menu-submenu-icon {
  color: var(--mat-menu-item-icon-color, var(--mat-sys-on-surface-variant));
}
.mat-mdc-menu-item[disabled] {
  cursor: default;
  opacity: 0.38;
}
.mat-mdc-menu-item[disabled]::after {
  display: block;
  position: absolute;
  content: "";
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;
}
.mat-mdc-menu-item:focus {
  outline: 0;
}
.mat-mdc-menu-item .mat-icon {
  flex-shrink: 0;
  margin-right: var(--mat-menu-item-spacing, 12px);
  height: var(--mat-menu-item-icon-size, 24px);
  width: var(--mat-menu-item-icon-size, 24px);
}
[dir=rtl] .mat-mdc-menu-item {
  text-align: right;
}
[dir=rtl] .mat-mdc-menu-item .mat-icon {
  margin-right: 0;
  margin-left: var(--mat-menu-item-spacing, 12px);
}
.mat-mdc-menu-item:not([disabled]):hover {
  background-color: var(--mat-menu-item-hover-state-layer-color, color-mix(in srgb, var(--mat-sys-on-surface) calc(var(--mat-sys-hover-state-layer-opacity) * 100%), transparent));
}
.mat-mdc-menu-item:not([disabled]).cdk-program-focused, .mat-mdc-menu-item:not([disabled]).cdk-keyboard-focused, .mat-mdc-menu-item:not([disabled]).mat-mdc-menu-item-highlighted {
  background-color: var(--mat-menu-item-focus-state-layer-color, color-mix(in srgb, var(--mat-sys-on-surface) calc(var(--mat-sys-focus-state-layer-opacity) * 100%), transparent));
}
@media (forced-colors: active) {
  .mat-mdc-menu-item {
    margin-top: 1px;
  }
}

.mat-mdc-menu-submenu-icon {
  width: var(--mat-menu-item-icon-size, 24px);
  height: 10px;
  fill: currentColor;
  padding-left: var(--mat-menu-item-spacing, 12px);
}
[dir=rtl] .mat-mdc-menu-submenu-icon {
  padding-right: var(--mat-menu-item-spacing, 12px);
  padding-left: 0;
}
[dir=rtl] .mat-mdc-menu-submenu-icon polygon {
  transform: scaleX(-1);
  transform-origin: center;
}
@media (forced-colors: active) {
  .mat-mdc-menu-submenu-icon {
    fill: CanvasText;
  }
}

.mat-mdc-menu-item .mat-mdc-menu-ripple {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  pointer-events: none;
}
`],encapsulation:2,changeDetection:0})}return i})(),Vf=new M("mat-menu-scroll-strategy",{providedIn:"root",factory:()=>{let i=l(U);return()=>mn(i)}});var fi=new WeakMap,Nf=(()=>{class i{_canHaveBackdrop;_element=l(N);_viewContainerRef=l(Jn);_menuItemInstance=l(jn,{optional:!0,self:!0});_dir=l(Ze,{optional:!0});_focusMonitor=l(It);_ngZone=l(X);_injector=l(U);_scrollStrategy=l(Vf);_changeDetectorRef=l(ne);_animationsDisabled=ye();_portal;_overlayRef=null;_menuOpen=!1;_closingActionsSubscription=oe.EMPTY;_menuCloseSubscription=oe.EMPTY;_pendingRemoval;_parentMaterialMenu;_parentInnerPadding;_openedBy=void 0;get _menu(){return this._menuInternal}set _menu(e){e!==this._menuInternal&&(this._menuInternal=e,this._menuCloseSubscription.unsubscribe(),e&&(this._parentMaterialMenu,this._menuCloseSubscription=e.close.subscribe(t=>{this._destroyMenu(t),(t==="click"||t==="tab")&&this._parentMaterialMenu&&this._parentMaterialMenu.closed.emit(t)})),this._menuItemInstance?._setTriggersSubmenu(this._triggersSubmenu()))}_menuInternal=null;constructor(e){this._canHaveBackdrop=e;let t=l(Cs,{optional:!0});this._parentMaterialMenu=t instanceof gi?t:void 0}ngOnDestroy(){this._menu&&this._ownsMenu(this._menu)&&fi.delete(this._menu),this._pendingRemoval?.unsubscribe(),this._menuCloseSubscription.unsubscribe(),this._closingActionsSubscription.unsubscribe(),this._overlayRef&&(this._overlayRef.dispose(),this._overlayRef=null)}get menuOpen(){return this._menuOpen}get dir(){return this._dir&&this._dir.value==="rtl"?"rtl":"ltr"}_triggersSubmenu(){return!!(this._menuItemInstance&&this._parentMaterialMenu&&this._menu)}_closeMenu(){this._menu?.close.emit()}_openMenu(e){if(this._triggerIsAriaDisabled())return;let t=this._menu;if(this._menuOpen||!t)return;this._pendingRemoval?.unsubscribe();let r=fi.get(t);fi.set(t,this),r&&r!==this&&r._closeMenu();let a=this._createOverlay(t),o=a.getConfig(),s=o.positionStrategy;this._setPosition(t,s),this._canHaveBackdrop?o.hasBackdrop=t.hasBackdrop==null?!this._triggersSubmenu():t.hasBackdrop:o.hasBackdrop=t.hasBackdrop??!1,a.hasAttached()||(a.attach(this._getPortal(t)),t.lazyContent?.attach(this.menuData)),this._closingActionsSubscription=this._menuClosingActions().subscribe(()=>this._closeMenu()),t.parentMenu=this._triggersSubmenu()?this._parentMaterialMenu:void 0,t.direction=this.dir,e&&t.focusFirstItem(this._openedBy||"program"),this._setIsMenuOpen(!0),t instanceof gi&&(t._setIsOpen(!0),t._directDescendantItems.changes.pipe(ge(t.close)).subscribe(()=>{s.withLockedPosition(!1).reapplyLastPosition(),s.withLockedPosition(!0)}))}focus(e,t){this._focusMonitor&&e?this._focusMonitor.focusVia(this._element,e,t):this._element.nativeElement.focus(t)}_destroyMenu(e){let t=this._overlayRef,r=this._menu;!t||!this.menuOpen||(this._closingActionsSubscription.unsubscribe(),this._pendingRemoval?.unsubscribe(),r instanceof gi&&this._ownsMenu(r)?(this._pendingRemoval=r._animationDone.pipe(Ne(1)).subscribe(()=>{t.detach(),fi.has(r)||r.lazyContent?.detach()}),r._setIsOpen(!1)):(t.detach(),r?.lazyContent?.detach()),r&&this._ownsMenu(r)&&fi.delete(r),this.restoreFocus&&(e==="keydown"||!this._openedBy||!this._triggersSubmenu())&&this.focus(this._openedBy),this._openedBy=void 0,this._setIsMenuOpen(!1))}_setIsMenuOpen(e){e!==this._menuOpen&&(this._menuOpen=e,this._menuOpen?this.menuOpened.emit():this.menuClosed.emit(),this._triggersSubmenu()&&this._menuItemInstance._setHighlighted(e),this._changeDetectorRef.markForCheck())}_createOverlay(e){if(!this._overlayRef){let t=this._getOverlayConfig(e);this._subscribeToPositions(e,t.positionStrategy),this._overlayRef=pn(this._injector,t),this._overlayRef.keydownEvents().subscribe(r=>{this._menu instanceof gi&&this._menu._handleKeydown(r)})}return this._overlayRef}_getOverlayConfig(e){return new si({positionStrategy:li(this._injector,this._getOverlayOrigin()).withLockedPosition().withGrowAfterOpen().withTransformOriginOn(".mat-menu-panel, .mat-mdc-menu-panel"),backdropClass:e.backdropClass||"cdk-overlay-transparent-backdrop",panelClass:e.overlayPanelClass,scrollStrategy:this._scrollStrategy(),direction:this._dir||"ltr",disableAnimations:this._animationsDisabled})}_subscribeToPositions(e,t){e.setPositionClasses&&t.positionChanges.subscribe(r=>{this._ngZone.run(()=>{let a=r.connectionPair.overlayX==="start"?"after":"before",o=r.connectionPair.overlayY==="top"?"below":"above";e.setPositionClasses(a,o)})})}_setPosition(e,t){let[r,a]=e.xPosition==="before"?["end","start"]:["start","end"],[o,s]=e.yPosition==="above"?["bottom","top"]:["top","bottom"],[d,u]=[o,s],[b,A]=[r,a],Q=0;if(this._triggersSubmenu()){if(A=r=e.xPosition==="before"?"start":"end",a=b=r==="end"?"start":"end",this._parentMaterialMenu){if(this._parentInnerPadding==null){let I=this._parentMaterialMenu.items.first;this._parentInnerPadding=I?I._getHostElement().offsetTop:0}Q=o==="bottom"?this._parentInnerPadding:-this._parentInnerPadding}}else e.overlapTrigger||(d=o==="top"?"bottom":"top",u=s==="top"?"bottom":"top");t.withPositions([{originX:r,originY:d,overlayX:b,overlayY:o,offsetY:Q},{originX:a,originY:d,overlayX:A,overlayY:o,offsetY:Q},{originX:r,originY:u,overlayX:b,overlayY:s,offsetY:-Q},{originX:a,originY:u,overlayX:A,overlayY:s,offsetY:-Q}])}_menuClosingActions(){let e=this._getOutsideClickStream(this._overlayRef),t=this._overlayRef.detachments(),r=this._parentMaterialMenu?this._parentMaterialMenu.closed:Ie(),a=this._parentMaterialMenu?this._parentMaterialMenu._hovered().pipe(De(o=>this._menuOpen&&o!==this._menuItemInstance)):Ie();return $e(e,r,a,t)}_getPortal(e){return(!this._portal||this._portal.templateRef!==e.templateRef)&&(this._portal=new ir(e.templateRef,this._viewContainerRef)),this._portal}_ownsMenu(e){return fi.get(e)===this}_triggerIsAriaDisabled(){return Y(this._element.nativeElement.getAttribute("aria-disabled"))}static \u0275fac=function(t){na()};static \u0275dir=E({type:i})}return i})(),Lc=(()=>{class i extends Nf{_cleanupTouchstart;_hoverSubscription=oe.EMPTY;get _deprecatedMatMenuTriggerFor(){return this.menu}set _deprecatedMatMenuTriggerFor(e){this.menu=e}get menu(){return this._menu}set menu(e){this._menu=e}menuData;restoreFocus=!0;menuOpened=new S;onMenuOpen=this.menuOpened;menuClosed=new S;onMenuClose=this.menuClosed;constructor(){super(!0);let e=l(we);this._cleanupTouchstart=e.listen(this._element.nativeElement,"touchstart",t=>{Nn(t)||(this._openedBy="touch")},{passive:!0})}triggersSubmenu(){return super._triggersSubmenu()}toggleMenu(){return this.menuOpen?this.closeMenu():this.openMenu()}openMenu(){this._openMenu(!0)}closeMenu(){this._closeMenu()}updatePosition(){this._overlayRef?.updatePosition()}ngAfterContentInit(){this._handleHover()}ngOnDestroy(){super.ngOnDestroy(),this._cleanupTouchstart(),this._hoverSubscription.unsubscribe()}_getOverlayOrigin(){return this._element}_getOutsideClickStream(e){return e.backdropClick()}_handleMousedown(e){Vn(e)||(this._openedBy=e.button===0?"mouse":void 0,this.triggersSubmenu()&&e.preventDefault())}_handleKeydown(e){let t=e.keyCode;(t===13||t===32)&&(this._openedBy="keyboard"),this.triggersSubmenu()&&(t===39&&this.dir==="ltr"||t===37&&this.dir==="rtl")&&(this._openedBy="keyboard",this.openMenu())}_handleClick(e){this.triggersSubmenu()?(e.stopPropagation(),this.openMenu()):this.toggleMenu()}_handleHover(){this.triggersSubmenu()&&this._parentMaterialMenu&&(this._hoverSubscription=this._parentMaterialMenu._hovered().subscribe(e=>{e===this._menuItemInstance&&!e.disabled&&this._parentMaterialMenu?._panelAnimationState!=="void"&&(this._openedBy="mouse",this._openMenu(!1))}))}static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,selectors:[["","mat-menu-trigger-for",""],["","matMenuTriggerFor",""]],hostAttrs:[1,"mat-mdc-menu-trigger"],hostVars:3,hostBindings:function(t,r){t&1&&T("click",function(o){return r._handleClick(o)})("mousedown",function(o){return r._handleMousedown(o)})("keydown",function(o){return r._handleKeydown(o)}),t&2&&B("aria-haspopup",r.menu?"menu":null)("aria-expanded",r.menuOpen)("aria-controls",r.menuOpen?r.menu==null?null:r.menu.panelId:null)},inputs:{_deprecatedMatMenuTriggerFor:[0,"mat-menu-trigger-for","_deprecatedMatMenuTriggerFor"],menu:[0,"matMenuTriggerFor","menu"],menuData:[0,"matMenuTriggerData","menuData"],restoreFocus:[0,"matMenuTriggerRestoreFocus","restoreFocus"]},outputs:{menuOpened:"menuOpened",onMenuOpen:"onMenuOpen",menuClosed:"menuClosed",onMenuClose:"onMenuClose"},exportAs:["matMenuTrigger"],features:[te]})}return i})();var Bc=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275mod=$({type:i});static \u0275inj=G({imports:[hi,Ht,fe,ai]})}return i})();var jf=["*"];function zf(i,n){if(i&1&&_(0,"i"),i&2){let e=m();Ae(Md("vitamui-icon ",e.icon())),w("mr-3",e.label())}}function Hf(i,n){if(i&1&&(p(0,"div",6)(1,"button",7),_(2,"i",8),h()()),i&2){let e=m();w("justify-content-end",e.overlayPos()==="end")}}var jc=(()=>{class i{constructor(){this.overlayPos=C("start"),this.icon=C(),this.label=C(),this.disabled=C(!1,{transform:yt}),this.menuColor=C("secondary"),this.menuButtonOutline=C(!1,{transform:yt}),this.menuButtonSize=C("large"),this.simpleOverlay=C(!1,{transform:yt})}static{this.\u0275fac=function(t){return new(t||i)}}static{this.\u0275cmp=x({type:i,selectors:[["vitamui-menu-button"]],inputs:{overlayPos:[1,"overlayPos"],icon:[1,"icon"],label:[1,"label"],disabled:[1,"disabled"],menuColor:[1,"menuColor"],menuButtonOutline:[1,"menuButtonOutline"],menuButtonSize:[1,"menuButtonSize"],simpleOverlay:[1,"simpleOverlay"]},ngContentSelectors:jf,decls:8,vars:19,consts:[["trigger","matMenuTrigger"],["moreMenu","matMenu"],["aria-label","more menu",1,"btn",3,"matMenuTriggerFor","ngClass","disabled"],[3,"class","mr-3"],[3,"xPosition","overlapTrigger"],[1,"d-flex",3,"justify-content-end"],[1,"d-flex"],["mat-menu-item","","disableRipple","",1,"menu-cancel-button"],[1,"vitamui-icon","vitamui-icon-close"]],template:function(t,r){if(t&1&&(Ee(),p(0,"button",2,0),v(2,zf,1,5,"i",3),P(3),h(),p(4,"mat-menu",4,1),v(6,Hf,3,2,"div",5),Z(7),h()),t&2){let a=Ke(1),o=Ke(5);Ae(r.menuButtonSize()),w("btn-circle",r.icon()&&!r.label())("hidden",!r.simpleOverlay()&&a.menuOpen),f("matMenuTriggerFor",o)("ngClass",r.menuButtonOutline()?`outline-${r.menuColor()}`:r.menuColor())("disabled",r.disabled()),c(2),y(r.icon()?2:-1),c(),Te(" ",r.label(),`
`),c(),Ae(wd("more-action-menu ",r.overlayPos()," ",r.menuColor()," ",r.simpleOverlay()?"simple":"")),f("xPosition",r.overlayPos()==="start"?"after":"before")("overlapTrigger",!r.simpleOverlay()),c(2),y(r.simpleOverlay()?-1:6)}},dependencies:[ca,ri,Ta,Bc,gi,jn,Lc],styles:[`.more-action-menu.mat-mdc-menu-panel.primary{--item-color: var(--vitamui-primary);--button-simple-bg-color: var(--vitamui-primary-50)}  .more-action-menu.mat-mdc-menu-panel.secondary{--item-color: var(--vitamui-secondary);--button-simple-bg-color: var(--vitamui-secondary-50)}  .more-action-menu.mat-mdc-menu-panel{max-width:max-content;background:none;box-shadow:none!important;border:none;overflow:visible}  .more-action-menu.mat-mdc-menu-panel .mat-mdc-menu-content{min-width:152px;padding:0}  .more-action-menu.mat-mdc-menu-panel.simple{border-radius:5px!important;box-shadow:0 0 2px rgba(var(--vitamui-grey-900-rgb),.2),0 1px 4px rgba(var(--vitamui-grey-900-rgb),.05)!important}  .more-action-menu.mat-mdc-menu-panel.simple .mat-mdc-menu-item{border-left-width:0!important;border-right-width:0!important;border-bottom-width:0!important}  .more-action-menu.mat-mdc-menu-panel.simple .mat-mdc-menu-item:nth-of-type(1){border-top-width:0!important}  .more-action-menu.mat-mdc-menu-panel.simple .mat-mdc-menu-item:nth-of-type(1){border-radius:5px 5px 0 0!important}  .more-action-menu.mat-mdc-menu-panel.simple button{background:var(--button-simple-bg-color)}  .more-action-menu.mat-mdc-menu-panel.start .mat-mdc-menu-item:nth-of-type(1){border-radius:0 5px 0 0}  .more-action-menu.mat-mdc-menu-panel .btn-circle.large>i{font-size:xx-large}  .more-action-menu.mat-mdc-menu-panel .mat-mdc-menu-item{border-left:1px solid var(--item-color);border-right:1px solid var(--item-color);border-top:1px solid var(--vitamui-grey-300);border-bottom:none;background-color:#fff;--mat-menu-item-label-text-size: 16px}  .more-action-menu.mat-mdc-menu-panel .mat-mdc-menu-item:nth-of-type(1){border-top:1px solid var(--item-color);border-radius:5px 0 0}  .more-action-menu.mat-mdc-menu-panel .mat-mdc-menu-item:last-of-type{border-bottom:1px solid var(--item-color);border-radius:0 0 5px 5px}  .more-action-menu.mat-mdc-menu-panel .mat-mdc-menu-item.menu-cancel-button{border:1px solid var(--item-color);border-bottom:none;border-radius:5px 5px 0 0!important;width:50px;height:50px;display:flex;align-items:center;justify-content:center}  .more-action-menu.mat-mdc-menu-panel .mat-mdc-menu-item.menu-cancel-button i{font-size:14px;color:var(--item-color)}  .more-action-menu.mat-mdc-menu-panel .mat-mdc-menu-item:hover:not([disabled]):not(.menu-cancel-button){background-color:var(--vitamui-grey-300)}  .more-action-menu.mat-mdc-menu-panel .mat-mdc-menu-item[disabled]{background-color:var(--vitamui-grey-400);opacity:1}
/*# sourceMappingURL=/vitam-ui/develop/vitamui-menu-button.component-HJZ5EV7O.css.map */`]})}}return i})();var $c=(()=>{class i{_renderer;_elementRef;onChange=e=>{};onTouched=()=>{};constructor(e,t){this._renderer=e,this._elementRef=t}setProperty(e,t){this._renderer.setProperty(this._elementRef.nativeElement,e,t)}registerOnTouched(e){this.onTouched=e}registerOnChange(e){this.onChange=e}setDisabledState(e){this.setProperty("disabled",e)}static \u0275fac=function(t){return new(t||i)(ie(we),ie(N))};static \u0275dir=E({type:i})}return i})(),Kc=(()=>{class i extends $c{static \u0275fac=(()=>{let e;return function(r){return(e||(e=bt(i)))(r||i)}})();static \u0275dir=E({type:i,features:[te]})}return i})(),dt=new M("");var Uf={provide:dt,useExisting:qe(()=>vi),multi:!0};function Wf(){let i=Yo()?Yo().getUserAgent():"";return/android (\d+)/.test(i.toLowerCase())}var Yf=new M(""),vi=(()=>{class i extends $c{_compositionMode;_composing=!1;constructor(e,t,r){super(e,t),this._compositionMode=r,this._compositionMode==null&&(this._compositionMode=!Wf())}writeValue(e){let t=e??"";this.setProperty("value",t)}_handleInput(e){(!this._compositionMode||this._compositionMode&&!this._composing)&&this.onChange(e)}_compositionStart(){this._composing=!0}_compositionEnd(e){this._composing=!1,this._compositionMode&&this.onChange(e)}static \u0275fac=function(t){return new(t||i)(ie(we),ie(N),ie(Yf,8))};static \u0275dir=E({type:i,selectors:[["input","formControlName","",3,"type","checkbox"],["textarea","formControlName",""],["input","formControl","",3,"type","checkbox"],["textarea","formControl",""],["input","ngModel","",3,"type","checkbox"],["textarea","ngModel",""],["","ngDefaultControl",""]],hostBindings:function(t,r){t&1&&T("input",function(o){return r._handleInput(o.target.value)})("blur",function(){return r.onTouched()})("compositionstart",function(){return r._compositionStart()})("compositionend",function(o){return r._compositionEnd(o.target.value)})},standalone:!1,features:[re([Uf]),te]})}return i})();function Ms(i){return i==null||ws(i)===0}function ws(i){return i==null?null:Array.isArray(i)||typeof i=="string"?i.length:i instanceof Set?i.size:null}var Jt=new M(""),Mr=new M(""),qf=/^(?=.{1,254}$)(?=.{1,64}@)[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/,de=class{static min(n){return Gf(n)}static max(n){return $f(n)}static required(n){return Zc(n)}static requiredTrue(n){return Kf(n)}static email(n){return Zf(n)}static minLength(n){return Qf(n)}static maxLength(n){return Qc(n)}static pattern(n){return Xf(n)}static nullValidator(n){return Ra()}static compose(n){return iu(n)}static composeAsync(n){return ru(n)}};function Gf(i){return n=>{if(n.value==null||i==null)return null;let e=parseFloat(n.value);return!isNaN(e)&&e<i?{min:{min:i,actual:n.value}}:null}}function $f(i){return n=>{if(n.value==null||i==null)return null;let e=parseFloat(n.value);return!isNaN(e)&&e>i?{max:{max:i,actual:n.value}}:null}}function Zc(i){return Ms(i.value)?{required:!0}:null}function Kf(i){return i.value===!0?null:{required:!0}}function Zf(i){return Ms(i.value)||qf.test(i.value)?null:{email:!0}}function Qf(i){return n=>{let e=n.value?.length??ws(n.value);return e===null||e===0?null:e<i?{minlength:{requiredLength:i,actualLength:e}}:null}}function Qc(i){return n=>{let e=n.value?.length??ws(n.value);return e!==null&&e>i?{maxlength:{requiredLength:i,actualLength:e}}:null}}function Xf(i){if(!i)return Ra;let n,e;return typeof i=="string"?(e="",i.charAt(0)!=="^"&&(e+="^"),e+=i,i.charAt(i.length-1)!=="$"&&(e+="$"),n=new RegExp(e)):(e=i.toString(),n=i),t=>{if(Ms(t.value))return null;let r=t.value;return n.test(r)?null:{pattern:{requiredPattern:e,actualValue:r}}}}function Ra(i){return null}function Xc(i){return i!=null}function Jc(i){return Ho(i)?sd(i):i}function eu(i){let n={};return i.forEach(e=>{n=e!=null?g(g({},n),e):n}),Object.keys(n).length===0?null:n}function tu(i,n){return n.map(e=>e(i))}function Jf(i){return!i.validate}function nu(i){return i.map(n=>Jf(n)?n:e=>n.validate(e))}function iu(i){if(!i)return null;let n=i.filter(Xc);return n.length==0?null:function(e){return eu(tu(e,n))}}function Es(i){return i!=null?iu(nu(i)):null}function ru(i){if(!i)return null;let n=i.filter(Xc);return n.length==0?null:function(e){let t=tu(e,n).map(Jc);return Zr(t).pipe(Fe(eu))}}function ks(i){return i!=null?ru(nu(i)):null}function zc(i,n){return i===null?[n]:Array.isArray(i)?[...i,n]:[i,n]}function au(i){return i._rawValidators}function ou(i){return i._rawAsyncValidators}function xs(i){return i?Array.isArray(i)?i:[i]:[]}function Va(i,n){return Array.isArray(i)?i.includes(n):i===n}function Hc(i,n){let e=xs(n);return xs(i).forEach(r=>{Va(e,r)||e.push(r)}),e}function Uc(i,n){return xs(n).filter(e=>!Va(i,e))}var Na=class{get value(){return this.control?this.control.value:null}get valid(){return this.control?this.control.valid:null}get invalid(){return this.control?this.control.invalid:null}get pending(){return this.control?this.control.pending:null}get disabled(){return this.control?this.control.disabled:null}get enabled(){return this.control?this.control.enabled:null}get errors(){return this.control?this.control.errors:null}get pristine(){return this.control?this.control.pristine:null}get dirty(){return this.control?this.control.dirty:null}get touched(){return this.control?this.control.touched:null}get status(){return this.control?this.control.status:null}get untouched(){return this.control?this.control.untouched:null}get statusChanges(){return this.control?this.control.statusChanges:null}get valueChanges(){return this.control?this.control.valueChanges:null}get path(){return null}_composedValidatorFn;_composedAsyncValidatorFn;_rawValidators=[];_rawAsyncValidators=[];_setValidators(n){this._rawValidators=n||[],this._composedValidatorFn=Es(this._rawValidators)}_setAsyncValidators(n){this._rawAsyncValidators=n||[],this._composedAsyncValidatorFn=ks(this._rawAsyncValidators)}get validator(){return this._composedValidatorFn||null}get asyncValidator(){return this._composedAsyncValidatorFn||null}_onDestroyCallbacks=[];_registerOnDestroy(n){this._onDestroyCallbacks.push(n)}_invokeOnDestroyCallbacks(){this._onDestroyCallbacks.forEach(n=>n()),this._onDestroyCallbacks=[]}reset(n=void 0){this.control?.reset(n)}hasError(n,e){return this.control?this.control.hasError(n,e):!1}getError(n,e){return this.control?this.control.getError(n,e):null}},xt=class extends Na{name;get formDirective(){return null}get path(){return null}},Qe=class extends Na{_parent=null;name=null;valueAccessor=null},La=class{_cd;constructor(n){this._cd=n}get isTouched(){return this._cd?.control?._touched?.(),!!this._cd?.control?.touched}get isUntouched(){return!!this._cd?.control?.untouched}get isPristine(){return this._cd?.control?._pristine?.(),!!this._cd?.control?.pristine}get isDirty(){return!!this._cd?.control?.dirty}get isValid(){return this._cd?.control?._status?.(),!!this._cd?.control?.valid}get isInvalid(){return!!this._cd?.control?.invalid}get isPending(){return!!this._cd?.control?.pending}get isSubmitted(){return this._cd?._submitted?.(),!!this._cd?.submitted}};var Xe=(()=>{class i extends La{constructor(e){super(e)}static \u0275fac=function(t){return new(t||i)(ie(Qe,2))};static \u0275dir=E({type:i,selectors:[["","formControlName",""],["","ngModel",""],["","formControl",""]],hostVars:14,hostBindings:function(t,r){t&2&&w("ng-untouched",r.isUntouched)("ng-touched",r.isTouched)("ng-pristine",r.isPristine)("ng-dirty",r.isDirty)("ng-valid",r.isValid)("ng-invalid",r.isInvalid)("ng-pending",r.isPending)},standalone:!1,features:[te]})}return i})(),i1=(()=>{class i extends La{constructor(e){super(e)}static \u0275fac=function(t){return new(t||i)(ie(xt,10))};static \u0275dir=E({type:i,selectors:[["","formGroupName",""],["","formArrayName",""],["","ngModelGroup",""],["","formGroup",""],["","formArray",""],["form",3,"ngNoForm",""],["","ngForm",""]],hostVars:16,hostBindings:function(t,r){t&2&&w("ng-untouched",r.isUntouched)("ng-touched",r.isTouched)("ng-pristine",r.isPristine)("ng-dirty",r.isDirty)("ng-valid",r.isValid)("ng-invalid",r.isInvalid)("ng-pending",r.isPending)("ng-submitted",r.isSubmitted)},standalone:!1,features:[te]})}return i})();var _r="VALID",Pa="INVALID",_i="PENDING",br="DISABLED",bn=class{},Ba=class extends bn{value;source;constructor(n,e){super(),this.value=n,this.source=e}},yr=class extends bn{pristine;source;constructor(n,e){super(),this.pristine=n,this.source=e}},Cr=class extends bn{touched;source;constructor(n,e){super(),this.touched=n,this.source=e}},bi=class extends bn{status;source;constructor(n,e){super(),this.status=n,this.source=e}},ja=class extends bn{source;constructor(n){super(),this.source=n}},xr=class extends bn{source;constructor(n){super(),this.source=n}};function As(i){return(Ya(i)?i.validators:i)||null}function eg(i){return Array.isArray(i)?Es(i):i||null}function Ss(i,n){return(Ya(n)?n.asyncValidators:i)||null}function tg(i){return Array.isArray(i)?ks(i):i||null}function Ya(i){return i!=null&&!Array.isArray(i)&&typeof i=="object"}function su(i,n,e){let t=i.controls;if(!(n?Object.keys(t):t).length)throw new Me(1e3,"");if(!t[e])throw new Me(1001,"")}function lu(i,n,e){i._forEachChild((t,r)=>{if(e[r]===void 0)throw new Me(-1002,"")})}var vn=class{_pendingDirty=!1;_hasOwnPendingAsyncValidator=null;_pendingTouched=!1;_onCollectionChange=()=>{};_updateOn;_parent=null;_asyncValidationSubscription;_composedValidatorFn;_composedAsyncValidatorFn;_rawValidators;_rawAsyncValidators;value;constructor(n,e){this._assignValidators(n),this._assignAsyncValidators(e)}get validator(){return this._composedValidatorFn}set validator(n){this._rawValidators=this._composedValidatorFn=n}get asyncValidator(){return this._composedAsyncValidatorFn}set asyncValidator(n){this._rawAsyncValidators=this._composedAsyncValidatorFn=n}get parent(){return this._parent}get status(){return W(this.statusReactive)}set status(n){W(()=>this.statusReactive.set(n))}_status=k(()=>this.statusReactive());statusReactive=V(void 0);get valid(){return this.status===_r}get invalid(){return this.status===Pa}get pending(){return this.status===_i}get disabled(){return this.status===br}get enabled(){return this.status!==br}errors;get pristine(){return W(this.pristineReactive)}set pristine(n){W(()=>this.pristineReactive.set(n))}_pristine=k(()=>this.pristineReactive());pristineReactive=V(!0);get dirty(){return!this.pristine}get touched(){return W(this.touchedReactive)}set touched(n){W(()=>this.touchedReactive.set(n))}_touched=k(()=>this.touchedReactive());touchedReactive=V(!1);get untouched(){return!this.touched}_events=new F;events=this._events.asObservable();valueChanges;statusChanges;get updateOn(){return this._updateOn?this._updateOn:this.parent?this.parent.updateOn:"change"}setValidators(n){this._assignValidators(n)}setAsyncValidators(n){this._assignAsyncValidators(n)}addValidators(n){this.setValidators(Hc(n,this._rawValidators))}addAsyncValidators(n){this.setAsyncValidators(Hc(n,this._rawAsyncValidators))}removeValidators(n){this.setValidators(Uc(n,this._rawValidators))}removeAsyncValidators(n){this.setAsyncValidators(Uc(n,this._rawAsyncValidators))}hasValidator(n){return Va(this._rawValidators,n)}hasAsyncValidator(n){return Va(this._rawAsyncValidators,n)}clearValidators(){this.validator=null}clearAsyncValidators(){this.asyncValidator=null}markAsTouched(n={}){let e=this.touched===!1;this.touched=!0;let t=n.sourceControl??this;n.onlySelf||this._parent?.markAsTouched(q(g({},n),{sourceControl:t})),e&&n.emitEvent!==!1&&this._events.next(new Cr(!0,t))}markAllAsDirty(n={}){this.markAsDirty({onlySelf:!0,emitEvent:n.emitEvent,sourceControl:this}),this._forEachChild(e=>e.markAllAsDirty(n))}markAllAsTouched(n={}){this.markAsTouched({onlySelf:!0,emitEvent:n.emitEvent,sourceControl:this}),this._forEachChild(e=>e.markAllAsTouched(n))}markAsUntouched(n={}){let e=this.touched===!0;this.touched=!1,this._pendingTouched=!1;let t=n.sourceControl??this;this._forEachChild(r=>{r.markAsUntouched({onlySelf:!0,emitEvent:n.emitEvent,sourceControl:t})}),n.onlySelf||this._parent?._updateTouched(n,t),e&&n.emitEvent!==!1&&this._events.next(new Cr(!1,t))}markAsDirty(n={}){let e=this.pristine===!0;this.pristine=!1;let t=n.sourceControl??this;n.onlySelf||this._parent?.markAsDirty(q(g({},n),{sourceControl:t})),e&&n.emitEvent!==!1&&this._events.next(new yr(!1,t))}markAsPristine(n={}){let e=this.pristine===!1;this.pristine=!0,this._pendingDirty=!1;let t=n.sourceControl??this;this._forEachChild(r=>{r.markAsPristine({onlySelf:!0,emitEvent:n.emitEvent})}),n.onlySelf||this._parent?._updatePristine(n,t),e&&n.emitEvent!==!1&&this._events.next(new yr(!0,t))}markAsPending(n={}){this.status=_i;let e=n.sourceControl??this;n.emitEvent!==!1&&(this._events.next(new bi(this.status,e)),this.statusChanges.emit(this.status)),n.onlySelf||this._parent?.markAsPending(q(g({},n),{sourceControl:e}))}disable(n={}){let e=this._parentMarkedDirty(n.onlySelf);this.status=br,this.errors=null,this._forEachChild(r=>{r.disable(q(g({},n),{onlySelf:!0}))}),this._updateValue();let t=n.sourceControl??this;n.emitEvent!==!1&&(this._events.next(new Ba(this.value,t)),this._events.next(new bi(this.status,t)),this.valueChanges.emit(this.value),this.statusChanges.emit(this.status)),this._updateAncestors(q(g({},n),{skipPristineCheck:e}),this),this._onDisabledChange.forEach(r=>r(!0))}enable(n={}){let e=this._parentMarkedDirty(n.onlySelf);this.status=_r,this._forEachChild(t=>{t.enable(q(g({},n),{onlySelf:!0}))}),this.updateValueAndValidity({onlySelf:!0,emitEvent:n.emitEvent}),this._updateAncestors(q(g({},n),{skipPristineCheck:e}),this),this._onDisabledChange.forEach(t=>t(!1))}_updateAncestors(n,e){n.onlySelf||(this._parent?.updateValueAndValidity(n),n.skipPristineCheck||this._parent?._updatePristine({},e),this._parent?._updateTouched({},e))}setParent(n){this._parent=n}getRawValue(){return this.value}updateValueAndValidity(n={}){if(this._setInitialStatus(),this._updateValue(),this.enabled){let t=this._cancelExistingSubscription();this.errors=this._runValidator(),this.status=this._calculateStatus(),(this.status===_r||this.status===_i)&&this._runAsyncValidator(t,n.emitEvent)}let e=n.sourceControl??this;n.emitEvent!==!1&&(this._events.next(new Ba(this.value,e)),this._events.next(new bi(this.status,e)),this.valueChanges.emit(this.value),this.statusChanges.emit(this.status)),n.onlySelf||this._parent?.updateValueAndValidity(q(g({},n),{sourceControl:e}))}_updateTreeValidity(n={emitEvent:!0}){this._forEachChild(e=>e._updateTreeValidity(n)),this.updateValueAndValidity({onlySelf:!0,emitEvent:n.emitEvent})}_setInitialStatus(){this.status=this._allControlsDisabled()?br:_r}_runValidator(){return this.validator?this.validator(this):null}_runAsyncValidator(n,e){if(this.asyncValidator){this.status=_i,this._hasOwnPendingAsyncValidator={emitEvent:e!==!1,shouldHaveEmitted:n!==!1};let t=Jc(this.asyncValidator(this));this._asyncValidationSubscription=t.subscribe(r=>{this._hasOwnPendingAsyncValidator=null,this.setErrors(r,{emitEvent:e,shouldHaveEmitted:n})})}}_cancelExistingSubscription(){if(this._asyncValidationSubscription){this._asyncValidationSubscription.unsubscribe();let n=(this._hasOwnPendingAsyncValidator?.emitEvent||this._hasOwnPendingAsyncValidator?.shouldHaveEmitted)??!1;return this._hasOwnPendingAsyncValidator=null,n}return!1}setErrors(n,e={}){this.errors=n,this._updateControlsErrors(e.emitEvent!==!1,this,e.shouldHaveEmitted)}get(n){let e=n;return e==null||(Array.isArray(e)||(e=e.split(".")),e.length===0)?null:e.reduce((t,r)=>t&&t._find(r),this)}getError(n,e){let t=e?this.get(e):this;return t?.errors?t.errors[n]:null}hasError(n,e){return!!this.getError(n,e)}get root(){let n=this;for(;n._parent;)n=n._parent;return n}_updateControlsErrors(n,e,t){this.status=this._calculateStatus(),n&&this.statusChanges.emit(this.status),(n||t)&&this._events.next(new bi(this.status,e)),this._parent&&this._parent._updateControlsErrors(n,e,t)}_initObservables(){this.valueChanges=new S,this.statusChanges=new S}_calculateStatus(){return this._allControlsDisabled()?br:this.errors?Pa:this._hasOwnPendingAsyncValidator||this._anyControlsHaveStatus(_i)?_i:this._anyControlsHaveStatus(Pa)?Pa:_r}_anyControlsHaveStatus(n){return this._anyControls(e=>e.status===n)}_anyControlsDirty(){return this._anyControls(n=>n.dirty)}_anyControlsTouched(){return this._anyControls(n=>n.touched)}_updatePristine(n,e){let t=!this._anyControlsDirty(),r=this.pristine!==t;this.pristine=t,n.onlySelf||this._parent?._updatePristine(n,e),r&&this._events.next(new yr(this.pristine,e))}_updateTouched(n={},e){this.touched=this._anyControlsTouched(),this._events.next(new Cr(this.touched,e)),n.onlySelf||this._parent?._updateTouched(n,e)}_onDisabledChange=[];_registerOnCollectionChange(n){this._onCollectionChange=n}_setUpdateStrategy(n){Ya(n)&&n.updateOn!=null&&(this._updateOn=n.updateOn)}_parentMarkedDirty(n){return!n&&!!this._parent?.dirty&&!this._parent._anyControlsDirty()}_find(n){return null}_assignValidators(n){this._rawValidators=Array.isArray(n)?n.slice():n,this._composedValidatorFn=eg(this._rawValidators)}_assignAsyncValidators(n){this._rawAsyncValidators=Array.isArray(n)?n.slice():n,this._composedAsyncValidatorFn=tg(this._rawAsyncValidators)}},zn=class extends vn{constructor(n,e,t){super(As(e),Ss(t,e)),this.controls=n,this._initObservables(),this._setUpdateStrategy(e),this._setUpControls(),this.updateValueAndValidity({onlySelf:!0,emitEvent:!!this.asyncValidator})}controls;registerControl(n,e){return this.controls[n]?this.controls[n]:(this.controls[n]=e,e.setParent(this),e._registerOnCollectionChange(this._onCollectionChange),e)}addControl(n,e,t={}){this.registerControl(n,e),this.updateValueAndValidity({emitEvent:t.emitEvent}),this._onCollectionChange()}removeControl(n,e={}){this.controls[n]&&this.controls[n]._registerOnCollectionChange(()=>{}),delete this.controls[n],this.updateValueAndValidity({emitEvent:e.emitEvent}),this._onCollectionChange()}setControl(n,e,t={}){this.controls[n]&&this.controls[n]._registerOnCollectionChange(()=>{}),delete this.controls[n],e&&this.registerControl(n,e),this.updateValueAndValidity({emitEvent:t.emitEvent}),this._onCollectionChange()}contains(n){return this.controls.hasOwnProperty(n)&&this.controls[n].enabled}setValue(n,e={}){lu(this,!0,n),Object.keys(n).forEach(t=>{su(this,!0,t),this.controls[t].setValue(n[t],{onlySelf:!0,emitEvent:e.emitEvent})}),this.updateValueAndValidity(e)}patchValue(n,e={}){n!=null&&(Object.keys(n).forEach(t=>{let r=this.controls[t];r&&r.patchValue(n[t],{onlySelf:!0,emitEvent:e.emitEvent})}),this.updateValueAndValidity(e))}reset(n={},e={}){this._forEachChild((t,r)=>{t.reset(n?n[r]:null,q(g({},e),{onlySelf:!0}))}),this._updatePristine(e,this),this._updateTouched(e,this),this.updateValueAndValidity(e),e?.emitEvent!==!1&&this._events.next(new xr(this))}getRawValue(){return this._reduceChildren({},(n,e,t)=>(n[t]=e.getRawValue(),n))}_syncPendingControls(){let n=this._reduceChildren(!1,(e,t)=>t._syncPendingControls()?!0:e);return n&&this.updateValueAndValidity({onlySelf:!0}),n}_forEachChild(n){Object.keys(this.controls).forEach(e=>{let t=this.controls[e];t&&n(t,e)})}_setUpControls(){this._forEachChild(n=>{n.setParent(this),n._registerOnCollectionChange(this._onCollectionChange)})}_updateValue(){this.value=this._reduceValue()}_anyControls(n){for(let[e,t]of Object.entries(this.controls))if(this.contains(e)&&n(t))return!0;return!1}_reduceValue(){let n={};return this._reduceChildren(n,(e,t,r)=>((t.enabled||this.disabled)&&(e[r]=t.value),e))}_reduceChildren(n,e){let t=n;return this._forEachChild((r,a)=>{t=e(t,r,a)}),t}_allControlsDisabled(){for(let n of Object.keys(this.controls))if(this.controls[n].enabled)return!1;return Object.keys(this.controls).length>0||this.disabled}_find(n){return this.controls.hasOwnProperty(n)?this.controls[n]:null}};var Ds=class extends zn{};var yi=new M("",{factory:()=>qa}),qa="always";function du(i,n){return[...n.path,i]}function Dr(i,n,e=qa){Is(i,n),n.valueAccessor.writeValue(i.value),(i.disabled||e==="always")&&n.valueAccessor.setDisabledState?.(i.disabled),ig(i,n),ag(i,n),rg(i,n),ng(i,n)}function za(i,n,e=!0){let t=()=>{};n?.valueAccessor?.registerOnChange(t),n?.valueAccessor?.registerOnTouched(t),Ua(i,n),i&&(n._invokeOnDestroyCallbacks(),i._registerOnCollectionChange(()=>{}))}function Ha(i,n){i.forEach(e=>{e.registerOnValidatorChange&&e.registerOnValidatorChange(n)})}function ng(i,n){if(n.valueAccessor.setDisabledState){let e=t=>{n.valueAccessor.setDisabledState(t)};i.registerOnDisabledChange(e),n._registerOnDestroy(()=>{i._unregisterOnDisabledChange(e)})}}function Is(i,n){let e=au(i);n.validator!==null?i.setValidators(zc(e,n.validator)):typeof e=="function"&&i.setValidators([e]);let t=ou(i);n.asyncValidator!==null?i.setAsyncValidators(zc(t,n.asyncValidator)):typeof t=="function"&&i.setAsyncValidators([t]);let r=()=>i.updateValueAndValidity();Ha(n._rawValidators,r),Ha(n._rawAsyncValidators,r)}function Ua(i,n){let e=!1;if(i!==null){if(n.validator!==null){let r=au(i);if(Array.isArray(r)&&r.length>0){let a=r.filter(o=>o!==n.validator);a.length!==r.length&&(e=!0,i.setValidators(a))}}if(n.asyncValidator!==null){let r=ou(i);if(Array.isArray(r)&&r.length>0){let a=r.filter(o=>o!==n.asyncValidator);a.length!==r.length&&(e=!0,i.setAsyncValidators(a))}}}let t=()=>{};return Ha(n._rawValidators,t),Ha(n._rawAsyncValidators,t),e}function ig(i,n){n.valueAccessor.registerOnChange(e=>{i._pendingValue=e,i._pendingChange=!0,i._pendingDirty=!0,i.updateOn==="change"&&cu(i,n)})}function rg(i,n){n.valueAccessor.registerOnTouched(()=>{i._pendingTouched=!0,i.updateOn==="blur"&&i._pendingChange&&cu(i,n),i.updateOn!=="submit"&&i.markAsTouched()})}function cu(i,n){i._pendingDirty&&i.markAsDirty(),i.setValue(i._pendingValue,{emitModelToViewChange:!1}),n.viewToModelUpdate(i._pendingValue),i._pendingChange=!1}function ag(i,n){let e=(t,r)=>{n.valueAccessor.writeValue(t),r&&n.viewToModelUpdate(t)};i.registerOnChange(e),n._registerOnDestroy(()=>{i._unregisterOnChange(e)})}function uu(i,n){i==null,Is(i,n)}function og(i,n){return Ua(i,n)}function Os(i,n){if(!i.hasOwnProperty("model"))return!1;let e=i.model;return e.isFirstChange()?!0:!Object.is(n,e.currentValue)}function sg(i){return Object.getPrototypeOf(i.constructor)===Kc}function mu(i,n){i._syncPendingControls(),n.forEach(e=>{let t=e.control;t.updateOn==="submit"&&t._pendingChange&&(e.viewToModelUpdate(t._pendingValue),t._pendingChange=!1)})}function Ts(i,n){if(!n)return null;Array.isArray(n);let e,t,r;return n.forEach(a=>{a.constructor===vi?e=a:sg(a)?t=a:r=a}),r||t||e||null}function lg(i,n){let e=i.indexOf(n);e>-1&&i.splice(e,1)}var dg={provide:xt,useExisting:qe(()=>Ga)},vr=Promise.resolve(),Ga=(()=>{class i extends xt{callSetDisabledState;get submitted(){return W(this.submittedReactive)}_submitted=k(()=>this.submittedReactive());submittedReactive=V(!1);_directives=new Set;form;ngSubmit=new S;options;constructor(e,t,r){super(),this.callSetDisabledState=r,this.form=new zn({},Es(e),ks(t))}ngAfterViewInit(){this._setUpdateStrategy()}get formDirective(){return this}get control(){return this.form}get path(){return[]}get controls(){return this.form.controls}addControl(e){vr.then(()=>{let t=this._findContainer(e.path);e.control=t.registerControl(e.name,e.control),Dr(e.control,e,this.callSetDisabledState),e.control.updateValueAndValidity({emitEvent:!1}),this._directives.add(e)})}getControl(e){return this.form.get(e.path)}removeControl(e){vr.then(()=>{this._findContainer(e.path)?.removeControl(e.name),this._directives.delete(e)})}addFormGroup(e){vr.then(()=>{let t=this._findContainer(e.path),r=new zn({});uu(r,e),t.registerControl(e.name,r),r.updateValueAndValidity({emitEvent:!1})})}removeFormGroup(e){vr.then(()=>{this._findContainer(e.path)?.removeControl?.(e.name)})}getFormGroup(e){return this.form.get(e.path)}updateModel(e,t){vr.then(()=>{this.form.get(e.path).setValue(t)})}setValue(e){this.control.setValue(e)}onSubmit(e){return this.submittedReactive.set(!0),mu(this.form,this._directives),this.ngSubmit.emit(e),this.form._events.next(new ja(this.control)),e?.target?.method==="dialog"}onReset(){this.resetForm()}resetForm(e=void 0){this.form.reset(e),this.submittedReactive.set(!1)}_setUpdateStrategy(){this.options&&this.options.updateOn!=null&&(this.form._updateOn=this.options.updateOn)}_findContainer(e){return e.pop(),e.length?this.form.get(e):this.form}static \u0275fac=function(t){return new(t||i)(ie(Jt,10),ie(Mr,10),ie(yi,8))};static \u0275dir=E({type:i,selectors:[["form",3,"ngNoForm","",3,"formGroup","",3,"formArray",""],["ng-form"],["","ngForm",""]],hostBindings:function(t,r){t&1&&T("submit",function(o){return r.onSubmit(o)})("reset",function(){return r.onReset()})},inputs:{options:[0,"ngFormOptions","options"]},outputs:{ngSubmit:"ngSubmit"},exportAs:["ngForm"],standalone:!1,features:[re([dg]),te]})}return i})();function Wc(i,n){let e=i.indexOf(n);e>-1&&i.splice(e,1)}function Yc(i){return typeof i=="object"&&i!==null&&Object.keys(i).length===2&&"value"in i&&"disabled"in i}var Ge=class extends vn{defaultValue=null;_onChange=[];_pendingValue;_pendingChange=!1;constructor(n=null,e,t){super(As(e),Ss(t,e)),this._applyFormState(n),this._setUpdateStrategy(e),this._initObservables(),this.updateValueAndValidity({onlySelf:!0,emitEvent:!!this.asyncValidator}),Ya(e)&&(e.nonNullable||e.initialValueIsDefault)&&(Yc(n)?this.defaultValue=n.value:this.defaultValue=n)}setValue(n,e={}){this.value=this._pendingValue=n,this._onChange.length&&e.emitModelToViewChange!==!1&&this._onChange.forEach(t=>t(this.value,e.emitViewToModelChange!==!1)),this.updateValueAndValidity(e)}patchValue(n,e={}){this.setValue(n,e)}reset(n=this.defaultValue,e={}){this._applyFormState(n),this.markAsPristine(e),this.markAsUntouched(e),this.setValue(this.value,e),e.overwriteDefaultValue&&(this.defaultValue=this.value),this._pendingChange=!1,e?.emitEvent!==!1&&this._events.next(new xr(this))}_updateValue(){}_anyControls(n){return!1}_allControlsDisabled(){return this.disabled}registerOnChange(n){this._onChange.push(n)}_unregisterOnChange(n){Wc(this._onChange,n)}registerOnDisabledChange(n){this._onDisabledChange.push(n)}_unregisterOnDisabledChange(n){Wc(this._onDisabledChange,n)}_forEachChild(n){}_syncPendingControls(){return this.updateOn==="submit"&&(this._pendingDirty&&this.markAsDirty(),this._pendingTouched&&this.markAsTouched(),this._pendingChange)?(this.setValue(this._pendingValue,{onlySelf:!0,emitModelToViewChange:!1}),!0):!1}_applyFormState(n){Yc(n)?(this.value=this._pendingValue=n.value,n.disabled?this.disable({onlySelf:!0,emitEvent:!1}):this.enable({onlySelf:!0,emitEvent:!1})):this.value=this._pendingValue=n}};var cg=i=>i instanceof Ge;var ug={provide:Qe,useExisting:qe(()=>Hn)},qc=Promise.resolve(),Hn=(()=>{class i extends Qe{_changeDetectorRef;callSetDisabledState;control=new Ge;static ngAcceptInputType_isDisabled;_registered=!1;viewModel;name="";isDisabled;model;options;update=new S;constructor(e,t,r,a,o,s){super(),this._changeDetectorRef=o,this.callSetDisabledState=s,this._parent=e,this._setValidators(t),this._setAsyncValidators(r),this.valueAccessor=Ts(this,a)}ngOnChanges(e){if(this._checkForErrors(),!this._registered||"name"in e){if(this._registered&&(this._checkName(),this.formDirective)){let t=e.name.previousValue;this.formDirective.removeControl({name:t,path:this._getPath(t)})}this._setUpControl()}"isDisabled"in e&&this._updateDisabled(e),Os(e,this.viewModel)&&(this._updateValue(this.model),this.viewModel=this.model)}ngOnDestroy(){this.formDirective?.removeControl(this)}get path(){return this._getPath(this.name)}get formDirective(){return this._parent?this._parent.formDirective:null}viewToModelUpdate(e){this.viewModel=e,this.update.emit(e)}_setUpControl(){this._setUpdateStrategy(),this._isStandalone()?this._setUpStandalone():this.formDirective.addControl(this),this._registered=!0}_setUpdateStrategy(){this.options&&this.options.updateOn!=null&&(this.control._updateOn=this.options.updateOn)}_isStandalone(){return!this._parent||!!(this.options&&this.options.standalone)}_setUpStandalone(){Dr(this.control,this,this.callSetDisabledState),this.control.updateValueAndValidity({emitEvent:!1})}_checkForErrors(){this._checkName()}_checkName(){this.options&&this.options.name&&(this.name=this.options.name),!this._isStandalone()&&this.name}_updateValue(e){qc.then(()=>{this.control.setValue(e,{emitViewToModelChange:!1}),this._changeDetectorRef?.markForCheck()})}_updateDisabled(e){let t=e.isDisabled.currentValue,r=t!==0&&Y(t);qc.then(()=>{r&&!this.control.disabled?this.control.disable():!r&&this.control.disabled&&this.control.enable(),this._changeDetectorRef?.markForCheck()})}_getPath(e){return this._parent?du(e,this._parent):[e]}static \u0275fac=function(t){return new(t||i)(ie(xt,9),ie(Jt,10),ie(Mr,10),ie(dt,10),ie(ne,8),ie(yi,8))};static \u0275dir=E({type:i,selectors:[["","ngModel","",3,"formControlName","",3,"formControl",""]],inputs:{name:"name",isDisabled:[0,"disabled","isDisabled"],model:[0,"ngModel","model"],options:[0,"ngModelOptions","options"]},outputs:{update:"ngModelChange"},exportAs:["ngModel"],standalone:!1,features:[re([ug]),te,pe]})}return i})();var a1=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,selectors:[["form",3,"ngNoForm","",3,"ngNativeValidate",""]],hostAttrs:["novalidate",""],standalone:!1})}return i})(),mg={provide:dt,useExisting:qe(()=>pg),multi:!0},pg=(()=>{class i extends Kc{writeValue(e){let t=e??"";this.setProperty("value",t)}registerOnChange(e){this.onChange=t=>{e(t==""?null:parseFloat(t))}}static \u0275fac=(()=>{let e;return function(r){return(e||(e=bt(i)))(r||i)}})();static \u0275dir=E({type:i,selectors:[["input","type","number","formControlName",""],["input","type","number","formControl",""],["input","type","number","ngModel",""]],hostBindings:function(t,r){t&1&&T("input",function(o){return r.onChange(o.target.value)})("blur",function(){return r.onTouched()})},standalone:!1,features:[re([mg]),te]})}return i})();var Wa=class extends vn{constructor(n,e,t){super(As(e),Ss(t,e)),this.controls=n,this._initObservables(),this._setUpdateStrategy(e),this._setUpControls(),this.updateValueAndValidity({onlySelf:!0,emitEvent:!!this.asyncValidator})}controls;at(n){return this.controls[this._adjustIndex(n)]}push(n,e={}){Array.isArray(n)?n.forEach(t=>{this.controls.push(t),this._registerControl(t)}):(this.controls.push(n),this._registerControl(n)),this.updateValueAndValidity({emitEvent:e.emitEvent}),this._onCollectionChange()}insert(n,e,t={}){this.controls.splice(n,0,e),this._registerControl(e),this.updateValueAndValidity({emitEvent:t.emitEvent})}removeAt(n,e={}){let t=this._adjustIndex(n);t<0&&(t=0),this.controls[t]&&this.controls[t]._registerOnCollectionChange(()=>{}),this.controls.splice(t,1),this.updateValueAndValidity({emitEvent:e.emitEvent})}setControl(n,e,t={}){let r=this._adjustIndex(n);r<0&&(r=0),this.controls[r]&&this.controls[r]._registerOnCollectionChange(()=>{}),this.controls.splice(r,1),e&&(this.controls.splice(r,0,e),this._registerControl(e)),this.updateValueAndValidity({emitEvent:t.emitEvent}),this._onCollectionChange()}get length(){return this.controls.length}setValue(n,e={}){lu(this,!1,n),n.forEach((t,r)=>{su(this,!1,r),this.at(r).setValue(t,{onlySelf:!0,emitEvent:e.emitEvent})}),this.updateValueAndValidity(e)}patchValue(n,e={}){n!=null&&(n.forEach((t,r)=>{this.at(r)&&this.at(r).patchValue(t,{onlySelf:!0,emitEvent:e.emitEvent})}),this.updateValueAndValidity(e))}reset(n=[],e={}){this._forEachChild((t,r)=>{t.reset(n[r],q(g({},e),{onlySelf:!0}))}),this._updatePristine(e,this),this._updateTouched(e,this),this.updateValueAndValidity(e),e?.emitEvent!==!1&&this._events.next(new xr(this))}getRawValue(){return this.controls.map(n=>n.getRawValue())}clear(n={}){this.controls.length<1||(this._forEachChild(e=>e._registerOnCollectionChange(()=>{})),this.controls.splice(0),this.updateValueAndValidity({emitEvent:n.emitEvent}))}_adjustIndex(n){return n<0?n+this.length:n}_syncPendingControls(){let n=this.controls.reduce((e,t)=>t._syncPendingControls()?!0:e,!1);return n&&this.updateValueAndValidity({onlySelf:!0}),n}_forEachChild(n){this.controls.forEach((e,t)=>{n(e,t)})}_updateValue(){this.value=this.controls.filter(n=>n.enabled||this.disabled).map(n=>n.value)}_anyControls(n){return this.controls.some(e=>e.enabled&&n(e))}_setUpControls(){this._forEachChild(n=>this._registerControl(n))}_allControlsDisabled(){for(let n of this.controls)if(n.enabled)return!1;return this.controls.length>0||this.disabled}_registerControl(n){n.setParent(this),n._registerOnCollectionChange(this._onCollectionChange)}_find(n){return this.at(n)??null}};var hg=(()=>{class i extends xt{callSetDisabledState;get submitted(){return W(this._submittedReactive)}set submitted(e){this._submittedReactive.set(e)}_submitted=k(()=>this._submittedReactive());_submittedReactive=V(!1);_oldForm;_onCollectionChange=()=>this._updateDomValue();directives=[];constructor(e,t,r){super(),this.callSetDisabledState=r,this._setValidators(e),this._setAsyncValidators(t)}ngOnChanges(e){this.onChanges(e)}ngOnDestroy(){this.onDestroy()}onChanges(e){this._checkFormPresent(),e.hasOwnProperty("form")&&(this._updateValidators(),this._updateDomValue(),this._updateRegistrations(),this._oldForm=this.form)}onDestroy(){this.form&&(Ua(this.form,this),this.form._onCollectionChange===this._onCollectionChange&&this.form._registerOnCollectionChange(()=>{}))}get formDirective(){return this}get path(){return[]}addControl(e){let t=this.form.get(e.path);return Dr(t,e,this.callSetDisabledState),t.updateValueAndValidity({emitEvent:!1}),this.directives.push(e),t}getControl(e){return this.form.get(e.path)}removeControl(e){za(e.control||null,e,!1),lg(this.directives,e)}addFormGroup(e){this._setUpFormContainer(e)}removeFormGroup(e){this._cleanUpFormContainer(e)}getFormGroup(e){return this.form.get(e.path)}getFormArray(e){return this.form.get(e.path)}addFormArray(e){this._setUpFormContainer(e)}removeFormArray(e){this._cleanUpFormContainer(e)}updateModel(e,t){this.form.get(e.path).setValue(t)}onReset(){this.resetForm()}resetForm(e=void 0,t={}){this.form.reset(e,t),this._submittedReactive.set(!1)}onSubmit(e){return this.submitted=!0,mu(this.form,this.directives),this.ngSubmit.emit(e),this.form._events.next(new ja(this.control)),e?.target?.method==="dialog"}_updateDomValue(){this.directives.forEach(e=>{let t=e.control,r=this.form.get(e.path);t!==r&&(za(t||null,e),cg(r)&&(Dr(r,e,this.callSetDisabledState),e.control=r))}),this.form._updateTreeValidity({emitEvent:!1})}_setUpFormContainer(e){let t=this.form.get(e.path);uu(t,e),t.updateValueAndValidity({emitEvent:!1})}_cleanUpFormContainer(e){let t=this.form?.get(e.path);t&&og(t,e)&&t.updateValueAndValidity({emitEvent:!1})}_updateRegistrations(){this.form._registerOnCollectionChange(this._onCollectionChange),this._oldForm?._registerOnCollectionChange(()=>{})}_updateValidators(){Is(this.form,this),this._oldForm&&Ua(this._oldForm,this)}_checkFormPresent(){this.form}static \u0275fac=function(t){return new(t||i)(ie(Jt,10),ie(Mr,10),ie(yi,8))};static \u0275dir=E({type:i,features:[te,pe]})}return i})();var Fs=new M(""),fg={provide:Qe,useExisting:qe(()=>et)},et=(()=>{class i extends Qe{_ngModelWarningConfig;callSetDisabledState;viewModel;form;set isDisabled(e){}model;update=new S;static _ngModelWarningSentOnce=!1;_ngModelWarningSent=!1;constructor(e,t,r,a,o){super(),this._ngModelWarningConfig=a,this.callSetDisabledState=o,this._setValidators(e),this._setAsyncValidators(t),this.valueAccessor=Ts(this,r)}ngOnChanges(e){if(this._isControlChanged(e)){let t=e.form.previousValue;t&&za(t,this,!1),Dr(this.form,this,this.callSetDisabledState),this.form.updateValueAndValidity({emitEvent:!1})}Os(e,this.viewModel)&&(this.form.setValue(this.model),this.viewModel=this.model)}ngOnDestroy(){this.form&&za(this.form,this,!1)}get path(){return[]}get control(){return this.form}viewToModelUpdate(e){this.viewModel=e,this.update.emit(e)}_isControlChanged(e){return e.hasOwnProperty("form")}static \u0275fac=function(t){return new(t||i)(ie(Jt,10),ie(Mr,10),ie(dt,10),ie(Fs,8),ie(yi,8))};static \u0275dir=E({type:i,selectors:[["","formControl",""]],inputs:{form:[0,"formControl","form"],isDisabled:[0,"disabled","isDisabled"],model:[0,"ngModel","model"]},outputs:{update:"ngModelChange"},exportAs:["ngForm"],standalone:!1,features:[re([fg]),te,pe]})}return i})();var gg={provide:Qe,useExisting:qe(()=>Ps)},Ps=(()=>{class i extends Qe{_ngModelWarningConfig;_added=!1;viewModel;control;name=null;set isDisabled(e){}model;update=new S;static _ngModelWarningSentOnce=!1;_ngModelWarningSent=!1;constructor(e,t,r,a,o){super(),this._ngModelWarningConfig=o,this._parent=e,this._setValidators(t),this._setAsyncValidators(r),this.valueAccessor=Ts(this,a)}ngOnChanges(e){this._added||this._setUpControl(),Os(e,this.viewModel)&&(this.viewModel=this.model,this.formDirective.updateModel(this,this.model))}ngOnDestroy(){this.formDirective?.removeControl(this)}viewToModelUpdate(e){this.viewModel=e,this.update.emit(e)}get path(){return du(this.name==null?this.name:this.name.toString(),this._parent)}get formDirective(){return this._parent?this._parent.formDirective:null}_setUpControl(){this.control=this.formDirective.addControl(this),this._added=!0}static \u0275fac=function(t){return new(t||i)(ie(xt,13),ie(Jt,10),ie(Mr,10),ie(dt,10),ie(Fs,8))};static \u0275dir=E({type:i,selectors:[["","formControlName",""]],inputs:{name:[0,"formControlName","name"],isDisabled:[0,"disabled","isDisabled"],model:[0,"ngModel","model"]},outputs:{update:"ngModelChange"},standalone:!1,features:[re([gg]),te,pe]})}return i})();var _g={provide:xt,useExisting:qe(()=>$a)},$a=(()=>{class i extends hg{form=null;ngSubmit=new S;get control(){return this.form}static \u0275fac=(()=>{let e;return function(r){return(e||(e=bt(i)))(r||i)}})();static \u0275dir=E({type:i,selectors:[["","formGroup",""]],hostBindings:function(t,r){t&1&&T("submit",function(o){return r.onSubmit(o)})("reset",function(){return r.onReset()})},inputs:{form:[0,"formGroup","form"]},outputs:{ngSubmit:"ngSubmit"},exportAs:["ngForm"],standalone:!1,features:[re([_g]),te]})}return i})();function bg(i){return typeof i=="number"?i:parseInt(i,10)}var pu=(()=>{class i{_validator=Ra;_onChange;_enabled;ngOnChanges(e){if(this.inputName in e){let t=this.normalizeInput(e[this.inputName].currentValue);this._enabled=this.enabled(t),this._validator=this._enabled?this.createValidator(t):Ra,this._onChange?.()}}validate(e){return this._validator(e)}registerOnValidatorChange(e){this._onChange=e}enabled(e){return e!=null}static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,features:[pe]})}return i})();var vg={provide:Jt,useExisting:qe(()=>ct),multi:!0};var ct=(()=>{class i extends pu{required;inputName="required";normalizeInput=Y;createValidator=e=>Zc;enabled(e){return e}static \u0275fac=(()=>{let e;return function(r){return(e||(e=bt(i)))(r||i)}})();static \u0275dir=E({type:i,selectors:[["","required","","formControlName","",3,"type","checkbox"],["","required","","formControl","",3,"type","checkbox"],["","required","","ngModel","",3,"type","checkbox"]],hostVars:1,hostBindings:function(t,r){t&2&&B("required",r._enabled?"":null)},inputs:{required:"required"},standalone:!1,features:[re([vg]),te]})}return i})();var yg={provide:Jt,useExisting:qe(()=>Cg),multi:!0},Cg=(()=>{class i extends pu{maxlength;inputName="maxlength";normalizeInput=e=>bg(e);createValidator=e=>Qc(e);static \u0275fac=(()=>{let e;return function(r){return(e||(e=bt(i)))(r||i)}})();static \u0275dir=E({type:i,selectors:[["","maxlength","","formControlName",""],["","maxlength","","formControl",""],["","maxlength","","ngModel",""]],hostVars:1,hostBindings:function(t,r){t&2&&B("maxlength",r._enabled?r.maxlength:null)},inputs:{maxlength:"maxlength"},standalone:!1,features:[re([yg]),te]})}return i})();var hu=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275mod=$({type:i});static \u0275inj=G({})}return i})();function Gc(i){return!!i&&(i.asyncValidators!==void 0||i.validators!==void 0||i.updateOn!==void 0)}var o1=(()=>{class i{useNonNullable=!1;get nonNullable(){let e=new i;return e.useNonNullable=!0,e}group(e,t=null){let r=this._reduceControls(e),a={};return Gc(t)?a=t:t!==null&&(a.validators=t.validator,a.asyncValidators=t.asyncValidator),new zn(r,a)}record(e,t=null){let r=this._reduceControls(e);return new Ds(r,t)}control(e,t,r){let a={};return this.useNonNullable?(Gc(t)?a=t:(a.validators=t,a.asyncValidators=r),new Ge(e,q(g({},a),{nonNullable:!0}))):new Ge(e,t,r)}array(e,t,r){let a=e.map(o=>this._createControl(o));return new Wa(a,t,r)}_reduceControls(e){let t={};return Object.keys(e).forEach(r=>{t[r]=this._createControl(e[r])}),t}_createControl(e){if(e instanceof Ge)return e;if(e instanceof vn)return e;if(Array.isArray(e)){let t=e[0],r=e.length>1?e[1]:null,a=e.length>2?e[2]:null;return this.control(t,r,a)}else return this.control(e)}static \u0275fac=function(t){return new(t||i)};static \u0275prov=K({token:i,factory:i.\u0275fac,providedIn:"root"})}return i})();var yn=(()=>{class i{static withConfig(e){return{ngModule:i,providers:[{provide:yi,useValue:e.callSetDisabledState??qa}]}}static \u0275fac=function(t){return new(t||i)};static \u0275mod=$({type:i});static \u0275inj=G({imports:[hu]})}return i})(),nt=(()=>{class i{static withConfig(e){return{ngModule:i,providers:[{provide:Fs,useValue:e.warnOnNgModelWithFormControl??"always"},{provide:yi,useValue:e.callSetDisabledState??qa}]}}static \u0275fac=function(t){return new(t||i)};static \u0275mod=$({type:i});static \u0275inj=G({imports:[hu]})}return i})();var wr=(function(i){return i.YEAR="^[0-9]{4}$",i.YEAR_MONTH="^[0-9]{4}-[0-9]{2}$",i.YEAR_MONTH_DAY="^[0-9]{4}-[0-9]{2}-[0-9]{2}$",i.FULL_DATE="^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}$",i})(wr||{});var Rs=class{_box;_destroyed=new F;_resizeSubject=new F;_resizeObserver;_elementObservables=new Map;constructor(n){this._box=n,typeof ResizeObserver<"u"&&(this._resizeObserver=new ResizeObserver(e=>this._resizeSubject.next(e)))}observe(n){return this._elementObservables.has(n)||this._elementObservables.set(n,new Kn(e=>{let t=this._resizeSubject.subscribe(e);return this._resizeObserver?.observe(n,{box:this._box}),()=>{this._resizeObserver?.unobserve(n),t.unsubscribe(),this._elementObservables.delete(n)}}).pipe(De(e=>e.some(t=>t.target===n)),gd({bufferSize:1,refCount:!0}),ge(this._destroyed))),this._elementObservables.get(n)}destroy(){this._destroyed.next(),this._destroyed.complete(),this._resizeSubject.complete(),this._elementObservables.clear()}},fu=(()=>{class i{_cleanupErrorListener;_observers=new Map;_ngZone=l(X);constructor(){typeof ResizeObserver<"u"}ngOnDestroy(){for(let[,e]of this._observers)e.destroy();this._observers.clear(),this._cleanupErrorListener?.()}observe(e,t){let r=t?.box||"content-box";return this._observers.has(r)||this._observers.set(r,new Rs(r)),this._observers.get(r).observe(e)}static \u0275fac=function(t){return new(t||i)};static \u0275prov=K({token:i,factory:i.\u0275fac,providedIn:"root"})}return i})();var xg=["notch"],Dg=["matFormFieldNotchedOutline",""],Mg=["*"],gu=["iconPrefixContainer"],_u=["textPrefixContainer"],bu=["iconSuffixContainer"],vu=["textSuffixContainer"],wg=["textField"],Eg=["*",[["mat-label"]],[["","matPrefix",""],["","matIconPrefix",""]],[["","matTextPrefix",""]],[["","matTextSuffix",""]],[["","matSuffix",""],["","matIconSuffix",""]],[["mat-error"],["","matError",""]],[["mat-hint",3,"align","end"]],[["mat-hint","align","end"]]],kg=["*","mat-label","[matPrefix], [matIconPrefix]","[matTextPrefix]","[matTextSuffix]","[matSuffix], [matIconSuffix]","mat-error, [matError]","mat-hint:not([align='end'])","mat-hint[align='end']"];function Ag(i,n){i&1&&_(0,"span",21)}function Sg(i,n){if(i&1&&(p(0,"label",20),Z(1,1),v(2,Ag,1,0,"span",21),h()),i&2){let e=m(2);f("floating",e._shouldLabelFloat())("monitorResize",e._hasOutline())("id",e._labelId),B("for",e._control.disableAutomaticLabeling?null:e._control.id),c(2),y(!e.hideRequiredMarker&&e._control.required?2:-1)}}function Ig(i,n){if(i&1&&v(0,Sg,3,5,"label",20),i&2){let e=m();y(e._hasFloatingLabel()?0:-1)}}function Og(i,n){i&1&&_(0,"div",7)}function Tg(i,n){}function Fg(i,n){if(i&1&&Oe(0,Tg,0,0,"ng-template",13),i&2){m(2);let e=Ke(1);f("ngTemplateOutlet",e)}}function Pg(i,n){if(i&1&&(p(0,"div",9),v(1,Fg,1,1,null,13),h()),i&2){let e=m();f("matFormFieldNotchedOutlineOpen",e._shouldLabelFloat()),c(),y(e._forceDisplayInfixLabel()?-1:1)}}function Rg(i,n){i&1&&(p(0,"div",10,2),Z(2,2),h())}function Vg(i,n){i&1&&(p(0,"div",11,3),Z(2,3),h())}function Ng(i,n){}function Lg(i,n){if(i&1&&Oe(0,Ng,0,0,"ng-template",13),i&2){m();let e=Ke(1);f("ngTemplateOutlet",e)}}function Bg(i,n){i&1&&(p(0,"div",14,4),Z(2,4),h())}function jg(i,n){i&1&&(p(0,"div",15,5),Z(2,5),h())}function zg(i,n){i&1&&_(0,"div",16)}function Hg(i,n){i&1&&(p(0,"div",18),Z(1,6),h())}function Ug(i,n){if(i&1&&(p(0,"mat-hint",22),P(1),h()),i&2){let e=m(2);f("id",e._hintLabelId),c(),ce(e.hintLabel)}}function Wg(i,n){if(i&1&&(p(0,"div",19),v(1,Ug,2,2,"mat-hint",22),Z(2,7),_(3,"div",23),Z(4,8),h()),i&2){let e=m();c(),y(e.hintLabel?1:-1)}}var Ci=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,selectors:[["mat-label"]]})}return i})(),Eu=new M("MatError"),Za=(()=>{class i{id=l(Re).getId("mat-mdc-error-");constructor(){}static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,selectors:[["mat-error"],["","matError",""]],hostAttrs:[1,"mat-mdc-form-field-error","mat-mdc-form-field-bottom-align"],hostVars:1,hostBindings:function(t,r){t&2&&He("id",r.id)},inputs:{id:"id"},features:[re([{provide:Eu,useExisting:i}])]})}return i})(),xi=(()=>{class i{align="start";id=l(Re).getId("mat-mdc-hint-");static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,selectors:[["mat-hint"]],hostAttrs:[1,"mat-mdc-form-field-hint","mat-mdc-form-field-bottom-align"],hostVars:4,hostBindings:function(t,r){t&2&&(He("id",r.id),B("align",null),w("mat-mdc-form-field-hint-end",r.align==="end"))},inputs:{align:"align",id:"id"}})}return i})(),Yg=new M("MatPrefix");var ku=new M("MatSuffix"),Qa=(()=>{class i{set _isTextSelector(e){this._isText=!0}_isText=!1;static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,selectors:[["","matSuffix",""],["","matIconSuffix",""],["","matTextSuffix",""]],inputs:{_isTextSelector:[0,"matTextSuffix","_isTextSelector"]},features:[re([{provide:ku,useExisting:i}])]})}return i})(),Au=new M("FloatingLabelParent"),yu=(()=>{class i{_elementRef=l(N);get floating(){return this._floating}set floating(e){this._floating=e,this.monitorResize&&this._handleResize()}_floating=!1;get monitorResize(){return this._monitorResize}set monitorResize(e){this._monitorResize=e,this._monitorResize?this._subscribeToResize():this._resizeSubscription.unsubscribe()}_monitorResize=!1;_resizeObserver=l(fu);_ngZone=l(X);_parent=l(Au);_resizeSubscription=new oe;constructor(){}ngOnDestroy(){this._resizeSubscription.unsubscribe()}getWidth(){return qg(this._elementRef.nativeElement)}get element(){return this._elementRef.nativeElement}_handleResize(){setTimeout(()=>this._parent._handleLabelResized())}_subscribeToResize(){this._resizeSubscription.unsubscribe(),this._ngZone.runOutsideAngular(()=>{this._resizeSubscription=this._resizeObserver.observe(this._elementRef.nativeElement,{box:"border-box"}).subscribe(()=>this._handleResize())})}static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,selectors:[["label","matFormFieldFloatingLabel",""]],hostAttrs:[1,"mdc-floating-label","mat-mdc-floating-label"],hostVars:2,hostBindings:function(t,r){t&2&&w("mdc-floating-label--float-above",r.floating)},inputs:{floating:"floating",monitorResize:"monitorResize"}})}return i})();function qg(i){let n=i;if(n.offsetParent!==null)return n.scrollWidth;let e=n.cloneNode(!0);e.style.setProperty("position","absolute"),e.style.setProperty("transform","translate(-9999px, -9999px)"),document.documentElement.appendChild(e);let t=e.scrollWidth;return e.remove(),t}var Cu="mdc-line-ripple--active",Ka="mdc-line-ripple--deactivating",xu=(()=>{class i{_elementRef=l(N);_cleanupTransitionEnd;constructor(){let e=l(X),t=l(we);e.runOutsideAngular(()=>{this._cleanupTransitionEnd=t.listen(this._elementRef.nativeElement,"transitionend",this._handleTransitionEnd)})}activate(){let e=this._elementRef.nativeElement.classList;e.remove(Ka),e.add(Cu)}deactivate(){this._elementRef.nativeElement.classList.add(Ka)}_handleTransitionEnd=e=>{let t=this._elementRef.nativeElement.classList,r=t.contains(Ka);e.propertyName==="opacity"&&r&&t.remove(Cu,Ka)};ngOnDestroy(){this._cleanupTransitionEnd()}static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,selectors:[["div","matFormFieldLineRipple",""]],hostAttrs:[1,"mdc-line-ripple"]})}return i})(),Du=(()=>{class i{_elementRef=l(N);_ngZone=l(X);open=!1;_notch;ngAfterViewInit(){let e=this._elementRef.nativeElement,t=e.querySelector(".mdc-floating-label");t?(e.classList.add("mdc-notched-outline--upgraded"),typeof requestAnimationFrame=="function"&&(t.style.transitionDuration="0s",this._ngZone.runOutsideAngular(()=>{requestAnimationFrame(()=>t.style.transitionDuration="")}))):e.classList.add("mdc-notched-outline--no-label")}_setNotchWidth(e){let t=this._notch.nativeElement;!this.open||!e?t.style.width="":t.style.width=`calc(${e}px * var(--mat-mdc-form-field-floating-label-scale, 0.75) + 9px)`}_setMaxWidth(e){this._notch.nativeElement.style.setProperty("--mat-form-field-notch-max-width",`calc(100% - ${e}px)`)}static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["div","matFormFieldNotchedOutline",""]],viewQuery:function(t,r){if(t&1&&ke(xg,5),t&2){let a;z(a=H())&&(r._notch=a.first)}},hostAttrs:[1,"mdc-notched-outline"],hostVars:2,hostBindings:function(t,r){t&2&&w("mdc-notched-outline--notched",r.open)},inputs:{open:[0,"matFormFieldNotchedOutlineOpen","open"]},attrs:Dg,ngContentSelectors:Mg,decls:5,vars:0,consts:[["notch",""],[1,"mat-mdc-notch-piece","mdc-notched-outline__leading"],[1,"mat-mdc-notch-piece","mdc-notched-outline__notch"],[1,"mat-mdc-notch-piece","mdc-notched-outline__trailing"]],template:function(t,r){t&1&&(Ee(),Pe(0,"div",1),le(1,"div",2,0),Z(3),he(),Pe(4,"div",3))},encapsulation:2,changeDetection:0})}return i})(),Xa=(()=>{class i{value=null;stateChanges;id;placeholder;ngControl=null;focused=!1;empty=!1;shouldLabelFloat=!1;required=!1;disabled=!1;errorState=!1;controlType;autofilled;userAriaDescribedBy;disableAutomaticLabeling;describedByIds;static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i})}return i})();var Er=new M("MatFormField"),Gg=new M("MAT_FORM_FIELD_DEFAULT_OPTIONS"),Mu="fill",$g="auto",wu="fixed",Kg="translateY(-50%)",kr=(()=>{class i{_elementRef=l(N);_changeDetectorRef=l(ne);_platform=l(be);_idGenerator=l(Re);_ngZone=l(X);_defaults=l(Gg,{optional:!0});_currentDirection;_textField;_iconPrefixContainer;_textPrefixContainer;_iconSuffixContainer;_textSuffixContainer;_floatingLabel;_notchedOutline;_lineRipple;_iconPrefixContainerSignal=St("iconPrefixContainer");_textPrefixContainerSignal=St("textPrefixContainer");_iconSuffixContainerSignal=St("iconSuffixContainer");_textSuffixContainerSignal=St("textSuffixContainer");_prefixSuffixContainers=k(()=>[this._iconPrefixContainerSignal(),this._textPrefixContainerSignal(),this._iconSuffixContainerSignal(),this._textSuffixContainerSignal()].map(e=>e?.nativeElement).filter(e=>e!==void 0));_formFieldControl;_prefixChildren;_suffixChildren;_errorChildren;_hintChildren;_labelChild=Ad(Ci);get hideRequiredMarker(){return this._hideRequiredMarker}set hideRequiredMarker(e){this._hideRequiredMarker=yt(e)}_hideRequiredMarker=!1;color="primary";get floatLabel(){return this._floatLabel||this._defaults?.floatLabel||$g}set floatLabel(e){e!==this._floatLabel&&(this._floatLabel=e,this._changeDetectorRef.markForCheck())}_floatLabel;get appearance(){return this._appearanceSignal()}set appearance(e){let t=e||this._defaults?.appearance||Mu;this._appearanceSignal.set(t)}_appearanceSignal=V(Mu);get subscriptSizing(){return this._subscriptSizing||this._defaults?.subscriptSizing||wu}set subscriptSizing(e){this._subscriptSizing=e||this._defaults?.subscriptSizing||wu}_subscriptSizing=null;get hintLabel(){return this._hintLabel}set hintLabel(e){this._hintLabel=e,this._processHints()}_hintLabel="";_hasIconPrefix=!1;_hasTextPrefix=!1;_hasIconSuffix=!1;_hasTextSuffix=!1;_labelId=this._idGenerator.getId("mat-mdc-form-field-label-");_hintLabelId=this._idGenerator.getId("mat-mdc-hint-");_describedByIds;get _control(){return this._explicitFormFieldControl||this._formFieldControl}set _control(e){this._explicitFormFieldControl=e}_destroyed=new F;_isFocused=null;_explicitFormFieldControl;_previousControl=null;_previousControlValidatorFn=null;_stateChanges;_valueChanges;_describedByChanges;_outlineLabelOffsetResizeObserver=null;_animationsDisabled=ye();constructor(){let e=this._defaults,t=l(Ze);e&&(e.appearance&&(this.appearance=e.appearance),this._hideRequiredMarker=!!e?.hideRequiredMarker,e.color&&(this.color=e.color)),Lt(()=>this._currentDirection=t.valueSignal()),this._syncOutlineLabelOffset()}ngAfterViewInit(){this._updateFocusState(),this._animationsDisabled||this._ngZone.runOutsideAngular(()=>{setTimeout(()=>{this._elementRef.nativeElement.classList.add("mat-form-field-animations-enabled")},300)}),this._changeDetectorRef.detectChanges()}ngAfterContentInit(){this._assertFormFieldControl(),this._initializeSubscript(),this._initializePrefixAndSuffix()}ngAfterContentChecked(){this._assertFormFieldControl(),this._control!==this._previousControl&&(this._initializeControl(this._previousControl),this._control.ngControl&&this._control.ngControl.control&&(this._previousControlValidatorFn=this._control.ngControl.control.validator),this._previousControl=this._control),this._control.ngControl&&this._control.ngControl.control&&this._control.ngControl.control.validator!==this._previousControlValidatorFn&&this._changeDetectorRef.markForCheck()}ngOnDestroy(){this._outlineLabelOffsetResizeObserver?.disconnect(),this._stateChanges?.unsubscribe(),this._valueChanges?.unsubscribe(),this._describedByChanges?.unsubscribe(),this._destroyed.next(),this._destroyed.complete()}getLabelId=k(()=>this._hasFloatingLabel()?this._labelId:null);getConnectedOverlayOrigin(){return this._textField||this._elementRef}_animateAndLockLabel(){this._hasFloatingLabel()&&(this.floatLabel="always")}_initializeControl(e){let t=this._control,r="mat-mdc-form-field-type-";e&&this._elementRef.nativeElement.classList.remove(r+e.controlType),t.controlType&&this._elementRef.nativeElement.classList.add(r+t.controlType),this._stateChanges?.unsubscribe(),this._stateChanges=t.stateChanges.subscribe(()=>{this._updateFocusState(),this._changeDetectorRef.markForCheck()}),this._describedByChanges?.unsubscribe(),this._describedByChanges=t.stateChanges.pipe(Le([void 0,void 0]),Fe(()=>[t.errorState,t.userAriaDescribedBy]),hd(),De(([[a,o],[s,d]])=>a!==s||o!==d)).subscribe(()=>this._syncDescribedByIds()),this._valueChanges?.unsubscribe(),t.ngControl&&t.ngControl.valueChanges&&(this._valueChanges=t.ngControl.valueChanges.pipe(ge(this._destroyed)).subscribe(()=>this._changeDetectorRef.markForCheck()))}_checkPrefixAndSuffixTypes(){this._hasIconPrefix=!!this._prefixChildren.find(e=>!e._isText),this._hasTextPrefix=!!this._prefixChildren.find(e=>e._isText),this._hasIconSuffix=!!this._suffixChildren.find(e=>!e._isText),this._hasTextSuffix=!!this._suffixChildren.find(e=>e._isText)}_initializePrefixAndSuffix(){this._checkPrefixAndSuffixTypes(),$e(this._prefixChildren.changes,this._suffixChildren.changes).subscribe(()=>{this._checkPrefixAndSuffixTypes(),this._changeDetectorRef.markForCheck()})}_initializeSubscript(){this._hintChildren.changes.subscribe(()=>{this._processHints(),this._changeDetectorRef.markForCheck()}),this._errorChildren.changes.subscribe(()=>{this._syncDescribedByIds(),this._changeDetectorRef.markForCheck()}),this._validateHints(),this._syncDescribedByIds()}_assertFormFieldControl(){this._control}_updateFocusState(){let e=this._control.focused;e&&!this._isFocused?(this._isFocused=!0,this._lineRipple?.activate()):!e&&(this._isFocused||this._isFocused===null)&&(this._isFocused=!1,this._lineRipple?.deactivate()),this._elementRef.nativeElement.classList.toggle("mat-focused",e),this._textField?.nativeElement.classList.toggle("mdc-text-field--focused",e)}_syncOutlineLabelOffset(){da({earlyRead:()=>{if(this._appearanceSignal()!=="outline")return this._outlineLabelOffsetResizeObserver?.disconnect(),null;if(globalThis.ResizeObserver){this._outlineLabelOffsetResizeObserver||=new globalThis.ResizeObserver(()=>{this._writeOutlinedLabelStyles(this._getOutlinedLabelOffset())});for(let e of this._prefixSuffixContainers())this._outlineLabelOffsetResizeObserver.observe(e,{box:"border-box"})}return this._getOutlinedLabelOffset()},write:e=>this._writeOutlinedLabelStyles(e())})}_shouldAlwaysFloat(){return this.floatLabel==="always"}_hasOutline(){return this.appearance==="outline"}_forceDisplayInfixLabel(){return!this._platform.isBrowser&&this._prefixChildren.length&&!this._shouldLabelFloat()}_hasFloatingLabel=k(()=>!!this._labelChild());_shouldLabelFloat(){return this._hasFloatingLabel()?this._control.shouldLabelFloat||this._shouldAlwaysFloat():!1}_shouldForward(e){let t=this._control?this._control.ngControl:null;return t&&t[e]}_getSubscriptMessageType(){return this._errorChildren&&this._errorChildren.length>0&&this._control.errorState?"error":"hint"}_handleLabelResized(){this._refreshOutlineNotchWidth()}_refreshOutlineNotchWidth(){!this._hasOutline()||!this._floatingLabel||!this._shouldLabelFloat()?this._notchedOutline?._setNotchWidth(0):this._notchedOutline?._setNotchWidth(this._floatingLabel.getWidth())}_processHints(){this._validateHints(),this._syncDescribedByIds()}_validateHints(){this._hintChildren}_syncDescribedByIds(){if(this._control){let e=[];if(this._control.userAriaDescribedBy&&typeof this._control.userAriaDescribedBy=="string"&&e.push(...this._control.userAriaDescribedBy.split(" ")),this._getSubscriptMessageType()==="hint"){let a=this._hintChildren?this._hintChildren.find(s=>s.align==="start"):null,o=this._hintChildren?this._hintChildren.find(s=>s.align==="end"):null;a?e.push(a.id):this._hintLabel&&e.push(this._hintLabelId),o&&e.push(o.id)}else this._errorChildren&&e.push(...this._errorChildren.map(a=>a.id));let t=this._control.describedByIds,r;if(t){let a=this._describedByIds||e;r=e.concat(t.filter(o=>o&&!a.includes(o)))}else r=e;this._control.setDescribedByIds(r),this._describedByIds=e}}_getOutlinedLabelOffset(){if(!this._hasOutline()||!this._floatingLabel)return null;if(!this._iconPrefixContainer&&!this._textPrefixContainer)return["",null];if(!this._isAttachedToDom())return null;let e=this._iconPrefixContainer?.nativeElement,t=this._textPrefixContainer?.nativeElement,r=this._iconSuffixContainer?.nativeElement,a=this._textSuffixContainer?.nativeElement,o=e?.getBoundingClientRect().width??0,s=t?.getBoundingClientRect().width??0,d=r?.getBoundingClientRect().width??0,u=a?.getBoundingClientRect().width??0,b=this._currentDirection==="rtl"?"-1":"1",A=`${o+s}px`,I=`calc(${b} * (${A} + var(--mat-mdc-form-field-label-offset-x, 0px)))`,me=`var(--mat-mdc-form-field-label-transform, ${Kg} translateX(${I}))`,Se=o+s+d+u;return[me,Se]}_writeOutlinedLabelStyles(e){if(e!==null){let[t,r]=e;this._floatingLabel&&(this._floatingLabel.element.style.transform=t),r!==null&&this._notchedOutline?._setMaxWidth(r)}}_isAttachedToDom(){let e=this._elementRef.nativeElement;if(e.getRootNode){let t=e.getRootNode();return t&&t!==e}return document.documentElement.contains(e)}static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["mat-form-field"]],contentQueries:function(t,r,a){if(t&1&&(xd(a,r._labelChild,Ci,5),sn(a,Xa,5)(a,Yg,5)(a,ku,5)(a,Eu,5)(a,xi,5)),t&2){Tn();let o;z(o=H())&&(r._formFieldControl=o.first),z(o=H())&&(r._prefixChildren=o),z(o=H())&&(r._suffixChildren=o),z(o=H())&&(r._errorChildren=o),z(o=H())&&(r._hintChildren=o)}},viewQuery:function(t,r){if(t&1&&(ni(r._iconPrefixContainerSignal,gu,5)(r._textPrefixContainerSignal,_u,5)(r._iconSuffixContainerSignal,bu,5)(r._textSuffixContainerSignal,vu,5),ke(wg,5)(gu,5)(_u,5)(bu,5)(vu,5)(yu,5)(Du,5)(xu,5)),t&2){Tn(4);let a;z(a=H())&&(r._textField=a.first),z(a=H())&&(r._iconPrefixContainer=a.first),z(a=H())&&(r._textPrefixContainer=a.first),z(a=H())&&(r._iconSuffixContainer=a.first),z(a=H())&&(r._textSuffixContainer=a.first),z(a=H())&&(r._floatingLabel=a.first),z(a=H())&&(r._notchedOutline=a.first),z(a=H())&&(r._lineRipple=a.first)}},hostAttrs:[1,"mat-mdc-form-field"],hostVars:38,hostBindings:function(t,r){t&2&&w("mat-mdc-form-field-label-always-float",r._shouldAlwaysFloat())("mat-mdc-form-field-has-icon-prefix",r._hasIconPrefix)("mat-mdc-form-field-has-icon-suffix",r._hasIconSuffix)("mat-form-field-invalid",r._control.errorState)("mat-form-field-disabled",r._control.disabled)("mat-form-field-autofilled",r._control.autofilled)("mat-form-field-appearance-fill",r.appearance=="fill")("mat-form-field-appearance-outline",r.appearance=="outline")("mat-form-field-hide-placeholder",r._hasFloatingLabel()&&!r._shouldLabelFloat())("mat-primary",r.color!=="accent"&&r.color!=="warn")("mat-accent",r.color==="accent")("mat-warn",r.color==="warn")("ng-untouched",r._shouldForward("untouched"))("ng-touched",r._shouldForward("touched"))("ng-pristine",r._shouldForward("pristine"))("ng-dirty",r._shouldForward("dirty"))("ng-valid",r._shouldForward("valid"))("ng-invalid",r._shouldForward("invalid"))("ng-pending",r._shouldForward("pending"))},inputs:{hideRequiredMarker:"hideRequiredMarker",color:"color",floatLabel:"floatLabel",appearance:"appearance",subscriptSizing:"subscriptSizing",hintLabel:"hintLabel"},exportAs:["matFormField"],features:[re([{provide:Er,useExisting:i},{provide:Au,useExisting:i}])],ngContentSelectors:kg,decls:18,vars:21,consts:[["labelTemplate",""],["textField",""],["iconPrefixContainer",""],["textPrefixContainer",""],["textSuffixContainer",""],["iconSuffixContainer",""],[1,"mat-mdc-text-field-wrapper","mdc-text-field",3,"click"],[1,"mat-mdc-form-field-focus-overlay"],[1,"mat-mdc-form-field-flex"],["matFormFieldNotchedOutline","",3,"matFormFieldNotchedOutlineOpen"],[1,"mat-mdc-form-field-icon-prefix"],[1,"mat-mdc-form-field-text-prefix"],[1,"mat-mdc-form-field-infix"],[3,"ngTemplateOutlet"],[1,"mat-mdc-form-field-text-suffix"],[1,"mat-mdc-form-field-icon-suffix"],["matFormFieldLineRipple",""],["aria-atomic","true","aria-live","polite",1,"mat-mdc-form-field-subscript-wrapper","mat-mdc-form-field-bottom-align"],[1,"mat-mdc-form-field-error-wrapper"],[1,"mat-mdc-form-field-hint-wrapper"],["matFormFieldFloatingLabel","",3,"floating","monitorResize","id"],["aria-hidden","true",1,"mat-mdc-form-field-required-marker","mdc-floating-label--required"],[3,"id"],[1,"mat-mdc-form-field-hint-spacer"]],template:function(t,r){if(t&1&&(Ee(Eg),Oe(0,Ig,1,1,"ng-template",null,0,Qt),p(2,"div",6,1),T("click",function(o){return r._control.onContainerClick(o)}),v(4,Og,1,0,"div",7),p(5,"div",8),v(6,Pg,2,2,"div",9),v(7,Rg,3,0,"div",10),v(8,Vg,3,0,"div",11),p(9,"div",12),v(10,Lg,1,1,null,13),Z(11),h(),v(12,Bg,3,0,"div",14),v(13,jg,3,0,"div",15),h(),v(14,zg,1,0,"div",16),h(),p(15,"div",17),v(16,Hg,2,0,"div",18)(17,Wg,5,1,"div",19),h()),t&2){let a;c(2),w("mdc-text-field--filled",!r._hasOutline())("mdc-text-field--outlined",r._hasOutline())("mdc-text-field--no-label",!r._hasFloatingLabel())("mdc-text-field--disabled",r._control.disabled)("mdc-text-field--invalid",r._control.errorState),c(2),y(!r._hasOutline()&&!r._control.disabled?4:-1),c(2),y(r._hasOutline()?6:-1),c(),y(r._hasIconPrefix?7:-1),c(),y(r._hasTextPrefix?8:-1),c(2),y(!r._hasOutline()||r._forceDisplayInfixLabel()?10:-1),c(2),y(r._hasTextSuffix?12:-1),c(),y(r._hasIconSuffix?13:-1),c(),y(r._hasOutline()?-1:14),c(),w("mat-mdc-form-field-subscript-dynamic-size",r.subscriptSizing==="dynamic");let o=r._getSubscriptMessageType();c(),y((a=o)==="error"?16:a==="hint"?17:-1)}},dependencies:[yu,Du,ln,xu,xi],styles:[`.mdc-text-field {
  display: inline-flex;
  align-items: baseline;
  padding: 0 16px;
  position: relative;
  box-sizing: border-box;
  overflow: hidden;
  will-change: opacity, transform, color;
  border-top-left-radius: 4px;
  border-top-right-radius: 4px;
  border-bottom-right-radius: 0;
  border-bottom-left-radius: 0;
}

.mdc-text-field__input {
  width: 100%;
  min-width: 0;
  border: none;
  border-radius: 0;
  background: none;
  padding: 0;
  -moz-appearance: none;
  -webkit-appearance: none;
  height: 28px;
}
.mdc-text-field__input::-webkit-calendar-picker-indicator, .mdc-text-field__input::-webkit-search-cancel-button {
  display: none;
}
.mdc-text-field__input::-ms-clear {
  display: none;
}
.mdc-text-field__input:focus {
  outline: none;
}
.mdc-text-field__input:invalid {
  box-shadow: none;
}
.mdc-text-field__input::placeholder {
  opacity: 0;
}
.mdc-text-field__input::-moz-placeholder {
  opacity: 0;
}
.mdc-text-field__input::-webkit-input-placeholder {
  opacity: 0;
}
.mdc-text-field__input:-ms-input-placeholder {
  opacity: 0;
}
.mdc-text-field--no-label .mdc-text-field__input::placeholder, .mdc-text-field--focused .mdc-text-field__input::placeholder {
  opacity: 1;
}
.mdc-text-field--no-label .mdc-text-field__input::-moz-placeholder, .mdc-text-field--focused .mdc-text-field__input::-moz-placeholder {
  opacity: 1;
}
.mdc-text-field--no-label .mdc-text-field__input::-webkit-input-placeholder, .mdc-text-field--focused .mdc-text-field__input::-webkit-input-placeholder {
  opacity: 1;
}
.mdc-text-field--no-label .mdc-text-field__input:-ms-input-placeholder, .mdc-text-field--focused .mdc-text-field__input:-ms-input-placeholder {
  opacity: 1;
}
.mdc-text-field--disabled:not(.mdc-text-field--no-label) .mdc-text-field__input.mat-mdc-input-disabled-interactive::placeholder {
  opacity: 0;
}
.mdc-text-field--disabled:not(.mdc-text-field--no-label) .mdc-text-field__input.mat-mdc-input-disabled-interactive::-moz-placeholder {
  opacity: 0;
}
.mdc-text-field--disabled:not(.mdc-text-field--no-label) .mdc-text-field__input.mat-mdc-input-disabled-interactive::-webkit-input-placeholder {
  opacity: 0;
}
.mdc-text-field--disabled:not(.mdc-text-field--no-label) .mdc-text-field__input.mat-mdc-input-disabled-interactive:-ms-input-placeholder {
  opacity: 0;
}
.mdc-text-field--outlined .mdc-text-field__input, .mdc-text-field--filled.mdc-text-field--no-label .mdc-text-field__input {
  height: 100%;
}
.mdc-text-field--outlined .mdc-text-field__input {
  display: flex;
  border: none !important;
  background-color: transparent;
}
.mdc-text-field--disabled .mdc-text-field__input {
  pointer-events: auto;
}
.mdc-text-field--filled:not(.mdc-text-field--disabled) .mdc-text-field__input {
  color: var(--mat-form-field-filled-input-text-color, var(--mat-sys-on-surface));
  caret-color: var(--mat-form-field-filled-caret-color, var(--mat-sys-primary));
}
.mdc-text-field--filled:not(.mdc-text-field--disabled) .mdc-text-field__input::placeholder {
  color: var(--mat-form-field-filled-input-text-placeholder-color, var(--mat-sys-on-surface-variant));
}
.mdc-text-field--filled:not(.mdc-text-field--disabled) .mdc-text-field__input::-moz-placeholder {
  color: var(--mat-form-field-filled-input-text-placeholder-color, var(--mat-sys-on-surface-variant));
}
.mdc-text-field--filled:not(.mdc-text-field--disabled) .mdc-text-field__input::-webkit-input-placeholder {
  color: var(--mat-form-field-filled-input-text-placeholder-color, var(--mat-sys-on-surface-variant));
}
.mdc-text-field--filled:not(.mdc-text-field--disabled) .mdc-text-field__input:-ms-input-placeholder {
  color: var(--mat-form-field-filled-input-text-placeholder-color, var(--mat-sys-on-surface-variant));
}
.mdc-text-field--outlined:not(.mdc-text-field--disabled) .mdc-text-field__input {
  color: var(--mat-form-field-outlined-input-text-color, var(--mat-sys-on-surface));
  caret-color: var(--mat-form-field-outlined-caret-color, var(--mat-sys-primary));
}
.mdc-text-field--outlined:not(.mdc-text-field--disabled) .mdc-text-field__input::placeholder {
  color: var(--mat-form-field-outlined-input-text-placeholder-color, var(--mat-sys-on-surface-variant));
}
.mdc-text-field--outlined:not(.mdc-text-field--disabled) .mdc-text-field__input::-moz-placeholder {
  color: var(--mat-form-field-outlined-input-text-placeholder-color, var(--mat-sys-on-surface-variant));
}
.mdc-text-field--outlined:not(.mdc-text-field--disabled) .mdc-text-field__input::-webkit-input-placeholder {
  color: var(--mat-form-field-outlined-input-text-placeholder-color, var(--mat-sys-on-surface-variant));
}
.mdc-text-field--outlined:not(.mdc-text-field--disabled) .mdc-text-field__input:-ms-input-placeholder {
  color: var(--mat-form-field-outlined-input-text-placeholder-color, var(--mat-sys-on-surface-variant));
}
.mdc-text-field--filled.mdc-text-field--invalid:not(.mdc-text-field--disabled) .mdc-text-field__input {
  caret-color: var(--mat-form-field-filled-error-caret-color, var(--mat-sys-error));
}
.mdc-text-field--outlined.mdc-text-field--invalid:not(.mdc-text-field--disabled) .mdc-text-field__input {
  caret-color: var(--mat-form-field-outlined-error-caret-color, var(--mat-sys-error));
}
.mdc-text-field--filled.mdc-text-field--disabled .mdc-text-field__input {
  color: var(--mat-form-field-filled-disabled-input-text-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}
.mdc-text-field--outlined.mdc-text-field--disabled .mdc-text-field__input {
  color: var(--mat-form-field-outlined-disabled-input-text-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}
@media (forced-colors: active) {
  .mdc-text-field--disabled .mdc-text-field__input {
    background-color: Window;
  }
}

.mdc-text-field--filled {
  height: 56px;
  border-bottom-right-radius: 0;
  border-bottom-left-radius: 0;
  border-top-left-radius: var(--mat-form-field-filled-container-shape, var(--mat-sys-corner-extra-small));
  border-top-right-radius: var(--mat-form-field-filled-container-shape, var(--mat-sys-corner-extra-small));
}
.mdc-text-field--filled:not(.mdc-text-field--disabled) {
  background-color: var(--mat-form-field-filled-container-color, var(--mat-sys-surface-variant));
}
.mdc-text-field--filled.mdc-text-field--disabled {
  background-color: var(--mat-form-field-filled-disabled-container-color, color-mix(in srgb, var(--mat-sys-on-surface) 4%, transparent));
}

.mdc-text-field--outlined {
  height: 56px;
  overflow: visible;
  padding-right: max(16px, var(--mat-form-field-outlined-container-shape, var(--mat-sys-corner-extra-small)));
  padding-left: max(16px, var(--mat-form-field-outlined-container-shape, var(--mat-sys-corner-extra-small)) + 4px);
}
[dir=rtl] .mdc-text-field--outlined {
  padding-right: max(16px, var(--mat-form-field-outlined-container-shape, var(--mat-sys-corner-extra-small)) + 4px);
  padding-left: max(16px, var(--mat-form-field-outlined-container-shape, var(--mat-sys-corner-extra-small)));
}

.mdc-floating-label {
  position: absolute;
  left: 0;
  transform-origin: left top;
  line-height: 1.15rem;
  text-align: left;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: text;
  overflow: hidden;
  will-change: transform;
}
[dir=rtl] .mdc-floating-label {
  right: 0;
  left: auto;
  transform-origin: right top;
  text-align: right;
}
.mdc-text-field .mdc-floating-label {
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
}
.mdc-notched-outline .mdc-floating-label {
  display: inline-block;
  position: relative;
  max-width: 100%;
}
.mdc-text-field--outlined .mdc-floating-label {
  left: 4px;
  right: auto;
}
[dir=rtl] .mdc-text-field--outlined .mdc-floating-label {
  left: auto;
  right: 4px;
}
.mdc-text-field--filled .mdc-floating-label {
  left: 16px;
  right: auto;
}
[dir=rtl] .mdc-text-field--filled .mdc-floating-label {
  left: auto;
  right: 16px;
}
.mdc-text-field--disabled .mdc-floating-label {
  cursor: default;
}
@media (forced-colors: active) {
  .mdc-text-field--disabled .mdc-floating-label {
    z-index: 1;
  }
}
.mdc-text-field--filled.mdc-text-field--no-label .mdc-floating-label {
  display: none;
}
.mdc-text-field--filled:not(.mdc-text-field--disabled) .mdc-floating-label {
  color: var(--mat-form-field-filled-label-text-color, var(--mat-sys-on-surface-variant));
}
.mdc-text-field--filled:not(.mdc-text-field--disabled).mdc-text-field--focused .mdc-floating-label {
  color: var(--mat-form-field-filled-focus-label-text-color, var(--mat-sys-primary));
}
.mdc-text-field--filled:not(.mdc-text-field--disabled):not(.mdc-text-field--focused):hover .mdc-floating-label {
  color: var(--mat-form-field-filled-hover-label-text-color, var(--mat-sys-on-surface-variant));
}
.mdc-text-field--filled.mdc-text-field--disabled .mdc-floating-label {
  color: var(--mat-form-field-filled-disabled-label-text-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}
.mdc-text-field--filled:not(.mdc-text-field--disabled).mdc-text-field--invalid .mdc-floating-label {
  color: var(--mat-form-field-filled-error-label-text-color, var(--mat-sys-error));
}
.mdc-text-field--filled:not(.mdc-text-field--disabled).mdc-text-field--invalid.mdc-text-field--focused .mdc-floating-label {
  color: var(--mat-form-field-filled-error-focus-label-text-color, var(--mat-sys-error));
}
.mdc-text-field--filled:not(.mdc-text-field--disabled).mdc-text-field--invalid:not(.mdc-text-field--disabled):hover .mdc-floating-label {
  color: var(--mat-form-field-filled-error-hover-label-text-color, var(--mat-sys-on-error-container));
}
.mdc-text-field--filled .mdc-floating-label {
  font-family: var(--mat-form-field-filled-label-text-font, var(--mat-sys-body-large-font));
  font-size: var(--mat-form-field-filled-label-text-size, var(--mat-sys-body-large-size));
  font-weight: var(--mat-form-field-filled-label-text-weight, var(--mat-sys-body-large-weight));
  letter-spacing: var(--mat-form-field-filled-label-text-tracking, var(--mat-sys-body-large-tracking));
}
.mdc-text-field--outlined:not(.mdc-text-field--disabled) .mdc-floating-label {
  color: var(--mat-form-field-outlined-label-text-color, var(--mat-sys-on-surface-variant));
}
.mdc-text-field--outlined:not(.mdc-text-field--disabled).mdc-text-field--focused .mdc-floating-label {
  color: var(--mat-form-field-outlined-focus-label-text-color, var(--mat-sys-primary));
}
.mdc-text-field--outlined:not(.mdc-text-field--disabled):not(.mdc-text-field--focused):hover .mdc-floating-label {
  color: var(--mat-form-field-outlined-hover-label-text-color, var(--mat-sys-on-surface));
}
.mdc-text-field--outlined.mdc-text-field--disabled .mdc-floating-label {
  color: var(--mat-form-field-outlined-disabled-label-text-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}
.mdc-text-field--outlined:not(.mdc-text-field--disabled).mdc-text-field--invalid .mdc-floating-label {
  color: var(--mat-form-field-outlined-error-label-text-color, var(--mat-sys-error));
}
.mdc-text-field--outlined:not(.mdc-text-field--disabled).mdc-text-field--invalid.mdc-text-field--focused .mdc-floating-label {
  color: var(--mat-form-field-outlined-error-focus-label-text-color, var(--mat-sys-error));
}
.mdc-text-field--outlined:not(.mdc-text-field--disabled).mdc-text-field--invalid:not(.mdc-text-field--disabled):hover .mdc-floating-label {
  color: var(--mat-form-field-outlined-error-hover-label-text-color, var(--mat-sys-on-error-container));
}
.mdc-text-field--outlined .mdc-floating-label {
  font-family: var(--mat-form-field-outlined-label-text-font, var(--mat-sys-body-large-font));
  font-size: var(--mat-form-field-outlined-label-text-size, var(--mat-sys-body-large-size));
  font-weight: var(--mat-form-field-outlined-label-text-weight, var(--mat-sys-body-large-weight));
  letter-spacing: var(--mat-form-field-outlined-label-text-tracking, var(--mat-sys-body-large-tracking));
}

.mdc-floating-label--float-above {
  cursor: auto;
  transform: translateY(-106%) scale(0.75);
}
.mdc-text-field--filled .mdc-floating-label--float-above {
  transform: translateY(-106%) scale(0.75);
}
.mdc-text-field--outlined .mdc-floating-label--float-above {
  transform: translateY(-37.25px) scale(1);
  font-size: 0.75rem;
}
.mdc-notched-outline .mdc-floating-label--float-above {
  text-overflow: clip;
}
.mdc-notched-outline--upgraded .mdc-floating-label--float-above {
  max-width: 133.3333333333%;
}
.mdc-text-field--outlined.mdc-notched-outline--upgraded .mdc-floating-label--float-above, .mdc-text-field--outlined .mdc-notched-outline--upgraded .mdc-floating-label--float-above {
  transform: translateY(-34.75px) scale(0.75);
}
.mdc-text-field--outlined.mdc-notched-outline--upgraded .mdc-floating-label--float-above, .mdc-text-field--outlined .mdc-notched-outline--upgraded .mdc-floating-label--float-above {
  font-size: 1rem;
}

.mdc-floating-label--required:not(.mdc-floating-label--hide-required-marker)::after {
  margin-left: 1px;
  margin-right: 0;
  content: "*";
}
[dir=rtl] .mdc-floating-label--required:not(.mdc-floating-label--hide-required-marker)::after {
  margin-left: 0;
  margin-right: 1px;
}

.mdc-notched-outline {
  display: flex;
  position: absolute;
  top: 0;
  right: 0;
  left: 0;
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  height: 100%;
  text-align: left;
  pointer-events: none;
}
[dir=rtl] .mdc-notched-outline {
  text-align: right;
}
.mdc-text-field--outlined .mdc-notched-outline {
  z-index: 1;
}

.mat-mdc-notch-piece {
  box-sizing: border-box;
  height: 100%;
  pointer-events: none;
  border: none;
  border-top: 1px solid;
  border-bottom: 1px solid;
}
.mdc-text-field--focused .mat-mdc-notch-piece {
  border-width: 2px;
}
.mdc-text-field--outlined:not(.mdc-text-field--disabled) .mat-mdc-notch-piece {
  border-color: var(--mat-form-field-outlined-outline-color, var(--mat-sys-outline));
  border-width: var(--mat-form-field-outlined-outline-width, 1px);
}
.mdc-text-field--outlined:not(.mdc-text-field--disabled):not(.mdc-text-field--focused):hover .mat-mdc-notch-piece {
  border-color: var(--mat-form-field-outlined-hover-outline-color, var(--mat-sys-on-surface));
}
.mdc-text-field--outlined:not(.mdc-text-field--disabled).mdc-text-field--focused .mat-mdc-notch-piece {
  border-color: var(--mat-form-field-outlined-focus-outline-color, var(--mat-sys-primary));
}
.mdc-text-field--outlined.mdc-text-field--disabled .mat-mdc-notch-piece {
  border-color: var(--mat-form-field-outlined-disabled-outline-color, color-mix(in srgb, var(--mat-sys-on-surface) 12%, transparent));
}
.mdc-text-field--outlined:not(.mdc-text-field--disabled).mdc-text-field--invalid .mat-mdc-notch-piece {
  border-color: var(--mat-form-field-outlined-error-outline-color, var(--mat-sys-error));
}
.mdc-text-field--outlined:not(.mdc-text-field--disabled).mdc-text-field--invalid:not(.mdc-text-field--focused):hover .mdc-notched-outline .mat-mdc-notch-piece {
  border-color: var(--mat-form-field-outlined-error-hover-outline-color, var(--mat-sys-on-error-container));
}
.mdc-text-field--outlined:not(.mdc-text-field--disabled).mdc-text-field--invalid.mdc-text-field--focused .mat-mdc-notch-piece {
  border-color: var(--mat-form-field-outlined-error-focus-outline-color, var(--mat-sys-error));
}
.mdc-text-field--outlined:not(.mdc-text-field--disabled).mdc-text-field--focused .mdc-notched-outline .mat-mdc-notch-piece {
  border-width: var(--mat-form-field-outlined-focus-outline-width, 2px);
}

.mdc-notched-outline__leading {
  border-left: 1px solid;
  border-right: none;
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
  border-top-left-radius: var(--mat-form-field-outlined-container-shape, var(--mat-sys-corner-extra-small));
  border-bottom-left-radius: var(--mat-form-field-outlined-container-shape, var(--mat-sys-corner-extra-small));
}
.mdc-text-field--outlined .mdc-notched-outline .mdc-notched-outline__leading {
  width: max(12px, var(--mat-form-field-outlined-container-shape, var(--mat-sys-corner-extra-small)));
}
[dir=rtl] .mdc-notched-outline__leading {
  border-left: none;
  border-right: 1px solid;
  border-bottom-left-radius: 0;
  border-top-left-radius: 0;
  border-top-right-radius: var(--mat-form-field-outlined-container-shape, var(--mat-sys-corner-extra-small));
  border-bottom-right-radius: var(--mat-form-field-outlined-container-shape, var(--mat-sys-corner-extra-small));
}

.mdc-notched-outline__trailing {
  flex-grow: 1;
  border-left: none;
  border-right: 1px solid;
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
  border-top-right-radius: var(--mat-form-field-outlined-container-shape, var(--mat-sys-corner-extra-small));
  border-bottom-right-radius: var(--mat-form-field-outlined-container-shape, var(--mat-sys-corner-extra-small));
}
[dir=rtl] .mdc-notched-outline__trailing {
  border-left: 1px solid;
  border-right: none;
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
  border-top-left-radius: var(--mat-form-field-outlined-container-shape, var(--mat-sys-corner-extra-small));
  border-bottom-left-radius: var(--mat-form-field-outlined-container-shape, var(--mat-sys-corner-extra-small));
}

.mdc-notched-outline__notch {
  flex: 0 0 auto;
  width: auto;
}
.mdc-text-field--outlined .mdc-notched-outline .mdc-notched-outline__notch {
  max-width: min(var(--mat-form-field-notch-max-width, 100%), calc(100% - max(12px, var(--mat-form-field-outlined-container-shape, var(--mat-sys-corner-extra-small))) * 2));
}
.mdc-text-field--outlined .mdc-notched-outline--notched .mdc-notched-outline__notch {
  max-width: min(100%, calc(100% - max(12px, var(--mat-form-field-outlined-container-shape, var(--mat-sys-corner-extra-small))) * 2));
}
.mdc-text-field--outlined .mdc-notched-outline--notched .mdc-notched-outline__notch {
  padding-top: 1px;
}
.mdc-text-field--focused.mdc-text-field--outlined .mdc-notched-outline--notched .mdc-notched-outline__notch {
  padding-top: 2px;
}
.mdc-notched-outline--notched .mdc-notched-outline__notch {
  padding-left: 0;
  padding-right: 8px;
  border-top: none;
}
[dir=rtl] .mdc-notched-outline--notched .mdc-notched-outline__notch {
  padding-left: 8px;
  padding-right: 0;
}
.mdc-notched-outline--no-label .mdc-notched-outline__notch {
  display: none;
}

.mdc-line-ripple::before, .mdc-line-ripple::after {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  border-bottom-style: solid;
  content: "";
}
.mdc-line-ripple::before {
  z-index: 1;
  border-bottom-width: var(--mat-form-field-filled-active-indicator-height, 1px);
}
.mdc-text-field--filled:not(.mdc-text-field--disabled) .mdc-line-ripple::before {
  border-bottom-color: var(--mat-form-field-filled-active-indicator-color, var(--mat-sys-on-surface-variant));
}
.mdc-text-field--filled:not(.mdc-text-field--disabled):not(.mdc-text-field--focused):hover .mdc-line-ripple::before {
  border-bottom-color: var(--mat-form-field-filled-hover-active-indicator-color, var(--mat-sys-on-surface));
}
.mdc-text-field--filled.mdc-text-field--disabled .mdc-line-ripple::before {
  border-bottom-color: var(--mat-form-field-filled-disabled-active-indicator-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}
.mdc-text-field--filled:not(.mdc-text-field--disabled).mdc-text-field--invalid .mdc-line-ripple::before {
  border-bottom-color: var(--mat-form-field-filled-error-active-indicator-color, var(--mat-sys-error));
}
.mdc-text-field--filled:not(.mdc-text-field--disabled).mdc-text-field--invalid:not(.mdc-text-field--focused):hover .mdc-line-ripple::before {
  border-bottom-color: var(--mat-form-field-filled-error-hover-active-indicator-color, var(--mat-sys-on-error-container));
}
.mdc-line-ripple::after {
  transform: scaleX(0);
  opacity: 0;
  z-index: 2;
}
.mdc-text-field--filled .mdc-line-ripple::after {
  border-bottom-width: var(--mat-form-field-filled-focus-active-indicator-height, 2px);
}
.mdc-text-field--filled:not(.mdc-text-field--disabled) .mdc-line-ripple::after {
  border-bottom-color: var(--mat-form-field-filled-focus-active-indicator-color, var(--mat-sys-primary));
}
.mdc-text-field--filled.mdc-text-field--invalid:not(.mdc-text-field--disabled) .mdc-line-ripple::after {
  border-bottom-color: var(--mat-form-field-filled-error-focus-active-indicator-color, var(--mat-sys-error));
}

.mdc-line-ripple--active::after {
  transform: scaleX(1);
  opacity: 1;
}

.mdc-line-ripple--deactivating::after {
  opacity: 0;
}

.mdc-text-field--disabled {
  pointer-events: none;
}

.mat-mdc-form-field-textarea-control {
  vertical-align: middle;
  resize: vertical;
  box-sizing: border-box;
  height: auto;
  margin: 0;
  padding: 0;
  border: none;
  overflow: auto;
}

.mat-mdc-form-field-input-control.mat-mdc-form-field-input-control {
  -moz-osx-font-smoothing: grayscale;
  -webkit-font-smoothing: antialiased;
  font: inherit;
  letter-spacing: inherit;
  text-decoration: inherit;
  text-transform: inherit;
  border: none;
}

.mat-mdc-form-field .mat-mdc-floating-label.mdc-floating-label {
  -moz-osx-font-smoothing: grayscale;
  -webkit-font-smoothing: antialiased;
  line-height: normal;
  pointer-events: all;
  will-change: auto;
}

.mat-mdc-form-field:not(.mat-form-field-disabled) .mat-mdc-floating-label.mdc-floating-label {
  cursor: inherit;
}

.mdc-text-field--no-label:not(.mdc-text-field--textarea) .mat-mdc-form-field-input-control.mdc-text-field__input,
.mat-mdc-text-field-wrapper .mat-mdc-form-field-input-control {
  height: auto;
}

.mat-mdc-text-field-wrapper .mat-mdc-form-field-input-control.mdc-text-field__input[type=color] {
  height: 23px;
}

.mat-mdc-text-field-wrapper {
  height: auto;
  flex: auto;
  will-change: auto;
}

.mat-mdc-form-field-has-icon-prefix .mat-mdc-text-field-wrapper {
  padding-left: 0;
  --mat-mdc-form-field-label-offset-x: -16px;
}

.mat-mdc-form-field-has-icon-suffix .mat-mdc-text-field-wrapper {
  padding-right: 0;
}

[dir=rtl] .mat-mdc-text-field-wrapper {
  padding-left: 16px;
  padding-right: 16px;
}
[dir=rtl] .mat-mdc-form-field-has-icon-suffix .mat-mdc-text-field-wrapper {
  padding-left: 0;
}
[dir=rtl] .mat-mdc-form-field-has-icon-prefix .mat-mdc-text-field-wrapper {
  padding-right: 0;
}

.mat-form-field-disabled .mdc-text-field__input::placeholder {
  color: var(--mat-form-field-disabled-input-text-placeholder-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}
.mat-form-field-disabled .mdc-text-field__input::-moz-placeholder {
  color: var(--mat-form-field-disabled-input-text-placeholder-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}
.mat-form-field-disabled .mdc-text-field__input::-webkit-input-placeholder {
  color: var(--mat-form-field-disabled-input-text-placeholder-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}
.mat-form-field-disabled .mdc-text-field__input:-ms-input-placeholder {
  color: var(--mat-form-field-disabled-input-text-placeholder-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}

.mat-mdc-form-field-label-always-float .mdc-text-field__input::placeholder {
  transition-delay: 40ms;
  transition-duration: 110ms;
  opacity: 1;
}

.mat-mdc-text-field-wrapper .mat-mdc-form-field-infix .mat-mdc-floating-label {
  left: auto;
  right: auto;
}

.mat-mdc-text-field-wrapper.mdc-text-field--outlined .mdc-text-field__input {
  display: inline-block;
}

.mat-mdc-form-field .mat-mdc-text-field-wrapper.mdc-text-field .mdc-notched-outline__notch {
  padding-top: 0;
}

.mat-mdc-form-field.mat-mdc-form-field.mat-mdc-form-field.mat-mdc-form-field.mat-mdc-form-field.mat-mdc-form-field .mdc-notched-outline__notch {
  border-left: 1px solid transparent;
}

[dir=rtl] .mat-mdc-form-field.mat-mdc-form-field.mat-mdc-form-field.mat-mdc-form-field.mat-mdc-form-field.mat-mdc-form-field .mdc-notched-outline__notch {
  border-left: none;
  border-right: 1px solid transparent;
}

.mat-mdc-form-field-infix {
  min-height: var(--mat-form-field-container-height, 56px);
  padding-top: var(--mat-form-field-filled-with-label-container-padding-top, 24px);
  padding-bottom: var(--mat-form-field-filled-with-label-container-padding-bottom, 8px);
}
.mdc-text-field--outlined .mat-mdc-form-field-infix, .mdc-text-field--no-label .mat-mdc-form-field-infix {
  padding-top: var(--mat-form-field-container-vertical-padding, 16px);
  padding-bottom: var(--mat-form-field-container-vertical-padding, 16px);
}

.mat-mdc-text-field-wrapper .mat-mdc-form-field-flex .mat-mdc-floating-label {
  top: calc(var(--mat-form-field-container-height, 56px) / 2);
}

.mdc-text-field--filled .mat-mdc-floating-label {
  display: var(--mat-form-field-filled-label-display, block);
}

.mat-mdc-text-field-wrapper.mdc-text-field--outlined .mdc-notched-outline--upgraded .mdc-floating-label--float-above {
  --mat-mdc-form-field-label-transform: translateY(calc(calc(6.75px + var(--mat-form-field-container-height, 56px) / 2) * -1))
    scale(var(--mat-mdc-form-field-floating-label-scale, 0.75));
  transform: var(--mat-mdc-form-field-label-transform);
}

@keyframes _mat-form-field-subscript-animation {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.mat-mdc-form-field-subscript-wrapper {
  box-sizing: border-box;
  width: 100%;
  position: relative;
}

.mat-mdc-form-field-hint-wrapper,
.mat-mdc-form-field-error-wrapper {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  padding: 0 16px;
  opacity: 1;
  transform: translateY(0);
  animation: _mat-form-field-subscript-animation 0ms cubic-bezier(0.55, 0, 0.55, 0.2);
}

.mat-mdc-form-field-subscript-dynamic-size .mat-mdc-form-field-hint-wrapper,
.mat-mdc-form-field-subscript-dynamic-size .mat-mdc-form-field-error-wrapper {
  position: static;
}

.mat-mdc-form-field-bottom-align::before {
  content: "";
  display: inline-block;
  height: 16px;
}

.mat-mdc-form-field-bottom-align.mat-mdc-form-field-subscript-dynamic-size::before {
  content: unset;
}

.mat-mdc-form-field-hint-end {
  order: 1;
}

.mat-mdc-form-field-hint-wrapper {
  display: flex;
}

.mat-mdc-form-field-hint-spacer {
  flex: 1 0 1em;
}

.mat-mdc-form-field-error {
  display: block;
  color: var(--mat-form-field-error-text-color, var(--mat-sys-error));
}

.mat-mdc-form-field-subscript-wrapper,
.mat-mdc-form-field-bottom-align::before {
  -moz-osx-font-smoothing: grayscale;
  -webkit-font-smoothing: antialiased;
  font-family: var(--mat-form-field-subscript-text-font, var(--mat-sys-body-small-font));
  line-height: var(--mat-form-field-subscript-text-line-height, var(--mat-sys-body-small-line-height));
  font-size: var(--mat-form-field-subscript-text-size, var(--mat-sys-body-small-size));
  letter-spacing: var(--mat-form-field-subscript-text-tracking, var(--mat-sys-body-small-tracking));
  font-weight: var(--mat-form-field-subscript-text-weight, var(--mat-sys-body-small-weight));
}

.mat-mdc-form-field-focus-overlay {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  opacity: 0;
  pointer-events: none;
  background-color: var(--mat-form-field-state-layer-color, var(--mat-sys-on-surface));
}
.mat-mdc-text-field-wrapper:hover .mat-mdc-form-field-focus-overlay {
  opacity: var(--mat-form-field-hover-state-layer-opacity, var(--mat-sys-hover-state-layer-opacity));
}
.mat-mdc-form-field.mat-focused .mat-mdc-form-field-focus-overlay {
  opacity: var(--mat-form-field-focus-state-layer-opacity, 0);
}

select.mat-mdc-form-field-input-control {
  -moz-appearance: none;
  -webkit-appearance: none;
  background-color: transparent;
  display: inline-flex;
  box-sizing: border-box;
}
select.mat-mdc-form-field-input-control:not(:disabled) {
  cursor: pointer;
}
select.mat-mdc-form-field-input-control:not(.mat-mdc-native-select-inline) option {
  color: var(--mat-form-field-select-option-text-color, var(--mat-sys-neutral10));
}
select.mat-mdc-form-field-input-control:not(.mat-mdc-native-select-inline) option:disabled {
  color: var(--mat-form-field-select-disabled-option-text-color, color-mix(in srgb, var(--mat-sys-neutral10) 38%, transparent));
}

.mat-mdc-form-field-type-mat-native-select .mat-mdc-form-field-infix::after {
  content: "";
  width: 0;
  height: 0;
  border-left: 5px solid transparent;
  border-right: 5px solid transparent;
  border-top: 5px solid;
  position: absolute;
  right: 0;
  top: 50%;
  margin-top: -2.5px;
  pointer-events: none;
  color: var(--mat-form-field-enabled-select-arrow-color, var(--mat-sys-on-surface-variant));
}
[dir=rtl] .mat-mdc-form-field-type-mat-native-select .mat-mdc-form-field-infix::after {
  right: auto;
  left: 0;
}
.mat-mdc-form-field-type-mat-native-select.mat-focused .mat-mdc-form-field-infix::after {
  color: var(--mat-form-field-focus-select-arrow-color, var(--mat-sys-primary));
}
.mat-mdc-form-field-type-mat-native-select.mat-form-field-disabled .mat-mdc-form-field-infix::after {
  color: var(--mat-form-field-disabled-select-arrow-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}
.mat-mdc-form-field-type-mat-native-select .mat-mdc-form-field-input-control {
  padding-right: 15px;
}
[dir=rtl] .mat-mdc-form-field-type-mat-native-select .mat-mdc-form-field-input-control {
  padding-right: 0;
  padding-left: 15px;
}

@media (forced-colors: active) {
  .mat-form-field-appearance-fill .mat-mdc-text-field-wrapper {
    outline: solid 1px;
  }
}
@media (forced-colors: active) {
  .mat-form-field-appearance-fill.mat-form-field-disabled .mat-mdc-text-field-wrapper {
    outline-color: GrayText;
  }
}

@media (forced-colors: active) {
  .mat-form-field-appearance-fill.mat-focused .mat-mdc-text-field-wrapper {
    outline: dashed 3px;
  }
}

@media (forced-colors: active) {
  .mat-mdc-form-field.mat-focused .mdc-notched-outline {
    border: dashed 3px;
  }
}

.mat-mdc-form-field-input-control[type=date], .mat-mdc-form-field-input-control[type=datetime], .mat-mdc-form-field-input-control[type=datetime-local], .mat-mdc-form-field-input-control[type=month], .mat-mdc-form-field-input-control[type=week], .mat-mdc-form-field-input-control[type=time] {
  line-height: 1;
}
.mat-mdc-form-field-input-control::-webkit-datetime-edit {
  line-height: 1;
  padding: 0;
  margin-bottom: -2px;
}

.mat-mdc-form-field {
  --mat-mdc-form-field-floating-label-scale: 0.75;
  display: inline-flex;
  flex-direction: column;
  min-width: 0;
  text-align: left;
  -moz-osx-font-smoothing: grayscale;
  -webkit-font-smoothing: antialiased;
  font-family: var(--mat-form-field-container-text-font, var(--mat-sys-body-large-font));
  line-height: var(--mat-form-field-container-text-line-height, var(--mat-sys-body-large-line-height));
  font-size: var(--mat-form-field-container-text-size, var(--mat-sys-body-large-size));
  letter-spacing: var(--mat-form-field-container-text-tracking, var(--mat-sys-body-large-tracking));
  font-weight: var(--mat-form-field-container-text-weight, var(--mat-sys-body-large-weight));
}
.mat-mdc-form-field .mdc-text-field--outlined .mdc-floating-label--float-above {
  font-size: calc(var(--mat-form-field-outlined-label-text-populated-size) * var(--mat-mdc-form-field-floating-label-scale));
}
.mat-mdc-form-field .mdc-text-field--outlined .mdc-notched-outline--upgraded .mdc-floating-label--float-above {
  font-size: var(--mat-form-field-outlined-label-text-populated-size);
}
[dir=rtl] .mat-mdc-form-field {
  text-align: right;
}

.mat-mdc-form-field-flex {
  display: inline-flex;
  align-items: baseline;
  box-sizing: border-box;
  width: 100%;
}

.mat-mdc-text-field-wrapper {
  width: 100%;
  z-index: 0;
}

.mat-mdc-form-field-icon-prefix,
.mat-mdc-form-field-icon-suffix {
  align-self: center;
  line-height: 0;
  pointer-events: auto;
  position: relative;
  z-index: 1;
}
.mat-mdc-form-field-icon-prefix > .mat-icon,
.mat-mdc-form-field-icon-suffix > .mat-icon {
  padding: 0 12px;
  box-sizing: content-box;
}

.mat-mdc-form-field-icon-prefix {
  color: var(--mat-form-field-leading-icon-color, var(--mat-sys-on-surface-variant));
}
.mat-form-field-disabled .mat-mdc-form-field-icon-prefix {
  color: var(--mat-form-field-disabled-leading-icon-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}

.mat-mdc-form-field-icon-suffix {
  color: var(--mat-form-field-trailing-icon-color, var(--mat-sys-on-surface-variant));
}
.mat-form-field-disabled .mat-mdc-form-field-icon-suffix {
  color: var(--mat-form-field-disabled-trailing-icon-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}
.mat-form-field-invalid .mat-mdc-form-field-icon-suffix {
  color: var(--mat-form-field-error-trailing-icon-color, var(--mat-sys-error));
}
.mat-form-field-invalid:not(.mat-focused):not(.mat-form-field-disabled) .mat-mdc-text-field-wrapper:hover .mat-mdc-form-field-icon-suffix {
  color: var(--mat-form-field-error-hover-trailing-icon-color, var(--mat-sys-on-error-container));
}
.mat-form-field-invalid.mat-focused .mat-mdc-text-field-wrapper .mat-mdc-form-field-icon-suffix {
  color: var(--mat-form-field-error-focus-trailing-icon-color, var(--mat-sys-error));
}

.mat-mdc-form-field-icon-prefix,
[dir=rtl] .mat-mdc-form-field-icon-suffix {
  padding: 0 4px 0 0;
}

.mat-mdc-form-field-icon-suffix,
[dir=rtl] .mat-mdc-form-field-icon-prefix {
  padding: 0 0 0 4px;
}

.mat-mdc-form-field-subscript-wrapper .mat-icon,
.mat-mdc-form-field label .mat-icon {
  width: 1em;
  height: 1em;
  font-size: inherit;
}

.mat-mdc-form-field-infix {
  flex: auto;
  min-width: 0;
  width: 180px;
  position: relative;
  box-sizing: border-box;
}
.mat-mdc-form-field-infix:has(textarea[cols]) {
  width: auto;
}

.mat-mdc-form-field .mdc-notched-outline__notch {
  margin-left: -1px;
  -webkit-clip-path: inset(-9em -999em -9em 1px);
  clip-path: inset(-9em -999em -9em 1px);
}
[dir=rtl] .mat-mdc-form-field .mdc-notched-outline__notch {
  margin-left: 0;
  margin-right: -1px;
  -webkit-clip-path: inset(-9em 1px -9em -999em);
  clip-path: inset(-9em 1px -9em -999em);
}

.mat-mdc-form-field.mat-form-field-animations-enabled .mdc-floating-label {
  transition: transform 150ms cubic-bezier(0.4, 0, 0.2, 1), color 150ms cubic-bezier(0.4, 0, 0.2, 1);
}
.mat-mdc-form-field.mat-form-field-animations-enabled .mdc-text-field__input {
  transition: opacity 150ms cubic-bezier(0.4, 0, 0.2, 1);
}
.mat-mdc-form-field.mat-form-field-animations-enabled .mdc-text-field__input::placeholder {
  transition: opacity 67ms cubic-bezier(0.4, 0, 0.2, 1);
}
.mat-mdc-form-field.mat-form-field-animations-enabled .mdc-text-field__input::-moz-placeholder {
  transition: opacity 67ms cubic-bezier(0.4, 0, 0.2, 1);
}
.mat-mdc-form-field.mat-form-field-animations-enabled .mdc-text-field__input::-webkit-input-placeholder {
  transition: opacity 67ms cubic-bezier(0.4, 0, 0.2, 1);
}
.mat-mdc-form-field.mat-form-field-animations-enabled .mdc-text-field__input:-ms-input-placeholder {
  transition: opacity 67ms cubic-bezier(0.4, 0, 0.2, 1);
}
.mat-mdc-form-field.mat-form-field-animations-enabled.mdc-text-field--no-label .mdc-text-field__input::placeholder, .mat-mdc-form-field.mat-form-field-animations-enabled.mdc-text-field--focused .mdc-text-field__input::placeholder {
  transition-delay: 40ms;
  transition-duration: 110ms;
}
.mat-mdc-form-field.mat-form-field-animations-enabled.mdc-text-field--no-label .mdc-text-field__input::-moz-placeholder, .mat-mdc-form-field.mat-form-field-animations-enabled.mdc-text-field--focused .mdc-text-field__input::-moz-placeholder {
  transition-delay: 40ms;
  transition-duration: 110ms;
}
.mat-mdc-form-field.mat-form-field-animations-enabled.mdc-text-field--no-label .mdc-text-field__input::-webkit-input-placeholder, .mat-mdc-form-field.mat-form-field-animations-enabled.mdc-text-field--focused .mdc-text-field__input::-webkit-input-placeholder {
  transition-delay: 40ms;
  transition-duration: 110ms;
}
.mat-mdc-form-field.mat-form-field-animations-enabled.mdc-text-field--no-label .mdc-text-field__input:-ms-input-placeholder, .mat-mdc-form-field.mat-form-field-animations-enabled.mdc-text-field--focused .mdc-text-field__input:-ms-input-placeholder {
  transition-delay: 40ms;
  transition-duration: 110ms;
}
.mat-mdc-form-field.mat-form-field-animations-enabled .mdc-text-field--filled:not(.mdc-ripple-upgraded):focus .mdc-text-field__ripple::before {
  transition-duration: 75ms;
}
.mat-mdc-form-field.mat-form-field-animations-enabled .mdc-line-ripple::after {
  transition: transform 180ms cubic-bezier(0.4, 0, 0.2, 1), opacity 180ms cubic-bezier(0.4, 0, 0.2, 1);
}
.mat-mdc-form-field.mat-form-field-animations-enabled .mat-mdc-form-field-hint-wrapper,
.mat-mdc-form-field.mat-form-field-animations-enabled .mat-mdc-form-field-error-wrapper {
  animation-duration: 300ms;
}

.mdc-notched-outline .mdc-floating-label {
  max-width: calc(100% + 1px);
}

.mdc-notched-outline--upgraded .mdc-floating-label--float-above {
  max-width: calc(133.3333333333% + 1px);
}
`],encapsulation:2,changeDetection:0})}return i})();var ut=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275mod=$({type:i});static \u0275inj=G({imports:[_a,kr,fe]})}return i})();function Qg(i,n){if(i&1&&(Pe(0,"i",0),le(1,"span"),P(2),O(3,"translate"),he()),i&2){let e=m();c(2),ce(L(3,1,e.hint()))}}var qt=(()=>{class i{constructor(){this.control=C.required(),this.hint=C(void 0)}static{this.\u0275fac=function(t){return new(t||i)}}static{this.\u0275cmp=x({type:i,selectors:[["vitamui-hint"]],inputs:{control:[1,"control"],hint:[1,"hint"]},decls:1,vars:1,consts:[[1,"vitamui-icon","vitamui-icon-info","mr-1"]],template:function(t,r){t&1&&v(0,Qg,4,3),t&2&&y(r.hint()&&r.control().valid?0:-1)},dependencies:[ut,ve],styles:[`[_nghost-%COMP%]{display:flex;align-items:center;color:var(--vitamui-grey-700);font-size:12px;padding-top:5px}i[_ngcontent-%COMP%]{font-size:14px}


























/*# sourceMappingURL=/vitam-ui/develop/hint.component-ZGYTAXGT.css.map */`]})}}return i})();var Vs=0;function Xg(){return Vs}function xn(i,n){return(...e)=>{try{return Vs=n,i(...e)}finally{Vs=0}}}function Jg(i){return!i}function Iu(i){return i}function Pt(i){return Array.isArray(i)}function Di(i){return(typeof i=="object"||typeof i=="function")&&i!=null}var Dn=Symbol(),ao=Symbol(),Sr=class{predicates;fns=[];constructor(n){this.predicates=n}push(n){this.fns.push(Ou(this.predicates,n))}mergeIn(n){let e=this.predicates?n.fns.map(t=>Ou(this.predicates,t)):n.fns;this.fns.push(...e)}},eo=class extends Sr{get defaultValue(){return!1}compute(n){return this.fns.some(e=>{let t=e(n);return t&&t!==ao})}},Mi=class i extends Sr{ignore;static ignoreNull(n){return new i(n,e=>e===null)}constructor(n,e){super(n),this.ignore=e}get defaultValue(){return[]}compute(n){return this.fns.reduce((e,t)=>{let r=t(n);return r===void 0||r===ao?e:Pt(r)?[...e,...this.ignore?r.filter(a=>!this.ignore(a)):r]:this.ignore&&this.ignore(r)?e:[...e,r]},[])}},Ns=class extends Mi{constructor(n){super(n,void 0)}},Ls=class extends Sr{key;get defaultValue(){return this.key.reducer.getInitial()}constructor(n,e){super(n),this.key=e}compute(n){if(this.fns.length===0)return this.key.reducer.getInitial();let e=this.key.reducer.getInitial();for(let t=0;t<this.fns.length;t++){let r=this.fns[t](n);r!==ao&&(e=this.key.reducer.reduce(e,r))}return e}};function Ou(i,n){return i.length===0?n:e=>{for(let t of i){let r=e.stateOf(t.path),a=W(r.structure.pathKeys).length-t.depth;for(let o=0;o<a;o++)r=r.structure.parent;if(!t.fn(r.context))return ao}return n(e)}}var wi=class{predicates;hidden;disabledReasons;readonly;syncErrors;syncTreeErrors;asyncErrors;metadata=new Map;constructor(n){this.predicates=n,this.hidden=new eo(n),this.disabledReasons=new Ns(n),this.readonly=new eo(n),this.syncErrors=Mi.ignoreNull(n),this.syncTreeErrors=Mi.ignoreNull(n),this.asyncErrors=Mi.ignoreNull(n)}hasMetadata(n){return this.metadata.has(n)}getMetadataKeys(){return this.metadata.keys()}getMetadata(n){return this.metadata.has(n)||this.metadata.set(n,new Ls(this.predicates,n)),this.metadata.get(n)}mergeIn(n){this.hidden.mergeIn(n.hidden),this.disabledReasons.mergeIn(n.disabledReasons),this.readonly.mergeIn(n.readonly),this.syncErrors.mergeIn(n.syncErrors),this.syncTreeErrors.mergeIn(n.syncTreeErrors),this.asyncErrors.mergeIn(n.asyncErrors);for(let e of n.getMetadataKeys()){let t=n.metadata.get(e);this.getMetadata(e).mergeIn(t)}}},to=class{depth;constructor(n){this.depth=n}build(){return new no(this,[],0)}},Ei=class i extends to{constructor(n){super(n)}current;all=[];addHiddenRule(n){this.getCurrent().addHiddenRule(n)}addDisabledReasonRule(n){this.getCurrent().addDisabledReasonRule(n)}addReadonlyRule(n){this.getCurrent().addReadonlyRule(n)}addSyncErrorRule(n){this.getCurrent().addSyncErrorRule(n)}addSyncTreeErrorRule(n){this.getCurrent().addSyncTreeErrorRule(n)}addAsyncErrorRule(n){this.getCurrent().addAsyncErrorRule(n)}addMetadataRule(n,e){this.getCurrent().addMetadataRule(n,e)}getChild(n){if(n===Dn){let e=this.getCurrent().children;e.size>(e.has(Dn)?1:0)&&(this.current=void 0)}return this.getCurrent().getChild(n)}hasLogic(n){return this===n?!0:this.all.some(({builder:e})=>e.hasLogic(n))}mergeIn(n,e){e?this.all.push({builder:n,predicate:{fn:xn(e.fn,this.depth),path:e.path}}):this.all.push({builder:n}),this.current=void 0}getCurrent(){return this.current===void 0&&(this.current=new Ir(this.depth),this.all.push({builder:this.current})),this.current}static newRoot(){return new i(0)}},Ir=class extends to{logic=new wi([]);children=new Map;constructor(n){super(n)}addHiddenRule(n){this.logic.hidden.push(xn(n,this.depth))}addDisabledReasonRule(n){this.logic.disabledReasons.push(xn(n,this.depth))}addReadonlyRule(n){this.logic.readonly.push(xn(n,this.depth))}addSyncErrorRule(n){this.logic.syncErrors.push(xn(n,this.depth))}addSyncTreeErrorRule(n){this.logic.syncTreeErrors.push(xn(n,this.depth))}addAsyncErrorRule(n){this.logic.asyncErrors.push(xn(n,this.depth))}addMetadataRule(n,e){this.logic.getMetadata(n).push(xn(e,this.depth))}getChild(n){return this.children.has(n)||this.children.set(n,new Ei(this.depth+1)),this.children.get(n)}hasLogic(n){return this===n}},no=class i{builder;predicates;depth;logic;constructor(n,e,t){this.builder=n,this.predicates=e,this.depth=t,this.logic=n?e_(n,e,t):new wi([])}getChild(n){let e=this.builder?Pu(this.builder,n):[];if(e.length===0)return new i(void 0,[],this.depth+1);if(e.length===1){let{builder:t,predicates:r}=e[0];return new i(t,[...this.predicates,...r.map(a=>js(a,this.depth))],this.depth+1)}else{let t=e.map(({builder:r,predicates:a})=>new i(r,[...this.predicates,...a.map(o=>js(o,this.depth))],this.depth+1));return new Bs(t)}}hasLogic(n){return this.builder?.hasLogic(n)??!1}},Bs=class i{all;logic;constructor(n){this.all=n,this.logic=new wi([]);for(let e of n)this.logic.mergeIn(e.logic)}getChild(n){return new i(this.all.flatMap(e=>e.getChild(n)))}hasLogic(n){return this.all.some(e=>e.hasLogic(n))}};function Pu(i,n){if(i instanceof Ei)return i.all.flatMap(({builder:e,predicate:t})=>{let r=Pu(e,n);return t?r.map(({builder:a,predicates:o})=>({builder:a,predicates:[...o,t]})):r});if(i instanceof Ir)return[...n!==Dn&&i.children.has(Dn)?[{builder:i.getChild(Dn),predicates:[]}]:[],...i.children.has(n)?[{builder:i.getChild(n),predicates:[]}]:[]];throw new Me(1909,!1)}function e_(i,n,e){let t=new wi(n);if(i instanceof Ei){let r=i.all.map(({builder:a,predicate:o})=>new no(a,o?[...n,js(o,e)]:n,e));for(let a of r)t.mergeIn(a.logic)}else if(i instanceof Ir)t.mergeIn(i.logic);else throw new Me(1909,!1);return t}function js(i,n){return q(g({},i),{depth:n})}var Ru=Symbol("PATH"),it=class i{keys;parent;keyInParent;root;children=new Map;fieldPathProxy=new Proxy(this,t_);logicBuilder;constructor(n,e,t,r){this.keys=n,this.parent=t,this.keyInParent=r,this.root=e??this,t||(this.logicBuilder=Ei.newRoot())}get builder(){return this.logicBuilder?this.logicBuilder:this.parent.builder.getChild(this.keyInParent)}getChild(n){return this.children.has(n)||this.children.set(n,new i([...this.keys,n],this.root,this,n)),this.children.get(n)}mergeIn(n,e){let t=n.compile();this.builder.mergeIn(t.builder,e)}static unwrapFieldPath(n){return n[Ru]}static newRoot(){return new i([],void 0,void 0,void 0)}},t_={get(i,n){return n===Ru?i:i.getChild(n).fieldPathProxy}},Ja,Ar=new Map,Or=class i{schemaFn;constructor(n){this.schemaFn=n}compile(){if(Ar.has(this))return Ar.get(this);let n=it.newRoot();Ar.set(this,n);let e=Ja;try{Ja=n,this.schemaFn(n.fieldPathProxy)}finally{Ja=e}return n}static create(n){return n instanceof i?n:new i(n)}static rootCompile(n){try{return Ar.clear(),n===void 0?it.newRoot():n instanceof i?n.compile():new i(n).compile()}finally{Ar.clear()}}};function n_(i){return i instanceof Or||typeof i=="function"}function Mn(i){if(Ja!==it.unwrapFieldPath(i).root)throw new Me(1908,!1)}function Ai(i,n,e){return Mn(i),it.unwrapFieldPath(i).builder.addMetadataRule(n,e),n}var en={list(){return{reduce:(i,n)=>n===void 0?i:[...i,n],getInitial:()=>[]}},min(){return{reduce:(i,n)=>i===void 0||n===void 0?i??n:Math.min(i,n),getInitial:()=>{}}},max(){return{reduce:(i,n)=>i===void 0||n===void 0?i??n:Math.max(i,n),getInitial:()=>{}}},or(){return{reduce:(i,n)=>i||n,getInitial:()=>!1}},and(){return{reduce:(i,n)=>i&&n,getInitial:()=>!0}},override:i_};function i_(i){return{reduce:(n,e)=>e,getInitial:()=>i?.()}}var Tr=class{reducer;create;brand;constructor(n,e){this.reducer=n,this.create=e}};function Gt(i){return new Tr(i??en.override())}function Vu(i,n){return new Tr(n??en.override(),i)}var Zs=Gt(en.or()),Nu=Gt(en.max()),Lu=Gt(en.min()),Bu=Gt(en.max()),Qs=Gt(en.min()),ju=Gt(en.list());function r_(i){return i.errors().length>0?"invalid":i.pending()?"unknown":"valid"}var zs=class{node;constructor(n){this.node=n}rawSyncTreeErrors=k(()=>this.shouldSkipValidation()?[]:[...this.node.logicNode.logic.syncTreeErrors.compute(this.node.context),...this.node.structure.parent?.validationState.rawSyncTreeErrors()??[]]);syncErrors=k(()=>this.shouldSkipValidation()?[]:[...this.node.logicNode.logic.syncErrors.compute(this.node.context),...this.syncTreeErrors(),...a_(this.node.submitState.submissionErrors())]);syncValid=k(()=>this.shouldSkipValidation()?!0:this.node.structure.reduceChildren(this.syncErrors().length===0,(n,e)=>e&&n.validationState.syncValid(),Jg));syncTreeErrors=k(()=>this.rawSyncTreeErrors().filter(n=>n.fieldTree===this.node.fieldTree));rawAsyncErrors=k(()=>this.shouldSkipValidation()?[]:[...this.node.logicNode.logic.asyncErrors.compute(this.node.context),...this.node.structure.parent?.validationState.rawAsyncErrors()??[]]);asyncErrors=k(()=>this.shouldSkipValidation()?[]:this.rawAsyncErrors().filter(n=>n==="pending"||n.fieldTree===this.node.fieldTree));parseErrors=k(()=>this.node.formFieldBindings().flatMap(n=>n.parseErrors()));errors=k(()=>[...this.parseErrors(),...this.syncErrors(),...this.asyncErrors().filter(n=>n!=="pending")]);errorSummary=k(()=>{let n=this.node.structure.reduceChildren(this.errors(),(e,t)=>[...t,...e.errorSummary()]);return W(()=>n.sort(o_)),n});pending=k(()=>this.node.structure.reduceChildren(this.asyncErrors().includes("pending"),(n,e)=>e||n.validationState.asyncErrors().includes("pending")));status=k(()=>{if(this.shouldSkipValidation())return"valid";let n=r_(this);return this.node.structure.reduceChildren(n,(e,t)=>t==="invalid"||e.validationState.status()==="invalid"?"invalid":t==="unknown"||e.validationState.status()==="unknown"?"unknown":"valid",e=>e==="invalid")});valid=k(()=>this.status()==="valid");invalid=k(()=>this.status()==="invalid");shouldSkipValidation=k(()=>this.node.hidden()||this.node.disabled()||this.node.readonly())};function a_(i){return i===void 0?[]:Pt(i)?i:[i]}function Pr(i,n){if(Pt(i))for(let e of i)e.fieldTree??=n;else i&&(i.fieldTree??=n);return i}function Tu(i){return i.formField?i.formField.element:i.fieldTree().formFieldBindings().reduce((n,e)=>!n||!e.element?n??e.element:n.compareDocumentPosition(e.element)&Node.DOCUMENT_POSITION_PRECEDING?e.element:n,void 0)}function o_(i,n){let e=Tu(i),t=Tu(n);return e===t?0:e===void 0||t===void 0?e===void 0?1:-1:e.compareDocumentPosition(t)&Node.DOCUMENT_POSITION_PRECEDING?1:-1}var io=Gt(),Hs=class{node;cache=new WeakMap;constructor(n){this.node=n}resolve(n){if(!this.cache.has(n)){let e=k(()=>{let t=it.unwrapFieldPath(n),r=this.node,a=Xg();for(;a>0||!r.structure.logic.hasLogic(t.root.builder);)if(a--,r=r.structure.parent,r===void 0)throw new Me(1900,!1);for(let o of t.keys)if(r=r.structure.getChild(o),r===void 0)throw new Me(1901,!1);return r.fieldTree});this.cache.set(n,e)}return this.cache.get(n)()}get fieldTree(){return this.node.fieldProxy}get state(){return this.node}get value(){return this.node.structure.value}get key(){return this.node.structure.keyInParent}get pathKeys(){return this.node.structure.pathKeys}index=k(()=>{let n=this.key();if(!Pt(W(this.node.structure.parent.value)))throw new Me(1906,!1);return Number(n)});fieldTreeOf=n=>this.resolve(n);stateOf=n=>this.resolve(n)();valueOf=n=>{let e=this.resolve(n)().value();if(e instanceof vn)throw new Me(1907,!1);return e}},Us=class{node;metadata=new Map;constructor(n){this.node=n;for(let e of this.node.logicNode.logic.getMetadataKeys())if(e.create){let t=this.node.logicNode.logic.getMetadata(e),r=W(()=>Ji(this.node.structure.injector,()=>e.create(k(()=>t.compute(this.node.context)))));this.metadata.set(e,r)}}get(n){if(this.has(n)&&!this.metadata.has(n)){if(n.create)throw new Me(1912,!1);let e=this.node.logicNode.logic.getMetadata(n);this.metadata.set(n,k(()=>e.compute(this.node.context)))}return this.metadata.get(n)}has(n){return this.node.logicNode.logic.hasMetadata(n)}},s_={get(i,n,e){let t=i(),r=t.structure.getChild(n);if(r!==void 0)return r.fieldTree;let a=W(t.value);if(Pt(a)){if(n==="length")return t.value().length;if(n===Symbol.iterator)return()=>(t.value(),Array.prototype[Symbol.iterator].apply(t.fieldTree))}if(Di(a)&&n===Symbol.iterator)return function*(){for(let o in e)yield[o,e[o]]}},getOwnPropertyDescriptor(i,n){let e=W(i().value),t=Reflect.getOwnPropertyDescriptor(e,n);return t&&!t.configurable&&(t.configurable=!0),t},ownKeys(i){let n=W(i().value);return typeof n=="object"&&n!==null?Reflect.ownKeys(n):[]}};function l_(i,n){let e=k(()=>i()[n()]);return e[Bo]=i[Bo],e.set=t=>{i.update(r=>d_(r,t,n()))},e.update=t=>{e.set(t(W(e)))},e.asReadonly=()=>e,e}function d_(i,n,e){if(Pt(i)){let t=[...i];return t[e]=n,t}else return q(g({},i),{[e]:n})}var ro=class{logic;node;createChildNode;identitySymbol=Symbol();_injector=void 0;get injector(){return this._injector??=U.create({providers:[],parent:this.fieldManager.injector}),this._injector}constructor(n,e,t){this.logic=n,this.node=e,this.createChildNode=t}children(){let n=this.childrenMap();return n===void 0?[]:Array.from(n.byPropertyKey.values()).map(e=>W(e.reader))}getChild(n){let e=n.toString(),t=W(this.childrenMap)?.byPropertyKey.get(e)?.reader;return t||(t=this.createReader(e)),t()}reduceChildren(n,e,t){let r=this.childrenMap();if(!r)return n;let a=n;for(let o of r.byPropertyKey.values()){if(t?.(a))break;a=e(W(o.reader),a)}return a}destroy(){this.injector.destroy()}createKeyInParent(n,e,t){if(n.kind==="root")return zu;if(e===void 0){let r=t;return k(()=>{if(this.parent.structure.getChild(r)!==this.node)throw new Me(-1902,!1);return r})}else{let r=t;return k(()=>{let a=this.parent.structure.value();if(!Pt(a))throw new Me(1903,!1);let o=a[r];if(Di(o)&&o.hasOwnProperty(this.parent.structure.identitySymbol)&&o[this.parent.structure.identitySymbol]===e)return r;for(let s=0;s<a.length;s++){let d=a[s];if(Di(d)&&d.hasOwnProperty(this.parent.structure.identitySymbol)&&d[this.parent.structure.identitySymbol]===e)return r=s.toString()}throw new Me(1904,!1)})}}createChildrenMap(){return jt({source:this.value,computation:(n,e)=>{if(!Di(n))return;let t=e?.value??{byPropertyKey:new Map},r,a=Pt(n);t!==void 0&&(a?r=u_(t,n,this.identitySymbol):r=m_(t,n));for(let o of Object.keys(n)){let s,d=n[o];if(d===void 0){t.byPropertyKey.has(o)&&(r??=g({},t),r.byPropertyKey.delete(o));continue}a&&Di(d)&&!Pt(d)&&(s=d[this.identitySymbol]??=Symbol(""));let u;s&&(t.byTrackingKey?.has(s)||(r??=g({},t),r.byTrackingKey??=new Map,r.byTrackingKey.set(s,this.createChildNode(o,s,a))),u=(r??t).byTrackingKey.get(s));let b=t.byPropertyKey.get(o);b===void 0?(r??=g({},t),r.byPropertyKey.set(o,{reader:this.createReader(o),node:u??this.createChildNode(o,s,a)})):u&&u!==b.node&&(r??=g({},t),b.node=u)}return r??t}})}createReader(n){return k(()=>this.childrenMap()?.byPropertyKey.get(n)?.node)}},Ws=class extends ro{fieldManager;value;get parent(){}get root(){return this.node}get pathKeys(){return c_}get keyInParent(){return zu}childrenMap;constructor(n,e,t,r,a){super(e,n,a),this.fieldManager=t,this.value=r,this.childrenMap=this.createChildrenMap()}},Ys=class extends ro{logic;parent;root;pathKeys;keyInParent;value;childrenMap;get fieldManager(){return this.root.structure.fieldManager}constructor(n,e,t,r,a,o){super(e,n,o),this.logic=e,this.parent=t,this.root=this.parent.structure.root,this.keyInParent=this.createKeyInParent({kind:"child",parent:t,pathNode:void 0,logic:e,initialKeyInParent:a,identityInParent:r,fieldAdapter:void 0},r,a),this.pathKeys=k(()=>[...t.structure.pathKeys(),this.keyInParent()]),this.value=l_(this.parent.structure.value,this.keyInParent),this.childrenMap=this.createChildrenMap(),this.fieldManager.structures.add(this)}};var c_=k(()=>[]),zu=k(()=>{throw new Me(1905,!1)});function u_(i,n,e){let t,r=new Set(i.byPropertyKey.keys()),a=new Set(i.byTrackingKey?.keys());for(let o=0;o<n.length;o++){let s=n[o];r.delete(o.toString()),Di(s)&&s.hasOwnProperty(e)&&a.delete(s[e])}if(r.size>0){t??=g({},i);for(let o of r)t.byPropertyKey.delete(o)}if(a.size>0){t??=g({},i);for(let o of a)t.byTrackingKey?.delete(o)}return t}function m_(i,n){let e;for(let t of i.byPropertyKey.keys())n.hasOwnProperty(t)||(e??=g({},i),e.byPropertyKey.delete(t));return e}var qs=class{node;selfSubmitting=V(!1);submissionErrors;constructor(n){this.node=n,this.submissionErrors=jt({source:this.node.structure.value,computation:()=>[]})}submitting=k(()=>this.selfSubmitting()||(this.node.structure.parent?.submitting()??!1))},Fr=class{structure;validationState;metadataState;nodeState;submitState;fieldAdapter;controlValue;_context=void 0;get context(){return this._context??=new Hs(this)}fieldProxy=new Proxy(()=>this,s_);pathNode;constructor(n){this.pathNode=n.pathNode,this.fieldAdapter=n.fieldAdapter,this.structure=this.fieldAdapter.createStructure(this,n),this.validationState=this.fieldAdapter.createValidationState(this,n),this.nodeState=this.fieldAdapter.createNodeState(this,n),this.metadataState=new Us(this),this.submitState=new qs(this),this.controlValue=this.controlValueSignal()}focusBoundControl(n){this.getBindingForFocus()?.focus(n)}getBindingForFocus(){let n=this.formFieldBindings().filter(e=>e.focus!==void 0).reduce(Fu,void 0);return n||this.structure.children().map(e=>e.getBindingForFocus()).reduce(Fu,void 0)}pendingSync=jt({source:()=>this.value(),computation:(n,e)=>{e?.value?.abort()}});get fieldTree(){return this.fieldProxy}get logicNode(){return this.structure.logic}get value(){return this.structure.value}get keyInParent(){return this.structure.keyInParent}get errors(){return this.validationState.errors}get parseErrors(){return this.validationState.parseErrors}get errorSummary(){return this.validationState.errorSummary}get pending(){return this.validationState.pending}get valid(){return this.validationState.valid}get invalid(){return this.validationState.invalid}get dirty(){return this.nodeState.dirty}get touched(){return this.nodeState.touched}get disabled(){return this.nodeState.disabled}get disabledReasons(){return this.nodeState.disabledReasons}get hidden(){return this.nodeState.hidden}get readonly(){return this.nodeState.readonly}get formFieldBindings(){return this.nodeState.formFieldBindings}get submitting(){return this.submitState.submitting}get name(){return this.nodeState.name}get max(){return this.metadata(Lu)}get maxLength(){return this.metadata(Qs)}get min(){return this.metadata(Nu)}get minLength(){return this.metadata(Bu)}get pattern(){return this.metadata(ju)??p_}get required(){return this.metadata(Zs)??h_}metadata(n){return this.metadataState.get(n)}hasMetadata(n){return this.metadataState.has(n)}markAsTouched(){W(()=>{this.nodeState.markAsTouched(),this.flushSync()})}markAsDirty(){this.nodeState.markAsDirty()}markAsPristine(){this.nodeState.markAsPristine()}markAsUntouched(){this.nodeState.markAsUntouched()}reset(n){W(()=>this._reset(n))}_reset(n){n!==void 0&&this.value.set(n),this.nodeState.markAsUntouched(),this.nodeState.markAsPristine();for(let e of this.structure.children())e._reset()}controlValueSignal(){let n=jt(this.value),{set:e,update:t}=n;return n.set=r=>{e(r),this.markAsDirty(),this.debounceSync()},n.update=r=>{t(r),this.markAsDirty(),this.debounceSync()},n}sync(){this.value.set(this.controlValue())}flushSync(){let n=this.pendingSync();n&&!n.signal.aborted&&(n.abort(),this.sync())}async debounceSync(){let n=W(()=>(this.pendingSync()?.abort(),this.nodeState.debouncer()));if(n){let e=new AbortController,t=n(e.signal);if(t&&(this.pendingSync.set(e),await t,e.signal.aborted))return}this.sync()}static newRoot(n,e,t,r){return r.newRoot(n,e,t,r)}createStructure(n){return n.kind==="root"?new Ws(this,n.logic,n.fieldManager,n.value,this.newChild.bind(this)):new Ys(this,n.logic,n.parent,n.identityInParent,n.initialKeyInParent,this.newChild.bind(this))}newChild(n,e,t){let r,a;return t?(r=this.pathNode.getChild(Dn),a=this.structure.logic.getChild(Dn)):(r=this.pathNode.getChild(n),a=this.structure.logic.getChild(n)),this.fieldAdapter.newChild({kind:"child",parent:this,pathNode:r,logic:a,initialKeyInParent:n,identityInParent:e,fieldAdapter:this.fieldAdapter})}},p_=k(()=>[]),h_=k(()=>!1);function Fu(i,n){return i?n&&i.element.compareDocumentPosition(n.element)&Node.DOCUMENT_POSITION_PRECEDING?n:i:n}var Gs=class{node;selfTouched=V(!1);selfDirty=V(!1);markAsTouched(){this.selfTouched.set(!0)}markAsDirty(){this.selfDirty.set(!0)}markAsPristine(){this.selfDirty.set(!1)}markAsUntouched(){this.selfTouched.set(!1)}formFieldBindings=V([]);constructor(n){this.node=n}dirty=k(()=>{let n=this.selfDirty()&&!this.isNonInteractive();return this.node.structure.reduceChildren(n,(e,t)=>t||e.nodeState.dirty(),Iu)});touched=k(()=>{let n=this.selfTouched()&&!this.isNonInteractive();return this.node.structure.reduceChildren(n,(e,t)=>t||e.nodeState.touched(),Iu)});disabledReasons=k(()=>[...this.node.structure.parent?.nodeState.disabledReasons()??[],...this.node.logicNode.logic.disabledReasons.compute(this.node.context)]);disabled=k(()=>!!this.disabledReasons().length);readonly=k(()=>(this.node.structure.parent?.nodeState.readonly()||this.node.logicNode.logic.readonly.compute(this.node.context))??!1);hidden=k(()=>(this.node.structure.parent?.nodeState.hidden()||this.node.logicNode.logic.hidden.compute(this.node.context))??!1);name=k(()=>{let n=this.node.structure.parent;return n?`${n.name()}.${this.node.structure.keyInParent()}`:this.node.structure.fieldManager.rootName});debouncer=k(()=>{if(this.node.logicNode.logic.hasMetadata(io)){let e=this.node.logicNode.logic.getMetadata(io).compute(this.node.context);if(e)return t=>e(this.node.context,t)}return this.node.structure.parent?.nodeState.debouncer?.()});isNonInteractive=k(()=>this.hidden()||this.disabled()||this.readonly())},$s=class{newRoot(n,e,t,r){return new Fr({kind:"root",fieldManager:n,value:e,pathNode:t,logic:t.builder.build(),fieldAdapter:r})}newChild(n){return new Fr(n)}createNodeState(n){return new Gs(n)}createValidationState(n){return new zs(n)}createStructure(n,e){return n.createStructure(e)}},Ks=class{injector;rootName;submitOptions;constructor(n,e,t){this.injector=n,this.rootName=e??`${this.injector.get(Jr)}.form${f_++}`,this.submitOptions=t}structures=new Set;createFieldManagementEffect(n){Lt(()=>{let e=new Set;this.markStructuresLive(n,e);for(let t of this.structures)e.has(t)||(this.structures.delete(t),W(()=>t.destroy()))},{injector:this.injector})}markStructuresLive(n,e){e.add(n);for(let t of n.children())this.markStructuresLive(t.structure,e)}},f_=0;function g_(i){let n,e,t;return i.length===3?[n,e,t]=i:i.length===2?n_(i[1])?[n,e]=i:[n,t]=i:[n]=i,[n,e,t]}function __(...i){let[n,e,t]=g_(i),r=t?.injector??l(U),a=Ji(r,()=>Or.rootCompile(e)),o=new Ks(r,t?.name,t?.submission),s=t?.adapter??new $s,d=Fr.newRoot(o,n,a,s);return o.createFieldManagementEffect(d.structure),d.fieldTree}function b_(i,n){Mn(i);let e=it.unwrapFieldPath(i).getChild(Dn).fieldPathProxy;Hu(e,n)}function Hu(i,n){Mn(i),it.unwrapFieldPath(i).mergeIn(Or.create(n))}async function Uu(i,n){let e=W(i);if(W(e.submitState.submitting))return!1;let t=n===void 0?e.structure.root.fieldProxy:i,r={root:e.structure.root.fieldProxy,submitted:i};n=typeof n=="function"?{action:n}:n??e.structure.fieldManager.submitOptions;let a=n?.action;if(!a)throw new Me(1915,!1);let o=n?.onInvalid,s=n?.ignoreValidators??"pending",d=!0;W(()=>{Wu(e),s==="none"?d=e.valid():s==="pending"&&(d=!e.invalid())});try{if(d){e.submitState.selfSubmitting.set(!0);let u=await W(()=>a?.(t,r));return u&&v_(e,u),!u||Pt(u)&&u.length===0}else W(()=>o?.(t,r));return!1}finally{e.submitState.selfSubmitting.set(!1)}}function Wu(i){if(!i.validationState.shouldSkipValidation()){i.markAsTouched();for(let n of i.structure.children())Wu(n)}}function v_(i,n){Pt(n)||(n=[n]);let e=new Map;for(let t of n){let r=Pr(t,i.fieldTree),a=r.fieldTree(),o=e.get(a);o||(o=[],e.set(a,o)),o.push(r)}for(let[t,r]of e)t.submitState.submissionErrors.set(r)}var ki=class{kind="compat";control;fieldTree;context;message;constructor({context:n,kind:e,control:t}){this.context=n,this.kind=e,this.control=t}};function Yu(i){if(i.length===0)return null;let n={};for(let e of i)n[e.kind]=e instanceof ki?e.context:e;return n}var y_=new M("");function nE(i,n){Mn(i),it.unwrapFieldPath(i).builder.addDisabledReasonRule(t=>{let r=!0;return typeof n=="string"?r=n:n&&(r=n(t)),typeof r=="string"?{fieldTree:t.fieldTree,message:r}:r?{fieldTree:t.fieldTree}:void 0})}function C_(i){let n=i;return typeof n.length=="number"?n.length:n.size}function oo(i,n){return i instanceof Function?i(n):i}function $u(i){return typeof i=="number"?isNaN(i):i===""||i===!1||i==null}function qu(i){return i===void 0?[]:Array.isArray(i)?i:[i]}function Ku(i,n){Mn(i),it.unwrapFieldPath(i).builder.addSyncErrorRule(t=>Pr(n(t),t.fieldTree))}function x_(i){return new Xs(i)}function D_(i,n){return new Js(i,n)}function iE(i,n){return new el(i,n)}var Si=class{__brand=void 0;kind="";fieldTree;message;constructor(n){n&&Object.assign(this,n)}},Xs=class extends Si{kind="required"};var Js=class extends Si{maxLength;kind="maxLength";constructor(n,e){super(e),this.maxLength=n}},el=class extends Si{pattern;kind="pattern";constructor(n,e){super(e),this.pattern=n}};var tl=class extends Si{kind="parse"};function rE(i,n,e){let t=Ai(i,Gt(),r=>typeof n=="number"?n:n(r));Ai(i,Qs,({state:r})=>r.metadata(t)()),Ku(i,r=>{if($u(r.value()))return;let a=r.state.metadata(t)();if(a!==void 0&&C_(r.value())>a)return e?.error?oo(e.error,r):D_(a,{message:oo(e?.message,r)})})}function aE(i,n){let e=Ai(i,Gt(),t=>n?.when?n.when(t):!0);Ai(i,Zs,({state:t})=>t.metadata(e)()),Ku(i,t=>{if(t.state.metadata(e)()&&$u(t.value()))return n?.error?oo(n.error,t):x_({message:oo(n?.message,t)})})}function oE(i,n){Mn(i);let e=it.unwrapFieldPath(i),t=Vu(n.factory);Ai(i,t,r=>{let o=r.stateOf(i).validationState;if(!(o.shouldSkipValidation()||!o.syncValid()))return n.params(r)}),e.builder.addAsyncErrorRule(r=>{let a=r.state.metadata(t),o;switch(a.status()){case"idle":return;case"loading":case"reloading":return"pending";case"resolved":case"local":return a.hasValue()?(o=n.onSuccess(a.value(),r),Pr(o,r.fieldTree)):void 0;case"error":return o=n.onError(a.error(),r),Pr(o,r.fieldTree)}})}function sE(i,n){Mn(i);let e=it.unwrapFieldPath(i),t=M_(n);e.builder.addMetadataRule(io,()=>t)}function M_(i){return typeof i=="function"?i:i==="blur"?E_():i>0?w_(i):k_}function w_(i){return(n,e)=>new Promise(t=>{let r,a=()=>{clearTimeout(r),t()};r=setTimeout(()=>{e.removeEventListener("abort",a),t()},i),e.addEventListener("abort",a,{once:!0})})}function E_(){return(i,n)=>new Promise(e=>{n.addEventListener("abort",()=>e(),{once:!0})})}function k_(){}var Zu=new M("");function Qu(i,n,e){let t=jt({source:i,computation:()=>[]}),r=a=>{let o=e(a);t.set(qu(o.error)),o.value!==void 0&&n(o.value),t.set(qu(o.error))};return{errors:t.asReadonly(),setRawValue:r}}function Xu(i,n){let{parse:e,format:t}=n,r=Qu(i,i.set,e),a=l(Zu,{self:!0,optional:!0});a&&a.set(r.errors);let o=jt(()=>t(i())),s=o;s.parseErrors=r.errors;let d=s.set.bind(s);return s.set=u=>{r.setRawValue(u),d(u)},s.update=u=>{s.set(u(o()))},s}var nl=class{field;constructor(n){this.field=n}control=this;get value(){return this.field().value()}get valid(){return this.field().valid()}get invalid(){return this.field().invalid()}get pending(){return this.field().pending()}get disabled(){return this.field().disabled()}get enabled(){return!this.field().disabled()}get errors(){return Yu(this.field().errors())}get pristine(){return!this.field().dirty()}get dirty(){return this.field().dirty()}get touched(){return this.field().touched()}get untouched(){return!this.field().touched()}get status(){if(this.field().disabled())return"DISABLED";if(this.field().valid())return"VALID";if(this.field().invalid())return"INVALID";if(this.field().pending())return"PENDING";throw new Me(1910,!1)}valueAccessor=null;hasValidator(n){return n===de.required?this.field().required():!1}updateValueAndValidity(){}},il={disabled:"disabled",disabledReasons:"disabledReasons",dirty:"dirty",errors:"errors",hidden:"hidden",invalid:"invalid",max:"max",maxLength:"maxLength",min:"min",minLength:"minLength",name:"name",pattern:"pattern",pending:"pending",readonly:"readonly",required:"required",touched:"touched"},A_=(()=>{let i={};for(let n of Object.keys(il))i[il[n]]=n;return i})();function rl(i,n){let e=A_[n];return i[e]?.()}var al=Object.values(il);function so(){return{}}function Un(i,n,e){return i[n]!==e?(i[n]=e,!0):!1}function S_(i){return i.tagName==="INPUT"||i.tagName==="SELECT"||i.tagName==="TEXTAREA"}function I_(i){if(i.tagName!=="INPUT")return!1;let n=i.type;return n==="date"||n==="datetime-local"||n==="month"||n==="number"||n==="range"||n==="time"||n==="week"}function O_(i){return i.tagName==="INPUT"||i.tagName==="TEXTAREA"}function T_(i,n){let e;if(i.validity.badInput)return{error:new tl};switch(i.type){case"checkbox":return{value:i.checked};case"number":case"range":case"datetime-local":if(e=W(n),typeof e=="number"||e===null)return{value:i.value===""?null:i.valueAsNumber};break;case"date":case"month":case"time":case"week":if(e=W(n),e===null||e instanceof Date)return{value:i.valueAsDate};if(typeof e=="number")return{value:i.valueAsNumber};break}return{value:i.value}}function F_(i,n){switch(i.type){case"checkbox":i.checked=n;return;case"radio":i.checked=n===i.value;return;case"number":case"range":case"datetime-local":if(typeof n=="number"){Gu(i,n);return}else if(n===null){i.value="";return}break;case"date":case"month":case"time":case"week":if(n===null||n instanceof Date){i.valueAsDate=n;return}else if(typeof n=="number"){Gu(i,n);return}}i.value=n}function Gu(i,n){isNaN(n)?i.value="":i.valueAsNumber=n}function ol(i,n,e,t){switch(e){case"name":i.setAttribute(n,e,t);break;case"disabled":case"readonly":case"required":t?i.setAttribute(n,e,""):i.removeAttribute(n,e);break;case"max":case"min":case"minLength":case"maxLength":t!==void 0?i.setAttribute(n,e,t.toString()):i.removeAttribute(n,e);break}}function P_(i,n){i.listenToCustomControlModel(t=>n.state().controlValue.set(t)),i.listenToCustomControlOutput("touchedChange",()=>n.state().markAsTouched()),n.registerAsBinding(i.customControl);let e=so();return()=>{let t=n.state(),r=t.controlValue();Un(e,"controlValue",r)&&i.setCustomControlModelInput(r);for(let a of al){let o;a==="errors"?o=n.errors():o=rl(t,a),Un(e,a,o)&&(i.setInputOnDirectives(a,o),n.elementAcceptsNativeProperty(a)&&!i.customControlHasInput(a)&&ol(n.renderer,n.nativeFormElement,a,o))}}}function R_(i,n){n.controlValueAccessor.registerOnChange(t=>n.state().controlValue.set(t)),n.controlValueAccessor.registerOnTouched(()=>n.state().markAsTouched()),n.registerAsBinding();let e=so();return()=>{let t=n.state(),r=t.value();Un(e,"controlValue",r)&&W(()=>n.controlValueAccessor.writeValue(r));for(let a of al){let o=rl(t,a);if(Un(e,a,o)){let s=i.setInputOnDirectives(a,o);a==="disabled"&&n.controlValueAccessor.setDisabledState?W(()=>n.controlValueAccessor.setDisabledState(o)):!s&&n.elementAcceptsNativeProperty(a)&&ol(n.renderer,n.nativeFormElement,a,o)}}}}function V_(i,n,e){if(typeof MutationObserver!="function")return;let t=new MutationObserver(r=>{r.some(a=>N_(a))&&n()});t.observe(i,{attributes:!0,attributeFilter:["value"],characterData:!0,childList:!0,subtree:!0}),e.onDestroy(()=>t.disconnect())}function N_(i){if(i.type==="childList"||i.type==="characterData"){if(i.target instanceof Comment)return!1;for(let n of i.addedNodes)if(!(n instanceof Comment))return!0;for(let n of i.removedNodes)if(!(n instanceof Comment))return!0;return!1}return i.type==="attributes"&&i.target instanceof HTMLOptionElement}function L_(i,n,e){let t=!1,r=n.nativeFormElement,a=Qu(()=>n.state().value(),s=>n.state().controlValue.set(s),()=>T_(r,n.state().value));e.set(a.errors),i.listenToDom("input",()=>a.setRawValue(void 0)),i.listenToDom("blur",()=>n.state().markAsTouched()),n.registerAsBinding(),r.tagName==="SELECT"&&V_(r,()=>{t&&(r.value=n.state().controlValue())},n.destroyRef);let o=so();return()=>{let s=n.state(),d=s.controlValue();Un(o,"controlValue",d)&&F_(r,d);for(let u of al){let b=rl(s,u);Un(o,u,b)&&(i.setInputOnDirectives(u,b),n.elementAcceptsNativeProperty(u)&&ol(n.renderer,r,u,b))}t=!0}}var B_=Symbol(),sl=new M(""),lE=(()=>{class i{field=C.required({alias:"formField"});renderer=l(we);destroyRef=l(zo);state=k(()=>this.field()());injector=l(U);element=l(N).nativeElement;elementIsNativeFormElement=S_(this.element);elementAcceptsNumericValues=I_(this.element);elementAcceptsTextualValues=O_(this.element);nativeFormElement=this.elementIsNativeFormElement?this.element:void 0;focuser=e=>this.element.focus(e);controlValueAccessors=l(dt,{optional:!0,self:!0});config=l(y_,{optional:!0});parseErrorsSource=V(void 0);_interopNgControl;get interopNgControl(){return this._interopNgControl??=new nl(this.state)}parseErrors=k(()=>this.parseErrorsSource()?.().map(e=>q(g({},e),{fieldTree:W(this.state).fieldTree,formField:this}))??[]);errors=k(()=>this.state().errors().filter(e=>!e.formField||e.formField===this));isFieldBinding=!1;get controlValueAccessor(){return this.controlValueAccessors?.[0]??this.interopNgControl?.valueAccessor??void 0}installClassBindingEffect(){let e=Object.entries(this.config?.classes??{}).map(([r,a])=>[r,k(()=>a(this))]);if(e.length===0)return;let t=so();da({write:()=>{for(let[r,a]of e){let o=a();Un(t,r,o)&&(o?this.renderer.addClass(this.element,r):this.renderer.removeClass(this.element,r))}}},{injector:this.injector})}focus(e){this.focuser(e)}registerAsBinding(e){if(this.isFieldBinding)throw new Me(1913,!1);this.isFieldBinding=!0,this.installClassBindingEffect(),e?.focus&&(this.focuser=t=>e.focus(t)),Lt(t=>{let r=this.state();r.nodeState.formFieldBindings.update(a=>[...a,this]),t(()=>{r.nodeState.formFieldBindings.update(a=>a.filter(o=>o!==this))})},{injector:this.injector})}[B_];\u0275ngControlCreate(e){if(!e.hasPassThrough)if(this.controlValueAccessor)this.\u0275ngControlUpdate=R_(e,this);else if(e.customControl)this.\u0275ngControlUpdate=P_(e,this);else if(this.elementIsNativeFormElement)this.\u0275ngControlUpdate=L_(e,this,this.parseErrorsSource);else throw new Me(1914,!1)}\u0275ngControlUpdate;elementAcceptsNativeProperty(e){if(!this.elementIsNativeFormElement)return!1;switch(e){case"min":case"max":return this.elementAcceptsNumericValues;case"minLength":case"maxLength":return this.elementAcceptsTextualValues;case"disabled":case"required":case"readonly":case"name":return!0;default:return!1}}static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,selectors:[["","formField",""]],inputs:{field:[1,"formField","field"]},exportAs:["formField"],features:[re([{provide:sl,useExisting:i},{provide:Qe,useFactory:()=>l(i).interopNgControl},{provide:Zu,useFactory:()=>l(i).parseErrorsSource}]),yd("formField")]})}return i})(),dE=(()=>{class i{fieldTree=C.required({alias:"formRoot"});onSubmit(e){e.preventDefault(),Uu(this.fieldTree())}static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,selectors:[["form","formRoot",""]],hostAttrs:["novalidate",""],hostBindings:function(t,r){t&1&&T("submit",function(o){return r.onSubmit(o)})},inputs:{fieldTree:[1,"formRoot","fieldTree"]}})}return i})();var Ju={requiredNotBlank:i=>i.value==null||i.value.trim().length===0?{requiredNotBlank:!0}:null,allowedIdentifier:i=>!i.value||i.value.match("^[a-zA-Z0-9+=@_-]*$")?null:{incorrectIdentifier:!0}};function lo(i){return i?.length?i.reduce((n,e)=>{if(e instanceof ki)return n[e.kind]=e.context,n;let d=e,{kind:t,fieldTree:r,message:a,formField:o}=d,s=Xi(d,["kind","fieldTree","message","formField"]);switch(t){case"maxLength":n.maxlength={requiredLength:s.maxLength};break;case"minLength":n.minlength={requiredLength:s.minLength};break;case"pattern":n.pattern={requiredPattern:String(s.pattern)};break;default:{let u=Object.fromEntries(Object.entries(s).filter(([b,A])=>!b.startsWith("__")&&A!==void 0));n[t]=Object.keys(u).length?u:!0}}return n},{}):null}function em(i){return Object.entries(i??{}).map(([n,e])=>e instanceof Object&&!Array.isArray(e)?q(g({},e),{kind:n}):{kind:n})}function tm(i,n){return Array.isArray(i)&&Array.isArray(n)?i.length===n.length&&i.every((e,t)=>e===n[t]):i===n}var co=(()=>{class i{#t;#e;#n;#i;constructor(e=l(U)){this.#t=l(ne),this.#e=null,this.#n=null,this.#i=V(!1),this.errorMessageMap=C(void 0),this.required=C(void 0,{transform:yt}),this.disabled=C(void 0,{transform:yt}),this.disabledState=jt(()=>this.disabled()),this.change=kd(),this.onChange=t=>this.change.emit(t),this.onTouched=()=>{},this.injector=e}#r;ngOnChanges(e){e.required&&this.control&&this.updateValidators(),e.disabled&&this.control&&this.updateDisabled()}setControl(e){return this.control.patchValue(e.value),this.control.addValidators(e._rawValidators),this.control.addAsyncValidators(e._rawAsyncValidators),e.registerOnDisabledChange(t=>t?this.control.disable():this.control.enable()),e.valueChanges.subscribe(t=>this.control.setValue(t)),this.afterControlSet(),this.control}canConfirmInWrapper(){return!!this.control.value}afterControlSet(){}ngOnInit(){let e=this.injector.get(Qe,null,{self:!0,optional:!0});if(e instanceof Hn)this.control=e.control,this.#r=e.control.valueChanges.subscribe(t=>{(e.model!==t||e.viewModel!==t)&&e.viewToModelUpdate(t)});else if(e instanceof et)this.control=e.control;else if(e instanceof Ps){let t=this.injector.get(xt).control;this.control=t.controls[e.name]}else this.control=new Ge,this.#e=this.injector.get(sl,null,{self:!0,optional:!0}),this.#e&&this.bindFormField(this.#e);this.updateValidators(),this.updateDisabled()}ngOnDestroy(){this.#r?.unsubscribe()}registerOnChange(e){this.onChange=t=>{e(t),this.change.emit(t)}}registerOnTouched(e){this.onTouched=e}writeValue(e){}isRequired(){return this.#i()||this.control.hasValidator(de.required)||this.control.hasValidator(Ju.requiredNotBlank)}reportErrors(e){this.#n?.set(e)}bindFormField(e){this.#n=Ji(this.injector,()=>Xu(V(null),{parse:o=>({error:em(o)}),format:()=>null}));let t=null,r=this.control.value,a=null;this.control.addValidators(()=>t),this.#r=this.control.valueChanges.subscribe(o=>{a&&!tm(o,r)&&!tm(o,W(a))&&a.set(o)}),Lt(()=>{let o=e.state(),s=o.value(),d=o.disabled(),u=o.touched(),b=o.required(),A=lo(e.errors());W(()=>{t=A,a=o.controlValue,this.#i.set(b),d!==this.control.disabled&&(d?this.control.disable({emitEvent:!1}):this.control.enable({emitEvent:!1})),r=s,this.control.setValue(s,{emitEvent:!1}),u?this.control.markAsTouched():this.control.markAsUntouched(),this.#t.markForCheck()})},{injector:this.injector})}updateValidators(){let e=this.required();e?this.control.addValidators(de.required):e===!1&&this.control.removeValidators(de.required)}updateDisabled(){setTimeout(()=>{let e=this.disabledState();e?this.control.disable():e===!1&&this.control.enable(),this.#t.markForCheck()})}static{this.\u0275fac=function(t){return new(t||i)(ie(U))}}static{this.\u0275dir=E({type:i,inputs:{errorMessageMap:[1,"errorMessageMap"],required:[1,"required"],disabled:[1,"disabled"]},outputs:{change:"change"},features:[pe]})}}return i})();var nm=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275mod=$({type:i});static \u0275inj=G({})}return i})();var im=new M("MAT_INPUT_VALUE_ACCESSOR");var rm=(()=>{class i{isErrorState(e,t){return!!(e&&e.invalid&&(e.touched||t&&t.submitted))}static \u0275fac=function(t){return new(t||i)};static \u0275prov=K({token:i,factory:i.\u0275fac,providedIn:"root"})}return i})();var uo=class{_defaultMatcher;ngControl;_parentFormGroup;_parentForm;_stateChanges;errorState=!1;matcher;constructor(n,e,t,r,a){this._defaultMatcher=n,this.ngControl=e,this._parentFormGroup=t,this._parentForm=r,this._stateChanges=a}updateErrorState(){let n=this.errorState,e=this._parentFormGroup||this._parentForm,t=this.matcher||this._defaultMatcher,r=this.ngControl?this.ngControl.control:null,a=t?.isErrorState(r,e)??!1;a!==n&&(this.errorState=a,this._stateChanges.next())}};var am=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275mod=$({type:i});static \u0275inj=G({imports:[ut,ut,nm,fe]})}return i})();var om=(()=>{class i{constructor(){this.errorKey=C(void 0),this.translateParams=C(void 0),this.errorMessageMap=C({})}getErrorMessageKey(e){return this.errorMessageMap()?.[e]||"ERRORS."+e}static{this.\u0275fac=function(t){return new(t||i)}}static{this.\u0275cmp=x({type:i,selectors:[["vitamui-form-error"]],inputs:{errorKey:[1,"errorKey"],translateParams:[1,"translateParams"],errorMessageMap:[1,"errorMessageMap"]},decls:4,vars:4,consts:[[1,"mt-1","hgap-1","align-items-start"],[1,"vitamui-icon","vitamui-icon-anomalie","danger"],[1,"text","caption","danger",3,"innerHTML"]],template:function(t,r){t&1&&(p(0,"div",0),_(1,"i",1)(2,"mat-error",2),O(3,"translate"),h()),t&2&&(c(2),f("innerHTML",Bt(3,1,r.getErrorMessageKey(r.errorKey()),r.translateParams()),ea))},dependencies:[Za,ve],styles:[`.vitamui-icon[_ngcontent-%COMP%]{line-height:20px}
/*# sourceMappingURL=/vitam-ui/develop/form-error.component-3NEI3JXS.css.map */`]})}}return i})();function j_(i,n){if(i&1&&_(0,"vitamui-form-error",0),i&2){let e=n.$implicit,t=m(2);f("errorMessageMap",t.errorMessageMap())("errorKey",e)("translateParams",t.errors[e])}}function z_(i,n){if(i&1&&Be(0,j_,1,3,"vitamui-form-error",0,at),i&2){let e=m();je(e.Object.keys(e.errors))}}var Ii=(()=>{class i{constructor(){this.control=C(void 0),this.field=C(void 0),this.errorMessageMap=C({}),this.Object=Object}get touched(){let e=this.field();return e?e().touched():!!this.control()?.touched}get errors(){let e=this.field();return e?lo(e().errors())??{}:g(g({},this.control().warnings),this.control().errors)}static{this.\u0275fac=function(t){return new(t||i)}}static{this.\u0275cmp=x({type:i,selectors:[["vitamui-form-errors"]],inputs:{control:[1,"control"],field:[1,"field"],errorMessageMap:[1,"errorMessageMap"]},decls:1,vars:1,consts:[[3,"errorMessageMap","errorKey","translateParams"]],template:function(t,r){t&1&&v(0,z_,2,0),t&2&&y(r.touched?0:-1)},dependencies:[ut,om],encapsulation:2})}}return i})();var H_=["determinateSpinner"];function U_(i,n){if(i&1&&(_t(),p(0,"svg",11),_(1,"circle",12),h()),i&2){let e=m();B("viewBox",e._viewBox()),c(),At("stroke-dasharray",e._strokeCircumference(),"px")("stroke-dashoffset",e._strokeCircumference()/2,"px")("stroke-width",e._circleStrokeWidth(),"%"),B("r",e._circleRadius())}}var W_=new M("mat-progress-spinner-default-options",{providedIn:"root",factory:()=>({diameter:sm})}),sm=100,Y_=10,lm=(()=>{class i{_elementRef=l(N);_noopAnimations;get color(){return this._color||this._defaultColor}set color(e){this._color=e}_color;_defaultColor="primary";_determinateCircle;constructor(){let e=l(W_),t=lr(),r=this._elementRef.nativeElement;this._noopAnimations=t==="di-disabled"&&!!e&&!e._forceAnimations,this.mode=r.nodeName.toLowerCase()==="mat-spinner"?"indeterminate":"determinate",!this._noopAnimations&&t==="reduced-motion"&&r.classList.add("mat-progress-spinner-reduced-motion"),e&&(e.color&&(this.color=this._defaultColor=e.color),e.diameter&&(this.diameter=e.diameter),e.strokeWidth&&(this.strokeWidth=e.strokeWidth))}mode;get value(){return this.mode==="determinate"?this._value:0}set value(e){this._value=Math.max(0,Math.min(100,e||0))}_value=0;get diameter(){return this._diameter}set diameter(e){this._diameter=e||0}_diameter=sm;get strokeWidth(){return this._strokeWidth??this.diameter/10}set strokeWidth(e){this._strokeWidth=e||0}_strokeWidth;_circleRadius(){return(this.diameter-Y_)/2}_viewBox(){let e=this._circleRadius()*2+this.strokeWidth;return`0 0 ${e} ${e}`}_strokeCircumference(){return 2*Math.PI*this._circleRadius()}_strokeDashOffset(){return this.mode==="determinate"?this._strokeCircumference()*(100-this._value)/100:null}_circleStrokeWidth(){return this.strokeWidth/this.diameter*100}static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["mat-progress-spinner"],["mat-spinner"]],viewQuery:function(t,r){if(t&1&&ke(H_,5),t&2){let a;z(a=H())&&(r._determinateCircle=a.first)}},hostAttrs:["role","progressbar","tabindex","-1",1,"mat-mdc-progress-spinner","mdc-circular-progress"],hostVars:18,hostBindings:function(t,r){t&2&&(B("aria-valuemin",0)("aria-valuemax",100)("aria-valuenow",r.mode==="determinate"?r.value:null)("mode",r.mode),Ae("mat-"+r.color),At("width",r.diameter,"px")("height",r.diameter,"px")("--mat-progress-spinner-size",r.diameter+"px")("--mat-progress-spinner-active-indicator-width",r.diameter+"px"),w("_mat-animation-noopable",r._noopAnimations)("mdc-circular-progress--indeterminate",r.mode==="indeterminate"))},inputs:{color:"color",mode:"mode",value:[2,"value","value",vt],diameter:[2,"diameter","diameter",vt],strokeWidth:[2,"strokeWidth","strokeWidth",vt]},exportAs:["matProgressSpinner"],decls:14,vars:11,consts:[["circle",""],["determinateSpinner",""],["aria-hidden","true",1,"mdc-circular-progress__determinate-container"],["xmlns","http://www.w3.org/2000/svg","focusable","false",1,"mdc-circular-progress__determinate-circle-graphic"],["cx","50%","cy","50%",1,"mdc-circular-progress__determinate-circle"],["aria-hidden","true",1,"mdc-circular-progress__indeterminate-container"],[1,"mdc-circular-progress__spinner-layer"],[1,"mdc-circular-progress__circle-clipper","mdc-circular-progress__circle-left"],[3,"ngTemplateOutlet"],[1,"mdc-circular-progress__gap-patch"],[1,"mdc-circular-progress__circle-clipper","mdc-circular-progress__circle-right"],["xmlns","http://www.w3.org/2000/svg","focusable","false",1,"mdc-circular-progress__indeterminate-circle-graphic"],["cx","50%","cy","50%"]],template:function(t,r){if(t&1&&(Oe(0,U_,2,8,"ng-template",null,0,Qt),p(2,"div",2,1),_t(),p(4,"svg",3),_(5,"circle",4),h()(),er(),p(6,"div",5)(7,"div",6)(8,"div",7),ia(9,8),h(),p(10,"div",9),ia(11,8),h(),p(12,"div",10),ia(13,8),h()()()),t&2){let a=Ke(1);c(4),B("viewBox",r._viewBox()),c(),At("stroke-dasharray",r._strokeCircumference(),"px")("stroke-dashoffset",r._strokeDashOffset(),"px")("stroke-width",r._circleStrokeWidth(),"%"),B("r",r._circleRadius()),c(4),f("ngTemplateOutlet",a),c(2),f("ngTemplateOutlet",a),c(2),f("ngTemplateOutlet",a)}},dependencies:[ln],styles:[`.mat-mdc-progress-spinner {
  --mat-progress-spinner-animation-multiplier: 1;
  display: block;
  overflow: hidden;
  line-height: 0;
  position: relative;
  direction: ltr;
  transition: opacity 250ms cubic-bezier(0.4, 0, 0.6, 1);
}
.mat-mdc-progress-spinner circle {
  stroke-width: var(--mat-progress-spinner-active-indicator-width, 4px);
}
.mat-mdc-progress-spinner._mat-animation-noopable, .mat-mdc-progress-spinner._mat-animation-noopable .mdc-circular-progress__determinate-circle {
  transition: none !important;
}
.mat-mdc-progress-spinner._mat-animation-noopable .mdc-circular-progress__indeterminate-circle-graphic,
.mat-mdc-progress-spinner._mat-animation-noopable .mdc-circular-progress__spinner-layer,
.mat-mdc-progress-spinner._mat-animation-noopable .mdc-circular-progress__indeterminate-container {
  animation: none !important;
}
.mat-mdc-progress-spinner._mat-animation-noopable .mdc-circular-progress__indeterminate-container circle {
  stroke-dasharray: 0 !important;
}
@media (forced-colors: active) {
  .mat-mdc-progress-spinner .mdc-circular-progress__indeterminate-circle-graphic,
  .mat-mdc-progress-spinner .mdc-circular-progress__determinate-circle {
    stroke: currentColor;
    stroke: CanvasText;
  }
}

.mat-progress-spinner-reduced-motion {
  --mat-progress-spinner-animation-multiplier: 1.25;
}

.mdc-circular-progress__determinate-container,
.mdc-circular-progress__indeterminate-circle-graphic,
.mdc-circular-progress__indeterminate-container,
.mdc-circular-progress__spinner-layer {
  position: absolute;
  width: 100%;
  height: 100%;
}

.mdc-circular-progress__determinate-container {
  transform: rotate(-90deg);
}
.mdc-circular-progress--indeterminate .mdc-circular-progress__determinate-container {
  opacity: 0;
}

.mdc-circular-progress__indeterminate-container {
  font-size: 0;
  letter-spacing: 0;
  white-space: nowrap;
  opacity: 0;
}
.mdc-circular-progress--indeterminate .mdc-circular-progress__indeterminate-container {
  opacity: 1;
  animation: mdc-circular-progress-container-rotate calc(1568.2352941176ms * var(--mat-progress-spinner-animation-multiplier)) linear infinite;
}

.mdc-circular-progress__determinate-circle-graphic,
.mdc-circular-progress__indeterminate-circle-graphic {
  fill: transparent;
}

.mat-mdc-progress-spinner .mdc-circular-progress__determinate-circle,
.mat-mdc-progress-spinner .mdc-circular-progress__indeterminate-circle-graphic {
  stroke: var(--mat-progress-spinner-active-indicator-color, var(--mat-sys-primary));
}
@media (forced-colors: active) {
  .mat-mdc-progress-spinner .mdc-circular-progress__determinate-circle,
  .mat-mdc-progress-spinner .mdc-circular-progress__indeterminate-circle-graphic {
    stroke: CanvasText;
  }
}

.mdc-circular-progress__determinate-circle {
  transition: stroke-dashoffset 500ms cubic-bezier(0, 0, 0.2, 1);
}

.mdc-circular-progress__gap-patch {
  position: absolute;
  top: 0;
  left: 47.5%;
  box-sizing: border-box;
  width: 5%;
  height: 100%;
  overflow: hidden;
}

.mdc-circular-progress__gap-patch .mdc-circular-progress__indeterminate-circle-graphic {
  left: -900%;
  width: 2000%;
  transform: rotate(180deg);
}
.mdc-circular-progress__circle-clipper .mdc-circular-progress__indeterminate-circle-graphic {
  width: 200%;
}
.mdc-circular-progress__circle-right .mdc-circular-progress__indeterminate-circle-graphic {
  left: -100%;
}
.mdc-circular-progress--indeterminate .mdc-circular-progress__circle-left .mdc-circular-progress__indeterminate-circle-graphic {
  animation: mdc-circular-progress-left-spin calc(1333ms * var(--mat-progress-spinner-animation-multiplier)) cubic-bezier(0.4, 0, 0.2, 1) infinite both;
}
.mdc-circular-progress--indeterminate .mdc-circular-progress__circle-right .mdc-circular-progress__indeterminate-circle-graphic {
  animation: mdc-circular-progress-right-spin calc(1333ms * var(--mat-progress-spinner-animation-multiplier)) cubic-bezier(0.4, 0, 0.2, 1) infinite both;
}

.mdc-circular-progress__circle-clipper {
  display: inline-flex;
  position: relative;
  width: 50%;
  height: 100%;
  overflow: hidden;
}

.mdc-circular-progress--indeterminate .mdc-circular-progress__spinner-layer {
  animation: mdc-circular-progress-spinner-layer-rotate calc(5332ms * var(--mat-progress-spinner-animation-multiplier)) cubic-bezier(0.4, 0, 0.2, 1) infinite both;
}

@keyframes mdc-circular-progress-container-rotate {
  to {
    transform: rotate(360deg);
  }
}
@keyframes mdc-circular-progress-spinner-layer-rotate {
  12.5% {
    transform: rotate(135deg);
  }
  25% {
    transform: rotate(270deg);
  }
  37.5% {
    transform: rotate(405deg);
  }
  50% {
    transform: rotate(540deg);
  }
  62.5% {
    transform: rotate(675deg);
  }
  75% {
    transform: rotate(810deg);
  }
  87.5% {
    transform: rotate(945deg);
  }
  100% {
    transform: rotate(1080deg);
  }
}
@keyframes mdc-circular-progress-left-spin {
  from {
    transform: rotate(265deg);
  }
  50% {
    transform: rotate(130deg);
  }
  to {
    transform: rotate(265deg);
  }
}
@keyframes mdc-circular-progress-right-spin {
  from {
    transform: rotate(-265deg);
  }
  50% {
    transform: rotate(-130deg);
  }
  to {
    transform: rotate(-265deg);
  }
}
`],encapsulation:2,changeDetection:0})}return i})();var sk=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275mod=$({type:i});static \u0275inj=G({imports:[fe]})}return i})();function q_(i,n){i&1&&(p(0,"span",9),P(1,"*"),h())}function G_(i,n){if(i&1&&(p(0,"label"),P(1),v(2,q_,2,0,"span",9),h()),i&2){let e=m(2);c(),Te(" ",e.placeholder()," "),c(),y(e.isRequired()?2:-1)}}function $_(i,n){if(i&1){let e=ze();p(0,"input",10),T("blur",function(){J(e);let r=m().$index,a=m();return ee(a.onBlur(r))})("focus",function(){J(e);let r=m().$index,a=m();return ee(a.onFocus(r))})("ngModelChange",function(r){J(e);let a=m().$index,o=m();return ee(o.onValueChange(r,a))}),h()}if(i&2){let e=m().$implicit,t=m();f("type",t.type())("ngModel",e.value)("disabled",t.control.disabled)}}function K_(i,n){if(i&1){let e=ze();p(0,"div",7)(1,"textarea",11),T("blur",function(){J(e);let r=m().$index,a=m();return ee(a.onBlur(r))})("focus",function(){J(e);let r=m().$index,a=m();return ee(a.onFocus(r))})("ngModelChange",function(r){J(e);let a=m().$index,o=m();return ee(o.onValueChange(r,a))}),h()()}if(i&2){let e=m().$implicit,t=m();B("data-replicated-value",e.value),c(),f("ngModel",e.value)("disabled",t.control.disabled)}}function Z_(i,n){if(i&1){let e=ze();p(0,"button",14),O(1,"translate"),T("click",function(){J(e);let r=m(2).$index,a=m();return ee(a.removeInput(r))}),_(2,"i",15),h()}if(i&2){let e=m(3);f("vitamuiTooltip",L(1,2,e.removeTooltipKey()))("outline",!0)}}function Q_(i,n){if(i&1){let e=ze();p(0,"button",16),O(1,"translate"),T("click",function(){J(e);let r=m(3);return ee(r.addInput())}),_(2,"i",17),h()}if(i&2){let e=m(2).$implicit,t=m();f("disabled",t.isEmpty(e.value))("vitamuiTooltip",L(1,3,t.addTooltipKey()))("outline",!0)}}function X_(i,n){if(i&1&&(p(0,"div"),v(1,Z_,3,4,"button",12),v(2,Q_,3,5,"button",13),h()),i&2){let e=m(),t=e.$implicit,r=e.$index,a=e.$count,o=m();w("hgap-1",!o.textarea())("gap-1",o.textarea()),c(),y(o.items.length>1?1:-1),c(),y(r===a-1&&(r===o.focused||!o.isEmpty(t.value))?2:-1)}}function J_(i,n){if(i&1&&(p(0,"div",4)(1,"div",5),v(2,G_,3,2,"label"),v(3,$_,1,3,"input",6),v(4,K_,2,3,"div",7),v(5,X_,3,6,"div",8),h()()),i&2){let e=n.$index,t=m();w("first",e===0)("vitamui-focused",e===t.focused),c(),w("first",e===0),c(),y(e===0?2:-1),c(),y(t.textarea()?-1:3),c(),y(t.textarea()?4:-1),c(),y(!t.control.disabled&&t.multiple()?5:-1)}}var eb={provide:dt,useExisting:qe(()=>Wn),multi:!0},Wn=(()=>{class i extends co{get isDisabled(){return this.control.disabled}get labelFloat(){return!!this.items&&!this.isEmpty(this.items[0]?.value)}onClick(e){let t=e;if(!["INPUT","TEXTAREA","BUTTON","I"].includes(t.tagName)){let r=t.querySelector("input, textarea");r?r.focus():this.elRef.nativeElement.querySelector("input:first-of-type, textarea:first-of-type").focus()}}constructor(){let e=l(U);super(e),this.elRef=l(N),this.placeholder=C(void 0),this.autofocus=C(void 0),this.multiple=C(!1),this.textarea=C(!1),this.addTooltipKey=C("INPUT.ADD_TOOLTIP"),this.removeTooltipKey=C("INPUT.REMOVE_TOOLTIP"),this.type=C("text"),this.items=[{id:0,value:""}],this.Validators=de}writeValue(e){this.items=(Array.isArray(e)?e&&e.length?e:[""]:[e||""]).map((t,r)=>({id:r,value:t.toString()}))}addInput(){let e=this.items.length-1;this.items.push({id:this.items[e].id+1,value:""}),setTimeout(()=>this.elRef.nativeElement.querySelectorAll("input, textarea")[e+1].focus())}removeInput(e){this.items.splice(e,1),this.onChange(this.items.map(t=>t.value).filter(t=>!this.isEmpty(t)))}onValueChange(e,t){this.items[t].value=e,this.onChange(this.multiple()?this.items.map(r=>r.value).filter(r=>!!r):this.items[0].value)}onFocus(e){this.focused=e}onBlur(e){this.focused=null;let t=this.items.length>1;this.isEmpty(this.items[e].value)&&t&&this.removeInput(e),this.onTouched()}isEmpty(e){return!e?.toString().replace(/\s/g,"")}trackBy(e,t){return t.id}static{this.\u0275fac=function(t){return new(t||i)}}static{this.\u0275cmp=x({type:i,selectors:[["vitamui-input"]],hostVars:6,hostBindings:function(t,r){t&1&&T("click",function(o){return r.onClick(o.target)}),t&2&&w("textarea",r.textarea())("disabled",r.isDisabled)("vitamui-float",r.labelFloat)},inputs:{placeholder:[1,"placeholder"],autofocus:[1,"autofocus"],multiple:[1,"multiple"],textarea:[1,"textarea"],addTooltipKey:[1,"addTooltipKey"],removeTooltipKey:[1,"removeTooltipKey"],type:[1,"type"]},features:[re([eb]),te],decls:5,vars:2,consts:[[1,"form-field"],[1,"item",3,"first","vitamui-focused"],["diameter","25"],[3,"control","errorMessageMap"],[1,"item"],[1,"vitamui-input"],[3,"type","ngModel","disabled"],[1,"grow-wrap"],[3,"hgap-1","gap-1"],[1,"required-marker"],[3,"blur","focus","ngModelChange","type","ngModel","disabled"],["rows","3","onInput","parentNode.dataset.replicatedValue = value",3,"blur","focus","ngModelChange","ngModel","disabled"],["type","button","vitamuiTooltipPosition","LEFT",1,"btn","btn-circle","link","primary","xsmall","remove",3,"vitamuiTooltip","outline"],["type","button","vitamuiTooltipPosition","LEFT",1,"btn","btn-circle","link","primary","xsmall",3,"disabled","vitamuiTooltip","outline"],["type","button","vitamuiTooltipPosition","LEFT",1,"btn","btn-circle","link","primary","xsmall","remove",3,"click","vitamuiTooltip","outline"],[1,"vitamui-icon","vitamui-icon-remove"],["type","button","vitamuiTooltipPosition","LEFT",1,"btn","btn-circle","link","primary","xsmall",3,"click","disabled","vitamuiTooltip","outline"],[1,"material-icons","vitamui-icon","vitamui-icon-add"]],template:function(t,r){t&1&&(p(0,"div",0),Be(1,J_,6,10,"div",1,r.trackBy,!0),_(3,"mat-spinner",2),h(),_(4,"vitamui-form-errors",3)),t&2&&(c(),je(r.items),c(3),f("control",r.control)("errorMessageMap",r.errorMessageMap()))},dependencies:[yn,vi,Xe,Hn,Yd,Ii,lm,ve],styles:[`mat-spinner[_ngcontent-%COMP%]{display:none;position:absolute;right:15px;top:18px}.ng-pending[_nghost-%COMP%]   mat-spinner[_ngcontent-%COMP%]{display:block}.ng-pending[_nghost-%COMP%]   .vitamui-input[_ngcontent-%COMP%]{padding-right:50px}.form-field[_ngcontent-%COMP%]{position:relative;box-shadow:0 0 1px rgba(var(--vitamui-grey-600-rgb),1),0 1px 2px rgba(var(--vitamui-grey-900-rgb),.4);cursor:text;border-radius:5px;border:solid 1px transparent;background-color:#fff;display:flex;flex-direction:column}.disabled[_nghost-%COMP%]   .form-field[_ngcontent-%COMP%]{background-color:var(--vitamui-grey-200)}.disabled[_nghost-%COMP%]   .form-field[_ngcontent-%COMP%]   label[_ngcontent-%COMP%], .disabled[_nghost-%COMP%]   .form-field[_ngcontent-%COMP%]   input[_ngcontent-%COMP%], .disabled[_nghost-%COMP%]   .form-field[_ngcontent-%COMP%]   textarea[_ngcontent-%COMP%]{color:var(--vitamui-grey-700)!important}.textarea[_nghost-%COMP%]   .form-field[_ngcontent-%COMP%]   .vitamui-input[_ngcontent-%COMP%]{height:100%!important;min-height:92px!important;align-items:flex-end}.textarea[_nghost-%COMP%]   .form-field[_ngcontent-%COMP%]   .vitamui-input.first[_ngcontent-%COMP%]{min-height:108px!important}.textarea[_nghost-%COMP%]   .form-field[_ngcontent-%COMP%]   label[_ngcontent-%COMP%]{top:29px}.focused[_nghost-%COMP%]   .form-field[_ngcontent-%COMP%], .form-field[_ngcontent-%COMP%]:focus-within{border:solid 1px var(--vitamui-primary);box-shadow:none!important}.ng-invalid.ng-touched[_nghost-%COMP%]   .form-field[_ngcontent-%COMP%]{border:1px solid var(--vitamui-red);box-shadow:none!important}input[_ngcontent-%COMP%]{margin-top:0!important}.vitamui-input[_ngcontent-%COMP%]:not(.first)   input[_ngcontent-%COMP%]{padding-top:0!important}.vitamui-input[_ngcontent-%COMP%]{height:44px!important;border-radius:0!important;background-color:transparent!important;box-shadow:none!important;border:none}.vitamui-input.first[_ngcontent-%COMP%]{height:58px!important}button.remove[_ngcontent-%COMP%]{display:none}.textarea[_nghost-%COMP%]   button.remove[_ngcontent-%COMP%]{display:block;visibility:hidden}.vitamui-input[_ngcontent-%COMP%]:hover   button.remove[_ngcontent-%COMP%]{display:block;visibility:visible}.vitamui-input[_ngcontent-%COMP%]{border:none!important}label[_ngcontent-%COMP%]{-webkit-user-select:none;user-select:none;cursor:text;position:absolute;line-height:0;font-size:14px;top:50%}.item[_ngcontent-%COMP%]{border-top:1px solid var(--vitamui-grey-300)}.item.first[_ngcontent-%COMP%]{border-top:none}textarea[_ngcontent-%COMP%]{padding:0}.grow-wrap[_ngcontent-%COMP%]{display:grid;width:100%}.first[_ngcontent-%COMP%]   .grow-wrap[_ngcontent-%COMP%]{margin-top:16px}.grow-wrap[_ngcontent-%COMP%]:after{content:attr(data-replicated-value) " ";white-space:pre-wrap;visibility:hidden}.grow-wrap[_ngcontent-%COMP%] > textarea[_ngcontent-%COMP%]{resize:none;overflow:hidden}.grow-wrap[_ngcontent-%COMP%] > textarea[_ngcontent-%COMP%], .grow-wrap[_ngcontent-%COMP%]:after{line-height:24px;height:100%;max-height:240px;overflow-y:auto;margin-top:0;grid-area:1/1/2/2}
/*# sourceMappingURL=/vitam-ui/develop/input.component-M66R7TZ5.css.map */`]})}}return i})();var mo=(()=>{class i{constructor(){this.control=C.required(),this.label=C(void 0),this.hint=C(void 0),this.required=C(!1)}static{this.\u0275fac=function(t){return new(t||i)}}static{this.\u0275cmp=x({type:i,selectors:[["vitamui-editor-input"]],inputs:{control:[1,"control"],label:[1,"label"],hint:[1,"hint"],required:[1,"required"]},decls:4,vars:9,consts:[[1,"w-100",3,"formControl","placeholder","required"],[3,"control","hint"]],template:function(t,r){t&1&&(_(0,"vitamui-input",0),O(1,"translate"),O(2,"empty"),_(3,"vitamui-hint",1)),t&2&&(f("formControl",r.control())("placeholder",L(2,7,L(1,5,r.label())))("required",r.required()),c(3),f("control",r.control())("hint",r.hint()))},dependencies:[qt,hn,Wn,nt,Xe,ct,et,tt,ve],encapsulation:2})}}return i})();var po=(()=>{class i{constructor(){this.control=C.required(),this.label=C(void 0),this.hint=C(void 0),this.required=C(!1)}static{this.\u0275fac=function(t){return new(t||i)}}static{this.\u0275cmp=x({type:i,selectors:[["vitamui-editor-textarea"]],inputs:{control:[1,"control"],label:[1,"label"],hint:[1,"hint"],required:[1,"required"]},decls:4,vars:10,consts:[[1,"w-100",3,"textarea","formControl","placeholder","required"],[3,"control","hint"]],template:function(t,r){t&1&&(_(0,"vitamui-input",0),O(1,"translate"),O(2,"empty"),_(3,"vitamui-hint",1)),t&2&&(f("textarea",!0)("formControl",r.control())("placeholder",L(2,8,L(1,6,r.label())))("required",r.required()),c(3),f("control",r.control())("hint",r.hint()))},dependencies:[qt,hn,nt,Xe,ct,et,Wn,tt,ve],encapsulation:2})}}return i})();var Rr=class{_multiple;_emitChanges;compareWith;_selection=new Set;_deselectedToEmit=[];_selectedToEmit=[];_selected=null;get selected(){return this._selected||(this._selected=Array.from(this._selection.values())),this._selected}changed=new F;constructor(n=!1,e,t=!0,r){this._multiple=n,this._emitChanges=t,this.compareWith=r,e&&e.length&&(n?e.forEach(a=>this._markSelected(a)):this._markSelected(e[0]),this._selectedToEmit.length=0)}select(...n){this._verifyValueAssignment(n),n.forEach(t=>this._markSelected(t));let e=this._hasQueuedChanges();return this._emitChangeEvent(),e}deselect(...n){this._verifyValueAssignment(n),n.forEach(t=>this._unmarkSelected(t));let e=this._hasQueuedChanges();return this._emitChangeEvent(),e}setSelection(...n){this._verifyValueAssignment(n);let e=this.selected,t=new Set(n.map(a=>this._getConcreteValue(a)));n.forEach(a=>this._markSelected(a)),e.filter(a=>!t.has(this._getConcreteValue(a,t))).forEach(a=>this._unmarkSelected(a));let r=this._hasQueuedChanges();return this._emitChangeEvent(),r}toggle(n){return this.isSelected(n)?this.deselect(n):this.select(n)}clear(n=!0){this._unmarkAll();let e=this._hasQueuedChanges();return n&&this._emitChangeEvent(),e}isSelected(n){return this._selection.has(this._getConcreteValue(n))}isEmpty(){return this._selection.size===0}hasValue(){return!this.isEmpty()}sort(n){this._multiple&&this.selected&&this._selected.sort(n)}isMultipleSelection(){return this._multiple}_emitChangeEvent(){this._selected=null,(this._selectedToEmit.length||this._deselectedToEmit.length)&&(this.changed.next({source:this,added:this._selectedToEmit,removed:this._deselectedToEmit}),this._deselectedToEmit=[],this._selectedToEmit=[])}_markSelected(n){n=this._getConcreteValue(n),this.isSelected(n)||(this._multiple||this._unmarkAll(),this.isSelected(n)||this._selection.add(n),this._emitChanges&&this._selectedToEmit.push(n))}_unmarkSelected(n){n=this._getConcreteValue(n),this.isSelected(n)&&(this._selection.delete(n),this._emitChanges&&this._deselectedToEmit.push(n))}_unmarkAll(){this.isEmpty()||this._selection.forEach(n=>this._unmarkSelected(n))}_verifyValueAssignment(n){n.length>1&&this._multiple}_hasQueuedChanges(){return!!(this._deselectedToEmit.length||this._selectedToEmit.length)}_getConcreteValue(n,e){if(this.compareWith){e=e??this._selection;for(let t of e)if(this.compareWith(n,t))return t;return n}else return n}};var tb=(()=>{class i{_listeners=[];notify(e,t){for(let r of this._listeners)r(e,t)}listen(e){return this._listeners.push(e),()=>{this._listeners=this._listeners.filter(t=>e!==t)}}ngOnDestroy(){this._listeners=[]}static \u0275fac=function(t){return new(t||i)};static \u0275prov=K({token:i,factory:i.\u0275fac,providedIn:"root"})}return i})();var dm=class{applyChanges(n,e,t,r,a){n.forEachOperation((o,s,d)=>{let u,b;if(o.previousIndex==null){let A=t(o,s,d);u=e.createEmbeddedView(A.templateRef,A.context,A.index),b=ma.INSERTED}else d==null?(e.remove(s),b=ma.REMOVED):(u=e.get(s),e.move(u,d),b=ma.MOVED);a&&a({context:u?.context,operation:b,record:o})})}detach(){}};var cm=(()=>{class i{_animationsDisabled=ye();state="unchecked";disabled=!1;appearance="full";constructor(){}static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["mat-pseudo-checkbox"]],hostAttrs:[1,"mat-pseudo-checkbox"],hostVars:12,hostBindings:function(t,r){t&2&&w("mat-pseudo-checkbox-indeterminate",r.state==="indeterminate")("mat-pseudo-checkbox-checked",r.state==="checked")("mat-pseudo-checkbox-disabled",r.disabled)("mat-pseudo-checkbox-minimal",r.appearance==="minimal")("mat-pseudo-checkbox-full",r.appearance==="full")("_mat-animation-noopable",r._animationsDisabled)},inputs:{state:"state",disabled:"disabled",appearance:"appearance"},decls:0,vars:0,template:function(t,r){},styles:[`.mat-pseudo-checkbox {
  border-radius: 2px;
  cursor: pointer;
  display: inline-block;
  vertical-align: middle;
  box-sizing: border-box;
  position: relative;
  flex-shrink: 0;
  transition: border-color 90ms cubic-bezier(0, 0, 0.2, 0.1), background-color 90ms cubic-bezier(0, 0, 0.2, 0.1);
}
.mat-pseudo-checkbox::after {
  position: absolute;
  opacity: 0;
  content: "";
  border-bottom: 2px solid currentColor;
  transition: opacity 90ms cubic-bezier(0, 0, 0.2, 0.1);
}
.mat-pseudo-checkbox._mat-animation-noopable {
  transition: none !important;
  animation: none !important;
}
.mat-pseudo-checkbox._mat-animation-noopable::after {
  transition: none;
}

.mat-pseudo-checkbox-disabled {
  cursor: default;
}

.mat-pseudo-checkbox-indeterminate::after {
  left: 1px;
  opacity: 1;
  border-radius: 2px;
}

.mat-pseudo-checkbox-checked::after {
  left: 1px;
  border-left: 2px solid currentColor;
  transform: rotate(-45deg);
  opacity: 1;
  box-sizing: content-box;
}

.mat-pseudo-checkbox-minimal.mat-pseudo-checkbox-checked::after, .mat-pseudo-checkbox-minimal.mat-pseudo-checkbox-indeterminate::after {
  color: var(--mat-pseudo-checkbox-minimal-selected-checkmark-color, var(--mat-sys-primary));
}
.mat-pseudo-checkbox-minimal.mat-pseudo-checkbox-checked.mat-pseudo-checkbox-disabled::after, .mat-pseudo-checkbox-minimal.mat-pseudo-checkbox-indeterminate.mat-pseudo-checkbox-disabled::after {
  color: var(--mat-pseudo-checkbox-minimal-disabled-selected-checkmark-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}

.mat-pseudo-checkbox-full {
  border-color: var(--mat-pseudo-checkbox-full-unselected-icon-color, var(--mat-sys-on-surface-variant));
  border-width: 2px;
  border-style: solid;
}
.mat-pseudo-checkbox-full.mat-pseudo-checkbox-disabled {
  border-color: var(--mat-pseudo-checkbox-full-disabled-unselected-icon-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}
.mat-pseudo-checkbox-full.mat-pseudo-checkbox-checked, .mat-pseudo-checkbox-full.mat-pseudo-checkbox-indeterminate {
  background-color: var(--mat-pseudo-checkbox-full-selected-icon-color, var(--mat-sys-primary));
  border-color: transparent;
}
.mat-pseudo-checkbox-full.mat-pseudo-checkbox-checked::after, .mat-pseudo-checkbox-full.mat-pseudo-checkbox-indeterminate::after {
  color: var(--mat-pseudo-checkbox-full-selected-checkmark-color, var(--mat-sys-on-primary));
}
.mat-pseudo-checkbox-full.mat-pseudo-checkbox-checked.mat-pseudo-checkbox-disabled, .mat-pseudo-checkbox-full.mat-pseudo-checkbox-indeterminate.mat-pseudo-checkbox-disabled {
  background-color: var(--mat-pseudo-checkbox-full-disabled-selected-icon-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}
.mat-pseudo-checkbox-full.mat-pseudo-checkbox-checked.mat-pseudo-checkbox-disabled::after, .mat-pseudo-checkbox-full.mat-pseudo-checkbox-indeterminate.mat-pseudo-checkbox-disabled::after {
  color: var(--mat-pseudo-checkbox-full-disabled-selected-checkmark-color, var(--mat-sys-surface));
}

.mat-pseudo-checkbox {
  width: 18px;
  height: 18px;
}

.mat-pseudo-checkbox-minimal.mat-pseudo-checkbox-checked::after {
  width: 14px;
  height: 6px;
  transform-origin: center;
  top: -4.2426406871px;
  left: 0;
  bottom: 0;
  right: 0;
  margin: auto;
}
.mat-pseudo-checkbox-minimal.mat-pseudo-checkbox-indeterminate::after {
  top: 8px;
  width: 16px;
}

.mat-pseudo-checkbox-full.mat-pseudo-checkbox-checked::after {
  width: 10px;
  height: 4px;
  transform-origin: center;
  top: -2.8284271247px;
  left: 0;
  bottom: 0;
  right: 0;
  margin: auto;
}
.mat-pseudo-checkbox-full.mat-pseudo-checkbox-indeterminate::after {
  top: 6px;
  width: 12px;
}
`],encapsulation:2,changeDetection:0})}return i})();var nb=["text"],ib=[[["mat-icon"]],"*"],rb=["mat-icon","*"];function ab(i,n){if(i&1&&_(0,"mat-pseudo-checkbox",1),i&2){let e=m();f("disabled",e.disabled)("state",e.selected?"checked":"unchecked")}}function ob(i,n){if(i&1&&_(0,"mat-pseudo-checkbox",3),i&2){let e=m();f("disabled",e.disabled)}}function sb(i,n){if(i&1&&(p(0,"span",4),P(1),h()),i&2){let e=m();c(),Te("(",e.group.label,")")}}var cl=new M("MAT_OPTION_PARENT_COMPONENT"),ul=new M("MatOptgroup");var dl=class{source;isUserInput;constructor(n,e=!1){this.source=n,this.isUserInput=e}},Oi=(()=>{class i{_element=l(N);_changeDetectorRef=l(ne);_parent=l(cl,{optional:!0});group=l(ul,{optional:!0});_signalDisableRipple=!1;_selected=!1;_active=!1;_mostRecentViewValue="";get multiple(){return this._parent&&this._parent.multiple}get selected(){return this._selected}value;id=l(Re).getId("mat-option-");get disabled(){return this.group&&this.group.disabled||this._disabled()}set disabled(e){this._disabled.set(e)}_disabled=V(!1);get disableRipple(){return this._signalDisableRipple?this._parent.disableRipple():!!this._parent?.disableRipple}get hideSingleSelectionIndicator(){return!!(this._parent&&this._parent.hideSingleSelectionIndicator)}onSelectionChange=new S;_text;_stateChanges=new F;constructor(){let e=l(Ue);e.load(_n),e.load(Ut),this._signalDisableRipple=!!this._parent&&nr(this._parent.disableRipple)}get active(){return this._active}get viewValue(){return(this._text?.nativeElement.textContent||"").trim()}select(e=!0){this._selected||(this._selected=!0,this._changeDetectorRef.markForCheck(),e&&this._emitSelectionChangeEvent())}deselect(e=!0){this._selected&&(this._selected=!1,this._changeDetectorRef.markForCheck(),e&&this._emitSelectionChangeEvent())}focus(e,t){let r=this._getHostElement();typeof r.focus=="function"&&r.focus(t)}setActiveStyles(){this._active||(this._active=!0,this._changeDetectorRef.markForCheck())}setInactiveStyles(){this._active&&(this._active=!1,this._changeDetectorRef.markForCheck())}getLabel(){return this.viewValue}_handleKeydown(e){(e.keyCode===13||e.keyCode===32)&&!xe(e)&&(this._selectViaInteraction(),e.preventDefault())}_selectViaInteraction(){this.disabled||(this._selected=this.multiple?!this._selected:!0,this._changeDetectorRef.markForCheck(),this._emitSelectionChangeEvent(!0))}_getTabIndex(){return this.disabled?"-1":"0"}_getHostElement(){return this._element.nativeElement}ngAfterViewChecked(){if(this._selected){let e=this.viewValue;e!==this._mostRecentViewValue&&(this._mostRecentViewValue&&this._stateChanges.next(),this._mostRecentViewValue=e)}}ngOnDestroy(){this._stateChanges.complete()}_emitSelectionChangeEvent(e=!1){this.onSelectionChange.emit(new dl(this,e))}static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["mat-option"]],viewQuery:function(t,r){if(t&1&&ke(nb,7),t&2){let a;z(a=H())&&(r._text=a.first)}},hostAttrs:["role","option",1,"mat-mdc-option","mdc-list-item"],hostVars:11,hostBindings:function(t,r){t&1&&T("click",function(){return r._selectViaInteraction()})("keydown",function(o){return r._handleKeydown(o)}),t&2&&(He("id",r.id),B("aria-selected",r.selected)("aria-disabled",r.disabled.toString()),w("mdc-list-item--selected",r.selected)("mat-mdc-option-multiple",r.multiple)("mat-mdc-option-active",r.active)("mdc-list-item--disabled",r.disabled))},inputs:{value:"value",id:"id",disabled:[2,"disabled","disabled",Y]},outputs:{onSelectionChange:"onSelectionChange"},exportAs:["matOption"],ngContentSelectors:rb,decls:8,vars:5,consts:[["text",""],["aria-hidden","true",1,"mat-mdc-option-pseudo-checkbox",3,"disabled","state"],[1,"mdc-list-item__primary-text"],["state","checked","aria-hidden","true","appearance","minimal",1,"mat-mdc-option-pseudo-checkbox",3,"disabled"],[1,"cdk-visually-hidden"],["aria-hidden","true","mat-ripple","",1,"mat-mdc-option-ripple","mat-focus-indicator",3,"matRippleTrigger","matRippleDisabled"]],template:function(t,r){t&1&&(Ee(ib),v(0,ab,1,2,"mat-pseudo-checkbox",1),Z(1),p(2,"span",2,0),Z(4,1),h(),v(5,ob,1,1,"mat-pseudo-checkbox",3),v(6,sb,2,1,"span",4),_(7,"div",5)),t&2&&(y(r.multiple?0:-1),c(5),y(!r.multiple&&r.selected&&!r.hideSingleSelectionIndicator?5:-1),c(),y(r.group&&r.group._inert?6:-1),c(),f("matRippleTrigger",r._getHostElement())("matRippleDisabled",r.disabled||r.disableRipple))},dependencies:[cm,Aa],styles:[`.mat-mdc-option {
  -webkit-user-select: none;
  user-select: none;
  -moz-osx-font-smoothing: grayscale;
  -webkit-font-smoothing: antialiased;
  display: flex;
  position: relative;
  align-items: center;
  justify-content: flex-start;
  overflow: hidden;
  min-height: 48px;
  padding: 0 16px;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  color: var(--mat-option-label-text-color, var(--mat-sys-on-surface));
  font-family: var(--mat-option-label-text-font, var(--mat-sys-label-large-font));
  line-height: var(--mat-option-label-text-line-height, var(--mat-sys-label-large-line-height));
  font-size: var(--mat-option-label-text-size, var(--mat-sys-body-large-size));
  letter-spacing: var(--mat-option-label-text-tracking, var(--mat-sys-label-large-tracking));
  font-weight: var(--mat-option-label-text-weight, var(--mat-sys-body-large-weight));
}
.mat-mdc-option:hover:not(.mdc-list-item--disabled) {
  background-color: var(--mat-option-hover-state-layer-color, color-mix(in srgb, var(--mat-sys-on-surface) calc(var(--mat-sys-hover-state-layer-opacity) * 100%), transparent));
}
.mat-mdc-option:focus.mdc-list-item, .mat-mdc-option.mat-mdc-option-active.mdc-list-item {
  background-color: var(--mat-option-focus-state-layer-color, color-mix(in srgb, var(--mat-sys-on-surface) calc(var(--mat-sys-focus-state-layer-opacity) * 100%), transparent));
  outline: 0;
}
.mat-mdc-option.mdc-list-item--selected:not(.mdc-list-item--disabled):not(.mat-mdc-option-active, .mat-mdc-option-multiple, :focus, :hover) {
  background-color: var(--mat-option-selected-state-layer-color, var(--mat-sys-secondary-container));
}
.mat-mdc-option.mdc-list-item--selected:not(.mdc-list-item--disabled):not(.mat-mdc-option-active, .mat-mdc-option-multiple, :focus, :hover) .mdc-list-item__primary-text {
  color: var(--mat-option-selected-state-label-text-color, var(--mat-sys-on-secondary-container));
}
.mat-mdc-option .mat-pseudo-checkbox {
  --mat-pseudo-checkbox-minimal-selected-checkmark-color: var(--mat-option-selected-state-label-text-color, var(--mat-sys-on-secondary-container));
}
.mat-mdc-option.mdc-list-item {
  align-items: center;
  background: transparent;
}
.mat-mdc-option.mdc-list-item--disabled {
  cursor: default;
  pointer-events: none;
}
.mat-mdc-option.mdc-list-item--disabled .mat-mdc-option-pseudo-checkbox, .mat-mdc-option.mdc-list-item--disabled .mdc-list-item__primary-text, .mat-mdc-option.mdc-list-item--disabled > mat-icon {
  opacity: 0.38;
}
.mat-mdc-optgroup .mat-mdc-option:not(.mat-mdc-option-multiple) {
  padding-left: 32px;
}
[dir=rtl] .mat-mdc-optgroup .mat-mdc-option:not(.mat-mdc-option-multiple) {
  padding-left: 16px;
  padding-right: 32px;
}
.mat-mdc-option .mat-icon,
.mat-mdc-option .mat-pseudo-checkbox-full {
  margin-right: 16px;
  flex-shrink: 0;
}
[dir=rtl] .mat-mdc-option .mat-icon,
[dir=rtl] .mat-mdc-option .mat-pseudo-checkbox-full {
  margin-right: 0;
  margin-left: 16px;
}
.mat-mdc-option .mat-pseudo-checkbox-minimal {
  margin-left: 16px;
  flex-shrink: 0;
}
[dir=rtl] .mat-mdc-option .mat-pseudo-checkbox-minimal {
  margin-right: 16px;
  margin-left: 0;
}
.mat-mdc-option .mat-mdc-option-ripple {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  position: absolute;
  pointer-events: none;
}
.mat-mdc-option .mdc-list-item__primary-text {
  white-space: normal;
  font-size: inherit;
  font-weight: inherit;
  letter-spacing: inherit;
  line-height: inherit;
  font-family: inherit;
  text-decoration: inherit;
  text-transform: inherit;
  margin-right: auto;
}
[dir=rtl] .mat-mdc-option .mdc-list-item__primary-text {
  margin-right: 0;
  margin-left: auto;
}
@media (forced-colors: active) {
  .mat-mdc-option.mdc-list-item--selected:not(:has(.mat-mdc-option-pseudo-checkbox))::after {
    content: "";
    position: absolute;
    top: 50%;
    right: 16px;
    transform: translateY(-50%);
    width: 10px;
    height: 0;
    border-bottom: solid 10px;
    border-radius: 10px;
  }
  [dir=rtl] .mat-mdc-option.mdc-list-item--selected:not(:has(.mat-mdc-option-pseudo-checkbox))::after {
    right: auto;
    left: 16px;
  }
}

.mat-mdc-option-multiple {
  --mat-list-list-item-selected-container-color: var(--mat-list-list-item-container-color, transparent);
}

.mat-mdc-option-active .mat-focus-indicator::before {
  content: "";
}
`],encapsulation:2,changeDetection:0})}return i})();function um(i,n,e){if(e.length){let t=n.toArray(),r=e.toArray(),a=0;for(let o=0;o<i+1;o++)t[o].group&&t[o].group===r[a]&&a++;return a}return 0}function mm(i,n,e,t){return i<e?i:i+n>e+t?Math.max(0,i-t+n):e}var pm=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275mod=$({type:i});static \u0275inj=G({imports:[fe]})}return i})();var ml=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275mod=$({type:i});static \u0275inj=G({imports:[hi,pm,Oi,fe]})}return i})();var lb=["trigger"],db=["panel"],cb=[[["mat-select-trigger"]],"*"],ub=["mat-select-trigger","*"];function mb(i,n){if(i&1&&(p(0,"span",4),P(1),h()),i&2){let e=m();c(),ce(e.placeholder)}}function pb(i,n){i&1&&Z(0)}function hb(i,n){if(i&1&&(p(0,"span",11),P(1),h()),i&2){let e=m(2);c(),ce(e.triggerValue)}}function fb(i,n){if(i&1&&(p(0,"span",5),v(1,pb,1,0)(2,hb,2,1,"span",11),h()),i&2){let e=m();c(),y(e.customTrigger?1:2)}}function gb(i,n){if(i&1){let e=ze();p(0,"div",12,1),T("keydown",function(r){J(e);let a=m();return ee(a._handleKeydown(r))}),Z(2,1),h()}if(i&2){let e=m();Ae(e.panelClass),w("mat-select-panel-animations-enabled",!e._animationsDisabled)("mat-primary",(e._parentFormField==null?null:e._parentFormField.color)==="primary")("mat-accent",(e._parentFormField==null?null:e._parentFormField.color)==="accent")("mat-warn",(e._parentFormField==null?null:e._parentFormField.color)==="warn")("mat-undefined",!(e._parentFormField!=null&&e._parentFormField.color)),B("id",e.id+"-panel")("aria-multiselectable",e.multiple)("aria-label",e.ariaLabel||null)("aria-labelledby",e._getPanelAriaLabelledby())}}var _b=new M("mat-select-scroll-strategy",{providedIn:"root",factory:()=>{let i=l(U);return()=>mn(i)}}),bb=new M("MAT_SELECT_CONFIG"),hm=new M("MatSelectTrigger"),pl=class{source;value;constructor(n,e){this.source=n,this.value=e}},fm=(()=>{class i{_viewportRuler=l(Ko);_changeDetectorRef=l(ne);_elementRef=l(N);_dir=l(Ze,{optional:!0});_idGenerator=l(Re);_renderer=l(we);_parentFormField=l(Er,{optional:!0});ngControl=l(Qe,{self:!0,optional:!0});_liveAnnouncer=l(is);_defaultOptions=l(bb,{optional:!0});_animationsDisabled=ye();_popoverLocation;_initialized=new F;_cleanupDetach;options;optionGroups;customTrigger;_positions=[{originX:"start",originY:"bottom",overlayX:"start",overlayY:"top"},{originX:"end",originY:"bottom",overlayX:"end",overlayY:"top"},{originX:"start",originY:"top",overlayX:"start",overlayY:"bottom",panelClass:"mat-mdc-select-panel-above"},{originX:"end",originY:"top",overlayX:"end",overlayY:"bottom",panelClass:"mat-mdc-select-panel-above"}];_scrollOptionIntoView(e){let t=this.options.toArray()[e];if(t){let r=this.panel.nativeElement,a=um(e,this.options,this.optionGroups),o=t._getHostElement();e===0&&a===1?r.scrollTop=0:r.scrollTop=mm(o.offsetTop,o.offsetHeight,r.scrollTop,r.offsetHeight)}}_positioningSettled(){this._scrollOptionIntoView(this._keyManager.activeItemIndex||0)}_getChangeEvent(e){return new pl(this,e)}_scrollStrategyFactory=l(_b);_panelOpen=!1;_compareWith=(e,t)=>e===t;_uid=this._idGenerator.getId("mat-select-");_triggerAriaLabelledBy=null;_previousControl;_destroy=new F;_errorStateTracker;stateChanges=new F;disableAutomaticLabeling=!0;userAriaDescribedBy;_selectionModel;_keyManager;_preferredOverlayOrigin;_overlayWidth;_onChange=()=>{};_onTouched=()=>{};_valueId=this._idGenerator.getId("mat-select-value-");_scrollStrategy;_overlayPanelClass=this._defaultOptions?.overlayPanelClass||"";get focused(){return this._focused||this._panelOpen}_focused=!1;controlType="mat-select";trigger;panel;_overlayDir;panelClass;disabled=!1;get disableRipple(){return this._disableRipple()}set disableRipple(e){this._disableRipple.set(e)}_disableRipple=V(!1);tabIndex=0;get hideSingleSelectionIndicator(){return this._hideSingleSelectionIndicator}set hideSingleSelectionIndicator(e){this._hideSingleSelectionIndicator=e,this._syncParentProperties()}_hideSingleSelectionIndicator=this._defaultOptions?.hideSingleSelectionIndicator??!1;get placeholder(){return this._placeholder}set placeholder(e){this._placeholder=e,this.stateChanges.next()}_placeholder;get required(){return this._required??this.ngControl?.control?.hasValidator(de.required)??!1}set required(e){this._required=e,this.stateChanges.next()}_required;get multiple(){return this._multiple}set multiple(e){this._selectionModel,this._multiple=e}_multiple=!1;disableOptionCentering=this._defaultOptions?.disableOptionCentering??!1;get compareWith(){return this._compareWith}set compareWith(e){this._compareWith=e,this._selectionModel&&this._initializeSelection()}get value(){return this._value}set value(e){this._assignValue(e)&&this._onChange(e)}_value;ariaLabel="";ariaLabelledby;get errorStateMatcher(){return this._errorStateTracker.matcher}set errorStateMatcher(e){this._errorStateTracker.matcher=e}typeaheadDebounceInterval;sortComparator;get id(){return this._id}set id(e){this._id=e||this._uid,this.stateChanges.next()}_id;get errorState(){return this._errorStateTracker.errorState}set errorState(e){this._errorStateTracker.errorState=e}panelWidth=this._defaultOptions&&typeof this._defaultOptions.panelWidth<"u"?this._defaultOptions.panelWidth:"auto";canSelectNullableOptions=this._defaultOptions?.canSelectNullableOptions??!1;optionSelectionChanges=Zn(()=>{let e=this.options;return e?e.changes.pipe(Le(e),an(()=>$e(...e.map(t=>t.onSelectionChange)))):this._initialized.pipe(an(()=>this.optionSelectionChanges))});openedChange=new S;_openedStream=this.openedChange.pipe(De(e=>e),Fe(()=>{}));_closedStream=this.openedChange.pipe(De(e=>!e),Fe(()=>{}));selectionChange=new S;valueChange=new S;constructor(){let e=l(rm),t=l(Ga,{optional:!0}),r=l($a,{optional:!0}),a=l(new ii("tabindex"),{optional:!0}),o=l(Ld,{optional:!0});this.ngControl&&(this.ngControl.valueAccessor=this),this._defaultOptions?.typeaheadDebounceInterval!=null&&(this.typeaheadDebounceInterval=this._defaultOptions.typeaheadDebounceInterval),this._errorStateTracker=new uo(e,this.ngControl,r,t,this.stateChanges),this._scrollStrategy=this._scrollStrategyFactory(),this.tabIndex=a==null?0:parseInt(a)||0,this._popoverLocation=o?.usePopover===!1?null:"inline",this.id=this.id}ngOnInit(){this._selectionModel=new Rr(this.multiple),this.stateChanges.next(),this._viewportRuler.change().pipe(ge(this._destroy)).subscribe(()=>{this.panelOpen&&(this._overlayWidth=this._getOverlayWidth(this._preferredOverlayOrigin),this._changeDetectorRef.detectChanges())})}ngAfterContentInit(){this._initialized.next(),this._initialized.complete(),this._initKeyManager(),this._selectionModel.changed.pipe(ge(this._destroy)).subscribe(e=>{e.added.forEach(t=>t.select()),e.removed.forEach(t=>t.deselect())}),this.options.changes.pipe(Le(null),ge(this._destroy)).subscribe(()=>{this._resetOptions(),this._initializeSelection()})}ngDoCheck(){let e=this._getTriggerAriaLabelledby(),t=this.ngControl;if(e!==this._triggerAriaLabelledBy){let r=this._elementRef.nativeElement;this._triggerAriaLabelledBy=e,e?r.setAttribute("aria-labelledby",e):r.removeAttribute("aria-labelledby")}t&&(this._previousControl!==t.control&&(this._previousControl!==void 0&&t.disabled!==null&&t.disabled!==this.disabled&&(this.disabled=t.disabled),this._previousControl=t.control),this.updateErrorState())}ngOnChanges(e){(e.disabled||e.userAriaDescribedBy)&&this.stateChanges.next(),e.typeaheadDebounceInterval&&this._keyManager&&this._keyManager.withTypeAhead(this.typeaheadDebounceInterval),e.panelClass&&this.panelClass instanceof Set&&(this.panelClass=Array.from(this.panelClass))}ngOnDestroy(){this._cleanupDetach?.(),this._keyManager?.destroy(),this._destroy.next(),this._destroy.complete(),this.stateChanges.complete(),this._clearFromModal()}toggle(){this.panelOpen?this.close():this.open()}open(){this._canOpen()&&(this._parentFormField&&(this._preferredOverlayOrigin=this._parentFormField.getConnectedOverlayOrigin()),this._cleanupDetach?.(),this._overlayWidth=this._getOverlayWidth(this._preferredOverlayOrigin),this._applyModalPanelOwnership(),this._panelOpen=!0,this._overlayDir.positionChange.pipe(Ne(1)).subscribe(()=>{this._changeDetectorRef.detectChanges(),this._positioningSettled()}),this._overlayDir.attachOverlay(),this._keyManager.withHorizontalOrientation(null),this._highlightCorrectOption(),this._changeDetectorRef.markForCheck(),this.stateChanges.next(),Promise.resolve().then(()=>this.openedChange.emit(!0)))}_trackedModal=null;_applyModalPanelOwnership(){let e=this._elementRef.nativeElement.closest('body > .cdk-overlay-container [aria-modal="true"]');if(!e)return;let t=`${this.id}-panel`;this._trackedModal&&wa(this._trackedModal,"aria-owns",t),us(e,"aria-owns",t),this._trackedModal=e}_clearFromModal(){if(!this._trackedModal)return;let e=`${this.id}-panel`;wa(this._trackedModal,"aria-owns",e),this._trackedModal=null}close(){this._panelOpen&&(this._panelOpen=!1,this._exitAndDetach(),this._keyManager.withHorizontalOrientation(this._isRtl()?"rtl":"ltr"),this._changeDetectorRef.markForCheck(),this._onTouched(),this.stateChanges.next(),Promise.resolve().then(()=>this.openedChange.emit(!1)))}_exitAndDetach(){if(this._animationsDisabled||!this.panel){this._detachOverlay();return}this._cleanupDetach?.(),this._cleanupDetach=()=>{t(),clearTimeout(r),this._cleanupDetach=void 0};let e=this.panel.nativeElement,t=this._renderer.listen(e,"animationend",a=>{a.animationName==="_mat-select-exit"&&(this._cleanupDetach?.(),this._detachOverlay())}),r=setTimeout(()=>{this._cleanupDetach?.(),this._detachOverlay()},200);e.classList.add("mat-select-panel-exit")}_detachOverlay(){this._overlayDir.detachOverlay(),this._changeDetectorRef.markForCheck()}writeValue(e){this._assignValue(e)}registerOnChange(e){this._onChange=e}registerOnTouched(e){this._onTouched=e}setDisabledState(e){this.disabled=e,this._changeDetectorRef.markForCheck(),this.stateChanges.next()}get panelOpen(){return this._panelOpen}get selected(){return this.multiple?this._selectionModel?.selected||[]:this._selectionModel?.selected[0]}get triggerValue(){if(this.empty)return"";if(this._multiple){let e=this._selectionModel.selected.map(t=>t.viewValue);return this._isRtl()&&e.reverse(),e.join(", ")}return this._selectionModel.selected[0].viewValue}updateErrorState(){this._errorStateTracker.updateErrorState()}_isRtl(){return this._dir?this._dir.value==="rtl":!1}_handleKeydown(e){this.disabled||(this.panelOpen?this._handleOpenKeydown(e):this._handleClosedKeydown(e))}_handleClosedKeydown(e){let t=e.keyCode,r=t===40||t===38||t===37||t===39,a=t===13||t===32,o=this._keyManager;if(!o.isTyping()&&a&&!xe(e)||(this.multiple||e.altKey)&&r)e.preventDefault(),this.open();else if(!this.multiple){let s=this.selected;o.onKeydown(e);let d=this.selected;d&&s!==d&&this._liveAnnouncer.announce(d.viewValue,1e4)}}_handleOpenKeydown(e){let t=this._keyManager,r=e.keyCode,a=r===40||r===38,o=t.isTyping();if(a&&e.altKey)e.preventDefault(),this.close();else if(!o&&(r===13||r===32)&&t.activeItem&&!xe(e))e.preventDefault(),t.activeItem._selectViaInteraction();else if(!o&&this._multiple&&r===65&&e.ctrlKey){e.preventDefault();let s=this.options.some(d=>!d.disabled&&!d.selected);this.options.forEach(d=>{d.disabled||(s?d.select():d.deselect())})}else{let s=t.activeItemIndex;t.onKeydown(e),this._multiple&&a&&e.shiftKey&&t.activeItem&&t.activeItemIndex!==s&&t.activeItem._selectViaInteraction()}}_handleOverlayKeydown(e){e.keyCode===27&&!xe(e)&&(e.preventDefault(),this.close())}_onFocus(){this.disabled||(this._focused=!0,this.stateChanges.next())}_onBlur(){this._focused=!1,this._keyManager?.cancelTypeahead(),!this.disabled&&!this.panelOpen&&(this._onTouched(),this._changeDetectorRef.markForCheck(),this.stateChanges.next())}get empty(){return!this._selectionModel||this._selectionModel.isEmpty()}_initializeSelection(){Promise.resolve().then(()=>{this.ngControl&&(this._value=this.ngControl.value),this._setSelectionByValue(this._value),this.stateChanges.next()})}_setSelectionByValue(e){if(this.options.forEach(t=>t.setInactiveStyles()),this._selectionModel.clear(),this.multiple&&e)Array.isArray(e),e.forEach(t=>this._selectOptionByValue(t)),this._sortValues();else{let t=this._selectOptionByValue(e);t?this._keyManager.updateActiveItem(t):this.panelOpen||this._keyManager.updateActiveItem(-1)}this._changeDetectorRef.markForCheck()}_selectOptionByValue(e){let t=this.options.find(r=>{if(this._selectionModel.isSelected(r))return!1;try{return(r.value!=null||this.canSelectNullableOptions)&&this._compareWith(r.value,e)}catch{return!1}});return t&&this._selectionModel.select(t),t}_assignValue(e){return e!==this._value||this._multiple&&Array.isArray(e)?(this.options&&this._setSelectionByValue(e),this._value=e,!0):!1}_skipPredicate=e=>this.panelOpen?!1:e.disabled;_getOverlayWidth(e){return this.panelWidth==="auto"?(e instanceof Zo?e.elementRef:e||this._elementRef).nativeElement.getBoundingClientRect().width:this.panelWidth===null?"":this.panelWidth}_syncParentProperties(){if(this.options)for(let e of this.options)e._changeDetectorRef.markForCheck()}_initKeyManager(){this._keyManager=new ur(this.options).withTypeAhead(this.typeaheadDebounceInterval).withVerticalOrientation().withHorizontalOrientation(this._isRtl()?"rtl":"ltr").withHomeAndEnd().withPageUpDown().withAllowedModifierKeys(["shiftKey"]).skipPredicate(this._skipPredicate),this._keyManager.tabOut.subscribe(()=>{this.panelOpen&&(!this.multiple&&this._keyManager.activeItem&&this._keyManager.activeItem._selectViaInteraction(),this.focus(),this.close())}),this._keyManager.change.subscribe(()=>{this._panelOpen&&this.panel?this._scrollOptionIntoView(this._keyManager.activeItemIndex||0):!this._panelOpen&&!this.multiple&&this._keyManager.activeItem&&this._keyManager.activeItem._selectViaInteraction()})}_resetOptions(){let e=$e(this.options.changes,this._destroy);this.optionSelectionChanges.pipe(ge(e)).subscribe(t=>{this._onSelect(t.source,t.isUserInput),t.isUserInput&&!this.multiple&&this._panelOpen&&(this.close(),this.focus())}),$e(...this.options.map(t=>t._stateChanges)).pipe(ge(e)).subscribe(()=>{this._changeDetectorRef.detectChanges(),this.stateChanges.next()})}_onSelect(e,t){let r=this._selectionModel.isSelected(e);!this.canSelectNullableOptions&&e.value==null&&!this._multiple?(e.deselect(),this._selectionModel.clear(),this.value!=null&&this._propagateChanges(e.value)):(r!==e.selected&&(e.selected?this._selectionModel.select(e):this._selectionModel.deselect(e)),t&&this._keyManager.setActiveItem(e),this.multiple&&(this._sortValues(),t&&this.focus())),r!==this._selectionModel.isSelected(e)&&this._propagateChanges(),this.stateChanges.next()}_sortValues(){if(this.multiple){let e=this.options.toArray();this._selectionModel.sort((t,r)=>this.sortComparator?this.sortComparator(t,r,e):e.indexOf(t)-e.indexOf(r)),this.stateChanges.next()}}_propagateChanges(e){let t;this.multiple?t=this.selected.map(r=>r.value):t=this.selected?this.selected.value:e,this._value=t,this.valueChange.emit(t),this._onChange(t),this.selectionChange.emit(this._getChangeEvent(t)),this._changeDetectorRef.markForCheck()}_highlightCorrectOption(){if(this._keyManager)if(this.empty){let e=-1;for(let t=0;t<this.options.length;t++)if(!this.options.get(t).disabled){e=t;break}this._keyManager.setActiveItem(e)}else this._keyManager.setActiveItem(this._selectionModel.selected[0])}_canOpen(){return!this._panelOpen&&!this.disabled&&this.options?.length>0&&!!this._overlayDir}focus(e){this._elementRef.nativeElement.focus(e)}_getPanelAriaLabelledby(){if(this.ariaLabel)return null;let e=this._parentFormField?.getLabelId()||null,t=e?e+" ":"";return this.ariaLabelledby?t+this.ariaLabelledby:e}_getAriaActiveDescendant(){return this.panelOpen&&this._keyManager&&this._keyManager.activeItem?this._keyManager.activeItem.id:null}_getTriggerAriaLabelledby(){if(this.ariaLabel)return null;let e=this._parentFormField?.getLabelId()||"";return this.ariaLabelledby&&(e+=" "+this.ariaLabelledby),e||(e=this._valueId),e}get describedByIds(){return this._elementRef.nativeElement.getAttribute("aria-describedby")?.split(" ")||[]}setDescribedByIds(e){let t=this._elementRef.nativeElement;e.length?t.setAttribute("aria-describedby",e.join(" ")):t.removeAttribute("aria-describedby")}onContainerClick(e){let t=ot(e);t&&(t.tagName==="MAT-OPTION"||t.classList.contains("cdk-overlay-backdrop")||t.closest(".mat-mdc-select-panel"))||(this.focus(),this.open())}get shouldLabelFloat(){return this.panelOpen||!this.empty||this.focused&&!!this.placeholder}static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["mat-select"]],contentQueries:function(t,r,a){if(t&1&&sn(a,hm,5)(a,Oi,5)(a,ul,5),t&2){let o;z(o=H())&&(r.customTrigger=o.first),z(o=H())&&(r.options=o),z(o=H())&&(r.optionGroups=o)}},viewQuery:function(t,r){if(t&1&&ke(lb,5)(db,5)(Qo,5),t&2){let a;z(a=H())&&(r.trigger=a.first),z(a=H())&&(r.panel=a.first),z(a=H())&&(r._overlayDir=a.first)}},hostAttrs:["role","combobox","aria-haspopup","listbox",1,"mat-mdc-select"],hostVars:21,hostBindings:function(t,r){t&1&&T("keydown",function(o){return r._handleKeydown(o)})("focus",function(){return r._onFocus()})("blur",function(){return r._onBlur()}),t&2&&(B("id",r.id)("tabindex",r.disabled?-1:r.tabIndex)("aria-controls",r.panelOpen?r.id+"-panel":null)("aria-expanded",r.panelOpen)("aria-label",r.ariaLabel||null)("aria-required",r.required.toString())("aria-disabled",r.disabled.toString())("aria-invalid",r.errorState)("aria-activedescendant",r._getAriaActiveDescendant()),w("mat-mdc-select-disabled",r.disabled)("mat-mdc-select-invalid",r.errorState)("mat-mdc-select-required",r.required)("mat-mdc-select-empty",r.empty)("mat-mdc-select-multiple",r.multiple)("mat-select-open",r.panelOpen))},inputs:{userAriaDescribedBy:[0,"aria-describedby","userAriaDescribedBy"],panelClass:"panelClass",disabled:[2,"disabled","disabled",Y],disableRipple:[2,"disableRipple","disableRipple",Y],tabIndex:[2,"tabIndex","tabIndex",e=>e==null?0:vt(e)],hideSingleSelectionIndicator:[2,"hideSingleSelectionIndicator","hideSingleSelectionIndicator",Y],placeholder:"placeholder",required:[2,"required","required",Y],multiple:[2,"multiple","multiple",Y],disableOptionCentering:[2,"disableOptionCentering","disableOptionCentering",Y],compareWith:"compareWith",value:"value",ariaLabel:[0,"aria-label","ariaLabel"],ariaLabelledby:[0,"aria-labelledby","ariaLabelledby"],errorStateMatcher:"errorStateMatcher",typeaheadDebounceInterval:[2,"typeaheadDebounceInterval","typeaheadDebounceInterval",vt],sortComparator:"sortComparator",id:"id",panelWidth:"panelWidth",canSelectNullableOptions:[2,"canSelectNullableOptions","canSelectNullableOptions",Y]},outputs:{openedChange:"openedChange",_openedStream:"opened",_closedStream:"closed",selectionChange:"selectionChange",valueChange:"valueChange"},exportAs:["matSelect"],features:[re([{provide:Xa,useExisting:i},{provide:cl,useExisting:i}]),pe],ngContentSelectors:ub,decls:11,vars:10,consts:[["fallbackOverlayOrigin","cdkOverlayOrigin","trigger",""],["panel",""],["cdk-overlay-origin","",1,"mat-mdc-select-trigger",3,"click"],[1,"mat-mdc-select-value"],[1,"mat-mdc-select-placeholder","mat-mdc-select-min-line"],[1,"mat-mdc-select-value-text"],[1,"mat-mdc-select-arrow-wrapper"],[1,"mat-mdc-select-arrow"],["viewBox","0 0 24 24","width","24px","height","24px","focusable","false","aria-hidden","true"],["d","M7 10l5 5 5-5z"],["cdk-connected-overlay","","cdkConnectedOverlayHasBackdrop","","cdkConnectedOverlayBackdropClass","cdk-overlay-transparent-backdrop",3,"detach","backdropClick","overlayKeydown","cdkConnectedOverlayDisableClose","cdkConnectedOverlayPanelClass","cdkConnectedOverlayScrollStrategy","cdkConnectedOverlayOrigin","cdkConnectedOverlayPositions","cdkConnectedOverlayWidth","cdkConnectedOverlayFlexibleDimensions","cdkConnectedOverlayUsePopover"],[1,"mat-mdc-select-min-line"],["role","listbox","tabindex","-1",1,"mat-mdc-select-panel","mdc-menu-surface","mdc-menu-surface--open",3,"keydown"]],template:function(t,r){if(t&1&&(Ee(cb),p(0,"div",2,0),T("click",function(){return r.open()}),p(3,"div",3),v(4,mb,2,1,"span",4)(5,fb,3,1,"span",5),h(),p(6,"div",6)(7,"div",7),_t(),p(8,"svg",8),_(9,"path",9),h()()()(),Oe(10,gb,3,16,"ng-template",10),T("detach",function(){return r.close()})("backdropClick",function(){return r.close()})("overlayKeydown",function(o){return r._handleOverlayKeydown(o)})),t&2){let a=Ke(1);c(3),B("id",r._valueId),c(),y(r.empty?4:5),c(6),f("cdkConnectedOverlayDisableClose",!0)("cdkConnectedOverlayPanelClass",r._overlayPanelClass)("cdkConnectedOverlayScrollStrategy",r._scrollStrategy)("cdkConnectedOverlayOrigin",r._preferredOverlayOrigin||a)("cdkConnectedOverlayPositions",r._positions)("cdkConnectedOverlayWidth",r._overlayWidth)("cdkConnectedOverlayFlexibleDimensions",!0)("cdkConnectedOverlayUsePopover",r._popoverLocation)}},dependencies:[Zo,Qo],styles:[`@keyframes _mat-select-enter {
  from {
    opacity: 0;
    transform: scaleY(0.8);
  }
  to {
    opacity: 1;
    transform: none;
  }
}
@keyframes _mat-select-exit {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}
.mat-mdc-select {
  display: inline-block;
  width: 100%;
  outline: none;
  -moz-osx-font-smoothing: grayscale;
  -webkit-font-smoothing: antialiased;
  color: var(--mat-select-enabled-trigger-text-color, var(--mat-sys-on-surface));
  font-family: var(--mat-select-trigger-text-font, var(--mat-sys-body-large-font));
  line-height: var(--mat-select-trigger-text-line-height, var(--mat-sys-body-large-line-height));
  font-size: var(--mat-select-trigger-text-size, var(--mat-sys-body-large-size));
  font-weight: var(--mat-select-trigger-text-weight, var(--mat-sys-body-large-weight));
  letter-spacing: var(--mat-select-trigger-text-tracking, var(--mat-sys-body-large-tracking));
}

div.mat-mdc-select-panel {
  box-shadow: var(--mat-select-container-elevation-shadow, 0px 3px 1px -2px rgba(0, 0, 0, 0.2), 0px 2px 2px 0px rgba(0, 0, 0, 0.14), 0px 1px 5px 0px rgba(0, 0, 0, 0.12));
}

.mat-mdc-select-disabled {
  color: var(--mat-select-disabled-trigger-text-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}
.mat-mdc-select-disabled .mat-mdc-select-placeholder {
  color: var(--mat-select-disabled-trigger-text-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}

.mat-mdc-select-trigger {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  position: relative;
  box-sizing: border-box;
  width: 100%;
}
.mat-mdc-select-disabled .mat-mdc-select-trigger {
  -webkit-user-select: none;
  user-select: none;
  cursor: default;
}

.mat-mdc-select-value {
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mat-mdc-select-value-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mat-mdc-select-arrow-wrapper {
  height: 24px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
}
.mat-form-field-appearance-fill .mdc-text-field--no-label .mat-mdc-select-arrow-wrapper {
  transform: none;
}

.mat-mdc-form-field .mat-mdc-select.mat-mdc-select-invalid .mat-mdc-select-arrow,
.mat-form-field-invalid:not(.mat-form-field-disabled) .mat-mdc-form-field-infix::after {
  color: var(--mat-select-invalid-arrow-color, var(--mat-sys-error));
}

.mat-mdc-select-arrow {
  width: 10px;
  height: 5px;
  position: relative;
  color: var(--mat-select-enabled-arrow-color, var(--mat-sys-on-surface-variant));
}
.mat-mdc-form-field.mat-focused .mat-mdc-select-arrow {
  color: var(--mat-select-focused-arrow-color, var(--mat-sys-primary));
}
.mat-mdc-form-field .mat-mdc-select.mat-mdc-select-disabled .mat-mdc-select-arrow {
  color: var(--mat-select-disabled-arrow-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}
.mat-select-open .mat-mdc-select-arrow {
  transform: rotate(180deg);
}
.mat-form-field-animations-enabled .mat-mdc-select-arrow {
  transition: transform 80ms linear;
}
.mat-mdc-select-arrow svg {
  fill: currentColor;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
@media (forced-colors: active) {
  .mat-mdc-select-arrow svg {
    fill: CanvasText;
  }
  .mat-mdc-select-disabled .mat-mdc-select-arrow svg {
    fill: GrayText;
  }
}

div.mat-mdc-select-panel {
  width: 100%;
  max-height: 275px;
  outline: 0;
  overflow: auto;
  padding: 8px 0;
  box-sizing: border-box;
  transform-origin: top center;
  border-radius: 0 0 4px 4px;
  position: relative;
  background-color: var(--mat-select-panel-background-color, var(--mat-sys-surface-container));
}
.mat-mdc-select-panel-above div.mat-mdc-select-panel {
  border-radius: 4px 4px 0 0;
  transform-origin: bottom center;
}
@media (forced-colors: active) {
  div.mat-mdc-select-panel {
    outline: solid 1px;
  }
}

.mat-select-panel-animations-enabled {
  animation: _mat-select-enter 120ms cubic-bezier(0, 0, 0.2, 1);
}
.mat-select-panel-animations-enabled.mat-select-panel-exit {
  animation: _mat-select-exit 100ms linear;
}

.mat-mdc-select-placeholder {
  transition: color 400ms 133.3333333333ms cubic-bezier(0.25, 0.8, 0.25, 1);
  color: var(--mat-select-placeholder-text-color, var(--mat-sys-on-surface-variant));
}
.mat-mdc-form-field:not(.mat-form-field-animations-enabled) .mat-mdc-select-placeholder, ._mat-animation-noopable .mat-mdc-select-placeholder {
  transition: none;
}
.mat-form-field-hide-placeholder .mat-mdc-select-placeholder {
  color: transparent;
  -webkit-text-fill-color: transparent;
  transition: none;
  display: block;
}

.mat-mdc-form-field-type-mat-select:not(.mat-form-field-disabled) .mat-mdc-text-field-wrapper {
  cursor: pointer;
}
.mat-mdc-form-field-type-mat-select.mat-form-field-appearance-fill .mat-mdc-floating-label {
  max-width: calc(100% - 18px);
}
.mat-mdc-form-field-type-mat-select.mat-form-field-appearance-fill .mdc-floating-label--float-above {
  max-width: calc(100% / 0.75 - 24px);
}
.mat-mdc-form-field-type-mat-select.mat-form-field-appearance-outline .mdc-notched-outline__notch {
  max-width: calc(100% - 60px);
}
.mat-mdc-form-field-type-mat-select.mat-form-field-appearance-outline .mdc-text-field--label-floating .mdc-notched-outline__notch {
  max-width: calc(100% - 24px);
}

.mat-mdc-select-min-line:empty::before {
  content: " ";
  white-space: pre;
  width: 1px;
  display: inline-block;
  visibility: hidden;
}

.mat-form-field-appearance-fill .mat-mdc-select-arrow-wrapper {
  transform: var(--mat-select-arrow-transform, translateY(-8px));
}
`],encapsulation:2,changeDetection:0})}return i})(),GA=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,selectors:[["mat-select-trigger"]],features:[re([{provide:hm,useExisting:i}])]})}return i})(),gm=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275mod=$({type:i});static \u0275inj=G({imports:[Ht,ml,fe,ai,ut,ml]})}return i})();function yb(i,n){if(i&1&&(p(0,"mat-option",2),P(1),h()),i&2){let e=n.$implicit;f("value",e),c(),Te(" ",e," ")}}var ho=(()=>{class i{constructor(){this.control=C.required(),this.options=C([]),this.multiple=C(!1),this.required=C(!1),this.label=C(void 0),this.hint=C(void 0)}static{this.\u0275fac=function(t){return new(t||i)}}static{this.\u0275cmp=x({type:i,selectors:[["vitamui-editor-select"]],inputs:{control:[1,"control"],options:[1,"options"],multiple:[1,"multiple"],required:[1,"required"],label:[1,"label"],hint:[1,"hint"]},decls:15,vars:10,consts:[[1,"mb-4","w-100","vitamui-mat-select"],["panelClass","vitamui-mat-select",3,"formControl","multiple"],[3,"value"],[1,"select-arrow"],[1,"material-icons"],[3,"control","hint"],[3,"control"]],template:function(t,r){t&1&&(p(0,"mat-form-field",0)(1,"mat-label"),P(2),O(3,"translate"),O(4,"empty"),h(),p(5,"mat-select",1),Be(6,yb,2,2,"mat-option",2,at),h(),p(8,"div",3)(9,"i",4),P(10,"keyboard_arrow_up"),h(),p(11,"i",4),P(12,"keyboard_arrow_down"),h()()(),_(13,"vitamui-hint",5)(14,"vitamui-form-errors",6)),t&2&&(c(2),ce(L(4,8,L(3,6,r.label()))),c(3),f("formControl",r.control())("multiple",r.multiple()),c(),je(r.options()),c(7),f("control",r.control())("hint",r.hint()),c(),f("control",r.control()))},dependencies:[nt,Xe,et,qt,hn,ut,kr,Ci,gm,fm,Oi,Ii,ve,tt],encapsulation:2})}}return i})();var oS=new M("MAT_DATE_LOCALE",{providedIn:"root",factory:()=>l(Ed)}),Ti="Method not implemented",mt=class{locale;_localeChanges=new F;localeChanges=this._localeChanges;setTime(n,e,t,r){throw new Error(Ti)}getHours(n){throw new Error(Ti)}getMinutes(n){throw new Error(Ti)}getSeconds(n){throw new Error(Ti)}parseTime(n,e){throw new Error(Ti)}addSeconds(n,e){throw new Error(Ti)}getValidDateOrNull(n){return this.isDateInstance(n)&&this.isValid(n)?n:null}deserialize(n){return n==null||this.isDateInstance(n)&&this.isValid(n)?n:this.invalid()}setLocale(n){this.locale=n,this._localeChanges.next()}compareDate(n,e){return this.getYear(n)-this.getYear(e)||this.getMonth(n)-this.getMonth(e)||this.getDate(n)-this.getDate(e)}compareTime(n,e){return this.getHours(n)-this.getHours(e)||this.getMinutes(n)-this.getMinutes(e)||this.getSeconds(n)-this.getSeconds(e)}sameDate(n,e){if(n&&e){let t=this.isValid(n),r=this.isValid(e);return t&&r?!this.compareDate(n,e):t==r}return n==e}sameTime(n,e){if(n&&e){let t=this.isValid(n),r=this.isValid(e);return t&&r?!this.compareTime(n,e):t==r}return n==e}clampDate(n,e,t){return e&&this.compareDate(n,e)<0?e:t&&this.compareDate(n,t)>0?t:n}},Fi=new M("mat-date-formats");var Cb=["tooltip"],xb=20;var Db=new M("mat-tooltip-scroll-strategy",{providedIn:"root",factory:()=>{let i=l(U);return()=>mn(i,{scrollThrottle:xb})}}),Mb=new M("mat-tooltip-default-options",{providedIn:"root",factory:()=>({showDelay:0,hideDelay:0,touchendHideDelay:1500})});var _m="tooltip-panel",wb={passive:!0},Eb=8,kb=8,Ab=24,Sb=200,bm=(()=>{class i{_elementRef=l(N);_ngZone=l(X);_platform=l(be);_ariaDescriber=l(gc);_focusMonitor=l(It);_dir=l(Ze);_injector=l(U);_viewContainerRef=l(Jn);_mediaMatcher=l(ui);_document=l(_e);_renderer=l(we);_animationsDisabled=ye();_defaultOptions=l(Mb,{optional:!0});_overlayRef=null;_tooltipInstance=null;_overlayPanelClass;_portal;_position="below";_positionAtOrigin=!1;_disabled=!1;_tooltipClass;_viewInitialized=!1;_pointerExitEventsInitialized=!1;_tooltipComponent=Ib;_viewportMargin=8;_currentPosition;_cssClassPrefix="mat-mdc";_ariaDescriptionPending=!1;_dirSubscribed=!1;get position(){return this._position}set position(e){e!==this._position&&(this._position=e,this._overlayRef&&(this._updatePosition(this._overlayRef),this._tooltipInstance?.show(0),this._overlayRef.updatePosition()))}get positionAtOrigin(){return this._positionAtOrigin}set positionAtOrigin(e){this._positionAtOrigin=yt(e),this._detach(),this._overlayRef=null}get disabled(){return this._disabled}set disabled(e){let t=yt(e);this._disabled!==t&&(this._disabled=t,t?this.hide(0):this._setupPointerEnterEventsIfNeeded(),this._syncAriaDescription(this.message))}get showDelay(){return this._showDelay}set showDelay(e){this._showDelay=dn(e)}_showDelay;get hideDelay(){return this._hideDelay}set hideDelay(e){this._hideDelay=dn(e),this._tooltipInstance&&(this._tooltipInstance._mouseLeaveHideDelay=this._hideDelay)}_hideDelay;touchGestures="auto";get message(){return this._message}set message(e){let t=this._message;this._message=e!=null?String(e).trim():"",!this._message&&this._isTooltipVisible()?this.hide(0):(this._setupPointerEnterEventsIfNeeded(),this._updateTooltipMessage()),this._syncAriaDescription(t)}_message="";get tooltipClass(){return this._tooltipClass}set tooltipClass(e){this._tooltipClass=e,this._tooltipInstance&&this._setTooltipClass(this._tooltipClass)}_eventCleanups=[];_touchstartTimeout=null;_destroyed=new F;_isDestroyed=!1;constructor(){let e=this._defaultOptions;e&&(this._showDelay=e.showDelay,this._hideDelay=e.hideDelay,e.position&&(this.position=e.position),e.positionAtOrigin&&(this.positionAtOrigin=e.positionAtOrigin),e.touchGestures&&(this.touchGestures=e.touchGestures),e.tooltipClass&&(this.tooltipClass=e.tooltipClass)),this._viewportMargin=Eb}ngAfterViewInit(){this._viewInitialized=!0,this._setupPointerEnterEventsIfNeeded(),this._focusMonitor.monitor(this._elementRef).pipe(ge(this._destroyed)).subscribe(e=>{e?e==="keyboard"&&this._ngZone.run(()=>this.show()):this._ngZone.run(()=>this.hide(0))})}ngOnDestroy(){let e=this._elementRef.nativeElement;this._touchstartTimeout&&clearTimeout(this._touchstartTimeout),this._overlayRef&&(this._overlayRef.dispose(),this._tooltipInstance=null),this._eventCleanups.forEach(t=>t()),this._eventCleanups.length=0,this._destroyed.next(),this._destroyed.complete(),this._isDestroyed=!0,this._ariaDescriber.removeDescription(e,this.message,"tooltip"),this._focusMonitor.stopMonitoring(e)}show(e=this.showDelay,t){if(this.disabled||!this.message||this._isTooltipVisible()){this._tooltipInstance?._cancelPendingAnimations();return}let r=this._createOverlay(t);this._detach(),this._portal=this._portal||new cn(this._tooltipComponent,this._viewContainerRef);let a=this._tooltipInstance=r.attach(this._portal).instance;a._triggerElement=this._elementRef.nativeElement,a._mouseLeaveHideDelay=this._hideDelay,a.afterHidden().pipe(ge(this._destroyed)).subscribe(()=>this._detach()),this._setTooltipClass(this._tooltipClass),this._updateTooltipMessage(),a.show(e)}hide(e=this.hideDelay){let t=this._tooltipInstance;t&&(t.isVisible()?t.hide(e):(t._cancelPendingAnimations(),this._detach()))}toggle(e){this._isTooltipVisible()?this.hide():this.show(void 0,e)}_isTooltipVisible(){return!!this._tooltipInstance&&this._tooltipInstance.isVisible()}_createOverlay(e){if(this._overlayRef){let o=this._overlayRef.getConfig().positionStrategy;if((!this.positionAtOrigin||!e)&&o._origin instanceof N)return this._overlayRef;this._detach()}let t=this._injector.get($o).getAncestorScrollContainers(this._elementRef),r=`${this._cssClassPrefix}-${_m}`,a=li(this._injector,this.positionAtOrigin?e||this._elementRef:this._elementRef).withTransformOriginOn(`.${this._cssClassPrefix}-tooltip`).withFlexibleDimensions(!1).withViewportMargin(this._viewportMargin).withScrollableContainers(t).withPopoverLocation("global");return a.positionChanges.pipe(ge(this._destroyed)).subscribe(o=>{this._updateCurrentPositionClass(o.connectionPair),this._tooltipInstance&&o.scrollableViewProperties.isOverlayClipped&&this._tooltipInstance.isVisible()&&this._ngZone.run(()=>this.hide(0))}),this._overlayRef=pn(this._injector,{direction:this._dir,positionStrategy:a,panelClass:this._overlayPanelClass?[...this._overlayPanelClass,r]:r,scrollStrategy:this._injector.get(Db)(),disableAnimations:this._animationsDisabled,eventPredicate:this._overlayEventPredicate}),this._updatePosition(this._overlayRef),this._overlayRef.detachments().pipe(ge(this._destroyed)).subscribe(()=>this._detach()),this._overlayRef.outsidePointerEvents().pipe(ge(this._destroyed)).subscribe(()=>this._tooltipInstance?._handleBodyInteraction()),this._overlayRef.keydownEvents().pipe(ge(this._destroyed)).subscribe(o=>{o.preventDefault(),o.stopPropagation(),this._ngZone.run(()=>this.hide(0))}),this._defaultOptions?.disableTooltipInteractivity&&this._overlayRef.addPanelClass(`${this._cssClassPrefix}-tooltip-panel-non-interactive`),this._dirSubscribed||(this._dirSubscribed=!0,this._dir.change.pipe(ge(this._destroyed)).subscribe(()=>{this._overlayRef&&this._updatePosition(this._overlayRef)})),this._overlayRef}_detach(){this._overlayRef&&this._overlayRef.hasAttached()&&this._overlayRef.detach(),this._tooltipInstance=null}_updatePosition(e){let t=e.getConfig().positionStrategy,r=this._getOrigin(),a=this._getOverlayPosition();t.withPositions([this._addOffset(g(g({},r.main),a.main)),this._addOffset(g(g({},r.fallback),a.fallback))])}_addOffset(e){let t=kb,r=!this._dir||this._dir.value=="ltr";return e.originY==="top"?e.offsetY=-t:e.originY==="bottom"?e.offsetY=t:e.originX==="start"?e.offsetX=r?-t:t:e.originX==="end"&&(e.offsetX=r?t:-t),e}_getOrigin(){let e=!this._dir||this._dir.value=="ltr",t=this.position,r;t=="above"||t=="below"?r={originX:"center",originY:t=="above"?"top":"bottom"}:t=="before"||t=="left"&&e||t=="right"&&!e?r={originX:"start",originY:"center"}:(t=="after"||t=="right"&&e||t=="left"&&!e)&&(r={originX:"end",originY:"center"});let{x:a,y:o}=this._invertPosition(r.originX,r.originY);return{main:r,fallback:{originX:a,originY:o}}}_getOverlayPosition(){let e=!this._dir||this._dir.value=="ltr",t=this.position,r;t=="above"?r={overlayX:"center",overlayY:"bottom"}:t=="below"?r={overlayX:"center",overlayY:"top"}:t=="before"||t=="left"&&e||t=="right"&&!e?r={overlayX:"end",overlayY:"center"}:(t=="after"||t=="right"&&e||t=="left"&&!e)&&(r={overlayX:"start",overlayY:"center"});let{x:a,y:o}=this._invertPosition(r.overlayX,r.overlayY);return{main:r,fallback:{overlayX:a,overlayY:o}}}_updateTooltipMessage(){this._tooltipInstance&&(this._tooltipInstance.message=this.message,this._tooltipInstance._markForCheck(),rt(()=>{this._tooltipInstance&&this._overlayRef.updatePosition()},{injector:this._injector}))}_setTooltipClass(e){this._tooltipInstance&&(this._tooltipInstance.tooltipClass=e instanceof Set?Array.from(e):e,this._tooltipInstance._markForCheck())}_invertPosition(e,t){return this.position==="above"||this.position==="below"?t==="top"?t="bottom":t==="bottom"&&(t="top"):e==="end"?e="start":e==="start"&&(e="end"),{x:e,y:t}}_updateCurrentPositionClass(e){let{overlayY:t,originX:r,originY:a}=e,o;if(t==="center"?this._dir&&this._dir.value==="rtl"?o=r==="end"?"left":"right":o=r==="start"?"left":"right":o=t==="bottom"&&a==="top"?"above":"below",o!==this._currentPosition){let s=this._overlayRef;if(s){let d=`${this._cssClassPrefix}-${_m}-`;s.removePanelClass(d+this._currentPosition),s.addPanelClass(d+o)}this._currentPosition=o}}_setupPointerEnterEventsIfNeeded(){this._disabled||!this.message||!this._viewInitialized||this._eventCleanups.length||(this._isTouchPlatform()?this.touchGestures!=="off"&&(this._disableNativeGesturesIfNecessary(),this._addListener("touchstart",e=>{let t=e.targetTouches?.[0],r=t?{x:t.clientX,y:t.clientY}:void 0;this._setupPointerExitEventsIfNeeded(),this._touchstartTimeout&&clearTimeout(this._touchstartTimeout);let a=500;this._touchstartTimeout=setTimeout(()=>{this._touchstartTimeout=null,this.show(void 0,r)},this._defaultOptions?.touchLongPressShowDelay??a)})):this._addListener("mouseenter",e=>{this._setupPointerExitEventsIfNeeded();let t;e.x!==void 0&&e.y!==void 0&&(t=e),this.show(void 0,t)}))}_setupPointerExitEventsIfNeeded(){if(!this._pointerExitEventsInitialized){if(this._pointerExitEventsInitialized=!0,!this._isTouchPlatform())this._addListener("mouseleave",e=>{let t=e.relatedTarget;(!t||!this._overlayRef?.overlayElement.contains(t))&&this.hide()}),this._addListener("wheel",e=>{if(this._isTooltipVisible()){let t=this._document.elementFromPoint(e.clientX,e.clientY),r=this._elementRef.nativeElement;t!==r&&!r.contains(t)&&this.hide()}});else if(this.touchGestures!=="off"){this._disableNativeGesturesIfNecessary();let e=()=>{this._touchstartTimeout&&clearTimeout(this._touchstartTimeout),this.hide(this._defaultOptions?.touchendHideDelay)};this._addListener("touchend",e),this._addListener("touchcancel",e)}}}_addListener(e,t){this._eventCleanups.push(this._renderer.listen(this._elementRef.nativeElement,e,t,wb))}_isTouchPlatform(){let e=this._defaultOptions?.detectHoverCapability;return typeof e=="function"?!e():this._platform.IOS||this._platform.ANDROID?!0:this._platform.isBrowser?!!e&&this._mediaMatcher.matchMedia("(any-hover: none)").matches:!1}_disableNativeGesturesIfNecessary(){let e=this.touchGestures;if(e!=="off"){let t=this._elementRef.nativeElement,r=t.style;(e==="on"||t.nodeName!=="INPUT"&&t.nodeName!=="TEXTAREA")&&(r.userSelect=r.msUserSelect=r.webkitUserSelect=r.MozUserSelect="none"),(e==="on"||!t.draggable)&&(r.webkitUserDrag="none"),r.touchAction="none",r.webkitTapHighlightColor="transparent"}}_syncAriaDescription(e){this._ariaDescriptionPending||(this._ariaDescriptionPending=!0,this._ariaDescriber.removeDescription(this._elementRef.nativeElement,e,"tooltip"),this._isDestroyed||rt({write:()=>{this._ariaDescriptionPending=!1,this.message&&!this.disabled&&this._ariaDescriber.describe(this._elementRef.nativeElement,this.message,"tooltip")}},{injector:this._injector}))}_overlayEventPredicate=e=>e.type==="keydown"?this._isTooltipVisible()&&e.keyCode===27&&!xe(e):!0;static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,selectors:[["","matTooltip",""]],hostAttrs:[1,"mat-mdc-tooltip-trigger"],hostVars:2,hostBindings:function(t,r){t&2&&w("mat-mdc-tooltip-disabled",r.disabled)},inputs:{position:[0,"matTooltipPosition","position"],positionAtOrigin:[0,"matTooltipPositionAtOrigin","positionAtOrigin"],disabled:[0,"matTooltipDisabled","disabled"],showDelay:[0,"matTooltipShowDelay","showDelay"],hideDelay:[0,"matTooltipHideDelay","hideDelay"],touchGestures:[0,"matTooltipTouchGestures","touchGestures"],message:[0,"matTooltip","message"],tooltipClass:[0,"matTooltipClass","tooltipClass"]},exportAs:["matTooltip"]})}return i})(),Ib=(()=>{class i{_changeDetectorRef=l(ne);_elementRef=l(N);_isMultiline=!1;message;tooltipClass;_showTimeoutId;_hideTimeoutId;_triggerElement;_mouseLeaveHideDelay;_animationsDisabled=ye();_tooltip;_closeOnInteraction=!1;_isVisible=!1;_onHide=new F;_showAnimation="mat-mdc-tooltip-show";_hideAnimation="mat-mdc-tooltip-hide";constructor(){}show(e){this._hideTimeoutId!=null&&clearTimeout(this._hideTimeoutId),this._showTimeoutId=setTimeout(()=>{this._toggleVisibility(!0),this._showTimeoutId=void 0},e)}hide(e){this._showTimeoutId!=null&&clearTimeout(this._showTimeoutId),this._hideTimeoutId=setTimeout(()=>{this._toggleVisibility(!1),this._hideTimeoutId=void 0},e)}afterHidden(){return this._onHide}isVisible(){return this._isVisible}ngOnDestroy(){this._cancelPendingAnimations(),this._onHide.complete(),this._triggerElement=null}_handleBodyInteraction(){this._closeOnInteraction&&this.hide(0)}_markForCheck(){this._changeDetectorRef.markForCheck()}_handleMouseLeave({relatedTarget:e}){(!e||!this._triggerElement.contains(e))&&(this.isVisible()?this.hide(this._mouseLeaveHideDelay):this._finalizeAnimation(!1))}_onShow(){this._isMultiline=this._isTooltipMultiline(),this._markForCheck()}_isTooltipMultiline(){let e=this._elementRef.nativeElement.getBoundingClientRect();return e.height>Ab&&e.width>=Sb}_handleAnimationEnd({animationName:e}){(e===this._showAnimation||e===this._hideAnimation)&&this._finalizeAnimation(e===this._showAnimation)}_cancelPendingAnimations(){this._showTimeoutId!=null&&clearTimeout(this._showTimeoutId),this._hideTimeoutId!=null&&clearTimeout(this._hideTimeoutId),this._showTimeoutId=this._hideTimeoutId=void 0}_finalizeAnimation(e){e?this._closeOnInteraction=!0:this.isVisible()||this._onHide.next()}_toggleVisibility(e){let t=this._tooltip.nativeElement,r=this._showAnimation,a=this._hideAnimation;if(t.classList.remove(e?a:r),t.classList.add(e?r:a),this._isVisible!==e&&(this._isVisible=e,this._changeDetectorRef.markForCheck()),e&&!this._animationsDisabled&&typeof getComputedStyle=="function"){let o=getComputedStyle(t);(o.getPropertyValue("animation-duration")==="0s"||o.getPropertyValue("animation-name")==="none")&&(this._animationsDisabled=!0)}e&&this._onShow(),this._animationsDisabled&&(t.classList.add("_mat-animation-noopable"),this._finalizeAnimation(e))}static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["mat-tooltip-component"]],viewQuery:function(t,r){if(t&1&&ke(Cb,7),t&2){let a;z(a=H())&&(r._tooltip=a.first)}},hostAttrs:["aria-hidden","true"],hostBindings:function(t,r){t&1&&T("mouseleave",function(o){return r._handleMouseLeave(o)})},decls:4,vars:5,consts:[["tooltip",""],[1,"mdc-tooltip","mat-mdc-tooltip",3,"animationend"],[1,"mat-mdc-tooltip-surface","mdc-tooltip__surface"]],template:function(t,r){t&1&&(le(0,"div",1,0),ti("animationend",function(o){return r._handleAnimationEnd(o)}),le(2,"div",2),P(3),he()()),t&2&&(Ae(r.tooltipClass),w("mdc-tooltip--multiline",r._isMultiline),c(3),ce(r.message))},styles:[`.mat-mdc-tooltip {
  position: relative;
  transform: scale(0);
  display: inline-flex;
}
.mat-mdc-tooltip::before {
  content: "";
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: -1;
  position: absolute;
}
.mat-mdc-tooltip-panel-below .mat-mdc-tooltip::before {
  top: -8px;
}
.mat-mdc-tooltip-panel-above .mat-mdc-tooltip::before {
  bottom: -8px;
}
.mat-mdc-tooltip-panel-right .mat-mdc-tooltip::before {
  left: -8px;
}
.mat-mdc-tooltip-panel-left .mat-mdc-tooltip::before {
  right: -8px;
}
.mat-mdc-tooltip._mat-animation-noopable {
  animation: none;
  transform: scale(1);
}

.mat-mdc-tooltip-surface {
  word-break: normal;
  overflow-wrap: anywhere;
  padding: 4px 8px;
  min-width: 40px;
  max-width: 200px;
  min-height: 24px;
  max-height: 40vh;
  box-sizing: border-box;
  overflow: hidden;
  text-align: center;
  will-change: transform, opacity;
  background-color: var(--mat-tooltip-container-color, var(--mat-sys-inverse-surface));
  color: var(--mat-tooltip-supporting-text-color, var(--mat-sys-inverse-on-surface));
  border-radius: var(--mat-tooltip-container-shape, var(--mat-sys-corner-extra-small));
  font-family: var(--mat-tooltip-supporting-text-font, var(--mat-sys-body-small-font));
  font-size: var(--mat-tooltip-supporting-text-size, var(--mat-sys-body-small-size));
  font-weight: var(--mat-tooltip-supporting-text-weight, var(--mat-sys-body-small-weight));
  line-height: var(--mat-tooltip-supporting-text-line-height, var(--mat-sys-body-small-line-height));
  letter-spacing: var(--mat-tooltip-supporting-text-tracking, var(--mat-sys-body-small-tracking));
}
.mat-mdc-tooltip-surface::before {
  position: absolute;
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  border: 1px solid transparent;
  border-radius: inherit;
  content: "";
  pointer-events: none;
}
.mdc-tooltip--multiline .mat-mdc-tooltip-surface {
  text-align: left;
}
[dir=rtl] .mdc-tooltip--multiline .mat-mdc-tooltip-surface {
  text-align: right;
}

.mat-mdc-tooltip-panel {
  line-height: normal;
}
.mat-mdc-tooltip-panel.mat-mdc-tooltip-panel-non-interactive {
  pointer-events: none;
}

@keyframes mat-mdc-tooltip-show {
  0% {
    opacity: 0;
    transform: scale(0.8);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}
@keyframes mat-mdc-tooltip-hide {
  0% {
    opacity: 1;
    transform: scale(1);
  }
  100% {
    opacity: 0;
    transform: scale(0.8);
  }
}
.mat-mdc-tooltip-show {
  animation: mat-mdc-tooltip-show 150ms cubic-bezier(0, 0, 0.2, 1) forwards;
}

.mat-mdc-tooltip-hide {
  animation: mat-mdc-tooltip-hide 75ms cubic-bezier(0.4, 0, 1, 1) forwards;
}
`],encapsulation:2,changeDetection:0})}return i})();var Ob=["mat-calendar-body",""];function Tb(i,n){return this._trackRow(n)}var wm=(i,n)=>n.id;function Fb(i,n){if(i&1&&(le(0,"tr",0)(1,"td",3),P(2),he()()),i&2){let e=m();c(),At("padding-top",e._cellPadding)("padding-bottom",e._cellPadding),B("colspan",e.numCols),c(),Te(" ",e.label," ")}}function Pb(i,n){if(i&1&&(le(0,"td",3),P(1),he()),i&2){let e=m(2);At("padding-top",e._cellPadding)("padding-bottom",e._cellPadding),B("colspan",e._firstRowOffset),c(),Te(" ",e._firstRowOffset>=e.labelMinRequiredCells?e.label:""," ")}}function Rb(i,n){if(i&1){let e=ze();le(0,"td",6)(1,"button",7),ti("click",function(r){let a=J(e).$implicit,o=m(2);return ee(o._cellClicked(a,r))})("focus",function(r){let a=J(e).$implicit,o=m(2);return ee(o._emitActiveDateChange(a,r))}),le(2,"span",8),P(3),he(),Pe(4,"span",9),he()()}if(i&2){let e=n.$implicit,t=n.$index,r=m().$index,a=m();At("width",a._cellWidth)("padding-top",a._cellPadding)("padding-bottom",a._cellPadding),B("data-mat-row",r)("data-mat-col",t),c(),Ae(e.cssClasses),w("mat-calendar-body-disabled",!e.enabled)("mat-calendar-body-active",a._isActiveCell(r,t))("mat-calendar-body-range-start",a._isRangeStart(e.compareValue))("mat-calendar-body-range-end",a._isRangeEnd(e.compareValue))("mat-calendar-body-in-range",a._isInRange(e.compareValue))("mat-calendar-body-comparison-bridge-start",a._isComparisonBridgeStart(e.compareValue,r,t))("mat-calendar-body-comparison-bridge-end",a._isComparisonBridgeEnd(e.compareValue,r,t))("mat-calendar-body-comparison-start",a._isComparisonStart(e.compareValue))("mat-calendar-body-comparison-end",a._isComparisonEnd(e.compareValue))("mat-calendar-body-in-comparison-range",a._isInComparisonRange(e.compareValue))("mat-calendar-body-preview-start",a._isPreviewStart(e.compareValue))("mat-calendar-body-preview-end",a._isPreviewEnd(e.compareValue))("mat-calendar-body-in-preview",a._isInPreview(e.compareValue)),He("tabIndex",a._isActiveCell(r,t)?0:-1),B("aria-label",e.ariaLabel)("aria-disabled",!e.enabled||null)("aria-pressed",a._isSelected(e.compareValue))("aria-current",a.todayValue===e.compareValue?"date":null)("aria-describedby",a._getDescribedby(e.compareValue)),c(),w("mat-calendar-body-selected",a._isSelected(e.compareValue))("mat-calendar-body-comparison-identical",a._isComparisonIdentical(e.compareValue))("mat-calendar-body-today",a.todayValue===e.compareValue),c(),Te(" ",e.displayValue," ")}}function Vb(i,n){if(i&1&&(le(0,"tr",1),v(1,Pb,2,6,"td",4),Be(2,Rb,5,49,"td",5,wm),he()),i&2){let e=n.$implicit,t=n.$index,r=m();c(),y(t===0&&r._firstRowOffset?1:-1),c(),je(e)}}function Nb(i,n){if(i&1&&(p(0,"th",2)(1,"span",6),P(2),h(),p(3,"span",3),P(4),h()()),i&2){let e=n.$implicit;c(2),ce(e.long),c(2),ce(e.narrow)}}var Lb=["*"];function Bb(i,n){}function jb(i,n){if(i&1){let e=ze();p(0,"mat-month-view",4),oa("activeDateChange",function(r){J(e);let a=m();return aa(a.activeDate,r)||(a.activeDate=r),ee(r)}),T("_userSelection",function(r){J(e);let a=m();return ee(a._dateSelected(r))})("dragStarted",function(r){J(e);let a=m();return ee(a._dragStarted(r))})("dragEnded",function(r){J(e);let a=m();return ee(a._dragEnded(r))}),h()}if(i&2){let e=m();ra("activeDate",e.activeDate),f("selected",e.selected)("dateFilter",e.dateFilter)("maxDate",e.maxDate)("minDate",e.minDate)("dateClass",e.dateClass)("comparisonStart",e.comparisonStart)("comparisonEnd",e.comparisonEnd)("startDateAccessibleName",e.startDateAccessibleName)("endDateAccessibleName",e.endDateAccessibleName)("activeDrag",e._activeDrag)}}function zb(i,n){if(i&1){let e=ze();p(0,"mat-year-view",5),oa("activeDateChange",function(r){J(e);let a=m();return aa(a.activeDate,r)||(a.activeDate=r),ee(r)}),T("monthSelected",function(r){J(e);let a=m();return ee(a._monthSelectedInYearView(r))})("selectedChange",function(r){J(e);let a=m();return ee(a._goToDateInView(r,"month"))}),h()}if(i&2){let e=m();ra("activeDate",e.activeDate),f("selected",e.selected)("dateFilter",e.dateFilter)("maxDate",e.maxDate)("minDate",e.minDate)("dateClass",e.dateClass)}}function Hb(i,n){if(i&1){let e=ze();p(0,"mat-multi-year-view",6),oa("activeDateChange",function(r){J(e);let a=m();return aa(a.activeDate,r)||(a.activeDate=r),ee(r)}),T("yearSelected",function(r){J(e);let a=m();return ee(a._yearSelectedInMultiYearView(r))})("selectedChange",function(r){J(e);let a=m();return ee(a._goToDateInView(r,"year"))}),h()}if(i&2){let e=m();ra("activeDate",e.activeDate),f("selected",e.selected)("dateFilter",e.dateFilter)("maxDate",e.maxDate)("minDate",e.minDate)("dateClass",e.dateClass)}}function Ub(i,n){}var Wb=["button"],Yb=[[["","matDatepickerToggleIcon",""]]],qb=["[matDatepickerToggleIcon]"];function Gb(i,n){i&1&&(_t(),p(0,"svg",2),_(1,"path",3),h())}var Vi=(()=>{class i{changes=new F;calendarLabel="Calendar";openCalendarLabel="Open calendar";closeCalendarLabel="Close calendar";prevMonthLabel="Previous month";nextMonthLabel="Next month";prevYearLabel="Previous year";nextYearLabel="Next year";prevMultiYearLabel="Previous 24 years";nextMultiYearLabel="Next 24 years";switchToMonthViewLabel="Choose date";switchToMultiYearViewLabel="Choose month and year";startDateLabel="Start date";endDateLabel="End date";comparisonDateLabel="Comparison range";formatYearRange(e,t){return`${e} \u2013 ${t}`}formatYearRangeLabel(e,t){return`${e} to ${t}`}static \u0275fac=function(t){return new(t||i)};static \u0275prov=K({token:i,factory:i.\u0275fac,providedIn:"root"})}return i})(),$b=0,Nr=class{value;displayValue;ariaLabel;enabled;compareValue;rawValue;id=$b++;cssClasses;constructor(n,e,t,r,a,o=n,s){this.value=n,this.displayValue=e,this.ariaLabel=t,this.enabled=r,this.compareValue=o,this.rawValue=s,this.cssClasses=a instanceof Set?Array.from(a):a}},Kb={passive:!1,capture:!0},fo={passive:!0,capture:!0},vm={passive:!0},Ri=(()=>{class i{_elementRef=l(N);_ngZone=l(X);_platform=l(be);_intl=l(Vi);_eventCleanups;_skipNextFocus=!1;_focusActiveCellAfterViewChecked=!1;label;rows;todayValue;startValue;endValue;labelMinRequiredCells;numCols=7;activeCell=0;ngAfterViewChecked(){this._focusActiveCellAfterViewChecked&&(this._focusActiveCell(),this._focusActiveCellAfterViewChecked=!1)}isRange=!1;cellAspectRatio=1;comparisonStart=null;comparisonEnd=null;previewStart=null;previewEnd=null;startDateAccessibleName=null;endDateAccessibleName=null;selectedValueChange=new S;previewChange=new S;activeDateChange=new S;dragStarted=new S;dragEnded=new S;_firstRowOffset;_cellPadding;_cellWidth;_startDateLabelId;_endDateLabelId;_comparisonStartDateLabelId;_comparisonEndDateLabelId;_didDragSinceMouseDown=!1;_injector=l(U);comparisonDateAccessibleName=this._intl.comparisonDateLabel;_trackRow=e=>e;constructor(){let e=l(we),t=l(Re);this._startDateLabelId=t.getId("mat-calendar-body-start-"),this._endDateLabelId=t.getId("mat-calendar-body-end-"),this._comparisonStartDateLabelId=t.getId("mat-calendar-body-comparison-start-"),this._comparisonEndDateLabelId=t.getId("mat-calendar-body-comparison-end-"),l(Ue).load(_n),this._ngZone.runOutsideAngular(()=>{let r=this._elementRef.nativeElement,a=[e.listen(r,"touchmove",this._touchmoveHandler,Kb),e.listen(r,"mouseenter",this._enterHandler,fo),e.listen(r,"focus",this._enterHandler,fo),e.listen(r,"mouseleave",this._leaveHandler,fo),e.listen(r,"blur",this._leaveHandler,fo),e.listen(r,"mousedown",this._mousedownHandler,vm),e.listen(r,"touchstart",this._mousedownHandler,vm)];this._platform.isBrowser&&a.push(e.listen("window","mouseup",this._mouseupHandler),e.listen("window","touchend",this._touchendHandler)),this._eventCleanups=a})}_cellClicked(e,t){this._didDragSinceMouseDown||e.enabled&&this.selectedValueChange.emit({value:e.value,event:t})}_emitActiveDateChange(e,t){e.enabled&&this.activeDateChange.emit({value:e.value,event:t})}_isSelected(e){return this.startValue===e||this.endValue===e}ngOnChanges(e){let t=e.numCols,{rows:r,numCols:a}=this;(e.rows||t)&&(this._firstRowOffset=r&&r.length&&r[0].length?a-r[0].length:0),(e.cellAspectRatio||t||!this._cellPadding)&&(this._cellPadding=`${50*this.cellAspectRatio/a}%`),(t||!this._cellWidth)&&(this._cellWidth=`${100/a}%`)}ngOnDestroy(){this._eventCleanups.forEach(e=>e())}_isActiveCell(e,t){let r=e*this.numCols+t;return e&&(r-=this._firstRowOffset),r==this.activeCell}_focusActiveCell(e=!0){rt(()=>{setTimeout(()=>{let t=this._elementRef.nativeElement.querySelector(".mat-calendar-body-active");t&&(e||(this._skipNextFocus=!0),t.focus())})},{injector:this._injector})}_scheduleFocusActiveCellAfterViewChecked(){this._focusActiveCellAfterViewChecked=!0}_isRangeStart(e){return gl(e,this.startValue,this.endValue)}_isRangeEnd(e){return _l(e,this.startValue,this.endValue)}_isInRange(e){return bl(e,this.startValue,this.endValue,this.isRange)}_isComparisonStart(e){return gl(e,this.comparisonStart,this.comparisonEnd)}_isComparisonBridgeStart(e,t,r){if(!this._isComparisonStart(e)||this._isRangeStart(e)||!this._isInRange(e))return!1;let a=this.rows[t][r-1];if(!a){let o=this.rows[t-1];a=o&&o[o.length-1]}return a&&!this._isRangeEnd(a.compareValue)}_isComparisonBridgeEnd(e,t,r){if(!this._isComparisonEnd(e)||this._isRangeEnd(e)||!this._isInRange(e))return!1;let a=this.rows[t][r+1];if(!a){let o=this.rows[t+1];a=o&&o[0]}return a&&!this._isRangeStart(a.compareValue)}_isComparisonEnd(e){return _l(e,this.comparisonStart,this.comparisonEnd)}_isInComparisonRange(e){return bl(e,this.comparisonStart,this.comparisonEnd,this.isRange)}_isComparisonIdentical(e){return this.comparisonStart===this.comparisonEnd&&e===this.comparisonStart}_isPreviewStart(e){return gl(e,this.previewStart,this.previewEnd)}_isPreviewEnd(e){return _l(e,this.previewStart,this.previewEnd)}_isInPreview(e){return bl(e,this.previewStart,this.previewEnd,this.isRange)}_getDescribedby(e){if(!this.isRange)return null;if(this.startValue===e&&this.endValue===e)return`${this._startDateLabelId} ${this._endDateLabelId}`;if(this.startValue===e)return this._startDateLabelId;if(this.endValue===e)return this._endDateLabelId;if(this.comparisonStart!==null&&this.comparisonEnd!==null){if(e===this.comparisonStart&&e===this.comparisonEnd)return`${this._comparisonStartDateLabelId} ${this._comparisonEndDateLabelId}`;if(e===this.comparisonStart)return this._comparisonStartDateLabelId;if(e===this.comparisonEnd)return this._comparisonEndDateLabelId}return null}_enterHandler=e=>{if(this._skipNextFocus&&e.type==="focus"){this._skipNextFocus=!1;return}if(e.target&&this.isRange){let t=this._getCellFromElement(e.target);t&&this._ngZone.run(()=>this.previewChange.emit({value:t.enabled?t:null,event:e}))}};_touchmoveHandler=e=>{if(!this.isRange)return;let t=ym(e),r=t?this._getCellFromElement(t):null;t!==e.target&&(this._didDragSinceMouseDown=!0),fl(e.target)&&e.preventDefault(),this._ngZone.run(()=>this.previewChange.emit({value:r?.enabled?r:null,event:e}))};_leaveHandler=e=>{this.previewEnd!==null&&this.isRange&&(e.type!=="blur"&&(this._didDragSinceMouseDown=!0),e.target&&this._getCellFromElement(e.target)&&!(e.relatedTarget&&this._getCellFromElement(e.relatedTarget))&&this._ngZone.run(()=>this.previewChange.emit({value:null,event:e})))};_mousedownHandler=e=>{if(!this.isRange)return;this._didDragSinceMouseDown=!1;let t=e.target&&this._getCellFromElement(e.target);!t||!this._isInRange(t.compareValue)||this._ngZone.run(()=>{this.dragStarted.emit({value:t.rawValue,event:e})})};_mouseupHandler=e=>{if(!this.isRange)return;let t=fl(e.target);if(!t){this._ngZone.run(()=>{this.dragEnded.emit({value:null,event:e})});return}t.closest(".mat-calendar-body")===this._elementRef.nativeElement&&this._ngZone.run(()=>{let r=this._getCellFromElement(t);this.dragEnded.emit({value:r?.rawValue??null,event:e})})};_touchendHandler=e=>{let t=ym(e);t&&this._mouseupHandler({target:t})};_getCellFromElement(e){let t=fl(e);if(t){let r=t.getAttribute("data-mat-row"),a=t.getAttribute("data-mat-col");if(r&&a)return this.rows[parseInt(r)]?.[parseInt(a)]||null}return null}static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["","mat-calendar-body",""]],hostAttrs:[1,"mat-calendar-body"],inputs:{label:"label",rows:"rows",todayValue:"todayValue",startValue:"startValue",endValue:"endValue",labelMinRequiredCells:"labelMinRequiredCells",numCols:"numCols",activeCell:"activeCell",isRange:"isRange",cellAspectRatio:"cellAspectRatio",comparisonStart:"comparisonStart",comparisonEnd:"comparisonEnd",previewStart:"previewStart",previewEnd:"previewEnd",startDateAccessibleName:"startDateAccessibleName",endDateAccessibleName:"endDateAccessibleName"},outputs:{selectedValueChange:"selectedValueChange",previewChange:"previewChange",activeDateChange:"activeDateChange",dragStarted:"dragStarted",dragEnded:"dragEnded"},exportAs:["matCalendarBody"],features:[pe],attrs:Ob,decls:11,vars:11,consts:[["aria-hidden","true"],["role","row"],[1,"mat-calendar-body-hidden-label",3,"id"],[1,"mat-calendar-body-label"],[1,"mat-calendar-body-label",3,"paddingTop","paddingBottom"],["role","gridcell",1,"mat-calendar-body-cell-container",3,"width","paddingTop","paddingBottom"],["role","gridcell",1,"mat-calendar-body-cell-container"],["type","button",1,"mat-calendar-body-cell",3,"click","focus","tabindex"],[1,"mat-calendar-body-cell-content","mat-focus-indicator"],["aria-hidden","true",1,"mat-calendar-body-cell-preview"]],template:function(t,r){t&1&&(v(0,Fb,3,6,"tr",0),Be(1,Vb,4,1,"tr",1,Tb,!0),le(3,"span",2),P(4),he(),le(5,"span",2),P(6),he(),le(7,"span",2),P(8),he(),le(9,"span",2),P(10),he()),t&2&&(y(r._firstRowOffset<r.labelMinRequiredCells?0:-1),c(),je(r.rows),c(2),He("id",r._startDateLabelId),c(),Te(" ",r.startDateAccessibleName,`
`),c(),He("id",r._endDateLabelId),c(),Te(" ",r.endDateAccessibleName,`
`),c(),He("id",r._comparisonStartDateLabelId),c(),Fn(" ",r.comparisonDateAccessibleName," ",r.startDateAccessibleName,`
`),c(),He("id",r._comparisonEndDateLabelId),c(),Fn(" ",r.comparisonDateAccessibleName," ",r.endDateAccessibleName,`
`))},styles:[`.mat-calendar-body {
  min-width: 224px;
}

.mat-calendar-body-today:not(.mat-calendar-body-selected):not(.mat-calendar-body-comparison-identical) {
  border-color: var(--mat-datepicker-calendar-date-today-outline-color, var(--mat-sys-primary));
}

.mat-calendar-body-label {
  height: 0;
  line-height: 0;
  text-align: start;
  padding-left: 4.7142857143%;
  padding-right: 4.7142857143%;
  font-size: var(--mat-datepicker-calendar-body-label-text-size, var(--mat-sys-title-small-size));
  font-weight: var(--mat-datepicker-calendar-body-label-text-weight, var(--mat-sys-title-small-weight));
  color: var(--mat-datepicker-calendar-body-label-text-color, var(--mat-sys-on-surface));
}

.mat-calendar-body-hidden-label {
  display: none;
}

.mat-calendar-body-cell-container {
  position: relative;
  height: 0;
  line-height: 0;
}

.mat-calendar-body-cell {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: none;
  text-align: center;
  outline: none;
  margin: 0;
  font-family: var(--mat-datepicker-calendar-text-font, var(--mat-sys-body-medium-font));
  font-size: var(--mat-datepicker-calendar-text-size, var(--mat-sys-body-medium-size));
  -webkit-user-select: none;
  user-select: none;
  cursor: pointer;
  outline: none;
  border: none;
  -webkit-tap-highlight-color: transparent;
}
.mat-calendar-body-cell::-moz-focus-inner {
  border: 0;
}

.mat-calendar-body-cell::before,
.mat-calendar-body-cell::after,
.mat-calendar-body-cell-preview {
  content: "";
  position: absolute;
  top: 5%;
  left: 0;
  z-index: 0;
  box-sizing: border-box;
  display: block;
  height: 90%;
  width: 100%;
}

.mat-calendar-body-range-start:not(.mat-calendar-body-in-comparison-range)::before,
.mat-calendar-body-range-start::after,
.mat-calendar-body-comparison-start:not(.mat-calendar-body-comparison-bridge-start)::before,
.mat-calendar-body-comparison-start::after,
.mat-calendar-body-preview-start .mat-calendar-body-cell-preview {
  left: 5%;
  width: 95%;
  border-top-left-radius: 999px;
  border-bottom-left-radius: 999px;
}
[dir=rtl] .mat-calendar-body-range-start:not(.mat-calendar-body-in-comparison-range)::before,
[dir=rtl] .mat-calendar-body-range-start::after,
[dir=rtl] .mat-calendar-body-comparison-start:not(.mat-calendar-body-comparison-bridge-start)::before,
[dir=rtl] .mat-calendar-body-comparison-start::after,
[dir=rtl] .mat-calendar-body-preview-start .mat-calendar-body-cell-preview {
  left: 0;
  border-radius: 0;
  border-top-right-radius: 999px;
  border-bottom-right-radius: 999px;
}

.mat-calendar-body-range-end:not(.mat-calendar-body-in-comparison-range)::before,
.mat-calendar-body-range-end::after,
.mat-calendar-body-comparison-end:not(.mat-calendar-body-comparison-bridge-end)::before,
.mat-calendar-body-comparison-end::after,
.mat-calendar-body-preview-end .mat-calendar-body-cell-preview {
  width: 95%;
  border-top-right-radius: 999px;
  border-bottom-right-radius: 999px;
}
[dir=rtl] .mat-calendar-body-range-end:not(.mat-calendar-body-in-comparison-range)::before,
[dir=rtl] .mat-calendar-body-range-end::after,
[dir=rtl] .mat-calendar-body-comparison-end:not(.mat-calendar-body-comparison-bridge-end)::before,
[dir=rtl] .mat-calendar-body-comparison-end::after,
[dir=rtl] .mat-calendar-body-preview-end .mat-calendar-body-cell-preview {
  left: 5%;
  border-radius: 0;
  border-top-left-radius: 999px;
  border-bottom-left-radius: 999px;
}

[dir=rtl] .mat-calendar-body-comparison-bridge-start.mat-calendar-body-range-end::after,
[dir=rtl] .mat-calendar-body-comparison-bridge-end.mat-calendar-body-range-start::after {
  width: 95%;
  border-top-right-radius: 999px;
  border-bottom-right-radius: 999px;
}

.mat-calendar-body-comparison-start.mat-calendar-body-range-end::after, [dir=rtl] .mat-calendar-body-comparison-start.mat-calendar-body-range-end::after,
.mat-calendar-body-comparison-end.mat-calendar-body-range-start::after,
[dir=rtl] .mat-calendar-body-comparison-end.mat-calendar-body-range-start::after {
  width: 90%;
}

.mat-calendar-body-in-preview {
  color: var(--mat-datepicker-calendar-date-preview-state-outline-color, var(--mat-sys-primary));
}
.mat-calendar-body-in-preview .mat-calendar-body-cell-preview {
  border-top: dashed 1px;
  border-bottom: dashed 1px;
}

.mat-calendar-body-preview-start .mat-calendar-body-cell-preview {
  border-left: dashed 1px;
}
[dir=rtl] .mat-calendar-body-preview-start .mat-calendar-body-cell-preview {
  border-left: 0;
  border-right: dashed 1px;
}

.mat-calendar-body-preview-end .mat-calendar-body-cell-preview {
  border-right: dashed 1px;
}
[dir=rtl] .mat-calendar-body-preview-end .mat-calendar-body-cell-preview {
  border-right: 0;
  border-left: dashed 1px;
}

.mat-calendar-body-disabled {
  cursor: default;
}
.mat-calendar-body-disabled > .mat-calendar-body-cell-content:not(.mat-calendar-body-selected):not(.mat-calendar-body-comparison-identical) {
  color: var(--mat-datepicker-calendar-date-disabled-state-text-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}
.mat-calendar-body-disabled > .mat-calendar-body-today:not(.mat-calendar-body-selected):not(.mat-calendar-body-comparison-identical) {
  border-color: var(--mat-datepicker-calendar-date-today-disabled-state-outline-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}
@media (forced-colors: active) {
  .mat-calendar-body-disabled {
    opacity: 0.5;
  }
}

.mat-calendar-body-cell-content {
  top: 5%;
  left: 5%;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  width: 90%;
  height: 90%;
  line-height: 1;
  border-width: 1px;
  border-style: solid;
  border-radius: 999px;
  color: var(--mat-datepicker-calendar-date-text-color, var(--mat-sys-on-surface));
  border-color: var(--mat-datepicker-calendar-date-outline-color, transparent);
}
.mat-calendar-body-cell-content.mat-focus-indicator {
  position: absolute;
}
@media (forced-colors: active) {
  .mat-calendar-body-cell-content {
    border: none;
  }
}

.cdk-keyboard-focused .mat-calendar-body-active > .mat-calendar-body-cell-content:not(.mat-calendar-body-selected):not(.mat-calendar-body-comparison-identical), .cdk-program-focused .mat-calendar-body-active > .mat-calendar-body-cell-content:not(.mat-calendar-body-selected):not(.mat-calendar-body-comparison-identical) {
  background-color: var(--mat-datepicker-calendar-date-focus-state-background-color, color-mix(in srgb, var(--mat-sys-on-surface) calc(var(--mat-sys-focus-state-layer-opacity) * 100%), transparent));
}

@media (hover: hover) {
  .mat-calendar-body-cell:not(.mat-calendar-body-disabled):hover > .mat-calendar-body-cell-content:not(.mat-calendar-body-selected):not(.mat-calendar-body-comparison-identical) {
    background-color: var(--mat-datepicker-calendar-date-hover-state-background-color, color-mix(in srgb, var(--mat-sys-on-surface) calc(var(--mat-sys-hover-state-layer-opacity) * 100%), transparent));
  }
}
.mat-calendar-body-selected {
  background-color: var(--mat-datepicker-calendar-date-selected-state-background-color, var(--mat-sys-primary));
  color: var(--mat-datepicker-calendar-date-selected-state-text-color, var(--mat-sys-on-primary));
}
.mat-calendar-body-disabled > .mat-calendar-body-selected {
  background-color: var(--mat-datepicker-calendar-date-selected-disabled-state-background-color, color-mix(in srgb, var(--mat-sys-on-surface) 38%, transparent));
}
.mat-calendar-body-selected.mat-calendar-body-today {
  box-shadow: inset 0 0 0 1px var(--mat-datepicker-calendar-date-today-selected-state-outline-color, var(--mat-sys-primary));
}

.mat-calendar-body-in-range::before {
  background: var(--mat-datepicker-calendar-date-in-range-state-background-color, var(--mat-sys-primary-container));
}

.mat-calendar-body-comparison-identical,
.mat-calendar-body-in-comparison-range::before {
  background: var(--mat-datepicker-calendar-date-in-comparison-range-state-background-color, var(--mat-sys-tertiary-container));
}

.mat-calendar-body-comparison-identical,
.mat-calendar-body-in-comparison-range::before {
  background: var(--mat-datepicker-calendar-date-in-comparison-range-state-background-color, var(--mat-sys-tertiary-container));
}

.mat-calendar-body-comparison-bridge-start::before,
[dir=rtl] .mat-calendar-body-comparison-bridge-end::before {
  background: linear-gradient(to right, var(--mat-datepicker-calendar-date-in-range-state-background-color, var(--mat-sys-primary-container)) 50%, var(--mat-datepicker-calendar-date-in-comparison-range-state-background-color, var(--mat-sys-tertiary-container)) 50%);
}

.mat-calendar-body-comparison-bridge-end::before,
[dir=rtl] .mat-calendar-body-comparison-bridge-start::before {
  background: linear-gradient(to left, var(--mat-datepicker-calendar-date-in-range-state-background-color, var(--mat-sys-primary-container)) 50%, var(--mat-datepicker-calendar-date-in-comparison-range-state-background-color, var(--mat-sys-tertiary-container)) 50%);
}

.mat-calendar-body-in-range > .mat-calendar-body-comparison-identical,
.mat-calendar-body-in-comparison-range.mat-calendar-body-in-range::after {
  background: var(--mat-datepicker-calendar-date-in-overlap-range-state-background-color, var(--mat-sys-secondary-container));
}

.mat-calendar-body-comparison-identical.mat-calendar-body-selected,
.mat-calendar-body-in-comparison-range > .mat-calendar-body-selected {
  background: var(--mat-datepicker-calendar-date-in-overlap-range-selected-state-background-color, var(--mat-sys-secondary));
}

@media (forced-colors: active) {
  .mat-datepicker-popup:not(:empty),
  .mat-calendar-body-cell:not(.mat-calendar-body-in-range) .mat-calendar-body-selected {
    outline: solid 1px;
  }
  .mat-calendar-body-today {
    outline: dotted 1px;
  }
  .mat-calendar-body-cell::before,
  .mat-calendar-body-cell::after,
  .mat-calendar-body-selected {
    background: none;
  }
  .mat-calendar-body-in-range::before,
  .mat-calendar-body-comparison-bridge-start::before,
  .mat-calendar-body-comparison-bridge-end::before {
    border-top: solid 1px;
    border-bottom: solid 1px;
  }
  .mat-calendar-body-range-start::before {
    border-left: solid 1px;
  }
  [dir=rtl] .mat-calendar-body-range-start::before {
    border-left: 0;
    border-right: solid 1px;
  }
  .mat-calendar-body-range-end::before {
    border-right: solid 1px;
  }
  [dir=rtl] .mat-calendar-body-range-end::before {
    border-right: 0;
    border-left: solid 1px;
  }
  .mat-calendar-body-in-comparison-range::before {
    border-top: dashed 1px;
    border-bottom: dashed 1px;
  }
  .mat-calendar-body-comparison-start::before {
    border-left: dashed 1px;
  }
  [dir=rtl] .mat-calendar-body-comparison-start::before {
    border-left: 0;
    border-right: dashed 1px;
  }
  .mat-calendar-body-comparison-end::before {
    border-right: dashed 1px;
  }
  [dir=rtl] .mat-calendar-body-comparison-end::before {
    border-right: 0;
    border-left: dashed 1px;
  }
}
`],encapsulation:2,changeDetection:0})}return i})();function hl(i){return i?.nodeName==="TD"}function fl(i){let n;return hl(i)?n=i:hl(i.parentNode)?n=i.parentNode:hl(i.parentNode?.parentNode)&&(n=i.parentNode.parentNode),n?.getAttribute("data-mat-row")!=null?n:null}function gl(i,n,e){return e!==null&&n!==e&&i<e&&i===n}function _l(i,n,e){return n!==null&&n!==e&&i>=n&&i===e}function bl(i,n,e,t){return t&&n!==null&&e!==null&&n!==e&&i>=n&&i<=e}function ym(i){let n=i.changedTouches[0];return document.elementFromPoint(n.clientX,n.clientY)}var Dt=class{start;end;_disableStructuralEquivalency;constructor(n,e){this.start=n,this.end=e}},Lr=(()=>{class i{selection;_adapter;_selectionChanged=new F;selectionChanged=this._selectionChanged;constructor(e,t){this.selection=e,this._adapter=t,this.selection=e}updateSelection(e,t){let r=this.selection;this.selection=e,this._selectionChanged.next({selection:e,source:t,oldValue:r})}ngOnDestroy(){this._selectionChanged.complete()}_isValidDateInstance(e){return this._adapter.isDateInstance(e)&&this._adapter.isValid(e)}static \u0275fac=function(t){na()};static \u0275prov=K({token:i,factory:i.\u0275fac})}return i})(),Zb=(()=>{class i extends Lr{constructor(e){super(null,e)}add(e){super.updateSelection(e,this)}isValid(){return this.selection!=null&&this._isValidDateInstance(this.selection)}isComplete(){return this.selection!=null}clone(){let e=new i(this._adapter);return e.updateSelection(this.selection,this),e}static \u0275fac=function(t){return new(t||i)(In(mt))};static \u0275prov=K({token:i,factory:i.\u0275fac})}return i})();var Em={provide:Lr,useFactory:()=>l(Lr,{optional:!0,skipSelf:!0})||new Zb(l(mt))};var km=new M("MAT_DATE_RANGE_SELECTION_STRATEGY");var vl=7,Qb=0,Cm=(()=>{class i{_changeDetectorRef=l(ne);_dateFormats=l(Fi,{optional:!0});_dateAdapter=l(mt,{optional:!0});_dir=l(Ze,{optional:!0});_rangeStrategy=l(km,{optional:!0});_rerenderSubscription=oe.EMPTY;_selectionKeyPressed=!1;get activeDate(){return this._activeDate}set activeDate(e){let t=this._activeDate,r=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e))||this._dateAdapter.today();this._activeDate=this._dateAdapter.clampDate(r,this.minDate,this.maxDate),this._hasSameMonthAndYear(t,this._activeDate)||this._init()}_activeDate;get selected(){return this._selected}set selected(e){e instanceof Dt?this._selected=e:this._selected=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e)),this._setRanges(this._selected)}_selected=null;get minDate(){return this._minDate}set minDate(e){this._minDate=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e))}_minDate=null;get maxDate(){return this._maxDate}set maxDate(e){this._maxDate=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e))}_maxDate=null;dateFilter;dateClass;comparisonStart=null;comparisonEnd=null;startDateAccessibleName=null;endDateAccessibleName=null;activeDrag=null;selectedChange=new S;_userSelection=new S;dragStarted=new S;dragEnded=new S;activeDateChange=new S;_matCalendarBody;_monthLabel=V("");_weeks=V([]);_firstWeekOffset=V(0);_rangeStart=V(null);_rangeEnd=V(null);_comparisonRangeStart=V(null);_comparisonRangeEnd=V(null);_previewStart=V(null);_previewEnd=V(null);_isRange=V(!1);_todayDate=V(null);_weekdays=V([]);constructor(){l(Ue).load(Ut),this._activeDate=this._dateAdapter.today()}ngAfterContentInit(){this._rerenderSubscription=this._dateAdapter.localeChanges.pipe(Le(null)).subscribe(()=>this._init())}ngOnChanges(e){let t=e.comparisonStart||e.comparisonEnd;t&&!t.firstChange&&this._setRanges(this.selected),e.activeDrag&&!this.activeDrag&&this._clearPreview()}ngOnDestroy(){this._rerenderSubscription.unsubscribe()}_dateSelected(e){let t=e.value,r=this._getDateFromDayOfMonth(t),a,o;this._selected instanceof Dt?(a=this._getDateInCurrentMonth(this._selected.start),o=this._getDateInCurrentMonth(this._selected.end)):a=o=this._getDateInCurrentMonth(this._selected),(a!==t||o!==t)&&this.selectedChange.emit(r),this._userSelection.emit({value:r,event:e.event}),this._clearPreview(),this._changeDetectorRef.markForCheck()}_updateActiveDate(e){let t=e.value,r=this._activeDate;this.activeDate=this._getDateFromDayOfMonth(t),this._dateAdapter.compareDate(r,this.activeDate)&&this.activeDateChange.emit(this._activeDate)}_handleCalendarBodyKeydown(e){let t=this._activeDate,r=this._isRtl();switch(e.keyCode){case 37:this.activeDate=this._dateAdapter.addCalendarDays(this._activeDate,r?1:-1);break;case 39:this.activeDate=this._dateAdapter.addCalendarDays(this._activeDate,r?-1:1);break;case 38:this.activeDate=this._dateAdapter.addCalendarDays(this._activeDate,-7);break;case 40:this.activeDate=this._dateAdapter.addCalendarDays(this._activeDate,7);break;case 36:this.activeDate=this._dateAdapter.addCalendarDays(this._activeDate,1-this._dateAdapter.getDate(this._activeDate));break;case 35:this.activeDate=this._dateAdapter.addCalendarDays(this._activeDate,this._dateAdapter.getNumDaysInMonth(this._activeDate)-this._dateAdapter.getDate(this._activeDate));break;case 33:this.activeDate=e.altKey?this._dateAdapter.addCalendarYears(this._activeDate,-1):this._dateAdapter.addCalendarMonths(this._activeDate,-1);break;case 34:this.activeDate=e.altKey?this._dateAdapter.addCalendarYears(this._activeDate,1):this._dateAdapter.addCalendarMonths(this._activeDate,1);break;case 13:case 32:this._selectionKeyPressed=!0,this._canSelect(this._activeDate)&&e.preventDefault();return;case 27:this._previewEnd()!=null&&!xe(e)&&(this._clearPreview(),this.activeDrag?this.dragEnded.emit({value:null,event:e}):(this.selectedChange.emit(null),this._userSelection.emit({value:null,event:e})),e.preventDefault(),e.stopPropagation());return;default:return}this._dateAdapter.compareDate(t,this.activeDate)&&(this.activeDateChange.emit(this.activeDate),this._focusActiveCellAfterViewChecked()),e.preventDefault()}_handleCalendarBodyKeyup(e){(e.keyCode===32||e.keyCode===13)&&(this._selectionKeyPressed&&this._canSelect(this._activeDate)&&this._dateSelected({value:this._dateAdapter.getDate(this._activeDate),event:e}),this._selectionKeyPressed=!1)}_init(){this._setRanges(this.selected),this._todayDate.set(this._getCellCompareValue(this._dateAdapter.today())),this._monthLabel.set(this._dateFormats.display.monthLabel?this._dateAdapter.format(this.activeDate,this._dateFormats.display.monthLabel):this._dateAdapter.getMonthNames("short")[this._dateAdapter.getMonth(this.activeDate)].toLocaleUpperCase());let e=this._dateAdapter.createDate(this._dateAdapter.getYear(this.activeDate),this._dateAdapter.getMonth(this.activeDate),1);this._firstWeekOffset.set((vl+this._dateAdapter.getDayOfWeek(e)-this._dateAdapter.getFirstDayOfWeek())%vl),this._initWeekdays(),this._createWeekCells(),this._changeDetectorRef.markForCheck()}_focusActiveCell(e){this._matCalendarBody._focusActiveCell(e)}_focusActiveCellAfterViewChecked(){this._matCalendarBody._scheduleFocusActiveCellAfterViewChecked()}_previewChanged({event:e,value:t}){if(this._rangeStrategy){let r=t?t.rawValue:null,a=this._rangeStrategy.createPreview(r,this.selected,e);if(this._previewStart.set(this._getCellCompareValue(a.start)),this._previewEnd.set(this._getCellCompareValue(a.end)),this.activeDrag&&r){let o=this._rangeStrategy.createDrag?.(this.activeDrag.value,this.selected,r,e);o&&(this._previewStart.set(this._getCellCompareValue(o.start)),this._previewEnd.set(this._getCellCompareValue(o.end)))}}}_dragEnded(e){if(this.activeDrag)if(e.value){let t=this._rangeStrategy?.createDrag?.(this.activeDrag.value,this.selected,e.value,e.event);this.dragEnded.emit({value:t??null,event:e.event})}else this.dragEnded.emit({value:null,event:e.event})}_getDateFromDayOfMonth(e){return this._dateAdapter.createDate(this._dateAdapter.getYear(this.activeDate),this._dateAdapter.getMonth(this.activeDate),e)}_initWeekdays(){let e=this._dateAdapter.getFirstDayOfWeek(),t=this._dateAdapter.getDayOfWeekNames("narrow"),a=this._dateAdapter.getDayOfWeekNames("long").map((o,s)=>({long:o,narrow:t[s],id:Qb++}));this._weekdays.set(a.slice(e).concat(a.slice(0,e)))}_createWeekCells(){let e=this._dateAdapter.getNumDaysInMonth(this.activeDate),t=this._dateAdapter.getDateNames(),r=[[]];for(let a=0,o=this._firstWeekOffset();a<e;a++,o++){o==vl&&(r.push([]),o=0);let s=this._dateAdapter.createDate(this._dateAdapter.getYear(this.activeDate),this._dateAdapter.getMonth(this.activeDate),a+1),d=this._shouldEnableDate(s),u=this._dateAdapter.format(s,this._dateFormats.display.dateA11yLabel),b=this.dateClass?this.dateClass(s,"month"):void 0;r[r.length-1].push(new Nr(a+1,t[a],u,d,b,this._getCellCompareValue(s),s))}this._weeks.set(r)}_shouldEnableDate(e){return!!e&&(!this.minDate||this._dateAdapter.compareDate(e,this.minDate)>=0)&&(!this.maxDate||this._dateAdapter.compareDate(e,this.maxDate)<=0)&&(!this.dateFilter||this.dateFilter(e))}_getDateInCurrentMonth(e){return e&&this._hasSameMonthAndYear(e,this.activeDate)?this._dateAdapter.getDate(e):null}_hasSameMonthAndYear(e,t){return!!(e&&t&&this._dateAdapter.getMonth(e)==this._dateAdapter.getMonth(t)&&this._dateAdapter.getYear(e)==this._dateAdapter.getYear(t))}_getCellCompareValue(e){if(e){let t=this._dateAdapter.getYear(e),r=this._dateAdapter.getMonth(e),a=this._dateAdapter.getDate(e);return new Date(t,r,a).getTime()}return null}_isRtl(){return this._dir&&this._dir.value==="rtl"}_setRanges(e){e instanceof Dt?(this._rangeStart.set(this._getCellCompareValue(e.start)),this._rangeEnd.set(this._getCellCompareValue(e.end)),this._isRange.set(!0)):(this._rangeStart.set(this._getCellCompareValue(e)),this._rangeEnd.set(this._rangeStart()),this._isRange.set(!1)),this._comparisonRangeStart.set(this._getCellCompareValue(this.comparisonStart)),this._comparisonRangeEnd.set(this._getCellCompareValue(this.comparisonEnd))}_canSelect(e){return!this.dateFilter||this.dateFilter(e)}_clearPreview(){this._previewStart.set(null),this._previewEnd.set(null)}static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["mat-month-view"]],viewQuery:function(t,r){if(t&1&&ke(Ri,5),t&2){let a;z(a=H())&&(r._matCalendarBody=a.first)}},inputs:{activeDate:"activeDate",selected:"selected",minDate:"minDate",maxDate:"maxDate",dateFilter:"dateFilter",dateClass:"dateClass",comparisonStart:"comparisonStart",comparisonEnd:"comparisonEnd",startDateAccessibleName:"startDateAccessibleName",endDateAccessibleName:"endDateAccessibleName",activeDrag:"activeDrag"},outputs:{selectedChange:"selectedChange",_userSelection:"_userSelection",dragStarted:"dragStarted",dragEnded:"dragEnded",activeDateChange:"activeDateChange"},exportAs:["matMonthView"],features:[pe],decls:8,vars:14,consts:[["role","grid",1,"mat-calendar-table"],[1,"mat-calendar-table-header"],["scope","col"],["aria-hidden","true"],["colspan","7",1,"mat-calendar-table-header-divider"],["mat-calendar-body","",3,"selectedValueChange","activeDateChange","previewChange","dragStarted","dragEnded","keyup","keydown","label","rows","todayValue","startValue","endValue","comparisonStart","comparisonEnd","previewStart","previewEnd","isRange","labelMinRequiredCells","activeCell","startDateAccessibleName","endDateAccessibleName"],[1,"cdk-visually-hidden"]],template:function(t,r){t&1&&(p(0,"table",0)(1,"thead",1)(2,"tr"),Be(3,Nb,5,2,"th",2,wm),h(),p(5,"tr",3),_(6,"th",4),h()(),p(7,"tbody",5),T("selectedValueChange",function(o){return r._dateSelected(o)})("activeDateChange",function(o){return r._updateActiveDate(o)})("previewChange",function(o){return r._previewChanged(o)})("dragStarted",function(o){return r.dragStarted.emit(o)})("dragEnded",function(o){return r._dragEnded(o)})("keyup",function(o){return r._handleCalendarBodyKeyup(o)})("keydown",function(o){return r._handleCalendarBodyKeydown(o)}),h()()),t&2&&(c(3),je(r._weekdays()),c(4),f("label",r._monthLabel())("rows",r._weeks())("todayValue",r._todayDate())("startValue",r._rangeStart())("endValue",r._rangeEnd())("comparisonStart",r._comparisonRangeStart())("comparisonEnd",r._comparisonRangeEnd())("previewStart",r._previewStart())("previewEnd",r._previewEnd())("isRange",r._isRange())("labelMinRequiredCells",3)("activeCell",r._dateAdapter.getDate(r.activeDate)-1)("startDateAccessibleName",r.startDateAccessibleName)("endDateAccessibleName",r.endDateAccessibleName))},dependencies:[Ri],encapsulation:2,changeDetection:0})}return i})(),pt=24,yl=4,xm=(()=>{class i{_changeDetectorRef=l(ne);_dateAdapter=l(mt,{optional:!0});_dir=l(Ze,{optional:!0});_rerenderSubscription=oe.EMPTY;_selectionKeyPressed=!1;get activeDate(){return this._activeDate}set activeDate(e){let t=this._activeDate,r=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e))||this._dateAdapter.today();this._activeDate=this._dateAdapter.clampDate(r,this.minDate,this.maxDate),Am(this._dateAdapter,t,this._activeDate,this.minDate,this.maxDate)||this._init()}_activeDate;get selected(){return this._selected}set selected(e){e instanceof Dt?this._selected=e:this._selected=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e)),this._setSelectedYear(e)}_selected=null;get minDate(){return this._minDate}set minDate(e){this._minDate=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e))}_minDate=null;get maxDate(){return this._maxDate}set maxDate(e){this._maxDate=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e))}_maxDate=null;dateFilter;dateClass;selectedChange=new S;yearSelected=new S;activeDateChange=new S;_matCalendarBody;_years=V([]);_todayYear=V(0);_selectedYear=V(null);constructor(){this._dateAdapter,this._activeDate=this._dateAdapter.today()}ngAfterContentInit(){this._rerenderSubscription=this._dateAdapter.localeChanges.pipe(Le(null)).subscribe(()=>this._init())}ngOnDestroy(){this._rerenderSubscription.unsubscribe()}_init(){this._todayYear.set(this._dateAdapter.getYear(this._dateAdapter.today()));let t=this._dateAdapter.getYear(this._activeDate)-Vr(this._dateAdapter,this.activeDate,this.minDate,this.maxDate),r=[];for(let a=0,o=[];a<pt;a++)o.push(t+a),o.length==yl&&(r.push(o.map(s=>this._createCellForYear(s))),o=[]);this._years.set(r),this._changeDetectorRef.markForCheck()}_yearSelected(e){let t=e.value,r=this._dateAdapter.createDate(t,0,1),a=this._getDateFromYear(t);this.yearSelected.emit(r),this.selectedChange.emit(a)}_updateActiveDate(e){let t=e.value,r=this._activeDate;this.activeDate=this._getDateFromYear(t),this._dateAdapter.compareDate(r,this.activeDate)&&this.activeDateChange.emit(this.activeDate)}_handleCalendarBodyKeydown(e){let t=this._activeDate,r=this._isRtl();switch(e.keyCode){case 37:this.activeDate=this._dateAdapter.addCalendarYears(this._activeDate,r?1:-1);break;case 39:this.activeDate=this._dateAdapter.addCalendarYears(this._activeDate,r?-1:1);break;case 38:this.activeDate=this._dateAdapter.addCalendarYears(this._activeDate,-yl);break;case 40:this.activeDate=this._dateAdapter.addCalendarYears(this._activeDate,yl);break;case 36:this.activeDate=this._dateAdapter.addCalendarYears(this._activeDate,-Vr(this._dateAdapter,this.activeDate,this.minDate,this.maxDate));break;case 35:this.activeDate=this._dateAdapter.addCalendarYears(this._activeDate,pt-Vr(this._dateAdapter,this.activeDate,this.minDate,this.maxDate)-1);break;case 33:this.activeDate=this._dateAdapter.addCalendarYears(this._activeDate,e.altKey?-pt*10:-pt);break;case 34:this.activeDate=this._dateAdapter.addCalendarYears(this._activeDate,e.altKey?pt*10:pt);break;case 13:case 32:this._selectionKeyPressed=!0;break;default:return}this._dateAdapter.compareDate(t,this.activeDate)&&this.activeDateChange.emit(this.activeDate),this._focusActiveCellAfterViewChecked(),e.preventDefault()}_handleCalendarBodyKeyup(e){(e.keyCode===32||e.keyCode===13)&&(this._selectionKeyPressed&&this._yearSelected({value:this._dateAdapter.getYear(this._activeDate),event:e}),this._selectionKeyPressed=!1)}_getActiveCell(){return Vr(this._dateAdapter,this.activeDate,this.minDate,this.maxDate)}_focusActiveCell(){this._matCalendarBody._focusActiveCell()}_focusActiveCellAfterViewChecked(){this._matCalendarBody._scheduleFocusActiveCellAfterViewChecked()}_getDateFromYear(e){let t=this._dateAdapter.getMonth(this.activeDate),r=this._dateAdapter.getNumDaysInMonth(this._dateAdapter.createDate(e,t,1));return this._dateAdapter.createDate(e,t,Math.min(this._dateAdapter.getDate(this.activeDate),r))}_createCellForYear(e){let t=this._dateAdapter.createDate(e,0,1),r=this._dateAdapter.getYearName(t),a=this.dateClass?this.dateClass(t,"multi-year"):void 0;return new Nr(e,r,r,this._shouldEnableYear(e),a)}_shouldEnableYear(e){if(e==null||this.maxDate&&e>this._dateAdapter.getYear(this.maxDate)||this.minDate&&e<this._dateAdapter.getYear(this.minDate))return!1;if(!this.dateFilter)return!0;let t=this._dateAdapter.createDate(e,0,1);for(let r=t;this._dateAdapter.getYear(r)==e;r=this._dateAdapter.addCalendarDays(r,1))if(this.dateFilter(r))return!0;return!1}_isRtl(){return this._dir&&this._dir.value==="rtl"}_setSelectedYear(e){if(this._selectedYear.set(null),e instanceof Dt){let t=e.start||e.end;t&&this._selectedYear.set(this._dateAdapter.getYear(t))}else e&&this._selectedYear.set(this._dateAdapter.getYear(e))}static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["mat-multi-year-view"]],viewQuery:function(t,r){if(t&1&&ke(Ri,5),t&2){let a;z(a=H())&&(r._matCalendarBody=a.first)}},inputs:{activeDate:"activeDate",selected:"selected",minDate:"minDate",maxDate:"maxDate",dateFilter:"dateFilter",dateClass:"dateClass"},outputs:{selectedChange:"selectedChange",yearSelected:"yearSelected",activeDateChange:"activeDateChange"},exportAs:["matMultiYearView"],decls:5,vars:7,consts:[["role","grid",1,"mat-calendar-table"],["aria-hidden","true",1,"mat-calendar-table-header"],["colspan","4",1,"mat-calendar-table-header-divider"],["mat-calendar-body","",3,"selectedValueChange","activeDateChange","keyup","keydown","rows","todayValue","startValue","endValue","numCols","cellAspectRatio","activeCell"]],template:function(t,r){t&1&&(p(0,"table",0)(1,"thead",1)(2,"tr"),_(3,"th",2),h()(),p(4,"tbody",3),T("selectedValueChange",function(o){return r._yearSelected(o)})("activeDateChange",function(o){return r._updateActiveDate(o)})("keyup",function(o){return r._handleCalendarBodyKeyup(o)})("keydown",function(o){return r._handleCalendarBodyKeydown(o)}),h()()),t&2&&(c(4),f("rows",r._years())("todayValue",r._todayYear())("startValue",r._selectedYear())("endValue",r._selectedYear())("numCols",4)("cellAspectRatio",4/7)("activeCell",r._getActiveCell()))},dependencies:[Ri],encapsulation:2,changeDetection:0})}return i})();function Am(i,n,e,t,r){let a=i.getYear(n),o=i.getYear(e),s=Sm(i,t,r);return Math.floor((a-s)/pt)===Math.floor((o-s)/pt)}function Vr(i,n,e,t){let r=i.getYear(n);return Xb(r-Sm(i,e,t),pt)}function Sm(i,n,e){let t=0;return e?t=i.getYear(e)-pt+1:n&&(t=i.getYear(n)),t}function Xb(i,n){return(i%n+n)%n}var Dm=(()=>{class i{_changeDetectorRef=l(ne);_dateFormats=l(Fi,{optional:!0});_dateAdapter=l(mt,{optional:!0});_dir=l(Ze,{optional:!0});_rerenderSubscription=oe.EMPTY;_selectionKeyPressed=!1;get activeDate(){return this._activeDate}set activeDate(e){let t=this._activeDate,r=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e))||this._dateAdapter.today();this._activeDate=this._dateAdapter.clampDate(r,this.minDate,this.maxDate),this._dateAdapter.getYear(t)!==this._dateAdapter.getYear(this._activeDate)&&this._init()}_activeDate;get selected(){return this._selected}set selected(e){e instanceof Dt?this._selected=e:this._selected=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e)),this._setSelectedMonth(e)}_selected=null;get minDate(){return this._minDate}set minDate(e){this._minDate=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e))}_minDate=null;get maxDate(){return this._maxDate}set maxDate(e){this._maxDate=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e))}_maxDate=null;dateFilter;dateClass;selectedChange=new S;monthSelected=new S;activeDateChange=new S;_matCalendarBody;_months=V([]);_yearLabel=V("");_todayMonth=V(null);_selectedMonth=V(null);constructor(){this._activeDate=this._dateAdapter.today()}ngAfterContentInit(){this._rerenderSubscription=this._dateAdapter.localeChanges.pipe(Le(null)).subscribe(()=>this._init())}ngOnDestroy(){this._rerenderSubscription.unsubscribe()}_monthSelected(e){let t=e.value,r=this._dateAdapter.createDate(this._dateAdapter.getYear(this.activeDate),t,1);this.monthSelected.emit(r);let a=this._getDateFromMonth(t);this.selectedChange.emit(a)}_updateActiveDate(e){let t=e.value,r=this._activeDate;this.activeDate=this._getDateFromMonth(t),this._dateAdapter.compareDate(r,this.activeDate)&&this.activeDateChange.emit(this.activeDate)}_handleCalendarBodyKeydown(e){let t=this._activeDate,r=this._isRtl();switch(e.keyCode){case 37:this.activeDate=this._dateAdapter.addCalendarMonths(this._activeDate,r?1:-1);break;case 39:this.activeDate=this._dateAdapter.addCalendarMonths(this._activeDate,r?-1:1);break;case 38:this.activeDate=this._dateAdapter.addCalendarMonths(this._activeDate,-4);break;case 40:this.activeDate=this._dateAdapter.addCalendarMonths(this._activeDate,4);break;case 36:this.activeDate=this._dateAdapter.addCalendarMonths(this._activeDate,-this._dateAdapter.getMonth(this._activeDate));break;case 35:this.activeDate=this._dateAdapter.addCalendarMonths(this._activeDate,11-this._dateAdapter.getMonth(this._activeDate));break;case 33:this.activeDate=this._dateAdapter.addCalendarYears(this._activeDate,e.altKey?-10:-1);break;case 34:this.activeDate=this._dateAdapter.addCalendarYears(this._activeDate,e.altKey?10:1);break;case 13:case 32:this._selectionKeyPressed=!0;break;default:return}this._dateAdapter.compareDate(t,this.activeDate)&&(this.activeDateChange.emit(this.activeDate),this._focusActiveCellAfterViewChecked()),e.preventDefault()}_handleCalendarBodyKeyup(e){(e.keyCode===32||e.keyCode===13)&&(this._selectionKeyPressed&&this._monthSelected({value:this._dateAdapter.getMonth(this._activeDate),event:e}),this._selectionKeyPressed=!1)}_init(){this._setSelectedMonth(this.selected),this._todayMonth.set(this._getMonthInCurrentYear(this._dateAdapter.today())),this._yearLabel.set(this._dateAdapter.getYearName(this.activeDate));let e=this._dateAdapter.getMonthNames("short");this._months.set([[0,1,2,3],[4,5,6,7],[8,9,10,11]].map(t=>t.map(r=>this._createCellForMonth(r,e[r])))),this._changeDetectorRef.markForCheck()}_focusActiveCell(){this._matCalendarBody._focusActiveCell()}_focusActiveCellAfterViewChecked(){this._matCalendarBody._scheduleFocusActiveCellAfterViewChecked()}_getMonthInCurrentYear(e){return e&&this._dateAdapter.getYear(e)==this._dateAdapter.getYear(this.activeDate)?this._dateAdapter.getMonth(e):null}_getDateFromMonth(e){let t=this._dateAdapter.createDate(this._dateAdapter.getYear(this.activeDate),e,1),r=this._dateAdapter.getNumDaysInMonth(t);return this._dateAdapter.createDate(this._dateAdapter.getYear(this.activeDate),e,Math.min(this._dateAdapter.getDate(this.activeDate),r))}_createCellForMonth(e,t){let r=this._dateAdapter.createDate(this._dateAdapter.getYear(this.activeDate),e,1),a=this._dateAdapter.format(r,this._dateFormats.display.monthYearA11yLabel),o=this.dateClass?this.dateClass(r,"year"):void 0;return new Nr(e,t.toLocaleUpperCase(),a,this._shouldEnableMonth(e),o)}_shouldEnableMonth(e){let t=this._dateAdapter.getYear(this.activeDate);if(e==null||this._isYearAndMonthAfterMaxDate(t,e)||this._isYearAndMonthBeforeMinDate(t,e))return!1;if(!this.dateFilter)return!0;let r=this._dateAdapter.createDate(t,e,1);for(let a=r;this._dateAdapter.getMonth(a)==e;a=this._dateAdapter.addCalendarDays(a,1))if(this.dateFilter(a))return!0;return!1}_isYearAndMonthAfterMaxDate(e,t){if(this.maxDate){let r=this._dateAdapter.getYear(this.maxDate),a=this._dateAdapter.getMonth(this.maxDate);return e>r||e===r&&t>a}return!1}_isYearAndMonthBeforeMinDate(e,t){if(this.minDate){let r=this._dateAdapter.getYear(this.minDate),a=this._dateAdapter.getMonth(this.minDate);return e<r||e===r&&t<a}return!1}_isRtl(){return this._dir&&this._dir.value==="rtl"}_setSelectedMonth(e){e instanceof Dt?this._selectedMonth.set(this._getMonthInCurrentYear(e.start)||this._getMonthInCurrentYear(e.end)):this._selectedMonth.set(this._getMonthInCurrentYear(e))}static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["mat-year-view"]],viewQuery:function(t,r){if(t&1&&ke(Ri,5),t&2){let a;z(a=H())&&(r._matCalendarBody=a.first)}},inputs:{activeDate:"activeDate",selected:"selected",minDate:"minDate",maxDate:"maxDate",dateFilter:"dateFilter",dateClass:"dateClass"},outputs:{selectedChange:"selectedChange",monthSelected:"monthSelected",activeDateChange:"activeDateChange"},exportAs:["matYearView"],decls:5,vars:9,consts:[["role","grid",1,"mat-calendar-table"],["aria-hidden","true",1,"mat-calendar-table-header"],["colspan","4",1,"mat-calendar-table-header-divider"],["mat-calendar-body","",3,"selectedValueChange","activeDateChange","keyup","keydown","label","rows","todayValue","startValue","endValue","labelMinRequiredCells","numCols","cellAspectRatio","activeCell"]],template:function(t,r){t&1&&(p(0,"table",0)(1,"thead",1)(2,"tr"),_(3,"th",2),h()(),p(4,"tbody",3),T("selectedValueChange",function(o){return r._monthSelected(o)})("activeDateChange",function(o){return r._updateActiveDate(o)})("keyup",function(o){return r._handleCalendarBodyKeyup(o)})("keydown",function(o){return r._handleCalendarBodyKeydown(o)}),h()()),t&2&&(c(4),f("label",r._yearLabel())("rows",r._months())("todayValue",r._todayMonth())("startValue",r._selectedMonth())("endValue",r._selectedMonth())("labelMinRequiredCells",2)("numCols",4)("cellAspectRatio",4/7)("activeCell",r._dateAdapter.getMonth(r.activeDate)))},dependencies:[Ri],encapsulation:2,changeDetection:0})}return i})(),Im=(()=>{class i{_intl=l(Vi);calendar=l(Cl);_dateAdapter=l(mt,{optional:!0});_dateFormats=l(Fi,{optional:!0});_periodButtonText;_periodButtonDescription;_periodButtonLabel;_prevButtonLabel;_nextButtonLabel;constructor(){l(Ue).load(Ut);let e=l(ne);this._updateLabels(),this.calendar.stateChanges.subscribe(()=>{this._updateLabels(),e.markForCheck()})}get periodButtonText(){return this._periodButtonText}get periodButtonDescription(){return this._periodButtonDescription}get periodButtonLabel(){return this._periodButtonLabel}get prevButtonLabel(){return this._prevButtonLabel}get nextButtonLabel(){return this._nextButtonLabel}currentPeriodClicked(){this.calendar.currentView=this.calendar.currentView=="month"?"multi-year":"month"}previousClicked(){this.previousEnabled()&&(this.calendar.activeDate=this.calendar.currentView=="month"?this._dateAdapter.addCalendarMonths(this.calendar.activeDate,-1):this._dateAdapter.addCalendarYears(this.calendar.activeDate,this.calendar.currentView=="year"?-1:-pt))}nextClicked(){this.nextEnabled()&&(this.calendar.activeDate=this.calendar.currentView=="month"?this._dateAdapter.addCalendarMonths(this.calendar.activeDate,1):this._dateAdapter.addCalendarYears(this.calendar.activeDate,this.calendar.currentView=="year"?1:pt))}previousEnabled(){return this.calendar.minDate?!this.calendar.minDate||!this._isSameView(this.calendar.activeDate,this.calendar.minDate):!0}nextEnabled(){return!this.calendar.maxDate||!this._isSameView(this.calendar.activeDate,this.calendar.maxDate)}_updateLabels(){let e=this.calendar,t=this._intl,r=this._dateAdapter;e.currentView==="month"?(this._periodButtonText=r.format(e.activeDate,this._dateFormats.display.monthYearLabel).toLocaleUpperCase(),this._periodButtonDescription=r.format(e.activeDate,this._dateFormats.display.monthYearLabel).toLocaleUpperCase(),this._periodButtonLabel=t.switchToMultiYearViewLabel,this._prevButtonLabel=t.prevMonthLabel,this._nextButtonLabel=t.nextMonthLabel):e.currentView==="year"?(this._periodButtonText=r.getYearName(e.activeDate),this._periodButtonDescription=r.getYearName(e.activeDate),this._periodButtonLabel=t.switchToMonthViewLabel,this._prevButtonLabel=t.prevYearLabel,this._nextButtonLabel=t.nextYearLabel):(this._periodButtonText=t.formatYearRange(...this._formatMinAndMaxYearLabels()),this._periodButtonDescription=t.formatYearRangeLabel(...this._formatMinAndMaxYearLabels()),this._periodButtonLabel=t.switchToMonthViewLabel,this._prevButtonLabel=t.prevMultiYearLabel,this._nextButtonLabel=t.nextMultiYearLabel)}_isSameView(e,t){return this.calendar.currentView=="month"?this._dateAdapter.getYear(e)==this._dateAdapter.getYear(t)&&this._dateAdapter.getMonth(e)==this._dateAdapter.getMonth(t):this.calendar.currentView=="year"?this._dateAdapter.getYear(e)==this._dateAdapter.getYear(t):Am(this._dateAdapter,e,t,this.calendar.minDate,this.calendar.maxDate)}_formatMinAndMaxYearLabels(){let t=this._dateAdapter.getYear(this.calendar.activeDate)-Vr(this._dateAdapter,this.calendar.activeDate,this.calendar.minDate,this.calendar.maxDate),r=t+pt-1,a=this._dateAdapter.getYearName(this._dateAdapter.createDate(t,0,1)),o=this._dateAdapter.getYearName(this._dateAdapter.createDate(r,0,1));return[a,o]}_periodButtonLabelId=l(Re).getId("mat-calendar-period-label-");static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["mat-calendar-header"]],exportAs:["matCalendarHeader"],ngContentSelectors:Lb,decls:17,vars:13,consts:[[1,"mat-calendar-header"],[1,"mat-calendar-controls"],["aria-live","polite",1,"cdk-visually-hidden",3,"id"],["matButton","","type","button",1,"mat-calendar-period-button",3,"click"],["aria-hidden","true"],["viewBox","0 0 10 5","focusable","false","aria-hidden","true",1,"mat-calendar-arrow"],["points","0,0 5,5 10,0"],[1,"mat-calendar-spacer"],["matIconButton","","type","button","disabledInteractive","",1,"mat-calendar-previous-button",3,"click","disabled","matTooltip"],["viewBox","0 0 24 24","focusable","false","aria-hidden","true"],["d","M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"],["matIconButton","","type","button","disabledInteractive","",1,"mat-calendar-next-button",3,"click","disabled","matTooltip"],["d","M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"]],template:function(t,r){t&1&&(Ee(),p(0,"div",0)(1,"div",1)(2,"span",2),P(3),h(),p(4,"button",3),T("click",function(){return r.currentPeriodClicked()}),p(5,"span",4),P(6),h(),_t(),p(7,"svg",5),_(8,"polygon",6),h()(),er(),_(9,"div",7),Z(10),p(11,"button",8),T("click",function(){return r.previousClicked()}),_t(),p(12,"svg",9),_(13,"path",10),h()(),er(),p(14,"button",11),T("click",function(){return r.nextClicked()}),_t(),p(15,"svg",9),_(16,"path",12),h()()()()),t&2&&(c(2),f("id",r._periodButtonLabelId),c(),ce(r.periodButtonDescription),c(),B("aria-label",r.periodButtonLabel)("aria-describedby",r._periodButtonLabelId),c(2),ce(r.periodButtonText),c(),w("mat-calendar-invert",r.calendar.currentView!=="month"),c(4),f("disabled",!r.previousEnabled())("matTooltip",r.prevButtonLabel),B("aria-label",r.prevButtonLabel),c(3),f("disabled",!r.nextEnabled())("matTooltip",r.nextButtonLabel),B("aria-label",r.nextButtonLabel))},dependencies:[vs,Oa,bm],encapsulation:2,changeDetection:0})}return i})(),Cl=(()=>{class i{_dateAdapter=l(mt,{optional:!0});_dateFormats=l(Fi,{optional:!0});_changeDetectorRef=l(ne);_elementRef=l(N);headerComponent;_calendarHeaderPortal;_intlChanges;_moveFocusOnNextTick=!1;get startAt(){return this._startAt}set startAt(e){this._startAt=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e))}_startAt=null;startView="month";get selected(){return this._selected}set selected(e){e instanceof Dt?this._selected=e:this._selected=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e))}_selected=null;get minDate(){return this._minDate}set minDate(e){this._minDate=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e))}_minDate=null;get maxDate(){return this._maxDate}set maxDate(e){this._maxDate=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e))}_maxDate=null;dateFilter;dateClass;comparisonStart=null;comparisonEnd=null;startDateAccessibleName=null;endDateAccessibleName=null;selectedChange=new S;yearSelected=new S;monthSelected=new S;viewChanged=new S(!0);_userSelection=new S;_userDragDrop=new S;monthView;yearView;multiYearView;get activeDate(){return this._clampedActiveDate}set activeDate(e){this._clampedActiveDate=this._dateAdapter.clampDate(e,this.minDate,this.maxDate),this.stateChanges.next(),this._changeDetectorRef.markForCheck()}_clampedActiveDate;get currentView(){return this._currentView}set currentView(e){let t=this._currentView!==e?e:null;this._currentView=e,this._moveFocusOnNextTick=!0,this._changeDetectorRef.markForCheck(),t&&(this.stateChanges.next(),this.viewChanged.emit(t))}_currentView;_activeDrag=null;stateChanges=new F;constructor(){this._intlChanges=l(Vi).changes.subscribe(()=>{this._changeDetectorRef.markForCheck(),this.stateChanges.next()})}ngAfterContentInit(){this._calendarHeaderPortal=new cn(this.headerComponent||Im),this.activeDate=this.startAt||this._dateAdapter.today(),this._currentView=this.startView}ngAfterViewChecked(){this._moveFocusOnNextTick&&(this._moveFocusOnNextTick=!1,this.focusActiveCell())}ngOnDestroy(){this._intlChanges.unsubscribe(),this.stateChanges.complete()}ngOnChanges(e){let t=e.minDate&&!this._dateAdapter.sameDate(e.minDate.previousValue,e.minDate.currentValue)?e.minDate:void 0,r=e.maxDate&&!this._dateAdapter.sameDate(e.maxDate.previousValue,e.maxDate.currentValue)?e.maxDate:void 0,a=t||r||e.dateFilter;if(a&&!a.firstChange){let o=this._getCurrentViewComponent();o&&(this._elementRef.nativeElement.contains(zt())&&(this._moveFocusOnNextTick=!0),this._changeDetectorRef.detectChanges(),o._init())}this.stateChanges.next()}focusActiveCell(){this._getCurrentViewComponent()?._focusActiveCell(!1)}updateTodaysDate(){this._getCurrentViewComponent()?._init()}_dateSelected(e){let t=e.value;(this.selected instanceof Dt||t&&!this._dateAdapter.sameDate(t,this.selected))&&this.selectedChange.emit(t),this._userSelection.emit(e)}_yearSelectedInMultiYearView(e){this.yearSelected.emit(e)}_monthSelectedInYearView(e){this.monthSelected.emit(e)}_goToDateInView(e,t){this.activeDate=e,this.currentView=t}_dragStarted(e){this._activeDrag=e}_dragEnded(e){this._activeDrag&&(e.value&&this._userDragDrop.emit(e),this._activeDrag=null)}_getCurrentViewComponent(){return this.monthView||this.yearView||this.multiYearView}static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["mat-calendar"]],viewQuery:function(t,r){if(t&1&&ke(Cm,5)(Dm,5)(xm,5),t&2){let a;z(a=H())&&(r.monthView=a.first),z(a=H())&&(r.yearView=a.first),z(a=H())&&(r.multiYearView=a.first)}},hostAttrs:[1,"mat-calendar"],inputs:{headerComponent:"headerComponent",startAt:"startAt",startView:"startView",selected:"selected",minDate:"minDate",maxDate:"maxDate",dateFilter:"dateFilter",dateClass:"dateClass",comparisonStart:"comparisonStart",comparisonEnd:"comparisonEnd",startDateAccessibleName:"startDateAccessibleName",endDateAccessibleName:"endDateAccessibleName"},outputs:{selectedChange:"selectedChange",yearSelected:"yearSelected",monthSelected:"monthSelected",viewChanged:"viewChanged",_userSelection:"_userSelection",_userDragDrop:"_userDragDrop"},exportAs:["matCalendar"],features:[re([Em]),pe],decls:5,vars:2,consts:[[3,"cdkPortalOutlet"],["cdkMonitorSubtreeFocus","","tabindex","-1",1,"mat-calendar-content"],[3,"activeDate","selected","dateFilter","maxDate","minDate","dateClass","comparisonStart","comparisonEnd","startDateAccessibleName","endDateAccessibleName","activeDrag"],[3,"activeDate","selected","dateFilter","maxDate","minDate","dateClass"],[3,"activeDateChange","_userSelection","dragStarted","dragEnded","activeDate","selected","dateFilter","maxDate","minDate","dateClass","comparisonStart","comparisonEnd","startDateAccessibleName","endDateAccessibleName","activeDrag"],[3,"activeDateChange","monthSelected","selectedChange","activeDate","selected","dateFilter","maxDate","minDate","dateClass"],[3,"activeDateChange","yearSelected","selectedChange","activeDate","selected","dateFilter","maxDate","minDate","dateClass"]],template:function(t,r){if(t&1&&(Oe(0,Bb,0,0,"ng-template",0),p(1,"div",1),v(2,jb,1,11,"mat-month-view",2)(3,zb,1,6,"mat-year-view",3)(4,Hb,1,6,"mat-multi-year-view",3),h()),t&2){let a;f("cdkPortalOutlet",r._calendarHeaderPortal),c(2),y((a=r.currentView)==="month"?2:a==="year"?3:a==="multi-year"?4:-1)}},dependencies:[un,Jo,Cm,Dm,xm],styles:[`.mat-calendar {
  display: block;
  line-height: normal;
  font-family: var(--mat-datepicker-calendar-text-font, var(--mat-sys-body-medium-font));
  font-size: var(--mat-datepicker-calendar-text-size, var(--mat-sys-body-medium-size));
}

.mat-calendar-header {
  padding: 8px 8px 0 8px;
}

.mat-calendar-content {
  padding: 0 8px 8px 8px;
  outline: none;
}

.mat-calendar-controls {
  display: flex;
  align-items: center;
  margin: 5% calc(4.7142857143% - 16px);
}

.mat-calendar-spacer {
  flex: 1 1 auto;
}

.mat-calendar-period-button {
  min-width: 0;
  margin: 0 8px;
  font-size: var(--mat-datepicker-calendar-period-button-text-size, var(--mat-sys-title-small-size));
  font-weight: var(--mat-datepicker-calendar-period-button-text-weight, var(--mat-sys-title-small-weight));
  --mat-button-text-label-text-color: var(--mat-datepicker-calendar-period-button-text-color, var(--mat-sys-on-surface-variant));
}

.mat-calendar-arrow {
  display: inline-block;
  width: 10px;
  height: 5px;
  margin: 0 0 0 5px;
  vertical-align: middle;
  fill: var(--mat-datepicker-calendar-period-button-icon-color, var(--mat-sys-on-surface-variant));
}
.mat-calendar-arrow.mat-calendar-invert {
  transform: rotate(180deg);
}
[dir=rtl] .mat-calendar-arrow {
  margin: 0 5px 0 0;
}
@media (forced-colors: active) {
  .mat-calendar-arrow {
    fill: CanvasText;
  }
}

.mat-datepicker-content .mat-calendar-previous-button:not(.mat-mdc-button-disabled),
.mat-datepicker-content .mat-calendar-next-button:not(.mat-mdc-button-disabled) {
  color: var(--mat-datepicker-calendar-navigation-button-icon-color, var(--mat-sys-on-surface-variant));
}
[dir=rtl] .mat-calendar-previous-button,
[dir=rtl] .mat-calendar-next-button {
  transform: rotate(180deg);
}

.mat-calendar-table {
  border-spacing: 0;
  border-collapse: collapse;
  width: 100%;
}

.mat-calendar-table-header th {
  text-align: center;
  padding: 0 0 8px 0;
  color: var(--mat-datepicker-calendar-header-text-color, var(--mat-sys-on-surface-variant));
  font-size: var(--mat-datepicker-calendar-header-text-size, var(--mat-sys-title-small-size));
  font-weight: var(--mat-datepicker-calendar-header-text-weight, var(--mat-sys-title-small-weight));
}

.mat-calendar-table-header-divider {
  position: relative;
  height: 1px;
}
.mat-calendar-table-header-divider::after {
  content: "";
  position: absolute;
  top: 0;
  left: -8px;
  right: -8px;
  height: 1px;
  background: var(--mat-datepicker-calendar-header-divider-color, transparent);
}

.mat-calendar-body-cell-content::before {
  margin: calc(calc(var(--mat-focus-indicator-border-width, 3px) + 3px) * -1);
}

.mat-calendar-body-cell:focus-visible .mat-focus-indicator::before {
  content: "";
}
`],encapsulation:2,changeDetection:0})}return i})(),Jb=new M("mat-datepicker-scroll-strategy",{providedIn:"root",factory:()=>{let i=l(U);return()=>mn(i)}}),Om=(()=>{class i{_elementRef=l(N);_animationsDisabled=ye();_changeDetectorRef=l(ne);_globalModel=l(Lr);_dateAdapter=l(mt);_ngZone=l(X);_rangeSelectionStrategy=l(km,{optional:!0});_stateChanges;_model;_eventCleanups;_animationFallback;_calendar;color;datepicker;comparisonStart=null;comparisonEnd=null;startDateAccessibleName=null;endDateAccessibleName=null;_isAbove=!1;_animationDone=new F;_isAnimating=!1;_closeButtonText;_closeButtonFocused=!1;_actionsPortal=null;_dialogLabelId=null;constructor(){if(l(Ue).load(Ut),this._closeButtonText=l(Vi).closeCalendarLabel,!this._animationsDisabled){let e=this._elementRef.nativeElement,t=l(we);this._eventCleanups=this._ngZone.runOutsideAngular(()=>[t.listen(e,"animationstart",this._handleAnimationEvent),t.listen(e,"animationend",this._handleAnimationEvent),t.listen(e,"animationcancel",this._handleAnimationEvent)])}}ngAfterViewInit(){this._stateChanges=this.datepicker.stateChanges.subscribe(()=>{this._changeDetectorRef.markForCheck()}),this._calendar.focusActiveCell()}ngOnDestroy(){clearTimeout(this._animationFallback),this._eventCleanups?.forEach(e=>e()),this._stateChanges?.unsubscribe(),this._animationDone.complete()}_handleUserSelection(e){let t=this._model.selection,r=e.value,a=t instanceof Dt;if(a&&this._rangeSelectionStrategy){let o=this._rangeSelectionStrategy.selectionFinished(r,t,e.event);this._model.updateSelection(o,this)}else r&&(a||!this._dateAdapter.sameDate(r,t))&&this._model.add(r);(!this._model||this._model.isComplete())&&!this._actionsPortal&&this.datepicker.close()}_handleUserDragDrop(e){this._model.updateSelection(e.value,this)}_startExitAnimation(){this._elementRef.nativeElement.classList.add("mat-datepicker-content-exit"),this._animationsDisabled?this._animationDone.next():(clearTimeout(this._animationFallback),this._animationFallback=setTimeout(()=>{this._isAnimating||this._animationDone.next()},200))}_handleAnimationEvent=e=>{let t=this._elementRef.nativeElement;e.target!==t||!e.animationName.startsWith("_mat-datepicker-content")||(clearTimeout(this._animationFallback),this._isAnimating=e.type==="animationstart",t.classList.toggle("mat-datepicker-content-animating",this._isAnimating),this._isAnimating||this._animationDone.next())};_getSelected(){return this._model.selection}_applyPendingSelection(){this._model!==this._globalModel&&this._globalModel.updateSelection(this._model.selection,this)}_assignActions(e,t){this._model=e?this._globalModel.clone():this._globalModel,this._actionsPortal=e,t&&this._changeDetectorRef.detectChanges()}static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["mat-datepicker-content"]],viewQuery:function(t,r){if(t&1&&ke(Cl,5),t&2){let a;z(a=H())&&(r._calendar=a.first)}},hostAttrs:[1,"mat-datepicker-content"],hostVars:6,hostBindings:function(t,r){t&2&&(Ae(r.color?"mat-"+r.color:""),w("mat-datepicker-content-touch",r.datepicker.touchUi)("mat-datepicker-content-animations-enabled",!r._animationsDisabled))},inputs:{color:"color"},exportAs:["matDatepickerContent"],decls:5,vars:26,consts:[["cdkTrapFocus","","role","dialog",1,"mat-datepicker-content-container"],[3,"yearSelected","monthSelected","viewChanged","_userSelection","_userDragDrop","id","startAt","startView","minDate","maxDate","dateFilter","headerComponent","selected","dateClass","comparisonStart","comparisonEnd","startDateAccessibleName","endDateAccessibleName"],[3,"cdkPortalOutlet"],["type","button","matButton","elevated",1,"mat-datepicker-close-button",3,"focus","blur","click","color"]],template:function(t,r){t&1&&(p(0,"div",0)(1,"mat-calendar",1),T("yearSelected",function(o){return r.datepicker._selectYear(o)})("monthSelected",function(o){return r.datepicker._selectMonth(o)})("viewChanged",function(o){return r.datepicker._viewChanged(o)})("_userSelection",function(o){return r._handleUserSelection(o)})("_userDragDrop",function(o){return r._handleUserDragDrop(o)}),h(),Oe(2,Ub,0,0,"ng-template",2),p(3,"button",3),T("focus",function(){return r._closeButtonFocused=!0})("blur",function(){return r._closeButtonFocused=!1})("click",function(){return r.datepicker.close()}),P(4),h()()),t&2&&(w("mat-datepicker-content-container-with-custom-header",r.datepicker.calendarHeaderComponent)("mat-datepicker-content-container-with-actions",r._actionsPortal),B("aria-modal",!0)("aria-labelledby",r._dialogLabelId??void 0),c(),Ae(r.datepicker.panelClass),f("id",r.datepicker.id)("startAt",r.datepicker.startAt)("startView",r.datepicker.startView)("minDate",r.datepicker._getMinDate())("maxDate",r.datepicker._getMaxDate())("dateFilter",r.datepicker._getDateFilter())("headerComponent",r.datepicker.calendarHeaderComponent)("selected",r._getSelected())("dateClass",r.datepicker.dateClass)("comparisonStart",r.comparisonStart)("comparisonEnd",r.comparisonEnd)("startDateAccessibleName",r.startDateAccessibleName)("endDateAccessibleName",r.endDateAccessibleName),c(),f("cdkPortalOutlet",r._actionsPortal),c(),w("cdk-visually-hidden",!r._closeButtonFocused),f("color",r.color||"primary"),c(),ce(r._closeButtonText))},dependencies:[ns,Cl,un,vs],styles:[`@keyframes _mat-datepicker-content-dropdown-enter {
  from {
    opacity: 0;
    transform: scaleY(0.8);
  }
  to {
    opacity: 1;
    transform: none;
  }
}
@keyframes _mat-datepicker-content-dialog-enter {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: none;
  }
}
@keyframes _mat-datepicker-content-exit {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}
.mat-datepicker-content {
  display: block;
  background-color: var(--mat-datepicker-calendar-container-background-color, var(--mat-sys-surface-container-high));
  color: var(--mat-datepicker-calendar-container-text-color, var(--mat-sys-on-surface));
  box-shadow: var(--mat-datepicker-calendar-container-elevation-shadow, 0px 0px 0px 0px rgba(0, 0, 0, 0.2), 0px 0px 0px 0px rgba(0, 0, 0, 0.14), 0px 0px 0px 0px rgba(0, 0, 0, 0.12));
  border-radius: var(--mat-datepicker-calendar-container-shape, var(--mat-sys-corner-large));
}
.mat-datepicker-content.mat-datepicker-content-animations-enabled {
  animation: _mat-datepicker-content-dropdown-enter 120ms cubic-bezier(0, 0, 0.2, 1);
}
.mat-datepicker-content .mat-calendar {
  width: 296px;
  height: 354px;
}
.mat-datepicker-content .mat-datepicker-content-container-with-custom-header .mat-calendar {
  height: auto;
}
.mat-datepicker-content .mat-datepicker-close-button {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 8px;
}
.mat-datepicker-content-animating .mat-datepicker-content .mat-datepicker-close-button {
  display: none;
}

.mat-datepicker-content-container {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.mat-datepicker-content-touch {
  display: block;
  max-height: 80vh;
  box-shadow: var(--mat-datepicker-calendar-container-touch-elevation-shadow, 0px 0px 0px 0px rgba(0, 0, 0, 0.2), 0px 0px 0px 0px rgba(0, 0, 0, 0.14), 0px 0px 0px 0px rgba(0, 0, 0, 0.12));
  border-radius: var(--mat-datepicker-calendar-container-touch-shape, var(--mat-sys-corner-extra-large));
  position: relative;
  overflow: visible;
  min-height: fit-content;
}
.mat-datepicker-content-touch.mat-datepicker-content-animations-enabled {
  animation: _mat-datepicker-content-dialog-enter 150ms cubic-bezier(0, 0, 0.2, 1);
}
.mat-datepicker-content-touch .mat-datepicker-content-container {
  min-height: 312px;
  max-height: 788px;
  min-width: 250px;
  max-width: 750px;
}
.mat-datepicker-content-touch .mat-calendar {
  width: 100%;
  height: auto;
}

.mat-datepicker-content-exit.mat-datepicker-content-animations-enabled {
  animation: _mat-datepicker-content-exit 100ms linear;
}

@media all and (orientation: landscape) {
  .mat-datepicker-content-touch .mat-datepicker-content-container {
    width: 64vh;
    height: 80vh;
  }
}
@media all and (orientation: portrait) {
  .mat-datepicker-content-touch .mat-datepicker-content-container {
    width: 80vw;
    height: 100vw;
  }
  .mat-datepicker-content-touch .mat-datepicker-content-container-with-actions {
    height: 115vw;
  }
}
`],encapsulation:2,changeDetection:0})}return i})(),Mm=(()=>{class i{_injector=l(U);_viewContainerRef=l(Jn);_dateAdapter=l(mt,{optional:!0});_dir=l(Ze,{optional:!0});_model=l(Lr);_animationsDisabled=ye();_scrollStrategy=l(Jb);_inputStateChanges=oe.EMPTY;_document=l(_e);calendarHeaderComponent;get startAt(){return this._startAt||(this.datepickerInput?this.datepickerInput.getStartValue():null)}set startAt(e){this._startAt=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e))}_startAt=null;startView="month";get color(){return this._color||(this.datepickerInput?this.datepickerInput.getThemePalette():void 0)}set color(e){this._color=e}_color;touchUi=!1;get disabled(){return this._disabled===void 0&&this.datepickerInput?this.datepickerInput.disabled:!!this._disabled}set disabled(e){e!==this._disabled&&(this._disabled=e,this.stateChanges.next(void 0))}_disabled;xPosition="start";yPosition="below";restoreFocus=!0;yearSelected=new S;monthSelected=new S;viewChanged=new S(!0);dateClass;openedStream=new S;closedStream=new S;get panelClass(){return this._panelClass}set panelClass(e){this._panelClass=Bd(e)}_panelClass;get opened(){return this._opened}set opened(e){e?this.open():this.close()}_opened=!1;id=l(Re).getId("mat-datepicker-");_getMinDate(){return this.datepickerInput&&this.datepickerInput.min}_getMaxDate(){return this.datepickerInput&&this.datepickerInput.max}_getDateFilter(){return this.datepickerInput&&this.datepickerInput.dateFilter}_overlayRef=null;_componentRef=null;_focusedElementBeforeOpen=null;_backdropHarnessClass=`${this.id}-backdrop`;_actionsPortal=null;datepickerInput;stateChanges=new F;_changeDetectorRef=l(ne);constructor(){this._dateAdapter,this._model.selectionChanged.subscribe(()=>{this._changeDetectorRef.markForCheck()})}ngOnChanges(e){let t=e.xPosition||e.yPosition;if(t&&!t.firstChange&&this._overlayRef){let r=this._overlayRef.getConfig().positionStrategy;r instanceof Nd&&(this._setConnectedPositions(r),this.opened&&this._overlayRef.updatePosition())}this.stateChanges.next(void 0)}ngOnDestroy(){this._destroyOverlay(),this.close(),this._inputStateChanges.unsubscribe(),this.stateChanges.complete()}select(e){this._model.add(e)}_selectYear(e){this.yearSelected.emit(e)}_selectMonth(e){this.monthSelected.emit(e)}_viewChanged(e){this.viewChanged.emit(e)}registerInput(e){return this.datepickerInput,this._inputStateChanges.unsubscribe(),this.datepickerInput=e,this._inputStateChanges=e.stateChanges.subscribe(()=>this.stateChanges.next(void 0)),this._model}registerActions(e){this._actionsPortal,this._actionsPortal=e,this._componentRef?.instance._assignActions(e,!0)}removeActions(e){e===this._actionsPortal&&(this._actionsPortal=null,this._componentRef?.instance._assignActions(null,!0))}open(){this._opened||this.disabled||this._componentRef?.instance._isAnimating||(this.datepickerInput,this._focusedElementBeforeOpen=zt(),this._openOverlay(),this._opened=!0,this.openedStream.emit())}close(){if(!this._opened||this._componentRef?.instance._isAnimating)return;let e=this.restoreFocus&&this._focusedElementBeforeOpen&&typeof this._focusedElementBeforeOpen.focus=="function",t=()=>{this._opened&&(this._opened=!1,this.closedStream.emit())};if(this._componentRef){let{instance:r,location:a}=this._componentRef;r._animationDone.pipe(Ne(1)).subscribe(()=>{let o=this._document.activeElement;e&&(!o||o===this._document.activeElement||a.nativeElement.contains(o))&&this._focusedElementBeforeOpen.focus(),this._focusedElementBeforeOpen=null,this._destroyOverlay()}),r._startExitAnimation()}e?setTimeout(t):t()}_applyPendingSelection(){this._componentRef?.instance?._applyPendingSelection()}_forwardContentValues(e){e.datepicker=this,e.color=this.color,e._dialogLabelId=this.datepickerInput.getOverlayLabelId(),e._assignActions(this._actionsPortal,!1)}_openOverlay(){this._destroyOverlay();let e=this.touchUi,t=new cn(Om,this._viewContainerRef),r=this._overlayRef=pn(this._injector,new si({positionStrategy:e?this._getDialogStrategy():this._getDropdownStrategy(),hasBackdrop:!0,backdropClass:[e?"cdk-overlay-dark-backdrop":"mat-overlay-transparent-backdrop",this._backdropHarnessClass],direction:this._dir||"ltr",scrollStrategy:e?oi(this._injector):this._scrollStrategy(),panelClass:`mat-datepicker-${e?"dialog":"popup"}`,disableAnimations:this._animationsDisabled}));this._getCloseStream(r).subscribe(a=>{a&&a.preventDefault(),this.close()}),r.keydownEvents().subscribe(a=>{let o=a.keyCode;(o===38||o===40||o===37||o===39||o===33||o===34)&&a.preventDefault()}),this._componentRef=r.attach(t),this._forwardContentValues(this._componentRef.instance),e||rt(()=>{r.updatePosition()},{injector:this._injector})}_destroyOverlay(){this._overlayRef&&(this._overlayRef.dispose(),this._overlayRef=this._componentRef=null)}_getDialogStrategy(){return di(this._injector).centerHorizontally().centerVertically()}_getDropdownStrategy(){let e=li(this._injector,this.datepickerInput.getConnectedOverlayOrigin()).withTransformOriginOn(".mat-datepicker-content").withFlexibleDimensions(!1).withViewportMargin(8).withLockedPosition();return this._setConnectedPositions(e)}_setConnectedPositions(e){let t=this.xPosition==="end"?"end":"start",r=t==="start"?"end":"start",a=this.yPosition==="above"?"bottom":"top",o=a==="top"?"bottom":"top";return e.withPositions([{originX:t,originY:o,overlayX:t,overlayY:a},{originX:t,originY:a,overlayX:t,overlayY:o},{originX:r,originY:o,overlayX:r,overlayY:a},{originX:r,originY:a,overlayX:r,overlayY:o}])}_getCloseStream(e){let t=["ctrlKey","shiftKey","metaKey"];return $e(e.backdropClick(),e.detachments(),e.keydownEvents().pipe(De(r=>r.keyCode===27&&!xe(r)||this.datepickerInput&&xe(r,"altKey")&&r.keyCode===38&&t.every(a=>!xe(r,a)))))}static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,inputs:{calendarHeaderComponent:"calendarHeaderComponent",startAt:"startAt",startView:"startView",color:"color",touchUi:[2,"touchUi","touchUi",Y],disabled:[2,"disabled","disabled",Y],xPosition:"xPosition",yPosition:"yPosition",restoreFocus:[2,"restoreFocus","restoreFocus",Y],dateClass:"dateClass",panelClass:"panelClass",opened:[2,"opened","opened",Y]},outputs:{yearSelected:"yearSelected",monthSelected:"monthSelected",viewChanged:"viewChanged",openedStream:"opened",closedStream:"closed"},features:[pe]})}return i})(),Tm=(()=>{class i extends Mm{static \u0275fac=(()=>{let e;return function(r){return(e||(e=bt(i)))(r||i)}})();static \u0275cmp=x({type:i,selectors:[["mat-datepicker"]],exportAs:["matDatepicker"],features:[re([Em,{provide:Mm,useExisting:i}]),te],decls:0,vars:0,template:function(t,r){},encapsulation:2,changeDetection:0})}return i})(),Pi=class{target;targetElement;value=null;constructor(n,e){this.target=n,this.targetElement=e,this.value=this.target.value}},ev=(()=>{class i{_elementRef=l(N);_dateAdapter=l(mt,{optional:!0});_dateFormats=l(Fi,{optional:!0});_isInitialized=!1;get value(){return this._model?this._getValueFromModel(this._model.selection):this._pendingValue}set value(e){this._assignValueProgrammatically(e,!0)}_model;get disabled(){return!!this._disabled||this._parentDisabled()}set disabled(e){let t=e,r=this._elementRef.nativeElement;this._disabled!==t&&(this._disabled=t,this.stateChanges.next(void 0)),t&&this._isInitialized&&r.blur&&r.blur()}_disabled;dateChange=new S;dateInput=new S;stateChanges=new F;_onTouched=()=>{};_validatorOnChange=()=>{};_cvaOnChange=()=>{};_valueChangesSubscription=oe.EMPTY;_localeSubscription=oe.EMPTY;_pendingValue=null;_parseValidator=()=>this._lastValueValid?null:{matDatepickerParse:{text:this._elementRef.nativeElement.value}};_filterValidator=e=>{let t=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e.value));return!t||this._matchesFilter(t)?null:{matDatepickerFilter:!0}};_minValidator=e=>{let t=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e.value)),r=this._getMinDate();return!r||!t||this._dateAdapter.compareDate(r,t)<=0?null:{matDatepickerMin:{min:r,actual:t}}};_maxValidator=e=>{let t=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e.value)),r=this._getMaxDate();return!r||!t||this._dateAdapter.compareDate(r,t)>=0?null:{matDatepickerMax:{max:r,actual:t}}};_getValidators(){return[this._parseValidator,this._minValidator,this._maxValidator,this._filterValidator]}_registerModel(e){this._model=e,this._valueChangesSubscription.unsubscribe(),this._pendingValue&&this._assignValue(this._pendingValue),this._valueChangesSubscription=this._model.selectionChanged.subscribe(t=>{if(this._shouldHandleChangeEvent(t)){let r=this._getValueFromModel(t.selection);this._lastValueValid=this._isValidValue(r),this._cvaOnChange(r),this._onTouched(),this._formatValue(r),this.dateInput.emit(new Pi(this,this._elementRef.nativeElement)),this.dateChange.emit(new Pi(this,this._elementRef.nativeElement))}})}_lastValueValid=!1;constructor(){this._localeSubscription=this._dateAdapter.localeChanges.subscribe(()=>{this._assignValueProgrammatically(this.value,!0)})}ngAfterViewInit(){this._isInitialized=!0}ngOnChanges(e){tv(e,this._dateAdapter)&&this.stateChanges.next(void 0)}ngOnDestroy(){this._valueChangesSubscription.unsubscribe(),this._localeSubscription.unsubscribe(),this.stateChanges.complete()}registerOnValidatorChange(e){this._validatorOnChange=e}validate(e){return this._validator?this._validator(e):null}writeValue(e){this._assignValueProgrammatically(e,e!==this.value)}registerOnChange(e){this._cvaOnChange=e}registerOnTouched(e){this._onTouched=e}setDisabledState(e){this.disabled=e}_onKeydown(e){let t=["ctrlKey","shiftKey","metaKey"];xe(e,"altKey")&&e.keyCode===40&&t.every(a=>!xe(e,a))&&!this._elementRef.nativeElement.readOnly&&(this._openPopup(),e.preventDefault())}_onInput(e){let t=e.target.value,r=this._lastValueValid,a=this._dateAdapter.parse(t,this._dateFormats.parse.dateInput);this._lastValueValid=this._isValidValue(a),a=this._dateAdapter.getValidDateOrNull(a);let o=!this._dateAdapter.sameDate(a,this.value);!a||o?this._cvaOnChange(a):(t&&!this.value&&this._cvaOnChange(a),r!==this._lastValueValid&&this._validatorOnChange()),o&&(this._assignValue(a),this.dateInput.emit(new Pi(this,this._elementRef.nativeElement)))}_onChange(){this.dateChange.emit(new Pi(this,this._elementRef.nativeElement))}_onBlur(){this.value&&this._formatValue(this.value),this._onTouched()}_formatValue(e){this._elementRef.nativeElement.value=e!=null?this._dateAdapter.format(e,this._dateFormats.display.dateInput):""}_assignValue(e){this._model?(this._assignValueToModel(e),this._pendingValue=null):this._pendingValue=e}_isValidValue(e){return!e||this._dateAdapter.isValid(e)}_parentDisabled(){return!1}_assignValueProgrammatically(e,t){e=this._dateAdapter.deserialize(e),this._lastValueValid=this._isValidValue(e),e=this._dateAdapter.getValidDateOrNull(e),this._assignValue(e),t&&this._formatValue(e)}_matchesFilter(e){let t=this._getDateFilter();return!t||t(e)}static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,inputs:{value:"value",disabled:[2,"disabled","disabled",Y]},outputs:{dateChange:"dateChange",dateInput:"dateInput"},features:[pe]})}return i})();function tv(i,n){let e=Object.keys(i);for(let t of e){let{previousValue:r,currentValue:a}=i[t];if(n.isDateInstance(r)&&n.isDateInstance(a)){if(!n.sameDate(r,a))return!0}else return!0}return!1}var nv={provide:dt,useExisting:qe(()=>go),multi:!0},iv={provide:Jt,useExisting:qe(()=>go),multi:!0},go=(()=>{class i extends ev{_formField=l(Er,{optional:!0});_closedSubscription=oe.EMPTY;_openedSubscription=oe.EMPTY;set matDatepicker(e){e&&(this._datepicker=e,this._ariaOwns.set(e.opened?e.id:null),this._closedSubscription=e.closedStream.subscribe(()=>{this._onTouched(),this._ariaOwns.set(null)}),this._openedSubscription=e.openedStream.subscribe(()=>{this._ariaOwns.set(e.id)}),this._registerModel(e.registerInput(this)))}_datepicker;_ariaOwns=V(null);get min(){return this._min}set min(e){let t=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e));this._dateAdapter.sameDate(t,this._min)||(this._min=t,this._validatorOnChange())}_min=null;get max(){return this._max}set max(e){let t=this._dateAdapter.getValidDateOrNull(this._dateAdapter.deserialize(e));this._dateAdapter.sameDate(t,this._max)||(this._max=t,this._validatorOnChange())}_max=null;get dateFilter(){return this._dateFilter}set dateFilter(e){let t=this._matchesFilter(this.value);this._dateFilter=e,this._matchesFilter(this.value)!==t&&this._validatorOnChange()}_dateFilter;_validator=null;constructor(){super(),this._validator=de.compose(super._getValidators())}getConnectedOverlayOrigin(){return this._formField?this._formField.getConnectedOverlayOrigin():this._elementRef}getOverlayLabelId(){return this._formField?this._formField.getLabelId():this._elementRef.nativeElement.getAttribute("aria-labelledby")}getThemePalette(){return this._formField?this._formField.color:void 0}getStartValue(){return this.value}ngOnDestroy(){super.ngOnDestroy(),this._closedSubscription.unsubscribe(),this._openedSubscription.unsubscribe()}_openPopup(){this._datepicker&&this._datepicker.open()}_getValueFromModel(e){return e}_assignValueToModel(e){this._model&&this._model.updateSelection(e,this)}_getMinDate(){return this._min}_getMaxDate(){return this._max}_getDateFilter(){return this._dateFilter}_shouldHandleChangeEvent(e){return e.source!==this}static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,selectors:[["input","matDatepicker",""]],hostAttrs:[1,"mat-datepicker-input"],hostVars:6,hostBindings:function(t,r){t&1&&T("input",function(o){return r._onInput(o)})("change",function(){return r._onChange()})("blur",function(){return r._onBlur()})("keydown",function(o){return r._onKeydown(o)}),t&2&&(He("disabled",r.disabled),B("aria-haspopup",r._datepicker?"dialog":null)("aria-owns",r._ariaOwns())("min",r.min?r._dateAdapter.toIso8601(r.min):null)("max",r.max?r._dateAdapter.toIso8601(r.max):null)("data-mat-calendar",r._datepicker?r._datepicker.id:null))},inputs:{matDatepicker:"matDatepicker",min:"min",max:"max",dateFilter:[0,"matDatepickerFilter","dateFilter"]},exportAs:["matDatepickerInput"],features:[re([nv,iv,{provide:im,useExisting:i}]),te]})}return i})(),xl=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275dir=E({type:i,selectors:[["","matDatepickerToggleIcon",""]]})}return i})(),Dl=(()=>{class i{_intl=l(Vi);_changeDetectorRef=l(ne);_stateChanges=oe.EMPTY;datepicker;tabIndex=null;ariaLabel;get disabled(){return this._disabled===void 0&&this.datepicker?this.datepicker.disabled:!!this._disabled}set disabled(e){this._disabled=e}_disabled;disableRipple=!1;_customIcon;_button;constructor(){let e=l(new ii("tabindex"),{optional:!0}),t=Number(e);this.tabIndex=t||t===0?t:null}ngOnChanges(e){e.datepicker&&this._watchStateChanges()}ngOnDestroy(){this._stateChanges.unsubscribe()}ngAfterContentInit(){this._watchStateChanges()}_open(e){this.datepicker&&!this.disabled&&(this.datepicker.open(),e.stopPropagation())}_watchStateChanges(){let e=this.datepicker?this.datepicker.stateChanges:Ie(),t=this.datepicker&&this.datepicker.datepickerInput?this.datepicker.datepickerInput.stateChanges:Ie(),r=this.datepicker?$e(this.datepicker.openedStream,this.datepicker.closedStream):Ie();this._stateChanges.unsubscribe(),this._stateChanges=$e(this._intl.changes,e,t,r).subscribe(()=>this._changeDetectorRef.markForCheck())}static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["mat-datepicker-toggle"]],contentQueries:function(t,r,a){if(t&1&&sn(a,xl,5),t&2){let o;z(o=H())&&(r._customIcon=o.first)}},viewQuery:function(t,r){if(t&1&&ke(Wb,5),t&2){let a;z(a=H())&&(r._button=a.first)}},hostAttrs:[1,"mat-datepicker-toggle"],hostVars:8,hostBindings:function(t,r){t&1&&T("click",function(o){return r._open(o)}),t&2&&(B("tabindex",null)("data-mat-calendar",r.datepicker?r.datepicker.id:null),w("mat-datepicker-toggle-active",r.datepicker&&r.datepicker.opened)("mat-accent",r.datepicker&&r.datepicker.color==="accent")("mat-warn",r.datepicker&&r.datepicker.color==="warn"))},inputs:{datepicker:[0,"for","datepicker"],tabIndex:"tabIndex",ariaLabel:[0,"aria-label","ariaLabel"],disabled:[2,"disabled","disabled",Y],disableRipple:"disableRipple"},exportAs:["matDatepickerToggle"],features:[pe],ngContentSelectors:qb,decls:4,vars:7,consts:[["button",""],["matIconButton","","type","button",3,"tabIndex","disabled","disableRipple"],["viewBox","0 0 24 24","width","24px","height","24px","fill","currentColor","focusable","false","aria-hidden","true",1,"mat-datepicker-toggle-default-icon"],["d","M19 3h-1V1h-2v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM7 10h5v5H7z"]],template:function(t,r){t&1&&(Ee(Yb),p(0,"button",1,0),v(2,Gb,2,0,":svg:svg",2),Z(3),h()),t&2&&(f("tabIndex",r.disabled?-1:r.tabIndex)("disabled",r.disabled)("disableRipple",r.disableRipple),B("aria-haspopup",r.datepicker?"dialog":null)("aria-label",r.ariaLabel||r._intl.openCalendarLabel)("aria-expanded",r.datepicker?r.datepicker.opened:null),c(2),y(r._customIcon?-1:2))},dependencies:[Oa],styles:[`.mat-datepicker-toggle {
  pointer-events: auto;
  color: var(--mat-datepicker-toggle-icon-color, var(--mat-sys-on-surface-variant));
}
.mat-datepicker-toggle button {
  color: inherit;
}

.mat-datepicker-toggle-active {
  color: var(--mat-datepicker-toggle-active-state-icon-color, var(--mat-sys-primary));
}

@media (forced-colors: active) {
  .mat-datepicker-toggle-default-icon {
    color: CanvasText;
  }
}
`],encapsulation:2,changeDetection:0})}return i})();var Fm=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275mod=$({type:i});static \u0275inj=G({providers:[Vi],imports:[Ta,Ht,or,Rn,Om,Dl,Im,fe,ai]})}return i})();var nn=class extends Error{},Fl=class extends nn{constructor(n){super(`Invalid DateTime: ${n.toMessage()}`)}},Pl=class extends nn{constructor(n){super(`Invalid Interval: ${n.toMessage()}`)}},Rl=class extends nn{constructor(n){super(`Invalid Duration: ${n.toMessage()}`)}},kn=class extends nn{},Mo=class extends nn{constructor(n){super(`Invalid unit ${n}`)}},We=class extends nn{},$t=class extends nn{constructor(){super("Zone is an abstract class")}},D="numeric",Nt="short",gt="long",wo={year:D,month:D,day:D},cp={year:D,month:Nt,day:D},av={year:D,month:Nt,day:D,weekday:Nt},up={year:D,month:gt,day:D},mp={year:D,month:gt,day:D,weekday:gt},pp={hour:D,minute:D},hp={hour:D,minute:D,second:D},fp={hour:D,minute:D,second:D,timeZoneName:Nt},gp={hour:D,minute:D,second:D,timeZoneName:gt},_p={hour:D,minute:D,hourCycle:"h23"},bp={hour:D,minute:D,second:D,hourCycle:"h23"},vp={hour:D,minute:D,second:D,hourCycle:"h23",timeZoneName:Nt},yp={hour:D,minute:D,second:D,hourCycle:"h23",timeZoneName:gt},Cp={year:D,month:D,day:D,hour:D,minute:D},xp={year:D,month:D,day:D,hour:D,minute:D,second:D},Dp={year:D,month:Nt,day:D,hour:D,minute:D},Mp={year:D,month:Nt,day:D,hour:D,minute:D,second:D},ov={year:D,month:Nt,day:D,weekday:Nt,hour:D,minute:D},wp={year:D,month:gt,day:D,hour:D,minute:D,timeZoneName:Nt},Ep={year:D,month:gt,day:D,hour:D,minute:D,second:D,timeZoneName:Nt},kp={year:D,month:gt,day:D,weekday:gt,hour:D,minute:D,timeZoneName:gt},Ap={year:D,month:gt,day:D,weekday:gt,hour:D,minute:D,second:D,timeZoneName:gt},$n=class{get type(){throw new $t}get name(){throw new $t}get ianaName(){return this.name}get isUniversal(){throw new $t}offsetName(n,e){throw new $t}formatOffset(n,e){throw new $t}offset(n){throw new $t}equals(n){throw new $t}get isValid(){throw new $t}},Ml=null,Eo=class i extends $n{static get instance(){return Ml===null&&(Ml=new i),Ml}get type(){return"system"}get name(){return new Intl.DateTimeFormat().resolvedOptions().timeZone}get isUniversal(){return!1}offsetName(n,{format:e,locale:t}){return Bp(n,e,t)}formatOffset(n,e){return Ur(this.offset(n),e)}offset(n){return-new Date(n).getTimezoneOffset()}equals(n){return n.type==="system"}get isValid(){return!0}},Vl=new Map;function sv(i){let n=Vl.get(i);return n===void 0&&(n=new Intl.DateTimeFormat("en-US",{hour12:!1,timeZone:i,year:"numeric",month:"2-digit",day:"2-digit",hour:"2-digit",minute:"2-digit",second:"2-digit",era:"short"}),Vl.set(i,n)),n}var lv={year:0,month:1,day:2,era:3,hour:4,minute:5,second:6};function dv(i,n){let e=i.format(n).replace(/\u200E/g,""),t=/(\d+)\/(\d+)\/(\d+) (AD|BC),? (\d+):(\d+):(\d+)/.exec(e),[,r,a,o,s,d,u,b]=t;return[o,r,a,s,d,u,b]}function cv(i,n){let e=i.formatToParts(n),t=[];for(let r=0;r<e.length;r++){let{type:a,value:o}=e[r],s=lv[a];a==="era"?t[s]=o:j(s)||(t[s]=parseInt(o,10))}return t}var wl=new Map,Sn=class i extends $n{static create(n){let e=wl.get(n);return e===void 0&&wl.set(n,e=new i(n)),e}static resetCache(){wl.clear(),Vl.clear()}static isValidSpecifier(n){return this.isValidZone(n)}static isValidZone(n){if(!n)return!1;try{return new Intl.DateTimeFormat("en-US",{timeZone:n}).format(),!0}catch{return!1}}constructor(n){super(),this.zoneName=n,this.valid=i.isValidZone(n)}get type(){return"iana"}get name(){return this.zoneName}get isUniversal(){return!1}offsetName(n,{format:e,locale:t}){return Bp(n,e,t,this.name)}formatOffset(n,e){return Ur(this.offset(n),e)}offset(n){if(!this.valid)return NaN;let e=new Date(n);if(isNaN(e))return NaN;let t=sv(this.name),[r,a,o,s,d,u,b]=t.formatToParts?cv(t,e):dv(t,e);s==="BC"&&(r=-Math.abs(r)+1);let Q=To({year:r,month:a,day:o,hour:d===24?0:d,minute:u,second:b,millisecond:0}),I=+e,me=I%1e3;return I-=me>=0?me:1e3+me,(Q-I)/(60*1e3)}equals(n){return n.type==="iana"&&n.name===this.name}get isValid(){return this.valid}},Pm={};function uv(i,n={}){let e=JSON.stringify([i,n]),t=Pm[e];return t||(t=new Intl.ListFormat(i,n),Pm[e]=t),t}var Nl=new Map;function Ll(i,n={}){let e=JSON.stringify([i,n]),t=Nl.get(e);return t===void 0&&(t=new Intl.DateTimeFormat(i,n),Nl.set(e,t)),t}var Bl=new Map;function mv(i,n={}){let e=JSON.stringify([i,n]),t=Bl.get(e);return t===void 0&&(t=new Intl.NumberFormat(i,n),Bl.set(e,t)),t}var jl=new Map;function pv(i,n={}){let o=n,{base:e}=o,t=Xi(o,["base"]),r=JSON.stringify([i,t]),a=jl.get(r);return a===void 0&&(a=new Intl.RelativeTimeFormat(i,n),jl.set(r,a)),a}var jr=null;function hv(){return jr||(jr=new Intl.DateTimeFormat().resolvedOptions().locale,jr)}var zl=new Map;function Sp(i){let n=zl.get(i);return n===void 0&&(n=new Intl.DateTimeFormat(i).resolvedOptions(),zl.set(i,n)),n}var Hl=new Map;function fv(i){let n=Hl.get(i);if(!n){let e=new Intl.Locale(i);n="getWeekInfo"in e?e.getWeekInfo():e.weekInfo,"minimalDays"in n||(n=g(g({},Ip),n)),Hl.set(i,n)}return n}function gv(i){let n=i.indexOf("-x-");n!==-1&&(i=i.substring(0,n));let e=i.indexOf("-u-");if(e===-1)return[i];{let t,r;try{t=Ll(i).resolvedOptions(),r=i}catch{let d=i.substring(0,e);t=Ll(d).resolvedOptions(),r=d}let{numberingSystem:a,calendar:o}=t;return[r,a,o]}}function _v(i,n,e){return(e||n)&&(i.includes("-u-")||(i+="-u"),e&&(i+=`-ca-${e}`),n&&(i+=`-nu-${n}`)),i}function bv(i){let n=[];for(let e=1;e<=12;e++){let t=se.utc(2009,e,1);n.push(i(t))}return n}function vv(i){let n=[];for(let e=1;e<=7;e++){let t=se.utc(2016,11,13+e);n.push(i(t))}return n}function _o(i,n,e,t){let r=i.listingMode();return r==="error"?null:r==="en"?e(n):t(n)}function yv(i){return i.numberingSystem&&i.numberingSystem!=="latn"?!1:i.numberingSystem==="latn"||!i.locale||i.locale.startsWith("en")||Sp(i.locale).numberingSystem==="latn"}var Ul=class{constructor(n,e,t){this.padTo=t.padTo||0,this.floor=t.floor||!1;let s=t,{padTo:r,floor:a}=s,o=Xi(s,["padTo","floor"]);if(!e||Object.keys(o).length>0){let d=g({useGrouping:!1},t);t.padTo>0&&(d.minimumIntegerDigits=t.padTo),this.inf=mv(n,d)}}format(n){if(this.inf){let e=this.floor?Math.floor(n):n;return this.inf.format(e)}else{let e=this.floor?Math.floor(n):td(n,3);return Ve(e,this.padTo)}}},Wl=class{constructor(n,e,t){this.opts=t,this.originalZone=void 0;let r;if(this.opts.timeZone)this.dt=n;else if(n.zone.type==="fixed"){let o=-1*(n.offset/60),s=o>=0?`Etc/GMT+${o}`:`Etc/GMT${o}`;n.offset!==0&&Sn.create(s).valid?(r=s,this.dt=n):(r="UTC",this.dt=n.offset===0?n:n.setZone("UTC").plus({minutes:n.offset}),this.originalZone=n.zone)}else n.zone.type==="system"?this.dt=n:n.zone.type==="iana"?(this.dt=n,r=n.zone.name):(r="UTC",this.dt=n.setZone("UTC").plus({minutes:n.offset}),this.originalZone=n.zone);let a=g({},this.opts);a.timeZone=a.timeZone||r,this.dtf=Ll(e,a)}format(){return this.originalZone?this.formatToParts().map(({value:n})=>n).join(""):this.dtf.format(this.dt.toJSDate())}formatToParts(){let n=this.dtf.formatToParts(this.dt.toJSDate());return this.originalZone?n.map(e=>{if(e.type==="timeZoneName"){let t=this.originalZone.offsetName(this.dt.ts,{locale:this.dt.locale,format:this.opts.timeZoneName});return q(g({},e),{value:t})}else return e}):n}resolvedOptions(){return this.dtf.resolvedOptions()}},Yl=class{constructor(n,e,t){this.opts=g({style:"long"},t),!e&&Np()&&(this.rtf=pv(n,t))}format(n,e){return this.rtf?this.rtf.format(n,e):zv(e,n,this.opts.numeric,this.opts.style!=="long")}formatToParts(n,e){return this.rtf?this.rtf.formatToParts(n,e):[]}},Ip={firstDay:1,minimalDays:4,weekend:[6,7]},ue=class i{static fromOpts(n){return i.create(n.locale,n.numberingSystem,n.outputCalendar,n.weekSettings,n.defaultToEN)}static create(n,e,t,r,a=!1){let o=n||Ce.defaultLocale,s=o||(a?"en-US":hv()),d=e||Ce.defaultNumberingSystem,u=t||Ce.defaultOutputCalendar,b=$l(r)||Ce.defaultWeekSettings;return new i(s,d,u,b,o)}static resetCache(){jr=null,Nl.clear(),Bl.clear(),jl.clear(),zl.clear(),Hl.clear()}static fromObject({locale:n,numberingSystem:e,outputCalendar:t,weekSettings:r}={}){return i.create(n,e,t,r)}constructor(n,e,t,r,a){let[o,s,d]=gv(n);this.locale=o,this.numberingSystem=e||s||null,this.outputCalendar=t||d||null,this.weekSettings=r,this.intl=_v(this.locale,this.numberingSystem,this.outputCalendar),this.weekdaysCache={format:{},standalone:{}},this.monthsCache={format:{},standalone:{}},this.meridiemCache=null,this.eraCache={},this.specifiedLocale=a,this.fastNumbersCached=null}get fastNumbers(){return this.fastNumbersCached==null&&(this.fastNumbersCached=yv(this)),this.fastNumbersCached}listingMode(){let n=this.isEnglish(),e=(this.numberingSystem===null||this.numberingSystem==="latn")&&(this.outputCalendar===null||this.outputCalendar==="gregory");return n&&e?"en":"intl"}clone(n){return!n||Object.getOwnPropertyNames(n).length===0?this:i.create(n.locale||this.specifiedLocale,n.numberingSystem||this.numberingSystem,n.outputCalendar||this.outputCalendar,$l(n.weekSettings)||this.weekSettings,n.defaultToEN||!1)}redefaultToEN(n={}){return this.clone(q(g({},n),{defaultToEN:!0}))}redefaultToSystem(n={}){return this.clone(q(g({},n),{defaultToEN:!1}))}months(n,e=!1){return _o(this,n,Hp,()=>{let t=this.intl==="ja"||this.intl.startsWith("ja-");e&=!t;let r=e?{month:n,day:"numeric"}:{month:n},a=e?"format":"standalone";if(!this.monthsCache[a][n]){let o=t?s=>this.dtFormatter(s,r).format():s=>this.extract(s,r,"month");this.monthsCache[a][n]=bv(o)}return this.monthsCache[a][n]})}weekdays(n,e=!1){return _o(this,n,Yp,()=>{let t=e?{weekday:n,year:"numeric",month:"long",day:"numeric"}:{weekday:n},r=e?"format":"standalone";return this.weekdaysCache[r][n]||(this.weekdaysCache[r][n]=vv(a=>this.extract(a,t,"weekday"))),this.weekdaysCache[r][n]})}meridiems(){return _o(this,void 0,()=>qp,()=>{if(!this.meridiemCache){let n={hour:"numeric",hourCycle:"h12"};this.meridiemCache=[se.utc(2016,11,13,9),se.utc(2016,11,13,19)].map(e=>this.extract(e,n,"dayperiod"))}return this.meridiemCache})}eras(n){return _o(this,n,Gp,()=>{let e={era:n};return this.eraCache[n]||(this.eraCache[n]=[se.utc(-40,1,1),se.utc(2017,1,1)].map(t=>this.extract(t,e,"era"))),this.eraCache[n]})}extract(n,e,t){let r=this.dtFormatter(n,e),a=r.formatToParts(),o=a.find(s=>s.type.toLowerCase()===t);return o?o.value:null}numberFormatter(n={}){return new Ul(this.intl,n.forceSimple||this.fastNumbers,n)}dtFormatter(n,e={}){return new Wl(n,this.intl,e)}relFormatter(n={}){return new Yl(this.intl,this.isEnglish(),n)}listFormatter(n={}){return uv(this.intl,n)}isEnglish(){return this.locale==="en"||this.locale.toLowerCase()==="en-us"||Sp(this.intl).locale.startsWith("en-us")}getWeekSettings(){return this.weekSettings?this.weekSettings:Lp()?fv(this.locale):Ip}getStartOfWeek(){return this.getWeekSettings().firstDay}getMinDaysInFirstWeek(){return this.getWeekSettings().minimalDays}getWeekendDays(){return this.getWeekSettings().weekend}equals(n){return this.locale===n.locale&&this.numberingSystem===n.numberingSystem&&this.outputCalendar===n.outputCalendar}toString(){return`Locale(${this.locale}, ${this.numberingSystem}, ${this.outputCalendar})`}},El=null,kt=class i extends $n{static get utcInstance(){return El===null&&(El=new i(0)),El}static instance(n){return n===0?i.utcInstance:new i(n)}static parseSpecifier(n){if(n){let e=n.match(/^utc(?:([+-]\d{1,2})(?::(\d{2}))?)?$/i);if(e)return new i(Fo(e[1],e[2]))}return null}constructor(n){super(),this.fixed=n}get type(){return"fixed"}get name(){return this.fixed===0?"UTC":`UTC${Ur(this.fixed,"narrow")}`}get ianaName(){return this.fixed===0?"Etc/UTC":`Etc/GMT${Ur(-this.fixed,"narrow")}`}offsetName(){return this.name}formatOffset(n,e){return Ur(this.fixed,e)}get isUniversal(){return!0}offset(){return this.fixed}equals(n){return n.type==="fixed"&&n.fixed===this.fixed}get isValid(){return!0}},ql=class extends $n{constructor(n){super(),this.zoneName=n}get type(){return"invalid"}get name(){return this.zoneName}get isUniversal(){return!1}offsetName(){return null}formatOffset(){return""}offset(){return NaN}equals(){return!1}get isValid(){return!1}};function En(i,n){if(j(i)||i===null)return n;if(i instanceof $n)return i;if(Ev(i)){let e=i.toLowerCase();return e==="default"?n:e==="local"||e==="system"?Eo.instance:e==="utc"||e==="gmt"?kt.utcInstance:kt.parseSpecifier(e)||Sn.create(i)}else return An(i)?kt.instance(i):typeof i=="object"&&"offset"in i&&typeof i.offset=="function"?i:new ql(i)}var Ql={arab:"[\u0660-\u0669]",arabext:"[\u06F0-\u06F9]",bali:"[\u1B50-\u1B59]",beng:"[\u09E6-\u09EF]",deva:"[\u0966-\u096F]",fullwide:"[\uFF10-\uFF19]",gujr:"[\u0AE6-\u0AEF]",hanidec:"[\u3007|\u4E00|\u4E8C|\u4E09|\u56DB|\u4E94|\u516D|\u4E03|\u516B|\u4E5D]",khmr:"[\u17E0-\u17E9]",knda:"[\u0CE6-\u0CEF]",laoo:"[\u0ED0-\u0ED9]",limb:"[\u1946-\u194F]",mlym:"[\u0D66-\u0D6F]",mong:"[\u1810-\u1819]",mymr:"[\u1040-\u1049]",orya:"[\u0B66-\u0B6F]",tamldec:"[\u0BE6-\u0BEF]",telu:"[\u0C66-\u0C6F]",thai:"[\u0E50-\u0E59]",tibt:"[\u0F20-\u0F29]",latn:"\\d"},Rm={arab:[1632,1641],arabext:[1776,1785],bali:[6992,7001],beng:[2534,2543],deva:[2406,2415],fullwide:[65296,65303],gujr:[2790,2799],khmr:[6112,6121],knda:[3302,3311],laoo:[3792,3801],limb:[6470,6479],mlym:[3430,3439],mong:[6160,6169],mymr:[4160,4169],orya:[2918,2927],tamldec:[3046,3055],telu:[3174,3183],thai:[3664,3673],tibt:[3872,3881]},Cv=Ql.hanidec.replace(/[\[|\]]/g,"").split("");function xv(i){let n=parseInt(i,10);if(isNaN(n)){n="";for(let e=0;e<i.length;e++){let t=i.charCodeAt(e);if(i[e].search(Ql.hanidec)!==-1)n+=Cv.indexOf(i[e]);else for(let r in Rm){let[a,o]=Rm[r];t>=a&&t<=o&&(n+=t-a)}}return parseInt(n,10)}else return n}var Gl=new Map;function Dv(){Gl.clear()}function Rt({numberingSystem:i},n=""){let e=i||"latn",t=Gl.get(e);t===void 0&&(t=new Map,Gl.set(e,t));let r=t.get(n);return r===void 0&&(r=new RegExp(`${Ql[e]}${n}`),t.set(n,r)),r}var Vm=()=>Date.now(),Nm="system",Lm=null,Bm=null,jm=null,zm=60,Hm,Um=null,Ce=class{static get now(){return Vm}static set now(n){Vm=n}static set defaultZone(n){Nm=n}static get defaultZone(){return En(Nm,Eo.instance)}static get defaultLocale(){return Lm}static set defaultLocale(n){Lm=n}static get defaultNumberingSystem(){return Bm}static set defaultNumberingSystem(n){Bm=n}static get defaultOutputCalendar(){return jm}static set defaultOutputCalendar(n){jm=n}static get defaultWeekSettings(){return Um}static set defaultWeekSettings(n){Um=$l(n)}static get twoDigitCutoffYear(){return zm}static set twoDigitCutoffYear(n){zm=n%100}static get throwOnInvalid(){return Hm}static set throwOnInvalid(n){Hm=n}static resetCaches(){ue.resetCache(),Sn.resetCache(),se.resetCache(),Dv()}},ft=class{constructor(n,e){this.reason=n,this.explanation=e}toMessage(){return this.explanation?`${this.reason}: ${this.explanation}`:this.reason}},Op=[0,31,59,90,120,151,181,212,243,273,304,334],Tp=[0,31,60,91,121,152,182,213,244,274,305,335];function wt(i,n){return new ft("unit out of range",`you specified ${n} (of type ${typeof n}) as a ${i}, which is invalid`)}function Xl(i,n,e){let t=new Date(Date.UTC(i,n-1,e));i<100&&i>=0&&t.setUTCFullYear(t.getUTCFullYear()-1900);let r=t.getUTCDay();return r===0?7:r}function Fp(i,n,e){return e+(Yr(i)?Tp:Op)[n-1]}function Pp(i,n){let e=Yr(i)?Tp:Op,t=e.findIndex(a=>a<n),r=n-e[t];return{month:t+1,day:r}}function Jl(i,n){return(i-n+7)%7+1}function ko(i,n=4,e=1){let{year:t,month:r,day:a}=i,o=Fp(t,r,a),s=Jl(Xl(t,r,a),e),d=Math.floor((o-s+14-n)/7),u;return d<1?(u=t-1,d=Wr(u,n,e)):d>Wr(t,n,e)?(u=t+1,d=1):u=t,g({weekYear:u,weekNumber:d,weekday:s},Po(i))}function Wm(i,n=4,e=1){let{weekYear:t,weekNumber:r,weekday:a}=i,o=Jl(Xl(t,1,n),e),s=zi(t),d=r*7+a-o-7+n,u;d<1?(u=t-1,d+=zi(u)):d>s?(u=t+1,d-=zi(t)):u=t;let{month:b,day:A}=Pp(u,d);return g({year:u,month:b,day:A},Po(i))}function kl(i){let{year:n,month:e,day:t}=i,r=Fp(n,e,t);return g({year:n,ordinal:r},Po(i))}function Ym(i){let{year:n,ordinal:e}=i,{month:t,day:r}=Pp(n,e);return g({year:n,month:t,day:r},Po(i))}function qm(i,n){if(!j(i.localWeekday)||!j(i.localWeekNumber)||!j(i.localWeekYear)){if(!j(i.weekday)||!j(i.weekNumber)||!j(i.weekYear))throw new kn("Cannot mix locale-based week fields with ISO-based week fields");return j(i.localWeekday)||(i.weekday=i.localWeekday),j(i.localWeekNumber)||(i.weekNumber=i.localWeekNumber),j(i.localWeekYear)||(i.weekYear=i.localWeekYear),delete i.localWeekday,delete i.localWeekNumber,delete i.localWeekYear,{minDaysInFirstWeek:n.getMinDaysInFirstWeek(),startOfWeek:n.getStartOfWeek()}}else return{minDaysInFirstWeek:4,startOfWeek:1}}function Mv(i,n=4,e=1){let t=Oo(i.weekYear),r=Et(i.weekNumber,1,Wr(i.weekYear,n,e)),a=Et(i.weekday,1,7);return t?r?a?!1:wt("weekday",i.weekday):wt("week",i.weekNumber):wt("weekYear",i.weekYear)}function wv(i){let n=Oo(i.year),e=Et(i.ordinal,1,zi(i.year));return n?e?!1:wt("ordinal",i.ordinal):wt("year",i.year)}function Rp(i){let n=Oo(i.year),e=Et(i.month,1,12),t=Et(i.day,1,Ao(i.year,i.month));return n?e?t?!1:wt("day",i.day):wt("month",i.month):wt("year",i.year)}function Vp(i){let{hour:n,minute:e,second:t,millisecond:r}=i,a=Et(n,0,23)||n===24&&e===0&&t===0&&r===0,o=Et(e,0,59),s=Et(t,0,59),d=Et(r,0,999);return a?o?s?d?!1:wt("millisecond",r):wt("second",t):wt("minute",e):wt("hour",n)}function j(i){return typeof i>"u"}function An(i){return typeof i=="number"}function Oo(i){return typeof i=="number"&&i%1===0}function Ev(i){return typeof i=="string"}function kv(i){return Object.prototype.toString.call(i)==="[object Date]"}function Np(){try{return typeof Intl<"u"&&!!Intl.RelativeTimeFormat}catch{return!1}}function Lp(){try{return typeof Intl<"u"&&!!Intl.Locale&&("weekInfo"in Intl.Locale.prototype||"getWeekInfo"in Intl.Locale.prototype)}catch{return!1}}function Av(i){return Array.isArray(i)?i:[i]}function Gm(i,n,e){if(i.length!==0)return i.reduce((t,r)=>{let a=[n(r),r];return t&&e(t[0],a[0])===t[0]?t:a},null)[1]}function Sv(i,n){return n.reduce((e,t)=>(e[t]=i[t],e),{})}function Wi(i,n){return Object.prototype.hasOwnProperty.call(i,n)}function $l(i){if(i==null)return null;if(typeof i!="object")throw new We("Week settings must be an object");if(!Et(i.firstDay,1,7)||!Et(i.minimalDays,1,7)||!Array.isArray(i.weekend)||i.weekend.some(n=>!Et(n,1,7)))throw new We("Invalid week settings");return{firstDay:i.firstDay,minimalDays:i.minimalDays,weekend:Array.from(i.weekend)}}function Et(i,n,e){return Oo(i)&&i>=n&&i<=e}function Iv(i,n){return i-n*Math.floor(i/n)}function Ve(i,n=2){let e=i<0,t;return e?t="-"+(""+-i).padStart(n,"0"):t=(""+i).padStart(n,"0"),t}function wn(i){if(!(j(i)||i===null||i===""))return parseInt(i,10)}function Yn(i){if(!(j(i)||i===null||i===""))return parseFloat(i)}function ed(i){if(!(j(i)||i===null||i==="")){let n=parseFloat("0."+i)*1e3;return Math.floor(n)}}function td(i,n,e="round"){let t=10**n;switch(e){case"expand":return i>0?Math.ceil(i*t)/t:Math.floor(i*t)/t;case"trunc":return Math.trunc(i*t)/t;case"round":return Math.round(i*t)/t;case"floor":return Math.floor(i*t)/t;case"ceil":return Math.ceil(i*t)/t;default:throw new RangeError(`Value rounding ${e} is out of range`)}}function Yr(i){return i%4===0&&(i%100!==0||i%400===0)}function zi(i){return Yr(i)?366:365}function Ao(i,n){let e=Iv(n-1,12)+1,t=i+(n-e)/12;return e===2?Yr(t)?29:28:[31,null,31,30,31,30,31,31,30,31,30,31][e-1]}function To(i){let n=Date.UTC(i.year,i.month-1,i.day,i.hour,i.minute,i.second,i.millisecond);return i.year<100&&i.year>=0&&(n=new Date(n),n.setUTCFullYear(i.year,i.month-1,i.day)),+n}function $m(i,n,e){return-Jl(Xl(i,1,n),e)+n-1}function Wr(i,n=4,e=1){let t=$m(i,n,e),r=$m(i+1,n,e);return(zi(i)-t+r)/7}function Kl(i){return i>99?i:i>Ce.twoDigitCutoffYear?1900+i:2e3+i}function Bp(i,n,e,t=null){let r=new Date(i),a={hourCycle:"h23",year:"numeric",month:"2-digit",day:"2-digit",hour:"2-digit",minute:"2-digit"};t&&(a.timeZone=t);let o=g({timeZoneName:n},a),s=new Intl.DateTimeFormat(e,o).formatToParts(r).find(d=>d.type.toLowerCase()==="timezonename");return s?s.value:null}function Fo(i,n){let e=parseInt(i,10);Number.isNaN(e)&&(e=0);let t=parseInt(n,10)||0,r=e<0||Object.is(e,-0)?-t:t;return e*60+r}function jp(i){let n=Number(i);if(typeof i=="boolean"||i===""||!Number.isFinite(n))throw new We(`Invalid unit value ${i}`);return n}function So(i,n){let e={};for(let t in i)if(Wi(i,t)){let r=i[t];if(r==null)continue;e[n(t)]=jp(r)}return e}function Ur(i,n){let e=Math.trunc(Math.abs(i/60)),t=Math.trunc(Math.abs(i%60)),r=i>=0?"+":"-";switch(n){case"short":return`${r}${Ve(e,2)}:${Ve(t,2)}`;case"narrow":return`${r}${e}${t>0?`:${t}`:""}`;case"techie":return`${r}${Ve(e,2)}${Ve(t,2)}`;default:throw new RangeError(`Value format ${n} is out of range for property format`)}}function Po(i){return Sv(i,["hour","minute","second","millisecond"])}var Ov=["January","February","March","April","May","June","July","August","September","October","November","December"],zp=["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"],Tv=["J","F","M","A","M","J","J","A","S","O","N","D"];function Hp(i){switch(i){case"narrow":return[...Tv];case"short":return[...zp];case"long":return[...Ov];case"numeric":return["1","2","3","4","5","6","7","8","9","10","11","12"];case"2-digit":return["01","02","03","04","05","06","07","08","09","10","11","12"];default:return null}}var Up=["Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"],Wp=["Mon","Tue","Wed","Thu","Fri","Sat","Sun"],Fv=["M","T","W","T","F","S","S"];function Yp(i){switch(i){case"narrow":return[...Fv];case"short":return[...Wp];case"long":return[...Up];case"numeric":return["1","2","3","4","5","6","7"];default:return null}}var qp=["AM","PM"],Pv=["Before Christ","Anno Domini"],Rv=["BC","AD"],Vv=["B","A"];function Gp(i){switch(i){case"narrow":return[...Vv];case"short":return[...Rv];case"long":return[...Pv];default:return null}}function Nv(i){return qp[i.hour<12?0:1]}function Lv(i,n){return Yp(n)[i.weekday-1]}function Bv(i,n){return Hp(n)[i.month-1]}function jv(i,n){return Gp(n)[i.year<0?0:1]}function zv(i,n,e="always",t=!1){let r={years:["year","yr."],quarters:["quarter","qtr."],months:["month","mo."],weeks:["week","wk."],days:["day","day","days"],hours:["hour","hr."],minutes:["minute","min."],seconds:["second","sec."]},a=["hours","minutes","seconds"].indexOf(i)===-1;if(e==="auto"&&a){let A=i==="days";switch(n){case 1:return A?"tomorrow":`next ${r[i][0]}`;case-1:return A?"yesterday":`last ${r[i][0]}`;case 0:return A?"today":`this ${r[i][0]}`}}let o=Object.is(n,-0)||n<0,s=Math.abs(n),d=s===1,u=r[i],b=t?d?u[1]:u[2]||u[1]:d?r[i][0]:i;return o?`${s} ${b} ago`:`in ${s} ${b}`}function Km(i,n){let e="";for(let t of i)t.literal?e+=t.val:e+=n(t.val);return e}var Hv={D:wo,DD:cp,DDD:up,DDDD:mp,t:pp,tt:hp,ttt:fp,tttt:gp,T:_p,TT:bp,TTT:vp,TTTT:yp,f:Cp,ff:Dp,fff:wp,ffff:kp,F:xp,FF:Mp,FFF:Ep,FFFF:Ap},ht=class i{static create(n,e={}){return new i(n,e)}static parseFormat(n){let e=null,t="",r=!1,a=[];for(let o=0;o<n.length;o++){let s=n.charAt(o);s==="'"?((t.length>0||r)&&a.push({literal:r||/^\s+$/.test(t),val:t===""?"'":t}),e=null,t="",r=!r):r||s===e?t+=s:(t.length>0&&a.push({literal:/^\s+$/.test(t),val:t}),t=s,e=s)}return t.length>0&&a.push({literal:r||/^\s+$/.test(t),val:t}),a}static macroTokenToFormatOpts(n){return Hv[n]}constructor(n,e){this.opts=e,this.loc=n,this.systemLoc=null}formatWithSystemDefault(n,e){return this.systemLoc===null&&(this.systemLoc=this.loc.redefaultToSystem()),this.systemLoc.dtFormatter(n,g(g({},this.opts),e)).format()}dtFormatter(n,e={}){return this.loc.dtFormatter(n,g(g({},this.opts),e))}formatDateTime(n,e){return this.dtFormatter(n,e).format()}formatDateTimeParts(n,e){return this.dtFormatter(n,e).formatToParts()}formatInterval(n,e){return this.dtFormatter(n.start,e).dtf.formatRange(n.start.toJSDate(),n.end.toJSDate())}resolvedOptions(n,e){return this.dtFormatter(n,e).resolvedOptions()}num(n,e=0,t=void 0){if(this.opts.forceSimple)return Ve(n,e);let r=g({},this.opts);return e>0&&(r.padTo=e),t&&(r.signDisplay=t),this.loc.numberFormatter(r).format(n)}formatDateTimeFromString(n,e){let t=this.loc.listingMode()==="en",r=this.loc.outputCalendar&&this.loc.outputCalendar!=="gregory",a=(I,me)=>this.loc.extract(n,I,me),o=I=>n.isOffsetFixed&&n.offset===0&&I.allowZ?"Z":n.isValid?n.zone.formatOffset(n.ts,I.format):"",s=()=>t?Nv(n):a({hour:"numeric",hourCycle:"h12"},"dayperiod"),d=(I,me)=>t?Bv(n,I):a(me?{month:I}:{month:I,day:"numeric"},"month"),u=(I,me)=>t?Lv(n,I):a(me?{weekday:I}:{weekday:I,month:"long",day:"numeric"},"weekday"),b=I=>{let me=i.macroTokenToFormatOpts(I);return me?this.formatWithSystemDefault(n,me):I},A=I=>t?jv(n,I):a({era:I},"era"),Q=I=>{switch(I){case"S":return this.num(n.millisecond);case"u":case"SSS":return this.num(n.millisecond,3);case"s":return this.num(n.second);case"ss":return this.num(n.second,2);case"uu":return this.num(Math.floor(n.millisecond/10),2);case"uuu":return this.num(Math.floor(n.millisecond/100));case"m":return this.num(n.minute);case"mm":return this.num(n.minute,2);case"h":return this.num(n.hour%12===0?12:n.hour%12);case"hh":return this.num(n.hour%12===0?12:n.hour%12,2);case"H":return this.num(n.hour);case"HH":return this.num(n.hour,2);case"Z":return o({format:"narrow",allowZ:this.opts.allowZ});case"ZZ":return o({format:"short",allowZ:this.opts.allowZ});case"ZZZ":return o({format:"techie",allowZ:this.opts.allowZ});case"ZZZZ":return n.zone.offsetName(n.ts,{format:"short",locale:this.loc.locale});case"ZZZZZ":return n.zone.offsetName(n.ts,{format:"long",locale:this.loc.locale});case"z":return n.zoneName;case"a":return s();case"d":return r?a({day:"numeric"},"day"):this.num(n.day);case"dd":return r?a({day:"2-digit"},"day"):this.num(n.day,2);case"c":return this.num(n.weekday);case"ccc":return u("short",!0);case"cccc":return u("long",!0);case"ccccc":return u("narrow",!0);case"E":return this.num(n.weekday);case"EEE":return u("short",!1);case"EEEE":return u("long",!1);case"EEEEE":return u("narrow",!1);case"L":return r?a({month:"numeric",day:"numeric"},"month"):this.num(n.month);case"LL":return r?a({month:"2-digit",day:"numeric"},"month"):this.num(n.month,2);case"LLL":return d("short",!0);case"LLLL":return d("long",!0);case"LLLLL":return d("narrow",!0);case"M":return r?a({month:"numeric"},"month"):this.num(n.month);case"MM":return r?a({month:"2-digit"},"month"):this.num(n.month,2);case"MMM":return d("short",!1);case"MMMM":return d("long",!1);case"MMMMM":return d("narrow",!1);case"y":return r?a({year:"numeric"},"year"):this.num(n.year);case"yy":return r?a({year:"2-digit"},"year"):this.num(n.year.toString().slice(-2),2);case"yyyy":return r?a({year:"numeric"},"year"):this.num(n.year,4);case"yyyyyy":return r?a({year:"numeric"},"year"):this.num(n.year,6);case"G":return A("short");case"GG":return A("long");case"GGGGG":return A("narrow");case"kk":return this.num(n.weekYear.toString().slice(-2),2);case"kkkk":return this.num(n.weekYear,4);case"W":return this.num(n.weekNumber);case"WW":return this.num(n.weekNumber,2);case"n":return this.num(n.localWeekNumber);case"nn":return this.num(n.localWeekNumber,2);case"ii":return this.num(n.localWeekYear.toString().slice(-2),2);case"iiii":return this.num(n.localWeekYear,4);case"o":return this.num(n.ordinal);case"ooo":return this.num(n.ordinal,3);case"q":return this.num(n.quarter);case"qq":return this.num(n.quarter,2);case"X":return this.num(Math.floor(n.ts/1e3));case"x":return this.num(n.ts);default:return b(I)}};return Km(i.parseFormat(e),Q)}formatDurationFromString(n,e){let t=this.opts.signMode==="negativeLargestOnly"?-1:1,r=b=>{switch(b[0]){case"S":return"milliseconds";case"s":return"seconds";case"m":return"minutes";case"h":return"hours";case"d":return"days";case"w":return"weeks";case"M":return"months";case"y":return"years";default:return null}},a=(b,A)=>Q=>{let I=r(Q);if(I){let me=A.isNegativeDuration&&I!==A.largestUnit?t:1,Se;return this.opts.signMode==="negativeLargestOnly"&&I!==A.largestUnit?Se="never":this.opts.signMode==="all"?Se="always":Se="auto",this.num(b.get(I)*me,Q.length,Se)}else return Q},o=i.parseFormat(e),s=o.reduce((b,{literal:A,val:Q})=>A?b:b.concat(Q),[]),d=n.shiftTo(...s.map(r).filter(b=>b)),u={isNegativeDuration:d<0,largestUnit:Object.keys(d.values)[0]};return Km(o,a(d,u))}},$p=/[A-Za-z_+-]{1,256}(?::?\/[A-Za-z0-9_+-]{1,256}(?:\/[A-Za-z0-9_+-]{1,256})?)?/;function Yi(...i){let n=i.reduce((e,t)=>e+t.source,"");return RegExp(`^${n}$`)}function qi(...i){return n=>i.reduce(([e,t,r],a)=>{let[o,s,d]=a(n,r);return[g(g({},e),o),s||t,d]},[{},null,1]).slice(0,2)}function Gi(i,...n){if(i==null)return[null,null];for(let[e,t]of n){let r=e.exec(i);if(r)return t(r)}return[null,null]}function Kp(...i){return(n,e)=>{let t={},r;for(r=0;r<i.length;r++)t[i[r]]=wn(n[e+r]);return[t,null,e+r]}}var Zp=/(?:([Zz])|([+-]\d\d)(?::?(\d\d))?)/,Uv=`(?:${Zp.source}?(?:\\[(${$p.source})\\])?)?`,nd=/(\d\d)(?::?(\d\d)(?::?(\d\d)(?:[.,](\d{1,30}))?)?)?/,Qp=RegExp(`${nd.source}${Uv}`),id=RegExp(`(?:[Tt]${Qp.source})?`),Wv=/([+-]\d{6}|\d{4})(?:-?(\d\d)(?:-?(\d\d))?)?/,Yv=/(\d{4})-?W(\d\d)(?:-?(\d))?/,qv=/(\d{4})-?(\d{3})/,Gv=Kp("weekYear","weekNumber","weekDay"),$v=Kp("year","ordinal"),Kv=/(\d{4})-(\d\d)-(\d\d)/,Xp=RegExp(`${nd.source} ?(?:${Zp.source}|(${$p.source}))?`),Zv=RegExp(`(?: ${Xp.source})?`);function Hi(i,n,e){let t=i[n];return j(t)?e:wn(t)}function Qv(i,n){return[{year:Hi(i,n),month:Hi(i,n+1,1),day:Hi(i,n+2,1)},null,n+3]}function $i(i,n){return[{hours:Hi(i,n,0),minutes:Hi(i,n+1,0),seconds:Hi(i,n+2,0),milliseconds:ed(i[n+3])},null,n+4]}function qr(i,n){let e=!i[n]&&!i[n+1],t=Fo(i[n+1],i[n+2]),r=e?null:kt.instance(t);return[{},r,n+3]}function Gr(i,n){let e=i[n]?Sn.create(i[n]):null;return[{},e,n+1]}var Xv=RegExp(`^T?${nd.source}$`),Jv=/^-?P(?:(?:(-?\d{1,20}(?:\.\d{1,20})?)Y)?(?:(-?\d{1,20}(?:\.\d{1,20})?)M)?(?:(-?\d{1,20}(?:\.\d{1,20})?)W)?(?:(-?\d{1,20}(?:\.\d{1,20})?)D)?(?:T(?:(-?\d{1,20}(?:\.\d{1,20})?)H)?(?:(-?\d{1,20}(?:\.\d{1,20})?)M)?(?:(-?\d{1,20})(?:[.,](-?\d{1,20}))?S)?)?)$/;function ey(i){let[n,e,t,r,a,o,s,d,u]=i,b=n[0]==="-",A=d&&d[0]==="-",Q=(I,me=!1)=>I!==void 0&&(me||I&&b)?-I:I;return[{years:Q(Yn(e)),months:Q(Yn(t)),weeks:Q(Yn(r)),days:Q(Yn(a)),hours:Q(Yn(o)),minutes:Q(Yn(s)),seconds:Q(Yn(d),d==="-0"),milliseconds:Q(ed(u),A)}]}var ty={GMT:0,EDT:-240,EST:-300,CDT:-300,CST:-360,MDT:-360,MST:-420,PDT:-420,PST:-480};function rd(i,n,e,t,r,a,o){let s={year:n.length===2?Kl(wn(n)):wn(n),month:zp.indexOf(e)+1,day:wn(t),hour:wn(r),minute:wn(a)};return o&&(s.second=wn(o)),i&&(s.weekday=i.length>3?Up.indexOf(i)+1:Wp.indexOf(i)+1),s}var ny=/^(?:(Mon|Tue|Wed|Thu|Fri|Sat|Sun),\s)?(\d{1,2})\s(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\s(\d{2,4})\s(\d\d):(\d\d)(?::(\d\d))?\s(?:(UT|GMT|[ECMP][SD]T)|([Zz])|(?:([+-]\d\d)(\d\d)))$/;function iy(i){let[,n,e,t,r,a,o,s,d,u,b,A]=i,Q=rd(n,r,t,e,a,o,s),I;return d?I=ty[d]:u?I=0:I=Fo(b,A),[Q,new kt(I)]}function ry(i){return i.replace(/\([^()]*\)|[\n\t]/g," ").replace(/(\s\s+)/g," ").trim()}var ay=/^(Mon|Tue|Wed|Thu|Fri|Sat|Sun), (\d\d) (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) (\d{4}) (\d\d):(\d\d):(\d\d) GMT$/,oy=/^(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday), (\d\d)-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-(\d\d) (\d\d):(\d\d):(\d\d) GMT$/,sy=/^(Mon|Tue|Wed|Thu|Fri|Sat|Sun) (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) ( \d|\d\d) (\d\d):(\d\d):(\d\d) (\d{4})$/;function Zm(i){let[,n,e,t,r,a,o,s]=i;return[rd(n,r,t,e,a,o,s),kt.utcInstance]}function ly(i){let[,n,e,t,r,a,o,s]=i;return[rd(n,s,e,t,r,a,o),kt.utcInstance]}var dy=Yi(Wv,id),cy=Yi(Yv,id),uy=Yi(qv,id),my=Yi(Qp),Jp=qi(Qv,$i,qr,Gr),py=qi(Gv,$i,qr,Gr),hy=qi($v,$i,qr,Gr),fy=qi($i,qr,Gr);function gy(i){return Gi(i,[dy,Jp],[cy,py],[uy,hy],[my,fy])}function _y(i){return Gi(ry(i),[ny,iy])}function by(i){return Gi(i,[ay,Zm],[oy,Zm],[sy,ly])}function vy(i){return Gi(i,[Jv,ey])}var yy=qi($i);function Cy(i){return Gi(i,[Xv,yy])}var xy=Yi(Kv,Zv),Dy=Yi(Xp),My=qi($i,qr,Gr);function wy(i){return Gi(i,[xy,Jp],[Dy,My])}var Qm="Invalid Duration",eh={weeks:{days:7,hours:168,minutes:10080,seconds:10080*60,milliseconds:10080*60*1e3},days:{hours:24,minutes:1440,seconds:1440*60,milliseconds:1440*60*1e3},hours:{minutes:60,seconds:3600,milliseconds:3600*1e3},minutes:{seconds:60,milliseconds:60*1e3},seconds:{milliseconds:1e3}},Ey=g({years:{quarters:4,months:12,weeks:52,days:365,hours:365*24,minutes:365*24*60,seconds:365*24*60*60,milliseconds:365*24*60*60*1e3},quarters:{months:3,weeks:13,days:91,hours:2184,minutes:2184*60,seconds:2184*60*60,milliseconds:2184*60*60*1e3},months:{weeks:4,days:30,hours:720,minutes:720*60,seconds:720*60*60,milliseconds:720*60*60*1e3}},eh),Mt=146097/400,Ni=146097/4800,ky=g({years:{quarters:4,months:12,weeks:Mt/7,days:Mt,hours:Mt*24,minutes:Mt*24*60,seconds:Mt*24*60*60,milliseconds:Mt*24*60*60*1e3},quarters:{months:3,weeks:Mt/28,days:Mt/4,hours:Mt*24/4,minutes:Mt*24*60/4,seconds:Mt*24*60*60/4,milliseconds:Mt*24*60*60*1e3/4},months:{weeks:Ni/7,days:Ni,hours:Ni*24,minutes:Ni*24*60,seconds:Ni*24*60*60,milliseconds:Ni*24*60*60*1e3}},eh),Gn=["years","quarters","months","weeks","days","hours","minutes","seconds","milliseconds"],Ay=Gn.slice(0).reverse();function tn(i,n,e=!1){let t={values:e?n.values:g(g({},i.values),n.values||{}),loc:i.loc.clone(n.loc),conversionAccuracy:n.conversionAccuracy||i.conversionAccuracy,matrix:n.matrix||i.matrix};return new Ye(t)}function th(i,n){let e=n.milliseconds??0;for(let t of Ay.slice(1))n[t]&&(e+=n[t]*i[t].milliseconds);return e}function Xm(i,n){let e=th(i,n)<0?-1:1;Gn.reduceRight((t,r)=>{if(j(n[r]))return t;if(t){let a=n[t]*e,o=i[r][t],s=Math.floor(a/o);n[r]+=s*e,n[t]-=s*o*e}return r},null),Gn.reduce((t,r)=>{if(j(n[r]))return t;if(t){let a=n[t]%1;n[t]-=a,n[r]+=a*i[t][r]}return r},null)}function Jm(i){let n={};for(let[e,t]of Object.entries(i))t!==0&&(n[e]=t);return n}var Ye=class i{constructor(n){let e=n.conversionAccuracy==="longterm"||!1,t=e?ky:Ey;n.matrix&&(t=n.matrix),this.values=n.values,this.loc=n.loc||ue.create(),this.conversionAccuracy=e?"longterm":"casual",this.invalid=n.invalid||null,this.matrix=t,this.isLuxonDuration=!0}static fromMillis(n,e){return i.fromObject({milliseconds:n},e)}static fromObject(n,e={}){if(n==null||typeof n!="object")throw new We(`Duration.fromObject: argument expected to be an object, got ${n===null?"null":typeof n}`);return new i({values:So(n,i.normalizeUnit),loc:ue.fromObject(e),conversionAccuracy:e.conversionAccuracy,matrix:e.matrix})}static fromDurationLike(n){if(An(n))return i.fromMillis(n);if(i.isDuration(n))return n;if(typeof n=="object")return i.fromObject(n);throw new We(`Unknown duration argument ${n} of type ${typeof n}`)}static fromISO(n,e){let[t]=vy(n);return t?i.fromObject(t,e):i.invalid("unparsable",`the input "${n}" can't be parsed as ISO 8601`)}static fromISOTime(n,e){let[t]=Cy(n);return t?i.fromObject(t,e):i.invalid("unparsable",`the input "${n}" can't be parsed as ISO 8601`)}static invalid(n,e=null){if(!n)throw new We("need to specify a reason the Duration is invalid");let t=n instanceof ft?n:new ft(n,e);if(Ce.throwOnInvalid)throw new Rl(t);return new i({invalid:t})}static normalizeUnit(n){let e={year:"years",years:"years",quarter:"quarters",quarters:"quarters",month:"months",months:"months",week:"weeks",weeks:"weeks",day:"days",days:"days",hour:"hours",hours:"hours",minute:"minutes",minutes:"minutes",second:"seconds",seconds:"seconds",millisecond:"milliseconds",milliseconds:"milliseconds"}[n&&n.toLowerCase()];if(!e)throw new Mo(n);return e}static isDuration(n){return n&&n.isLuxonDuration||!1}get locale(){return this.isValid?this.loc.locale:null}get numberingSystem(){return this.isValid?this.loc.numberingSystem:null}toFormat(n,e={}){let t=q(g({},e),{floor:e.round!==!1&&e.floor!==!1});return this.isValid?ht.create(this.loc,t).formatDurationFromString(this,n):Qm}toHuman(n={}){if(!this.isValid)return Qm;let e=n.showZeros!==!1,t=Gn.map(r=>{let a=this.values[r];return j(a)||a===0&&!e?null:this.loc.numberFormatter(q(g({style:"unit",unitDisplay:"long"},n),{unit:r.slice(0,-1)})).format(a)}).filter(r=>r);return this.loc.listFormatter(g({type:"conjunction",style:n.listStyle||"narrow"},n)).format(t)}toObject(){return this.isValid?g({},this.values):{}}toISO(){if(!this.isValid)return null;let n="P";return this.years!==0&&(n+=this.years+"Y"),(this.months!==0||this.quarters!==0)&&(n+=this.months+this.quarters*3+"M"),this.weeks!==0&&(n+=this.weeks+"W"),this.days!==0&&(n+=this.days+"D"),(this.hours!==0||this.minutes!==0||this.seconds!==0||this.milliseconds!==0)&&(n+="T"),this.hours!==0&&(n+=this.hours+"H"),this.minutes!==0&&(n+=this.minutes+"M"),(this.seconds!==0||this.milliseconds!==0)&&(n+=td(this.seconds+this.milliseconds/1e3,3)+"S"),n==="P"&&(n+="T0S"),n}toISOTime(n={}){if(!this.isValid)return null;let e=this.toMillis();return e<0||e>=864e5?null:(n=q(g({suppressMilliseconds:!1,suppressSeconds:!1,includePrefix:!1,format:"extended"},n),{includeOffset:!1}),se.fromMillis(e,{zone:"UTC"}).toISOTime(n))}toJSON(){return this.toISO()}toString(){return this.toISO()}[Symbol.for("nodejs.util.inspect.custom")](){return this.isValid?`Duration { values: ${JSON.stringify(this.values)} }`:`Duration { Invalid, reason: ${this.invalidReason} }`}toMillis(){return this.isValid?th(this.matrix,this.values):NaN}valueOf(){return this.toMillis()}plus(n){if(!this.isValid)return this;let e=i.fromDurationLike(n),t={};for(let r of Gn)(Wi(e.values,r)||Wi(this.values,r))&&(t[r]=e.get(r)+this.get(r));return tn(this,{values:t},!0)}minus(n){if(!this.isValid)return this;let e=i.fromDurationLike(n);return this.plus(e.negate())}mapUnits(n){if(!this.isValid)return this;let e={};for(let t of Object.keys(this.values))e[t]=jp(n(this.values[t],t));return tn(this,{values:e},!0)}get(n){return this[i.normalizeUnit(n)]}set(n){if(!this.isValid)return this;let e=g(g({},this.values),So(n,i.normalizeUnit));return tn(this,{values:e})}reconfigure({locale:n,numberingSystem:e,conversionAccuracy:t,matrix:r}={}){let o={loc:this.loc.clone({locale:n,numberingSystem:e}),matrix:r,conversionAccuracy:t};return tn(this,o)}as(n){return this.isValid?this.shiftTo(n).get(n):NaN}normalize(){if(!this.isValid)return this;let n=this.toObject();return Xm(this.matrix,n),tn(this,{values:n},!0)}rescale(){if(!this.isValid)return this;let n=Jm(this.normalize().shiftToAll().toObject());return tn(this,{values:n},!0)}shiftTo(...n){if(!this.isValid)return this;if(n.length===0)return this;n=n.map(o=>i.normalizeUnit(o));let e={},t={},r=this.toObject(),a;for(let o of Gn)if(n.indexOf(o)>=0){a=o;let s=0;for(let u in t)s+=this.matrix[u][o]*t[u],t[u]=0;An(r[o])&&(s+=r[o]);let d=Math.trunc(s);e[o]=d,t[o]=(s*1e3-d*1e3)/1e3}else An(r[o])&&(t[o]=r[o]);for(let o in t)t[o]!==0&&(e[a]+=o===a?t[o]:t[o]/this.matrix[a][o]);return Xm(this.matrix,e),tn(this,{values:e},!0)}shiftToAll(){return this.isValid?this.shiftTo("years","months","weeks","days","hours","minutes","seconds","milliseconds"):this}negate(){if(!this.isValid)return this;let n={};for(let e of Object.keys(this.values))n[e]=this.values[e]===0?0:-this.values[e];return tn(this,{values:n},!0)}removeZeros(){if(!this.isValid)return this;let n=Jm(this.values);return tn(this,{values:n},!0)}get years(){return this.isValid?this.values.years||0:NaN}get quarters(){return this.isValid?this.values.quarters||0:NaN}get months(){return this.isValid?this.values.months||0:NaN}get weeks(){return this.isValid?this.values.weeks||0:NaN}get days(){return this.isValid?this.values.days||0:NaN}get hours(){return this.isValid?this.values.hours||0:NaN}get minutes(){return this.isValid?this.values.minutes||0:NaN}get seconds(){return this.isValid?this.values.seconds||0:NaN}get milliseconds(){return this.isValid?this.values.milliseconds||0:NaN}get isValid(){return this.invalid===null}get invalidReason(){return this.invalid?this.invalid.reason:null}get invalidExplanation(){return this.invalid?this.invalid.explanation:null}equals(n){if(!this.isValid||!n.isValid||!this.loc.equals(n.loc))return!1;function e(t,r){return t===void 0||t===0?r===void 0||r===0:t===r}for(let t of Gn)if(!e(this.values[t],n.values[t]))return!1;return!0}},Li="Invalid Interval";function Sy(i,n){return!i||!i.isValid?Ui.invalid("missing or invalid start"):!n||!n.isValid?Ui.invalid("missing or invalid end"):n<i?Ui.invalid("end before start",`The end of an interval must be after its start, but you had start=${i.toISO()} and end=${n.toISO()}`):null}var Ui=class i{constructor(n){this.s=n.start,this.e=n.end,this.invalid=n.invalid||null,this.isLuxonInterval=!0}static invalid(n,e=null){if(!n)throw new We("need to specify a reason the Interval is invalid");let t=n instanceof ft?n:new ft(n,e);if(Ce.throwOnInvalid)throw new Pl(t);return new i({invalid:t})}static fromDateTimes(n,e){let t=Br(n),r=Br(e),a=Sy(t,r);return a??new i({start:t,end:r})}static after(n,e){let t=Ye.fromDurationLike(e),r=Br(n);return i.fromDateTimes(r,r.plus(t))}static before(n,e){let t=Ye.fromDurationLike(e),r=Br(n);return i.fromDateTimes(r.minus(t),r)}static fromISO(n,e){let[t,r]=(n||"").split("/",2);if(t&&r){let a,o;try{a=se.fromISO(t,e),o=a.isValid}catch{o=!1}let s,d;try{s=se.fromISO(r,e),d=s.isValid}catch{d=!1}if(o&&d)return i.fromDateTimes(a,s);if(o){let u=Ye.fromISO(r,e);if(u.isValid)return i.after(a,u)}else if(d){let u=Ye.fromISO(t,e);if(u.isValid)return i.before(s,u)}}return i.invalid("unparsable",`the input "${n}" can't be parsed as ISO 8601`)}static isInterval(n){return n&&n.isLuxonInterval||!1}get start(){return this.isValid?this.s:null}get end(){return this.isValid?this.e:null}get lastDateTime(){return this.isValid&&this.e?this.e.minus(1):null}get isValid(){return this.invalidReason===null}get invalidReason(){return this.invalid?this.invalid.reason:null}get invalidExplanation(){return this.invalid?this.invalid.explanation:null}length(n="milliseconds"){return this.isValid?this.toDuration(n).get(n):NaN}count(n="milliseconds",e){if(!this.isValid)return NaN;let t=this.start.startOf(n,e),r;return e?.useLocaleWeeks?r=this.end.reconfigure({locale:t.locale}):r=this.end,r=r.startOf(n,e),Math.floor(r.diff(t,n).get(n))+(r.valueOf()!==this.end.valueOf())}hasSame(n){return this.isValid?this.isEmpty()||this.e.minus(1).hasSame(this.s,n):!1}isEmpty(){return this.s.valueOf()===this.e.valueOf()}isAfter(n){return this.isValid?this.s>n:!1}isBefore(n){return this.isValid?this.e<=n:!1}contains(n){return this.isValid?this.s<=n&&this.e>n:!1}set({start:n,end:e}={}){return this.isValid?i.fromDateTimes(n||this.s,e||this.e):this}splitAt(...n){if(!this.isValid)return[];let e=n.map(Br).filter(o=>this.contains(o)).sort((o,s)=>o.toMillis()-s.toMillis()),t=[],{s:r}=this,a=0;for(;r<this.e;){let o=e[a]||this.e,s=+o>+this.e?this.e:o;t.push(i.fromDateTimes(r,s)),r=s,a+=1}return t}splitBy(n){let e=Ye.fromDurationLike(n);if(!this.isValid||!e.isValid||e.as("milliseconds")===0)return[];let{s:t}=this,r=1,a,o=[];for(;t<this.e;){let s=this.start.plus(e.mapUnits(d=>d*r));a=+s>+this.e?this.e:s,o.push(i.fromDateTimes(t,a)),t=a,r+=1}return o}divideEqually(n){return this.isValid?this.splitBy(this.length()/n).slice(0,n):[]}overlaps(n){return this.e>n.s&&this.s<n.e}abutsStart(n){return this.isValid?+this.e==+n.s:!1}abutsEnd(n){return this.isValid?+n.e==+this.s:!1}engulfs(n){return this.isValid?this.s<=n.s&&this.e>=n.e:!1}equals(n){return!this.isValid||!n.isValid?!1:this.s.equals(n.s)&&this.e.equals(n.e)}intersection(n){if(!this.isValid)return this;let e=this.s>n.s?this.s:n.s,t=this.e<n.e?this.e:n.e;return e>=t?null:i.fromDateTimes(e,t)}union(n){if(!this.isValid)return this;let e=this.s<n.s?this.s:n.s,t=this.e>n.e?this.e:n.e;return i.fromDateTimes(e,t)}static merge(n){let[e,t]=n.sort((r,a)=>r.s-a.s).reduce(([r,a],o)=>a?a.overlaps(o)||a.abutsStart(o)?[r,a.union(o)]:[r.concat([a]),o]:[r,o],[[],null]);return t&&e.push(t),e}static xor(n){let e=null,t=0,r=[],a=n.map(d=>[{time:d.s,type:"s"},{time:d.e,type:"e"}]),o=Array.prototype.concat(...a),s=o.sort((d,u)=>d.time-u.time);for(let d of s)t+=d.type==="s"?1:-1,t===1?e=d.time:(e&&+e!=+d.time&&r.push(i.fromDateTimes(e,d.time)),e=null);return i.merge(r)}difference(...n){return i.xor([this].concat(n)).map(e=>this.intersection(e)).filter(e=>e&&!e.isEmpty())}toString(){return this.isValid?`[${this.s.toISO()} \u2013 ${this.e.toISO()})`:Li}[Symbol.for("nodejs.util.inspect.custom")](){return this.isValid?`Interval { start: ${this.s.toISO()}, end: ${this.e.toISO()} }`:`Interval { Invalid, reason: ${this.invalidReason} }`}toLocaleString(n=wo,e={}){return this.isValid?ht.create(this.s.loc.clone(e),n).formatInterval(this):Li}toISO(n){return this.isValid?`${this.s.toISO(n)}/${this.e.toISO(n)}`:Li}toISODate(){return this.isValid?`${this.s.toISODate()}/${this.e.toISODate()}`:Li}toISOTime(n){return this.isValid?`${this.s.toISOTime(n)}/${this.e.toISOTime(n)}`:Li}toFormat(n,{separator:e=" \u2013 "}={}){return this.isValid?`${this.s.toFormat(n)}${e}${this.e.toFormat(n)}`:Li}toDuration(n,e){return this.isValid?this.e.diff(this.s,n,e):Ye.invalid(this.invalidReason)}mapEndpoints(n){return i.fromDateTimes(n(this.s),n(this.e))}},ji=class{static hasDST(n=Ce.defaultZone){let e=se.now().setZone(n).set({month:12});return!n.isUniversal&&e.offset!==e.set({month:6}).offset}static isValidIANAZone(n){return Sn.isValidZone(n)}static normalizeZone(n){return En(n,Ce.defaultZone)}static getStartOfWeek({locale:n=null,locObj:e=null}={}){return(e||ue.create(n)).getStartOfWeek()}static getMinimumDaysInFirstWeek({locale:n=null,locObj:e=null}={}){return(e||ue.create(n)).getMinDaysInFirstWeek()}static getWeekendWeekdays({locale:n=null,locObj:e=null}={}){return(e||ue.create(n)).getWeekendDays().slice()}static months(n="long",{locale:e=null,numberingSystem:t=null,locObj:r=null,outputCalendar:a="gregory"}={}){return(r||ue.create(e,t,a)).months(n)}static monthsFormat(n="long",{locale:e=null,numberingSystem:t=null,locObj:r=null,outputCalendar:a="gregory"}={}){return(r||ue.create(e,t,a)).months(n,!0)}static weekdays(n="long",{locale:e=null,numberingSystem:t=null,locObj:r=null}={}){return(r||ue.create(e,t,null)).weekdays(n)}static weekdaysFormat(n="long",{locale:e=null,numberingSystem:t=null,locObj:r=null}={}){return(r||ue.create(e,t,null)).weekdays(n,!0)}static meridiems({locale:n=null}={}){return ue.create(n).meridiems()}static eras(n="short",{locale:e=null}={}){return ue.create(e,null,"gregory").eras(n)}static features(){return{relative:Np(),localeWeek:Lp()}}};function ep(i,n){let e=r=>r.toUTC(0,{keepLocalTime:!0}).startOf("day").valueOf(),t=e(n)-e(i);return Math.floor(Ye.fromMillis(t).as("days"))}function Iy(i,n,e){let t=[["years",(d,u)=>u.year-d.year],["quarters",(d,u)=>u.quarter-d.quarter+(u.year-d.year)*4],["months",(d,u)=>u.month-d.month+(u.year-d.year)*12],["weeks",(d,u)=>{let b=ep(d,u);return(b-b%7)/7}],["days",ep]],r={},a=i,o,s;for(let[d,u]of t)e.indexOf(d)>=0&&(o=d,r[d]=u(i,n),s=a.plus(r),s>n?(r[d]--,i=a.plus(r),i>n&&(s=i,r[d]--,i=a.plus(r))):i=s);return[i,r,s,o]}function Oy(i,n,e,t){let[r,a,o,s]=Iy(i,n,e),d=n-r,u=e.filter(A=>["hours","minutes","seconds","milliseconds"].indexOf(A)>=0);u.length===0&&(o<n&&(o=r.plus({[s]:1})),o!==r&&(a[s]=(a[s]||0)+d/(o-r)));let b=Ye.fromObject(a,t);return u.length>0?Ye.fromMillis(d,t).shiftTo(...u).plus(b):b}var Ty="missing Intl.DateTimeFormat.formatToParts support";function ae(i,n=e=>e){return{regex:i,deser:([e])=>n(xv(e))}}var Fy="\xA0",nh=`[ ${Fy}]`,ih=new RegExp(nh,"g");function Py(i){return i.replace(/\./g,"\\.?").replace(ih,nh)}function tp(i){return i.replace(/\./g,"").replace(ih," ").toLowerCase()}function Vt(i,n){return i===null?null:{regex:RegExp(i.map(Py).join("|")),deser:([e])=>i.findIndex(t=>tp(e)===tp(t))+n}}function np(i,n){return{regex:i,deser:([,e,t])=>Fo(e,t),groups:n}}function bo(i){return{regex:i,deser:([n])=>n}}function Ry(i){return i.replace(/[\-\[\]{}()*+?.,\\\^$|#\s]/g,"\\$&")}function Vy(i,n){let e=Rt(n),t=Rt(n,"{2}"),r=Rt(n,"{3}"),a=Rt(n,"{4}"),o=Rt(n,"{6}"),s=Rt(n,"{1,2}"),d=Rt(n,"{1,3}"),u=Rt(n,"{1,6}"),b=Rt(n,"{1,9}"),A=Rt(n,"{2,4}"),Q=Rt(n,"{4,6}"),I=Je=>({regex:RegExp(Ry(Je.val)),deser:([Kt])=>Kt,literal:!0}),Se=(Je=>{if(i.literal)return I(Je);switch(Je.val){case"G":return Vt(n.eras("short"),0);case"GG":return Vt(n.eras("long"),0);case"y":return ae(u);case"yy":return ae(A,Kl);case"yyyy":return ae(a);case"yyyyy":return ae(Q);case"yyyyyy":return ae(o);case"M":return ae(s);case"MM":return ae(t);case"MMM":return Vt(n.months("short",!0),1);case"MMMM":return Vt(n.months("long",!0),1);case"L":return ae(s);case"LL":return ae(t);case"LLL":return Vt(n.months("short",!1),1);case"LLLL":return Vt(n.months("long",!1),1);case"d":return ae(s);case"dd":return ae(t);case"o":return ae(d);case"ooo":return ae(r);case"HH":return ae(t);case"H":return ae(s);case"hh":return ae(t);case"h":return ae(s);case"mm":return ae(t);case"m":return ae(s);case"q":return ae(s);case"qq":return ae(t);case"s":return ae(s);case"ss":return ae(t);case"S":return ae(d);case"SSS":return ae(r);case"u":return bo(b);case"uu":return bo(s);case"uuu":return ae(e);case"a":return Vt(n.meridiems(),0);case"kkkk":return ae(a);case"kk":return ae(A,Kl);case"W":return ae(s);case"WW":return ae(t);case"E":case"c":return ae(e);case"EEE":return Vt(n.weekdays("short",!1),1);case"EEEE":return Vt(n.weekdays("long",!1),1);case"ccc":return Vt(n.weekdays("short",!0),1);case"cccc":return Vt(n.weekdays("long",!0),1);case"Z":case"ZZ":return np(new RegExp(`([+-]${s.source})(?::(${t.source}))?`),2);case"ZZZ":return np(new RegExp(`([+-]${s.source})(${t.source})?`),2);case"z":return bo(/[a-z_+-/]{1,256}?/i);case" ":return bo(/[^\S\n\r]/);default:return I(Je)}})(i)||{invalidReason:Ty};return Se.token=i,Se}var Ny={year:{"2-digit":"yy",numeric:"yyyyy"},month:{numeric:"M","2-digit":"MM",short:"MMM",long:"MMMM"},day:{numeric:"d","2-digit":"dd"},weekday:{short:"EEE",long:"EEEE"},dayperiod:"a",dayPeriod:"a",hour12:{numeric:"h","2-digit":"hh"},hour24:{numeric:"H","2-digit":"HH"},minute:{numeric:"m","2-digit":"mm"},second:{numeric:"s","2-digit":"ss"},timeZoneName:{long:"ZZZZZ",short:"ZZZ"}};function Ly(i,n,e){let{type:t,value:r}=i;if(t==="literal"){let d=/^\s+$/.test(r);return{literal:!d,val:d?" ":r}}let a=n[t],o=t;t==="hour"&&(n.hour12!=null?o=n.hour12?"hour12":"hour24":n.hourCycle!=null?n.hourCycle==="h11"||n.hourCycle==="h12"?o="hour12":o="hour24":o=e.hour12?"hour12":"hour24");let s=Ny[o];if(typeof s=="object"&&(s=s[a]),s)return{literal:!1,val:s}}function By(i){return[`^${i.map(e=>e.regex).reduce((e,t)=>`${e}(${t.source})`,"")}$`,i]}function jy(i,n,e){let t=i.match(n);if(t){let r={},a=1;for(let o in e)if(Wi(e,o)){let s=e[o],d=s.groups?s.groups+1:1;!s.literal&&s.token&&(r[s.token.val[0]]=s.deser(t.slice(a,a+d))),a+=d}return[t,r]}else return[t,{}]}function zy(i){let n=a=>{switch(a){case"S":return"millisecond";case"s":return"second";case"m":return"minute";case"h":case"H":return"hour";case"d":return"day";case"o":return"ordinal";case"L":case"M":return"month";case"y":return"year";case"E":case"c":return"weekday";case"W":return"weekNumber";case"k":return"weekYear";case"q":return"quarter";default:return null}},e=null,t;return j(i.z)||(e=Sn.create(i.z)),j(i.Z)||(e||(e=new kt(i.Z)),t=i.Z),j(i.q)||(i.M=(i.q-1)*3+1),j(i.h)||(i.h<12&&i.a===1?i.h+=12:i.h===12&&i.a===0&&(i.h=0)),i.G===0&&i.y&&(i.y=-i.y),j(i.u)||(i.S=ed(i.u)),[Object.keys(i).reduce((a,o)=>{let s=n(o);return s&&(a[s]=i[o]),a},{}),e,t]}var Al=null;function Hy(){return Al||(Al=se.fromMillis(1555555555555)),Al}function Uy(i,n){if(i.literal)return i;let e=ht.macroTokenToFormatOpts(i.val),t=oh(e,n);return t==null||t.includes(void 0)?i:t}function rh(i,n){return Array.prototype.concat(...i.map(e=>Uy(e,n)))}var Io=class{constructor(n,e){if(this.locale=n,this.format=e,this.tokens=rh(ht.parseFormat(e),n),this.units=this.tokens.map(t=>Vy(t,n)),this.disqualifyingUnit=this.units.find(t=>t.invalidReason),!this.disqualifyingUnit){let[t,r]=By(this.units);this.regex=RegExp(t,"i"),this.handlers=r}}explainFromTokens(n){if(this.isValid){let[e,t]=jy(n,this.regex,this.handlers),[r,a,o]=t?zy(t):[null,null,void 0];if(Wi(t,"a")&&Wi(t,"H"))throw new kn("Can't include meridiem when specifying 24-hour format");return{input:n,tokens:this.tokens,regex:this.regex,rawMatches:e,matches:t,result:r,zone:a,specificOffset:o}}else return{input:n,tokens:this.tokens,invalidReason:this.invalidReason}}get isValid(){return!this.disqualifyingUnit}get invalidReason(){return this.disqualifyingUnit?this.disqualifyingUnit.invalidReason:null}};function ah(i,n,e){return new Io(i,e).explainFromTokens(n)}function Wy(i,n,e){let{result:t,zone:r,specificOffset:a,invalidReason:o}=ah(i,n,e);return[t,r,a,o]}function oh(i,n){if(!i)return null;let t=ht.create(n,i).dtFormatter(Hy()),r=t.formatToParts(),a=t.resolvedOptions();return r.map(o=>Ly(o,i,a))}var Sl="Invalid DateTime",ip=864e13;function zr(i){return new ft("unsupported zone",`the zone "${i.name}" is not supported`)}function Il(i){return i.weekData===null&&(i.weekData=ko(i.c)),i.weekData}function Ol(i){return i.localWeekData===null&&(i.localWeekData=ko(i.c,i.loc.getMinDaysInFirstWeek(),i.loc.getStartOfWeek())),i.localWeekData}function qn(i,n){let e={ts:i.ts,zone:i.zone,c:i.c,o:i.o,loc:i.loc,invalid:i.invalid};return new se(q(g(g({},e),n),{old:e}))}function sh(i,n,e){let t=i-n*60*1e3,r=e.offset(t);if(n===r)return[t,n];t-=(r-n)*60*1e3;let a=e.offset(t);return r===a?[t,r]:[i-Math.min(r,a)*60*1e3,Math.max(r,a)]}function vo(i,n){i+=n*60*1e3;let e=new Date(i);return{year:e.getUTCFullYear(),month:e.getUTCMonth()+1,day:e.getUTCDate(),hour:e.getUTCHours(),minute:e.getUTCMinutes(),second:e.getUTCSeconds(),millisecond:e.getUTCMilliseconds()}}function Co(i,n,e){return sh(To(i),n,e)}function rp(i,n){let e=i.o,t=i.c.year+Math.trunc(n.years),r=i.c.month+Math.trunc(n.months)+Math.trunc(n.quarters)*3,a=q(g({},i.c),{year:t,month:r,day:Math.min(i.c.day,Ao(t,r))+Math.trunc(n.days)+Math.trunc(n.weeks)*7}),o=Ye.fromObject({years:n.years-Math.trunc(n.years),quarters:n.quarters-Math.trunc(n.quarters),months:n.months-Math.trunc(n.months),weeks:n.weeks-Math.trunc(n.weeks),days:n.days-Math.trunc(n.days),hours:n.hours,minutes:n.minutes,seconds:n.seconds,milliseconds:n.milliseconds}).as("milliseconds"),s=To(a),[d,u]=sh(s,e,i.zone);return o!==0&&(d+=o,u=i.zone.offset(d)),{ts:d,o:u}}function Bi(i,n,e,t,r,a){let{setZone:o,zone:s}=e;if(i&&Object.keys(i).length!==0||n){let d=n||s,u=se.fromObject(i,q(g({},e),{zone:d,specificOffset:a}));return o?u:u.setZone(s)}else return se.invalid(new ft("unparsable",`the input "${r}" can't be parsed as ${t}`))}function yo(i,n,e=!0){return i.isValid?ht.create(ue.create("en-US"),{allowZ:e,forceSimple:!0}).formatDateTimeFromString(i,n):null}function Tl(i,n,e){let t=i.c.year>9999||i.c.year<0,r="";if(t&&i.c.year>=0&&(r+="+"),r+=Ve(i.c.year,t?6:4),e==="year")return r;if(n){if(r+="-",r+=Ve(i.c.month),e==="month")return r;r+="-"}else if(r+=Ve(i.c.month),e==="month")return r;return r+=Ve(i.c.day),r}function ap(i,n,e,t,r,a,o){let s=!e||i.c.millisecond!==0||i.c.second!==0,d="";switch(o){case"day":case"month":case"year":break;default:if(d+=Ve(i.c.hour),o==="hour")break;if(n){if(d+=":",d+=Ve(i.c.minute),o==="minute")break;s&&(d+=":",d+=Ve(i.c.second))}else{if(d+=Ve(i.c.minute),o==="minute")break;s&&(d+=Ve(i.c.second))}if(o==="second")break;s&&(!t||i.c.millisecond!==0)&&(d+=".",d+=Ve(i.c.millisecond,3))}return r&&(i.isOffsetFixed&&i.offset===0&&!a?d+="Z":i.o<0?(d+="-",d+=Ve(Math.trunc(-i.o/60)),d+=":",d+=Ve(Math.trunc(-i.o%60))):(d+="+",d+=Ve(Math.trunc(i.o/60)),d+=":",d+=Ve(Math.trunc(i.o%60)))),a&&(d+="["+i.zone.ianaName+"]"),d}var lh={month:1,day:1,hour:0,minute:0,second:0,millisecond:0},Yy={weekNumber:1,weekday:1,hour:0,minute:0,second:0,millisecond:0},qy={ordinal:1,hour:0,minute:0,second:0,millisecond:0},xo=["year","month","day","hour","minute","second","millisecond"],Gy=["weekYear","weekNumber","weekday","hour","minute","second","millisecond"],$y=["year","ordinal","hour","minute","second","millisecond"];function Do(i){let n={year:"year",years:"year",month:"month",months:"month",day:"day",days:"day",hour:"hour",hours:"hour",minute:"minute",minutes:"minute",quarter:"quarter",quarters:"quarter",second:"second",seconds:"second",millisecond:"millisecond",milliseconds:"millisecond",weekday:"weekday",weekdays:"weekday",weeknumber:"weekNumber",weeksnumber:"weekNumber",weeknumbers:"weekNumber",weekyear:"weekYear",weekyears:"weekYear",ordinal:"ordinal"}[i.toLowerCase()];if(!n)throw new Mo(i);return n}function op(i){switch(i.toLowerCase()){case"localweekday":case"localweekdays":return"localWeekday";case"localweeknumber":case"localweeknumbers":return"localWeekNumber";case"localweekyear":case"localweekyears":return"localWeekYear";default:return Do(i)}}function Ky(i){if(Hr===void 0&&(Hr=Ce.now()),i.type!=="iana")return i.offset(Hr);let n=i.name,e=Zl.get(n);return e===void 0&&(e=i.offset(Hr),Zl.set(n,e)),e}function sp(i,n){let e=En(n.zone,Ce.defaultZone);if(!e.isValid)return se.invalid(zr(e));let t=ue.fromObject(n),r,a;if(j(i.year))r=Ce.now();else{for(let d of xo)j(i[d])&&(i[d]=lh[d]);let o=Rp(i)||Vp(i);if(o)return se.invalid(o);let s=Ky(e);[r,a]=Co(i,s,e)}return new se({ts:r,zone:e,loc:t,o:a})}function lp(i,n,e){let t=j(e.round)?!0:e.round,r=j(e.rounding)?"trunc":e.rounding,a=(s,d)=>(s=td(s,t||e.calendary?0:2,e.calendary?"round":r),n.loc.clone(e).relFormatter(e).format(s,d)),o=s=>e.calendary?n.hasSame(i,s)?0:n.startOf(s).diff(i.startOf(s),s).get(s):n.diff(i,s).get(s);if(e.unit)return a(o(e.unit),e.unit);for(let s of e.units){let d=o(s);if(Math.abs(d)>=1)return a(d,s)}return a(i>n?-0:0,e.units[e.units.length-1])}function dp(i){let n={},e;return i.length>0&&typeof i[i.length-1]=="object"?(n=i[i.length-1],e=Array.from(i).slice(0,i.length-1)):e=Array.from(i),[n,e]}var Hr,Zl=new Map,se=class i{constructor(n){let e=n.zone||Ce.defaultZone,t=n.invalid||(Number.isNaN(n.ts)?new ft("invalid input"):null)||(e.isValid?null:zr(e));this.ts=j(n.ts)?Ce.now():n.ts;let r=null,a=null;if(!t)if(n.old&&n.old.ts===this.ts&&n.old.zone.equals(e))[r,a]=[n.old.c,n.old.o];else{let s=An(n.o)&&!n.old?n.o:e.offset(this.ts);r=vo(this.ts,s),t=Number.isNaN(r.year)?new ft("invalid input"):null,r=t?null:r,a=t?null:s}this._zone=e,this.loc=n.loc||ue.create(),this.invalid=t,this.weekData=null,this.localWeekData=null,this.c=r,this.o=a,this.isLuxonDateTime=!0}static now(){return new i({})}static local(){let[n,e]=dp(arguments),[t,r,a,o,s,d,u]=e;return sp({year:t,month:r,day:a,hour:o,minute:s,second:d,millisecond:u},n)}static utc(){let[n,e]=dp(arguments),[t,r,a,o,s,d,u]=e;return n.zone=kt.utcInstance,sp({year:t,month:r,day:a,hour:o,minute:s,second:d,millisecond:u},n)}static fromJSDate(n,e={}){let t=kv(n)?n.valueOf():NaN;if(Number.isNaN(t))return i.invalid("invalid input");let r=En(e.zone,Ce.defaultZone);return r.isValid?new i({ts:t,zone:r,loc:ue.fromObject(e)}):i.invalid(zr(r))}static fromMillis(n,e={}){if(An(n))return n<-ip||n>ip?i.invalid("Timestamp out of range"):new i({ts:n,zone:En(e.zone,Ce.defaultZone),loc:ue.fromObject(e)});throw new We(`fromMillis requires a numerical input, but received a ${typeof n} with value ${n}`)}static fromSeconds(n,e={}){if(An(n))return new i({ts:n*1e3,zone:En(e.zone,Ce.defaultZone),loc:ue.fromObject(e)});throw new We("fromSeconds requires a numerical input")}static fromObject(n,e={}){n=n||{};let t=En(e.zone,Ce.defaultZone);if(!t.isValid)return i.invalid(zr(t));let r=ue.fromObject(e),a=So(n,op),{minDaysInFirstWeek:o,startOfWeek:s}=qm(a,r),d=Ce.now(),u=j(e.specificOffset)?t.offset(d):e.specificOffset,b=!j(a.ordinal),A=!j(a.year),Q=!j(a.month)||!j(a.day),I=A||Q,me=a.weekYear||a.weekNumber;if((I||b)&&me)throw new kn("Can't mix weekYear/weekNumber units with year/month/day or ordinals");if(Q&&b)throw new kn("Can't mix ordinal dates with month/day");let Se=me||a.weekday&&!I,Je,Kt,Zt=vo(d,u);Se?(Je=Gy,Kt=Yy,Zt=ko(Zt,o,s)):b?(Je=$y,Kt=qy,Zt=kl(Zt)):(Je=xo,Kt=lh);let Ki=!1;for(let Qi of Je){let Vh=a[Qi];j(Vh)?Ki?a[Qi]=Kt[Qi]:a[Qi]=Zt[Qi]:Ki=!0}let Th=Se?Mv(a,o,s):b?wv(a):Rp(a),ad=Th||Vp(a);if(ad)return i.invalid(ad);let Fh=Se?Wm(a,o,s):b?Ym(a):a,[Ph,Rh]=Co(Fh,u,t),Zi=new i({ts:Ph,zone:t,o:Rh,loc:r});return a.weekday&&I&&n.weekday!==Zi.weekday?i.invalid("mismatched weekday",`you can't specify both a weekday of ${a.weekday} and a date of ${Zi.toISO()}`):Zi.isValid?Zi:i.invalid(Zi.invalid)}static fromISO(n,e={}){let[t,r]=gy(n);return Bi(t,r,e,"ISO 8601",n)}static fromRFC2822(n,e={}){let[t,r]=_y(n);return Bi(t,r,e,"RFC 2822",n)}static fromHTTP(n,e={}){let[t,r]=by(n);return Bi(t,r,e,"HTTP",e)}static fromFormat(n,e,t={}){if(j(n)||j(e))throw new We("fromFormat requires an input string and a format");let{locale:r=null,numberingSystem:a=null}=t,o=ue.fromOpts({locale:r,numberingSystem:a,defaultToEN:!0}),[s,d,u,b]=Wy(o,n,e);return b?i.invalid(b):Bi(s,d,t,`format ${e}`,n,u)}static fromString(n,e,t={}){return i.fromFormat(n,e,t)}static fromSQL(n,e={}){let[t,r]=wy(n);return Bi(t,r,e,"SQL",n)}static invalid(n,e=null){if(!n)throw new We("need to specify a reason the DateTime is invalid");let t=n instanceof ft?n:new ft(n,e);if(Ce.throwOnInvalid)throw new Fl(t);return new i({invalid:t})}static isDateTime(n){return n&&n.isLuxonDateTime||!1}static parseFormatForOpts(n,e={}){let t=oh(n,ue.fromObject(e));return t?t.map(r=>r?r.val:null).join(""):null}static expandFormat(n,e={}){return rh(ht.parseFormat(n),ue.fromObject(e)).map(r=>r.val).join("")}static resetCache(){Hr=void 0,Zl.clear()}get(n){return this[n]}get isValid(){return this.invalid===null}get invalidReason(){return this.invalid?this.invalid.reason:null}get invalidExplanation(){return this.invalid?this.invalid.explanation:null}get locale(){return this.isValid?this.loc.locale:null}get numberingSystem(){return this.isValid?this.loc.numberingSystem:null}get outputCalendar(){return this.isValid?this.loc.outputCalendar:null}get zone(){return this._zone}get zoneName(){return this.isValid?this.zone.name:null}get year(){return this.isValid?this.c.year:NaN}get quarter(){return this.isValid?Math.ceil(this.c.month/3):NaN}get month(){return this.isValid?this.c.month:NaN}get day(){return this.isValid?this.c.day:NaN}get hour(){return this.isValid?this.c.hour:NaN}get minute(){return this.isValid?this.c.minute:NaN}get second(){return this.isValid?this.c.second:NaN}get millisecond(){return this.isValid?this.c.millisecond:NaN}get weekYear(){return this.isValid?Il(this).weekYear:NaN}get weekNumber(){return this.isValid?Il(this).weekNumber:NaN}get weekday(){return this.isValid?Il(this).weekday:NaN}get isWeekend(){return this.isValid&&this.loc.getWeekendDays().includes(this.weekday)}get localWeekday(){return this.isValid?Ol(this).weekday:NaN}get localWeekNumber(){return this.isValid?Ol(this).weekNumber:NaN}get localWeekYear(){return this.isValid?Ol(this).weekYear:NaN}get ordinal(){return this.isValid?kl(this.c).ordinal:NaN}get monthShort(){return this.isValid?ji.months("short",{locObj:this.loc})[this.month-1]:null}get monthLong(){return this.isValid?ji.months("long",{locObj:this.loc})[this.month-1]:null}get weekdayShort(){return this.isValid?ji.weekdays("short",{locObj:this.loc})[this.weekday-1]:null}get weekdayLong(){return this.isValid?ji.weekdays("long",{locObj:this.loc})[this.weekday-1]:null}get offset(){return this.isValid?+this.o:NaN}get offsetNameShort(){return this.isValid?this.zone.offsetName(this.ts,{format:"short",locale:this.locale}):null}get offsetNameLong(){return this.isValid?this.zone.offsetName(this.ts,{format:"long",locale:this.locale}):null}get isOffsetFixed(){return this.isValid?this.zone.isUniversal:null}get isInDST(){return this.isOffsetFixed?!1:this.offset>this.set({month:1,day:1}).offset||this.offset>this.set({month:5}).offset}getPossibleOffsets(){if(!this.isValid||this.isOffsetFixed)return[this];let n=864e5,e=6e4,t=To(this.c),r=this.zone.offset(t-n),a=this.zone.offset(t+n),o=this.zone.offset(t-r*e),s=this.zone.offset(t-a*e);if(o===s)return[this];let d=t-o*e,u=t-s*e,b=vo(d,o),A=vo(u,s);return b.hour===A.hour&&b.minute===A.minute&&b.second===A.second&&b.millisecond===A.millisecond?[qn(this,{ts:d}),qn(this,{ts:u})]:[this]}get isInLeapYear(){return Yr(this.year)}get daysInMonth(){return Ao(this.year,this.month)}get daysInYear(){return this.isValid?zi(this.year):NaN}get weeksInWeekYear(){return this.isValid?Wr(this.weekYear):NaN}get weeksInLocalWeekYear(){return this.isValid?Wr(this.localWeekYear,this.loc.getMinDaysInFirstWeek(),this.loc.getStartOfWeek()):NaN}resolvedLocaleOptions(n={}){let{locale:e,numberingSystem:t,calendar:r}=ht.create(this.loc.clone(n),n).resolvedOptions(this);return{locale:e,numberingSystem:t,outputCalendar:r}}toUTC(n=0,e={}){return this.setZone(kt.instance(n),e)}toLocal(){return this.setZone(Ce.defaultZone)}setZone(n,{keepLocalTime:e=!1,keepCalendarTime:t=!1}={}){if(n=En(n,Ce.defaultZone),n.equals(this.zone))return this;if(n.isValid){let r=this.ts;if(e||t){let a=n.offset(this.ts),o=this.toObject();[r]=Co(o,a,n)}return qn(this,{ts:r,zone:n})}else return i.invalid(zr(n))}reconfigure({locale:n,numberingSystem:e,outputCalendar:t}={}){let r=this.loc.clone({locale:n,numberingSystem:e,outputCalendar:t});return qn(this,{loc:r})}setLocale(n){return this.reconfigure({locale:n})}set(n){if(!this.isValid)return this;let e=So(n,op),{minDaysInFirstWeek:t,startOfWeek:r}=qm(e,this.loc),a=!j(e.weekYear)||!j(e.weekNumber)||!j(e.weekday),o=!j(e.ordinal),s=!j(e.year),d=!j(e.month)||!j(e.day),u=s||d,b=e.weekYear||e.weekNumber;if((u||o)&&b)throw new kn("Can't mix weekYear/weekNumber units with year/month/day or ordinals");if(d&&o)throw new kn("Can't mix ordinal dates with month/day");let A;a?A=Wm(g(g({},ko(this.c,t,r)),e),t,r):j(e.ordinal)?(A=g(g({},this.toObject()),e),j(e.day)&&(A.day=Math.min(Ao(A.year,A.month),A.day))):A=Ym(g(g({},kl(this.c)),e));let[Q,I]=Co(A,this.o,this.zone);return qn(this,{ts:Q,o:I})}plus(n){if(!this.isValid)return this;let e=Ye.fromDurationLike(n);return qn(this,rp(this,e))}minus(n){if(!this.isValid)return this;let e=Ye.fromDurationLike(n).negate();return qn(this,rp(this,e))}startOf(n,{useLocaleWeeks:e=!1}={}){if(!this.isValid)return this;let t={},r=Ye.normalizeUnit(n);switch(r){case"years":t.month=1;case"quarters":case"months":t.day=1;case"weeks":case"days":t.hour=0;case"hours":t.minute=0;case"minutes":t.second=0;case"seconds":t.millisecond=0;break}if(r==="weeks")if(e){let a=this.loc.getStartOfWeek(),{weekday:o}=this;o<a&&(t.weekNumber=this.weekNumber-1),t.weekday=a}else t.weekday=1;if(r==="quarters"){let a=Math.ceil(this.month/3);t.month=(a-1)*3+1}return this.set(t)}endOf(n,e){return this.isValid?this.plus({[n]:1}).startOf(n,e).minus(1):this}toFormat(n,e={}){return this.isValid?ht.create(this.loc.redefaultToEN(e)).formatDateTimeFromString(this,n):Sl}toLocaleString(n=wo,e={}){return this.isValid?ht.create(this.loc.clone(e),n).formatDateTime(this):Sl}toLocaleParts(n={}){return this.isValid?ht.create(this.loc.clone(n),n).formatDateTimeParts(this):[]}toISO({format:n="extended",suppressSeconds:e=!1,suppressMilliseconds:t=!1,includeOffset:r=!0,extendedZone:a=!1,precision:o="milliseconds"}={}){if(!this.isValid)return null;o=Do(o);let s=n==="extended",d=Tl(this,s,o);return xo.indexOf(o)>=3&&(d+="T"),d+=ap(this,s,e,t,r,a,o),d}toISODate({format:n="extended",precision:e="day"}={}){return this.isValid?Tl(this,n==="extended",Do(e)):null}toISOWeekDate(){return yo(this,"kkkk-'W'WW-c")}toISOTime({suppressMilliseconds:n=!1,suppressSeconds:e=!1,includeOffset:t=!0,includePrefix:r=!1,extendedZone:a=!1,format:o="extended",precision:s="milliseconds"}={}){return this.isValid?(s=Do(s),(r&&xo.indexOf(s)>=3?"T":"")+ap(this,o==="extended",e,n,t,a,s)):null}toRFC2822(){return yo(this,"EEE, dd LLL yyyy HH:mm:ss ZZZ",!1)}toHTTP(){return yo(this.toUTC(),"EEE, dd LLL yyyy HH:mm:ss 'GMT'")}toSQLDate(){return this.isValid?Tl(this,!0):null}toSQLTime({includeOffset:n=!0,includeZone:e=!1,includeOffsetSpace:t=!0}={}){let r="HH:mm:ss.SSS";return(e||n)&&(t&&(r+=" "),e?r+="z":n&&(r+="ZZ")),yo(this,r,!0)}toSQL(n={}){return this.isValid?`${this.toSQLDate()} ${this.toSQLTime(n)}`:null}toString(){return this.isValid?this.toISO():Sl}[Symbol.for("nodejs.util.inspect.custom")](){return this.isValid?`DateTime { ts: ${this.toISO()}, zone: ${this.zone.name}, locale: ${this.locale} }`:`DateTime { Invalid, reason: ${this.invalidReason} }`}valueOf(){return this.toMillis()}toMillis(){return this.isValid?this.ts:NaN}toSeconds(){return this.isValid?this.ts/1e3:NaN}toUnixInteger(){return this.isValid?Math.floor(this.ts/1e3):NaN}toJSON(){return this.toISO()}toBSON(){return this.toJSDate()}toObject(n={}){if(!this.isValid)return{};let e=g({},this.c);return n.includeConfig&&(e.outputCalendar=this.outputCalendar,e.numberingSystem=this.loc.numberingSystem,e.locale=this.loc.locale),e}toJSDate(){return new Date(this.isValid?this.ts:NaN)}diff(n,e="milliseconds",t={}){if(!this.isValid||!n.isValid)return Ye.invalid("created by diffing an invalid DateTime");let r=g({locale:this.locale,numberingSystem:this.numberingSystem},t),a=Av(e).map(Ye.normalizeUnit),o=n.valueOf()>this.valueOf(),s=o?this:n,d=o?n:this,u=Oy(s,d,a,r);return o?u.negate():u}diffNow(n="milliseconds",e={}){return this.diff(i.now(),n,e)}until(n){return this.isValid?Ui.fromDateTimes(this,n):this}hasSame(n,e,t){if(!this.isValid)return!1;let r=n.valueOf(),a=this.setZone(n.zone,{keepLocalTime:!0});return a.startOf(e,t)<=r&&r<=a.endOf(e,t)}equals(n){return this.isValid&&n.isValid&&this.valueOf()===n.valueOf()&&this.zone.equals(n.zone)&&this.loc.equals(n.loc)}toRelative(n={}){if(!this.isValid)return null;let e=n.base||i.fromObject({},{zone:this.zone}),t=n.padding?this<e?-n.padding:n.padding:0,r=["years","months","days","hours","minutes","seconds"],a=n.unit;return Array.isArray(n.unit)&&(r=n.unit,a=void 0),lp(e,this.plus(t),q(g({},n),{numeric:"always",units:r,unit:a}))}toRelativeCalendar(n={}){return this.isValid?lp(n.base||i.fromObject({},{zone:this.zone}),this,q(g({},n),{numeric:"auto",units:["years","months","days"],calendary:!0})):null}static min(...n){if(!n.every(i.isDateTime))throw new We("min requires all arguments be DateTimes");return Gm(n,e=>e.valueOf(),Math.min)}static max(...n){if(!n.every(i.isDateTime))throw new We("max requires all arguments be DateTimes");return Gm(n,e=>e.valueOf(),Math.max)}static fromFormatExplain(n,e,t={}){let{locale:r=null,numberingSystem:a=null}=t,o=ue.fromOpts({locale:r,numberingSystem:a,defaultToEN:!0});return ah(o,n,e)}static fromStringExplain(n,e,t={}){return i.fromFormatExplain(n,e,t)}static buildFormatParser(n,e={}){let{locale:t=null,numberingSystem:r=null}=e,a=ue.fromOpts({locale:t,numberingSystem:r,defaultToEN:!0});return new Io(a,n)}static fromFormatParser(n,e,t={}){if(j(n)||j(e))throw new We("fromFormatParser requires an input string and a format parser");let{locale:r=null,numberingSystem:a=null}=t,o=ue.fromOpts({locale:r,numberingSystem:a,defaultToEN:!0});if(!o.equals(e.locale))throw new We(`fromFormatParser called with a locale of ${o}, but the format parser was created for ${e.locale}`);let{result:s,zone:d,specificOffset:u,invalidReason:b}=e.explainFromTokens(n);return b?i.invalid(b):Bi(s,d,t,`format ${e.format}`,n,u)}static get DATE_SHORT(){return wo}static get DATE_MED(){return cp}static get DATE_MED_WITH_WEEKDAY(){return av}static get DATE_FULL(){return up}static get DATE_HUGE(){return mp}static get TIME_SIMPLE(){return pp}static get TIME_WITH_SECONDS(){return hp}static get TIME_WITH_SHORT_OFFSET(){return fp}static get TIME_WITH_LONG_OFFSET(){return gp}static get TIME_24_SIMPLE(){return _p}static get TIME_24_WITH_SECONDS(){return bp}static get TIME_24_WITH_SHORT_OFFSET(){return vp}static get TIME_24_WITH_LONG_OFFSET(){return yp}static get DATETIME_SHORT(){return Cp}static get DATETIME_SHORT_WITH_SECONDS(){return xp}static get DATETIME_MED(){return Dp}static get DATETIME_MED_WITH_SECONDS(){return Mp}static get DATETIME_MED_WITH_WEEKDAY(){return ov}static get DATETIME_FULL(){return wp}static get DATETIME_FULL_WITH_SECONDS(){return Ep}static get DATETIME_HUGE(){return kp}static get DATETIME_HUGE_WITH_SECONDS(){return Ap}};function Br(i){if(se.isDateTime(i))return i;if(i&&i.valueOf&&An(i.valueOf()))return se.fromJSDate(i);if(i&&typeof i=="object")return se.fromObject(i);throw new We(`Unknown datetime argument: ${i}, of type ${typeof i}`)}var Ro=class{static pattern(n,e){return t=>{if(!t.value)return null;let r=de.pattern(n)(t);return r?{pattern:q(g({},r.pattern),{message:e||r.pattern.requiredPattern})}:null}}static date(n){let e=n.replace("yyyy","([1-9]\\d{3})").replace("MM","(0[1-9]|1[0-2])").replace("dd","(0[1-9]|[1-2]\\d|3[0-1])");return t=>{if(!t.value)return null;let r=/^.*[\\/-].*[\\/-].*$/.test(e),a=de.pattern(e)(t);return r?a||(se.fromFormat(t.value,n).isValid?null:{invalidDate:!0}):a}}};function dh(i){return Error(`Unable to find icon with the name "${i}"`)}function Zy(){return Error("Could not find HttpClient for use with Angular Material icons. Please add provideHttpClient() to your providers.")}function ch(i){return Error(`The URL provided to MatIconRegistry was not trusted as a resource URL via Angular's DomSanitizer. Attempted URL was "${i}".`)}function uh(i){return Error(`The literal provided to MatIconRegistry was not trusted as safe HTML by Angular's DomSanitizer. Attempted literal was "${i}".`)}var rn=class{url;svgText;options;svgElement=null;constructor(n,e,t){this.url=n,this.svgText=e,this.options=t}},ph=(()=>{class i{_httpClient;_sanitizer;_errorHandler;_document;_svgIconConfigs=new Map;_iconSetConfigs=new Map;_cachedIconsByUrl=new Map;_inProgressUrlFetches=new Map;_fontCssClassesByAlias=new Map;_resolvers=[];_defaultFontSetClass=["material-icons","mat-ligature-font"];constructor(e,t,r,a){this._httpClient=e,this._sanitizer=t,this._errorHandler=a,this._document=r}addSvgIcon(e,t,r){return this.addSvgIconInNamespace("",e,t,r)}addSvgIconLiteral(e,t,r){return this.addSvgIconLiteralInNamespace("",e,t,r)}addSvgIconInNamespace(e,t,r,a){return this._addSvgIconConfig(e,t,new rn(r,null,a))}addSvgIconResolver(e){return this._resolvers.push(e),this}addSvgIconLiteralInNamespace(e,t,r,a){let o=this._sanitizer.sanitize(on.HTML,r);if(!o)throw uh(r);let s=Ln(o);return this._addSvgIconConfig(e,t,new rn("",s,a))}addSvgIconSet(e,t){return this.addSvgIconSetInNamespace("",e,t)}addSvgIconSetLiteral(e,t){return this.addSvgIconSetLiteralInNamespace("",e,t)}addSvgIconSetInNamespace(e,t,r){return this._addSvgIconSetConfig(e,new rn(t,null,r))}addSvgIconSetLiteralInNamespace(e,t,r){let a=this._sanitizer.sanitize(on.HTML,t);if(!a)throw uh(t);let o=Ln(a);return this._addSvgIconSetConfig(e,new rn("",o,r))}registerFontClassAlias(e,t=e){return this._fontCssClassesByAlias.set(e,t),this}classNameForFontAlias(e){return this._fontCssClassesByAlias.get(e)||e}setDefaultFontSetClass(...e){return this._defaultFontSetClass=e,this}getDefaultFontSetClass(){return this._defaultFontSetClass}getSvgIconFromUrl(e){let t=this._sanitizer.sanitize(on.RESOURCE_URL,e);if(!t)throw ch(e);let r=this._cachedIconsByUrl.get(t);return r?Ie(Vo(r)):this._loadSvgIconFromConfig(new rn(e,null)).pipe(Xn(a=>this._cachedIconsByUrl.set(t,a)),Fe(a=>Vo(a)))}getNamedSvgIcon(e,t=""){let r=mh(t,e),a=this._svgIconConfigs.get(r);if(a)return this._getSvgFromConfig(a);if(a=this._getIconConfigFromResolvers(t,e),a)return this._svgIconConfigs.set(r,a),this._getSvgFromConfig(a);let o=this._iconSetConfigs.get(t);return o?this._getSvgFromIconSetConfigs(e,o):ld(dh(r))}ngOnDestroy(){this._resolvers=[],this._svgIconConfigs.clear(),this._iconSetConfigs.clear(),this._cachedIconsByUrl.clear()}_getSvgFromConfig(e){return e.svgText?Ie(Vo(this._svgElementFromConfig(e))):this._loadSvgIconFromConfig(e).pipe(Fe(t=>Vo(t)))}_getSvgFromIconSetConfigs(e,t){let r=this._extractIconWithNameFromAnySet(e,t);if(r)return Ie(r);let a=t.filter(o=>!o.svgText).map(o=>this._loadSvgIconSetFromConfig(o).pipe(ud(s=>{let u=`Loading icon set URL: ${this._sanitizer.sanitize(on.RESOURCE_URL,o.url)} failed: ${s.message}`;return this._errorHandler.handleError(new Error(u)),Ie(null)})));return Zr(a).pipe(Fe(()=>{let o=this._extractIconWithNameFromAnySet(e,t);if(!o)throw dh(e);return o}))}_extractIconWithNameFromAnySet(e,t){for(let r=t.length-1;r>=0;r--){let a=t[r];if(a.svgText&&a.svgText.toString().indexOf(e)>-1){let o=this._svgElementFromConfig(a),s=this._extractSvgIconFromSet(o,e,a.options);if(s)return s}}return null}_loadSvgIconFromConfig(e){return this._fetchIcon(e).pipe(Xn(t=>e.svgText=t),Fe(()=>this._svgElementFromConfig(e)))}_loadSvgIconSetFromConfig(e){return e.svgText?Ie(null):this._fetchIcon(e).pipe(Xn(t=>e.svgText=t))}_extractSvgIconFromSet(e,t,r){let a=e.querySelector(`[id="${t}"]`);if(!a)return null;let o=a.cloneNode(!0);if(o.removeAttribute("id"),o.nodeName.toLowerCase()==="svg")return this._setSvgAttributes(o,r);if(o.nodeName.toLowerCase()==="symbol")return this._setSvgAttributes(this._toSvgElement(o),r);let s=this._svgElementFromString(Ln("<svg></svg>"));return s.appendChild(o),this._setSvgAttributes(s,r)}_svgElementFromString(e){let t=this._document.createElement("DIV");t.innerHTML=e;let r=t.querySelector("svg");if(!r)throw Error("<svg> tag not found");return r}_toSvgElement(e){let t=this._svgElementFromString(Ln("<svg></svg>")),r=e.attributes;for(let a=0;a<r.length;a++){let{name:o,value:s}=r[a];o!=="id"&&t.setAttribute(o,s)}for(let a=0;a<e.childNodes.length;a++)e.childNodes[a].nodeType===this._document.ELEMENT_NODE&&t.appendChild(e.childNodes[a].cloneNode(!0));return t}_setSvgAttributes(e,t){return e.setAttribute("fit",""),e.setAttribute("height","100%"),e.setAttribute("width","100%"),e.setAttribute("preserveAspectRatio","xMidYMid meet"),e.setAttribute("focusable","false"),t&&t.viewBox&&e.setAttribute("viewBox",t.viewBox),e}_fetchIcon(e){let{url:t,options:r}=e,a=r?.withCredentials??!1;if(!this._httpClient)throw Zy();if(t==null)throw Error(`Cannot fetch icon from URL "${t}".`);let o=this._sanitizer.sanitize(on.RESOURCE_URL,t);if(!o)throw ch(t);let s=this._inProgressUrlFetches.get(o);if(s)return s;let d=this._httpClient.get(o,{responseType:"text",withCredentials:a}).pipe(Fe(u=>Ln(u)),pd(()=>this._inProgressUrlFetches.delete(o)),fd());return this._inProgressUrlFetches.set(o,d),d}_addSvgIconConfig(e,t,r){return this._svgIconConfigs.set(mh(e,t),r),this}_addSvgIconSetConfig(e,t){let r=this._iconSetConfigs.get(e);return r?r.push(t):this._iconSetConfigs.set(e,[t]),this}_svgElementFromConfig(e){if(!e.svgElement){let t=this._svgElementFromString(e.svgText);this._setSvgAttributes(t,e.options),e.svgElement=t}return e.svgElement}_getIconConfigFromResolvers(e,t){for(let r=0;r<this._resolvers.length;r++){let a=this._resolvers[r](t,e);if(a)return Qy(a)?new rn(a.url,null,a.options):new rn(a,null)}}static \u0275fac=function(t){return new(t||i)(In(Td,8),In(ua),In(_e,8),In(Xr))};static \u0275prov=K({token:i,factory:i.\u0275fac,providedIn:"root"})}return i})();function Vo(i){return i.cloneNode(!0)}function mh(i,n){return i+":"+n}function Qy(i){return!!(i.url&&i.options)}var Xy=["*"],Jy=new M("MAT_ICON_DEFAULT_OPTIONS"),e0=new M("mat-icon-location",{providedIn:"root",factory:()=>{let i=l(_e),n=i?i.location:null;return{getPathname:()=>n?n.pathname+n.search:""}}}),hh=["clip-path","color-profile","src","cursor","fill","filter","marker","marker-start","marker-mid","marker-end","mask","stroke"],t0=hh.map(i=>`[${i}]`).join(", "),n0=/^url\(['"]?#(.*?)['"]?\)$/,fh=(()=>{class i{_elementRef=l(N);_iconRegistry=l(ph);_location=l(e0);_errorHandler=l(Xr);_defaultColor;get color(){return this._color||this._defaultColor}set color(e){this._color=e}_color;inline=!1;get svgIcon(){return this._svgIcon}set svgIcon(e){e!==this._svgIcon&&(e?this._updateSvgIcon(e):this._svgIcon&&this._clearSvgElement(),this._svgIcon=e)}_svgIcon;get fontSet(){return this._fontSet}set fontSet(e){let t=this._cleanupFontValue(e);t!==this._fontSet&&(this._fontSet=t,this._updateFontIconClasses())}_fontSet;get fontIcon(){return this._fontIcon}set fontIcon(e){let t=this._cleanupFontValue(e);t!==this._fontIcon&&(this._fontIcon=t,this._updateFontIconClasses())}_fontIcon;_previousFontSetClass=[];_previousFontIconClass;_svgName=null;_svgNamespace=null;_previousPath;_elementsWithExternalReferences;_currentIconFetch=oe.EMPTY;constructor(){let e=l(new ii("aria-hidden"),{optional:!0}),t=l(Jy,{optional:!0});t&&(t.color&&(this.color=this._defaultColor=t.color),t.fontSet&&(this.fontSet=t.fontSet)),e||this._elementRef.nativeElement.setAttribute("aria-hidden","true")}_splitIconName(e){if(!e)return["",""];let t=e.split(":");switch(t.length){case 1:return["",t[0]];case 2:return t;default:throw Error(`Invalid icon name: "${e}"`)}}ngOnInit(){this._updateFontIconClasses()}ngAfterViewChecked(){let e=this._elementsWithExternalReferences;if(e&&e.size){let t=this._location.getPathname();t!==this._previousPath&&(this._previousPath=t,this._prependPathToReferences(t))}}ngOnDestroy(){this._currentIconFetch.unsubscribe(),this._elementsWithExternalReferences&&this._elementsWithExternalReferences.clear()}_usingFontIcon(){return!this.svgIcon}_setSvgElement(e){this._clearSvgElement();let t=this._location.getPathname();this._previousPath=t,this._cacheChildrenWithExternalReferences(e),this._prependPathToReferences(t),this._elementRef.nativeElement.appendChild(e)}_clearSvgElement(){let e=this._elementRef.nativeElement,t=e.childNodes.length;for(this._elementsWithExternalReferences&&this._elementsWithExternalReferences.clear();t--;){let r=e.childNodes[t];(r.nodeType!==1||r.nodeName.toLowerCase()==="svg")&&r.remove()}}_updateFontIconClasses(){if(!this._usingFontIcon())return;let e=this._elementRef.nativeElement,t=(this.fontSet?this._iconRegistry.classNameForFontAlias(this.fontSet).split(/ +/):this._iconRegistry.getDefaultFontSetClass()).filter(r=>r.length>0);this._previousFontSetClass.forEach(r=>e.classList.remove(r)),t.forEach(r=>e.classList.add(r)),this._previousFontSetClass=t,this.fontIcon!==this._previousFontIconClass&&!t.includes("mat-ligature-font")&&(this._previousFontIconClass&&e.classList.remove(this._previousFontIconClass),this.fontIcon&&e.classList.add(this.fontIcon),this._previousFontIconClass=this.fontIcon)}_cleanupFontValue(e){return typeof e=="string"?e.trim().split(" ")[0]:e}_prependPathToReferences(e){let t=this._elementsWithExternalReferences;t&&t.forEach((r,a)=>{r.forEach(o=>{a.setAttribute(o.name,`url('${e}#${o.value}')`)})})}_cacheChildrenWithExternalReferences(e){let t=e.querySelectorAll(t0),r=this._elementsWithExternalReferences=this._elementsWithExternalReferences||new Map;for(let a=0;a<t.length;a++)hh.forEach(o=>{let s=t[a],d=s.getAttribute(o),u=d?d.match(n0):null;if(u){let b=r.get(s);b||(b=[],r.set(s,b)),b.push({name:o,value:u[1]})}})}_updateSvgIcon(e){if(this._svgNamespace=null,this._svgName=null,this._currentIconFetch.unsubscribe(),e){let[t,r]=this._splitIconName(e);t&&(this._svgNamespace=t),r&&(this._svgName=r),this._currentIconFetch=this._iconRegistry.getNamedSvgIcon(r,t).pipe(Ne(1)).subscribe(a=>this._setSvgElement(a),a=>{let o=`Error retrieving icon ${t}:${r}! ${a.message}`;this._errorHandler.handleError(new Error(o))})}}static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["mat-icon"]],hostAttrs:["role","img",1,"mat-icon","notranslate"],hostVars:10,hostBindings:function(t,r){t&2&&(B("data-mat-icon-type",r._usingFontIcon()?"font":"svg")("data-mat-icon-name",r._svgName||r.fontIcon)("data-mat-icon-namespace",r._svgNamespace||r.fontSet)("fontIcon",r._usingFontIcon()?r.fontIcon:null),Ae(r.color?"mat-"+r.color:""),w("mat-icon-inline",r.inline)("mat-icon-no-color",r.color!=="primary"&&r.color!=="accent"&&r.color!=="warn"))},inputs:{color:"color",inline:[2,"inline","inline",Y],svgIcon:"svgIcon",fontSet:"fontSet",fontIcon:"fontIcon"},exportAs:["matIcon"],ngContentSelectors:Xy,decls:1,vars:0,template:function(t,r){t&1&&(Ee(),Z(0))},styles:[`mat-icon, mat-icon.mat-primary, mat-icon.mat-accent, mat-icon.mat-warn {
  color: var(--mat-icon-color, inherit);
}

.mat-icon {
  -webkit-user-select: none;
  user-select: none;
  background-repeat: no-repeat;
  display: inline-block;
  fill: currentColor;
  height: 24px;
  width: 24px;
  overflow: hidden;
}
.mat-icon.mat-icon-inline {
  font-size: inherit;
  height: inherit;
  line-height: inherit;
  width: inherit;
}
.mat-icon.mat-ligature-font[fontIcon]::before {
  content: attr(fontIcon);
}

[dir=rtl] .mat-icon-rtl-mirror {
  transform: scale(-1, 1);
}

.mat-form-field:not(.mat-form-field-appearance-legacy) .mat-form-field-prefix .mat-icon,
.mat-form-field:not(.mat-form-field-appearance-legacy) .mat-form-field-suffix .mat-icon {
  display: block;
}
.mat-form-field:not(.mat-form-field-appearance-legacy) .mat-form-field-prefix .mat-icon-button .mat-icon,
.mat-form-field:not(.mat-form-field-appearance-legacy) .mat-form-field-suffix .mat-icon-button .mat-icon {
  margin: auto;
}
`],encapsulation:2,changeDetection:0})}return i})(),gh=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275mod=$({type:i});static \u0275inj=G({imports:[fe]})}return i})();var r0=["vitamUIInput"],a0=["datepicker"],o0=["hintArea"],s0=i=>({format:i}),l0=(i,n)=>({pattern:i,invalidDate:n});function d0(i,n){i&1&&(p(0,"span",4),P(1,"*"),h())}function c0(i,n){if(i&1&&(p(0,"mat-hint"),P(1),O(2,"translate"),h()),i&2){let e=m(2);c(),Te(" ",L(2,1,e.hint())," ")}}function u0(i,n){if(i&1&&(p(0,"div",12),_(1,"i",14),v(2,c0,3,3,"mat-hint"),h()),i&2){let e=m();c(2),y(e.hint()?2:-1)}}function _h(i){return typeof i=="number"?new Date(i):i??null}var m0={provide:dt,useExisting:qe(()=>$r),multi:!0},$r=(()=>{class i extends co{get labelFloat(){return!!this.displayedValue}onClick(e){this.hintArea().nativeElement.contains(e)||this.vitamUIInput().nativeElement.focus()}constructor(){let e=l(U);super(e),this.datePipe=l(Id),this.cdr=l(ne),this.pickerType=C("day"),this.format=C(),this.startView=Sd(void 0),this.label=C("DATE.DATE"),this.hint=C(void 0),this.min=C(void 0),this.max=C(void 0),this.minDate=k(()=>_h(this.min())),this.maxDate=k(()=>_h(this.max())),this.outputType=C("String"),this.displayedValue="",this.vitamUIInput=St("vitamUIInput"),this.datepicker=St("datepicker"),this.hintArea=St("hintArea"),this.startViewMapping=new Map([["year","multi-year"],["month","year"],["day","month"]]),this.defaultFormatMapping=new Map([["day","yyyy-MM-dd"],["month","yyyy-MM"],["year","yyyy"]]),this.selectedFormat=k(()=>this.format()||this.defaultFormatMapping.get(this.pickerType()))}ngOnInit(){super.ngOnInit(),this.startView()||this.startView.set(this.startViewMapping.get(this.pickerType())),this.control.addValidators(e=>{let t=this.outputType()==="Date"?{value:this.formatDateToString(e.value)}:e;return Ro.date(this.selectedFormat())(t)})}writeValue(e){this.displayedValue=this.formatDateToString(e),this.datePickerValue=e?e instanceof Date?e:se.fromFormat(e,this.selectedFormat()).toJSDate():null,this.cdr.markForCheck()}onFocus(){this.control.disabled||this.onTouched()}onBlur(){if(this.onTouched(),this.displayedValue!==(this.outputType()==="Date"?this.formatDateToString(this.control.value):this.control.value)){let e=se.fromFormat(this.displayedValue,this.selectedFormat());e.isValid?this.changeValue(e.toJSDate()):this.changeValue(null)}}onTextChange(e){if(this.displayedValue=e,se.fromFormat(e,this.selectedFormat()).isValid){let t=e.replaceAll("-","/"),a=/\d{4,}$/.test(e)?t.split("/").reverse().join("/"):t;this.changeValue(new Date(a))}else this.changeValue(null)}onDateChange(e,t){if(this.pickerType()===e){this.changeValue(t);let r=this.datepicker();r.opened&&r.close()}}formatDateToString(e){return e instanceof Date?this.datePipe.transform(e,this.selectedFormat()):e}changeValue(e){this.datePickerValue=e,e&&(this.displayedValue=this.formatDateToString(e));let t=this.outputType()==="Date"?e:this.displayedValue;this.control.setValue(t),this.onChange(t)}static{this.\u0275fac=function(t){return new(t||i)}}static{this.\u0275cmp=x({type:i,selectors:[["vitamui-datepicker"]],viewQuery:function(t,r){t&1&&ni(r.vitamUIInput,r0,5)(r.datepicker,a0,5)(r.hintArea,o0,5),t&2&&Tn(3)},hostVars:2,hostBindings:function(t,r){t&1&&T("click",function(o){return r.onClick(o.target)}),t&2&&w("vitamui-float",r.labelFloat)},inputs:{pickerType:[1,"pickerType"],format:[1,"format"],startView:[1,"startView"],label:[1,"label"],hint:[1,"hint"],min:[1,"min"],max:[1,"max"],outputType:[1,"outputType"]},outputs:{startView:"startViewChange"},features:[re([m0]),te],decls:20,vars:31,consts:[["vitamUIInput",""],["datepicker",""],["hintArea",""],[1,"vitamui-input"],[1,"required-marker"],[1,"hidden-input",3,"dateChange","matDatepicker","value","min","max"],[3,"focus","blur","ngModelChange","disabled","required","ngModel"],["matSuffix","",3,"for"],["matDatepickerToggleIcon",""],[1,"vitamui-icon","vitamui-icon-calendar"],["panelClass","picker",3,"yearSelected","monthSelected","startView","disabled"],[1,"hint-area"],[1,"info"],[3,"control","errorMessageMap"],[1,"vitamui-icon","vitamui-icon-info"]],template:function(t,r){if(t&1&&(p(0,"div",3)(1,"label"),P(2),O(3,"translate"),v(4,d0,2,0,"span",4),h(),p(5,"input",5),T("dateChange",function(o){return r.onDateChange("day",o.value)}),h(),p(6,"input",6,0),T("focus",function(){return r.onFocus()})("blur",function(){return r.onBlur()})("ngModelChange",function(o){return r.onTextChange(o)}),h(),p(8,"mat-datepicker-toggle",7)(9,"mat-icon",8),_(10,"i",9),h()(),p(11,"mat-datepicker",10,1),T("yearSelected",function(o){return r.onDateChange("year",o)})("monthSelected",function(o){return r.onDateChange("month",o)}),h()(),p(13,"div",11,2),v(15,u0,3,1,"div",12),h(),_(16,"vitamui-form-errors",13),O(17,"translate"),O(18,"translate"),O(19,"translate")),t&2){let a=Ke(12);w("disabled",r.control.disabled),c(2),Te(" ",L(3,17,r.label())," "),c(2),y(r.isRequired()?4:-1),c(),f("matDatepicker",a)("value",r.datePickerValue)("min",r.minDate())("max",r.maxDate()),c(),f("disabled",r.control.disabled)("required",r.isRequired())("ngModel",r.displayedValue),c(2),f("for",a),c(3),f("startView",r.startView())("disabled",r.control.disabled),c(4),y(r.hint()&&(r.control.valid||r.control.disabled||r.control.pristine)?15:-1),c(),f("control",r.control)("errorMessageMap",la(28,l0,Bt(18,21,"DATE.EXPECTED_FORMAT",sa(26,s0,L(17,19,"DATE.FORMAT."+r.selectedFormat()))),L(19,24,"DATE.INVALID")))}},dependencies:[Ii,yn,vi,Xe,ct,Hn,Fm,Tm,go,Dl,xl,ut,xi,Qa,gh,fh,am,nt,ve],styles:[`.vitamui-input[_ngcontent-%COMP%], .vitamui-input[_ngcontent-%COMP%]   label[_ngcontent-%COMP%]{cursor:text}.hidden-input[_ngcontent-%COMP%]{visibility:hidden;position:absolute;pointer-events:none}mat-datepicker-toggle[_ngcontent-%COMP%]   mat-icon[_ngcontent-%COMP%]{vertical-align:unset;font-size:18px;color:var(--vitamui-primary)}  .mat-datepicker-content{box-shadow:0 2px 8px rgba(var(--vitamui-grey-900-rgb),.2),0 4px 16px rgba(var(--vitamui-grey-900-rgb),.05)!important;--mat-datepicker-calendar-navigation-button-icon-color: var(--vitamui-primary);--mat-datepicker-calendar-navigation-button-disabled-icon-color: red}  .mat-datepicker-content .mat-calendar{height:fit-content!important}  .mat-datepicker-content .mat-calendar .mat-calendar-header .mat-calendar-period-button{font-weight:700}  .mat-datepicker-content .mat-calendar .mat-calendar-header .mat-calendar-previous-button,   .mat-datepicker-content .mat-calendar .mat-calendar-header .mat-calendar-next-button{color:var(--vitamui-primary)}  .mat-datepicker-content .mat-calendar .mat-calendar-header .mat-calendar-previous-button[disabled],   .mat-datepicker-content .mat-calendar .mat-calendar-header .mat-calendar-next-button[disabled]{color:var(--vitamui-grey)}  .mat-datepicker-content .mat-calendar .mat-calendar-content .mat-calendar-table tbody>tr:not(.no-hover):hover{background-color:transparent}  .mat-datepicker-content .mat-calendar .mat-calendar-content .mat-calendar-table tr th:first-child:not(.no-first-cell-padding){padding-left:0}  .mat-datepicker-content .mat-calendar .mat-calendar-content .mat-calendar-table tr td:first-child:not(.no-first-cell-padding){padding-left:15px}  .mat-datepicker-content .mat-calendar .mat-calendar-content .mat-calendar-table tr td:not(.no-border):before,   .mat-datepicker-content .mat-calendar .mat-calendar-content .mat-calendar-table tr th:not(.no-border):before{border:none}  .mat-datepicker-content .mat-calendar .mat-calendar-content .mat-calendar-body-cell-content:hover,   .mat-datepicker-content .mat-calendar .mat-calendar-content .mat-calendar-body-active .mat-calendar-body-cell-content{background:var(--vitamui-grey-300)!important}  .mat-datepicker-content .mat-calendar .mat-calendar-content .mat-calendar-body-today{border-color:var(--vitamui-primary)}  .mat-datepicker-content .mat-calendar .mat-calendar-content .mat-calendar-body-selected.mat-calendar-body-today{box-shadow:none!important}  .mat-datepicker-content .mat-calendar .mat-calendar-content .mat-calendar-body-selected{background:var(--vitamui-primary)}  .mat-datepicker-content .mat-calendar .mat-calendar-content .mat-calendar-body-selected:hover{color:var(--vitamui-primary)}  .mat-datepicker-content .mat-calendar .mat-calendar-content .mat-calendar-body-active .mat-calendar-body-selected{color:var(--vitamui-primary)}.hint-area[_ngcontent-%COMP%]{display:flex;flex-direction:column;align-items:flex-start;gap:5px;color:var(--vitamui-grey-700);font-size:12px;padding-top:5px}.hint-area[_ngcontent-%COMP%]   .info[_ngcontent-%COMP%]   i[_ngcontent-%COMP%], .hint-area[_ngcontent-%COMP%]   .info[_ngcontent-%COMP%]   mat-hint[_ngcontent-%COMP%]{color:var(--vitamui-grey-700)}.hint-area[_ngcontent-%COMP%]   .error[_ngcontent-%COMP%]   i[_ngcontent-%COMP%], .hint-area[_ngcontent-%COMP%]   .error[_ngcontent-%COMP%]   mat-hint[_ngcontent-%COMP%]{color:var(--vitamui-red)}
/*# sourceMappingURL=/vitam-ui/develop/datepicker.component-L5JQORCD.css.map */`]})}}return i})();function p0(i,n){if(i&1&&_(0,"vitamui-editor-input",0),i&2){let e,t=m(2);f("control",t.control)("label",(e=t.editObject().displayRule)==null||e.ui==null?null:e.ui.label)("hint",t.editObject().hint)("required",t.editObject().required)}}function h0(i,n){if(i&1&&_(0,"vitamui-editor-textarea",0),i&2){let e,t=m(2);f("control",t.control)("label",(e=t.editObject().displayRule)==null||e.ui==null?null:e.ui.label)("hint",t.editObject().hint)("required",t.editObject().required)}}function f0(i,n){if(i&1&&_(0,"vitamui-editor-select",1),i&2){let e,t=m(2);f("control",t.control)("label",(e=t.editObject().displayRule)==null||e.ui==null?null:e.ui.label)("options",t.editObject().options)("hint",t.editObject().hint)("multiple",t.editObject().cardinality.includes("MANY"))("required",t.editObject().required)}}function g0(i,n){if(i&1&&(p(0,"vitamui-datepicker",2),O(1,"translate"),O(2,"empty"),_(3,"vitamui-hint",3),h()),i&2){let e=m(2);f("formControl",e.control)("label",L(2,8,L(1,6,e.editObject().displayRule.ui.label)))("required",e.editObject().required)("pickerType",e.getPickerType(e.editObject())),c(3),f("control",e.editObject().control)("hint",e.editObject().hint)}}function _0(i,n){if(i&1&&(p(0,"vitamui-datepicker",2),O(1,"translate"),O(2,"empty"),_(3,"vitamui-hint",3),h()),i&2){let e=m(2);f("formControl",e.control)("label",L(2,8,L(1,6,e.editObject().displayRule.ui.label)))("required",e.editObject().required)("pickerType",e.getPickerType(e.editObject())),c(3),f("control",e.editObject().control)("hint",e.editObject().hint)}}function b0(i,n){if(i&1&&v(0,p0,1,4,"vitamui-editor-input",0)(1,h0,1,4,"vitamui-editor-textarea",0)(2,f0,1,6,"vitamui-editor-select",1)(3,g0,4,10,"vitamui-datepicker",2)(4,_0,4,10,"vitamui-datepicker",2),i&2){let e,t=m();y((e=t.uiComponent)==="textfield"?0:e==="textarea"?1:e==="select"?2:e==="datepicker"?3:e==="datetime"?4:-1)}}var No=(()=>{class i{constructor(){this.logger=l(jd),this.dateDisplayService=l(Wd),this.editObject=C(void 0),this.FormControl=Ge}ngOnInit(){let e=this.editObject();e&&(e.control||this.logger.warn(this,e.path,"No control assigned !"),this.uiComponent=e.displayRule?.ui?.component,this.dateFormat=this.dateDisplayService.getFormat(this.uiComponent))}get control(){return this.editObject().control}getPickerType(e){if(e.pattern){if(e.pattern===wr.YEAR)return"year";if(e.pattern===wr.YEAR_MONTH_DAY)return"day";if(e.pattern===wr.YEAR_MONTH)return"month"}return"day"}static{this.\u0275fac=function(t){return new(t||i)}}static{this.\u0275cmp=x({type:i,selectors:[["vitamui-common-primitive-editor"]],inputs:{editObject:[1,"editObject"]},decls:1,vars:1,consts:[[3,"control","label","hint","required"],[3,"control","label","options","hint","multiple","required"],["outputType","String",3,"formControl","label","required","pickerType"],[3,"control","hint"]],template:function(t,r){if(t&1&&v(0,b0,5,1),t&2){let a;y((a=r.editObject())!=null&&a.control&&r.editObject().cardinality!=="ZERO"?0:-1)}},dependencies:[mo,po,ho,$r,yn,Xe,ct,nt,et,qt,tt,ve],styles:[`[_nghost-%COMP%]{width:100%}
/*# sourceMappingURL=/vitam-ui/develop/primitive-editor.component-NTKUBIYG.css.map */`]})}}return i})();var bh=(()=>{class i{constructor(){this.editObject=C.required(),this.subscriptions=new oe}ngOnInit(){let t=[...this.editObject().control.value].shift(),r=Object.keys(this.editObject()).reduce((a,o)=>{let s=this.editObject();return o==="required"&&s[o]&&a.push(de.required),o==="pattern"&&s[o]&&a.push(de.pattern(s[o])),a},[]);this.control=new Ge(t,r),this.subscriptions.add(this.control.valueChanges.subscribe(a=>{this.editObject().control.setValue(a?[a]:[]),this.editObject().control.markAsDirty()}))}ngOnDestroy(){this.subscriptions.unsubscribe()}static{this.\u0275fac=function(t){return new(t||i)}}static{this.\u0275cmp=x({type:i,selectors:[["vitamui-editor-list-input"]],inputs:{editObject:[1,"editObject"]},decls:1,vars:4,consts:[[3,"control","label","hint","required"]],template:function(t,r){if(t&1&&_(0,"vitamui-editor-input",0),t&2){let a;f("control",r.control)("label",(a=r.editObject().displayRule)==null||a.ui==null?null:a.ui.label)("hint",r.editObject().hint)("required",r.editObject().required)}},dependencies:[mo],encapsulation:2})}}return i})();var vh=(()=>{class i{constructor(){this.editObject=C.required(),this.subscriptions=new oe}ngOnInit(){let t=[...this.editObject().control.value].shift(),r=Object.keys(this.editObject()).reduce((a,o)=>{let s=this.editObject();return o==="required"&&s[o]&&a.push(de.required),o==="pattern"&&s[o]&&a.push(de.pattern(s[o])),a},[]);this.control=new Ge(t,r),this.subscriptions.add(this.control.valueChanges.subscribe(a=>{this.editObject().control.setValue(a?[a]:[]),this.editObject().control.markAsDirty()}))}ngOnDestroy(){this.subscriptions.unsubscribe()}static{this.\u0275fac=function(t){return new(t||i)}}static{this.\u0275cmp=x({type:i,selectors:[["vitamui-editor-list-textarea"]],inputs:{editObject:[1,"editObject"]},decls:1,vars:4,consts:[[3,"control","label","hint","required"]],template:function(t,r){if(t&1&&_(0,"vitamui-editor-textarea",0),t&2){let a;f("control",r.control)("label",(a=r.editObject().displayRule)==null||a.ui==null?null:a.ui.label)("hint",r.editObject().hint)("required",r.editObject().required)}},dependencies:[po],encapsulation:2})}}return i})();var yh=(()=>{class i{constructor(){this.editObject=C.required(),this.subscriptions=new oe}get selectedControl(){return this.control||this.editObject().control}ngOnInit(){let e=this.editObject().cardinality.includes("MANY"),t=this.editObject().options.filter(a=>this.editObject().control.value.includes(a)),r=this.editObject();if(!t.length&&r.options.length&&(t=[r.options[0]]),e)r.control.setValue(t);else{let a=Object.keys(r).reduce((s,d)=>(d==="required"&&this.editObject()[d]&&s.push(de.required),s),[]),o=t[0]||r.options[0]||null;this.control=new Ge(null,a),this.subscriptions.add(this.control.valueChanges.subscribe(s=>{let d=this.editObject();d.control.value!==s&&(d.control.setValue([s]),d.control.markAsDirty())})),this.control.setValue(o)}}ngOnDestroy(){this.subscriptions.unsubscribe()}static{this.\u0275fac=function(t){return new(t||i)}}static{this.\u0275cmp=x({type:i,selectors:[["vitamui-editor-list-select"]],inputs:{editObject:[1,"editObject"]},decls:1,vars:6,consts:[[3,"control","label","hint","options","multiple","required"]],template:function(t,r){if(t&1&&_(0,"vitamui-editor-select",0),t&2){let a;f("control",r.selectedControl)("label",(a=r.editObject().displayRule)==null||a.ui==null?null:a.ui.label)("hint",r.editObject().hint)("options",r.editObject().options)("multiple",r.editObject().cardinality.includes("MANY"))("required",r.editObject().required)}},dependencies:[ho],encapsulation:2})}}return i})();var Ch=(()=>{class i{constructor(){this.editObject=C.required(),this.subscriptions=new oe}ngOnInit(){let t=[...this.editObject().control.value].shift(),r=Object.keys(this.editObject()).reduce((a,o)=>{let s=this.editObject();return o==="required"&&s[o]&&a.push(de.required),o==="pattern"&&s[o]&&a.push(de.pattern(s[o])),a},[]);this.control=new Ge(t,r),this.subscriptions.add(this.control.valueChanges.subscribe(a=>{this.editObject().control.setValue(a?[a]:[]),this.editObject().control.markAsDirty()}))}ngOnDestroy(){this.subscriptions.unsubscribe()}static{this.\u0275fac=function(t){return new(t||i)}}static{this.\u0275cmp=x({type:i,selectors:[["vitamui-editor-list-date"]],inputs:{editObject:[1,"editObject"]},decls:4,vars:9,consts:[["pickerType","day","outputType","String","format","yyyy-MM-dd",3,"formControl","label","required"],[3,"control","hint"]],template:function(t,r){t&1&&(p(0,"vitamui-datepicker",0),O(1,"translate"),O(2,"empty"),_(3,"vitamui-hint",1),h()),t&2&&(f("formControl",r.control)("label",L(2,7,L(1,5,r.editObject().displayRule.ui.label)))("required",r.editObject().required),c(3),f("control",r.editObject().control)("hint",r.editObject().hint))},dependencies:[qt,$r,hn,nt,Xe,ct,et,tt,ve],encapsulation:2})}}return i})();var Lo=(()=>{class i{transform(e,t=!0){return`${e}${t?" *":""}`}static{this.\u0275fac=function(t){return new(t||i)}}static{this.\u0275pipe=vd({name:"appendStar",type:i,pure:!0})}}return i})();var xh=()=>[import("./chunk-BKR62W2N.js").then(i=>i.GroupEditorComponent)],v0=(i,n)=>({"display-none":i,disabled:n}),Dh=()=>["textfield","textarea"],Mh=()=>["textfield"],wh=()=>["textarea"];function y0(i,n){if(i&1&&(p(0,"div",7),P(1),O(2,"translate"),O(3,"empty"),h()),i&2){let e=m(2);c(),Fn("",L(2,2,e.favoritePath),": ",L(3,4,e.favoriteEntry[1]))}}function C0(i,n){i&1&&_(0,"div")}function x0(i,n){if(i&1){let e=ze();p(0,"div",1),T("click",function(){J(e);let r=m();return ee(r.addFirst())}),p(1,"div",2)(2,"div",3)(3,"div",2)(4,"div",4),_(5,"i",5),p(6,"span",6),P(7),O(8,"translate"),O(9,"empty"),O(10,"appendStar"),h()(),v(11,y0,4,6,"div",7),h()()()(),p(12,"div",8),Oe(13,C0,1,0,"div",9),h()}if(i&2){let e=m(),t=Ke(3);f("ngClass",la(11,v0,e.editObject().children.length>0,e.editObject().children.length===0)),c(7),ce(Bt(10,8,L(9,6,L(8,4,e.editObject().displayRule.ui.label)),e.editObject().required)),c(4),y(e.favoriteEntry?11:-1),c(2),f("ngTemplateOutlet",t)}}function D0(i,n){if(i&1&&_(0,"vitamui-common-group-editor",11),i&2){let e=m(2).$implicit;f("editObject",e)}}function M0(i,n){i&1&&(ei(0,D0,1,1),Uo(1,0,xh),Wo())}function w0(i,n){if(i&1&&_(0,"vitamui-common-list-editor",11),i&2){let e=m().$implicit;f("editObject",e)}}function E0(i,n){if(i&1&&_(0,"vitamui-common-primitive-editor",11),i&2){let e=m().$implicit;f("editObject",e)}}function k0(i,n){if(i&1&&(p(0,"div",12)(1,"div",13),v(2,M0,3,0),v(3,w0,1,1,"vitamui-common-list-editor",11),v(4,E0,1,1,"vitamui-common-primitive-editor",11),h()()),i&2){let e=n.$implicit,t=m(3);c(2),y(e.type===t.DisplayObjectType.GROUP?2:-1),c(),y(e.type===t.DisplayObjectType.LIST?3:-1),c(),y(e.type===t.DisplayObjectType.PRIMITIVE?4:-1)}}function A0(i,n){if(i&1&&Be(0,k0,5,3,"div",12,at),i&2){let e=m(2);je(e.editObject().children)}}function S0(i,n){if(i&1&&(_(0,"vitamui-input",10),O(1,"translate"),O(2,"empty")),i&2){let e=m(2);f("multiple",!0)("placeholder",L(2,7,L(1,5,e.editObject().displayRule.ui.label)))("required",e.editObject().required)("textarea",e.editObject().displayRule.ui.layout.size==="large")("formControl",e.formControl)}}function I0(i,n){if(i&1&&_(0,"vitamui-editor-list-input",11),i&2){let e=m(2);f("editObject",e.editObject())}}function O0(i,n){if(i&1&&_(0,"vitamui-editor-list-textarea",11),i&2){let e=m(2);f("editObject",e.editObject())}}function T0(i,n){if(i&1&&_(0,"vitamui-editor-list-select",11),i&2){let e=m(2);f("editObject",e.editObject())}}function F0(i,n){if(i&1&&_(0,"vitamui-editor-list-date",11),i&2){let e=m(2);f("editObject",e.editObject())}}function P0(i,n){if(i&1&&(v(0,A0,2,0),v(1,S0,3,9,"vitamui-input",10),v(2,I0,1,1,"vitamui-editor-list-input",11),v(3,O0,1,1,"vitamui-editor-list-textarea",11),v(4,T0,1,1,"vitamui-editor-list-select",11),v(5,F0,1,1,"vitamui-editor-list-date",11)),i&2){let e,t,r,a,o,s,d=m();y(((e=d.editObject())==null?null:e.kind)==="object-array"?0:-1),c(),y(((t=d.editObject())==null?null:t.kind)==="primitive-array"&&((t=d.editObject())!=null&&t.cardinality.includes("MANY"))&&Pn(6,Dh).includes((t=d.editObject())==null||t.displayRule==null||t.displayRule.ui==null?null:t.displayRule.ui.component)?1:-1),c(),y(((r=d.editObject())==null?null:r.kind)==="primitive-array"&&((r=d.editObject())!=null&&r.cardinality.includes("ONE"))&&Pn(7,Mh).includes((r=d.editObject())==null||r.displayRule==null||r.displayRule.ui==null?null:r.displayRule.ui.component)?2:-1),c(),y(((a=d.editObject())==null?null:a.kind)==="primitive-array"&&((a=d.editObject())!=null&&a.cardinality.includes("ONE"))&&Pn(8,wh).includes((a=d.editObject())==null||a.displayRule==null||a.displayRule.ui==null?null:a.displayRule.ui.component)?3:-1),c(),y(((o=d.editObject())==null?null:o.kind)==="primitive-array"&&((o=d.editObject())==null||o.displayRule==null||o.displayRule.ui==null?null:o.displayRule.ui.component)==="select"?4:-1),c(),y(((s=d.editObject())==null?null:s.kind)==="primitive-array"&&((s=d.editObject())==null||s.displayRule==null||s.displayRule.ui==null?null:s.displayRule.ui.component)==="datetime"?5:-1)}}function R0(i,n){if(i&1&&_(0,"vitamui-common-group-editor",11),i&2){let e=m(2).$implicit;f("editObject",e)}}function V0(i,n){i&1&&(ei(0,R0,1,1),Uo(1,0,xh),Wo())}function N0(i,n){if(i&1&&_(0,"vitamui-common-list-editor",11),i&2){let e=m().$implicit;f("editObject",e)}}function L0(i,n){if(i&1&&_(0,"vitamui-common-primitive-editor",11),i&2){let e=m().$implicit;f("editObject",e)}}function B0(i,n){if(i&1&&(p(0,"div",12)(1,"div",13),v(2,V0,3,0),v(3,N0,1,1,"vitamui-common-list-editor",11),v(4,L0,1,1,"vitamui-common-primitive-editor",11),h()()),i&2){let e=n.$implicit,t=m(3);c(2),y(e.type===t.DisplayObjectType.GROUP?2:-1),c(),y(e.type===t.DisplayObjectType.LIST?3:-1),c(),y(e.type===t.DisplayObjectType.PRIMITIVE?4:-1)}}function j0(i,n){if(i&1&&Be(0,B0,5,3,"div",12,at),i&2){let e=m(2);je(e.editObject().children)}}function z0(i,n){if(i&1&&(_(0,"vitamui-input",10),O(1,"translate"),O(2,"empty")),i&2){let e=m(2);f("multiple",!0)("placeholder",L(2,7,L(1,5,e.editObject().displayRule.ui.label)))("required",e.editObject().required)("textarea",e.editObject().displayRule.ui.layout.size==="large")("formControl",e.formControl)}}function H0(i,n){if(i&1&&_(0,"vitamui-editor-list-input",11),i&2){let e=m(2);f("editObject",e.editObject())}}function U0(i,n){if(i&1&&_(0,"vitamui-editor-list-textarea",11),i&2){let e=m(2);f("editObject",e.editObject())}}function W0(i,n){if(i&1&&_(0,"vitamui-editor-list-select",11),i&2){let e=m(2);f("editObject",e.editObject())}}function Y0(i,n){if(i&1&&_(0,"vitamui-editor-list-date",11),i&2){let e=m(2);f("editObject",e.editObject())}}function q0(i,n){if(i&1&&(v(0,j0,2,0),v(1,z0,3,9,"vitamui-input",10),v(2,H0,1,1,"vitamui-editor-list-input",11),v(3,U0,1,1,"vitamui-editor-list-textarea",11),v(4,W0,1,1,"vitamui-editor-list-select",11),v(5,Y0,1,1,"vitamui-editor-list-date",11)),i&2){let e,t,r,a,o,s,d=m();y(((e=d.editObject())==null?null:e.kind)==="object-array"?0:-1),c(),y(((t=d.editObject())==null?null:t.kind)==="primitive-array"&&((t=d.editObject())!=null&&t.cardinality.includes("MANY"))&&Pn(6,Dh).includes((t=d.editObject())==null||t.displayRule==null||t.displayRule.ui==null?null:t.displayRule.ui.component)?1:-1),c(),y(((r=d.editObject())==null?null:r.kind)==="primitive-array"&&((r=d.editObject())!=null&&r.cardinality.includes("ONE"))&&Pn(7,Mh).includes((r=d.editObject())==null||r.displayRule==null||r.displayRule.ui==null?null:r.displayRule.ui.component)?2:-1),c(),y(((a=d.editObject())==null?null:a.kind)==="primitive-array"&&((a=d.editObject())!=null&&a.cardinality.includes("ONE"))&&Pn(8,wh).includes((a=d.editObject())==null||a.displayRule==null||a.displayRule.ui==null?null:a.displayRule.ui.component)?3:-1),c(),y(((o=d.editObject())==null?null:o.kind)==="primitive-array"&&((o=d.editObject())==null||o.displayRule==null||o.displayRule.ui==null?null:o.displayRule.ui.component)==="select"?4:-1),c(),y(((s=d.editObject())==null?null:s.kind)==="primitive-array"&&((s=d.editObject())==null||s.displayRule==null||s.displayRule.ui==null?null:s.displayRule.ui.component)==="datetime"?5:-1)}}var Eh=(()=>{class i{constructor(){this.favoriteEntryService=l(ha),this.editObject=C(void 0),this.DisplayObjectType=pa}get formControl(){return this.editObject().control}ngOnChanges(e){let{editObject:t}=e;t&&this.favoriteEntryService.favoriteEntry(this.editObject())}addFirst(){let e=this.editObject();e.children.length===0&&e.actions.add.handler()}add(e){this.editObject().actions.add.handler(),e.stopPropagation()}removeAt(e,t){this.editObject().actions.removeAt.handler(t),e.stopPropagation()}static{this.\u0275fac=function(t){return new(t||i)}}static{this.\u0275cmp=x({type:i,selectors:[["vitamui-common-list-editor"]],inputs:{editObject:[1,"editObject"]},features:[pe],decls:4,vars:1,consts:[["content",""],[1,"header","mat-elevation-z1",3,"click","ngClass"],[1,"v-row"],[1,"v-col"],[1,"v-col","align-items-center"],[1,"vitamui-icon","icon","vitamui-icon-chevron-right"],[1,"object-name"],[1,"v-col","align-items-center","favorite-entry"],[1,"content"],[4,"ngTemplateOutlet"],[3,"multiple","placeholder","required","textarea","formControl"],[3,"editObject"],[1,"row"],[1,"col"]],template:function(t,r){if(t&1&&(v(0,x0,14,14)(1,P0,6,9),Oe(2,q0,6,9,"ng-template",null,0,Qt)),t&2){let a;y((a=r.editObject())!=null&&a.key&&((a=r.editObject())==null?null:a.kind)==="object-array"?0:1)}},dependencies:[i,ri,ln,No,Wn,yn,Xe,ct,nt,et,bh,vh,yh,Ch,tt,Lo,ve],styles:[`[_nghost-%COMP%]{width:100%;display:flex;flex-direction:column}.v-col[_ngcontent-%COMP%]{display:flex;flex:1}.v-col[_ngcontent-%COMP%]:not(:last-child){margin-right:10px}.v-row[_ngcontent-%COMP%]{display:flex;flex-direction:row;flex-wrap:wrap;width:100%}.align-items-center[_ngcontent-%COMP%]{align-items:center}.display-none[_ngcontent-%COMP%]{display:none}.content[_ngcontent-%COMP%]{background-color:transparent;display:flex;flex-direction:column;gap:10px}.content[_ngcontent-%COMP%]   .content[_ngcontent-%COMP%]   .header[_ngcontent-%COMP%]{min-height:40px;border-radius:unset;font-size:16px;line-height:24px;font-weight:700;padding-left:16px;padding-right:16px}.content[_ngcontent-%COMP%]   .content[_ngcontent-%COMP%]   .header.opened[_ngcontent-%COMP%]{border-left:2px solid var(--vitamui-primary);color:var(--vitamui-primary)}.content[_ngcontent-%COMP%]   .content[_ngcontent-%COMP%]   .header[_ngcontent-%COMP%]   .favorite-entry[_ngcontent-%COMP%]{font-size:14px;line-height:22px;font-weight:700}.content[_ngcontent-%COMP%]   .content[_ngcontent-%COMP%]   .content[_ngcontent-%COMP%]   .header[_ngcontent-%COMP%]{min-height:30px}.content[_ngcontent-%COMP%]   .content[_ngcontent-%COMP%]   .content[_ngcontent-%COMP%]   .content[_ngcontent-%COMP%]   .header[_ngcontent-%COMP%]{min-height:20px}.content[_ngcontent-%COMP%]   .content[_ngcontent-%COMP%]{background-color:#fff;padding:20px 30px}.header[_ngcontent-%COMP%]{min-height:50px;padding:0 16px;border-radius:5px;display:flex;background-color:#fff;flex-direction:row;font-size:20px;line-height:28px;font-weight:700;cursor:pointer;border-left:2px solid transparent}.header.opened[_ngcontent-%COMP%]{border-left:2px solid var(--vitamui-primary);color:var(--vitamui-primary);margin-bottom:5px}.header.disabled[_ngcontent-%COMP%]{border-left:2px solid transparent;color:var(--vitamui-grey)}.header.display-none[_ngcontent-%COMP%]{display:none}.icon[_ngcontent-%COMP%]{font-size:24px;margin-left:5px;margin-right:5px}
/*# sourceMappingURL=/vitam-ui/develop/list-editor.component-LTWM6N2C.css.map */`]})}}return i})();function G0(i,n){i&1&&Pe(0,"div",2)}var $0=new M("MAT_PROGRESS_BAR_DEFAULT_OPTIONS");var Ah=(()=>{class i{_elementRef=l(N);_ngZone=l(X);_changeDetectorRef=l(ne);_renderer=l(we);_cleanupTransitionEnd;constructor(){let e=lr(),t=l($0,{optional:!0});this._isNoopAnimation=e==="di-disabled",e==="reduced-motion"&&this._elementRef.nativeElement.classList.add("mat-progress-bar-reduced-motion"),t&&(t.color&&(this.color=this._defaultColor=t.color),this.mode=t.mode||this.mode)}_isNoopAnimation;get color(){return this._color||this._defaultColor}set color(e){this._color=e}_color;_defaultColor="primary";get value(){return this._value}set value(e){this._value=kh(e||0),this._changeDetectorRef.markForCheck()}_value=0;get bufferValue(){return this._bufferValue||0}set bufferValue(e){this._bufferValue=kh(e||0),this._changeDetectorRef.markForCheck()}_bufferValue=0;animationEnd=new S;get mode(){return this._mode}set mode(e){this._mode=e,this._changeDetectorRef.markForCheck()}_mode="determinate";ngAfterViewInit(){this._ngZone.runOutsideAngular(()=>{this._cleanupTransitionEnd=this._renderer.listen(this._elementRef.nativeElement,"transitionend",this._transitionendHandler)})}ngOnDestroy(){this._cleanupTransitionEnd?.()}_getPrimaryBarTransform(){return`scaleX(${this._isIndeterminate()?1:this.value/100})`}_getBufferBarFlexBasis(){return`${this.mode==="buffer"?this.bufferValue:100}%`}_isIndeterminate(){return this.mode==="indeterminate"||this.mode==="query"}_transitionendHandler=e=>{this.animationEnd.observers.length===0||!e.target||!e.target.classList.contains("mdc-linear-progress__primary-bar")||(this.mode==="determinate"||this.mode==="buffer")&&this._ngZone.run(()=>this.animationEnd.next({value:this.value}))};static \u0275fac=function(t){return new(t||i)};static \u0275cmp=x({type:i,selectors:[["mat-progress-bar"]],hostAttrs:["role","progressbar","aria-valuemin","0","aria-valuemax","100","tabindex","-1",1,"mat-mdc-progress-bar","mdc-linear-progress"],hostVars:10,hostBindings:function(t,r){t&2&&(B("aria-valuenow",r._isIndeterminate()?null:r.value)("mode",r.mode),Ae("mat-"+r.color),w("_mat-animation-noopable",r._isNoopAnimation)("mdc-linear-progress--animation-ready",!r._isNoopAnimation)("mdc-linear-progress--indeterminate",r._isIndeterminate()))},inputs:{color:"color",value:[2,"value","value",vt],bufferValue:[2,"bufferValue","bufferValue",vt],mode:"mode"},outputs:{animationEnd:"animationEnd"},exportAs:["matProgressBar"],decls:7,vars:5,consts:[["aria-hidden","true",1,"mdc-linear-progress__buffer"],[1,"mdc-linear-progress__buffer-bar"],[1,"mdc-linear-progress__buffer-dots"],["aria-hidden","true",1,"mdc-linear-progress__bar","mdc-linear-progress__primary-bar"],[1,"mdc-linear-progress__bar-inner"],["aria-hidden","true",1,"mdc-linear-progress__bar","mdc-linear-progress__secondary-bar"]],template:function(t,r){t&1&&(le(0,"div",0),Pe(1,"div",1),v(2,G0,1,0,"div",2),he(),le(3,"div",3),Pe(4,"span",4),he(),le(5,"div",5),Pe(6,"span",4),he()),t&2&&(c(),At("flex-basis",r._getBufferBarFlexBasis()),c(),y(r.mode==="buffer"?2:-1),c(),At("transform",r._getPrimaryBarTransform()))},styles:[`.mat-mdc-progress-bar {
  --mat-progress-bar-animation-multiplier: 1;
  display: block;
  text-align: start;
}
.mat-mdc-progress-bar[mode=query] {
  transform: scaleX(-1);
}
.mat-mdc-progress-bar._mat-animation-noopable .mdc-linear-progress__buffer-dots,
.mat-mdc-progress-bar._mat-animation-noopable .mdc-linear-progress__primary-bar,
.mat-mdc-progress-bar._mat-animation-noopable .mdc-linear-progress__secondary-bar,
.mat-mdc-progress-bar._mat-animation-noopable .mdc-linear-progress__bar-inner.mdc-linear-progress__bar-inner {
  animation: none;
}
.mat-mdc-progress-bar._mat-animation-noopable .mdc-linear-progress__primary-bar,
.mat-mdc-progress-bar._mat-animation-noopable .mdc-linear-progress__buffer-bar {
  transition: transform 1ms;
}

.mat-progress-bar-reduced-motion {
  --mat-progress-bar-animation-multiplier: 2;
}

.mdc-linear-progress {
  position: relative;
  width: 100%;
  transform: translateZ(0);
  outline: 1px solid transparent;
  overflow-x: hidden;
  transition: opacity 250ms 0ms cubic-bezier(0.4, 0, 0.6, 1);
  height: max(var(--mat-progress-bar-track-height, 4px), var(--mat-progress-bar-active-indicator-height, 4px));
}
@media (forced-colors: active) {
  .mdc-linear-progress {
    outline-color: CanvasText;
  }
}

.mdc-linear-progress__bar {
  position: absolute;
  top: 0;
  bottom: 0;
  margin: auto 0;
  width: 100%;
  animation: none;
  transform-origin: top left;
  transition: transform 250ms 0ms cubic-bezier(0.4, 0, 0.6, 1);
  height: var(--mat-progress-bar-active-indicator-height, 4px);
}
.mdc-linear-progress--indeterminate .mdc-linear-progress__bar {
  transition: none;
}
[dir=rtl] .mdc-linear-progress__bar {
  right: 0;
  transform-origin: center right;
}

.mdc-linear-progress__bar-inner {
  display: inline-block;
  position: absolute;
  width: 100%;
  animation: none;
  border-top-style: solid;
  border-color: var(--mat-progress-bar-active-indicator-color, var(--mat-sys-primary));
  border-top-width: var(--mat-progress-bar-active-indicator-height, 4px);
}

.mdc-linear-progress__buffer {
  display: flex;
  position: absolute;
  top: 0;
  bottom: 0;
  margin: auto 0;
  width: 100%;
  overflow: hidden;
  height: var(--mat-progress-bar-track-height, 4px);
  border-radius: var(--mat-progress-bar-track-shape, var(--mat-sys-corner-none));
}

.mdc-linear-progress__buffer-dots {
  background-image: radial-gradient(circle, var(--mat-progress-bar-track-color, var(--mat-sys-surface-variant)) calc(var(--mat-progress-bar-track-height, 4px) / 2), transparent 0);
  background-repeat: repeat-x;
  background-size: calc(calc(var(--mat-progress-bar-track-height, 4px) / 2) * 5);
  background-position: left;
  flex: auto;
  transform: rotate(180deg);
  animation: mdc-linear-progress-buffering calc(250ms * var(--mat-progress-bar-animation-multiplier)) infinite linear;
}
@media (forced-colors: active) {
  .mdc-linear-progress__buffer-dots {
    background-color: ButtonBorder;
  }
}
[dir=rtl] .mdc-linear-progress__buffer-dots {
  animation: mdc-linear-progress-buffering-reverse calc(250ms * var(--mat-progress-bar-animation-multiplier)) infinite linear;
  transform: rotate(0);
}

.mdc-linear-progress__buffer-bar {
  flex: 0 1 100%;
  transition: flex-basis 250ms 0ms cubic-bezier(0.4, 0, 0.6, 1);
  background-color: var(--mat-progress-bar-track-color, var(--mat-sys-surface-variant));
}

.mdc-linear-progress__primary-bar {
  transform: scaleX(0);
}
.mdc-linear-progress--indeterminate .mdc-linear-progress__primary-bar {
  left: -145.166611%;
}
.mdc-linear-progress--indeterminate.mdc-linear-progress--animation-ready .mdc-linear-progress__primary-bar {
  animation: mdc-linear-progress-primary-indeterminate-translate calc(2s * var(--mat-progress-bar-animation-multiplier)) infinite linear;
}
.mdc-linear-progress--indeterminate.mdc-linear-progress--animation-ready .mdc-linear-progress__primary-bar > .mdc-linear-progress__bar-inner {
  animation: mdc-linear-progress-primary-indeterminate-scale calc(2s * var(--mat-progress-bar-animation-multiplier)) infinite linear;
}
[dir=rtl] .mdc-linear-progress.mdc-linear-progress--animation-ready .mdc-linear-progress__primary-bar {
  animation-name: mdc-linear-progress-primary-indeterminate-translate-reverse;
}
[dir=rtl] .mdc-linear-progress.mdc-linear-progress--indeterminate .mdc-linear-progress__primary-bar {
  right: -145.166611%;
  left: auto;
}

.mdc-linear-progress__secondary-bar {
  display: none;
}
.mdc-linear-progress--indeterminate .mdc-linear-progress__secondary-bar {
  left: -54.888891%;
  display: block;
}
.mdc-linear-progress--indeterminate.mdc-linear-progress--animation-ready .mdc-linear-progress__secondary-bar {
  animation: mdc-linear-progress-secondary-indeterminate-translate calc(2s * var(--mat-progress-bar-animation-multiplier)) infinite linear;
}
.mdc-linear-progress--indeterminate.mdc-linear-progress--animation-ready .mdc-linear-progress__secondary-bar > .mdc-linear-progress__bar-inner {
  animation: mdc-linear-progress-secondary-indeterminate-scale calc(2s * var(--mat-progress-bar-animation-multiplier)) infinite linear;
}
[dir=rtl] .mdc-linear-progress.mdc-linear-progress--animation-ready .mdc-linear-progress__secondary-bar {
  animation-name: mdc-linear-progress-secondary-indeterminate-translate-reverse;
}
[dir=rtl] .mdc-linear-progress.mdc-linear-progress--indeterminate .mdc-linear-progress__secondary-bar {
  right: -54.888891%;
  left: auto;
}

@keyframes mdc-linear-progress-buffering {
  from {
    transform: rotate(180deg) translateX(calc(var(--mat-progress-bar-track-height, 4px) * -2.5));
  }
}
@keyframes mdc-linear-progress-primary-indeterminate-translate {
  0% {
    transform: translateX(0);
  }
  20% {
    animation-timing-function: cubic-bezier(0.5, 0, 0.701732, 0.495819);
    transform: translateX(0);
  }
  59.15% {
    animation-timing-function: cubic-bezier(0.302435, 0.381352, 0.55, 0.956352);
    transform: translateX(83.67142%);
  }
  100% {
    transform: translateX(200.611057%);
  }
}
@keyframes mdc-linear-progress-primary-indeterminate-scale {
  0% {
    transform: scaleX(0.08);
  }
  36.65% {
    animation-timing-function: cubic-bezier(0.334731, 0.12482, 0.785844, 1);
    transform: scaleX(0.08);
  }
  69.15% {
    animation-timing-function: cubic-bezier(0.06, 0.11, 0.6, 1);
    transform: scaleX(0.661479);
  }
  100% {
    transform: scaleX(0.08);
  }
}
@keyframes mdc-linear-progress-secondary-indeterminate-translate {
  0% {
    animation-timing-function: cubic-bezier(0.15, 0, 0.515058, 0.409685);
    transform: translateX(0);
  }
  25% {
    animation-timing-function: cubic-bezier(0.31033, 0.284058, 0.8, 0.733712);
    transform: translateX(37.651913%);
  }
  48.35% {
    animation-timing-function: cubic-bezier(0.4, 0.627035, 0.6, 0.902026);
    transform: translateX(84.386165%);
  }
  100% {
    transform: translateX(160.277782%);
  }
}
@keyframes mdc-linear-progress-secondary-indeterminate-scale {
  0% {
    animation-timing-function: cubic-bezier(0.205028, 0.057051, 0.57661, 0.453971);
    transform: scaleX(0.08);
  }
  19.15% {
    animation-timing-function: cubic-bezier(0.152313, 0.196432, 0.648374, 1.004315);
    transform: scaleX(0.457104);
  }
  44.15% {
    animation-timing-function: cubic-bezier(0.257759, -0.003163, 0.211762, 1.38179);
    transform: scaleX(0.72796);
  }
  100% {
    transform: scaleX(0.08);
  }
}
@keyframes mdc-linear-progress-primary-indeterminate-translate-reverse {
  0% {
    transform: translateX(0);
  }
  20% {
    animation-timing-function: cubic-bezier(0.5, 0, 0.701732, 0.495819);
    transform: translateX(0);
  }
  59.15% {
    animation-timing-function: cubic-bezier(0.302435, 0.381352, 0.55, 0.956352);
    transform: translateX(-83.67142%);
  }
  100% {
    transform: translateX(-200.611057%);
  }
}
@keyframes mdc-linear-progress-secondary-indeterminate-translate-reverse {
  0% {
    animation-timing-function: cubic-bezier(0.15, 0, 0.515058, 0.409685);
    transform: translateX(0);
  }
  25% {
    animation-timing-function: cubic-bezier(0.31033, 0.284058, 0.8, 0.733712);
    transform: translateX(-37.651913%);
  }
  48.35% {
    animation-timing-function: cubic-bezier(0.4, 0.627035, 0.6, 0.902026);
    transform: translateX(-84.386165%);
  }
  100% {
    transform: translateX(-160.277782%);
  }
}
@keyframes mdc-linear-progress-buffering-reverse {
  from {
    transform: translateX(-10px);
  }
}
`],encapsulation:2,changeDetection:0})}return i})();function kh(i,n=0,e=100){return Math.max(n,Math.min(e,i))}var Sh=(()=>{class i{static \u0275fac=function(t){return new(t||i)};static \u0275mod=$({type:i});static \u0275inj=G({imports:[fe]})}return i})();function Z0(i,n){if(i&1&&(P(0),O(1,"translate")),i&2){let e=m(2);Dd(" ",L(1,3,"PROGRESS_BAR.STEP")," ",e.index()+1," / ",e.count()," ")}}function Q0(i,n){if(i&1&&(P(0),O(1,"number")),i&2){let e=m(2);Te(" ",Bt(1,1,e.progressValue(),"1.0-0")," % ")}}function X0(i,n){if(i&1&&(p(0,"div",2),v(1,Z0,2,5)(2,Q0,2,4),h()),i&2){let e=m();w("right",!e.count()),c(),y(e.count()?1:2)}}var J0=100,Ih=(()=>{class i{constructor(){this.hideCounter=C(!1),this.index=C(),this.count=C(),this.percent=C(void 0),this.progressValue=k(()=>this.index()!==void 0||this.count()!==void 0?this.getProgressValue(this.index(),this.count()):this.percent())}getProgressValue(e,t){return(e+1)/t*J0}static{this.\u0275fac=function(t){return new(t||i)}}static{this.\u0275cmp=x({type:i,selectors:[["vitamui-common-progress-bar"]],inputs:{hideCounter:[1,"hideCounter"],index:[1,"index"],count:[1,"count"],percent:[1,"percent"]},decls:2,vars:2,consts:[[1,"text","caption","bold","primary","steps",3,"right"],["mode","determinate",3,"value"],[1,"text","caption","bold","primary","steps"]],template:function(t,r){t&1&&(v(0,X0,3,3,"div",0),_(1,"mat-progress-bar",1)),t&2&&(y(r.hideCounter()?-1:0),c(),f("value",r.progressValue()))},dependencies:[ca,Sh,Ah,Od,ve],styles:[`.steps[_ngcontent-%COMP%]{margin:0 0 5px}.right[_ngcontent-%COMP%]{text-align:right}
/*# sourceMappingURL=/vitam-ui/develop/common-progress-bar.component-2QEGGFR5.css.map */`]})}}return i})();function eC(i,n){if(i&1&&_(0,"vitamui-common-progress-bar",1),i&2){let e=m();f("index",e.stepper().selectedIndex)("count",e.stepper().steps.length)}}function tC(i,n){if(i&1&&(p(0,"div",2),P(1),h()),i&2){let e=m();c(),ce(e.subhead())}}function nC(i,n){if(i&1&&_(0,"div",3),i&2){let e=m();f("innerHTML",e.getTitle(),ea)}}var Oh=(()=>{class i{constructor(){this.stepper=C(void 0),this.title=C(void 0),this.subhead=C(void 0)}getTitle(){let e=this.stepper(),t=this.title();return Array.isArray(t)?e?t[e.selectedIndex]??`MISSING TITLE AT INDEX ${e.selectedIndex}`:"TITLE SHOULDN'T BE AN ARRAY":t}static{this.\u0275fac=function(t){return new(t||i)}}static{this.\u0275cmp=x({type:i,selectors:[["vitamui-dialog-header"]],inputs:{stepper:[1,"stepper"],title:[1,"title"],subhead:[1,"subhead"]},decls:4,vars:5,consts:[["mat-dialog-title",""],[3,"index","count"],[1,"text","medium","light"],[1,"text","large","bold",3,"innerHTML"]],template:function(t,r){if(t&1&&(p(0,"div",0),v(1,eC,1,2,"vitamui-common-progress-bar",1),v(2,tC,2,1,"div",2),v(3,nC,1,1,"div",3),h()),t&2){let a;w("with-progress-bar",r.stepper()&&r.stepper().steps.length),c(),y(!((a=r.stepper())==null||a.steps==null)&&a.steps.length?1:-1),c(),y(r.subhead()?2:-1),c(),y(r.title()?3:-1)}},dependencies:[kc,Mc,Ih],styles:[`.gap-0[_ngcontent-%COMP%]:not(.row){display:flex;gap:0;flex-direction:column;align-items:flex-start}.hgap-0[_ngcontent-%COMP%]:not(.row){display:flex;gap:0;flex-direction:row;align-items:center}.gap-0.row[_ngcontent-%COMP%]{row-gap:0}.gap-1[_ngcontent-%COMP%]:not(.row){display:flex;gap:5px;flex-direction:column;align-items:flex-start}.hgap-1[_ngcontent-%COMP%]:not(.row){display:flex;gap:5px;flex-direction:row;align-items:center}.gap-1.row[_ngcontent-%COMP%]{row-gap:5px}.gap-2[_ngcontent-%COMP%]:not(.row){display:flex;gap:10px;flex-direction:column;align-items:flex-start}.hgap-2[_ngcontent-%COMP%]:not(.row){display:flex;gap:10px;flex-direction:row;align-items:center}.gap-2.row[_ngcontent-%COMP%]{row-gap:10px}.gap-3[_ngcontent-%COMP%]:not(.row){display:flex;gap:15px;flex-direction:column;align-items:flex-start}.hgap-3[_ngcontent-%COMP%]:not(.row){display:flex;gap:15px;flex-direction:row;align-items:center}.gap-3.row[_ngcontent-%COMP%]{row-gap:15px}.gap-4[_ngcontent-%COMP%]:not(.row){display:flex;gap:20px;flex-direction:column;align-items:flex-start}.hgap-4[_ngcontent-%COMP%]:not(.row){display:flex;gap:20px;flex-direction:row;align-items:center}.gap-4.row[_ngcontent-%COMP%]{row-gap:20px}.gap-5[_ngcontent-%COMP%]:not(.row){display:flex;gap:30px;flex-direction:column;align-items:flex-start}.hgap-5[_ngcontent-%COMP%]:not(.row){display:flex;gap:30px;flex-direction:row;align-items:center}.gap-5.row[_ngcontent-%COMP%]{row-gap:30px}.gap-6[_ngcontent-%COMP%]:not(.row){display:flex;gap:40px;flex-direction:column;align-items:flex-start}.hgap-6[_ngcontent-%COMP%]:not(.row){display:flex;gap:40px;flex-direction:row;align-items:center}.gap-6.row[_ngcontent-%COMP%]{row-gap:40px}.gap-7[_ngcontent-%COMP%]:not(.row){display:flex;gap:50px;flex-direction:column;align-items:flex-start}.hgap-7[_ngcontent-%COMP%]:not(.row){display:flex;gap:50px;flex-direction:row;align-items:center}.gap-7.row[_ngcontent-%COMP%]{row-gap:50px}.gap-8[_ngcontent-%COMP%]:not(.row){display:flex;gap:60px;flex-direction:column;align-items:flex-start}.hgap-8[_ngcontent-%COMP%]:not(.row){display:flex;gap:60px;flex-direction:row;align-items:center}.gap-8.row[_ngcontent-%COMP%]{row-gap:60px}.m-0[_ngcontent-%COMP%]{margin:0!important}.mt-0[_ngcontent-%COMP%], .my-0[_ngcontent-%COMP%]{margin-top:0!important}.mr-0[_ngcontent-%COMP%], .mx-0[_ngcontent-%COMP%]{margin-right:0!important}.mb-0[_ngcontent-%COMP%], .my-0[_ngcontent-%COMP%]{margin-bottom:0!important}.ml-0[_ngcontent-%COMP%], .mx-0[_ngcontent-%COMP%]{margin-left:0!important}.m-1[_ngcontent-%COMP%]{margin:5px!important}.mt-1[_ngcontent-%COMP%], .my-1[_ngcontent-%COMP%]{margin-top:5px!important}.mr-1[_ngcontent-%COMP%], .mx-1[_ngcontent-%COMP%]{margin-right:5px!important}.mb-1[_ngcontent-%COMP%], .my-1[_ngcontent-%COMP%]{margin-bottom:5px!important}.ml-1[_ngcontent-%COMP%], .mx-1[_ngcontent-%COMP%]{margin-left:5px!important}.m-2[_ngcontent-%COMP%]{margin:10px!important}.mt-2[_ngcontent-%COMP%], .my-2[_ngcontent-%COMP%]{margin-top:10px!important}.mr-2[_ngcontent-%COMP%], .mx-2[_ngcontent-%COMP%]{margin-right:10px!important}.mb-2[_ngcontent-%COMP%], .my-2[_ngcontent-%COMP%]{margin-bottom:10px!important}.ml-2[_ngcontent-%COMP%], .mx-2[_ngcontent-%COMP%]{margin-left:10px!important}.m-3[_ngcontent-%COMP%]{margin:15px!important}.mt-3[_ngcontent-%COMP%], .my-3[_ngcontent-%COMP%]{margin-top:15px!important}.mr-3[_ngcontent-%COMP%], .mx-3[_ngcontent-%COMP%]{margin-right:15px!important}.mb-3[_ngcontent-%COMP%], .my-3[_ngcontent-%COMP%]{margin-bottom:15px!important}.ml-3[_ngcontent-%COMP%], .mx-3[_ngcontent-%COMP%]{margin-left:15px!important}.m-4[_ngcontent-%COMP%]{margin:20px!important}.mt-4[_ngcontent-%COMP%], .my-4[_ngcontent-%COMP%]{margin-top:20px!important}.mr-4[_ngcontent-%COMP%], .mx-4[_ngcontent-%COMP%]{margin-right:20px!important}.mb-4[_ngcontent-%COMP%], vitamui-common-progress-bar[_ngcontent-%COMP%], .my-4[_ngcontent-%COMP%]{margin-bottom:20px!important}.ml-4[_ngcontent-%COMP%], .mx-4[_ngcontent-%COMP%]{margin-left:20px!important}.m-5[_ngcontent-%COMP%]{margin:30px!important}.mt-5[_ngcontent-%COMP%], .my-5[_ngcontent-%COMP%]{margin-top:30px!important}.mr-5[_ngcontent-%COMP%], .mx-5[_ngcontent-%COMP%]{margin-right:30px!important}.mb-5[_ngcontent-%COMP%], .my-5[_ngcontent-%COMP%]{margin-bottom:30px!important}.ml-5[_ngcontent-%COMP%], .mx-5[_ngcontent-%COMP%]{margin-left:30px!important}.m-6[_ngcontent-%COMP%]{margin:40px!important}.mt-6[_ngcontent-%COMP%], .my-6[_ngcontent-%COMP%]{margin-top:40px!important}.mr-6[_ngcontent-%COMP%], .mx-6[_ngcontent-%COMP%]{margin-right:40px!important}.mb-6[_ngcontent-%COMP%], .my-6[_ngcontent-%COMP%]{margin-bottom:40px!important}.ml-6[_ngcontent-%COMP%], .mx-6[_ngcontent-%COMP%]{margin-left:40px!important}.m-7[_ngcontent-%COMP%]{margin:50px!important}.mt-7[_ngcontent-%COMP%], .my-7[_ngcontent-%COMP%]{margin-top:50px!important}.mr-7[_ngcontent-%COMP%], .mx-7[_ngcontent-%COMP%]{margin-right:50px!important}.mb-7[_ngcontent-%COMP%], .my-7[_ngcontent-%COMP%]{margin-bottom:50px!important}.ml-7[_ngcontent-%COMP%], .mx-7[_ngcontent-%COMP%]{margin-left:50px!important}.m-8[_ngcontent-%COMP%]{margin:60px!important}.mt-8[_ngcontent-%COMP%], .my-8[_ngcontent-%COMP%]{margin-top:60px!important}.mr-8[_ngcontent-%COMP%], .mx-8[_ngcontent-%COMP%]{margin-right:60px!important}.mb-8[_ngcontent-%COMP%], .my-8[_ngcontent-%COMP%]{margin-bottom:60px!important}.ml-8[_ngcontent-%COMP%], .mx-8[_ngcontent-%COMP%]{margin-left:60px!important}.p-0[_ngcontent-%COMP%]{padding:0!important}.pt-0[_ngcontent-%COMP%], .py-0[_ngcontent-%COMP%]{padding-top:0!important}.pr-0[_ngcontent-%COMP%], .px-0[_ngcontent-%COMP%]{padding-right:0!important}.pb-0[_ngcontent-%COMP%], .py-0[_ngcontent-%COMP%]{padding-bottom:0!important}.pl-0[_ngcontent-%COMP%], .px-0[_ngcontent-%COMP%]{padding-left:0!important}.p-1[_ngcontent-%COMP%]{padding:5px!important}.pt-1[_ngcontent-%COMP%], .py-1[_ngcontent-%COMP%]{padding-top:5px!important}.pr-1[_ngcontent-%COMP%], .px-1[_ngcontent-%COMP%]{padding-right:5px!important}.pb-1[_ngcontent-%COMP%], .py-1[_ngcontent-%COMP%]{padding-bottom:5px!important}.pl-1[_ngcontent-%COMP%], .px-1[_ngcontent-%COMP%]{padding-left:5px!important}.p-2[_ngcontent-%COMP%]{padding:10px!important}.pt-2[_ngcontent-%COMP%], .py-2[_ngcontent-%COMP%]{padding-top:10px!important}.pr-2[_ngcontent-%COMP%], .px-2[_ngcontent-%COMP%]{padding-right:10px!important}.pb-2[_ngcontent-%COMP%], .py-2[_ngcontent-%COMP%]{padding-bottom:10px!important}.pl-2[_ngcontent-%COMP%], .px-2[_ngcontent-%COMP%]{padding-left:10px!important}.p-3[_ngcontent-%COMP%]{padding:15px!important}.pt-3[_ngcontent-%COMP%], .py-3[_ngcontent-%COMP%]{padding-top:15px!important}.pr-3[_ngcontent-%COMP%], .px-3[_ngcontent-%COMP%]{padding-right:15px!important}.pb-3[_ngcontent-%COMP%], .py-3[_ngcontent-%COMP%]{padding-bottom:15px!important}.pl-3[_ngcontent-%COMP%], .px-3[_ngcontent-%COMP%]{padding-left:15px!important}.p-4[_ngcontent-%COMP%]{padding:20px!important}.pt-4[_ngcontent-%COMP%], .py-4[_ngcontent-%COMP%]{padding-top:20px!important}.pr-4[_ngcontent-%COMP%], .px-4[_ngcontent-%COMP%]{padding-right:20px!important}.pb-4[_ngcontent-%COMP%], .py-4[_ngcontent-%COMP%]{padding-bottom:20px!important}.pl-4[_ngcontent-%COMP%], .px-4[_ngcontent-%COMP%]{padding-left:20px!important}.p-5[_ngcontent-%COMP%]{padding:30px!important}.pt-5[_ngcontent-%COMP%], .py-5[_ngcontent-%COMP%]{padding-top:30px!important}.pr-5[_ngcontent-%COMP%], .px-5[_ngcontent-%COMP%]{padding-right:30px!important}.pb-5[_ngcontent-%COMP%], .py-5[_ngcontent-%COMP%]{padding-bottom:30px!important}.pl-5[_ngcontent-%COMP%], .px-5[_ngcontent-%COMP%]{padding-left:30px!important}.p-6[_ngcontent-%COMP%]{padding:40px!important}.pt-6[_ngcontent-%COMP%], .py-6[_ngcontent-%COMP%]{padding-top:40px!important}.pr-6[_ngcontent-%COMP%], .px-6[_ngcontent-%COMP%]{padding-right:40px!important}.pb-6[_ngcontent-%COMP%], .py-6[_ngcontent-%COMP%]{padding-bottom:40px!important}.pl-6[_ngcontent-%COMP%], .px-6[_ngcontent-%COMP%]{padding-left:40px!important}.p-7[_ngcontent-%COMP%]{padding:50px!important}.pt-7[_ngcontent-%COMP%], .py-7[_ngcontent-%COMP%]{padding-top:50px!important}.pr-7[_ngcontent-%COMP%], .px-7[_ngcontent-%COMP%]{padding-right:50px!important}.pb-7[_ngcontent-%COMP%], .py-7[_ngcontent-%COMP%]{padding-bottom:50px!important}.pl-7[_ngcontent-%COMP%], .px-7[_ngcontent-%COMP%]{padding-left:50px!important}.p-8[_ngcontent-%COMP%]{padding:60px!important}.pt-8[_ngcontent-%COMP%], .py-8[_ngcontent-%COMP%]{padding-top:60px!important}.pr-8[_ngcontent-%COMP%], .px-8[_ngcontent-%COMP%]{padding-right:60px!important}.pb-8[_ngcontent-%COMP%], .py-8[_ngcontent-%COMP%]{padding-bottom:60px!important}.pl-8[_ngcontent-%COMP%], .px-8[_ngcontent-%COMP%]{padding-left:60px!important}[_nghost-%COMP%]{display:block}.with-progress-bar[_ngcontent-%COMP%]{margin-top:-21px}vitamui-common-progress-bar[_ngcontent-%COMP%]{display:block}
/*# sourceMappingURL=/vitam-ui/develop/dialog-header.component-5RMEFB74.css.map */`]})}}return i})();var iC=["removeDialog"],rC=i=>({empty:i});function aC(i,n){if(i&1&&(p(0,"span"),P(1),O(2,"translate"),O(3,"empty"),h()),i&2){let e=m(2);c(),Fn("",L(2,2,e.favoritePath),": ",L(3,4,e.favoriteEntry[1]))}}function oC(i,n){if(i&1){let e=ze();p(0,"button",10),T("click",function(){let r=J(e).$implicit;return ee(r.handler())}),P(1),O(2,"translate"),h()}if(i&2){let e=n.$implicit;c(),ce(L(2,1,e.label))}}function sC(i,n){if(i&1){let e=ze();p(0,"vitamui-menu-button",8),T("click",function(r){J(e);let a=m(2);return ee(a.stopPropagation(r))}),Be(1,oC,3,3,"button",9,at),h()}if(i&2){let e=m(2);c(),je(e.actionList)}}function lC(i,n){i&1&&_(0,"div")}function dC(i,n){if(i&1&&(p(0,"vitamui-common-accordion",2)(1,"span",3),P(2),O(3,"translate"),O(4,"empty"),O(5,"appendStar"),h(),p(6,"div",4),v(7,aC,4,6,"span"),v(8,sC,3,0,"vitamui-menu-button",5),h(),p(9,"div",6),Oe(10,lC,1,0,"div",7),h()()),i&2){let e,t=m(),r=Ke(3);f("opened",t.editObject().open),c(),f("ngClass",sa(13,rC,t.isEmptyObject((e=t.editObject())==null?null:e.value))),c(),ce(Bt(5,10,L(4,8,L(3,6,t.editObject().displayRule.ui.label)),t.editObject().required)),c(5),y(t.favoriteEntry?7:-1),c(),y(t.actionList.length?8:-1),c(2),f("ngTemplateOutlet",r)}}function cC(i,n){i&1&&_(0,"div")}function uC(i,n){if(i&1&&_(0,"vitamui-common-group-editor",13),i&2){let e=m(2).$implicit;f("editObject",e)}}function mC(i,n){if(i&1&&_(0,"vitamui-common-list-editor",13),i&2){let e=m(2).$implicit;f("editObject",e)}}function pC(i,n){if(i&1&&_(0,"vitamui-common-primitive-editor",13),i&2){let e=m(2).$implicit;f("editObject",e)}}function hC(i,n){if(i&1&&v(0,uC,1,1,"vitamui-common-group-editor",13)(1,mC,1,1,"vitamui-common-list-editor",13)(2,pC,1,1,"vitamui-common-primitive-editor",13),i&2){let e,t=m().$implicit,r=m(3);y((e=t.type)===r.DisplayObjectType.GROUP?0:e===r.DisplayObjectType.LIST?1:e===r.DisplayObjectType.PRIMITIVE?2:-1)}}function fC(i,n){if(i&1&&(p(0,"div",12),v(1,cC,1,0,"div"),v(2,hC,3,1),h()),i&2){let e=n.$implicit;c(),y(e?-1:1),c(),y(e?2:-1)}}function gC(i,n){if(i&1&&(p(0,"div",11),Be(1,fC,3,2,"div",12,at),h()),i&2){let e=n.$implicit;c(),je(e)}}function _C(i,n){if(i&1&&(p(0,"div"),P(1),O(2,"empty"),h()),i&2){let e=m(2);c(),ce(L(2,1,e.editObject().value))}}function bC(i,n){if(i&1&&(Be(0,gC,3,0,"div",11,at),v(2,_C,3,3,"div")),i&2){let e=m();je(e.rows),c(2),y(e.editObject()&&(e.editObject().value===null||e.editObject().value===void 0)?2:-1)}}function vC(i,n){i&1&&_(0,"div")}function yC(i,n){if(i&1&&_(0,"vitamui-common-group-editor",13),i&2){let e=m(2).$implicit;f("editObject",e)}}function CC(i,n){if(i&1&&_(0,"vitamui-common-list-editor",13),i&2){let e=m(2).$implicit;f("editObject",e)}}function xC(i,n){if(i&1&&_(0,"vitamui-common-primitive-editor",13),i&2){let e=m(2).$implicit;f("editObject",e)}}function DC(i,n){if(i&1&&v(0,yC,1,1,"vitamui-common-group-editor",13)(1,CC,1,1,"vitamui-common-list-editor",13)(2,xC,1,1,"vitamui-common-primitive-editor",13),i&2){let e,t=m().$implicit,r=m(3);y((e=t.type)===r.DisplayObjectType.GROUP?0:e===r.DisplayObjectType.LIST?1:e===r.DisplayObjectType.PRIMITIVE?2:-1)}}function MC(i,n){if(i&1&&(p(0,"div",12),v(1,vC,1,0,"div"),v(2,DC,3,1),h()),i&2){let e=n.$implicit;c(),y(e?-1:1),c(),y(e?2:-1)}}function wC(i,n){if(i&1&&(p(0,"div",11),Be(1,MC,3,2,"div",12,at),h()),i&2){let e=n.$implicit;c(),je(e)}}function EC(i,n){if(i&1&&(p(0,"div"),P(1),O(2,"empty"),h()),i&2){let e=m(2);c(),ce(L(2,1,e.editObject().value))}}function kC(i,n){if(i&1&&(Be(0,wC,3,0,"div",11,at),v(2,EC,3,3,"div")),i&2){let e=m();je(e.rows),c(2),y(e.editObject()&&(e.editObject().value===null||e.editObject().value===void 0)?2:-1)}}function AC(i,n){i&1&&(_(0,"vitamui-dialog-header",14),O(1,"translate"),O(2,"translate"),p(3,"mat-dialog-actions")(4,"button",15),P(5),O(6,"translate"),h(),p(7,"button",16),P(8),O(9,"translate"),h()()),i&2&&(f("subhead",L(1,5,"ARCHIVE_UNIT.DIALOGS.REMOVE.TITLE"))("title",L(2,7,"ARCHIVE_UNIT.DIALOGS.REMOVE.CONTENT")),c(4),f("matDialogClose",!0),c(),Te(" ",L(6,9,"ARCHIVE_UNIT.DIALOGS.REMOVE.BUTTONS.CONFIRM")," "),c(3),ce(L(9,11,"ARCHIVE_UNIT.DIALOGS.REMOVE.BUTTONS.CANCEL")))}var NT=(()=>{class i{constructor(){this.layoutService=l(Hd),this.favoriteEntryService=l(ha),this.typeService=l(zd),this.matDialog=l(hr),this.cdr=l(ne),this.editObject=C(void 0),this.removeDialog=St("removeDialog"),this.entries=[],this.rows=[[]],this.actionList=[],this.DisplayObjectType=pa,this.dialogConfig={autoFocus:!1}}ngOnChanges(e){let{editObject:t}=e;if(t){let r=this.editObject();r?.childrenChange&&(this.subscription&&this.subscription.unsubscribe(),this.subscription=r.childrenChange.subscribe(()=>{this.computeLayout(),this.computeActions(),this.isEmptyObject(t.currentValue),this.cdr.markForCheck()})),this.computeLayout()}}ngAfterViewInit(){this.computeActions(),this.cdr.markForCheck()}ngOnDestroy(){this.subscription&&this.subscription.unsubscribe()}computeLayout(){this.favoriteEntry=this.favoriteEntryService.favoriteEntry(this.editObject()),this.favoritePath=this.favoriteEntryService.favoritePath(this.editObject()),this.rows=this.layoutService.compute(this.editObject())}computeActions(){let e=this.editObject();if(!e?.actions)return;let t=e.actions.remove;if(t){let r=this.withValidation(t,()=>this.typeService.isConsistent(this.editObject().control.value));this.replaceAction(t,r)}this.actionList=Object.values(e.actions)}toggle(){this.editObject().open=!this.editObject().open}stopPropagation(e){e.stopPropagation()}isEmptyObject(e){return Object.values(e).every(t=>t===null||Array.isArray(t)&&t.length===0||typeof t=="object"&&Object.keys(t).length===0)}withValidation(e,t){let{label:r,handler:a}=e;return{name:`${e.name}WithValidation`,label:r,handler:()=>{if(!t())return a();let o=this.matDialog.open(this.removeDialog(),this.dialogConfig).afterClosed().pipe(De(s=>s),an(()=>Ie(a()))).subscribe(()=>{this.cdr.markForCheck(),o.unsubscribe()})}}}replaceAction(e,t){delete this.editObject().actions[e.name],this.editObject().actions[t.name]=t}static{this.\u0275fac=function(t){return new(t||i)}}static{this.\u0275cmp=x({type:i,selectors:[["vitamui-common-group-editor"]],viewQuery:function(t,r){t&1&&ni(r.removeDialog,iC,5),t&2&&Tn()},inputs:{editObject:[1,"editObject"]},features:[pe],decls:6,vars:1,consts:[["content",""],["removeDialog",""],[3,"opened"],[1,"title",3,"ngClass"],[1,"title-extra","v-col","align-items-center","justify-content-end","favorite-entry"],["overlayPos","end","icon","vitamui-icon-more-vert","menuButtonSize","small","menuColor","secondary"],[1,"content"],[4,"ngTemplateOutlet"],["overlayPos","end","icon","vitamui-icon-more-vert","menuButtonSize","small","menuColor","secondary",3,"click"],["type","button","mat-menu-item",""],["type","button","mat-menu-item","",3,"click"],[1,"v-row"],[1,"v-col"],[3,"editObject"],[3,"subhead","title"],[1,"btn","primary","btn-confirm-dialog",3,"matDialogClose"],["matDialogClose","",1,"btn","link","cancel","cancel-popup"]],template:function(t,r){if(t&1&&(v(0,dC,11,15,"vitamui-common-accordion",2)(1,bC,3,1),Oe(2,kC,3,1,"ng-template",null,0,Qt)(4,AC,10,13,"ng-template",null,1,Qt)),t&2){let a;y((a=r.editObject())!=null&&a.key?0:1)}},dependencies:[i,Ud,ri,jc,jn,ln,Eh,No,Oh,wc,xc,tt,Lo,ve],styles:[`[_nghost-%COMP%]{width:100%;display:flex;gap:10px;flex-direction:column}.v-col[_ngcontent-%COMP%]{display:flex;flex:1}.v-row[_ngcontent-%COMP%]{display:flex;flex-direction:row;flex-wrap:wrap;width:100%;gap:10px}.empty[_ngcontent-%COMP%]{color:var(--vitamui-grey)}
/*# sourceMappingURL=/vitam-ui/develop/group-editor.component-EV2FBWCN.css.map */`]})}}return i})();export{Vn as a,Nn as b,It as c,Jo as d,es as e,_x as f,_a as g,va as h,ya as i,ns as j,is as k,or as l,cc as m,Xx as n,ye as o,mr as p,ss as q,kD as r,pr as s,cf as t,uf as u,hr as v,xc as w,dM as x,wc as y,kc as z,Ah as A,Sh as B,Ih as C,Oh as D,Rr as E,dt as F,vi as G,Jt as H,de as I,xt as J,Xe as K,i1 as L,Ge as M,Hn as N,a1 as O,pg as P,et as Q,$a as R,ct as S,Cg as T,o1 as U,yn as V,nt as W,tb as X,dm as Y,gr as Z,fs as _,Aa as $,_n as aa,Oa as ba,hi as ca,vs as da,aw as ea,Ta as fa,Ai as ga,Zs as ha,__ as ia,b_ as ja,nE as ka,Ku as la,x_ as ma,iE as na,rE as oa,aE as pa,oE as qa,sE as ra,lE as sa,dE as ta,co as ua,lm as va,sk as wa,fu as xa,Ci as ya,Qa as za,kr as Aa,ut as Ba,am as Ca,om as Da,Ii as Ea,oS as Fa,mt as Ga,Fi as Ha,Fm as Ia,cm as Ja,Oi as Ka,pm as La,ml as Ma,fm as Na,GA as Oa,gm as Pa,jn as Qa,gi as Ra,Lc as Sa,Bc as Ta,jc as Ua,wr as Va,qt as Wa,Wn as Xa,mo as Ya,po as Za,ho as _a,Ro as $a,fh as ab,gh as bb,$r as cb,No as db,bh as eb,vh as fb,yh as gb,Ch as hb,Eh as ib,NT as jb};
//# sourceMappingURL=chunk-DITVZEK6.js.map
