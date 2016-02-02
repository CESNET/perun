/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


$(document).ready(function() {

    var secret = getURLParameter("secret");

    callExternalScript("scripts/unsubscribe.cgi", {secret: secret}, function(data) {
        if (data.errorId) {
            switch (data.name) {
                case "MemberNotExistsException":
                    (staticMessager.newMessage("You are not a member of mailing list bioinfo-cz@elixir-czech.cz.", "So we can not unsubscribe you.", "danger", $("#messager"), false)).draw();
                    break;
                case "UserNotFoundByEmailException":
                    (staticMessager.newMessage("You are not a member of mailing list bioinfo-cz@elixir-czech.cz.", "So we can not unsubscribe you.", "danger", $("#messager"), false)).draw();
                    break;
                case "TimestampExceetedMaxAgeException":
                    (staticMessager.newMessage("Link has expired", "Please continue to <a href='unsubscribe.html'>Elixir mailing list Manager</a> and send a new unsubscribe request", "danger", $("#messager"), false)).draw();
                    break;
                case "NotGroupMemberException":
                    (staticMessager.newMessage("User is not subscribed", "or was already removed from mailing list bioinfo-cz@elixir-czech.cz", "danger", $("#messager"), false)).draw();
                    break;
                default :
                    (staticMessager.newMessage("Internal error", "Please, try it later. If problem still persists contact support@elixir-czech.cz"
                            + " and attach this error number: " + data.errorId, "danger", $("#messager"), false)).draw();
                    break;
            }
        } else {
            (staticMessager.newMessage("Your email was successfully unsubscribed", "from mailing list bioinfo-cz@elixir-czech.cz", "success", $("#messager"), false)).draw();
        }
    });

});
