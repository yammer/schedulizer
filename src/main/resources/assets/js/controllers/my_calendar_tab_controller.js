App.controller('MyCalendarTabController', function ($scope, $timeout, $rootScope, ProgressBar, AssignableDay) {

    $scope.calendar = {};

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

    // TODO: <extract>

    $scope.progressBar = {inner: null/* .st-progress */, outer: null/* .st-progress-bar */}

    // After errorThreshold number of errors that we consider an error worth displaying the user
    var errorHits = 0;
    var errorThreshold = 5;

    function progressWatcher() {
        var status = $scope.calendar.loadingStatus();
        if (status.weeks.loaded < status.weeks.total && !status.active) {
            if (errorHits >= errorThreshold) {
                return -1;
            } else {
                errorHits++;
            }
        } else {
            errorHits = 0;
            var d = $scope.progressBar.previousLoadedWeeks;
            if (status.weeks.total - d <= 0) return 1;
            var p = Math.max(0, status.weeks.loaded - d) / Math.max(0, status.weeks.total - d);
            return p;
        }
    }

    function onBeforeWatch() {
        var status = $scope.calendar.loadingStatus().weeks;
        $scope.progressBar.previousLoadedWeeks = status.loaded;
    }

    $scope.$watchGroup(['progressBar.inner', 'progressBar.outer'], function(values) {
        var bar = $scope.progressBar;
        if (bar.inner == null || bar.outer == null) return;
        progressBar = new ProgressBar(bar.inner, bar.outer, progressWatcher, {
            onBeforeWatch: onBeforeWatch
        });
    });

    // TODO: </extract>

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
       // progressBar.trigger();
        terminate();
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
