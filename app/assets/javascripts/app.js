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

