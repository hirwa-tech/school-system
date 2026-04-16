
document.addEventListener('DOMContentLoaded', function() {
    const select = document.getElementById('classroomSelect');
    if (select) {
        fetch('/api/classrooms/public')
            .then(response => response.json())
            .then(classrooms => {
                select.innerHTML = '<option value="">Select Classroom</option>';
                classrooms.forEach(classroom => {
                    const option = document.createElement('option');
                    option.value = classroom.id;
                    option.textContent = classroom.name + ' (' + classroom.teacher.username + ')';
                    select.appendChild(option);
                });
            })
            .catch(error => {
                console.error('Error loading classrooms:', error);
                select.innerHTML = '<option value="">Error loading classrooms</option>';
            });
    }
});
