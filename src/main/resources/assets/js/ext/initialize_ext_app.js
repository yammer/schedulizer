'use strict';

/* Freemarker html is loaded synchronouly here */
(function() {
    var request = new XMLHttpRequest();
    request.open('GET', '/service/ext-app/config.html', false);
    request.send(null);
    document.write(request.responseText);
})();