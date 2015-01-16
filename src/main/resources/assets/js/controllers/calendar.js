var _MS_PER_DAY = 1000 * 60 * 60 * 24;

// a and b are javascript Date objects
function dateDiffInDays(a, b) {
    // Discard the time and time-zone information.
    var utc1 = Date.UTC(a.getFullYear(), a.getMonth(), a.getDate());
    var utc2 = Date.UTC(b.getFullYear(), b.getMonth(), b.getDate());

    return Math.floor((utc2 - utc1) / _MS_PER_DAY);
}

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
    var diff =  dateDiffInDays(firstDay, lastDay);


    return Math.ceil(diff/7.0);
}

function initWeek(day){
    var currentWeek = {};
    currentWeek.month = {
        begins: false, // to be filled
        name: day.getMonthYear(),
        nweeks: 0 // to be filled in case month begins
    };
    currentWeek.days = [];
    return currentWeek;
}

function initMonth(day01) {
    var month = {};
    month.name = day01.getMonthYear();
    month.begins = true;
    month.nweeks = computeNWeeks(new Date(day01.getTime()));
    return month;
}

App.controller('CalendarController', function ($scope) {
    $scope.calendar = [];
    Date.prototype.getMonthName = function() {
        var monthNames = [ "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December" ];
        return monthNames[this.getMonth()];
    };
    Date.prototype.getMonthYear = function() {
        return this.getMonthName() + " / " + this.getFullYear();
    };

    $scope.firstDay = new Date();
    $scope.firstDay.setDate(1); // beginning of the month
    $scope.firstDay.setDate($scope.firstDay.getDate() - $scope.firstDay.getDay()); // sunday
    $scope.lastDay = new Date($scope.firstDay.getTime())

    $scope.loadPreviousMonth = function() {
        var isBegginingOfMonth = false;
        do {
            $scope.firstDay.setDate($scope.firstDay.getDate() - 7);
            var currentWeek = initWeek($scope.firstDay);
            for(var dayCount = 0; dayCount < 7; dayCount++) {
                if($scope.firstDay.getDate() == 1) {
                    isBegginingOfMonth = true;
                    currentWeek.month = initMonth($scope.firstDay);
                }
                currentWeek.days.push({ value: $scope.firstDay.getDate() });
                $scope.firstDay.setDate($scope.firstDay.getDate() + 1);
            }
            $scope.calendar.unshift(currentWeek);
            $scope.firstDay.setDate($scope.firstDay.getDate() - 7);
        } while(!isBegginingOfMonth);
        // TODO: CHANGE THE HARD CODED 75px AND STOP USING JQUERY (FIND A BETTER WAY)
        var elemHeight = $(".calendarcell").outerHeight();
        var h = (elemHeight > 0) ? elemHeight : 75;
        return h * $scope.calendar[0].month.nweeks; // size of the chunk
    }

    $scope.loadNextMonth = function() {
        var isBegginingOfMonth = false;
        if($scope.calendar.length > 0 ) {
            $scope.calendar[$scope.calendar.length - 1].month.name = $scope.lastDay.getMonthYear();
        }
        do {
            var currentWeek = initWeek($scope.lastDay);
            for(var dayCount = 0; dayCount < 7; dayCount++) {
                if($scope.lastDay.getDate() == 1) {
                    isBegginingOfMonth = true;
                    currentWeek.month = initMonth($scope.lastDay);
                }
                currentWeek.days.push({ value: $scope.lastDay.getDate() });
                $scope.lastDay.setDate($scope.lastDay.getDate() + 1);
            }
            $scope.calendar.push(currentWeek);
        } while(!isBegginingOfMonth);
        $scope.calendar[$scope.calendar.length - 1].month.name = "";
    }

    $scope.initCalendar = function() {
        $scope.loadNextMonth();
        $scope.loadNextMonth();
        $scope.loadNextMonth();
        $scope.loadPreviousMonth();
        $scope.loadPreviousMonth();
    }

    $scope.initCalendar();


});

App.directive('calendar', function() {
    return {
        restrict: 'E',
        templateUrl: 'html/partials/calendar.html',
        controller: 'CalendarController'
    };
});