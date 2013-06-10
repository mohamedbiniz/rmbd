person(1).
person(2).
thing(6).
thing(7).
cabinet(500).
cabinet(501).
cabinet(502).
cabinet(503).
cabinet(504).
room(1000).
room(1001).
room(1002).
room(1003).
room(1004).
personTOthing(1,6).
personTOthing(2,7).

{cabinetTOthing(X,Y):cabinet(X)} :- thing(Y).
:- 2 {cabinetTOthing(X,Y):cabinet(X)}, thing(Y).
:- Z = {cabinetTOthing(X,Y):cabinet(X)}, thing(Y), Z=0.
:- 6 {cabinetTOthing(X,Y):thing(Y)}, cabinet(X).

{roomTOcabinet(X,Y):room(X)}:-cabinet(Y).
:- 2 {roomTOcabinet(X,Y):room(X)}, cabinet(Y).
:- Z = {roomTOcabinet(X,Y):room(X)}, cabinet(Y), Z=0.
:- 5 {roomTOcabinet(X,Y):cabinet(Y)}, room(X).

personTOroom(P,R):-personTOthing(P,X), cabinetTOthing(C,X), 
	roomTOcabinet(R,C).

:-cabinetTOthing(X,Y1), cabinetTOthing(X,Y2), personTOthing(P1,Y1),
	personTOthing(P2,Y2), P1!=P2.

:- personTOroom(P1,R), personTOroom(P2,R).
