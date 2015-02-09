/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function Authorization(roles, vo, group) {
    this.roles = roles;
    this.vo = vo;
    this.group = group;
    this.objects = [];
    
    this.addObject = function(object, rights) {
        this.objects.push({object: object, rights: rights});
    };
    
    this.check = function() {
        for(var i in this.objects) {
            this.objects[i].object.hide();
            for(var j in this.objects[i].rights) {
                if (this.hasRole(this.objects[i].rights[j])) {
                    this.objects[i].object.show();
                    break;
                }
            }
        }
    };
    
    this.hasRole = function(role) {
        if (!this.roles[role]) {
            return false;
        }
        switch(role) {
            case "PERUNADMIN":
                return true;
            break;
            case "VOOBSERVER":
                if (!this.vo) {
                    return false;
                }
                for(var i in this.roles[role].Vo) {
                    if (this.roles[role].Vo[i] === this.vo.id) {
                        return true;
                    }
                }
            break;
            case "VOADMIN":
                if (!this.vo) {
                    return false;
                }
                for(var i in this.roles[role].Vo) {
                    if (this.roles[role].Vo[i] === this.vo.id) {
                        return true;
                    }
                }
            break;
            case "TOPGROUPCREATOR":
                if (!this.vo) {
                    return false;
                }
                for(var i in this.roles[role].Vo) {
                    if (this.roles[role].Vo[i] === this.vo.id) {
                        return true;
                    }
                }
            break;
            case "GROUPADMIN":
                if (!this.group) {
                    return false;
                }
                for(var i in this.roles[role].Group) {
                    if (this.roles[role].Group[i] === this.group.id) {
                        return true;
                    }
                }
            break;
            default:
                return false;
            break;
        }
        return false;
    }
}