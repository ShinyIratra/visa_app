(function () {
    'use strict';


    /**
     * Fonction appelee 
     * - apres avoir load les donnees statiques
     * - apres avoir change le select: VISA demande
     */
    function updateDossiersAFournir() {
        // Avy ao @ form.html: typeSelect, dossierList, donneesStatiques
        const selectedId = (window.typeSelect && window.typeSelect.value) || (typeSelect && typeSelect.value) || '';
        const types = (window.donneesStatiques && window.donneesStatiques.typesDemande) || (typeof donneesStatiques !== 'undefined' && donneesStatiques.typesDemande) || [];
        const selectedType = types ? types.find(t => t.id == selectedId) : null;
        const libelle = selectedType ? selectedType.libelle.toLowerCase() : "";

        const listEl = (window.dossierList) || (typeof dossierList !== 'undefined' && dossierList) || document.getElementById('dossiersList');
        if (!listEl) return;
        listEl.innerHTML = "";

        const ds = window.donneesStatiques || (typeof donneesStatiques !== 'undefined' && donneesStatiques) || {};

        // Selon la valeur du select choisi, On ajoute dans cette liste la liste des dossiers a afficher
        const sections = [
            { label: "DOSSIERS COMMUNES", items: ds.dossiersCommuns || [] }
        ];

        // Tsy generalise fa alefako amin'io aloha amin'izay mazava ilay code
        if (libelle.includes("travailleur")) {
            sections.push({ label: "DOSSIERS TRAVAILLEUR", items: ds.dossiersTravailleur || [] });
        } else if (libelle.includes("investisseur")) {
            sections.push({ label: "DOSSIERS INVESTISSEUR", items: ds.dossiersInvestisseur || [] });
        }

        sections.forEach(sec => renderDossierSection(sec, listEl));
    }

    function renderDossierSection(sec, listEl) {
        if (!sec || !sec.items || sec.items.length === 0) return;
        const group = document.createElement('div');
        group.className = "checkbox-item group-label";
        group.innerText = sec.label;
        listEl.appendChild(group);

        sec.items.forEach(item => {
            const div = document.createElement('label');
            div.className = "checkbox-item";
            const labelText = (item.libelle || item.nom || "Dossier sans label");

            // Creation checkbox
            const checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.name = 'dossierIds';
            checkbox.value = item.id;
            checkbox.id = 'dossier_' + item.id;
            if (item.obligatoire === true) {
                // Marquage cote code
                checkbox.dataset.obligatoire = 'true';
                checkbox.required = false; // Mailo
            }

            // label
            div.appendChild(checkbox);
            const textNode = document.createTextNode(' ' + labelText);
            div.appendChild(textNode);

            // Maquage
            if (item.obligatoire === true) {
                const star = document.createElement('span');
                star.className = 'required-star';
                star.style.color = 'red';
                star.style.marginLeft = '6px';
                star.innerText = '*';
                div.appendChild(star);
            }

            listEl.appendChild(div);
        });
    }

    window.updateDossiersAFournir = updateDossiersAFournir;
    window.renderDossierSection = renderDossierSection;
})();
