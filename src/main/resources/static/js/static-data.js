(function () {
    'use strict';

    function loadDonneesStatiques() {
        fetch('/api/static/all')
            .then(r => r.json())
            .then(resp => {
                // Noproposen copilot zah hoe aleo console.error sy return maina be eto fa tsy mithrow new error
                // fa tsy mahatoky anazy a dia avelako amin'io aloha  
                if (!resp || !resp.success) {
                    throw new Error(`${resp?.error || 'Erreur inconnue'}`);
                }

                donneesStatiques = resp.data || {};
                populateAllSelects(donneesStatiques);
                marquerChampRequis(donneesStatiques.requiredFields || {});
                updateDossiersAFournir();
            })
            .catch(e => console.error('Error loading static data', e));
        }

    function populateAllSelects(data) {
        fillSelect('typesDemande', data.typesDemande, i => i.libelle);
        fillSelect('situationsFamiliales', data.situationsFamiliales, i => i.libelle);
        fillSelect('nationalites', data.nationalites, i => i.libelle);
    }

    function fillSelect(id, arr, texte) {
        const s = document.getElementById(id);
        if (!s || !Array.isArray(arr)) return;

        // s.innerHTML = '<option value="">--</option>';
        s.innerHTML = '';
        arr.forEach(it => {
            const opt = document.createElement('option');
            opt.value = it.id;
            opt.text = texte ? texte(it) : (it.libelle || it.numero || it.reference || it.id);
            s.appendChild(opt);
        });
    }

    window.loadDonneesStatiques = loadDonneesStatiques;
    window.populateAllSelects = populateAllSelects;
    window.fillSelect = fillSelect;
})();
