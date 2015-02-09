/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

$(document).ready(function() {

    $("#sshkeysLink").click(function() {
        loadSSHKeys(user);
    });
    

    $("#addNewSSHKey").click(function() {
        if (!$("#newSSHKey").val().trim()) {
            (flowMessager.newMessage("SSH key", "field can't be empty", "warning")).draw();
            return;
        }
        var newSSHKey = $("#newSSHKey").val().trim();
        $("#newSSHKey").val("");
        var loadImage = new LoadImage($("#sshkeys-table"), "auto");
        
        callPerun("attributesManager", "getAttribute", {user: user.id, attributeName: "urn:perun:user:attribute-def:def:sshPublicKey"}, function(sshPublicKey) {
            if (!sshPublicKey) {
                (flowMessager.newMessage("SSH keys", "can't be loaded", "danger")).draw();
                return;
            }
            // if it's first SSH key.
            if (!sshPublicKey.value) {
                sshPublicKey.value = [];
            }
            sshPublicKey.value.push(newSSHKey);
            callPerunPost("attributesManager", "setAttribute", {user: user.id, attribute: sshPublicKey}, function() {
                fillSSHKeys(sshPublicKey);
                loadImage.hide();
                (flowMessager.newMessage("SSH key", "was added successfully", "success")).draw();
            });
        });
    });

});

function loadSSHKeys(user) {
    if (!user) {
        (flowMessager.newMessage("SSH keys", "can't be loaded becouse user isn't loaded.", "danger")).draw();
        return;
    }
    var loadImage = new LoadImage($("#sshkeys-table"), "auto");
    
    callPerun("attributesManager", "getAttribute", {user: user.id, attributeName: "urn:perun:user:attribute-def:def:sshPublicKey"}, function(sshPublicKey) {
        if (!sshPublicKey) {
            (flowMessager.newMessage("SSH keys", "can't be loaded", "danger")).draw();
            return;
        }
        fillSSHKeys(sshPublicKey);
        loadImage.hide();
        //(flowMessager.newMessage("SSH keys", "was loaded successfully.", "success")).draw();
    });
}

function fillSSHKeys(sshPublicKey) {
    if (!sshPublicKey) {
        (flowMessager.newMessage("SSH keys", "can't be fill", "danger")).draw();
        return;
    }
    var sshKeysTable = new PerunTable();
    sshKeysTable.addColumn({type:"number", title:"#"});
    sshKeysTable.addColumn({type:"text", title:"SSH keys", name:"value"});
    sshKeysTable.addColumn({type:"button", title:"", btnText:"remove", btnType:"danger", btnId:"key", btnName:"removeSSHKey"});
    sshKeysTable.setList(sshPublicKey.value);
    var tableHtml = sshKeysTable.draw();
    $("#sshkeys-table").html(tableHtml);
    
    
    $('#sshkeys-table button[id^="removeSSHKey-"]').click(function() {
        var sshId = parseInt(this.id.split('-')[1]);
        var loadImage = new LoadImage($("#sshkeys-table"), "auto");
        
        callPerun("attributesManager", "getAttribute", {user: user.id, attributeName: "urn:perun:user:attribute-def:def:sshPublicKey"}, function(sshPublicKey) {
            if (!sshPublicKey) {
                (flowMessager.newMessage("SSH keys", "can't be loaded", "danger")).draw();
                return;
            }
            if (!sshPublicKey.value) {
                (flowMessager.newMessage("SSH keys", "is empty", "warning")).draw();
                return;
            }
            sshPublicKey.value.splice(sshId, 1);
            callPerunPost("attributesManager", "setAttribute", {user: user.id, attribute: sshPublicKey}, function() {
                fillSSHKeys(sshPublicKey);
                loadImage.hide();
                (flowMessager.newMessage("SSH key", "was removed successfully", "success")).draw();
            });
        });
    });
}