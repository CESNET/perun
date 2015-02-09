/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function Modal(title, name, place) {
    this.self;
    this.name = name;
    this.place = place;
    this.title = title;
    this.body = "";
    this.footer = "";
    this.type = "default";
    
    this.setType = function (type) {
        this.type = type;
    }
    
    this.addBody = function (body) {
        this.body += body;
        this.self.find(".modal-body").append(body);
    };
    
    this.addFooter = function (footer) {
        if (!this.footer || this.footer.length <= 0) {
            var html;
            html  = '      <div class="modal-footer">';
            html += '      </div>';
            this.self.find(".modal-content").append(html);
        }
        this.footer += footer;
        this.self.find(".modal-footer").append(footer);
    };
    
    this.getSelf = function () {
        return this.self;
    };
    
    this.clear = function() {
        this.body = "";
        this.footer = "";
        this.self.find(".modal-body, .modal-footer").html("");
    };
    
    this.init = function () {
        var html;
        html = '<div id="' + this.name + '" class="modal">';
        html += '  <div class="modal-dialog modal-' + this.type + '">';
        html += '    <div class="modal-content">';
        if (this.title && this.title.length > 0) {
            html += '    <div class="modal-header">';
            html += '       <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">';
            html += '          &times;</span><span class="sr-only">Close</span>';
            html += '       </button>';
            html += '      <h4 class="modal-title">';
            html += this.title;
            html += '      </h4>';
            html += '    </div>';
        }
        html += '      <div class="modal-body">';
        html += this.body;
        html += '      </div>';
        if (this.footer && this.footer.length > 0) {
            html += '      <div class="modal-footer">';
            html += this.footer;
            html += '      </div>';
        }
        html += '    </div><!-- /.modal-content -->';
        html += '  </div><!-- /.modal-dialog -->';
        html += '</div><!-- /.modal -->';
        this.place.find(".modal#" + this.name).remove();
        this.place.append(html);
        this.self = this.place.find(".modal#" + this.name);
    };
}