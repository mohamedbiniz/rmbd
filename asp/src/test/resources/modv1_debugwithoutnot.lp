% ooasp.lp
% transitive closure
ooasp_subclass(V,SUB,SUPER2):-ooasp_subclass(V,SUB,SUPER1),ooasp_subclass(V,SUPER1,SUPER2).

% derive domain for boolean attributes
ooasp_attribute_enum(V,C,N,true):-ooasp_attribute(V,C,N,boolean).
ooasp_attribute_enum(V,C,N,false):-ooasp_attribute(V,C,N,boolean).

% true, if attribute has domain
ooasp_attribute_hasdomain(V,C,N):-ooasp_attribute_enum(V,C,N,D).

ooasp_hassubclass(V,C):-ooasp_subclass(V,SUB,C).
% leafclasses are classes without subclasses
%ooasp_leafclass(V,C):-ooasp_class(V,C),not ooasp_hassubclass(V,C).

% Instantiation
% ooasp_isa(V,C,ID).
% if kb isa instanceof subclass it is also an instanceof superclass
ooasp_isa(CONFIG,SUPER,ID):-ooasp_isa(CONFIG,SUB,ID),ooasp_subclass(V,SUB,SUPER),ooasp_configuration(V,CONFIG).

% configobjects are instantiations of leaf classes
ooasp_configobject(CONFIG,C,ID):-ooasp_leafclass(V,C),ooasp_isa(CONFIG,C,ID),ooasp_configuration(V,CONFIG).

% helper: ooasp_domainandsuperclasses(V,C,ID)
% for an ID and version contains the ooasp_domain-class and it's superclasses
ooasp_domainandsuperclasses(CONFIG,C,ID):-ooasp_domain(CONFIG,C,ID),ooasp_configuration(V,CONFIG).
ooasp_domainandsuperclasses(CONFIG,SUPER,ID):-ooasp_domainandsuperclasses(CONFIG,C,ID),ooasp_subclass(V,C,SUPER),ooasp_configuration(V,CONFIG).

% helper: ooasp_domainandsubclasses(V,C,ID)
ooasp_domainandsubclasses(CONFIG,C,ID):-ooasp_domain(CONFIG,C,ID),ooasp_configuration(V,CONFIG).
ooasp_domainandsubclasses(CONFIG,SUB,ID):-ooasp_domainandsubclasses(CONFIG,C,ID),ooasp_subclass(V,SUB,C),ooasp_configuration(V,CONFIG).

% ooasp_canbe(V,C,ID) Which classes can an ID be
% defines the possible classes an ID could become
ooasp_canbe(CONFIG,C,ID):-ooasp_domainandsubclasses(CONFIG,C,ID).
ooasp_canbe(CONFIG,C,ID):-ooasp_domainandsuperclasses(CONFIG,C,ID).

% ooasp_canbeleafclass(V,C,ID) Which leafclasses can an ID be
ooasp_canbe_leafclass(CONFIG,C,ID):-ooasp_leafclass(V,C),ooasp_canbe(CONFIG,C,ID),ooasp_configuration(V,CONFIG).

% ooasp_config.lp
% ooasp_config(VERSION)
% this predicate defines, which version should be configured 

% instantiation of classes
% TODO: currently the user must add a fact ooasp_domain(V,C,ID) for
% every ooasp_isa(V,C,ID) because
% otherwise we get a unstratified predicate error
% ooasp_domain(1,C,ID):-ooasp_isa(1,C,ID).
% An objectid in a ooasp_domain predicate can become an instance of this class
% if it is an instance of the class, it must also become an instance of its leafclasses
0 { ooasp_isa(CONFIG,LEAFCLASS,ID) : ooasp_canbe_leafclass(CONFIG,LEAFCLASS,ID) } :- ooasp_domain(CONFIG,C,ID),ooasp_configure(CONFIG).

% symmetry breaking
%:- ooasp_domain(CONFIG,C,X),ooasp_domain(CONFIG,C,Y),ooasp_isa(CONFIG,C,Y),X<Y,not ooasp_isa(CONFIG,C,X),ooasp_configure(CONFIG).

% an ID can be only instance of 1 class
%constraint(CONFIG,I,C1,C2,"isa of unrelated classes"):-ooasp_isa(CONFIG,C1,I),ooasp_isa(CONFIG,C2,I),C1!=C2,{ ooasp_subclass(V,C1,C2),ooasp_subclass(V,C2,C1) } 0,ooasp_configure(CONFIG).

% Associations
% ooasp_assoc(V,NAME,C1,C1MIN,C1MAX,C2MIN,C2MAX).

% generation rule for assocs
C2MIN { ooasp_associated(CONFIG,ASSOC,ID1,ID2):ooasp_canbe(CONFIG,C2,ID2) }:- ooasp_isa(CONFIG,C1,ID1),ooasp_assoc(V,ASSOC,C1,C1MIN,C1MAX,C2,C2MIN,C2MAX),ooasp_configuration(V,CONFIG),ooasp_configure(CONFIG).
:- C2MAX+1 { ooasp_associated(CONFIG,ASSOC,ID1,ID2):ooasp_canbe(CONFIG,C2,ID2) },ooasp_isa(CONFIG,C1,ID1),ooasp_assoc(V,ASSOC,C1,C1MIN,C1MAX,C2,C2MIN,C2MAX),C2MAX>0,ooasp_configuration(V,CONFIG),ooasp_configure(CONFIG).

% TODO: do we need both sides as rule?
C1MIN { ooasp_associated(CONFIG,ASSOC,ID1,ID2):ooasp_canbe(CONFIG,C1,ID1) }:- ooasp_isa(CONFIG,C2,ID2),ooasp_assoc(V,ASSOC,C1,C1MIN,C1MAX,C2,C2MIN,C2MAX),ooasp_configuration(V,CONFIG),ooasp_configure(CONFIG).
:- C1MAX+1 { ooasp_associated(CONFIG,ASSOC,ID1,ID2):ooasp_canbe(CONFIG,C1,ID1) },ooasp_isa(CONFIG,C2,ID2),ooasp_assoc(V,ASSOC,C1,C1MIN,C1MAX,C2,C2MIN,C2MAX),C1MAX>0,ooasp_configuration(V,CONFIG),ooasp_configure(CONFIG).

% type check - only use instantiated object ids

ooasp_isa(CONFIG,C1,ID1):- ooasp_associated(CONFIG,ASSOC,ID1,ID2),ooasp_assoc(V,ASSOC,C1,C1MIN,C1MAX,C2,C2MIN,C2MAX),ooasp_configuration(V,CONFIG),ooasp_configure(CONFIG).
ooasp_isa(CONFIG,C2,ID2):- ooasp_associated(CONFIG,ASSOC,ID1,ID2),ooasp_assoc(V,ASSOC,C1,C1MIN,C1MAX,C2,C2MIN,C2MAX),ooasp_configuration(V,CONFIG),ooasp_configure(CONFIG).

:-ooasp_isa(CONFIG,C1,ID1),ooasp_isa(CONFIG,C2,ID1),C1!=C2,ooasp_leafclass(V,C1),ooasp_leafclass(V,C2).

% Attributes
% ooasp_attribute(SOURCE,bicycle,bicycleHaslights,string).
1 { ooasp_attribute_value(CONFIG,N,ID,VALUE):ooasp_attribute_enum(V,C,N,VALUE) } :-ooasp_isa(CONFIG,C,ID),ooasp_attribute(V,C,N,T),ooasp_attribute_hasdomain(V,C,N),ooasp_configuration(V,CONFIG),ooasp_configure(CONFIG).

% TODO: this rule is only used if MAX-MIN < 100
1 { ooasp_attribute_value(CONFIG,N,ID,VALUE):VALUE=MIN..MAX } :-ooasp_attribute(V,C,N,T),
	ooasp_isa(CONFIG,C,ID),
	ooasp_attribute_minInclusive(V,C,N,MIN),
	ooasp_attribute_maxInclusive(V,C,N,MAX),
	%(MAX-MIN)<100,
	ooasp_configuration(V,CONFIG),
	ooasp_configure(CONFIG).

% modv1_kb.lp
% classes
ooasp_class("mv1","Frame").
ooasp_class("mv1","Module").
ooasp_class("mv1","ModuleA").
ooasp_class("mv1","ModuleB").
ooasp_class("mv1","ModuleC").
ooasp_class("mv1","Element").
ooasp_class("mv1","ElementA").
ooasp_class("mv1","ElementB").
ooasp_leafclass("mv1","ModuleA").
ooasp_leafclass("mv1","ModuleB").
ooasp_leafclass("mv1","ModuleC").
ooasp_class("mv1","ElementA").
ooasp_class("mv1","ElementB").

% class inheritance
ooasp_subclass("mv1","ElementA","Element").
ooasp_subclass("mv1","ElementB","Element").
ooasp_subclass("mv1","ModuleA","Module").
ooasp_subclass("mv1","ModuleB","Module").
ooasp_subclass("mv1","ModuleC","Module").

% attributes & associations
% class Frame
ooasp_assoc("mv1","Frame_modules","Frame",1,1,"Module",0,5).

% class Module
ooasp_attribute("mv1","Module","position","integer").
ooasp_attribute_minInclusive("mv1","Module","position",1).
ooasp_attribute_maxInclusive("mv1","Module","position",5).

% class Element
ooasp_assoc("mv1","Element_module","Element",1,1,"Module",1,1).

% modv1.edb

ooasp_configuration("mv1","config1").

ooasp_domain("config1","Frame",1).
ooasp_domain("config1","ElementA",10..12).
ooasp_isa("config1","ElementA",10..12).
ooasp_domain("config1","ElementB",13..14).
ooasp_isa("config1","ElementB",13..14).
ooasp_domain("config1","Module",20..24).

% modv1.lp
ooasp_configure("config1").

% position in frame unique
:- ooasp_configuration("mv1",CONFIG),
    ooasp_associated(CONFIG,"Frame_modules",F,M1),
    ooasp_associated(CONFIG,"Frame_modules",F,M2),
    ooasp_attribute_value(CONFIG,"position",M1,P1),
    ooasp_attribute_value(CONFIG,"position",M2,P2),
    M1!=M2,
    P1==P2.  
  
:-ooasp_associated(CONFIG,"Element_module",E1,M1),
  ooasp_associated(CONFIG,"Element_module",E2,M2),
  E1<E2,
  M1>M2.

ooasp_isa(CONFIG,"ModuleA",M1):-
    ooasp_associated(CONFIG,"Element_module",E1,M1),
    ooasp_isa(CONFIG,"ElementA",E1).

ooasp_isa(CONFIG,"ModuleB",M1):-ooasp_associated(CONFIG,"Element_module",E1,M1),
   ooasp_isa(CONFIG,"ElementB",E1).

%  dont put 2 modules of type moduleA next to each other
ooasp_constraint(CONFIG,info("Frame",F,"Module",M1,P1,"Module",M2,P2)):- ooasp_configuration("mv1",CONFIG),
    ooasp_associated(CONFIG,"Frame_modules",F,M1),
    ooasp_associated(CONFIG,"Frame_modules",F,M2),
    ooasp_attribute_value(CONFIG,"position",M1,P1),
    ooasp_attribute_value(CONFIG,"position",M2,P2),
    ooasp_configobject(CONFIG,"ModuleA",M1),
    ooasp_configobject(CONFIG,"ModuleA",M2),
    P1 = P2 + 1.

:-ooasp_constraint(CONFIG,I).

:-ooasp_associated(CONFIG,"Frame_modules",F,M1),
  ooasp_associated(CONFIG,"Frame_modules",F,M2),
  ooasp_attribute_value(CONFIG,"position",M1,P1),
  ooasp_attribute_value(CONFIG,"position",M2,P2),
  M1<M2,
  % here is the bug -> not all modules but only modules of the same subclass must be ordered
  % uncomment the next 2 lines and it works
  %ooasp_configobject(CONFIG,MTYPE,M1),
  %ooasp_configobject(CONFIG,MTYPE,M2),  
  P1>P2.

#hide.
#show ooasp_configobject(X,Y,Z).
#show ooasp_associated/4.
#show ooasp_attribute_value/4.
#show ooasp_constraint/2.