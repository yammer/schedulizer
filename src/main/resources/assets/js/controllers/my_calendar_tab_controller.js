App.controller('MyCalendarTabController', function ($scope, $timeout, $rootScope, Session, DayRestriction, EmployeeAssignmentsResource) {

    $scope.calendar = {};
    $scope.progressBar = {trigger: function() {}};
    $scope.availabilityStates = [
        {
            className: "available",
            title: "Available",
            glyphicon: 'glyphicon-ok'
        },
        {
            className: "mid-available",
            title: "Partially Available",
            glyphicon: 'glyphicon-minus'
        },
        {
            className: "not-available",
            title: "Not Available",
            glyphicon: 'glyphicon-remove'
        }
    ];

    $scope.goToToday = function() {
        $scope.calendar.goToToday();
        $scope.dayStamp = Date.TODAY;
    };

    $scope.clearSelection = function() {
        $scope.calendar.clearSelectedDays();
        $scope.selectedDays = [];
    };

    $scope.dayStamp = Date.TODAY; 

    $scope.selectedDays = [];
    $scope.selectedDay = undefined;

    $scope.onSelectDays = function(selection) {
        $scope.selectedDays = selection.dates();
    };

    $scope.onHoverDay = function(day) {
        $scope.dayStamp = day.date;
        $scope.hoveredDay = day;
    };

    $scope.employeeId = null;

    $scope.$watch(function() {return Session;}, function(Session) {
        if (Session != null && Session.userId != null) {
            $scope.employeeId = Session.userId;
            $scope.progressBar.trigger();
        }
    }, true);

    $scope.onLoadDayContent = function(terminate, days) {
        var startDate = days[0].date;
        var endDate = days[days.length - 1].date;

        if ($scope.employeeId == null) {
            return terminate(true);
        }

        $scope.progressBar.trigger();
        var partiallyFetched = false;

        DayRestriction.query({
                employee_id: $scope.employeeId,
                start_date: startDate.toISOLocalDateString(),
                end_date: endDate.toISOLocalDateString()
            }).$promise.then(function(dayRestrictions) {
                updateDayRestrictions(dayRestrictions);
                if (partiallyFetched && terminate) {
                    terminate();
                }
                else {
                    partiallyFetched = true;
                }
            }).catch(function(e) {
                terminate(true);
                terminate = undefined; // dont allow terminate to be called twice
        });

        EmployeeAssignmentsResource.query({
                employee_id: $scope.employeeId,
                start_date: startDate.toISOLocalDateString(),
                end_date: endDate.toISOLocalDateString()
            }).$promise.then(function(assignments) {
                updateAssignments(assignments);
                if (partiallyFetched && terminate) {
                    terminate();
                }
                else {
                    partiallyFetched = true;
                }
            }).catch(function(e) {
                terminate(true);
                terminate = undefined;
        });
    };

    function updateDayRestrictions(dayRestrictions) {
        var dates = _.map(dayRestrictions, function(d) {return d.getDate();});
        var daysMap = indexDaysByISOString($scope.calendar.getDays(dates));
        _.each(dayRestrictions, function(dayRestriction) {
            if (daysMap[dayRestriction.date].content == undefined) {
                daysMap[dayRestriction.date].content = new MyCalendarDayContent();
            }
            daysMap[dayRestriction.date].content.dayRestriction = dayRestriction;
        });

    }

    function updateAssignments(assignments) {
        var dates = _.map(assignments, function(a) {return a.getDate();});
        var daysMap = indexDaysByISOString($scope.calendar.getDays(dates));
        _.each(assignments, function(assignment) {
            if (daysMap[assignment.date].content == undefined) {
                daysMap[assignment.date].content = new MyCalendarDayContent();
            }
            daysMap[assignment.date].content.assignment = assignment;
        });

    }

    var MyCalendarDayContent = function() {};

    MyCalendarDayContent.prototype.dayRestriction = null;
    MyCalendarDayContent.prototype.assignment = null;

    MyCalendarDayContent.prototype.isAvailable = function() {
        return this.dayRestriction == undefined || this.dayRestriction.restrictionLevel == 0;
    }

    MyCalendarDayContent.prototype.isMidAvailable = function() {
        return this.dayRestriction && this.dayRestriction.restrictionLevel == 1;
    };

    MyCalendarDayContent.prototype.isNotAvailable = function() {
        return this.dayRestriction && this.dayRestriction.restrictionLevel == 2;
    };

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
