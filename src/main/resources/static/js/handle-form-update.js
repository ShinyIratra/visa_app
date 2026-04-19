(function () {
    'use strict';

    async function initFormByDemandeId() {
        const demandeId = document.getElementById('demandeId').value;
        if (!demandeId) {
            return;
        }

        try {
            const response = await fetch(`/api/demandes-visa/${demandeId}`);
            const result = await response.json();

            if (result.success) {
                fillForm(result.data);
            } else {
                throw new Error(result.error);
            }
        } catch (e) {
            throw new Error('Erreur lors de la recuperation des donnees:' + e.message);
        }
    }

    function fillForm(data) {
        // Set type demande
        const typeSelect = document.getElementById('typesDemande');
        typeSelect.value = data.typeDemandeId || '';
        
        // Reconstruction an'ny DOM an'ny section dossiers selon data.typeDemandeId
        if (typeof updateDossiersAFournir === 'function') {
            updateDossiersAFournir();
        }

        // Etat civil
        const ec = data['etat civil'] || {};
        document.getElementById('demandeur_nom').value = ec.nom || '';
        document.getElementById('demandeur_prenom').value = ec.prenom || '';
        document.getElementById('demandeur_nomJeuneFille').value = ec.nomJeuneFille || '';
        document.getElementById('situationsFamiliales').value = ec.situationFamiliale || '';
        document.getElementById('nationalites').value = ec.nationalite || '';
        document.getElementById('demandeur_dateNaissance').value = ec.dateNaissance || '';
        document.getElementById('demandeur_adresse').value = ec.adresse || '';
        document.getElementById('demandeur_email').value = ec.email || '';
        document.getElementById('demandeur_numTel').value = ec.numTel || '';

        // Passeport
        const pass = data['passeport'] || {};
        document.getElementById('passeport_numero').value = pass.numero || '';
        document.getElementById('passeport_dateDelivrance').value = pass.dateDelivrance || '';
        document.getElementById('passeport_dateExpiration').value = pass.dateExpiration || '';

        // Visa
        const visa = data['visaTransformable'] || {};
        document.getElementById('visa_reference').value = visa.reference || '';
        document.getElementById('visa_dateEntree').value = visa.dateEntree || '';
        document.getElementById('visa_lieuEntree').value = visa.lieuEntree || '';
        document.getElementById('visa_dateExpiration').value = visa.dateExpiration || '';

        // Dossiers fournis
        const dossiersFournis = data.dossiersFournis || [];
        dossiersFournis.forEach(id => {
            const cb = document.getElementById('dossier_' + id);
            if (cb) {
                cb.checked = true;
            } else {
                console.warn(`Checkbox for dossier ID ${id} not found in the DOM (id="dossier_${id}").`);
            }
        });
    }

    window.initFormByDemandeId = initFormByDemandeId;
})();
