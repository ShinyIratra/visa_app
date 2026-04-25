(function () {
    'use strict';

    function handleFormSubmit(event, lien_retour, lien_api) {
        event.preventDefault();

        const typeDemandeValue = document.getElementById('typesDemande').value;

        const formData = {
            typeDemandeId: typeDemandeValue ? Number(typeDemandeValue) : null,
            "etat civil": {
                nom: document.getElementById('demandeur_nom').value,
                prenom: document.getElementById('demandeur_prenom').value,
                nomJeuneFille: document.getElementById('demandeur_nomJeuneFille').value,
                situationFamiliale: document.getElementById('situationsFamiliales').value,
                nationalite: document.getElementById('nationalites').value,
                dateNaissance: document.getElementById('demandeur_dateNaissance').value,
                adresse: document.getElementById('demandeur_adresse').value,
                email: document.getElementById('demandeur_email').value,
                numTel: document.getElementById('demandeur_numTel').value
            },
            "passeport": {
                numero: document.getElementById('passeport_numero').value,
                dateDelivrance: document.getElementById('passeport_dateDelivrance').value,
                dateExpiration: document.getElementById('passeport_dateExpiration').value
            },
            "visaTransformable": {
                reference: document.getElementById('visa_reference').value,
                dateEntree: document.getElementById('visa_dateEntree').value,
                lieuEntree: document.getElementById('visa_lieuEntree').value,
                dateExpiration: document.getElementById('visa_dateExpiration').value
            },
            "dossiersFournis": Array.from(document.querySelectorAll('input[name="dossierIds"]:checked')).map(cb => Number(cb.value))
        };

        fetch(lien_api, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        })
        .then(r => r.json())
        .then(resp => {
            if (resp.success) {
                // alert('Demande enregistree avec succes');
                // console.log('Response:', resp.data); // TODO: redirection ?
                window.location.href = lien_retour || '/';
            } else {
                throw new Error(resp.error);
            }
        })
        .catch(e => {
            console.error('Submit error:', e);
            alert('Erreur: ' + e.message);
        });
    }

    window.handleFormSubmit = handleFormSubmit;
})();
