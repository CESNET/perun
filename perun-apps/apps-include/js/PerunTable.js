/**
 * Creates a table which can easily visualize JSON data
 */
function PerunTable() {
    this.columns = [];  // required attr: type("button", "number", "icon", ...), title
    this.values = [];
    this.clicableRows = {isClicable: false, id: "", prefix: "row-", toggle: false};
    this.hasHeader = true;
    /**
     * Returns a new instance of the table
     */
    /**
     * Adds a new column. Name is the name of JSON
     */
    this.addColumn = function (column) {
        this.columns.push(column);
    }
    this.setClicableRows = function (clicableRows) {
        this.clicableRows = clicableRows;
    }
    this.setHasHeader = function (hasHeader) {
        this.hasHeader = hasHeader;
    }

    /**
     * Adds the array with values
     */
    this.setValues = function (values) {
        this.values = values;
    }
    this.setList = function (values) {
        for (var id in values) {
            this.values.push({key: id, value: values[id]});

        }
    }
    this.addValue = function (value) {
        this.values.push(value);
    }

    /**
     * Draws the table and returns the HTML string
     */
    this.draw = function () {
        if (this.clicableRows.isClicable) {
            var html = "<table class=\"table table-bordered table-hover\">";
        } else {
            var html = "<table class=\"table table-bordered\">";
        }

        // draw headers
        if (this.hasHeader) {
            html += "<thead><tr>";
        } else {
            html += "<thead style='display: none;'><tr>";
        }
        for (var i in this.columns) {
            var value = this.columns[i].title;
            html += "<th class='col-" + this.columns[i].type + "'>" + value + "</th>";
        }
        html += "</tr></thead>";

        html += "<tbody>";


        for (var row in this.values) {
            if (this.clicableRows.isClicable) {
                html += "<tr class='clicable' id='" + this.clicableRows.prefix + this.values[row][this.clicableRows.id] + "'>";
            } else {
                html += "<tr>";
            }
            for (var id in this.columns) {
                var column = this.columns[id];
                html += "<td class='col-" + column.type + "'>";
                switch (column.type) {
                    case "button":
                        html += (new TableButton(this.values[row][column.btnId], column.btnName, column.btnText, column.btnType)).html();
                        break;
                    case "button2":
                        var btn = this.values[row][column.button];
                        if (btn) {
                            html += btn.html();
                        }
                        break;
                    case "number":
                        html += (1 + parseInt(row));
                        break;
                    case "boolean":
                        if (this.values[row][column.name] === true) {
                            html += "<i class='glyphicon glyphicon-ok true'></i>";
                        } else if (this.values[row][column.name] === false) {
                            html += "<i class='glyphicon glyphicon-remove false'></i>";
                        } else {
                            html += " ";
                        }
                        break;
                    case "icon":
                        if (!this.values[row][column.name]) {
                            html += "";
                        } else {
                            html += "<i class='glyphicon " + this.values[row][column.name] + "' title='" + column.description + "' data-toggle='tooltip'></i>";
                            //need active tooltip after table will be draw -- table.find('[data-toggle="tooltip"]').tooltip();
                        }
                        break;
                    case "list":
                        for (var i in this.values[row][column.name]) {
                            html += htmlEncode(this.values[row][column.name][i]);
                            if (i != this.values[row][column.name].length-1) {
                                html += ", ";
                            }
                        }
                        break;
                    case "table":
                        html += this.values[row][column.name].draw();
                        break;
                    case "date":
                        if (this.values[row][column.name] instanceof Date) {
                            html += this.values[row][column.name].toLocaleDateString();
                        } else {
                            html += this.values[row][column.name];
                        }
                        break;
                    case "html":
                        if (this.values.length == 0) {
                            break;
                        }
                        html += this.values[row][column.name];
                        break;
                    case "text":
                        if (this.values.length == 0) {
                            break;
                        }
                        html += htmlEncode(this.values[row][column.name]);
                        break;
                    default :
                        if (this.values.length == 0) {
                            break;
                        }
                        html += htmlEncode(this.values[row][column.name]);
                        break;
                }
                html += "</td>";
            }
            html += "</tr>";
            if (this.clicableRows.toggle) {
                if (this.clicableRows.isClicable) {
                    html += "<tr class='toggle' id='toggle-" + this.clicableRows.prefix + this.values[row][this.clicableRows.id] + "'>";
                } else {
                    html += "<tr>";
                }
                html += "<td colspan='" + this.columns.length + "'>";
                html += '<div class="toggle-wrap"  style="display:none;"><div class="progress" id="">';
                html += '  <div class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" ';
                html += '    aria-valuemax="100" style="width: 0%;">';
                html += '    0%';
                html += '  </div>';
                html += '</div></div>';
                html += "</td>";
                html += "</tr>";
            }
        }
        html += "</tbody></table>";
        return html;
    }
}
;



function TableButton(id, name, title, type, action) {
    this.id = id;
    this.name = name;
    this.title = title;
    this.type = type;
    this.action = action;
    this.disabled = false;

    this.html = function () {
        var onclick = "";
        var disabled = "";
        if (this.action) {
            onclick = ' onclick="' + this.action.fnc.name + '(' + this.action.params + ')" ';
        }
        if (this.disabled) {
            disabled = ' disabled ';
        }
        var html = '<button ' + onclick + disabled + ' id="' + this.name + "-" + this.id + '" class="btn btn-' + this.type + '">' + this.title + '</button>';
        return html;
    };

    this.getAction = function () {
        return this.action;
    };

    this.setDisabled = function(disabled) {
        this.disabled = disabled;
    }
}