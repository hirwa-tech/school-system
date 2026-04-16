
(function() {
    'use strict';

    document.addEventListener('DOMContentLoaded', function() {
        
        const deadlineMillis = window.quizData ? window.quizData.deadline : 0;
        
        const timerEl = document.getElementById('timer');
        const form = document.getElementById('quizForm');
        const reasonField = document.getElementById('submissionReason');
        const submitBtn = document.getElementById('submitBtn');
        let isSubmitted = false;

       
        if (!form) {
            console.error("Quiz form not found!");
            return;
        }

       
        function secureSubmit(reason) {
            if (isSubmitted) return;
            isSubmitted = true;

            
            if (reasonField) reasonField.value = reason;
            
           
            if (submitBtn) submitBtn.disabled = true;

           
            const overlayMessage = (reason === 'TAB_SWITCH') 
                ? 'SECURITY VIOLATION: Tab switch detected. Submitting...' 
                : 'TIME EXPIRED: Submitting your answers...';

            document.body.innerHTML = `
                <div style="position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.95);
                color:white;display:flex;align-items:center;justify-content:center;font-size:24px;z-index:9999;text-align:center;padding:20px;font-family:sans-serif;">
                    <div>
                        <div style="margin-bottom: 20px; font-size: 50px;">⚠️</div>
                        ${overlayMessage}
                    </div>
                </div>`;
            
           
            form.submit();
        }

        
        function startCountdown() {
            if (!deadlineMillis || deadlineMillis <= 0) {
                if (timerEl) timerEl.textContent = "No Limit";
                return;
            }

            const updateTimer = () => {
                if (isSubmitted) return;

                const now = Date.now();
                const remaining = deadlineMillis - now;

                if (remaining <= 0) {
                    if (timerEl) timerEl.textContent = "00:00";
                    secureSubmit('TIMEOUT');
                } else {
                    const minutes = Math.floor(remaining / 60000);
                    const seconds = Math.floor((remaining % 60000) / 1000);
                    if (timerEl) {
                        timerEl.textContent = `${minutes}:${seconds.toString().padStart(2, '0')}`;
                    }
                   
                    setTimeout(updateTimer, 1000);
                }
            };
            updateTimer();
        }

        
        document.addEventListener('visibilitychange', function() {
            if (document.visibilityState === 'hidden' && !isSubmitted) {
                console.warn('Tab switch detected - security trigger.');
                secureSubmit('TAB_SWITCH');
            }
        });

       
        window.addEventListener('beforeunload', function() {
            if (!isSubmitted) {
                if (reasonField) reasonField.value = 'PAGE_UNLOAD';
            }
        });

        
        startCountdown();
    });
})();