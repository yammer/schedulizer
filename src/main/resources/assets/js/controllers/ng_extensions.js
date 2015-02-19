
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
                        if (scope.displayAbove == "true") {
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
                }
            });

            scope.$watch("tags", function(value) {
                if (value == null) return;

                if (scope.displayAbove == "true") {
                    availableTags = value.reverse();
                }
                else {
                    availableTags = value;
                }

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
            scope.$watch(attrs.stTryBackgroundImage, function(value) {
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
            Objects.deepField(scope, attrs.stName, element);
        }
    }
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
      }
    }
  }
}]);

App.filter('orderObjectBy', function() {
    return function(items, field, reverse) {
        var filtered = [];
        var deepField = field.split(".");
        angular.forEach(items, function(item) {
             filtered.push(item);
        });
        if (filtered.length <= 1) return filtered;
        filtered.sort(function(a, b) {
            for (var i = 0; i < deepField.length; i++) {
                a = a[deepField[i]];
                b = b[deepField[i]];
            }
            if (typeof a == 'string') {
                a = a.toLowerCase();
            }
            if (typeof b == 'string') {
                b = b.toLowerCase();
            }
            return (a > b ? 1 : -1);
        });
        if (reverse) filtered.reverse();
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
        if (filtered.length <= 1) return filtered;
        filtered.sort(function (a, b) {
            return (func(a.key) > func(b.key) ? 1 : -1);
        });
        if (reverse) filtered.reverse();
        return filtered;
    };
});

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
        // TODO: Remove preventBurst and lastOfBurst and use _.debounce
        preventBurst: function(func, threshold) {
            if (threshold) func._burstThreshold = threshold;
            if (!func._burstThreshold) func._burstThreshold = 50;

            return function() {
                if (!func._okToIssue) return func._lastValue;
                func._okToIssue = false;
                $timeout(function() {func._okToIssue = true;}, func._burstThreshold);
                func._lastValue = func();
                return func._lastValue;
            }
        },
        lastOfBurst: function(func, threshold) {
            if (threshold) func._lastOfBurstThreshold = threshold;
            if (!func._lastOfBurstThreshold) func._lastOfBurstThreshold = 50;

            func._burstTimeout = null;
            return function() {
                if (func._burstTimeout != null) {
                    $timeout.cancel(func._burstTimeout);
                }
                func._burstTimeout = $timeout(function() {
                    func._burstTimeout = null;
                    func();
                }, func._lastOfBurstThreshold);
            }
        }

    };
}]);

function JobQueue(/* arguments */) {
    return this.initialize.apply(this, arguments);
}

App.factory('GenerativeJobQueue', [function() {

    function GenerativeJobQueue(options, executor) {
        this.executor = options.executor;
        this.bottleneck = options.bottleneck || this.bottleneck;
    }

    GenerativeJobQueue.prototype.bottleneck = 2; // Generally used for xhr calls

    GenerativeJobQueue.prototype.pool = [];

    GenerativeJobQueue.prototype.terminator = function(job, stop) {
        this.pool.remove(job);
        if (!stop) {
            this.trigger();
        }
    }

    /* public */

    GenerativeJobQueue.prototype.trigger = function() {
        var terminators = [];
        while (this.pool.length < this.bottleneck) {
            var job = {};
            this.pool.push(job);
            terminators.push(this.terminator.curry(this, job));
        }
        // Separate loop because terminator can be sync
        _.each(terminators, function(terminator) {
            this.executor(terminator);
        }.curry(this));
    }

    GenerativeJobQueue.prototype.active = function() {
        return this.pool.length > 0;
    }

    return GenerativeJobQueue;
}]);

App.factory('ProgressBar', ['$timeout', '$interval', function($timeout, $interval) {

    var EPS = 0.001;

    function ProgressBar(inner, outer, watcher, options) {
        this.inner = $(inner);
        this.outer = $(outer);
        this.watcher = watcher;
        if (options && options.interval) this.interval = options.interval;
        if (options && options.delay) this.interval = options.delay;
        if (options && options.onBeforeWatch) this.onBeforeWatch = options.onBeforeWatch;
        if (options && options.timeout) this.timeout = options.timeout;
        if (options && options.headstart) this.headstart = option.headstart;
        this.promise = null
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
    }

    /* public */

    ProgressBar.prototype.trigger = function() {
        if (this.promise != null) return;
        this.onBeforeWatch();
        this.inner.css({visibility: 'visible'});
        this.promise = $interval(this.wrappedWatcher.curry(this), this.interval)

        // timeout
        $timeout(function() {
            if (this.promise != null) {
                this.dismiss();
            }
        }.curry(this), this.timeout);
    }

    ProgressBar.prototype.dismiss = function() {
        $interval.cancel(this.promise);
        this.promise = null;
        $timeout(function() {
            this.inner.removeClass('error');
            this.inner.css({visibility: 'hidden', width: '0%'});
        }.curry(this), this.delay);
    }

    return ProgressBar;
}]);

App.directive('stTooltip', function(){
    return {
        restrict: 'A',
        link: function(scope, element, attrs){
            if (attrs.stTooltip == "true") {
                $(element).tooltip({
                    content: function() {
                        return $(this).attr('title');
                    },
                    position: {my: 'left bottom-5', at: 'left top'},
                    show: false,
                    hide: false
                });
            }
        }
    };
});