App.controller('GroupTabController', function($scope, $timeout, $location, $state, $rootScope,
                                              Group, GroupEmployee, AssignmentType, EMPTY_GROUP, NAV_TABS) {

    $scope.selectedGroup = EMPTY_GROUP;

    function selectGroup() {
        $scope.selectedGroup = _.find($scope.groups, function(g) {
            return g.id == parseInt($state.params.groupId);
        });
        if ($scope.selectedGroup == null) {
            // Changing the location path instead of $state.go because
            // I want to go to a parent state, so it wouldn't change the url
            $location.path('groups')
        }
    }

    $rootScope.$on('$stateChangeSuccess', selectGroup);

    $scope.groups = Group.query({}, selectGroup);

    $scope.createNewGroup = function() {
        var groupName = $scope.newGroupName;
        if(groupName == undefined || groupName == "") { return; }

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
        group.$delete({}, function() {
            $scope.groups.remove(group);
            if ($scope.isSelectedGroup(group)) {
                if ($scope.groups.length == 0) {
                    $state.go('groups');
                } else {
                    $state.go('.', {groupId: $scope.groups[0].id});
                }
            }
        });
        $event.preventDefault();
        $event.stopPropagation();
    }

    $scope.isSelectedGroup = function(group) {
        return group && $scope.selectedGroup && group.id == $scope.selectedGroup.id;
    }
});
