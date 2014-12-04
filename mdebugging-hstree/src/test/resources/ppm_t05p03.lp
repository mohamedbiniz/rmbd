kval(1).
kval(N1) :- kval(N), N < L, patternlength(L), N1=N+1. 
1 { subt(K,I,E) : t(I,E) } 1 :- kval(K), patternlength(L).
:- subt(K1,I1,X), subt(K2,I2,Y), K1<K2, I1 >= I2.

solution(K,E) :- subt(K,X,E).

:- solution(K1,ET1), solution(K2,ET2), p(K1,EP1), p(K1,EP2), ET1 < ET2, EP1 >= EP2.

t(1,5).
t(2,3).
t(3,1).
t(4,4).
t(5,2).
patternlength(3).
p(1, 3).
p(2, 1).
p(3, 2).
