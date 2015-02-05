
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
            autocompleteSelect: "="
            //itemClass: "=attribute"

        },
        link: function(scope, element, attrs) {
            var availableTags;

            $(function() {
                availableTags = [];
                var autocomplete = $(element).autocomplete({
                    source: availableTags,
                    select: function(event, ui) {
                        scope.$apply(function() {
                            scope.autocompleteSelect(ui.item.value);
                        });
                        return false;
                    },
                    focus: function(event, ui) {
                        scope.$apply(function() {
                            scope.autocompleteSelect(ui.item.value);
                        });
                        $(element).val(ui.item.label);
                        return false;
                    }
                });
                if (scope.getCustomTemplate != null) {
                    autocomplete.data("ui-autocomplete")._renderItem = function (ul, item) {
                        var itemClass = (attrs.itemClass != null) ? ' class="' + attrs.itemClass + '"' : "";
                        return $('<li' + itemClass + '></li>')
                            .append(scope.getCustomTemplate(item.value))
                            .appendTo(ul);
                    };
                }
                autocomplete.data("ui-autocomplete")._resizeMenu = function () {
                    var ul = this.menu.element;
                    ul.outerWidth(element.outerWidth());
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
        link: function(scope, element, attrs) {
            console.log(attrs.stTryBackgroundImage)
            scope.$watch(attrs.stTryBackgroundImage, function(value) {
                console.log("new value = ", value);
                if(value == undefined) {
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
