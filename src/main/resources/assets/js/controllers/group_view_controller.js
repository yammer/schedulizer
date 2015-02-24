App.controller('GroupViewController', function($scope, $timeout, $rootScope, $dialogs, Utils, Session, GroupRestrictionsResource,
                                               Group, AssignmentType, AssignableDay, EMPTY_GROUP, AVAILABILITY_STATES) {

    var NEW_EMPLOYEE = {name: undefined, image: undefined}
    $scope.availabilityStates = AVAILABILITY_STATES;

    // Will hold the calendar api
    $scope.calendar = {};

    // When availability calendar mode is false, the selected employee should be undefinde
    $scope.availabilityCalendarMode = false;
    $scope.selectedEmployee = undefined;
    $scope.availabilityModeTooltip = "Click here to quit employee's availability mode";

    function reloadView() {
        if ($scope.calendar && $scope.calendar.invalidateContent) {
            $scope.calendar.invalidateContent();
        }
        $scope.clearEmployeeSelection();
        $scope.clearSelection();
    }

    var debouncedReloadView = Utils.lastOfBurst(reloadView, 200);
    $scope.$watch(function() {return Session;}, debouncedReloadView, true);
    $scope.$watch(function() {
        return $scope.isGroupAdmin($scope.selectedGroup);
    }, debouncedReloadView, true);

    $scope.getCellClass = function(day) {
        if (day.content == undefined) {
            return {
                'available': $scope.availabilityCalendarMode
            };
        }

        var cellClass = {
            'assigned': !$scope.availabilityCalendarMode && day.content.isAssigned(),
            'mid-assigned': !$scope.availabilityCalendarMode && day.content.isMidAssigned(),
            'available': $scope.availabilityCalendarMode && $scope.selectedEmployee && day.content.isAvailable($scope.selectedEmployee.id),
            'mid-available': $scope.availabilityCalendarMode && $scope.selectedEmployee && day.content.isMidAvailable($scope.selectedEmployee.id),
            'not-available': $scope.availabilityCalendarMode && $scope.selectedEmployee && day.content.isNotAvailable($scope.selectedEmployee.id)
        }

        if ($scope.availabilityCalendarMode && day.content.assignments && $scope.selectedEmployee) {
            var assignmentCount = getSelectedEmployeeAssignmentsCount(day);
            if(assignmentCount > 0) {
                cellClass["assignment-count-" + assignmentCount] = true;
            }
        }
        return cellClass;
    }

    function getSelectedEmployeeAssignmentsCount(day) {
        var assignments = _.filter(_.flatten(_.values(day.content.assignments)), function(a) { return a.id == $scope.selectedEmployee.id; });
        return assignments == undefined ? 0 : assignments.length;
    }

    $scope.clearEmployeeSelection = function() {
        $scope.availabilityCalendarMode = false;
        $scope.selectedEmployee = undefined;
    }

    $scope.$watch('selectedGroup.employees.length', function() {
        if ($scope.selectedEmployee == undefined) { return; }
        if (_.find($scope.selectedGroup.employees, function(e) { return e.id == $scope.selectedEmployee.id; }) == undefined) {
            $scope.clearEmployeeSelection();
        }
    });

    var savedSelection = null;

    $scope.selectEmployee = function(employee) {
        if (!$scope.isGroupAdmin($scope.selectedGroup)) { return; }
        if ($scope.availabilityCalendarMode && $scope.selectedEmployee && $scope.selectedEmployee.id == employee.id) {
            $scope.clearEmployeeSelection();
            return;
        }

        $scope.selectedEmployee = employee;
        $scope.availabilityCalendarMode = true;
    }

    $scope.$watch('availabilityCalendarMode', function(availabilityCalendarMode) {
        if (availabilityCalendarMode == false && savedSelection) {
            $scope.clearSelection();
            $scope.calendar.selectDates(savedSelection);
            savedSelection = null;
        }
        else if (availabilityCalendarMode) {
            if (savedSelection == null) {
                savedSelection = $scope.selectedDates.clone();
            }
            $scope.clearSelection();
        }
    })


    $scope.$watchCollection('selectedDates', function(value) {
        if (value && value.length > 0) {
            $scope.clearEmployeeSelection();
        }
    });

    $scope.getDayTooltip = function(day) {
        if (!$scope.availabilityCalendarMode || day.content == undefined) {
            return undefined;
        }
        var restriction = _.find(day.content.restrictions, function(r) {
            return r.employeeId == $scope.selectedEmployee.id;
        });
        return (restriction != undefined && restriction.comment != "") ? restriction.comment : undefined;
    }

    function tryInvalidateCalendarContent() {
        if ($scope.calendar.invalidateContent != null) {
            $scope.calendar.invalidateContent();
        }
    }

    $scope.$watchCollection('selectedGroup.assignmentTypes', function(assignmentTypes) {
        if (assignmentTypes == null || assignmentTypes.$resolved == false) return;
        tryInvalidateCalendarContent();
    });

    $scope.assignmentTypeBuckets = {};

    function getAssignmentTypeData() {
        var group = $scope.selectedGroup;
        if (group == EMPTY_GROUP) {
            group.assignmentTypes = [];
            return;
        }
        var oldAssignments = group.assignmentTypes;
        AssignmentType.query({group_id: group.id}, function(assignmentTypes) {
            group.setAssignmentTypes(assignmentTypes);
            // HACK: Angular does not trigger watch when we return an empty list of assignment types bc it
            // was empty before as well)
            if (oldAssignments.length == 0 && assignmentTypes.length == 0) {
                tryInvalidateCalendarContent();
            }
        });
    }

    $scope.$watch('selectedGroup', function() {
        if ($scope.selectedGroup == null) return;
        getAssignmentTypeData($scope.selectedGroup);
    });

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

    $scope.goToToday = function() {
        $scope.calendar.goToToday();
        $scope.dayStamp = Date.TODAY;
    }

    $scope.dayStamp = Date.TODAY;

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

    $scope.showHoveredDayEmployees = function(hoveredDay, employees) {
        return  (!$scope.availabilityCalendarMode && employees.length > 0) ||
                ($scope.availabilityCalendarMode && _.find(employees, function(e){return e.id==$scope.selectedEmployee.id; }));
    }

    $scope.hasAssignment = function(day) {
        return day && day.content && day.content.assignments && Object.keys(day.content.assignments).length > 0;
    }

    $scope.orderHoveredDayBy = function(key){
        return $scope.selectedGroup.assignmentTypeFor(key).id;
    }

    $scope.clearSelection = function() {
        $scope.calendar.clearSelectedDays();
        $scope.selectedDates = [];
        $scope.employeeRestrictions = {};
    }

    $scope.selectedDates = [];
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
        $scope.selectedDates = selection.dates();
        $scope.assignmentTypeBuckets = {};
        initAssignmentBuckets();

        var assignableDays = _.filter(selection.days, function(day){
            return day.content != null && day.content.assignableDay != null
        }).map(function(day){
            return day.content.assignableDay;
        });
        updateAssignmentTypeBuckets(assignableDays)

        var restrictions = _.flatten(_.filter(selection.days, function(day){
            return day.content != null && day.content.restrictions != null && day.content.restrictions.length > 0
        }).map(function(day){
            return day.content.restrictions;
        }));
        updateEmployeeRestrictions(restrictions);
    }

    function updateEmployeeRestrictions(restrictions) {
        $scope.employeeRestrictions = {};
        angular.forEach(restrictions, function(restriction) {
            var currRestriction = $scope.employeeRestrictions[restriction.employeeId];
            if (currRestriction == undefined ||
                restriction.restrictionLevel > currRestriction) {
                $scope.employeeRestrictions[restriction.employeeId] = restriction.restrictionLevel;
            }
        });
    }

    var GroupViewDayContent = function(assignableDay) {
        this.assignableDay = undefined;
        this.restrictions = [];
        this.assignments = {};
    }

    GroupViewDayContent.prototype.assignableDay = null;

    GroupViewDayContent.prototype.assignments = {};

    GroupViewDayContent.prototype.restrictions = null;

    GroupViewDayContent.prototype.setAssignments = function(assignableDay) {
        this.assignments = _.groupBy(assignableDay.assignments, function(assignment) {
            return assignment.assignmentTypeId;
        });
        this.assignments = _.object(_.map(this.assignments, function(assignments, id) {
            var employees = _.uniq(_.map(assignments, function(assignment) {
                return $scope.selectedGroup.employeeFor(assignment.employeeId);
            }));
            return [id, employees]
        }));
        this.assignableDay = assignableDay;
    }

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

    GroupViewDayContent.prototype.restrictionLevel = function(employee_id) {
        var restriction = _.find(this.restrictions, function(r) {
            return r.employeeId == employee_id;
        });
        return (restriction != null) ? restriction.restrictionLevel : 0;
    };

    GroupViewDayContent.prototype.isAvailable = function(employee_id) {return this.restrictionLevel(employee_id) == 0;};
    GroupViewDayContent.prototype.isMidAvailable = function(employee_id) {return this.restrictionLevel(employee_id) == 1;};
    GroupViewDayContent.prototype.isNotAvailable = function(employee_id) {return this.restrictionLevel(employee_id) == 2;};

    function indexDaysByISOString(days) {
        return _.indexBy(days, function(day) {
            return day.date.toISOLocalDateString();
        })
    }

    $scope.progressBar = {trigger: function() {console.log('empty trigger()');}};

    $scope.onLoadDayContent = function(terminate, days) {
        var startDate = days[0].date;
        var endDate = days[days.length - 1].date;
        var daysMap = indexDaysByISOString(days);

        if ($scope.selectedGroup == null || $scope.selectedGroup.id == null) {
            return terminate(true);
        }
        $scope.progressBar.trigger();

        var i = $scope.isGroupAdmin($scope.selectedGroup) ? 2 : 1;
        var totalError = false;
        var wrappedTerminate = function(error) {
            totalError = totalError || error;
            if (--i == 0) terminate(totalError);
        };

        AssignableDay.query({
                group_id: $scope.selectedGroup.id,
                start_date: startDate.toISOLocalDateString(),
                end_date: endDate.toISOLocalDateString()
            }).$promise.then(function(assignableDays) {
                updateDayAssignments(assignableDays, daysMap);
                wrappedTerminate();
            }).catch(function(e) {
                wrappedTerminate(true);
        });

        if (!$scope.isGroupAdmin($scope.selectedGroup)) { return; }

        GroupRestrictionsResource.query({
                group_id: $scope.selectedGroup.id,
                start_date: startDate.toISOLocalDateString(),
                end_date: endDate.toISOLocalDateString()
            }).$promise.then(function(restrictions) {
                updateDayRestrictions(restrictions, daysMap);
                wrappedTerminate();
            }).catch(function(e) {
                wrappedTerminate(true);
        });
    }

    function updateDayAssignments(assignableDays, daysMap) {
        if (daysMap == null) {
            var dates = _.map(assignableDays, function(a) {return a.getDate();});
            daysMap = indexDaysByISOString($scope.calendar.getDays(dates));
        }

        _.each(assignableDays, function(assignableDay) {
            if (daysMap[assignableDay.date].content == undefined) {
                daysMap[assignableDay.date].content = new GroupViewDayContent();
            }
            daysMap[assignableDay.date].content.setAssignments(assignableDay);
        });
    }

    function updateDayRestrictions(restrictions, daysMap) {
        if (daysMap == null) {
            var dates = _.map(restrictions, function(a) {return a.getDate();});
            daysMap = indexDaysByISOString($scope.calendar.getDays(dates));
        }

        _.each(restrictions, function(restriction) {
            if (daysMap[restriction.date].content == undefined) {
                daysMap[restriction.date].content = new GroupViewDayContent();
            }
            daysMap[restriction.date].content.restrictions.push(restriction);
            daysMap[restriction.date].content.restrictions = _.unique(daysMap[restriction.date].content.restrictions);
        });
    }

    $scope.addAssignment = function(employee, assignmentType) {
        var group = $scope.selectedGroup;
        var days = $scope.selectedDates;
        var daysString = days.map(function(d) {return d.toISOLocalDateString();}).join(',');
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

    $scope.deleteAssignmentsIfHasPrivileges = function(bucketEmployee, bucketEmployeeList) {
        if ($scope.isGroupAdmin($scope.selectedGroup)) {
            $scope.deleteAssignments(bucketEmployee, bucketEmployeeList);
        }
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

    // TODO: Ugly hack!
    $timeout(function() {
        $rootScope.$broadcast('trigger-resize');
    }, 300);
});
