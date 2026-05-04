document.addEventListener('DOMContentLoaded', () => {
    fetchDemandeurs();
});

function fetchDemandeurs() {
    fetch('/api/demandeurs/list')
        .then(response => response.json())
        .then(res => {
            const tbody = document.getElementById('demandeurTableBody');
            tbody.innerHTML = '';
            
            if (res.success && res.data && res.data.length > 0) {
                res.data.forEach(d => {
                    const row = document.createElement('tr');
                    
                    const nomPrenom = `${d.nom} ${d.prenom}`;
                    
                    let passeportOriginalStr = 'Aucun';
                    if (d.passeportOriginal) {
                        passeportOriginalStr = `<strong>${d.passeportOriginal.numero}</strong><br>(${formatDate(d.passeportOriginal.dateDelivrance)} - ${formatDate(d.passeportOriginal.dateExpiration)})`;
                    }

                    let passeportActuelStr = 'Aucun';
                    if (d.passeportActuel) {
                        passeportActuelStr = `<strong>${d.passeportActuel.numero}</strong><br>(${formatDate(d.passeportActuel.dateDelivrance)} - ${formatDate(d.passeportActuel.dateExpiration)})`;
                    }

                    let visasStr = 'Aucun';
                    if (d.visas && d.visas.length > 0) {
                        visasStr = d.visas.map(v => `<strong>${v.numero}</strong><br>(${formatDate(v.dateDebut)} - ${formatDate(v.dateExpiration)})`).join('<br><br>');
                    }

                    let cartesStr = 'Aucune';
                    if (d.cartesResident && d.cartesResident.length > 0) {
                        cartesStr = d.cartesResident.map(c => `<strong>${c.numero}</strong><br>(${formatDate(c.dateDebut)} - ${formatDate(c.dateExpiration)})`).join('<br><br>');
                    }

                    row.innerHTML = `
                        <td>${nomPrenom}</td>
                        <td>${passeportOriginalStr}</td>
                        <td>${passeportActuelStr}</td>
                        <td>${visasStr}</td>
                        <td>${cartesStr}</td>
                    `;
                    tbody.appendChild(row);
                });
            } else {
                tbody.innerHTML = '<tr><td colspan="5" class="empty">Aucun demandeur trouve.</td></tr>';
            }
        })
        .catch(err => {
            console.error('Erreur:', err);
            document.getElementById('demandeurTableBody').innerHTML = '<tr><td colspan="5" class="empty">Erreur lors du chargement des donnees.</td></tr>';
        });
}

function formatDate(dateString) {
    if (!dateString) return '-';
    // dateString can be like "2026-05-01T00:00:00"
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return '-';
    
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    
    return `${day}/${month}/${year}`;
}