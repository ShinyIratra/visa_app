document.addEventListener('DOMContentLoaded', function() {
    const tableBody = document.querySelector('table tbody');

    if (!tableBody) return;

    fetch('/duplicata/list')
        .then(response => response.json())
        .then(apiResponse => {
            if (apiResponse.success) {
                renderTable(apiResponse.data);
            } else {
                console.error('Erreur API:', apiResponse.error);
                tableBody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: red;">${apiResponse.message || 'Une erreur est survenue.'}</td></tr>`;
            }
        })
        .catch(error => {
            console.error('Erreur fetch:', error);
            tableBody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: red;">Erreur lors du chargement des données.</td></tr>`;
        });

    function renderTable(data) {
        tableBody.innerHTML = '';
        if (data.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="6" style="text-align: center;">Aucune demande de duplicata trouvée.</td></tr>';
            return;
        }

        data.forEach(item => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${item.id}</td>
                <td>${item.demandeur}</td>
                <td>${item.numeroPasseport}</td>
                <td>${item.nationalite}</td>
                <td>${item.statut}</td>
                <td>
                    <button class="btn-action" onclick="accepterDemande(${item.id})">Accepter demande</button>
                </td>
            `;
            tableBody.appendChild(tr);
        });
    }

    window.accepterDemande = function(id) {
        if (!confirm('Accepter la demande de duplicata ?')) 
            return;

        fetch(`/duplicata/${id}/accepter`, {
            method: 'POST'
        })
        .then(response => response.json())
        .then(apiResponse => {
            if (apiResponse.success) {
                window.location.reload();
            } else {
                throw new Error(apiResponse.error || 'Erreur lors de l\'acceptation');
            }
        })
        .catch(error => {
            alert(error.message);
        });
    };
});