@echo off
setlocal EnableDelayedExpansion

:: ================= CONFIGURATION =================
:: --------------- Seulement à modifier ------------
set "DB_HOST=localhost"
set "DB_USER=postgres"
set "PGPASSWORD=post"
set "PSQL_CMD=C:\Program Files\PostgreSQL\17\bin\psql.exe"
:: -------------------------------------------------

set "INITIAL_DB=postgres"

:: --- CORRECTION DES CHEMINS (Absolus + Slashes) ---
:: 1. Récupère le dossier où se trouve le script .bat (ex: C:\Projet\script\)
set "BASE_DIR=%~dp0"

:: 2. Remplace les backslashes '\' par des slashes '/' pour Postgres
set "BASE_DIR=!BASE_DIR:\=/!"

:: 3. Définit les dossiers en absolu
set "MIGRATION_DIR=!BASE_DIR!migrations"
set "STATIC_DATA_DIR=!BASE_DIR!seeders/staticdata"
set "DUMMY_DATA_DIR=!BASE_DIR!seeders/dummydata"

:: Nom du fichier temporaire généré
set "MASTER_FILE=__exec_master.sql"
:: =================================================

:: 1. Force l'encodage UTF-8
chcp 65001 > nul

echo.
echo GENERATION DU SCRIPT DE MIGRATION...
echo ------------------------------------

:: 2. Initialisation (create.sql)
:: On utilise des guillemets '' pour le chemin dans le SQL au cas où il y ait des espaces
echo \i '%MIGRATION_DIR%/create.sql' > %MASTER_FILE%

:: 3. PASSE 1 : La STRUCTURE
echo. >> %MASTER_FILE%
echo -- 1. STRUCTURE -- >> %MASTER_FILE%
:: Note: On utilise le chemin Windows normal "%~dp0migrations" pour la commande DIR
for /f "delims=" %%f in ('dir /b /on "%~dp0migrations\*script*.sql"') do (
    echo [Structure] Ajout de : %%f
    echo \i '%MIGRATION_DIR%/%%f' >> %MASTER_FILE%
)

:: 4. PASSE 2 : Les DONNEES STATIQUES
echo. >> %MASTER_FILE%
echo -- 2. STATIC DATA -- >> %MASTER_FILE%
for /f "delims=" %%f in ('dir /b /on "%~dp0seeders\staticdata\*sd*.sql"') do (
    echo [Static Data] Ajout de : %%f
    echo \i '%STATIC_DATA_DIR%/%%f' >> %MASTER_FILE%
)

:: 5. PASSE 3 : Les DONNEES DUMMY
echo. >> %MASTER_FILE%
echo -- 3. DUMMY DATA -- >> %MASTER_FILE%
for /f "delims=" %%f in ('dir /b /on "%~dp0seeders\dummydata\*dd*.sql"') do (
    echo [Dummy Data] Ajout de : %%f
    echo \i '%DUMMY_DATA_DIR%/%%f' >> %MASTER_FILE%
)

echo.
echo EXECUTION SUR POSTGRESQL...
echo ------------------------------------

:: 6. Exécution unique
"%PSQL_CMD%" -h %DB_HOST% -U %DB_USER% -d %INITIAL_DB% -v ON_ERROR_STOP=1 -f %MASTER_FILE%

if %errorlevel% neq 0 (
    echo.
    echo ❌ UNE ERREUR EST SURVENUE !
    echo Verifie le fichier %MASTER_FILE%.
    pause
    exit /b 1
)

:: 7. Nettoyage
del %MASTER_FILE%

echo.
echo ✅ SUCCES ! Migration terminee.
pause