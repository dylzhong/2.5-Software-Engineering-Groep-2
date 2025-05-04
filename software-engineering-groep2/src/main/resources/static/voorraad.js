function loadVoorraad() {
    $.getJSON("/voorraad", function(data) {
        $('#table_voorraad').DataTable({
            "data": data,
            "columns": [
                { "data": "id" },
                { "data": "ndc" },
                { "data": "details" }
            ]
        });
    });
}