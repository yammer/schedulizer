App.controller("YammerAutocompleteInputController", function($scope, $timeout, yammer, Utils) {
    $scope.autocompleteList = [];
    window.autocompleteScope = $scope;

    var timeout;
    var AUTOCOMPLETE_QUERY_WAIT_TIME = 300; // as suggested by yammers api
    $scope.$watch('inputValue', function(prefix) {
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
                            if (user.full_name == undefined) { // test user
                                user.full_name = "";
                            }
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

    if ($scope.api == undefined) $scope.api = {};
    $scope.api.setValue = function (value) {
        $scope.inputValue = value;
    }
    $scope.api.shake = function() {
        Utils.shakeOnError($scope.autocompleteInput);
    }
});

App.directive('yammerAutocompleteInput', function() {
    return {
        restrict: 'E',
        scope: {
            onSelectAutocomplete: "=",
            onKeyDown: "=",
            onEnter: "=",
            api: "=exposeApiTo",
            displayAbove: "@"
        },
        templateUrl: 'views/yammer_autocomplete_input.html',
        controller: 'YammerAutocompleteInputController'
    };
});