(window.webpackJsonp=window.webpackJsonp||[]).push([[11],{cRhG:function(e,i,t){"use strict";t.r(i),t.d(i,"ProfileModule",(function(){return me}));var o=t("2kYt"),r=t("nIj0"),n=t("jdnZ"),l=t("Jb3d"),a=t("66mq"),c=t("lQ7A"),s=t("OWX3"),b=t("PCNd"),m=t("l7x+"),u=t("29Wa"),d=t("Cd2c"),p=t("R7+U"),f=t("W1gw"),S=t("EM62");let O=(()=>{class e{}return e.\u0275mod=S.Lb({type:e}),e.\u0275inj=S.Kb({factory:function(i){return new(i||e)},imports:[[o.c,b.a,n.c,u.d,d.c,a.b,p.b,f.c,r.k,r.v,m.a,s.xb,s.gb]]}),e})();var E=t("KZIX"),h=t("Y2X+");let v=(()=>{class e{}return e.\u0275mod=S.Lb({type:e}),e.\u0275inj=S.Kb({factory:function(i){return new(i||e)},imports:[[o.c,b.a,l.b,E.c,h.b,r.v,s.xb,s.gb]]}),e})();var R=t("csyo");let T=(()=>{class e{}return e.\u0275mod=S.Lb({type:e}),e.\u0275inj=S.Kb({factory:function(i){return new(i||e)},imports:[[o.c,b.a,R.a,s.xb]]}),e})();var I=t("sEIs"),P=t("4Xmu"),g=t("ROBh"),N=t("YtkY"),_=t("xVbo"),F=t("TLy2"),A=t("4e/d"),U=t("6WTZ"),L=t("8lHc"),C=t("J+dc");let M=(()=>{class e{constructor(e){this.rngProfileService=e,this.debounceTime=400,this.nameExists=(e,i,t,o)=>r=>Object(L.a)(this.debounceTime).pipe(Object(F.a)(()=>r.value!==o?this.rngProfileService.exists(e,i,t,r.value):Object(g.a)(!1)),Object(C.a)(1),Object(N.a)(e=>e?{nameExists:!0}:null))}}return e.\u0275fac=function(i){return new(i||e)(S.bc(P.a))},e.\u0275prov=S.Jb({token:e,factory:e.\u0275fac,providedIn:"root"}),e})();var B=t("s2Ay");function j(e,i){1&e&&(S.Tb(0,"span"),S.Oc(1),S.jc(2,"translate"),S.Sb()),2&e&&(S.Bb(1),S.Pc(S.kc(2,1,"USER_PROFILE.INFORMATIONS.INACTIVE_SWITCH")))}function k(e,i){1&e&&(S.Tb(0,"span"),S.Oc(1),S.jc(2,"translate"),S.Sb()),2&e&&(S.Bb(1),S.Pc(S.kc(2,1,"USER_PROFILE.INFORMATIONS.ACTIVE_SWITCH")))}let y=(()=>{class e{constructor(e,i,t,o){this.formBuilder=e,this.rngProfileService=i,this.profileValidators=t,this.authService=o,this.roleEnum=s.db,this.form=this.formBuilder.group({name:[null,r.x.required],identifier:[{value:null,disabled:!0},r.x.required],description:[null,r.x.required],enabled:!1,level:[null,Object(s.Gb)(this.authService.user)],roles:[]}),this.updateFormSub=this.form.valueChanges.pipe(Object(N.a)(()=>Object(s.Ib)(this.form.value,this.previousValue)),Object(_.a)(e=>!Object(U.c)(e)),Object(N.a)(e=>this.completeRoles(e)),Object(N.a)(e=>Object(U.a)({id:this.profile.id,customerId:this.profile.customerId,tenantIdentifier:this.profile.tenantIdentifier},e)),Object(F.a)(e=>this.rngProfileService.patch(e).pipe(Object(A.a)(()=>Object(g.a)(null))))).subscribe(e=>{this.resetForm(this.form,e,this.readOnly)})}ngOnInit(){this.userLevel=this.authService.user.level}completeRoles(e){const i=this.completeUpdateRoles(e);return this.completeCreateRoles(i)}completeUpdateRoles(e){if(e.roles){const i=[s.db.ROLE_MFA_USERS.toString(),s.db.ROLE_UPDATE_STANDARD_USERS.toString()],t=e.roles.some(e=>i.includes(e.name)),o=e.roles.findIndex(e=>e.name===s.db.ROLE_UPDATE_USERS),r=e.roles.findIndex(e=>e.name===s.db.ROLE_UPDATE_USER_INFOS);t?(-1===o&&e.roles.push({name:s.db.ROLE_UPDATE_USERS}),-1===r&&e.roles.push({name:s.db.ROLE_UPDATE_USER_INFOS})):(-1!==o&&e.roles.splice(o,1),-1!==r&&e.roles.splice(r,1))}return e}completeCreateRoles(e){if(e.roles){const i=[s.db.ROLE_CREATE_USERS.toString()],t=e.roles.some(e=>i.includes(e.name)),o=e.roles.findIndex(e=>e.name===s.db.ROLE_CREATE_USER_INFOS);t?-1===o&&e.roles.push({name:s.db.ROLE_CREATE_USER_INFOS}):-1!==o&&e.roles.splice(o,1)}return e}ngOnDestroy(){this.updateFormSub.unsubscribe()}ngOnChanges(e){(e.hasOwnProperty("profile")||e.hasOwnProperty("readOnly"))&&this.profile&&(this.resetForm(this.form,this.profile,this.readOnly),this.previousValue=this.form.value)}resetForm(e,i,t){e.reset(i,{emitEvent:!1}),this.initFormValidators(e,i),this.initCustomFormActivationState(e,i,t),e.updateValueAndValidity({emitEvent:!1})}initFormValidators(e,i){e.get("name").setAsyncValidators(this.profileValidators.nameExists(i.tenantIdentifier,i.level,i.applicationName,i.name))}initCustomFormActivationState(e,i,t){this.initFormActivationState(e,t),e.get("identifier").disable({emitEvent:!1}),i.groupsCount&&i.groupsCount>0&&(this.form.get("enabled").disable({emitEvent:!1}),this.form.get("level").disable({emitEvent:!1}))}initFormActivationState(e,i){i?e.disable({emitEvent:!1}):e.enable({emitEvent:!1})}}return e.\u0275fac=function(i){return new(i||e)(S.Nb(r.d),S.Nb(P.a),S.Nb(M),S.Nb(s.k))},e.\u0275cmp=S.Hb({type:e,selectors:[["app-information-tab"]],inputs:{profile:"profile",readOnly:"readOnly"},features:[S.zb],decls:61,vars:69,consts:[[3,"formGroup"],["formControlName","enabled","matTooltipClass","vitamui-tooltip",3,"matTooltip","matTooltipDisabled"],[4,"ngIf"],[1,"row"],[1,"col-12"],["formControlName","identifier","maxlength","12",3,"label","validator","asyncValidator"],["formControlName","name","maxlength","100",3,"label","validator","asyncValidator"],["errorKey","required"],["errorKey","nameExists"],[1,"col-6"],[1,"read-only-field","disabled-input"],["matTooltipClass","vitamui-tooltip",1,"col-6",3,"matTooltip","matTooltipDisabled"],["formControlName","level","maxlength","250",3,"prefix","label","validator"],["formControlName","description","maxlength","380",3,"label","validator"],[1,"col-6","my-4"],["formControlName","roles"],[3,"name"]],template:function(e,i){if(1&e&&(S.Tb(0,"form",0),S.Tb(1,"vitamui-common-slide-toggle",1),S.jc(2,"translate"),S.Mc(3,j,3,3,"span",2),S.Mc(4,k,3,3,"span",2),S.Sb(),S.Tb(5,"div",3),S.Tb(6,"div",4),S.Ob(7,"vitamui-common-editable-input",5),S.jc(8,"translate"),S.Sb(),S.Sb(),S.Tb(9,"div",3),S.Tb(10,"div",4),S.Tb(11,"vitamui-common-editable-input",6),S.jc(12,"translate"),S.Tb(13,"vitamui-common-field-error",7),S.Oc(14),S.jc(15,"translate"),S.Sb(),S.Tb(16,"vitamui-common-field-error",8),S.Oc(17),S.jc(18,"translate"),S.Sb(),S.Sb(),S.Sb(),S.Sb(),S.Tb(19,"div",3),S.Tb(20,"div",9),S.Tb(21,"div",10),S.Tb(22,"label"),S.Oc(23),S.jc(24,"translate"),S.Sb(),S.Tb(25,"div"),S.Oc(26),S.Sb(),S.Sb(),S.Sb(),S.Tb(27,"div",11),S.jc(28,"translate"),S.Tb(29,"vitamui-common-editable-level-input",12),S.jc(30,"translate"),S.Tb(31,"vitamui-common-field-error",7),S.Oc(32),S.jc(33,"translate"),S.Sb(),S.Tb(34,"vitamui-common-field-error",8),S.Oc(35),S.jc(36,"translate"),S.Sb(),S.Sb(),S.Sb(),S.Sb(),S.Tb(37,"div",3),S.Tb(38,"div",4),S.Tb(39,"vitamui-common-editable-input",13),S.jc(40,"translate"),S.Tb(41,"vitamui-common-field-error",7),S.Oc(42),S.jc(43,"translate"),S.Sb(),S.Sb(),S.Sb(),S.Tb(44,"div",14),S.Tb(45,"vitamui-common-role-toggle",15),S.Tb(46,"vitamui-common-role",16),S.Oc(47),S.jc(48,"translate"),S.Sb(),S.Tb(49,"vitamui-common-role",16),S.Oc(50),S.jc(51,"translate"),S.Sb(),S.Tb(52,"vitamui-common-role",16),S.Oc(53),S.jc(54,"translate"),S.Sb(),S.Tb(55,"vitamui-common-role",16),S.Oc(56),S.jc(57,"translate"),S.Sb(),S.Tb(58,"vitamui-common-role",16),S.Oc(59),S.jc(60,"translate"),S.Sb(),S.Sb(),S.Sb(),S.Sb(),S.Sb()),2&e){var t=null,o=null,r=null,n=null,l=null,a=null;S.oc("formGroup",i.form),S.Bb(1),S.oc("matTooltip",S.kc(2,35,"USER_PROFILE.INFORMATIONS.ACTIVE_SWITCH_TOOLTIP"))("matTooltipDisabled",0==(null==i.profile?null:i.profile.groupsCount)),S.Bb(2),S.oc("ngIf",!(null!=i.form&&i.form.get("enabled").value)),S.Bb(1),S.oc("ngIf",null==i.form?null:i.form.get("enabled").value),S.Bb(3),S.oc("label",S.kc(8,37,"COMMON.ID"))("validator",null==i.form||null==(t=i.form.get("identifier"))?null:t.validator)("asyncValidator",null==i.form||null==(o=i.form.get("identifier"))?null:o.asyncValidator),S.Bb(4),S.oc("label",S.kc(12,39,"USER_PROFILE.INFORMATIONS.NAME"))("validator",null==i.form||null==(r=i.form.get("name"))?null:r.validator)("asyncValidator",null==i.form||null==(n=i.form.get("name"))?null:n.asyncValidator),S.Bb(3),S.Pc(S.kc(15,41,"COMMON.ERROR.REQUIRED")),S.Bb(3),S.Pc(S.kc(18,43,"USER_PROFILE.INFORMATIONS.EXISTING_NAME")),S.Bb(6),S.Pc(S.kc(24,45,"USER_PROFILE.INFORMATIONS.LEVEL")),S.Bb(3),S.Pc(i.userLevel),S.Bb(1),S.oc("matTooltip",S.kc(28,47,"USER_PROFILE.INFORMATIONS.SUBLEVEL_TOOLTIP"))("matTooltipDisabled",0==(null==i.profile?null:i.profile.groupsCount)),S.Bb(2),S.oc("prefix",i.userLevel)("label",S.kc(30,49,"USER_PROFILE.INFORMATIONS.SUBLEVEL"))("validator",null==i.form||null==(l=i.form.get("level"))?null:l.validator),S.Bb(3),S.Pc(S.kc(33,51,"COMMON.ERROR.REQUIRED")),S.Bb(3),S.Pc(S.kc(36,53,"USER_PROFILE.INFORMATIONS.WRONG_FORMAT")),S.Bb(4),S.oc("label",S.kc(40,55,"USER_PROFILE.INFORMATIONS.DESCRIPTION"))("validator",null==i.form||null==(a=i.form.get("description"))?null:a.validator),S.Bb(3),S.Pc(S.kc(43,57,"COMMON.ERROR.REQUIRED")),S.Bb(4),S.oc("name",i.roleEnum.ROLE_MFA_USERS),S.Bb(1),S.Pc(S.kc(48,59,"USER_PROFILE.INFORMATIONS.STRONG_AUTHENT")),S.Bb(2),S.oc("name",i.roleEnum.ROLE_CREATE_USERS),S.Bb(1),S.Pc(S.kc(51,61,"USER_PROFILE.INFORMATIONS.USERS_CREATION")),S.Bb(2),S.oc("name",i.roleEnum.ROLE_UPDATE_STANDARD_USERS),S.Bb(1),S.Pc(S.kc(54,63,"USER_PROFILE.INFORMATIONS.MODIF_INFORMATIONS")),S.Bb(2),S.oc("name",i.roleEnum.ROLE_ANONYMIZATION_USERS),S.Bb(1),S.Pc(S.kc(57,65,"USER_PROFILE.INFORMATIONS.ANONYMOUS")),S.Bb(2),S.oc("name",i.roleEnum.ROLE_GENERIC_USERS),S.Bb(1),S.Qc(" ",S.kc(60,67,"USER_PROFILE.INFORMATIONS.USERS_MANAGEMENT"),"")}},directives:[r.y,r.r,r.i,s.ac,r.q,r.g,h.a,o.p,s.F,r.l,s.zb,s.G,s.fb,s.eb],pipes:[B.d],styles:[""]}),e})(),x=(()=>{class e{constructor(e,i,t){this.rngProfileService=e,this.authService=i,this.startupService=t,this.profileClose=new S.n}set id(e){this.rngProfileService.get(e).subscribe(e=>this.profile=e)}ngOnInit(){this.profileUpdateSub=this.rngProfileService.updated.subscribe(e=>{e&&this.rngProfileService.get(e.id).subscribe(e=>{this.profile=e})})}openPopup(){window.open(this.startupService.getConfigStringValue("UI_URL")+"/profile/"+this.profile.id,"detailPopup","width=584, height=713, resizable=no, location=no"),this.emitClose()}levelNotAllowed(){if(this.profile)return!Object(s.Kb)(this.authService.user,this.profile.level)}emitClose(){this.profileClose.emit()}ngOnDestroy(){this.profileUpdateSub.unsubscribe()}filterEvents(e){return e.outDetail&&(e.outDetail.includes("EXT_VITAMUI_CREATE_PROFILE")||e.outDetail.includes("EXT_VITAMUI_UPDATE_PROFILE"))}}return e.\u0275fac=function(i){return new(i||e)(S.Nb(P.a),S.Nb(s.k),S.Nb(s.lb))},e.\u0275cmp=S.Hb({type:e,selectors:[["app-profile-detail"]],inputs:{id:"id",profile:"profile",isPopup:"isPopup"},outputs:{profileClose:"profileClose"},decls:9,vars:14,consts:[[3,"title","icon","badge","onclose"],[1,"vitamui-sidepanel-body"],[1,"preview-tab-group"],[3,"label"],[3,"profile","readOnly"],["collectionName","profiles",3,"id","identifier","filter"]],template:function(e,i){1&e&&(S.Tb(0,"vitamui-common-sidenav-header",0),S.ec("onclose",(function(){return i.emitClose()})),S.Sb(),S.Tb(1,"div",1),S.Tb(2,"mat-tab-group",2),S.Tb(3,"mat-tab",3),S.jc(4,"translate"),S.Ob(5,"app-information-tab",4),S.Sb(),S.Tb(6,"mat-tab",3),S.jc(7,"translate"),S.Ob(8,"vitamui-common-operation-history-tab",5),S.Sb(),S.Sb(),S.Sb()),2&e&&(S.oc("title",null==i.profile?null:i.profile.name)("icon","vitamui-icon-gestion-de-profil")("badge",null!=i.profile&&i.profile.enabled?"green":"grey"),S.Bb(3),S.oc("label",S.kc(4,10,"USER_PROFILE.TAB.INFORMATIONS")),S.Bb(2),S.oc("profile",i.profile)("readOnly",i.isPopup||(null==i.profile?null:i.profile.readonly)||i.levelNotAllowed()),S.Bb(1),S.oc("label",S.kc(7,12,"USER_PROFILE.TAB.HISTORY")),S.Bb(2),S.oc("id",null==i.profile?null:i.profile.id)("identifier",null==i.profile?null:i.profile.identifier)("filter",i.filterEvents))},directives:[s.mc,E.b,E.a,y,s.Xb],pipes:[B.d],styles:[""]}),e})(),D=(()=>{class e{constructor(e){this.route=e,this.profile=this.route.snapshot.data.profile}ngOnInit(){}closePopup(){window.close()}}return e.\u0275fac=function(i){return new(i||e)(S.Nb(I.a))},e.\u0275cmp=S.Hb({type:e,selectors:[["app-profile-popup"]],decls:1,vars:2,consts:[[3,"profile","isPopup","profileClose"]],template:function(e,i){1&e&&(S.Tb(0,"app-profile-detail",0),S.ec("profileClose",(function(){return i.closePopup()})),S.Sb()),2&e&&S.oc("profile",i.profile)("isPopup",!0)},directives:[x],encapsulation:2}),e})(),w=(()=>{class e{constructor(e,i){this.rngProfileService=e,this.router=i}resolve(e){const i=e.paramMap.get("id");return this.rngProfileService.get(i).pipe(Object(C.a)(1),Object(N.a)(e=>e||(this.router.navigate(["/"]),null)))}}return e.\u0275fac=function(i){return new(i||e)(S.bc(P.a),S.bc(I.d))},e.\u0275prov=S.Jb({token:e,factory:e.\u0275fac,providedIn:"root"}),e})();var V=t("OZ4H"),H=t("O4oY");function G(e,i){1&e&&(S.Tb(0,"span"),S.Oc(1),S.jc(2,"translate"),S.Sb()),2&e&&(S.Bb(1),S.Pc(S.kc(2,1,"USER_PROFILE.INFORMATIONS.INACTIVE_SWITCH")))}function W(e,i){1&e&&(S.Tb(0,"span"),S.Oc(1),S.jc(2,"translate"),S.Sb()),2&e&&(S.Bb(1),S.Pc(S.kc(2,1,"USER_PROFILE.INFORMATIONS.ACTIVE_SWITCH")))}function q(e,i){1&e&&(S.Tb(0,"vitamui-common-input-error"),S.Oc(1),S.jc(2,"translate"),S.Sb()),2&e&&(S.Bb(1),S.Pc(S.kc(2,1,"COMMON.ERROR.REQUIRED")))}function Q(e,i){1&e&&(S.Tb(0,"vitamui-common-input-error"),S.Oc(1),S.jc(2,"translate"),S.Sb()),2&e&&(S.Bb(1),S.Pc(S.kc(2,1,"USER_PROFILE.INFORMATIONS.MODAL.EXISTING_NAME")))}function K(e,i){1&e&&(S.Tb(0,"vitamui-common-input-error"),S.Oc(1),S.jc(2,"translate"),S.Sb()),2&e&&(S.Bb(1),S.Pc(S.kc(2,1,"COMMON.ERROR.REQUIRED")))}function X(e,i){1&e&&(S.Tb(0,"vitamui-common-input-error"),S.Oc(1),S.jc(2,"translate"),S.Sb()),2&e&&(S.Bb(1),S.Pc(S.kc(2,1,"COMMON.ERROR.REQUIRED")))}function Y(e,i){1&e&&(S.Tb(0,"vitamui-common-input-error"),S.Oc(1),S.jc(2,"translate"),S.Sb()),2&e&&(S.Bb(1),S.Pc(S.kc(2,1,"USER_PROFILE.INFORMATIONS.MODAL.WRONG_FORMAT")))}let Z=(()=>{class e{constructor(e,i,t,o,n,l,a,c){this.dialogRef=e,this.data=i,this.rngProfileService=t,this.authService=o,this.customerService=n,this.profileValidators=l,this.formBuilder=a,this.confirmDialogService=c,this.selectedProfileGroups=[],this.selectedProfileGroupsId=[],this.roleEnum=s.db,this.adminProfileForm=this.formBuilder.group({enabled:!0,name:[null,r.x.required],description:[null,r.x.required],level:["",Object(s.Gb)(this.authService.user)],customerId:[this.authService.user.customerId],applicationName:"USERS_APP",tenantIdentifier:this.tenantWithProofId,roles:[[{name:this.roleEnum.ROLE_GET_USERS},{name:this.roleEnum.ROLE_GET_GROUPS},{name:this.roleEnum.ROLE_GET_USER_INFOS}]]})}ngOnInit(){this.tenantWithProofId=this.authService.user.proofTenantIdentifier,this.adminProfileForm.get("tenantIdentifier").setValue(this.tenantWithProofId),this.adminProfileForm.get("name").setAsyncValidators(this.profileValidators.nameExists(Number(this.tenantWithProofId),"",s.h.USERS_APP)),this.adminProfileForm.get("level").valueChanges.subscribe(e=>{this.adminProfileForm.get("name").setAsyncValidators(this.profileValidators.nameExists(Number(this.tenantWithProofId),e,s.h.USERS_APP)),this.adminProfileForm.get("name").updateValueAndValidity()}),this.keyPressSubscription=this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(()=>this.onCancel())}ngOnDestroy(){this.keyPressSubscription.unsubscribe()}onCancel(){this.adminProfileForm.dirty?this.confirmDialogService.confirmBeforeClosing(this.dialogRef):this.dialogRef.close()}firstStepInvalid(){return this.adminProfileForm.get("name").invalid||this.adminProfileForm.get("name").pending||this.adminProfileForm.get("description").invalid||this.adminProfileForm.get("description").pending||this.adminProfileForm.get("level").invalid}completeUpdateRoles(e){const i=[s.db.ROLE_MFA_USERS.toString(),s.db.ROLE_UPDATE_STANDARD_USERS.toString()],t=e.roles.some(e=>i.includes(e.name)),o=e.roles.findIndex(e=>e.name===s.db.ROLE_UPDATE_USERS),r=e.roles.findIndex(e=>e.name===s.db.ROLE_UPDATE_USER_INFOS);t?(-1===o&&e.roles.push({name:s.db.ROLE_UPDATE_USERS}),-1===r&&e.roles.push({name:s.db.ROLE_UPDATE_USER_INFOS})):(-1!==o&&e.roles.splice(o,1),-1!==r&&e.roles.splice(r,1))}completeCreateRoles(e){const i=[s.db.ROLE_CREATE_USERS.toString()],t=e.roles.some(e=>i.includes(e.name)),o=e.roles.findIndex(e=>e.name===s.db.ROLE_CREATE_USER_INFOS);t?-1===o&&e.roles.push({name:s.db.ROLE_CREATE_USER_INFOS}):-1!==o&&e.roles.splice(o,1)}onSubmit(){if(this.adminProfileForm.invalid)return;const e=this.adminProfileForm.getRawValue();this.completeUpdateRoles(e),this.completeCreateRoles(e),this.rngProfileService.create(e).subscribe(()=>this.dialogRef.close(!0),e=>{console.error(e)})}}return e.\u0275fac=function(i){return new(i||e)(S.Nb(V.g),S.Nb(V.a),S.Nb(P.a),S.Nb(s.k),S.Nb(H.a),S.Nb(M),S.Nb(r.d),S.Nb(s.t))},e.\u0275cmp=S.Hb({type:e,selectors:[["app-profile-create"]],decls:51,vars:51,consts:[[1,"header"],[3,"index","count"],[3,"formGroup","ngSubmit"],[1,"content","vitamui-form"],[1,"text","large","bold"],[1,"mb-2"],["formControlName","enabled"],[4,"ngIf"],["formControlName","name","minlength","2","maxlength","100","required","",1,"col-9","px-0",3,"placeholder"],["formControlName","description","minlength","2","maxlength","250","required","",1,"col-9","px-0",3,"placeholder"],[1,"d-flex"],[1,"mr-4","p-2"],[1,"text","normal","light"],[1,"text","medium","bold"],["formControlName","level",3,"prefix"],["formControlName","roles",1,"d-flex","flex-column"],[3,"name"],[1,"d-flex","mt-4"],["type","submit",1,"btn","primary","mr-4",3,"disabled"],["type","button",1,"btn","link","cancel",3,"click"]],template:function(e,i){if(1&e&&(S.Tb(0,"div",0),S.Ob(1,"vitamui-common-progress-bar",1),S.Sb(),S.Tb(2,"form",2),S.ec("ngSubmit",(function(){return i.onSubmit()})),S.Tb(3,"div",3),S.Tb(4,"div",4),S.Oc(5),S.jc(6,"translate"),S.Sb(),S.Tb(7,"div",5),S.Tb(8,"vitamui-common-slide-toggle",6),S.Mc(9,G,3,3,"span",7),S.Mc(10,W,3,3,"span",7),S.Sb(),S.Sb(),S.Tb(11,"vitamui-common-input",8),S.jc(12,"translate"),S.Mc(13,q,3,3,"vitamui-common-input-error",7),S.Mc(14,Q,3,3,"vitamui-common-input-error",7),S.Sb(),S.Tb(15,"vitamui-common-input",9),S.jc(16,"translate"),S.Mc(17,K,3,3,"vitamui-common-input-error",7),S.Sb(),S.Tb(18,"div",10),S.Tb(19,"div",11),S.Tb(20,"div",12),S.Oc(21),S.jc(22,"translate"),S.Sb(),S.Tb(23,"div",13),S.Oc(24),S.Sb(),S.Sb(),S.Tb(25,"vitamui-common-level-input",14),S.Mc(26,X,3,3,"vitamui-common-input-error",7),S.Mc(27,Y,3,3,"vitamui-common-input-error",7),S.Sb(),S.Sb(),S.Tb(28,"vitamui-common-role-toggle",15),S.Tb(29,"vitamui-common-role",16),S.Oc(30),S.jc(31,"translate"),S.Sb(),S.Tb(32,"vitamui-common-role",16),S.Oc(33),S.jc(34,"translate"),S.Sb(),S.Tb(35,"vitamui-common-role",16),S.Oc(36),S.jc(37,"translate"),S.Sb(),S.Tb(38,"vitamui-common-role",16),S.Oc(39),S.jc(40,"translate"),S.Sb(),S.Tb(41,"vitamui-common-role",16),S.Oc(42),S.jc(43,"translate"),S.Sb(),S.Sb(),S.Tb(44,"div",17),S.Tb(45,"button",18),S.Oc(46),S.jc(47,"translate"),S.Sb(),S.Tb(48,"button",19),S.ec("click",(function(){return i.onCancel()})),S.Oc(49),S.jc(50,"translate"),S.Sb(),S.Sb(),S.Sb(),S.Sb()),2&e){var t=null,o=null,r=null,n=null,l=null;S.Bb(1),S.oc("index",0)("count",1),S.Bb(1),S.oc("formGroup",i.adminProfileForm),S.Bb(3),S.Pc(S.kc(6,29,"USER_PROFILE.INFORMATIONS.MODAL.TITLE")),S.Bb(4),S.oc("ngIf",!(null!=i.adminProfileForm&&i.adminProfileForm.get("enabled").value)),S.Bb(1),S.oc("ngIf",null==i.adminProfileForm?null:i.adminProfileForm.get("enabled").value),S.Bb(1),S.oc("placeholder",S.kc(12,31,"USER_PROFILE.HOME.TITLE_PLACEHOLDER")),S.Bb(2),S.oc("ngIf",(null==i.adminProfileForm||null==(t=i.adminProfileForm.get("name"))?null:t.touched)&&!(null==i.adminProfileForm||null==(t=i.adminProfileForm.get("name"))||null==t.errors||!t.errors.required)),S.Bb(1),S.oc("ngIf",(null==i.adminProfileForm||null==(o=i.adminProfileForm.get("name"))?null:o.touched)&&!(null==i.adminProfileForm||null==(o=i.adminProfileForm.get("name"))||null==o.errors||!o.errors.nameExists)),S.Bb(1),S.oc("placeholder",S.kc(16,33,"USER_PROFILE.INFORMATIONS.DESCRIPTION")),S.Bb(2),S.oc("ngIf",(null==i.adminProfileForm||null==(r=i.adminProfileForm.get("description"))?null:r.touched)&&!(null==i.adminProfileForm||null==(r=i.adminProfileForm.get("description"))||null==r.errors||!r.errors.required)),S.Bb(4),S.Pc(S.kc(22,35,"USER_PROFILE.INFORMATIONS.LEVEL")),S.Bb(3),S.Pc(i.authService.user.level),S.Bb(1),S.oc("prefix",i.authService.user.level),S.Bb(1),S.oc("ngIf",(null==i.adminProfileForm||null==(n=i.adminProfileForm.get("level"))?null:n.touched)&&(null==i.adminProfileForm||null==(n=i.adminProfileForm.get("level"))?null:n.hasError("required"))),S.Bb(1),S.oc("ngIf",(null==i.adminProfileForm||null==(l=i.adminProfileForm.get("level"))?null:l.touched)&&(null==i.adminProfileForm||null==(l=i.adminProfileForm.get("level"))?null:l.hasError("pattern"))),S.Bb(2),S.oc("name",i.roleEnum.ROLE_MFA_USERS),S.Bb(1),S.Pc(S.kc(31,37,"USER_PROFILE.INFORMATIONS.STRONG_AUTHENT")),S.Bb(2),S.oc("name",i.roleEnum.ROLE_CREATE_USERS),S.Bb(1),S.Pc(S.kc(34,39,"USER_PROFILE.INFORMATIONS.USERS_CREATION")),S.Bb(2),S.oc("name",i.roleEnum.ROLE_UPDATE_STANDARD_USERS),S.Bb(1),S.Pc(S.kc(37,41,"USER_PROFILE.INFORMATIONS.MODIF_INFORMATIONS")),S.Bb(2),S.oc("name",i.roleEnum.ROLE_ANONYMIZATION_USERS),S.Bb(1),S.Pc(S.kc(40,43,"USER_PROFILE.INFORMATIONS.ANONYMOUS")),S.Bb(2),S.oc("name",i.roleEnum.ROLE_GENERIC_USERS),S.Bb(1),S.Pc(S.kc(43,45,"USER_PROFILE.INFORMATIONS.USERS_MANAGEMENT")),S.Bb(3),S.oc("disabled",i.firstStepInvalid()),S.Bb(1),S.Qc(" ",S.kc(47,47,"COMMON.SUBMIT")," "),S.Bb(3),S.Qc(" ",S.kc(50,49,"COMMON.UNDO")," ")}},directives:[s.dc,r.y,r.r,r.i,s.ac,r.q,r.g,o.p,s.Qb,r.m,r.l,r.w,s.R,s.fb,s.eb,s.Rb],pipes:[B.d],styles:[""],data:{animation:[s.Hb]}}),e})();var z=t("ZTXN"),J=t("g6G6"),$=t("jIqt"),ee=t("mWib");function ie(e,i){if(1&e){const e=S.Ub();S.Tb(0,"div",13),S.ec("click",(function(){S.Dc(e);const t=i.$implicit;return S.ic().profileClick.emit(t)})),S.Tb(1,"div",14),S.Tb(2,"div",3),S.Ob(3,"i",15),S.Sb(),S.Tb(4,"div",16),S.Oc(5),S.Sb(),S.Tb(6,"div",17),S.Oc(7),S.Sb(),S.Tb(8,"div",17),S.Oc(9),S.jc(10,"truncate"),S.Sb(),S.Tb(11,"div",17),S.Oc(12),S.Sb(),S.Tb(13,"div",17),S.Oc(14),S.Sb(),S.Sb(),S.Sb()}if(2&e){const e=i.$implicit;S.Bb(3),S.oc("ngClass",null!=e&&e.enabled?"status-badge-green":"status-badge-grey"),S.Bb(2),S.Pc(e.name),S.Bb(2),S.Pc(e.identifier),S.Bb(2),S.Pc(S.lc(10,6,e.description,50)),S.Bb(3),S.Pc(e.level),S.Bb(2),S.Pc(e.usersCount)}}function te(e,i){1&e&&S.Ob(0,"div")}function oe(e,i){1&e&&S.Ob(0,"div")}function re(e,i){1&e&&S.Ob(0,"div")}function ne(e,i){1&e&&(S.Tb(0,"div",18),S.Oc(1),S.jc(2,"translate"),S.Sb()),2&e&&(S.Bb(1),S.Pc(S.kc(2,1,"COMMON.NO_RESULT")))}function le(e,i){if(1&e){const e=S.Ub();S.Tb(0,"div",19),S.ec("click",(function(){return S.Dc(e),S.ic().rngProfileService.loadMore()})),S.Tb(1,"span",20),S.Oc(2),S.jc(3,"translate"),S.Sb(),S.Sb()}2&e&&(S.Bb(2),S.Qc("A",S.kc(3,1,"COMMON.SHOW_MORE_RESULTS"),""))}function ae(e,i){1&e&&(S.Tb(0,"div",21),S.Ob(1,"mat-spinner",22),S.Sb())}let ce=(()=>{class e extends s.O{constructor(e){super(e),this.rngProfileService=e,this.orderBy="name",this.direction=s.x.ASCENDANT,this.filterChange=new z.a,this.searchChange=new z.a,this.orderChange=new z.a,this.searchKeys=["name","description","identifier"],this.profileClick=new S.n,this.updatedProfileSub=this.rngProfileService.updated.subscribe(e=>{const i=this.dataSource.findIndex(i=>e.id===i.id);i>-1&&(this.dataSource[i]={id:this.dataSource[i].id,identifier:e.identifier,enabled:e.enabled,name:e.name,level:e.level,customerId:e.customerId,groupsCount:e.groupsCount,description:e.description,usersCount:this.dataSource[i].usersCount,tenantIdentifier:this.dataSource[i].tenantIdentifier,tenantName:this.dataSource[i].tenantName,applicationName:this.dataSource[i].applicationName,roles:this.dataSource[i].roles,readonly:this.dataSource[i].readonly,externalParamId:this.dataSource[i].externalParamId})})}set searchText(e){this._searchText=e,this.searchChange.next(e)}ngOnInit(){Object(J.a)(this.searchChange,this.filterChange,this.orderChange).pipe(Object($.a)(null),Object(ee.a)(400)).subscribe(()=>this.search())}ngOnDestroy(){this.updatedProfileSub.unsubscribe()}search(){const e={criteria:[{key:"applicationName",value:s.h.USERS_APP,operator:s.W.equals},...Object(s.Fb)(this._searchText,this.searchKeys)]},i=new s.Z(0,s.w,this.orderBy,this.direction,JSON.stringify(e));super.search(i)}}return e.\u0275fac=function(i){return new(i||e)(S.Nb(P.a))},e.\u0275cmp=S.Hb({type:e,selectors:[["app-profile-list"]],inputs:{searchText:["search","searchText"]},outputs:{profileClick:"profileClick"},features:[S.yb],decls:31,vars:22,consts:[["vitamuiCommonInfiniteScroll","",3,"vitamuiScroll"],[1,"vitamui-table"],[1,"vitamui-table-head"],[1,"col-1"],[1,"vitamui-icon","vitamui-icon-gestion-de-profil"],[1,"col-3"],[1,"col-2"],[1,"vitamui-table-body"],["class","vitamui-table-rows",3,"click",4,"ngFor","ngForOf"],[4,"ngIf","ngIfThen"],["noResults",""],["loadMore",""],["loadingSpinner",""],[1,"vitamui-table-rows",3,"click"],[1,"vitamui-row","d-flex","align-items-center","clickable"],[1,"vitamui-icon","vitamui-icon-gestion-de-profil",3,"ngClass"],["vitamuiCommonEllipsis","",1,"col-3"],["vitamuiCommonEllipsis","",1,"col-2"],[1,"vitamui-min-content","vitamui-table-message"],[1,"vitamui-min-content","vitamui-table-message",3,"click"],[1,"clickable"],[1,"vitamui-min-content"],[1,"vitamui-spinner","medium"]],template:function(e,i){if(1&e&&(S.Tb(0,"div",0),S.ec("vitamuiScroll",(function(){return i.onScroll()})),S.Tb(1,"div",1),S.Tb(2,"div",2),S.Tb(3,"div",3),S.Ob(4,"i",4),S.Sb(),S.Tb(5,"div",5),S.Oc(6),S.jc(7,"translate"),S.Sb(),S.Tb(8,"div",6),S.Oc(9),S.jc(10,"translate"),S.Sb(),S.Tb(11,"div",6),S.Oc(12),S.jc(13,"translate"),S.Sb(),S.Tb(14,"div",6),S.Oc(15),S.jc(16,"translate"),S.Sb(),S.Tb(17,"div",6),S.Oc(18),S.jc(19,"translate"),S.Sb(),S.Sb(),S.Tb(20,"div",7),S.Mc(21,ie,15,9,"div",8),S.Sb(),S.Sb(),S.Mc(22,te,1,0,"div",9),S.Mc(23,oe,1,0,"div",9),S.Mc(24,re,1,0,"div",9),S.Sb(),S.Mc(25,ne,3,3,"ng-template",null,10,S.Nc),S.Mc(27,le,4,3,"ng-template",null,11,S.Nc),S.Mc(29,ae,2,0,"ng-template",null,12,S.Nc)),2&e){const e=S.zc(26),t=S.zc(28),o=S.zc(30);S.Bb(6),S.Pc(S.kc(7,12,"USER_PROFILE.HOME.RESULTS_TABLE.NAME")),S.Bb(3),S.Pc(S.kc(10,14,"COMMON.ID")),S.Bb(3),S.Pc(S.kc(13,16,"USER_PROFILE.HOME.RESULTS_TABLE.DESCRIPTION")),S.Bb(3),S.Pc(S.kc(16,18,"USER_PROFILE.HOME.RESULTS_TABLE.LEVEL")),S.Bb(3),S.Pc(S.kc(19,20,"USER_PROFILE.HOME.RESULTS_TABLE.USERS_NUMBER")),S.Bb(3),S.oc("ngForOf",i.dataSource),S.Bb(1),S.oc("ngIf",!i.dataSource||i.pending)("ngIfThen",o),S.Bb(1),S.oc("ngIf",!i.pending&&0===(null==i.dataSource?null:i.dataSource.length))("ngIfThen",e),S.Bb(1),S.oc("ngIf",i.infiniteScrollDisabled&&i.rngProfileService.canLoadMore&&!i.pending)("ngIfThen",t)}},directives:[s.N,o.o,o.p,o.n,s.Zb,R.b],pipes:[B.d,s.qc],styles:[""]}),e})();const se=[{path:"",component:(()=>{class e extends s.kb{constructor(e,i,t){super(i,t),this.dialog=e,this.route=i,this.globalEventService=t}openProfilAdminCreateDialog(){this.dialog.open(Z,{panelClass:"vitamui-modal",disableClose:!0}).afterClosed().subscribe(e=>{e&&this.refreshList()})}onSearchSubmit(e){this.search=e}refreshList(){this.profileListComponent&&this.profileListComponent.search()}}return e.\u0275fac=function(i){return new(i||e)(S.Nb(V.b),S.Nb(I.a),S.Nb(s.M))},e.\u0275cmp=S.Hb({type:e,selectors:[["app-profile"]],viewQuery:function(e,i){var t;1&e&&S.Ic(ce,!0),2&e&&S.yc(t=S.fc())&&(i.profileListComponent=t.first)},features:[S.yb],decls:17,vars:14,consts:[[3,"autosize","hasBackdrop"],["mode","side","position","end",3,"fixedInViewport"],["panel",""],[3,"profile","profileClose"],[1,"vitamui-heading"],[3,"searchbarPlaceholder","search"],[1,"btn","primary","ml-5",3,"click"],[1,"vitamui-content"],[3,"search","profileClick"]],template:function(e,i){1&e&&(S.Tb(0,"mat-sidenav-container",0),S.Tb(1,"mat-sidenav",1,2),S.Tb(3,"app-profile-detail",3),S.ec("profileClose",(function(){return i.closePanel()})),S.Sb(),S.Sb(),S.Tb(4,"mat-sidenav-content"),S.Tb(5,"div",4),S.Tb(6,"vitamui-common-title-breadcrumb"),S.Oc(7),S.jc(8,"translate"),S.Sb(),S.Tb(9,"vitamui-common-banner",5),S.ec("search",(function(e){return i.onSearchSubmit(e)})),S.jc(10,"translate"),S.Tb(11,"button",6),S.ec("click",(function(){return i.openProfilAdminCreateDialog()})),S.Tb(12,"span"),S.Oc(13),S.jc(14,"translate"),S.Sb(),S.Sb(),S.Sb(),S.Sb(),S.Tb(15,"div",7),S.Tb(16,"app-profile-list",8),S.ec("profileClick",(function(e){return i.openPanel(e)})),S.Sb(),S.Sb(),S.Sb(),S.Sb()),2&e&&(S.oc("autosize",!0)("hasBackdrop",!1),S.Bb(1),S.oc("fixedInViewport",!0),S.Bb(2),S.oc("profile",i.openedItem),S.Bb(4),S.Qc(" ",S.kc(8,8,"APPLICATION.PROFILES_APP.NAME")," "),S.Bb(2),S.oc("searchbarPlaceholder",S.kc(10,10,"USER_PROFILE.HOME.TITLE_PLACEHOLDER")),S.Bb(4),S.Pc(S.kc(14,12,"USER_PROFILE.HOME.ACTION_BUTTON")),S.Bb(3),S.oc("search",i.search))},directives:[c.b,c.a,x,c.c,s.cc,s.jc,ce],pipes:[B.d],styles:[""]}),e})(),pathMatch:"full"},{path:":id",component:D,resolve:{profile:w},data:{isPopup:!0,appId:"PROFILES_APP"}}];let be=(()=>{class e{}return e.\u0275mod=S.Lb({type:e}),e.\u0275inj=S.Kb({factory:function(i){return new(i||e)},imports:[[o.c,I.g.forChild(se)],I.g]}),e})(),me=(()=>{class e{}return e.\u0275mod=S.Lb({type:e}),e.\u0275inj=S.Kb({factory:function(i){return new(i||e)},imports:[[o.c,s.xb,b.a,n.c,l.b,T,v,a.b,r.v,O,m.a,s.S,c.d,be]]}),e})()}}]);