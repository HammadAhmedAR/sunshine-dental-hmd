// Progressive enhancement only: the service validates everything without JavaScript.
const patientSelector = document.getElementById("existingPatientId");
if (patientSelector) {
    const fields = document.querySelectorAll("#new-patient-fields input, #new-patient-fields textarea");
    const summary = document.getElementById("selected-patient");
    function updatePatientFields() {
        const existing = patientSelector.value !== "";
        fields.forEach(field => {
            field.disabled = existing;
            field.required = !existing;
        });
        const option = patientSelector.selectedOptions[0];
        summary.textContent = existing
            ? "Saved address: " + (option.dataset.address || "Not recorded — contact the administrator to complete this record.")
                + " · Contact: " + option.dataset.phone
            : "";
    }
    patientSelector.addEventListener("change", updatePatientFields);
    updatePatientFields();
}
