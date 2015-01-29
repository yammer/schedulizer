Date.MS_PER_DAY = 1000 * 60 * 60 * 24;

Date.TODAY = new Date();

Date.min = function(a, b) {
    return (a < b) ? a : b;
};

Date.max = function(a, b) {
 return (a > b) ? a : b;
};

Date.firstDayOfThisMonth = function() {
    var d = new Date();
    d.setDate(1);
    return d;
};

Date.prototype.plusDays = function(days) {
    var d = this.clone();
    d.setDate(d.getDate() + days);
    return d;
};

Date.prototype.plusWeeks = function(weeks) {
    return this.plusDays(weeks * 7);
};

Date.prototype.clone = function() {
    return new Date(this.getTime());
};

Date.differenceInDays = function(from, to) {
    // Discard the time and time-zone information.
    var utc1 = Date.UTC(from.getFullYear(), from.getMonth(), from.getDate());
    var utc2 = Date.UTC(to.getFullYear(), to.getMonth(), to.getDate());
    return Math.floor((utc2 - utc1) / Date.MS_PER_DAY);
};

Date.prototype.isToday = function() {
    return this.equalsDate(Date.TODAY);
};

Date.prototype.next = function() {
    return this.plusDays(1);
};

Date.prototype.previous = function() {
    return this.plusDays(-1);
};

Date.prototype.lastSunday = function() {
    return this.plusDays(-this.getDay());
};

Date.prototype.equalsDate = function(date) {
    return this.toDateString() == date.toDateString();
};

Date.prototype.getMonthName = function() {
    var monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September",
        "October", "November", "December"];
    return monthNames[this.getMonth()];
};

Date.prototype.toISOLocalDateString = function() {
    var pad = function(n) {
        if (n < 10) return '0' + n;
        return n;
    }
    return this.getFullYear() +
        '-' + pad(this.getMonth() + 1) +
        '-' + pad(this.getDate());
}

Array.prototype.remove = function(item) {
    this.splice(this.indexOf(item), 1);
};

Array.prototype.clear = function() {
    this.splice(0, this.length);
};
