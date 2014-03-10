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