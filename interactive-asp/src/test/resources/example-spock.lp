head(r1, a).
bodyN(r1, d).
head(r2, b).
bodyP(r2, a).
head(r3, c).
bodyP(r3, b).
head(r4, d).
bodyP(r4, c).
bodyP(r5, d).

% aux

atom(A) :- head(R, A).
atom(A) :- bodyP(R, A).
atom(A) :- bodyN(R, A).
rule(R) :- head(R, A).
rule(R) :- bodyP(R, A).
rule(R) :- bodyN(R, A).

% int

int(A) :- atom(A), not -int(A).
-int(A) :- atom(A), not int(A).

% ap

ap(R) :- not bl(R), rule(R).
bl(R) :- bodyN(R, A), int(A).
bl(R) :- bodyP(R, A), -int(A).

% sat

hasHead(R) :- head(R, A).
someHInI(R) :- head(R, A), int(A).
violated(C) :- ap(C), rule(C), not hasHead(C).
unsatisfied(R) :- ap(R), hasHead(R),not someHInI(R).

% supp

otherHInI(R, A1) :- head(R, A2), int(A2), head(R, A1), A1!=A2.
supported(A) :- head(R,A), ap(R), not otherHInI(R, A).
unsupported(A) :- int(A), atom(A), not supported(A).

% ufloop

ufLoop(A) :- int(A), atom(A), supported(A), not -ufLoop(A).
-ufLoop(A) :- int(A), atom(A), not ufLoop(A).

someBInLoop(R) :- bodyP(R, A), ufLoop(A).
someHOutLoop(R) :- head(R, A), -ufLoop(A).

:- head(R, A), ufLoop(A), ap(R), not someHOutLoop(R), not someBInLoop(R).

% loop must be strongly connected by rules supporting it:
dpcy(A1, A2) :- head(R, A1), bodyP(R, A2), ap(R), ufLoop(A1), ufLoop(A2), not someHOutLoop(R).
dpcy(A1, A2) :- dpcy(A1, A3), dpcy(A3, A2), atom(A1), atom(A2), atom(A3).
:- ufLoop(A1), ufLoop(A2), not dpcy(A1,A2), atom(A1), atom(A2).


%noAS
noAnswerSet :- unsatisfied(R), rule(R).
noAnswerSet :- ufLoop(A), atom(A).
noAnswerSet :- violated(R), rule(R).
noAnswerSet :- unsupported(A), atom(A).

:- not noAnswerSet.

%%%%%%%%%%%%%%%%%%%%

#show int/1.
#show -int/1.

#show unsatisfied/1.
#show violated/1.
#show ufLoop/1.
#show unsupported/1.

:~ unsatisfied(X). [1@1,unsatisfied(X)]
:~ violated(X). [1@1,violated(X)]
:~ unsupported(X). [1@1,unsupported(X)]
:~ ufLoop(X). [1@1,ufLoop(X)]

%  --opt-mode=optN --number=9 --quiet=1,1