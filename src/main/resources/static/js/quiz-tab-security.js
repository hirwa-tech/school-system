

(function() {
    'use strict';
    
    let isSubmitted = false;
    const form = document.getElementById('quizForm');
    
    function submitForm() {
        if (!isSubmitted && form) {
            isSubmitted = true;
            const submitBtn = document.getElementById('submitBtn');
            if (submitBtn) submitBtn.disabled = true;
            form.submit();
        }
    }
    
    function handleVisibilityChange() {
        if (document.hidden && !isSubmitted && form) {
            console.log('Tab switched - auto-submitting quiz');
            submitForm();
        }
    }
    
   
    document.addEventListener('visibilitychange', handleVisibilityChange);
    
 
    window.addEventListener('beforeunload', function() {
        if (!isSubmitted && form) {
            submitForm();
        }
    });
    
   
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            console.log('Quiz security loaded');
        });
    } else {
        console.log('Quiz security loaded');
    }
})();
