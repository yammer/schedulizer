App.controller('MyCalendarTabController', function ($scope, $timeout, $rootScope, Session, DayRestriction, DateUtils,
                                                    Utils, EmployeeAssignmentsResource, AVAILABILITY_STATES) {

    $scope.calendar = {};
    $scope.progressBar = {trigger: function() {}};
    $scope.availabilityStates = AVAILABILITY_STATES;
    $scope.getCellClass = function(day) {
        var classMap = {
            'available': !day.content || day.content.isAvailable(),
            'mid-available': day.content && day.content.isMidAvailable(),
            'not-available': day.content && day.content.isNotAvailable(),
        };
        if (day.content && day.content.assignments.length > 0) {
            classMap["assignment-count-" + day.content.assignments.length] = true;
        }
        return classMap;
    }

    $scope.getDayTooltip = function(day) {
        if (day.content == undefined) return undefined;
        if (day.content.dayRestriction == undefined) return undefined;
        if (day.content.dayRestriction.comment == "") return undefined;
        return day.content.dayRestriction.comment;
    };

    $scope.goToToday = function() {
        $scope.calendar.goToToday();
        $scope.dayStamp = DateUtils.TODAY;
        var today = $(".view-calendar .day.today");
        Utils.animate('tada', today);
    };

    $scope.availabilityFormStatus = 0; // 0 if false mask 1 if restriction is checked and mask 2 if comments changed
    $scope.AVAILABILITY_FORM_STATUS = {
        RESTRICTION_CHANGED: 1,
        COMMENT_CHANGED: 2,
        FORM_COMPLETE: 3
    };

    $scope.clearSelection = function() {
        $scope.availabilityFormStatus = 0;
        $scope.calendar.clearSelectedDays();
        $scope.selectedDays = [];
    };

    $scope.onComment = function() {
        $scope.availability.comment.valid = true;
        $scope.availabilityFormStatus |= $scope.AVAILABILITY_FORM_STATUS.COMMENT_CHANGED;
    };

    $scope.onCheckRestriction = function(state) {
        $scope.availability.state = state.label;
        $scope.availabilityFormStatus |= $scope.AVAILABILITY_FORM_STATUS.RESTRICTION_CHANGED;
    };

    $scope.dayStamp = DateUtils.TODAY;

    var DEFAULT_AVAILABILITY = function() {
        return {
            comment: {
                multiple: false,
                valid: true,
                text: ""
            },
            state: null
        };
    };

    // Holds the info to be submitted
    $scope.availability = DEFAULT_AVAILABILITY();

    $scope.selectedDays = [];
    $scope.selectedDay = undefined;

    $scope.onSelectDays = function(selection) {
        $scope.selectedDays = selection.getDays();
        if ($scope.availabilityFormStatus != $scope.AVAILABILITY_FORM_STATUS.FORM_COMPLETE) {
            $scope.availabilityFormStatus = 0;
            $scope.availability = DEFAULT_AVAILABILITY();
            $scope.availability.comment = getAvailabilityCommentFromDays($scope.selectedDays);
            $scope.availability.state = getAvailabilityStateLabelFromDays($scope.selectedDays);
        }
    };

    function getAvailabilityCommentFromDays(days) {
        var comments = _.chain(days)
            .map('content')
            .map(function(content) {
                return (content && content.dayRestriction && content.dayRestriction.comment)
                    ? content.dayRestriction.comment
                    : "";
            })
            .value();
        var uniq = _.uniq(comments).length == 1;
        var text = (uniq) ? comments[0] : "";
        return {valid: uniq, text: text};
    }

    function getAvailabilityStateLabelFromDays(days) {
        var states = _.chain(days)
            .map(getDayState)
            .uniq()
            .value();
        return (states.length == 1) ? states[0] : null;
    }

    function getDayState(day) {
        return (day.content) ? day.content.state() : 'available';
    }

    function isAnySelectedDayInState(stateLabel) {
        return _.any($scope.selectedDays, function(day) {return getDayState(day) == stateLabel});
    }

    $scope.getCommentPlaceholder = function() {
        return ($scope.availability.comment.valid) ? "Comments" : "Multiple values";
    };

    $scope.isStateToggleActive = function(stateLabel) {
        return ($scope.availability.state != null)
            ? $scope.availability.state == stateLabel
            : isAnySelectedDayInState(stateLabel);
    };

    $scope.canSubmitChange = function() {
        return $scope.availability.state != null && $scope.availability.comment.valid;
    };

    $scope.submitAvailabilityChange = function() {
        if (!$scope.canSubmitChange()) return;

        var dates = _.map($scope.selectedDays, function(day){
            return DateUtils.toISOLocalDateString(day.date);
        }).join(',');
        var restrictionLevel = _.find($scope.availabilityStates, function(state) {
            return state.label == $scope.availability.state;
        }).level;

        DayRestriction.save({
            employeeId: $scope.employeeId,
            dates: dates,
            comment: $scope.availability.comment.text,
            restriction_level: restrictionLevel
        }, function(dayRestrictions) {
            updateDayRestrictions(dayRestrictions);
            $scope.clearSelection();
        });
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

        var i = 2;
        var totalError = false;
        var wrappedTerminate = function(error) {
            totalError = totalError || error;
            if (--i == 0) terminate(totalError);
        };

        DayRestriction.query({
                employee_id: $scope.employeeId,
                start_date: DateUtils.toISOLocalDateString(startDate),
                end_date: DateUtils.toISOLocalDateString(endDate)
            }).$promise.then(function(dayRestrictions) {
                updateDayRestrictions(dayRestrictions);
                wrappedTerminate();
            }).catch(function(e) {
                wrappedTerminate(true);
        });

        EmployeeAssignmentsResource.query({
                employee_id: $scope.employeeId,
                start_date: DateUtils.toISOLocalDateString(startDate),
                end_date: DateUtils.toISOLocalDateString(endDate)
            }).$promise.then(function(assignments) {
                updateAssignments(assignments);
                wrappedTerminate()
            }).catch(function(e) {
                wrappedTerminate(true);
        });
    };

    $scope.hasAssignment = function(day) {
        return day && day.content && day.content.assignments && day.content.assignments.length > 0;
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
            daysMap[assignment.date].content.assignments.push(assignment);
            daysMap[assignment.date].content.assignments = _.unique(daysMap[assignment.date].content.assignments);
        });

    }

    var MyCalendarDayContent = function() {
        this.assignments = [];
    };

    MyCalendarDayContent.prototype.dayRestriction = null;
    MyCalendarDayContent.prototype.assignments = null;

    MyCalendarDayContent.prototype.state = function() {
        var state = _.find($scope.availabilityStates, function(s) {
            return this.dayRestriction && s.level == this.dayRestriction.restrictionLevel;
        }, this);
        return (state != null) ? state.label : 'available';
    };

    MyCalendarDayContent.prototype.isAvailable = function() {return this.state() == 'available';};
    MyCalendarDayContent.prototype.isMidAvailable = function() {return this.state() == 'mid-available';};
    MyCalendarDayContent.prototype.isNotAvailable = function() {return this.state() == 'not-available';};

    function indexDaysByISOString(days) {
        return _.indexBy(days, function(day) {
            return DateUtils.toISOLocalDateString(day.date);
        })
    }

    $scope.getMonthName = DateUtils.getMonthName;

    // TODO: Ugly hack!
    $timeout(function() {
        $rootScope.$broadcast('trigger-resize');
    }, 300);
});
