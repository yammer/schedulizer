App.controller('CalendarController', function ($timeout, $scope) {

    var INITIAL_MONTHS_SHOWN = 15;
    var WEEKS_OFFSET = 2;

    $scope.calendar = [];
    var firstDay = Date.firstDayOfThisMonth().lastSunday();
    var lastDay = firstDay.clone();

    var cellHeight = null;

    function getCellHeight() {
        if (cellHeight === null) {
            var h = $(".week").outerHeight();
            if (h > 0) {
                // +4 from border-spacing, TODO refactor
                cellHeight = h + 4;
            }
        }
        return cellHeight;
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
            previousUnloadedWeek: null,
            nextUnloadedWeek: null,
            month: createMonth(day, 0),
            firstOfTheMonth: false,
            days: []
        };
    }

    function createMonthByFirstDay(day01) {
        var nweeks = computeNumberOfWeeks(day01);
        return createMonth(day01, nweeks);
    }

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
    }

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
    }

    $scope.onScrollStop = function(top, bottom, total) {
        var n = $scope.calendar.length;
        var i = Math.floor(top * n / total);
        var j = Math.floor(bottom * n / total);
        loadDayContent(i, j);
    }

    function loadDayContent(i, j) {
        i = Math.max(i - 0, 0);
        j = Math.min(j + 0, $scope.calendar.length - 1);
        i = nextUnloadedWeek(i);
        j = previousUnloadedWeek(j);
        if (i == null || j == null || i > j) {
            return;
        }
        var first = $scope.calendar[i].days[0].date;
        var last = $scope.calendar[j].days[6].date;
        var days = [];
        for (var w = i; w <= j; w++) {
            var week = $scope.calendar[w];
            week.loaded = true;
            days = days.concat(week.days);
        }
        if (days.length > 0 && $scope.onLoadDayContent != null) {
            $scope.onLoadDayContent(days);
        }
    }

    $scope.getDays = function(dates) {
        return _.map(dates, function(date) {
            return getDay(date);
        })
    }

    function nextUnloadedWeek(i) {
        for (var n = $scope.calendar.length; i < n; i++) {
            if (!$scope.calendar[i].loaded) return i;
        }
        return null;
    }

    function previousUnloadedWeek(i) {
        for ( ; i >= 0; i--) {
            if (!$scope.calendar[i].loaded) return i;
        }
        return null;
    }



    $scope.goToDate = function(date, duration) {
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
    }

    $scope.goToToday = function(duration) {
        $scope.goToDate(Date.TODAY, duration);
    }

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
            $scope.goToToday(0);
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

    $scope.clearSelectedDays = function() {
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
    }
    var state = States.IDLE

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
    }

    $scope.onMouseEnter = function(day) {
        $scope.onHoverDayParent(day);
        if (state == States.DOWN) {
            if (state.day != day) {
                var start = state.day
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
    }

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
            onLoadDayContent: '=onLoadDayContent'
        },
        templateUrl: 'views/calendar.html',
        controller: 'CalendarController'
    };
});