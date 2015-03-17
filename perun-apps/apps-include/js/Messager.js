/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
flowMessager = new Messager($("#flowMessager"), 9000);
staticMessager = new Messager($("#staticMessager"), undefined);

function Messager(place, timeout) {
    this.place = place;
    this.timeout = timeout;
    
    this.newMessage = function (title, text, type) {
        return (new Message(title, text, type, this.place, this.timeout));
    };
}


currentMessageId = 0;
function Message(title, text, type, place, timeout) {
    currentMessageId++;
    this.id = currentMessageId;
    this.title = title;
    this.text = text;
    this.type = type;
    this.place = place;
    this.timeout = timeout;
    
    if (!this.place) {
        this.place = $("#messager");
    }
    
    this.draw = function() {
        this.place.append(
                '<div class="alert alert-' + this.type + ' alert-dismissible fade in" data-dismiss="alert" role="alert" id="message' + this.id + '" >' +
                '<strong>' + this.title + '</strong> ' + this.text + ' &nbsp; ' +
                '</div>'
                );

        this.place.find("#message" + this.id).hide();
        this.place.find("#message" + this.id).show(200);
        
        this.place.find("#message" + this.id).on('close.bs.alert', function () {
            $(this).hide(200);
        });
        
        if (this.timeout) {
            setTimeout(function() {
                this.place.find("#message" + this.id).alert('close');
            }.bind(this), this.timeout);
        }

    };
}


