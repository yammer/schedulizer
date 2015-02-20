App.controller('CalendarController', function ($timeout, $scope, Utils, GenerativeJobQueue) {

    var INITIAL_MONTHS_SHOWN = 15;
    var WEEKS_OFFSET = 2;

    // This object holds the external api for the calendar, i.e. function that other components outside
    // calendar can call to manipulate the calendar from the outside
    $scope.api = {};

    $scope.calendar = [];

    var baseCellClasses = function(day, week) {
        return {
            'empty': !day.content,
            'even': day.date.getMonth() % 2 == 0,
            'odd': day.date.getMonth() % 2 == 1,
            'today': day.date.isToday(),
            'selected': day.selected,
            'hint--top hint--no-animate': $scope.tooltip != undefined && $scope.tooltip(day) != null
        };
    };

    $scope.cellClass = function(day, week) {
        var classes = baseCellClasses(day, week);
        if ($scope.providedCellClass != null) {
            _.extend(classes, $scope.providedCellClass({day: day, week: week}));
        }
        return classes;
    };

    var firstDay = Date.firstDayOfThisMonth().lastSunday();
    var lastDay = firstDay.clone();
    var loadedWeeks = [];
    var pendingWeeks = [];

    var weekElements = null;

    function getCellHeight() {
        if (weekElements == null) {
            weekElements = $(".week");
        }
        var h = weekElements.outerHeight();
        if (h == null || h <= 0) {
            weekElements = null;
            return null;
        } else {
            return h + 4;
        }
    }

    function computeNumberOfWeeks(firstDay) {
        // Get first day of the next month
        var lastDay = firstDay.plusDays(27);
        while (lastDay.getDate() != 1) {
            lastDay = lastDay.next();
        }
        lastDay = lastDay.lastSunday();
        var days = Date.differenceInDays(firstDay, lastDay);
        return Math.ceil(days / 7.0);
    }

    function createMonth(day, nweeks) {
        return {
            number: day.getMonth(),
            name: day.getMonthName(),
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
            date: date.clone()
        });
    }

    $scope.loadPreviousMonth = function() {
        var isBeginningOfMonth = false;
        while (!isBeginningOfMonth) {
            firstDay = firstDay.plusWeeks(-1);
            var currentWeek = createWeek(firstDay);

            for (var i = 0; i < 7; i++) {
                if (firstDay.getDate() == 1) {
                    isBeginningOfMonth = true;
                    currentWeek.firstOfTheMonth = true;
                    currentWeek.month = createMonthByFirstDay(firstDay);
                }
                pushDayIntoWeek(currentWeek, firstDay);
                firstDay = firstDay.next();
            }

            if ($scope.calendar.length > 0) {
                currentWeek.nextUnloadedWeek = $scope.calendar[0];
                $scope.calendar[0].previousUnloadedWeek = currentWeek;
            }
            $scope.calendar.unshift(currentWeek);
            firstDay = firstDay.plusWeeks(-1);
        }
        // calendar[0].month.nweeks is the number of weeks (lines) added, so
        // cellHeight * it will give us the height
        return getCellHeight() * $scope.calendar[0].month.nweeks;
    };

    $scope.loadNextMonth = function() {
        var isBeginningOfMonth = false;
        if ($scope.calendar.length > 0) {
            lastWeek = $scope.calendar[$scope.calendar.length - 1];
            lastWeek.month.name = lastDay.getMonthName();
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
                lastDay = lastDay.next();
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
        if (currentScroll != null) {
            setLoadAreaByScroll(currentScroll);
        }
    }

    $scope.onScroll = function(top, bottom, total) {
        currentScroll = {top: top, bottom: bottom, total: total};
        setLoadAreaByScroll(currentScroll);
        contentQueue.trigger();
    }

    var contentQueue = new GenerativeJobQueue({
        bottleneck: 2, // number of requests goes up to 2 * bottleneck

        executor: function(terminate) {
            if (nextLoadArea == null) return terminate(true);

            var a = {valid: false};
            a.j = previousUnloadedWeek(nextLoadArea.i);
            if (a.j != null) {
                a.i = Math.max(0, a.j - 2 * nextLoadArea.a);
                a.i = nextUnloadedWeek(a.i);
                a.valid = isValidArea(a.i, a.j);
            }
            if (a.valid) nextLoadArea.i = a.i;

            var b = {valid: false};
            b.i = nextUnloadedWeek(nextLoadArea.j);
            if (b.i != null) {
                b.j = Math.min($scope.calendar.length - 1, b.i + 2 * nextLoadArea.a);
                b.j = previousUnloadedWeek(b.j);
                b.valid = isValidArea(b.i, b.j);
            }
            if (b.valid) nextLoadArea.j = b.j;

            if (!a.valid && !b.valid) {
                return terminate(true);
            }

            var i = a.valid + b.valid;
            var stop = true;
            var doubleTerminator = function(localStop) {
                stop = stop && localStop;
                if (--i == 0) terminate(stop);
            };

            if (a.valid) loadDayContent(doubleTerminator, a.i, a.j);
            if (b.valid) loadDayContent(doubleTerminator, b.i, b.j);
        }
    });

    function isValidArea(i, j) {
        return i != null && j != null && i <= j;
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
        if (!authorizedRequests.contains(id)) {
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
        if (weeks.length == 0) {
            return '<empty weeks>'
        }
        return 'end = ' + weeks[weeks.length - 1].days[6].date.toISOLocalDateString() + ', start = ' + weeks[0].days[0]
        .date.toISOLocalDateString();
    }

    // To prevent requests returned after calendar was invalidated from changing it
    var authorizedRequests = [];

    function loadDayContent(terminator, i, j) {
        var id = {};
        authorizedRequests.push(id);

        var weeks = $scope.calendar.slice(i, j + 1);
        var days = _.chain(weeks).map('days').flatten().value();
        markAsPending(weeks);

        if (days.length > 0 && $scope.onLoadDayContent != null) {
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
        })
    };

    function nextUnloadedWeek(i) {
        for (var n = $scope.calendar.length; i < n; i++) {
            var w = $scope.calendar[i];
            if (!w.loaded && !w.pending) return i;
        }
        return null;
    }

    function previousUnloadedWeek(i) {
        for ( ; i >= 0; i--) {
            var w = $scope.calendar[i];
            if (!w.loaded && !w.pending) return i;
        }
        return null;
    }

    $scope.api.goToDate = function(date, duration) {
        if (duration == undefined) {
            duration = 'fast';
        }
        var weeks = getWeekIndex(date) - WEEKS_OFFSET;
        if (weeks < 0) {
            // TODO: Create on demand
        } else if (weeks >= $scope.calendar.length) {
            // TODO: Create on demand
        } else {
            // TODO: Find an angular way of doing this
            var calendarElement = $(".view-calendar-wrapper");
            var scroll = getCellHeight() * weeks;
            if (duration != 0) {
                calendarElement.animate({scrollTop: scroll}, duration);
            } else {
                calendarElement[0].scrollTop = scroll;
            }
        }
    };

    $scope.api.goToToday = function(duration) {
        $scope.api.goToDate(Date.TODAY, duration);

        // Better way to get the today cell
        var today = $(".view-calendar .day.today");
        Utils.animate('tada', today);
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

    initializeCalendar();

    function getDay(date) {
        var w = getWeekIndex(date);
        var day = $scope.calendar[w].days[date.getDay()];
        if (!date.equalsDate(day.date)) {
            console.log("Error! CalendarController getDay() not working")
        }
        return day;
    }

    function getWeekIndex(date) {
        var days = Date.differenceInDays(firstDay.lastSunday(), date);
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
        switch (state) {
            case States.DOWN:
                var e = state.event;
                if (!isCtrl(e)) {
                    selection.clear();
                }
                break;
            case States.CLICKED:
                //onDayClick(state.day, state.event);
                var e = state.event;
                var day = state.day;
                if (e.shiftKey) {
                    if (twoStepStart != null) {
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
                if (state.previous != null) {
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

    $scope.onMouseEnter = function(day) {
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
            if (state.current != day) throw "Invalid state";
            var start = state.day;
            state = States.DRAGGED;
            state.start = start;
            state.end = day;
            onStateUpdate();
        }
    }

});

App.directive('calendar', function() {
    return {
        restrict: 'E',
        scope: {
            onSelectDaysParent: '=onSelectDays',
            onHoverDayParent: '=onHoverDay',
            onLoadDayContent: '=onLoadDayContent',
            providedCellClass: '&cellClass',
            tooltip: '=getDayTooltip',
            api: '=exposeApiTo'
        },
        templateUrl: 'views/calendar.html',
        controller: 'CalendarController'
    };
});
