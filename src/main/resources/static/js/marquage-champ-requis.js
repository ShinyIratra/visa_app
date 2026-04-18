(function () {
    'use strict';

    // requiredFields: { demandeur: [Nom, ...], ... }
    function marquerChampRequis(requiredFields) {
        if (!requiredFields || typeof requiredFields !== 'object') return;

        const overrides = {
            demandeur: {
                situationFamiliale: 'situationsFamiliales',
                nationalite: 'nationalites'
            },
            visaTransformable: {}
        };

        Object.keys(requiredFields).forEach(entite => {
            const fields = requiredFields[entite] || [];
            fields.forEach(champ => {
                const candidates = [];
                
                // Candidat ormal: entite_champ
                candidates.push(`${entite}_${champ}`);
                
                // visaTransformable -> visa_
                if (entite === 'visaTransformable'){ 
                    candidates.push(`visa_${champ}`);
                }

                // Priorite overide
                if (overrides[entite] && overrides[entite][champ]) {
                    candidates.unshift(overrides[entite][champ]);
                }

                let el = null;
                for (const id of candidates) {
                    el = document.getElementById(id);
                    if (el) 
                        break;
                }

                if (!el) 
                    return; // Tsisy marquena

                // Get label
                let label = null;
                if (el.previousElementSibling && el.previousElementSibling.tagName === 'LABEL') {
                    label = el.previousElementSibling;
                } else if (el.parentElement) {
                    label = el.parentElement.querySelector('label');
                }

                if (!label) return;

                // Miala @ double marquage oz copilot
                if (label.querySelector && label.querySelector('.required-star')) 
                    return;

                const span = document.createElement('span');
                span.className = 'required-star';
                span.style.color = 'red';
                span.style.marginLeft = '6px';
                span.innerText = '*';
                label.appendChild(span);
            });
        });
    }

    window.marquerChampRequis = marquerChampRequis;
})();
