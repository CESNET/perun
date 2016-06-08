/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

$(document).ready(function() {
    var form = $("#subscribeForm");
    preFill(form);

    form.submit(function(event) {
        event.preventDefault();

        var name = form.find("input#name");
        var surname = form.find("input#surname");
        var email = form.find("input#email");
        
        callExternalScript("scripts/sendSubscribeEmail.cgi", {name: name.val(), surname: surname.val(), email: email.val()}, function(data) {
            if (data.errorId) {
                switch (data.name) {
                    case "EmailNotSendException":
                        (staticMessager.newMessage("Failed to send a email.", "Please, try it later. If problem still persists contact support@elixir-czech.cz", "danger", $("#messager"), false)).draw();
                        break;
                    default :
                        (staticMessager.newMessage("Internal error", "Please, try it later. If problem still persists contact support@elixir-czech.cz"
                                +" and attach this error number: "+data.errorId, "danger", $("#messager"), false)).draw();
                        break;
                }
            } else {
                name.val("");
                surname.val("");
                email.val("");
                form.hide();
                (staticMessager.newMessage("Confirmation email", "has been sent to "+email.val()+". To finish the subscription process, please click on the link from the e-mail.", "success", $("#messager"), false)).draw();
            }

        });

    });

});
