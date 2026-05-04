document.addEventListener('DOMContentLoaded', function() {
    const tableBody = document.getElementById('visaTableBody');
    const btnFilter = document.getElementById('btnFilter');
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');

    function loadVisas() {
        let url = '/visas/data';
        const params = new URLSearchParams();
        
        if (startDateInput.value) params.append('start', startDateInput.value);
        if (endDateInput.value) params.append('end', endDateInput.value);
        
        if (params.toString()) {
            url += '?' + params.toString();
        }

        fetch(url)
            .then(response => response.json())
            .then(data => {
                tableBody.innerHTML = '';
                if (data.length === 0) {
                    tableBody.innerHTML = '<tr><td colspan="6" class="empty">Aucun visa trouve.</td></tr>';
                    return;
                }
                data.forEach(visa => {
                    const row = document.createElement('tr');
                    
                    const dateStr = visa.dateCreation ? new Date(visa.dateCreation).toLocaleString() : '-';
                    const dateDebutStr = visa.dateDebut ? new Date(visa.dateDebut).toLocaleString() : '-';
                    const dateExpirationStr = visa.dateExpiration ? new Date(visa.dateExpiration).toLocaleString() : '-';
                    
                    row.innerHTML = `
                        <td>${visa.id}</td>
                        <td>${dateStr}</td>
                        <td>${dateDebutStr} <br> ${dateExpirationStr}</td>
                        <td>${visa.nomComplet || 'Inconnu'}</td>
                        <td>${visa.ancienPasseport || '-'}</td>
                        <td>${visa.nouveauPasseport || '-'}</td>
                        <td>
                            <a href="/visa-requests/${visa.demandeId}/edit">${visa.demandeId}</a>
                        </td>
                    `;
                    tableBody.appendChild(row);
                });
            })
            .catch(error => {
                console.error('Erreur:', error);
                tableBody.innerHTML = '<tr><td colspan="6" class="empty empty-error">Erreur lors du chargement des donnees.</td></tr>';
            });
    }

    btnFilter.addEventListener('click', loadVisas);
    loadVisas();
});
