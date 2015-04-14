'use strict';

App.controller("GlobalAdminModalController", function($scope, $modalInstance, $dialogs, GlobalAdminsResource, AuthService, Session) {
    $scope.newGlobalAdmin = undefined;
    $scope.globalAdminInput = {};
    window.globalAdminScope = $scope;
    var changed = false;

    $scope.ok = function(){
      $modalInstance.close(changed);
    };

    $scope.onSelectAutocomplete = function(user){
        $scope.newGlobalAdmin = user;
    };

    $scope.onKeyDown = function(){
        $scope.newGlobalAdmin = undefined;
    };

    function getGlobalAdminsData() {
        $scope.globalAdmins = GlobalAdminsResource.query({});
    }

    function addGlobalAdmin(yEmployee) {
        var confirm = $dialogs.confirm('Please confirm',
                                       'Are you sure you want to make this user a GLOBAL admin? <br>' +
                                       'He will have ALL possible privileges in this website!');
        confirm.result.then(function(btn){
            var employee = new GlobalAdminsResource();
            employee.extAppId = yEmployee.id;
            employee.name = yEmployee.full_name;
            employee.imageUrlTemplate = yEmployee.photo;
            employee.$save({}, function(employee) {
                if (_.find($scope.groupAdmins, function(e) { return e.id == employee.id; }) === undefined) {
                    $scope.globalAdmins.push(employee);
                    changed = true;
                }
                $scope.globalAdminInput.setValue("");
            });
        });
    }

    $scope.deleteGlobalAdmin = function(employee) {
        var confirm;
        if (employee.id == Session.userId) {
            confirm = $dialogs.confirm('Please confirm',
                                       'Are you sure you want to revoke your own global admin privileges? <br>' +
                                       'YOU WILL LOSE A LOT OF POWER!');
        }
        else {
            confirm = $dialogs.confirm('Please confirm',
                                       'Are you sure you want to revoke global admin privileges from this user? <br>');
        }
        confirm.result.then(function(btn){
            if ($scope.globalAdmins.length == 1) {
                $dialogs.error("You cannot delete the last global admin");
            } else {
                employee.$delete({}, function() {
                    changed = true;
                    if (employee.id == Session.userId) {
                        AuthService.removeGlobalAdminPrivileges();
                        $modalInstance.close(changed);
                    }
                    $scope.globalAdmins = _.without($scope.globalAdmins, employee);
                }, function(m){
                    $dialogs.error("Something went wrong");
                });
            }
        });
    };

    $scope.triggerAddGlobalAdmin = function() {
        if ($scope.newGlobalAdmin) {
            addGlobalAdmin($scope.newGlobalAdmin);
            $scope.newGlobalAdmin = undefined;
        } else {
            $scope.globalAdminInput.shake();
        }
    };

    getGlobalAdminsData();

});