/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


function entryPoint(user) {
}

$(document).ready(function() {
    $("form#establishForm").submit(function(e) {
        e.preventDefault();
        var form = $("form#establishForm");
        var queue = "perun";
        var subject = "Application for establish new VO";
        var text = getTextFromForm(form);
        form[0].reset();
        if (!isDevel()) {
            callPerunPost("rtMessagesManager", "sentMessageToRT", {queue: queue, subject: subject, text: text}, function() {
                (flowMessager.newMessage("Application", "was send successfully", "success")).draw();
            });
        } else {
            console.log("This is devel mode. Everything seems fine. But final message to RT hasn't been send.");
            (staticMessager.newMessage("Devel", "This is devel mode. Everything seems fine. But final message to RT hasn't been send.", "warning")).draw();
        }
        
        function getTextFromForm(form) {
            var text = 'Request has been created, detailed information follow: \n ';
            form.find('input, textarea').each(function() {
                text += form.find("label[for="+$(this).attr("id")+"]").text()+": ";
                text += $(this).val() + '   \n ';
            });
            console.log(text);
            return text;
        }
    });
});