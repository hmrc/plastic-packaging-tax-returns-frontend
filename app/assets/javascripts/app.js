// prevent resubmit warning
if (window.history && window.history.replaceState && typeof window.history.replaceState === 'function') {
  window.history.replaceState(null, null, window.location.href);
}

// handle back click
var backLinkElem = document.querySelector('.govuk-back-link');

if(backLinkElem != null) {
    backLinkElem.addEventListener('click', function(e){
        e.preventDefault();
        e.stopPropagation();
        window.history.back();
    });
}

// return submit status
var processingStatusUrl = document.querySelector('#processing-status-url')
if( processingStatusUrl !== null ){
    var url = processingStatusUrl.value;
    function pollData(){
        fetch(url).then(function (response) {
            if (response.ok) {
                return response.json();
            } else {
                return Promise.reject(response);
            }
        }).then(function (data) {
            if (data.status === "processing")
                setTimeout(function() {
                    pollData();
                }, 2000);
            else
                location.reload();
        }).catch(function (err) {
            console.warn('Something went wrong.', err);
        });
    }
    pollData();
}