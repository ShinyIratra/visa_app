document.addEventListener('DOMContentLoaded', function() {
    const tableBody = document.querySelector('table tbody');

    if (!tableBody) return;

    fetch('/transfert-visa/list')
        .then(response => response.json())
        .then(apiResponse => {
            if (apiResponse.success) {
                renderTable(apiResponse.data);
            } else {
                console.error('Erreur API:', apiResponse.error);
                tableBody.innerHTML = `<tr><td colspan="7" class="empty empty-error">${apiResponse.message}</td></tr>`;
            }
        })
        .catch(error => {
            console.error('Erreur fetch:', error);
            tableBody.innerHTML = '<tr><td colspan="7" class="empty empty-error">Erreur lors du chargement des donnees.</td></tr>';
        });

    function renderTable(data) {
        tableBody.innerHTML = '';
        if (data.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="7" class="empty">Aucune demande de transfert trouvee.</td></tr>';
            return;
        }

        data.forEach(item => {
            const tr = document.createElement('tr');

            tr.innerHTML = `
                <td>${item.id}</td>
                <td>${item.demandeur}</td>
                <td>${item.ancienPasseport}</td>
                <td>${item.nouveauPasseport}</td>
                <td>${item.nationalite}</td>
                <td>${item.statut}</td>
                <td>
                    <button class="btn-action" onclick="accepterDemande(${item.id})">Accepter demande</button>
                    <button class="btn-action" onclick="window.location.href='/transfert-visa/${item.id}/edit'">Modifier</button>
                </td>
            `;
            tableBody.appendChild(tr);
        });
    }

    window.accepterDemande = function(id) {
        if (!confirm('Accepter la demande ?')) 
            return;

        fetch(`/transfert-visa/${id}/accepter`, {
            method: 'POST'
        })
        .then(response => response.json())
        .then(apiResponse => {
            if (apiResponse.success) {
                window.location.reload();
            } else {
                throw new Error(apiResponse.error);
            }
        })
        .catch(error => {
            alert(error);
        });
    };
});
