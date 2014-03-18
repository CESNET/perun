/**
 * Creates a table which can easily visualize JSON data
 */
PerunTable = {

    columnNames : [],
    
    columnTitles : [],
    
    values : [],
    
    type : "",
    /**
     * Returns a new instance of the table
     */
    create : function(){
        return $.extend(true, {}, this);
    },
    
    /**
     * Adds a new column. Name is the name of JSON
     */
    addColumn : function(name, title){
        this.columnNames.push(name);
        this.columnTitles.push(title);
    },
    
    
    /**
     * Adds the array with values
     */
    add : function(values){
        this.values = values;    
    },

    addList : function(values){
        this.values = values;    
        this.type = "list";
    },

    addListOfObjects : function(values){
        this.values = values;    
        this.type = "listOfObjects";
    },


    addArray : function(values){
        this.values = values;    
        this.type = "array";
    },


    /**
     * Draws the table and returns the HTML string
     */
    draw : function(){
        
        var html = "<table class=\"table table-striped table-bordered\">";
        
        // draw headers
        
        html += "<tr>";
        for(var i in this.columnTitles){
            var value = this.columnTitles[i];
            html += "    <th>" + value + "</th>";
        }
        html += "</tr>";
       
        if (this.type == "list") {
          for(var n in this.columnNames){
            var colName = this.columnNames[n];
            for (var i in this.values[colName]) {
              var rowHtml = "<tr>";
              rowHtml += "    <td>" + this.values[colName][i] + "</td>";
              rowHtml += "</tr>";
              html += rowHtml;
            }
          }
        }  else if (this.type == "array") {
          for (var n in this.columnNames){
            var colName = this.columnNames[n];
            var colNameParts = colName.split(".");
            for (var i in this.values[colNameParts[0]]) {
              var rowHtml = "<tr>";
              rowHtml += "    <td>" + i + "</td>";
              rowHtml += "    <td>" + this.values[colNameParts[0]][i] + "</td>";
              rowHtml += "</tr>";
              html += rowHtml;
            }
          }
        } else if (this.type == "listOfObjects") {
          for (var i in this.values) {
            var rowHtml = "<tr>";
            for (var n in this.columnNames){
            var colName = this.columnNames[n];
              var obj = this.values[i];
              rowHtml += "    <td>" + eval("obj." + colName) + "</td>";
            }
            rowHtml += "</tr>";
            html += rowHtml;
          }
        } else {
        // draw values
        for(var i in this.values){
           
            var row = this.values[i];
            
            var rowHtml = "<tr>";
        
            for(var n in this.columnNames){
            
                var colName = this.columnNames[n];
                var colNameParts = colName.split(".");
                var value = "";
                value = row;
                
                for(var o in colNameParts){
                    var localName = colNameParts[o];
                    if (value != null && typeof value[localName] != 'undefined') {
                      value = value[localName];
                      rowHtml += "    <td>" + value + "</td>";
                    }
                }
                
            
              }
            rowHtml += "</tr>";
            
            html += rowHtml;
          }
        }
        
        html += "</table>";
        
        return html;
    }    


}
