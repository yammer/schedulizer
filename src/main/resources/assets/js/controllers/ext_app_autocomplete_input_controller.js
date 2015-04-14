'use strict';

App.controller("ExtAppAutocompleteInputController", function($scope, $timeout, extAppApi, Utils) {
    $scope.autocompleteList = [];
    window.autocompleteScope = $scope;
    if ($scope.autocompleteType === undefined) {
        $scope.autocompleteType = "user";
    }

    if ($scope.onEnter === undefined) {
        $scope.onEnter = function() {};
    }

    if ($scope.onKeyDown === undefined) {
        $scope.onKeyDown = function() {};
    }

    if ($scope.onKeyUp === undefined) {
        $scope.onKeyUp = function() {};
    }

    if ($scope.onBlur === undefined) {
        $scope.onKoeyUp = function() {};
    }

    if ($scope.placeholder === undefined) {
        $scope.placeholder = "Name";
    }

    var isInitialValue = false;
    if ($scope.initialValue) {
        $scope.inputValue = $scope.initialValue;
        isInitialValue = true;
    }
    var timeout;
    var AUTOCOMPLETE_QUERY_WAIT_TIME = 300; // in order not to send too many queries
    $scope.$watch('inputValue', function(prefix) {
        if (isInitialValue) {
            isInitialValue = false;
            return;
        }
        if (prefix === undefined || prefix === "" || $scope.newEmployee != null) {
            return;
        }
        if (timeout !== undefined) {
            $timeout.cancel(timeout);
        }
        timeout = $timeout(function() {
            extAppApi.autocomplete(prefix, function(response) {
                if (response === undefined) {
                    return;
                }
                var users = response.items;
                $timeout(function(){
                    $scope.autocompleteList =
                        (users.map(function(item) {
                            if (item.full_name == null) { // test user
                                item.full_name = "";
                            }
                            if ($scope.autocompleteType == "user") {
                                var names = item.full_name.split(" ");
                                item.label = names[0] + " " + names[names.length - 1];
                            } else {
                                item.label = item.full_name;
                            }
                            return {
                                label: item.label,
                                value: item
                            };
                        }));
                    $scope.autocompleteList = _.unique($scope.autocompleteList, function(e) { return e.label; } );
                });
            }, $scope.autocompleteType);

        }, AUTOCOMPLETE_QUERY_WAIT_TIME);
    });

    $scope.getAutocompleteItem = function(item) {
        return "" +
            "<div class=\"employee-image\"><img src=\"" + item.photo + "\"/></div>" +
            "<div class=\"employee-name\">" + item.label + "</div>";
    };

    if ($scope.api === undefined) { $scope.api = {}; }

    $scope.api.setValue = function (value) {
        $scope.inputValue = value;
    };
    $scope.api.shake = function() {
        Utils.shakeOnError($scope.autocompleteInput);
    };
});

App.directive('extAppAutocompleteInput', function() {
    return {
        restrict: 'E',
        scope: {
            onSelectAutocomplete: "=",
            onKeyDown: "=?",
            onKeyUp: "=?",
            onBlur: "=?",
            onEnter: "=?",
            api: "=exposeApiTo",
            displayAbove: "@",
            autocompleteType: "@?",
            placeholder: "@?",
            initialValue: "@?"
        },
        templateUrl: 'views/ext_app_autocomplete_input.html',
        controller: 'ExtAppAutocompleteInputController'
    };
});