/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function entryPoint(user) {
  loadAlternativePasswords(user);
}

$(document).ready(function() {

    $("#createAltPassword").submit(function(event) {
        event.preventDefault();

        var password = $("#sambaPassword").val();

        if (!/^[\x20-\x7E]{1,}$/.test(password)) {
            (flowMessager.newMessage("Heslo pro SAMBA", "Heslo nesmí obsahovat diakritiku nebo netisknutelné/řídící znaky.", "danger")).draw();
            return;
        }

        var charcounter = 0;
        if (/^.*[a-z].*$/.test(password)) charcounter++;
        if (/^.*[A-Z].*$/.test(password)) charcounter++;
        if (/^.*[0-9].*$/.test(password)) charcounter++;
        if (/^.*[\x20-\x2F\x3A-\x40\x5B-\x60\x7B-\x7E].*$/.test(password)) charcounter++;

        if (charcounter < 3) {
            (flowMessager.newMessage("Heslo pro SAMBA", "Heslo musí obsahovat alespoň 3 ze 4 kategorií znaků: malá / velká písmena, číslice, spec. znaky.", "danger")).draw();
            return;
        }

        if (password.length < 10) {
            (flowMessager.newMessage("Heslo pro SAMBA", "Heslo musí být alespoň 10 znaků dlouhé.", "danger")).draw();
            return;
        }

        var timestamp = new Date().getTime();
        callPerunPost("usersManager", "createAlternativePassword", {user: user.id, description: timestamp, loginNamespace: "samba-du", password: password}, function() {
          $("#sambaPassword").val("");
          loadAlternativePasswords(user);
          (flowMessager.newMessage("Heslo pro SAMBA", "úspěšně nastaveno", "success")).draw();
        });

    });

});

function loadAlternativePasswords(user) {
    if (!user) {
        (flowMessager.newMessage("Aplikaci", "nelze načíst, protože nejsou dostupné informace o uživateli")).draw();
        return;
    }

    callPerun("attributesManager", "getAttribute", {user: user.id, attributeName: "urn:perun:user:attribute-def:def:altPasswords:samba-du"}, function(altPasswords) {
        if (!altPasswords) {
            (flowMessager.newMessage("Aplikaci", "nelze načíst.", "danger")).draw();
            return;
        }
        if (altPasswords.value == null) {
          $("#sambaPassword-info").text("Heslo pro SAMBA ještě nemáte nastaveno.");
        } else {
          $("#sambaPassword-info").html("Heslo pro SAMBA <strong>máte</strong> nastaveno, zadáním nového původní přepíšete.");
        }
    });
}

