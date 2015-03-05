App.controller("GroupSettingsModalController", function($scope, $modalInstance, $dialogs, Utils, AuthService, Session, data, CustomStat, Group, AssignmentType) {
    $scope.newGlobalAdmin = undefined;
    $scope.globalAdminInput = {};
    window.globalAdminScope = $scope;
    $scope.group = data.group;
    $scope.groupName = { input:  {}};

    $scope.customStatHelp = function() {
        var help = "Here you can write any mathematical expression that will represent your custom statistic.<br>";
        help += "You can reference the Assignment Types by $N where N is its index. <br> That is: <br>";
        $scope.group.assignmentTypes = _.sortBy($scope.group.assignmentTypes, "id");
        for (var i = 0; i < $scope.group.assignmentTypes.length; i++) {
            help += "$" + i + " -> " + $scope.group.assignmentTypes[i].name + "<br>";
        }
        return help;
    }

    $scope.customStat =  {
        value: CustomStat.load($scope.group.id),
        input: {}
    }

    function saveCustomStat(callback) {
        if (CustomStat.validate($scope.customStat.value, $scope.group)) {
            CustomStat.save($scope.group.id, $scope.customStat.value);
            callback();
        }
        else {
            Utils.shakeOnError($scope.customStat.input);
        }
    }

    function updateGroupName(callback) {
        if ($scope.group.name == "") {
            Utils.shakeOnError($scope.groupName.input);
        }
        else {
            Group.save({ id: $scope.group.id, name: $scope.group.name }).$promise.then(callback);
        }

    }

    function updateAssignmentStatsName(callback) {
        var count = 0;
        function wrappedCallback() {
            count++;
            if (count == $scope.group.assignmentTypes.length) callback();
        }
        _.each($scope.group.assignmentTypes, function(a) {
            a.$save({group_id: $scope.group.id}).then(wrappedCallback);
        });
    }

    $scope.ok = function(){
        saveCustomStat(function() {
            updateGroupName(function() {
                updateAssignmentStatsName(function() {
                    $modalInstance.close($scope.group);
                });
            });
        });
    };

    $scope.cancel = function() {
        $modalInstance.close();
    }
});