App.controller('GroupViewController', function($scope, $timeout, $location,  $stateParams,yammer,
                                              Group, GroupEmployee, AssignmentType, AssignableDay, EMPTY_GROUP) {

    var NEW_EMPLOYEE = {name: undefined, image: undefined}

    // Will hold the calendar api
    $scope.calendar = null;

    $scope.$watchCollection('selectedGroup.assignmentTypes', function(assignmentTypes) {
        if (assignmentTypes && assignmentTypes.$resolved && $scope.calendar != null) {
            $scope.calendar.invalidateAssignments();
        }
    });

    $scope.assignmentTypeBuckets = {};

    function getGroupEmployeesData(group) {
        if (group == EMPTY_GROUP || group == undefined) {
            group.employees = [];
            return;
        }
        group.employees = GroupEmployee.query({group_id: group.id}, function(employees) {
            group.employeeMap = _.indexBy(employees, 'id');
        });
    }

    function addEmployee(yEmployee) {
        var group = $scope.selectedGroup;
        var yid = yEmployee.id;
        if (yid == undefined || yid == "") {
            return false;
        }
        if (_.find(group.employees, function(e){ return e.yammerId == yid; })) {
            $scope.newEmployeeName = "";
            return false;
        }

        var employee = new GroupEmployee({groupId: group.id});
        employee.yammerId = yid;
        employee.name = yEmployee.full_name;
        employee.imageUrlTemplate = yEmployee.photo;
        employee.$save({}, function(response) {
            group.addEmployee(employee);
            $scope.newEmployeeName = "";
        });
        return true;

    }

    $scope.deleteEmployee = function (employee) {
        var group = $scope.selectedGroup;
        group.employees = _.without(group.employees, _.findWhere(group.employees, employee));
        employee.groupId = group.id;
        employee.$delete();
    }

    function getAssignmentTypeData(group) {
        if (group == EMPTY_GROUP) {
            group.assignmentTypes = [];
            return;
        }
        group.assignmentTypes = AssignmentType.query({ group_id: group.id });
    }

    $scope.addAssignmentType = function () {
        var name = $scope.newAssignmentTypeName;
        var group = $scope.selectedGroup;
        if (name == undefined || name == "") { return; }
        var assignmentType = new AssignmentType({ groupId: group.id });
        assignmentType.name = name;
        assignmentType.$save({}, function() {
            group.assignmentTypes.unshift(assignmentType);
            $scope.newAssignmentTypeName = "";
            initAssignmentTypeBucket(assignmentType);
        });

    }

    var hideInput = null;

    $scope.tryAddAssignmentType = function() {
        if ($scope.isCreatingAssignmentType) {
            $scope.addAssignmentType();
        }
    }

    $scope.onInputBlur = function() {
        hideInput = $timeout(function(){
            $scope.isCreatingAssignmentType = false;
        }, 200);
    }

    $scope.deleteAssignmentType = function(assignmentType) {
        var group = $scope.selectedGroup;
        assignmentType.groupId = group.id;
        assignmentType.$delete({}, function() {
            group.assignmentTypes.remove(assignmentType);
            delete $scope.assignmentTypeBuckets[assignmentType.id];
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
        getGroupEmployeesData($scope.selectedGroup);
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
        return _.size(this.assignments);
    }

    GroupViewDayContent.prototype.isMidAssigned = function() {
        var roles = this.numberOfRoles();
        return 0 < roles && roles < $scope.selectedGroup.assignmentTypes.length;
    }

    GroupViewDayContent.prototype.isAssigned = function() {
        return this.numberOfRoles() == $scope.selectedGroup.assignmentTypes.length;
    }

    GroupViewDayContent.prototype.assign = function() {
    }

    function indexDaysByISOString(days) {
        return _.indexBy(days, function(day) {
            return day.date.toISOLocalDateString();
        })
    }

    $scope.onLoadDayContent = function(days) {
        var startDate = days[0].date;
        var endDate = days[days.length - 1].date;
        var daysMap = indexDaysByISOString(days);

        var assignableDays = AssignableDay.query(
            {
                group_id: $scope.selectedGroup.id,
                start_date: startDate.toISOLocalDateString(),
                end_date: endDate.toISOLocalDateString()
            }, function(assignableDays) {
                updateDayAssignments(assignableDays, daysMap);
            }
        );

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

    $scope.autocompleteList = [];

    var timeout;
    var AUTOCOMPLETE_QUERY_WAIT_TIME = 300; // as suggested by yammers api
    $scope.$watch('newEmployeeName', function(prefix) {
        if (prefix == undefined || prefix == "" || $scope.newEmployee != undefined) {
            return;
        }
        if (timeout != undefined) {
            $timeout.cancel(timeout);
        }
        timeout = $timeout(function() {
            yammer.autocomplete(prefix, function(response) {
                if (response == undefined) {
                    return;
                }
                var users = response.user;
                $timeout(function(){
                    $scope.autocompleteList =
                        (users.map(function(user) {
                            var names = user.full_name.split(" ");
                            user.label = names[0] + " " + names[names.length - 1];
                            return {
                                label: user.label,
                                value: user
                            }
                        }));
                    $scope.autocompleteList = _.unique($scope.autocompleteList, function(e) { return e.label; } );
                });
            });

        }, AUTOCOMPLETE_QUERY_WAIT_TIME);
    });

    $scope.getAutocompleteItem = function(user) {
        return "" +
            "<div>" +
            "<img width=\"20\" height=\"20\" src=\"" + user.photo + "\"/>" + user.label +
            "</div>";
    }

    var userSelectionConfirmed = false; // so that enter in autocomplete selection does not trigger input submit

    $scope.userInputKeyDown = function(e) {
        switch (e.which) {
            case 13: // enter
                if (userSelectionConfirmed && $scope.newEmployee) {
                    addEmployee($scope.newEmployee);
                    $scope.newEmployee = undefined;
                    userSelectionConfirmed = false;
                }
                else {
                    // shake... I think you should put more conditions... try and you'll see
                }
                break;
            default:
                $scope.newEmployee = undefined;
                userSelectionConfirmed = false;
                break;
        }
    }

    $scope.onSelectAutocomplete = function(user) {
        $scope.newEmployeeName = user.label;
        $scope.newEmployee = user;
        $timeout(function() { userSelectionConfirmed = true; }, 100);
    }

    // TODO: Ugly hack!
    $timeout(resizeCalendar, 300)
});
