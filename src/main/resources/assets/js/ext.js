Date.prototype.isToday = function() {
    return this.equalsDate(new Date());
}

Date.prototype.tomorrow = function() {
    var d = new Date(this.getTime());
    d.setDate(d.getDate() + 1);
    return d;
}

Date.min = function(a, b) {
    return (a < b) ? a : b;
}

Date.max = function(a, b) {
 return (a > b) ? a : b;
}

Date.prototype.equalsDate = function(date) {
    return this.toDateString() == date.toDateString();
}

Array.prototype.remove = function(item) {
    this.splice(this.indexOf(item), 1);
}

Array.prototype.clear = function() {
    this.splice(0, this.length);
}