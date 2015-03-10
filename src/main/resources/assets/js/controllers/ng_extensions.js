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
        },
        onInitialization: function (variableName, scope, callback) {
            var unwatch = scope.$watch(variableName, function(variable) {
                if (variable == undefined || variable.$resolved == false) return;
                callback();
                unwatch();
            })
        },
        Objects: {
            deepField: function(object, deepField, value) {
               var fields = deepField.split('.');
               var last = fields.pop();
               var previous = _.reduce(fields, function(proxy, field) {
                   return proxy[field];
               }, object);
               if (arguments.length == 3) {
                   previous[last] = value;
                   return value;
               } else {
                   return previous[last];
               }
            }
        }
    };
}]);

App.factory('DateUtils', [function() {
    var TODAY = new Date();
    MS_PER_DAY = 1000 * 60 * 60 * 24;
    var monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September",
                    "October", "November", "December"];
    return {
        TODAY: TODAY,
        MS_PER_DAY: MS_PER_DAY,
        SORT_BY: function(d) {
            //noinspection JSConstructorReturnsPrimitive
            return d.getTime();
        },
        min: function(a, b) {
            return (a < b) ? a : b;
        },
        max: function(a, b) {
         return (a > b) ? a : b;
        },
        isRange: function(dates) {
            if (dates.length == 0) return true;
            var min = _.min(dates, this.SORT_BY);
            var max = _.max(dates, this.SORT_BY);
            return this.differenceInDays(min, max) + 1 == dates.length;
        },
        fromISOLocalString: function(string) {
            var parts = string.split("-");
            var d = new Date();
            d.setYear(parseInt(parts[0]));
            d.setMonth(parseInt(parts[1]) - 1, parseInt(parts[2]));
            return d;
        },
        firstDayOfThisMonth: function() {
            var d = new Date(TODAY.getTime());
            d.setDate(1);
            return d;
        },
        plusDays: function(date, days) {
            var d = this.clone(date);
            d.setDate(d.getDate() + days);
            return d;
        },
        plusWeeks: function(date, weeks) {
            return this.plusDays(date, weeks * 7);
        },
        clone: function(date) {
            return new Date(date.getTime());
        },
        differenceInDays: function(from, to) {
            // Discard the time and time-zone information.
            var utc1 = Date.UTC(from.getFullYear(), from.getMonth(), from.getDate());
            var utc2 = Date.UTC(to.getFullYear(), to.getMonth(), to.getDate());
            return Math.floor((utc2 - utc1) / MS_PER_DAY);
        },
        isToday: function(date) {
            return this.equalsDate(date, TODAY);
        },
        nextDay: function(date) {
            return this.plusDays(date, 1);
        },
        previousDay: function(date) {
            return this.plusDays(date, -1);
        },
        lastSunday: function(date) {
            return this.plusDays(date, -date.getDay());
        },
        equalsDate: function(date1, date2) {
            return date1.toDateString() == date2.toDateString();
        },
        getMonthName: function(date) {
            return monthNames[date.getMonth()];
        },
        toISOLocalDateString: function(date) {
            var pad = function(n) {
                if (n < 10) return '0' + n;
                return n;
            }
            return date.getFullYear() +
                '-' + pad(date.getMonth() + 1) +
                '-' + pad(date.getDate());
        }
    }
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
        this.pool.splice(this.pool.indexOf(job), 1);

        if (!stop) {
            this.trigger();
        }
    }

    /* public */
    var maxJobId = 0;
    GenerativeJobQueue.prototype.trigger = function() {
        var terminators = [];
        while (this.pool.length < this.bottleneck) {
            var job = {id: maxJobId++};
            this.pool.push(job);
            terminators.push(this.terminator.bind(this, job));
        }
        // Separate loop because terminator can be sync
        _.each(terminators, function(terminator) {
            this.executor(terminator);
        }.bind(this));
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
        this.promise = $interval(this.wrappedWatcher.bind(this), this.interval)

        // timeout
        $timeout(function() {
            if (this.promise != null) {
                this.dismiss();
            }
        }.bind(this), this.timeout);
    }

    ProgressBar.prototype.dismiss = function() {
        $interval.cancel(this.promise);
        this.promise = null;
        $timeout(function() {
            this.inner.removeClass('error');
            this.inner.css({visibility: 'hidden', width: '0%'});
        }.bind(this), this.delay);
    }

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
                if (scope.stTooltip == "true") {
                    $(element).tooltip('enable');
                } else {
                    $(element).tooltip('disable');
                }
            });
        }
    };
});