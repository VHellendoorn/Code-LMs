define( {
    load: function ( resourceId, req, load, config ) {
        var xhr = window.XMLHttpRequest
            ? new XMLHttpRequest()
            : new ActiveXObject("Microsoft.XMLHTTP");

        xhr.onreadystatechange = function () {
            if (xhr.readyState == 4) {
                load( xhr.responseText );
            }
        };
        xhr.open('GET', req.toUrl( resourceId + '.html' ), true);
        xhr.send(null);
    }
} );