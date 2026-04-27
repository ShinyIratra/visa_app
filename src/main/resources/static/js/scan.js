(function () {
    'use strict';

    function initFileInputs() {
        var list = document.getElementById('dossiersList');
        if (!list) {
            return;
        }

        var inputs = list.querySelectorAll('input[type="file"]');
        for (var i = 0; i < inputs.length; i++) {
            (function (input) {
                var wrapper = input.parentElement;
                var fileName = wrapper ? wrapper.querySelector('.file-name') : null;

                input.addEventListener('change', function () {
                    if (!fileName) {
                        return;
                    }

                    if (input.files && input.files.length > 0) {
                        fileName.textContent = input.files[0].name;
                    } else {
                        fileName.textContent = '(Aucun fichier selectionne)';
                    }
                });
            })(inputs[i]);
        }
    }

    function initSubmit() {
        var form = document.getElementById('scanForm');
        if (!form) {
            return;
        }

        var demandeId = form.getAttribute('data-demande-id');
        if (!demandeId) {
            return;
        }

        form.addEventListener('submit', function (event) {
            event.preventDefault();

            if (!allUploadsPresent()) {
                alert('Tous les dossiers doivent etre uploades avant de terminer le scan.');
                return;
            }

            fetch('/visa-requests/scan/api/' + demandeId + '/terminer', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({})
            })
            .then(function (response) {
                return response.json();
            })
            .then(function (result) {
                if (!result.success) {
                    throw new Error(result.error || 'Erreur');
                }
                window.location.href = '/visa-requests';
            })
            .catch(function (error) {
                alert('Erreur: ' + error.message);
            });
        });
    }

    function allUploadsPresent() {
        var list = document.getElementById('dossiersList');
        if (!list) {
            return false;
        }

        var inputs = list.querySelectorAll('input[type="file"]');
        if (!inputs || inputs.length === 0) {
            return false;
        }

        for (var i = 0; i < inputs.length; i++) {
            if (!inputs[i].files || inputs[i].files.length === 0) {
                return false;
            }
        }

        return true;
    }

    document.addEventListener('DOMContentLoaded', function () {
        initFileInputs();
        initSubmit();
    });
})();

