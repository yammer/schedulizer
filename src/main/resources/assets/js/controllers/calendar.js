function computeNWeeks(firstDay) {
    var lastDay = new Date(firstDay.getTime());
    /* Get first day of the next month */
    lastDay.setDate(lastDay.getDate() + 27);
    while(lastDay.getDate() != 1) {
        lastDay.setDate(lastDay.getDate() + 1);
    }
    /* Get last sunday */
    while(lastDay.getDay() != 0) {
        lastDay.setDate(lastDay.getDate() - 1);
    }
    /* Get number of days between two dates*/
    var diff =  Math.floor(( Date.parse(lastDay) - Date.parse(firstDay) ) / 86400000);
    return Math.ceil(diff/7.0);
}

App.controller('CalendarController', function ($scope) {
    $scope.calendar = [];
    console.log("prototyping");
    Date.prototype.getMonthName = function() {
        var monthNames = [ "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December" ];
        return monthNames[this.getMonth()];
    };

    var today = new Date();
    var weekday = today.getDay();
    var lastDay = new Date();
    var firstDay = new Date();
    lastDay.setDate(today.getDate() - 7*7 - today.getDay()); /* guaranteed to be Sunday */
    firstDay.setDate(today.getDate() - 50);
    var firstMonthStarted = false;
    for(var weekCount = 0; weekCount < 25; weekCount++) {
        var currentWeek = {};
        currentWeek.month = {
            begins: false, // to be filled
            name: lastDay.getMonthName(),
            nweeks: 0 // to be filled in case month begins
        };
        currentWeek.days = [];
        for(var dayCount = 0; dayCount < 7; dayCount++){
            if(lastDay.getDate() == 1) {
                firstMonthStarted = true;
                currentWeek.month.name = lastDay.getMonthName();
                currentWeek.month.begins = true;
                currentWeek.month.nweeks = computeNWeeks(new Date(lastDay.getTime()));
            }
            currentWeek.days.push({value: lastDay.getDate()});
            lastDay.setDate(lastDay.getDate() + 1);
        }
        if(firstMonthStarted) {
            $scope.calendar.push(currentWeek);
        }
    }
});

App.directive('calendar', function() {
    return {
        restrict: 'E',
        templateUrl: 'html/partials/calendar.html',
        controller: 'CalendarController'
    };
});