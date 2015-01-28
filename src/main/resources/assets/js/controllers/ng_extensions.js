
App.directive('ngEnter', function () {
    return function (scope, element, attrs) {
        element.bind("keydown keypress", function (event) {
            if(event.which === 13) {
                scope.$apply(function (){
                    scope.$eval(attrs.ngEnter);
                });
                event.preventDefault();
            }
        });
    };
});

App.directive('ngFocus', function($timeout) {
    return {
        replace: true,
        restrict: 'A',
        link: function(scope, element, attr) {
            scope.$watch(attr.ngFocus, function(value) {
                if(value) {
                    $timeout(function() {
                        element[0].focus();
                    });
                }
            });
        }
    };
});