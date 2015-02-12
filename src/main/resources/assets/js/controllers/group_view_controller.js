App.controller('GroupViewController', function($scope, $timeout, $rootScope, $dialogs, Utils, ProgressBar,
                                               Group, AssignmentType, AssignableDay, EMPTY_GROUP) {

    var NEW_EMPLOYEE = {name: undefined, image: undefined}

    // Will hold the calendar api
    $scope.calendar = null;

    $scope.$watchCollection('selectedGroup.assignmentTypes', function(assignmentTypes) {
        if (assignmentTypes == null || assignmentTypes.$resolved == false) return;
        if ($scope.calendar != null) {
            $scope.calendar.invalidateAssignments();
        }
    });

    $scope.assignmentTypeBuckets = {};

    function getAssignmentTypeData(group) {
        if (group == EMPTY_GROUP) {
            group.assignmentTypes = [];
            return;
        }
        group.assignmentTypes = AssignmentType.query({group_id: group.id}, function(assignmentTypes) {
            group.setAssignmentTypes(assignmentTypes);
        });
    }

    var hideInput = null;

    $scope.assignmentTypeInput = null;

    $scope.addAssignmentTypeButtonClick = function() {
        if ($scope.isCreatingAssignmentType) {
            if ($scope.addAssignmentType()) {
                focusOnAssignmentTypeInput();
            } else {
                $scope.isCreatingAssignmentType = false;
            }
        } else {
            $scope.isCreatingAssignmentType = true;
        }
    }

    $scope.assignmentTypeInputEnter = function() {
        if (!$scope.addAssignmentType()) {
            Utils.shakeOnError($scope.assignmentTypeInput);
        }
    }

    $scope.addAssignmentType = function () {
        var name = $scope.newAssignmentTypeName;
        if (name == null || name == "") {
            return false;
        }
        var group = $scope.selectedGroup;
        var assignmentType = new AssignmentType({ groupId: group.id });
        assignmentType.name = name;
        assignmentType.$save({}, function() {
            group.addAssignmentType(assignmentType);
            $scope.newAssignmentTypeName = "";
            initAssignmentTypeBucket(assignmentType);
        });
        return true;
    }

    $scope.onInputBlur = function() {
        hideInput = $timeout(function(){
            $scope.isCreatingAssignmentType = false;
        }, 200);
    }

    function focusOnAssignmentTypeInput() {
        $timeout.cancel(hideInput);
        $scope.assignmentTypeInput.focus();
    }

    $scope.deleteAssignmentType = function(assignmentType) {
        var confirm = $dialogs.confirm('Please confirm',
                                       'Are you sure you want to delete this assignment type?<br>' +
                                       'Bad things may happen, ' +
                                       'because this operation can not be undone!<br>' +
                                       'All the assignments related to this assignment type will also be deleted.');

        confirm.result.then(function(btn){
            var doubleConfirm = $dialogs.confirm('Please confirm again',
                                                 'Are you really sure?');
            doubleConfirm.result.then(function(btn2) {
                var group = $scope.selectedGroup;
                assignmentType.groupId = group.id;
                assignmentType.$delete({}, function() {
                    group.assignmentTypes.remove(assignmentType);
                    delete $scope.assignmentTypeBuckets[assignmentType.id];
                });
                focusOnAssignmentTypeInput();
            });
        });
    }

    $scope.calendar = null;

    $scope.goToToday = function() {
        // TODO: Find another way to do this
        $scope.calendar.goToToday();
        $scope.dayStamp = new Date();
    }

    $scope.dayStamp = new Date(); /* Extract TODAY constant from calendar stuff */

    $scope.onHoverDay = function(day) {
        $scope.dayStamp = day.date;
        $scope.hoveredDay = day;
        // Adding empty assignments when there is no assignment in a given assignment type
        if ($scope.hasAssignment($scope.hoveredDay)) {
            angular.forEach($scope.selectedGroup.assignmentTypes, function(assignmentType) {
                if ($scope.hoveredDay.content.assignments[assignmentType.id] == undefined) {
                    $scope.hoveredDay.content.assignments[assignmentType.id] = [];
                }
            });
        }
    }

    $scope.hasAssignment = function(day) {
        return day && day.content && day.content.assignments && Object.keys(day.content.assignments).length > 0;
    }

    $scope.orderHoveredDayBy = function(key){
        return $scope.selectedGroup.assignmentTypeFor(key).name.toLowerCase();
    }

    $scope.clearSelection = function() {
        $scope.calendar.clearSelectedDays();
        $scope.selectedDays = [];
    }

    $scope.selectedDays = [];
    $scope.selectedDay = undefined;

    function initAssignmentTypeBucket(assignmentType) {
        $scope.assignmentTypeBuckets[assignmentType.id] = {
            employeeList: {},
            assignmentType: assignmentType
        };
    }

    function initAssignmentBuckets() {
        var assignmentTypes = $scope.selectedGroup.assignmentTypes;
        for (var i = 0; i < assignmentTypes.length; i++) {
            initAssignmentTypeBucket(assignmentTypes[i]);
        }
    }

    function updateAssignmentTypeBuckets(assignableDays) {
        for (var i = 0; i < assignableDays.length; i++) {
            for (var j = 0; j < assignableDays[i].assignments.length; j++) {
                var assignmentTypeId = assignableDays[i].assignments[j].assignmentTypeId;
                var employeeId = assignableDays[i].assignments[j].employeeId;
                var employee = $scope.selectedGroup.employeeFor(employeeId);
                if ($scope.assignmentTypeBuckets[assignmentTypeId].employeeList[employeeId] == undefined) {
                    $scope.assignmentTypeBuckets[assignmentTypeId].employeeList[employeeId] = {
                        employee: employee,
                        assignments: []
                    };
                }
                $scope
                    .assignmentTypeBuckets[assignmentTypeId]
                    .employeeList[employeeId]
                    .assignments.push(assignableDays[i].assignments[j].id);

                $scope.assignmentTypeBuckets[assignmentTypeId].employeeList[employeeId].assignments =
                    _.unique($scope.assignmentTypeBuckets[assignmentTypeId].employeeList[employeeId].assignments);
            }
        }
    }

    $scope.onSelectDays = function(selection) {
        $scope.selectedDays = selection.dates();
        $scope.assignmentTypeBuckets = {};
        initAssignmentBuckets();

        var assignableDays = _.filter(selection.days, function(day){
            return day.content != null && day.content.assignableDay != null
        }).map(function(day){
            return day.content.assignableDay;
        });
        updateAssignmentTypeBuckets(assignableDays)
    }

    $scope.$watch('selectedGroup', function() {
        if ($scope.selectedGroup == null) return;
        getAssignmentTypeData($scope.selectedGroup);
    });

    var GroupViewDayContent = function(assignableDay) {
        this.assignableDay = assignableDay;
        this.assignments = _.groupBy(assignableDay.assignments, function(assignment) {
            return assignment.assignmentTypeId;
        });
        this.assignments = _.object(_.map(this.assignments, function(assignments, id) {
            var employees = _.uniq(_.map(assignments, function(assignment) {
                return $scope.selectedGroup.employeeFor(assignment.employeeId);
            }));
            return [id, employees]
        }));
    }

    GroupViewDayContent.prototype.assignableDay = null;

    GroupViewDayContent.prototype.assignments = {};

    GroupViewDayContent.prototype.numberOfRoles = function() {
        var self = this;
        return Object.keys(this.assignments).filter(function(x){return self.assignments[x].length>0}).length;
    }

    GroupViewDayContent.prototype.isMidAssigned = function() {
        var roles = this.numberOfRoles();
        return 0 < roles && roles < $scope.selectedGroup.assignmentTypes.length;
    }

    GroupViewDayContent.prototype.isAssigned = function() {
        var roles = this.numberOfRoles();
        return roles != 0 && roles == $scope.selectedGroup.assignmentTypes.length;
    }

    GroupViewDayContent.prototype.assign = function() {
    }

    function indexDaysByISOString(days) {
        return _.indexBy(days, function(day) {
            return day.date.toISOLocalDateString();
        })
    }

    var progressBar = null;

    $scope.onLoadDayContent = function(terminate, days) {
        var startDate = days[0].date;
        var endDate = days[days.length - 1].date;
        var daysMap = indexDaysByISOString(days);

        if ($scope.selectedGroup == null || $scope.selectedGroup.id == null) {
            return terminate(true);
        }
        progressBar.trigger();
        var log = 'assignments? end = ' + endDate.toISOLocalDateString() + ', start = ' + startDate
        .toISOLocalDateString()
        var assignableDays = AssignableDay.query({
                group_id: $scope.selectedGroup.id,
                start_date: startDate.toISOLocalDateString(),
                end_date: endDate.toISOLocalDateString()
            }).$promise.then(function(assignableDays) {
                updateDayAssignments(assignableDays, daysMap);
                terminate();
            }).catch(function(e) {
                terminate(true);
        });
    }

    function updateDayAssignments(assignableDays, daysMap) {
        if (daysMap == null) {
            var dates = _.map(assignableDays, function(assignableDay) {
                return assignableDay.getDate();
            });
            daysMap = indexDaysByISOString($scope.calendar.getDays(dates));
        }

        _.each(assignableDays, function(assignableDay) {
            daysMap[assignableDay.date].content = new GroupViewDayContent(assignableDay);
        });
    }

    $scope.addAssignment = function(employee, assignmentType) {
        var group = $scope.selectedGroup;
        var days = $scope.selectedDays;
        var daysString = days.map(function(d) {return d.toISOLocalDateString();}).join();
        AssignableDay.save({
            groupId: group.id,
            employee_id:employee.id,
            assignment_type_id:assignmentType.id,
            dates: daysString
        }, function(assignableDays) {
            updateDayAssignments(assignableDays);
            updateAssignmentTypeBuckets(assignableDays);
        });
    }

    $scope.deleteAssignments = function(bucketEmployee, bucketEmployeeList) {
        var count = 0;
        var total = bucketEmployee.assignments.length;
        angular.forEach(bucketEmployee.assignments, function(assignmentId) {
            AssignableDay.delete({
                assignment_id: assignmentId,
                group_id: $scope.selectedGroup.id
            }, function(assignableDay) {
                updateDayAssignments([assignableDay]);
                count++;
                if (count == total) {
                    delete bucketEmployeeList[bucketEmployee.employee.id];
                }
            });
        });
    }

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

    // TODO: Ugly hack!
    $timeout(function() {
        $rootScope.$broadcast('trigger-resize');
    }, 300);
});
