@echo off
setlocal EnableDelayedExpansion

:: ================= CONFIGURATION =================

:: --------------- Seulement à modifier ------------
set "DB_HOST=localhost"
set "DB_USER=postgres"
set "PGPASSWORD=post"
set "DB_NAME=visa_db"
set "PSQL_CMD=C:\Program Files\PostgreSQL\17\bin\psql.exe"
:: -------------------------------------------------

:: --- CORRECTION DES CHEMINS (Absolus + Slashes) ---
:: 1. Récupère le dossier où se trouve le script .bat (ex: C:\Projet\script\)
set "BASE_DIR=%~dp0"

:: 2. Remplace les backslashes '\' par des slashes '/' pour Postgres
set "BASE_DIR=!BASE_DIR:\=/!"

:: 3. Définit les dossiers en absolu
set "DUMMY_DATA_DIR=!BASE_DIR!seeders/dummydata"

:: Nom du fichier temporaire généré
set "DUMMY_FILE=__add_dummy.sql"
:: =================================================

:: 1. Force l'encodage UTF-8
chcp 65001 > nul

echo.
echo GENERATION DU SCRIPT D'AJOUT DES DONNEES DUMMY...
echo -------------------------------------------------

:: 2. AJOUT DES DONNEES DUMMY
echo -- DUMMY DATA -- > %DUMMY_FILE%
for /f "delims=" %%f in ('dir /b /on "%~dp0seeders\dummydata\*dd*.sql"') do (
    echo [Dummy Data] Ajout de : %%f
    echo \i '%DUMMY_DATA_DIR%/%%f' >> %DUMMY_FILE%
)

echo.
echo EXECUTION SUR POSTGRESQL...
echo -----------------------------

:: 3. Exécution
"%PSQL_CMD%" -h %DB_HOST% -U %DB_USER% -d %DB_NAME% -v ON_ERROR_STOP=1 -f %DUMMY_FILE%

if %errorlevel% neq 0 (
    echo.
    echo ❌ UNE ERREUR EST SURVENUE !
    echo Verifie le fichier %DUMMY_FILE%.
    pause
    exit /b 1
)

:: 4. Nettoyage
del %DUMMY_FILE%

echo.
echo ✅ SUCCES ! Donnees dummy ajoutees.
pause