var calendarUtils = angular.module('calendarUtils', []);

calendarUtils.factory('DateUtils', [function() {
    var TODAY = new Date();
    MS_PER_DAY = 1000 * 60 * 60 * 24;
    var monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September",
                    "October", "November", "December"];
    return {
        TODAY: TODAY,
        MS_PER_DAY: MS_PER_DAY,
        SORT_BY: function(d) {
            //noinspection JSConstructorReturnsPrimitive
            return d.getTime();
        },
        min: function(a, b) {
            return (a < b) ? a : b;
        },
        max: function(a, b) {
         return (a > b) ? a : b;
        },
        isRange: function(dates) {
            if (dates.length == 0) return true;
            var min = _.min(dates, this.SORT_BY);
            var max = _.max(dates, this.SORT_BY);
            return this.differenceInDays(min, max) + 1 == dates.length;
        },
        fromISOLocalString: function(string) {
            var parts = string.split("-");
            var d = new Date();
            d.setYear(parseInt(parts[0]));
            d.setMonth(parseInt(parts[1]) - 1, parseInt(parts[2]));
            return d;
        },
        firstDayOfThisMonth: function() {
            var d = new Date(TODAY.getTime());
            d.setDate(1);
            return d;
        },
        plusDays: function(date, days) {
            var d = this.clone(date);
            d.setDate(d.getDate() + days);
            return d;
        },
        plusWeeks: function(date, weeks) {
            return this.plusDays(date, weeks * 7);
        },
        clone: function(date) {
            return new Date(date.getTime());
        },
        differenceInDays: function(from, to) {
            // Discard the time and time-zone information.
            var utc1 = Date.UTC(from.getFullYear(), from.getMonth(), from.getDate());
            var utc2 = Date.UTC(to.getFullYear(), to.getMonth(), to.getDate());
            return Math.floor((utc2 - utc1) / MS_PER_DAY);
        },
        isToday: function(date) {
            return this.equalsDate(date, TODAY);
        },
        nextDay: function(date) {
            return this.plusDays(date, 1);
        },
        previousDay: function(date) {
            return this.plusDays(date, -1);
        },
        lastSunday: function(date) {
            return this.plusDays(date, -date.getDay());
        },
        equalsDate: function(date1, date2) {
            return date1.toDateString() == date2.toDateString();
        },
        getMonthName: function(date) {
            return monthNames[date.getMonth()];
        },
        toISOLocalDateString: function(date) {
            var pad = function(n) {
                if (n < 10) return '0' + n;
                return n;
            }
            return date.getFullYear() +
                '-' + pad(date.getMonth() + 1) +
                '-' + pad(date.getDate());
        }
    }
}]);
