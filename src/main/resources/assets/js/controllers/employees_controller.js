App.controller('EmployeesController', function($scope, $timeout, $dialogs, yammer, Session,
                                               Utils, GroupEmployee, AssignmentStats, AdminsResource, EMPTY_GROUP) {

        function getGroupEmployeesData(group) {
            if (group == EMPTY_GROUP) {
                group.employees = [];
                return;
            }
            group.employees = GroupEmployee.query({group_id: group.id}, function(response) {
                group.employeeMap = _.indexBy(group.employees, 'id');
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
            var confirm = $dialogs.confirm('Please confirm',
                                           'Are you sure you want to remove this employee from this group? <br>');
            confirm.result.then(function(btn) {
                var group = $scope.selectedGroup;
                employee.groupId = group.id;
                employee.$delete().then(function() {
                    group.employees = _.without(group.employees, _.findWhere(group.employees, employee));
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
                "<div class=\"employee-image\"><img src=\"" + user.photo + "\"/></div>" +
                "<div class=\"employee-name\">" + user.label + "</div>";
        }

        $scope.stat = {
            selected: {counter: 3, unit: 'months'},
            units: {
                days:   {label: 'days',   multiplier: 1  },
                weeks:  {label: 'weeks',  multiplier: 7  },
                months: {label: 'months', multiplier: 30 },
                years:  {label: 'years',  multiplier: 365}
            }
        }

        $scope.getDaysOffset = function() {
            var counter = $scope.stat.selected.counter;
            var unit = $scope.stat.units[$scope.stat.selected.unit];
            return counter * unit.multiplier;
        }

        $scope.incrementStatCounter = function(delta) {
            $scope.stat.selected.counter = Math.max(0, $scope.stat.selected.counter + delta);
        }

        $scope.getAssignmentStats = function() {
            var group = $scope.selectedGroup;
            if (group == EMPTY_GROUP) {
                return;
            }

            var endDate = Date.TODAY;
            var startDate = endDate.plusDays(- $scope.getDaysOffset());

            AssignmentStats.query({
                group_id: group.id,
                start_date: startDate.toISOLocalDateString(),
                end_date: endDate.toISOLocalDateString()
            }, function(assignmentStats) {
                _.each(group.employees, function(e) {
                    e.statistics = {};
                    if (!assignmentStats[e.id]) return;
                    e.statistics = _.chain(assignmentStats[e.id])
                        .each(function(s) {s.assignmentType = group.assignmentTypeFor(s.assignmentTypeId);})
                        .indexBy('assignmentTypeId')
                        .value();
                });
                // complete with "count: 0" for other assignment types
                _.each(group.employees, function(e) {
                    _.each(group.assignmentTypes, function(a) {
                        if (e.statisticsFor(a.id)) return;
                        e.statistics[a.id] = {
                            assignmentTypeId: a.id,
                            assignmentType: a,
                            count: 0
                        };
                    });
                });
            });
        }

        $scope.$watch('assignmentsChange', $scope.getAssignmentStats);
        $scope.$watch('getDaysOffset()', $scope.getAssignmentStats);

        $scope.getAssignmentTypeHeaderStyle = function(assignmentType) {
            var ids = _.map($scope.employeeOrder, Math.abs);
            var i = _.indexOf(ids, assignmentType.id);
            var n = _.size(ids);
            var current = n - i - 1;
            var srcScale = [0, n - 1];

            return {
                position: 'relative',
                opacity: Utils.interpolate(current, srcScale, [0.25, 0.9]),
                paddingTop: Utils.interpolate(current, srcScale, [2, 8]) + 'px',
                bottom: Utils.interpolate(current, srcScale, [-3, 3]) + 'px'
            };
        }

        $scope.employeeOrder = []
        $scope.employeeOrderKey = []

        function computeEmployeeOrderKey(employeeOrder) {
            return _.map(employeeOrder, function(id) {
                var prefix = (id < 0) ? '-' : '+';
                return prefix + 'statistics[' + Math.abs(id) + '].count';
            })
        }

        function getOrderedId(assignmentType) {
            return (assignmentType.desc) ? -assignmentType.id : +assignmentType.id;
        }

        function tryUpdateOrder(newOrder) {
            if (_.isEqual(newOrder, $scope.employeeOrder)) return;
            $scope.employeeOrder = newOrder;
            $scope.employeeOrderKey = computeEmployeeOrderKey(newOrder);
        }

        function tryComputeEmployeeOrder() {
            var assignmentTypes = $scope.selectedGroup.assignmentTypes;
            if (assignmentTypes == null || assignmentTypes.length <= 0) return;
            var map = _.indexBy(assignmentTypes, 'id');
            var newIds = _.map(assignmentTypes, 'id');
            var currentIds = _.map($scope.employeeOrder, Math.abs);
            var newOrder = _.chain(currentIds)       // [unorderedId]
                .union(newIds)                       // [unorderedId]
                .intersection(newIds)                // intersect() after union() to keep original order
                .map(function(id) {return map[id];}) // [assignmentType]
                .map(getOrderedId)                   // [orderedId]
                .value();

            tryUpdateOrder(newOrder);
        }

        $scope.$watch('assignmentsChange', tryComputeEmployeeOrder);

        $scope.updateEmployeesOrder = function(assignmentType) {
            var oldId = getOrderedId(assignmentType);
            assignmentType.desc = !assignmentType.desc;
            var newId = getOrderedId(assignmentType);

            var newOrder = _.chain($scope.employeeOrder)
                .without(oldId)
                .unshift(newId)
                .value();

            tryUpdateOrder(newOrder);
        }

        $scope.employeeInput = null; // <input/>

        $scope.triggerAddEmployee = function() {
            if ($scope.newEmployee) {
                addEmployee($scope.newEmployee);
                $scope.newEmployee = undefined;
            } else {
                Utils.shakeOnError($scope.employeeInput);
            }
        }

        $scope.userInputKeyDown = function(e) {
            $scope.newEmployee = undefined;
        }

        $scope.userInputEnter = function(e) {
            $scope.triggerAddEmployee();
        }

        $scope.onSelectAutocomplete = function(user) {
            $scope.newEmployee = user;
            $scope.newEmployeeName = user.label;
        }

        $scope.toggleAdmin = function(employee) {
            if (employee.groupAdmin) {
                var confirm;
                if (employee.id == Session.userId) {
                    confirm = $dialogs.confirm('Please confirm',
                                               'Are you sure you want to revoke your own admin privileges? <br>' +
                                               'YOU WILL LOSE POWER!');
                }
                else {
                    confirm = $dialogs.confirm('Please confirm',
                                               'Are you sure you want to revoke admin privileges for this user? <br>');
                }
                confirm.result.then(function(btn){
                    $scope.deleteAdmin(employee);
                });
            } else {
                var confirm = $dialogs.confirm('Please confirm',
                                               'Are you sure you want to make this user an admin? <br>' +
                                               'He will be able to edit this group as much as he wants');
                confirm.result.then(function(btn){
                    $scope.addAdmin(employee);
                });
            }
        }

        $scope.addAdmin = function(employee) {
            AdminsResource.save({groupId: $scope.selectedGroup.id, employeeId: employee.id}, function() {
                employee.groupAdmin = true;
            });
        }

        $scope.deleteAdmin = function(employee) {
            AdminsResource.delete({group_id: $scope.selectedGroup.id, employee_id: employee.id}, function() {
                employee.groupAdmin = false;
            });
        }

        $scope.$watch('selectedGroup', function() {
            if ($scope.selectedGroup == null || $scope.selectedGroup == EMPTY_GROUP) return;
            getGroupEmployeesData($scope.selectedGroup);
            $scope.getAssignmentStats();
        });

        // Create new property to centralize assignments change events
        $scope.assignmentsChange = 0;

        function touchAssignments() {
            $scope.assignmentsChange++;
        }

        $scope.$watch('selectedGroup.employees.length', Utils.lastOfBurst(touchAssignments));
        $scope.$watch('selectedGroup.assignmentTypes.length', Utils.lastOfBurst(touchAssignments));
        $scope.$watch('assignmentTypeBuckets', Utils.lastOfBurst(touchAssignments), true);

});
