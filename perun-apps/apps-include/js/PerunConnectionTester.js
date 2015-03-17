$(document).ready(function() {
    if (!isDevel()) {
        // Methods which check if the Perun connection is OK
        executeQuery();
	    //setTimeout(executeQuery, 5000);
    }
});


function reloadMsg() {
    alert("Data connection lost. Click OK to relad the page.");
    window.location.reload();
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
		    window.location.reload();
		},
		0: function() {
		    window.location.reload();
		}
            },
            error: function(jqXHR, textStatus, errorThrown) {
                if (jqXHR.status == 0) {
		  window.location.reload();
		} else if (jqXHR.status == 302) {
		  window.location.reload();
		} else {
		  reloadMsg();
		}
            }
        });
}
