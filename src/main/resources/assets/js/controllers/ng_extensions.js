
App.directive('ngEnter', function() {
    return function(scope, element, attrs) {
        element.bind("keydown keypress", function(event) {
            if (event.which === 13) {
                scope.$apply(function(){
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

App.directive('stAutocomplete', function($timeout, $compile) {
    return {
        restrict: 'A',
        scope: {
            tags: "=",
            getCustomTemplate: "=",
            autocompleteWidth: "=",
            autocompleteSelect: "="
        },
        link: function(scope, element, attr) {
            var availableTags;

            $(function() {
                availableTags = [];
                var autocomplete = $(element).autocomplete({
                    source: availableTags,
                    select: function(event, ui) {
                        scope.autocompleteSelect(ui.item.value);
                        return false;
                    },
                    focus: function( event, ui ) {
                        $(element).val(ui.item.label);
                        return false;
                    }
                });
                if (scope.getCustomTemplate != null) {
                    autocomplete.data("ui-autocomplete")._renderItem = function (ul, item) {
                        return $('<li></li>')
                            .append(scope.getCustomTemplate(item.value))
                            .appendTo(ul);
                    };
                }
                if (scope.autocompleteWidth != null) {
                    autocomplete.data("ui-autocomplete")._resizeMenu = function () {
                        var ul = this.menu.element;
                        ul.outerWidth(scope.autocompleteWidth);

                    }
                }
            });

            scope.$watch("tags", function(value) {
                if (value == null) return;
                availableTags = value;
                $(element).autocomplete("option", "source", availableTags);
                $(element).autocomplete("search");
            });
        }
    }
});

App.directive('stTryBackgroundImage', function($timeout) {
    return {
        restrict: 'A',
        link: function(scope, element, attr) {
            scope.$watch(attr.stTryBackgroundImage, function(value) {
                if(value== undefined) {
                    $(element)[0].style.backgroundImage = "";
                    return;
                }
                $(element)[0].style.backgroundImage = "url(" + value + ")";
            });
        }
    }
})

App.directive('stName', function() {
    return {
        restrict: 'A',
        // acts as below without requiring isolated scope
        //scope: {
        //    stName: '='
        //},
        link: function(scope, element, attrs) {
            scope[attrs.stName] = element;
        }
    }
})
