App.controller('MyCalendarTabController', function ($scope, $timeout, $rootScope, AssignableDay) {

    $scope.calendar = {};
    $scope.progressBar = {};

    $scope.goToToday = function() {
        $scope.calendar.goToToday();
        $scope.dayStamp = new Date();
    }

    $scope.clearSelection = function() {
        $scope.calendar.clearSelectedDays();
        $scope.selectedDays = [];
    }

    $scope.dayStamp = new Date(); /* Extract TODAY constant from calendar stuff */

    $scope.selectedDays = [];
    $scope.selectedDay = undefined;

    $scope.onSelectDays = function(selection) {
        $scope.selectedDays = selection.dates();
    }

    $scope.onHoverDay = function(day) {
        $scope.dayStamp = day.date;
        $scope.hoveredDay = day;
    }

    $scope.onLoadDayContent = function(terminate, days) {
        var startDate = days[0].date;
        var endDate = days[days.length - 1].date;
        var daysMap = indexDaysByISOString(days);
        $scope.progressBar.trigger();
        $timeout(terminate, 100);
        //terminate();
    }

    function indexDaysByISOString(days) {
        return _.indexBy(days, function(day) {
            return day.date.toISOLocalDateString();
        })
    }

    // TODO: Ugly hack!
    $timeout(function() {
        $rootScope.$broadcast('trigger-resize');
    }, 300);
});
