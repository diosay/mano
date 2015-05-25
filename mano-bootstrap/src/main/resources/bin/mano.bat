@echo off
rem Copyright (C) 2014-2015 The MANO Authors. 
rem All rights reserved. Use is subject to license terms. 
rem
rem     http://mano.diosay.com/
rem

echo DIOSAY MANO Server (Version 1.4 BETA) 
echo (C) 2014-2015 The MANO Authors. All rights reserved.


setlocal
set "CURRENT_DIR=%~dp0"
set "DRIVER=%~d0"
cd "%CURRENT_DIR%"
%DRIVER%
java -jar mano.bootstrap.jar

pause