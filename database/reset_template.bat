@echo off
setlocal EnableDelayedExpansion

:: ================= CONFIGURATION =================
:: --------------- Seulement à modifier ------------
set "DB_HOST=localhost"
set "DB_USER=postgres"
set "PGPASSWORD=post"
set "DB_NAME=backoffice"
set "PSQL_CMD=C:\Program Files\PostgreSQL\17\bin\psql.exe"
:: -------------------------------------------------

:: --- CORRECTION DES CHEMINS (Absolus + Slashes) ---
:: 1. Récupère le dossier où se trouve le script .bat (ex: C:\Projet\script\)
set "BASE_DIR=%~dp0"

:: 2. Remplace les backslashes '\' par des slashes '/' pour Postgres
set "BASE_DIR=!BASE_DIR:\=/!"

:: 3. Définit les dossiers en absolu
set "STATIC_DATA_DIR=!BASE_DIR!seeders/staticdata"

:: Nom du fichier temporaire généré
set "RESET_FILE=reset.sql"
:: =================================================

:: 1. Force l'encodage UTF-8
chcp 65001 > nul

echo.
echo GENERATION DU SCRIPT DE RESET...
echo --------------------------------

:: 2. TRUNCATE toutes les tables et reset sequences de manière dynamique
echo -- RESET DES DONNEES -- > %RESET_FILE%
echo DO $$ >> %RESET_FILE%
echo DECLARE >> %RESET_FILE%
echo     rec RECORD; >> %RESET_FILE%
echo BEGIN >> %RESET_FILE%
echo     FOR rec IN SELECT schemaname, tablename FROM pg_tables WHERE schemaname IN ('staging', 'prod') LOOP >> %RESET_FILE%
echo         EXECUTE 'TRUNCATE TABLE ' ^|^| rec.schemaname ^|^| '.' ^|^| rec.tablename ^|^| ' RESTART IDENTITY CASCADE'; >> %RESET_FILE%
echo     END LOOP; >> %RESET_FILE%
echo END $$ >> %RESET_FILE%
echo ; >> %RESET_FILE%

:: 3. AJOUT DES DONNEES STATIQUES
echo. >> %RESET_FILE%
echo -- STATIC DATA -- >> %RESET_FILE%
for /f "delims=" %%f in ('dir /b /on "%~dp0seeders\staticdata\*sd*.sql"') do (
    echo [Static Data] Ajout de : %%f
    echo \i '%STATIC_DATA_DIR%/%%f' >> %RESET_FILE%
)

echo.
echo EXECUTION SUR POSTGRESQL...
echo -----------------------------

:: 4. Exécution
"%PSQL_CMD%" -h %DB_HOST% -U %DB_USER% -d %DB_NAME% -v ON_ERROR_STOP=1 -f %RESET_FILE%

if %errorlevel% neq 0 (
    echo.
    echo ❌ UNE ERREUR EST SURVENUE !
    echo Verifie le fichier %RESET_FILE%.
    pause
    exit /b 1
)

:: 5. Nettoyage
del %RESET_FILE%

echo.
echo ✅ SUCCES ! Reset termine.
pause