pc(m1).
paper(p1).
bid(m1,p1,2).
assigned(p1,m1).
author(p1,m1).

conflict_of_interest(M,P) :- bid(M,P,0).
conflict_of_interest(M,P) :- pc(M), paper(P), author(M,P).
bid(M,P,0) :- pc(M), paper(P), conflict_of_interest(M,P).
:- assigned(P,M), bid(M,P,0).
:- pc(M).