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
                tableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: red;">${apiResponse.message}</td></tr>`;
            }
        })
        .catch(error => {
            console.error('Erreur fetch:', error);
            tableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: red;">Erreur lors du chargement des données.</td></tr>`;
        });

    function renderTable(data) {
        tableBody.innerHTML = '';
        if (data.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="7" style="text-align: center;">Aucune demande de transfert trouvée.</td></tr>';
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
                </td>
            `;
            tableBody.appendChild(tr);
        });
    }

    window.accepterDemande = function(id) {
        if (!confirm('Acceptr la demande ?')) 
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
