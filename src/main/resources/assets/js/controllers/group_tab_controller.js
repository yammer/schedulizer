App.controller('GroupTabController', function($scope, $timeout, $location, $state, $rootScope,
                                              Group, GroupEmployee, AssignmentType, EMPTY_GROUP, NAV_TABS) {

    $scope.selectedGroup = EMPTY_GROUP;

    function selectGroup() {
        $scope.selectedGroup = _.find($scope.groups, function(g) {
            return g.id == parseInt($state.params.groupId);
        }) || EMPTY_GROUP;
    }

    $rootScope.$on('$stateChangeSuccess', selectGroup);

    $scope.groups = Group.query({}, function() {
        selectGroup();
    });

    $scope.createNewGroup = function() {
        var groupName = $scope.newGroupName;
        if(groupName == undefined || groupName == "") { return; }

        var group = new Group();
        group.name = groupName;
        group.$save(function() {
            if ($scope.groups.length == 0) {
                $timeout( function(){
                    $scope.groups = Group.query({});
                },0);
            }
            else {
                $scope.groups.push(group);
            }
            $scope.newGroupName = "";
        });
    }

    $scope.deleteGroup = function(group, $event) {
        group.$delete({}, function() {
            $scope.groups.remove(group);
            if ($scope.isSelectedGroup(group)) {
                if ($scope.groups.length == 0) {
                    $state.go('.', { groupId: 'default' });
                }
                else {
                    $state.go('.', { groupId: $scope.groups[0].id });
                }
            }
        });
        $event.preventDefault();
        $event.stopPropagation();
    }

    $scope.isSelectedGroup = function(group) {
        return group && group.id == $scope.selectedGroup.id;
    }
});
