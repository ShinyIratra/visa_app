document.addEventListener('DOMContentLoaded', function() {
    const tableBody = document.querySelector('table tbody');

    if (!tableBody) return;

    function loadData(start = '', end = '') {
        let url = '/duplicata/list';
        const params = new URLSearchParams();
        if (start) params.append('start', start);
        if (end) params.append('end', end);
        if (params.toString()) url += '?' + params.toString();

        fetch(url)
            .then(response => response.json())
            .then(apiResponse => {
                if (apiResponse.success) {
                    renderTable(apiResponse.data);
                } else {
                    console.error('Erreur API:', apiResponse.error);
                    tableBody.innerHTML = `<tr><td colspan="7" class="empty empty-error">${apiResponse.message || 'Une erreur est survenue.'}</td></tr>`;
                }
            })
            .catch(error => {
                console.error('Erreur fetch:', error);
                tableBody.innerHTML = '<tr><td colspan="7" class="empty empty-error">Erreur lors du chargement des donnees.</td></tr>';
            });
    }

    loadData();

    function renderTable(data) {
        tableBody.innerHTML = '';
        if (data.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="7" class="empty">Aucune demande de duplicata trouvee.</td></tr>';
            return;
        }

        data.forEach(item => {
            const tr = document.createElement('tr');
            const dateStr = item.dateCreation ? new Date(item.dateCreation).toLocaleString() : '';
            tr.innerHTML = `
                <td>${item.id}</td>
                <td>${item.demandeur}</td>
                <td>${item.numeroPasseport}</td>
                <td>${item.nationalite}</td>
                <td>${item.statut}</td>
                <td>${dateStr}</td>
                <td>
                    <button class="btn-action" onclick="accepterDemande(${item.id})">Accepter demande</button>
                </td>
            `;
            tableBody.appendChild(tr);
        });
    }

    window.filterByDate = function() {
        const start = document.getElementById('dateStart').value;
        const end = document.getElementById('dateEnd').value;
        loadData(start, end);
    };

    window.resetFilter = function() {
        document.getElementById('dateStart').value = '';
        document.getElementById('dateEnd').value = '';
        loadData();
    };

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