App.controller('CalendarProgressBarController', function ($scope, ProgressBar) {

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

    var progressBar = null;

    $scope.$watchGroup(['progressBar.inner', 'progressBar.outer'], function(values) {
        var bar = $scope.progressBar;
        if (bar.inner == null || bar.outer == null) return;
        progressBar = new ProgressBar(bar.inner, bar.outer, progressWatcher, {
            onBeforeWatch: onBeforeWatch
        });
    });

    $scope.api.trigger = function () {
        if (progressBar) {
            progressBar.trigger();
        }
    }
});


App.directive('calendarProgressBar', function() {
    return {
        restrict: 'E',
        scope: {
            calendar: '=',
            api: '=exposeApiTo'
        },
        templateUrl: 'views/calendar_progress_bar.html',
        controller: 'CalendarProgressBarController'
    };
});