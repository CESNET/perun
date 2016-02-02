/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


function callExternalScript(url, args, callBack) {
    $.ajax({
        url: url,
        data: args,
        dataType: "jsonp",
        type: "get",
        timeout: 8000,
        success: function(data, textStatus, jqXHR)
        {
            callBack(data);
        },
        error: function(jqXHR, textStatus, errorThrown)
        {
            (staticMessager.newMessage("jQuery ajax error: ", errorThrown + " - " + textStatus + ". Please try it later.", "danger", $("#messager"), false)).draw();
        }
    });
}
;


function preFill(form) {
    var getParams = window.location.search.substr(1).split('&');
    for (var id in getParams) {
        var param = getParams[id].split('=');
        if (param[0]) {
            form.find("#"+param[0]).val(decodeURIComponent(param[1]));
        }
    }
}

