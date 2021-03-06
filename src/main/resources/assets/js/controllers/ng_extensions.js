'use strict';

App.factory('Utils', ['$rootScope', '$animate', '$timeout', function($rootScope, $animate, $timeout) {
    return {
        animate: function(type, element) {
            return $animate.addClass(element, type + ' animated').then(function() {
                $rootScope.$apply(function() {
                    $animate.removeClass(element, type + ' animated');
                });
            });
        },
        shakeOnError: function(input) {
            var parent = input.parent();
            parent.addClass('has-error');
            this.animate('shake', input).then(function() {
                parent.removeClass('has-error');
            });
        },
        interpolate: function(current, srcScale, tgtScale) {
            return tgtScale[0] + (current - srcScale[0]) * (tgtScale[1] - tgtScale[0]) / (srcScale[1] - srcScale[0]);
        },
        // Angular-friendly ($timeout) versions of _.debounce()
        preventBurst: function(func, threshold) {
            if (threshold) { func._burstThreshold = threshold; }
            if (!func._burstThreshold) { func._burstThreshold = 50; }

            return function() {
                if (!func._okToIssue) { return func._lastValue; }
                func._okToIssue = false;
                $timeout(function() {func._okToIssue = true;}, func._burstThreshold);
                func._lastValue = func();
                return func._lastValue;
            };
        },
        lastOfBurst: function(func, threshold) {
            if (threshold) { func._lastOfBurstThreshold = threshold; }
            if (!func._lastOfBurstThreshold) { func._lastOfBurstThreshold = 50; }

            func._burstTimeout = null;
            return function() {
                if (func._burstTimeout != null) {
                    $timeout.cancel(func._burstTimeout);
                }
                func._burstTimeout = $timeout(function() {
                    func._burstTimeout = null;
                    func();
                }, func._lastOfBurstThreshold);
            };
        },
        onInitialization: function (variableName, scope, callback) {
            var unwatch = scope.$watch(variableName, function(variable) {
                if (variable === undefined || variable.$resolved === false) { return; }
                callback();
                unwatch();
            });
        },
        Objects: {
            deepField: function(object, deepField, value) {
               var fields = deepField.split('.');
               var last = fields.pop();
               var previous = _.reduce(fields, function(proxy, field) {
                   return proxy[field];
               }, object);
               if (arguments.length === 3) {
                   previous[last] = value;
                   return value;
               } else {
                   return previous[last];
               }
            }
        }
    };
}]);

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
            autocompleteSelect: "=",
            displayAbove: "@"
            //itemClass: "=attribute"

        },
        link: function(scope, element, attrs) {
            var availableTags;

            $(function() {
                availableTags = [];
                var autocomplete = $(element).autocomplete({
                    source: availableTags,
                    open: function(event, ui){
                        if (scope.displayAbove === "true") {
                            var input = $(event.target);
                            var results = input.autocomplete("widget");
                            var top = results.position().top;
                            var height = results.outerHeight();
                            var inputHeight = input.outerHeight();
                            var newTop = top - height - inputHeight;
                            results.css("top", newTop + "px");
                        }
                    },
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
                };
            });

            scope.$watch("tags", function(value) {
                if (value == null) { return; }

                if (scope.displayAbove === "true") {
                    availableTags = value.reverse();
                }
                else {
                    availableTags = value;
                }

                $(element).autocomplete("option", "source", availableTags);
                $(element).autocomplete("search");
            });
        }
    };
});

App.directive('stTryBackgroundImage', function($timeout) {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            scope.$watch(attrs.stTryBackgroundImage, function(value) {
                if(value == null) {
                    $(element)[0].style.backgroundImage = "";
                    return;
                }
                $(element)[0].style.backgroundImage = "url(" + value + ")";
            });
        }
    };
});

App.directive('stName', function(Utils) {
    return {
        restrict: 'A',
        // acts as below without requiring isolated scope
        //scope: {
        //    stName: '='
        //},
        link: function(scope, element, attrs) {
            Utils.Objects.deepField(scope, attrs.stName, element);
        }
    };
});

App.directive('stIf', ['$animate', function($animate) {
  return {
    transclude: 'element',
    priority: 1000,
    terminal: true,
    restrict: 'A',
    compile: function (element, attr, transclude) {
      return function ($scope, $element, $attr) {
        var childElement, childScope;
        $scope.$watch($attr.stIf, function ngIfWatchAction(value) {
          if (childElement) {
            $animate.leave(childElement);
            childElement = undefined;
          }
          if (value) {
            transclude($scope, function (clone) {
              childElement = clone;
              $animate.enter(clone, $element.parent(), $element);
            });
          }
        });
      };
    }
  };
}]);

App.filter('orderObjectBy', function() {
    return function(items, field, reverse) {
        var filtered = [];
        var deepField = field.split(".");
        angular.forEach(items, function(item) {
             filtered.push(item);
        });
        if (filtered.length <= 1) { return filtered; }
        filtered.sort(function(a, b) {
            for (var i = 0; i < deepField.length; i++) {
                a = a[deepField[i]];
                b = b[deepField[i]];
            }
            if (typeof a === 'string') {
                a = a.toLowerCase();
            }
            if (typeof b === 'string') {
                b = b.toLowerCase();
            }
            return (a > b ? 1 : -1);
        });
        if (reverse) { filtered.reverse(); }
        return filtered;
    };
});

App.filter('orderByExpressionAppliedOnTheKey', function() {
    return function(items, func, reverse) {
        var filtered = [];
        angular.forEach(items, function(item, key) {
             item.key = key;
             filtered.push(item);
        });
        if (filtered.length <= 1) { return filtered; }
        filtered.sort(function (a, b) {
            return (func(a.key) > func(b.key) ? 1 : -1);
        });
        if (reverse) { filtered.reverse(); }
        return filtered;
    };
});


App.factory('ProgressBar', ['$timeout', '$interval', function($timeout, $interval) {

    var EPS = 0.001;

    function ProgressBar(inner, outer, watcher, options) {
        this.inner = $(inner);
        this.outer = $(outer);
        this.watcher = watcher;
        if (options && options.interval) { this.interval = options.interval; }
        if (options && options.delay) { this.interval = options.delay; }
        if (options && options.onBeforeWatch) { this.onBeforeWatch = options.onBeforeWatch; }
        if (options && options.timeout) { this.timeout = options.timeout; }
        if (options && options.headstart) { this.headstart = options.headstart; }
        this.promise = null;
    }

    ProgressBar.prototype.promise = null;

    ProgressBar.prototype.interval = 200;

    ProgressBar.prototype.delay = 500;

    ProgressBar.prototype.timeout = 5 * 60 * 1000; // 5 minutes before killing

    ProgressBar.prototype.headstart = 0.06; // Useful for giving the user a hint that the progress bar exists

    ProgressBar.prototype.wrappedWatcher = function() {
        var p = this.headstart + this.watcher() * (1 - this.headstart);
        if (p < 0) { // means error
            this.inner.addClass('error');
            //this.dismiss();
        } else {
            // set visibility to ensure hidden timeout doesn't prevail after a trigger
            this.inner.removeClass('error');
            this.inner.css({visibility: 'visible', width: Math.floor(p * 100) + '%'});
            if (p + EPS > 1) {
                this.dismiss();
            }
        }
    };

    /* public */

    ProgressBar.prototype.trigger = function() {
        if (this.promise != null) { return; }
        this.onBeforeWatch();
        this.inner.css({visibility: 'visible'});
        this.promise = $interval(this.wrappedWatcher.bind(this), this.interval);

        // timeout
        $timeout(function() {
            if (this.promise != null) {
                this.dismiss();
            }
        }.bind(this), this.timeout);
    };

    ProgressBar.prototype.dismiss = function() {
        $interval.cancel(this.promise);
        this.promise = null;
        $timeout(function() {
            this.inner.removeClass('error');
            this.inner.css({visibility: 'hidden', width: '0%'});
        }.bind(this), this.delay);
    };

    return ProgressBar;
}]);

App.directive('stTooltip', function($timeout){
    return {
        restrict: 'A',
        scope: {
            stTooltip: "@",
            my: "@",
            at: "@",
            temporaryTooltip: "=?",
            tooltipDelay: "=?"
        },
        link: function(scope, element, attrs){
            var my = scope.my || 'left bottom-5';
            var at = scope.at || 'left top';
            $(element).tooltip({
                content: function() {
                    return $(this).attr('title');
                },
                position: {my: my, at: at},
                show: false,
                hide: false
            });
            if (scope.temporaryTooltip) {
                var delay = scope.tooltipDelay || 1000;
                $timeout(function() {
                    try {
                        $(element).tooltip("open");
                    } catch (e) {
                    }
                    $timeout(function() {
                        try {
                            $(element).tooltip("close");
                            $(element).tooltip("disable");
                        } catch (e) {
                        }
                    }, delay);
                }, 100);
            }
            scope.$watch('stTooltip', function () {
                if (scope.stTooltip === "true") {
                    // TODO: find out specific reason why this exception is being thrown
                    try{ // avoid annoying exception
                        $(element).tooltip('enable');
                    } catch (e) {}

                } else {
                    // TODO: find out specific reason why this exception is being thrown
                    try { // avoid annoying exception
                        $(element).tooltip('disable');
                    } catch (e) {}
                }
            });
        }
    };
});

// Two way binding with divs that have contenteditable
App.directive('contenteditable', function() {
    return {
        require: 'ngModel',
        link: function(scope, el, attrs, controller) {
            controller.$render = function() {
                if (typeof controller.$viewValue !== typeof "") {
                    return;
                }
                el.html(controller.$viewValue.replace(/\n/g, "<br>").replace(/\s/g, "&nbsp;"));
            };
            el.bind('blur keyup', function(e) {
                scope.$apply(function() {
                    controller.$setViewValue(el.html().replace(/<br\>/g, "\n").replace(/\&nbsp\;/g, " "));
                });
            });
            controller.$render();
        }
    };
});