@echo off
set PATH=%PATH%;C:\Users\kostya\Desktop\Software
set LOCAL=c:\Users\kostya\.m2\repository\at\ainf
set SERVER=kostya@pantora.ifit.uni-klu.ac.at:.m2/repository/at/ainf
set DIAG=diagnosis/1.0-SNAPSHOT
set OWL=owlapi-3/1.0-SNAPSHOT
set KEY=C:/Users/kostya/Desktop/Software/keys/junken_open.ppk

call mvn install -pl owlapi-3 -am -Dmaven.test.skip=true

pscp -i %KEY% %LOCAL%/%OWL%/*.jar %SERVER%/%OWL%/
pscp -i %KEY% %LOCAL%/%DIAG%/*.jar %SERVER%/%DIAG%/

