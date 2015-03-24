/**
 * calendarjs
 * @version 0.1.1
 */
var calendarFlow = angular.module('calendar-flow', ['calendar-templates']);

calendarFlow.controller("MainController", function($scope,$timeout) {
});

/* ----------- */
calendarFlow.controller('CalendarController', function ($timeout, $scope, GenerativeJobQueue,
                                                        DateUtils, DaysSelection) {

    // Define constants
    var INITIAL_MONTHS_SHOWN = 15;
    var WEEKS_OFFSET = 2;

    // Create defaults
    if ($scope.onSelectDaysParent === undefined) { $scope.onSelectDaysParent = function() {}; }
    if ($scope.onHoverDayParent === undefined) { $scope.onHoverDayParent = function() {}; }
    if ($scope.onLoadDayContent === undefined) {
        $scope.onLoadDayContent = function(t, days) {
            t();
        };
    }
    if ($scope.providedCellClass === undefined) { $scope.providedCellClass = ""; }

    // This object holds the external api for the calendar, i.e. function that other components outside
    // calendar can call to manipulate the calendar from the outside
    $scope.api = {};

    $scope.calendar = [];

    if ($scope.showHover === undefined) {
        $scope.showHover = true;
    }

    var baseCellClasses = function(day, week) {
        return {
            'cf-empty': !day.content,
            'cf-even': day.date.getMonth() % 2 === 0,
            'cf-odd': day.date.getMonth() % 2 === 1,
            'cf-today': DateUtils.isToday(day.date),
            'cf-selected': day.selected
        };
    };

    $scope.cellClass = function(day, week) {
        var classes = baseCellClasses(day, week);
        if ($scope.providedCellClass !== null) {
            _.extend(classes, $scope.providedCellClass({day: day, week: week}));
        }
        return classes;
    };

    var firstDay = DateUtils.lastSunday(DateUtils.firstDayOfThisMonth());
    var lastDay = DateUtils.clone(firstDay);
    var loadedWeeks = [];
    var pendingWeeks = [];

    var weekElements = null;

    function hasPositiveHeight(element) {
        return element !== null && element.outerHeight() !== null && element.outerHeight() > 0;
    }

    function getCellHeight() {
        if (!hasPositiveHeight(weekElements)) {
            weekElements = $scope.calendarElem.find(".cf-week");
        }
        if (!hasPositiveHeight(weekElements)) {
            weekElements = null;
            return null;
        } else {
            return weekElements.outerHeight() + 4;
        }
    }

    function computeNumberOfWeeks(firstDay) {
        // Get first day of the next month
        var lastDay = DateUtils.plusDays(firstDay, 27);
        while (lastDay.getDate() != 1) {
            lastDay = DateUtils.nextDay(lastDay);
        }
        lastDay = DateUtils.lastSunday(lastDay);
        var days = DateUtils.differenceInDays(firstDay, lastDay);
        return Math.ceil(days / 7.0);
    }

    function createMonth(day, nweeks) {
        return {
            number: day.getMonth(),
            name: DateUtils.getMonthName(day),
            year: day.getFullYear(),
            nweeks: nweeks
        };
    }

    function createWeek(day){
        return {
            loaded: false,
            pending: false,
            month: createMonth(day, 0),
            firstOfTheMonth: false,
            days: []
        };
    }

    function createMonthByFirstDay(day01) {
        var nweeks = computeNumberOfWeeks(day01);
        return createMonth(day01, nweeks);
    }

    // Days cannot be moved from within weeks or arbitrary added to weeks
    function pushDayIntoWeek(week, date) {
        week.days.push({
            selected: false,
            previousSelectedState: false,
            content: null,
            date: DateUtils.clone(date)
        });
    }

    $scope.loadPreviousMonth = function() {
        var isBeginningOfMonth = false;
        while (!isBeginningOfMonth) {
            firstDay = DateUtils.plusWeeks(firstDay, -1);
            var currentWeek = createWeek(firstDay);

            for (var i = 0; i < 7; i++) {
                if (firstDay.getDate() == 1) {
                    isBeginningOfMonth = true;
                    currentWeek.firstOfTheMonth = true;
                    currentWeek.month = createMonthByFirstDay(firstDay);
                }
                pushDayIntoWeek(currentWeek, firstDay);
                firstDay = DateUtils.nextDay(firstDay);
            }

            if ($scope.calendar.length > 0) {
                currentWeek.nextUnloadedWeek = $scope.calendar[0];
                $scope.calendar[0].previousUnloadedWeek = currentWeek;
            }
            $scope.calendar.unshift(currentWeek);
            firstDay = DateUtils.plusWeeks(firstDay, -1);
        }
        // calendar[0].month.nweeks is the number of weeks (lines) added, so
        // cellHeight * it will give us the height
        return getCellHeight() * $scope.calendar[0].month.nweeks;
    };

    $scope.loadNextMonth = function() {
        var isBeginningOfMonth = false;
        if ($scope.calendar.length > 0) {
            lastWeek = $scope.calendar[$scope.calendar.length - 1];
            lastWeek.month.name = DateUtils.getMonthName(lastDay);
            lastWeek.month.year = lastDay.getFullYear();
        }
        while (!isBeginningOfMonth) {
            var currentWeek = createWeek(lastDay);

            for (var i = 0; i < 7; i++) {
                if (lastDay.getDate() == 1) {
                    isBeginningOfMonth = true;
                    currentWeek.firstOfTheMonth = true;
                    currentWeek.month = createMonthByFirstDay(lastDay);
                }
                pushDayIntoWeek(currentWeek, lastDay);
                lastDay = DateUtils.nextDay(lastDay);
            }

            var n = $scope.calendar.length;
            if (n > 0) {
                $scope.calendar[n - 1].nextUnloadedWeek = currentWeek;
                currentWeek.previousUnloadedWeek = $scope.calendar[n - 1];
            }
            $scope.calendar.push(currentWeek);
        }
        $scope.calendar[$scope.calendar.length - 1].month.name = "";
    };

    var nextLoadArea = null; // {i, j, a}

    var currentScroll = null;

    function setLoadAreaByScroll(scroll) {
        var n = $scope.calendar.length;
        var i = Math.floor(scroll.top * n / scroll.total);      // first week
        var j = Math.floor(scroll.bottom * n / scroll.total);   // last week
        var m = Math.floor((i + j) / 2);
        var a = j - i + 1;
        nextLoadArea = {i: m, j: m + 1, a: a};
    }

    function trySetLoadAreaByLastScroll() {
        if (currentScroll !== null) {
            setLoadAreaByScroll(currentScroll);
        }
    }

    $scope.onScroll = function(top, bottom, total) {
        currentScroll = {top: top, bottom: bottom, total: total};
        setLoadAreaByScroll(currentScroll);
        contentQueue.trigger();
    };

    var contentQueue = new GenerativeJobQueue({
        bottleneck: 2, // number of requests goes up to 2 * bottleneck

        executor: function(terminate) {
            if (nextLoadArea === null) { return terminate(true); }

            var a = {valid: false};
            a.j = previousUnloadedWeek(nextLoadArea.i);
            if (a.j !== null) {
                a.i = Math.max(0, a.j - 2 * nextLoadArea.a);
                a.i = nextUnloadedWeek(a.i);
                a.valid = isValidArea(a.i, a.j);
            }
            if (a.valid) { nextLoadArea.i = a.i; }

            var b = {valid: false};
            b.i = nextUnloadedWeek(nextLoadArea.j);
            if (b.i !== null) {
                b.j = Math.min($scope.calendar.length - 1, b.i + 2 * nextLoadArea.a);
                b.j = previousUnloadedWeek(b.j);
                b.valid = isValidArea(b.i, b.j);
            }
            if (b.valid) { nextLoadArea.j = b.j; }

            if (!a.valid && !b.valid) {
                return terminate(true);
            }

            var i = a.valid + b.valid;
            var stop = true;
            var doubleTerminator = function(localStop) {
                stop = stop && localStop;
                if (--i === 0) { terminate(stop); }
            };

            if (a.valid) { loadDayContent(doubleTerminator, a.i, a.j); }
            if (b.valid) { loadDayContent(doubleTerminator, b.i, b.j); }
        }
    });

    function isValidArea(i, j) {
        return i !== null && j !== null && i <= j;
    }

    function markAsLoaded(weeks) {
        _.each(weeks, function(week) {
            week.loaded = true;
            loadedWeeks.push(week);
        });
    }

    function unloadWeeks(weeks) {
        _.each(weeks, function(week) {
            week.loaded = false;
            _.each(week.days, function(day) {
                day.content = null;
            });
        });
        loadedWeeks = _.difference(loadedWeeks, weeks);
    }

    function markAsPending(weeks) {
        _.each(weeks, function(week) {
            week.pending = true;
            pendingWeeks.push(week);
        });
    }

    function unsetPendingWeeks(weeks) {
        _.each(weeks, function(week) {
            week.pending = false;
        });
        pendingWeeks = _.difference(pendingWeeks, weeks);
    }

    // return true to stop subsequent requests
    function onDayContentLoaded(days, weeks, error, id) {
        if (!_.contains(authorizedRequests, id)) {
            // Even though invalidate clears the weeks, when the invalidated request returns
            // the caller (above us) can (and will) change the day objects, so we invalidate
            // them again here, before Angular take over the ui
            unloadWeeks(weeks);
            //
            // TODO: Handle the following case, where Request 1 and Request 2 have the same weeks
            //
            //                | -> invalidation
            //                |
            // Request 1: [---|----------]
            // Request 2:     | [-----]
            //
            // In this case Request 1 return will invalidates (and clears) Request 2 results
            //
            // Hint1: Put an attribute on every week called lastRequestId and overload unloadWeeks()
            //        with a version that receives the request id that wants to unload the weeks, and
            //        test if lastRequestId equals the id provided, if not, don't unload.
            // Hint2: To test this, put a gaussian timeout on AssignmentsResource.getAssignableDays()
            //        open up Chrome's network console tab and wait for the case scenario.
            //
            return false;
        }

        unsetPendingWeeks(weeks);
        if (error) {
            trySetLoadAreaByLastScroll();
            // TODO: The logic below is just for one request per worker and one worker,
            // TODO: we may have up to 2 request per worker and a lot of workers
            // Suppose the server is faulty, on average we will retry (1-p)/p times
            // where p is the probability of stopping, so for an average of 5 times
            // we have (1-p)/p = 5 => p = 1/6 chances of stopping (= not retrying)
            // But to stop the worker we need to make both requests (if 2) stop,
            // hence chances gotta go up to sqrt(1/6) = 0.40825
            // But we have more than one worker (cmd+f bottleneck), we have 2, so
            // new value is sqrt(0.40825) = 0.63894
            var stop = Math.random() < 0.63894;
            return stop;
        } else {
            markAsLoaded(weeks);
            return false;
        }
    }

    function debugWeeks(weeks) {
        if (weeks.length === 0) {
            return '<empty weeks>';
        }
        return 'end = ' + DateUtils.toISOLocalDateString(weeks[weeks.length - 1].days[6].date) +
               ', start = ' + DateUtils.toISOLocalDateString(weeks[0].days[0].date);
    }

    // To prevent requests returned after calendar was invalidated from changing it
    var authorizedRequests = [];

    function loadDayContent(terminator, i, j) {
        var id = {};
        authorizedRequests.push(id);

        var weeks = $scope.calendar.slice(i, j + 1);
        var days = _.chain(weeks).map('days').flatten().value();
        markAsPending(weeks);

        if (days.length > 0 && $scope.onLoadDayContent !== null) {
            var wrappedTerminator = function(error) {
                var stop = onDayContentLoaded(days, weeks, error, id);
                terminator(stop);
            };
            $scope.onLoadDayContent(wrappedTerminator, days);
        } else {
            terminator(true);
        }
    }

    $scope.api.invalidateContent = function() {
        // TODO: Stop pending requests
        unloadWeeks(loadedWeeks);
        unsetPendingWeeks(pendingWeeks);
        authorizedRequests = [];
        trySetLoadAreaByLastScroll();
        contentQueue.trigger();
    };

    $scope.api.loadingStatus = function() {
        return {
            weeks: {
                pending: pendingWeeks.length,
                loaded: loadedWeeks.length,
                total: $scope.calendar.length
            },
            active: contentQueue.active()

        };
    };

    $scope.api.getDays = function(dates) {
        return _.map(dates, function(date) {
            return getDay(date);
        });
    };

    function nextUnloadedWeek(i) {
        for (var n = $scope.calendar.length; i < n; i++) {
            var w = $scope.calendar[i];
            if (!w.loaded && !w.pending) { return i; }
        }
        return null;
    }

    function previousUnloadedWeek(i) {
        for ( ; i >= 0; i--) {
            var w = $scope.calendar[i];
            if (!w.loaded && !w.pending) { return i; }
        }
        return null;
    }

    $scope.api.goToDate = function(date, duration) {
        if (duration === undefined) {
            duration = 'fast';
        }
        var weeks = getWeekIndex(date) - WEEKS_OFFSET;
        /* if (weeks < 0) {
            // TODO: Create on demand
        } else if (weeks >= $scope.calendar.length) {
            // TODO: Create on demand
        } else */
        if(weeks >= 0) {
            // TODO: Find an angular way of doing this
            var calendarElement = $scope.calendarElem.find(".cf-view-calendar-wrapper");
            var scroll = getCellHeight() * weeks;
            if (duration !== 0) {
                calendarElement.animate({scrollTop: scroll}, duration);
            } else {
                calendarElement[0].scrollTop = scroll;
            }
        }
    };

    $scope.api.goToToday = function(duration) {
        $scope.api.goToDate(DateUtils.TODAY, duration);
    };

    $scope.showCalendar = true;

    function initializeCalendar() {
        for (var i = 1; i <= INITIAL_MONTHS_SHOWN; i++) {
            if (i <= Math.ceil(INITIAL_MONTHS_SHOWN / 2.0)) {
                $scope.loadNextMonth();
            } else {
                $scope.loadPreviousMonth();
            }
        }

        // Wait some time so that getCellHeight() can return
        // something useful, since this function is used inside goToToday(...)
        $timeout(function() {
            $scope.api.goToToday(0);
            // BUG: A piece of the screen is cut
            //$scope.showCalendar = true;
        });
    }

    $timeout(initializeCalendar);

    function getDay(date) {
        var w = getWeekIndex(date);
        var day = $scope.calendar[w].days[date.getDay()];
        if (!DateUtils.equalsDate(date, day.date)) {
            console.log("Error! CalendarController getDay() not working");
        }
        return day;
    }

    function getWeekIndex(date) {
        var days = DateUtils.differenceInDays(DateUtils.lastSunday(firstDay), date);
        return Math.floor(days / 7);
    }

    function dayRange(a, b) {
        var dates = DaysSelection.dateRange(a.date, b.date);
        return _.map(dates, function(date) {
            return getDay(date);
        });
    }

    $scope.api.clearSelectedDays = function() {
        selection.clear();
        twoStepStart = null;
    };

    function onSelectDays() {
        $scope.onSelectDaysParent(selection);
    }

    $scope.api.selectDates = function(dates) {
        var days = $scope.api.getDays(dates);
        selection.select(days);
        onSelectDays();
    };

    function isCtrl(e) {
        return e.ctrlKey || e.metaKey; // metaKey is apple's cmd
    }

    var selection = new DaysSelection();
    var twoStepStart = null;

    // State Machine regarding one mouse operation (down, move, up), not across operations
    var States = {
        IDLE: {},
        DOWN: {day: null, event: null},
        CLICKED: {event: null, day: null},
        DRAGGING: {start: null, current: null, previous: null},
        DRAGGED: {start: null, end: null}
    };
    var state = States.IDLE;

    function onStateUpdate() {
        var e = state.event;
        switch (state) {
            case States.DOWN:
                if (!isCtrl(e)) {
                    selection.clear();
                }
                break;
            case States.CLICKED:
                // onDayClick(state.day, state.event);
                var day = state.day;
                if (e.shiftKey) {
                    if (twoStepStart !== null) {
                        var range = dayRange(twoStepStart, day);
                        selection.select(range);
                    } else {
                        selection.select([day]);
                        twoStepStart = day;
                    }
                } else {
                    if (day.selected && isCtrl(e)) {
                        selection.unselect([day]);
                        twoStepStart = null;
                    } else if (!day.selected) {
                        selection.select([day]);
                        twoStepStart = day;
                    }
                }
                state = States.IDLE;
                break;
            case States.DRAGGED:
                // They are already selected (see States.DRAGGING)
                state = States.IDLE;
                break;
            case States.DRAGGING:
                if (state.previous !== null) {
                    var previousRange = dayRange(state.start, state.previous);
                    selection.resetToPreviousState(previousRange);
                }
                var currentRange = dayRange(state.start, state.current);
                selection.select(currentRange);
                twoStepStart = null;
                break;
        }
        if (state == States.DOWN) { return; } // we should just update the parent when mouse is up or dragging
        onSelectDays();
    }

    $scope.onMouseDown = function(day, $event) {
        if (state == States.IDLE) {
            state = States.DOWN;
            state.day = day;
            state.event = $event;
            onStateUpdate();
        }
    };

    $scope.showTooltip = false;

    $scope.onMouseEnter = function(day) {
        if ($scope.tooltip !== undefined && $scope.tooltip(day) !== undefined) {
            $scope.showTooltip = true;
        } else {
            $scope.showTooltip = false;
        }

        $scope.onHoverDayParent(day);
        if (state == States.DOWN) {
            if (state.day != day) {
                var start = state.day;
                state = States.DRAGGING;
                state.start = start;
                state.current = day;
                state.previous = null;
                onStateUpdate();
            }
        } else if (state == States.DRAGGING) {
            if (state.current != day) {
                state.previous = state.current;
                state.current = day;
                onStateUpdate();
            }
        }
    };

    $scope.onMouseUp = function(day, $event) {
        if (state == States.DOWN) {
            state = States.CLICKED;
            state.event = $event;
            state.day = day;
            onStateUpdate();
        } else if (state == States.DRAGGING) {
            if (state.current != day) { throw "Invalid state"; }
            var start = state.day;
            state = States.DRAGGED;
            state.start = start;
            state.end = day;
            onStateUpdate();
        }
    };
});

calendarFlow.directive('calendar', function() {
    return {
        restrict: 'E',
        scope: {
            onSelectDaysParent: '=?onSelectDays',
            onHoverDayParent: '=?onHoverDay',
            onLoadDayContent: '=?onLoadDayContent',
            providedCellClass: '&?cellClass',
            tooltip: '=?getDayTooltip',
            api: '=?exposeApiTo',
            showHover: "=?"
        },
        link: function(scope, elem, attr) {
            scope.calendarElem = $(elem);
        },
        templateUrl: 'views/calendar.html',
        controller: 'CalendarController'
    };
});

/* ----------- */
calendarFlow.factory('DateUtils', [function() {
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
            if (dates.length === 0) { return true; }
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
                if (n < 10) { return '0' + n; }
                return n;
            };
            return date.getFullYear() +
                '-' + pad(date.getMonth() + 1) +
                '-' + pad(date.getDate());
        }
    };
}]);

calendarFlow.factory('GenerativeJobQueue', [function() {

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
    };

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
    };

    GenerativeJobQueue.prototype.active = function() {
        return this.pool.length > 0;
    };

    return GenerativeJobQueue;
}]);

calendarFlow.directive('dayTooltip', function($timeout) {
    return {
        restrict: 'A',
        scope: {
            dayTooltip: "@",
        },
        link: function(scope, element, attrs) {
            var my = 'left bottom-5';
            var at = 'left top';
            $(element).tooltip({
                content: function() {
                    return $(this).attr('title');
                },
                position: {my: my, at: at},
                show: false,
                hide: false
            });
            scope.$watch('dayTooltip', function () {
                if (scope.dayTooltip == "true") {
                    $(element).tooltip('enable');
                } else {
                    $(element).tooltip('disable');
                }
            });
        }
    };
});

/* ----------- */
// Class to manipulate a selection of days, in which day is an object defined in calendar_controller.js

calendarFlow.factory('DaysSelection', ['DateUtils', function(DateUtils) {
    function DaysSelection() {
        this.days = [];
    }

    DaysSelection.prototype.each = function(f) {
        _.each(this.days, f, this);
    };

    DaysSelection.prototype.select = function(days) {
        _.each(days, function(day) {
            day.previousSelectedState = day.selected;
            day.selected = true;
            if (!this.contains(day)) {
                this.days.push(day);
            }
        }, this);
    };

    DaysSelection.prototype.unselect = function(days) {
        _.each(days, function(day) {
            day.previousSelectedState = day.selected;
            day.selected = false;
            if (this.contains(day)) {
                this.days = _.without(this.days, day);
            }
        }, this);
    };

    DaysSelection.prototype.clear = function() {
        _.each(this.days, function(day) {
            day.previousSelectedState = true;
            day.selected = false;
        }, this);
        this.days = [];
    };

    DaysSelection.prototype.contains = function(day) {
        return _.contains(this.days, day);
    };

    DaysSelection.prototype.resetToPreviousState = function(days) {
        _.each(days, function(day) {
            day.selected = day.previousSelectedState;
            if (day.selected && !this.contains(day)) {
                this.days.push(day);
            } else if (!day.selected && this.contains(day)) {
                this.days = _.without(this.days, day);
            }
        }, this);
    };

    DaysSelection.prototype.dates = function() {
        return _.map(this.days, function(day) {
            return day.date;
        }, this);
    };

    DaysSelection.prototype.getDays = function() {
        return _.clone(this.days);
    };

    DaysSelection.dateRange = function(a, b) {
        var i = DateUtils.min(a, b);
        b = DateUtils.max(a, b);
        var range = [];
        while (i < b || DateUtils.equalsDate(i, b)) {
            // Because Date holds a moment (timestamp) the last iteration relies on the time of each
            // date object, hence we make sure to include the last day here
            range.push(i);
            i = DateUtils.nextDay(i);
        }
        return range;
    };

    return DaysSelection;
}]);

/* ----------- */
calendarFlow.directive('scroller', function($timeout) {
    return {
        restrict: 'A',
        scope: {
            nextChunkMethod: "=",
            previousChunkMethod: "=", // return the height of the chunk if you don't want a flash on the screen,
            onScroll: "=",
            numberOfLoadedChunksPerScroll: "="
        },
        // Make scroll even if it does not have scroll (increase height to test)
        link: function($scope, element, attrs) {
            var rawElement = element[0];
            if ($scope.numberOfLoadedChunksPerScroll === undefined) {
                $scope.numberOfLoadedChunksPerScroll = 1;
            }

            element.bind('scroll', function () {
                $timeout(function() {
                    $scope.onScroll(
                        rawElement.scrollTop,
                        rawElement.scrollTop + rawElement.offsetHeight - 5,
                        rawElement.scrollHeight);

                    // + 5 error margin
                    if ((rawElement.scrollTop + rawElement.offsetHeight + 5) >= rawElement.scrollHeight &&
                        $scope.nextChunkMethod) {
                        // User has scrolled down and we should fetch next chunk
                        $scope.$apply(function() {
                            for(var i = 0; i < $scope.numberOfLoadedChunksPerScroll; i++) {
                                $scope.nextChunkMethod();
                            }
                        });
                    }
                    if (rawElement.scrollTop === 0 && $scope.previousChunkMethod){
                        // User has scrolled up and we should fetch previous chunk
                        $scope.$apply(function() {
                            var chunkSize = 0;
                            for(var i = 0; i < $scope.numberOfLoadedChunksPerScroll; i++) {
                                chunkSize += $scope.previousChunkMethod();
                            }
                            if (chunkSize) { // If the provided function gave us the height, GOOD
                                rawElement.scrollTop = chunkSize;
                            } else { // Otherwise we have to calculate and flash the screen =(
                                var previousHeight = rawElement.scrollHeight;
                                setTimeout(function () {
                                    rawElement.scrollTop = rawElement.scrollHeight - previousHeight;
                                }, 0);
                            }
                        });
                    }
                });
            });
        }
    };
});

/* ----------- */
angular.module('calendar-templates', ['views/calendar.html']);

angular.module("views/calendar.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("views/calendar.html",
    "<div class=cf-calendar-container><table class=\"cf-calendar-header cf-view-calendar\"><tr><th class=cf-day>Sun</th><th class=cf-day>Mon</th><th class=cf-day>Tue</th><th class=cf-day>Wed</th><th class=cf-day>Thu</th><th class=cf-day>Fri</th><th class=cf-day>Sat</th><th class=cf-month>Month</th></tr></table><div class=cf-view-calendar-wrapper scroller previous-chunk-method=loadPreviousMonth next-chunk-method=loadNextMonth number-of-loaded-chunks-per-scroll=1 on-scroll=onScroll ng-class=\"{'cf-invisible': !showCalendar}\"><table class=cf-view-calendar ng-class=\"{'cf-hoverable': showHover}\"><tr ng-repeat=\"week in calendar\" ng-class=\"{\n" +
    "                'cf-first': week.firstOfTheMonth,\n" +
    "                'cf-week': true,\n" +
    "                'cf-pending': week.pending || !week.loaded\n" +
    "            }\"><td class=cf-day ng-repeat=\"day in week.days\" day-tooltip={{showTooltip}} ng-attr-title={{tooltip(day)}} ng-mousedown=\"onMouseDown(day, $event)\" ng-mouseup=\"onMouseUp(day, $event)\" ng-mouseenter=\"onMouseEnter(day, $event)\" ng-class=\"cellClass(day, week)\">{{day.date.getDate()}}</td><td ng-if=week.firstOfTheMonth rowspan={{week.month.nweeks}} ng-class=\"{\n" +
    "                    'cf-month': true,\n" +
    "                    'cf-even': week.month.number % 2 == 0,\n" +
    "                    'cf-odd': week.month.number % 2 == 1,\n" +
    "                }\">{{week.month.name}}<br>{{week.month.year}}</td></tr></table></div></div>");
}]);
