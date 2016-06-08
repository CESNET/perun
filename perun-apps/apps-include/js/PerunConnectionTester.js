$(document).ready(function() {
    if (!isDevel()) {
        // Methods which check if the Perun connection is OK
        executeQuery();
	    //setTimeout(executeQuery, 5000);
    }
});


function reloadMsg() {
    alert("Data connection lost. Click OK to reload the page.");
    reload();
}

function reload() {
    window.location.reload(true);
}


function executeQuery() {
        $.ajax({
            url: getRpcUrl(),
            success: function(data) {
                if (!(data.indexOf("OK!") == 0)) {
                    reloadMsg();
                }
                setTimeout(executeQuery, 5000);
            },
            statusCode: {
                404: function() {
                    reloadMsg();
                },
                401: function() {
                    reloadMsg();
                },
                302: function() {
                    reload()
                },
                0: function() {
                    reload()
                }
            },
            error: function(jqXHR, textStatus, errorThrown) {
                if (jqXHR.status == 0) {
                    reload()
                } else if (jqXHR.status == 302) {
                            reload()
                } else {
                  reloadMsg();
                }
            }
        });
}
