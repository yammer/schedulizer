// Class to manipulate a selection of days, in which day is an object defined in calendar_controller.js

function DaysSelection() {
    this.days = [];
};

DaysSelection.prototype.each = function(f) {
    _.each(this.days, f, this);
}

DaysSelection.prototype.select = function(days) {
    console.log("selection.select(", days.length, days, ")")
    _.each(days, function(day) {
        day.previousSelectedState = day.selected;
        day.selected = true;
        if (!this.contains(day)) {
            this.days.push(day);
        }
    }, this);
};

DaysSelection.prototype.unselect = function(days) {
    console.log("selection.unselect(", days.length, days, ")")
    _.each(days, function(day) {
        day.previousSelectedState = day.selected;
        day.selected = false;
        if (this.contains(day)) {
          this.days.remove(day);
        }
    }, this);
};

DaysSelection.prototype.clear = function() {
    console.log("selection.clear()")
    _.each(this.days, function(day) {
        day.previousSelectedState = true;
        day.selected = false;
    }, this);
    this.days.clear();
};

DaysSelection.prototype.contains = function(day) {
    return _.contains(this.days, day);
};

DaysSelection.prototype.resetToPreviousState = function(days) {
    console.log("selection.resetToPreviousState(", days.length, days, ")")
    _.each(days, function(day) {
        day.selected = day.previousSelectedState;
        if (day.selected && !this.contains(day)) {
            this.days.push(day);
        } else if (!day.selected && this.contains(day)) {
            this.days.remove(day);
        }
    }, this);
};

DaysSelection.prototype.dates = function() {
    return _.map(this.days, function(day) {
        return day.date;
    }, this)
};

DaysSelection.dateRange = function(a, b) {
    var i = Date.min(a, b);
    b = Date.max(a, b);
    var range = []
    while (i < b || i.equalsDate(b)) {
        // Because Date holds a moment (timestamp) the last iteration relies on the time of each
        // date object, hence we make sure to include the last day here
        range.push(i);
        i = i.next();
    }
    return range;
};

