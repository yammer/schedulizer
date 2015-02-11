var groupTabStateChangeSuccessOff = undefined;

App.controller('GroupTabController', function(
        $scope, $timeout, $location, $state, $rootScope, $dialogs, Utils,
        Group, GroupEmployee, AssignmentType, EMPTY_GROUP, NAV_TABS, NESTED_VIEWS) {

    $scope.selectedGroup = EMPTY_GROUP;
    $scope.newGroupName = "";

    function selectGroup() {
        $scope.selectedGroup = _.find($scope.groups, function(g) {
            return g.id == parseInt($state.params.groupId);
        });
        if ($scope.selectedGroup == null) {
            if ($scope.groups.length == 0) {
                // Changing the location path instead of $state.go because
                // I want to go to a parent state, so it wouldn't change the url
                $location.path('groups')
            }
            else {
                $state.go('groups.view', {groupId: $scope.groups[0].id});
            }
        }
    }

    if (groupTabStateChangeSuccessOff) {
        groupTabStateChangeSuccessOff(); // turning event off if it already exists
        // we need to stick to the current scope
        // so we need to delete the event handler created by the other instance of the controller and register it again
    }
    groupTabStateChangeSuccessOff = $rootScope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams) {
        if(toState.stateName == NAV_TABS.group.stateName || toState.stateName == NESTED_VIEWS.groupsView.stateName) {
            selectGroup();
        }
    });

    $scope.groups = Group.query({}, selectGroup);

    $scope.groupInput = null;

    $scope.createNewGroup = function() {
        var groupName = $scope.newGroupName;
        if (groupName == null || groupName == "") {
            Utils.shakeOnError($scope.groupInput);
            return;
        }

        var group = new Group();
        group.name = groupName;
        group.$save(function() {
            $scope.groups.$promise.then(function() {
                $scope.groups.push(group);
                $state.go('groups.view', {groupId: group.id});
            })
            $scope.newGroupName = "";
        });
    }

    $scope.deleteGroup = function(group, $event) {
        var confirm = $dialogs.confirm('Please confirm',
                                       'Are you sure you want to delete this group?' +
                                       '<br> Bad things may happen, ' +
                                       'because this operation can not be undone!');

        confirm.result.then(function(btn) {
            var doubleConfirm = $dialogs.confirm('Please confirm again',
                                                 'Are you really sure?');
            doubleConfirm.result.then(function(btn2) {
                group.$delete({}, function() {
                    $scope.groups.remove(group);
                    if ($scope.isSelectedGroup(group)) {
                        if ($scope.groups.length == 0) {
                            $location.path('groups');
                        } else {
                            $state.go('.', {groupId: $scope.groups[0].id});
                        }
                    }
                });
            });
        });
        $event.preventDefault();
        $event.stopPropagation();
    }

    $scope.isSelectedGroup = function(group) {
        return group && $scope.selectedGroup && group.id == $scope.selectedGroup.id;
    }
});
