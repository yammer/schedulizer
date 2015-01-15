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

function initWeek(day){
    var currentWeek = {};
    currentWeek.month = {
        begins: false, // to be filled
        name: day.getMonthName(),
        nweeks: 0 // to be filled in case month begins
    };
    currentWeek.days = [];
    return currentWeek;
}

function initMonth(day01) {
    var month = {};
    month.name = day01.getMonthName();
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
            $scope.calendar[$scope.calendar.length - 1].month.name = $scope.lastDay.getMonthName();
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
App.directive('scroller', function () {
    return {
        restrict: 'A',
        scope: {
            nextChunkMethod: "&",
            previousChunkMethod: "&"
        },
        link: function ($scope, elem, attrs) {
            rawElement = elem[0];

            elem.bind('scroll', function () {
                if((rawElement.scrollTop + rawElement.offsetHeight+5) >= rawElement.scrollHeight &&
                    $scope.nextChunkMethod){
                    $scope.$apply($scope.nextChunkMethod);
                }
                if(rawElement.scrollTop == 0 &&
                    $scope.previousChunkMethod){
                    $scope.$apply(function() {
                        var chunkSize = $scope.previousChunkMethod();
                        if(chunkSize) {
                            rawElement.scrollTop = chunkSize;
                        }
                        else {
                            setTimeout(function () {
                                rawElement.scrollTop = rawElement.scrollHeight - previousHeight;
                            }, 0);
                        }
                    });

                }
            });
        }
    };
});
