Date.prototype.getMonthName = function() {
    var monthNames = [ "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December" ];
    return monthNames[this.getMonth()];
};

var MS_PER_DAY = 1000 * 60 * 60 * 24;

// a and b are javascript Date objects
function dateDiffInDays(a, b) {
    // Discard the time and time-zone information.
    var utc1 = Date.UTC(a.getFullYear(), a.getMonth(), a.getDate());
    var utc2 = Date.UTC(b.getFullYear(), b.getMonth(), b.getDate());

    return Math.floor((utc2 - utc1) / MS_PER_DAY);
}

function computeNWeeks(firstDay) {
    var lastDay = new Date(firstDay.getTime());
    /* Get first day of the next month */
    lastDay.setDate(lastDay.getDate() + 27);
    while(lastDay.getDate() != 1) {
        lastDay.setDate(lastDay.getDate() + 1);
    }
    /* Get last sunday */
    while(lastDay.getDay() != 0) {
        lastDay.setDate(lastDay.getDate() - 1);
    }
    /* Get number of days between two dates*/
    var diff =  dateDiffInDays(firstDay, lastDay);


    return Math.ceil(diff/7.0);
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
        month: createMonth(day, 0),
        firstOfTheMonth: false,
        days: []
    };
}

function createMonthByFirstDay(day01) {
    var nweeks = computeNWeeks(new Date(day01.getTime()));
    return createMonth(day01, nweeks);
}

var TODAY = new Date();

function pushDayIntoWeek(week, day) {
    week.days.push({
        value: day.getDate(),
        month: day.getMonth(),
        date: new Date(day.getTime()),
        today: day.isToday()
    });
}

function getSunday(day) {
    var ans = new Date(day.getTime());
    ans.setDate(day.getDate() - day.getDay());
    return ans;
}


App.controller('CalendarController', function ($timeout, $scope) {

    var INITIAL_MONTHS_SHOWN = 15;
    var WEEKS_OFFSET = 2;

    $scope.calendar = []; // array of week object
    $scope.firstDay = new Date();
    $scope.firstDay.setDate(1); // beginning of the month
    $scope.firstDay = getSunday($scope.firstDay);
    $scope.lastDay = new Date($scope.firstDay.getTime());

    var calendarCellHeight = null;

    function getCalendarCellHeight() {
        if (calendarCellHeight === null) {
            var h = $(".week").outerHeight();
            if (h > 0) {
                // +4 from border-spacing, TODO refactor
                calendarCellHeight = h + 4;
            }
        }
        return calendarCellHeight;
    }

    $scope.loadPreviousMonth = function() {
        var isBeginningOfMonth = false;
        while (!isBeginningOfMonth) {
            $scope.firstDay.setDate($scope.firstDay.getDate() - 7);
            var currentWeek = createWeek($scope.firstDay);
            for (var dayCount = 0; dayCount < 7; dayCount++) {
                if ($scope.firstDay.getDate() == 1) {
                    isBeginningOfMonth = true;
                    currentWeek.firstOfTheMonth = true;
                    currentWeek.month = createMonthByFirstDay($scope.firstDay);
                }
                pushDayIntoWeek(currentWeek, $scope.firstDay);
                $scope.firstDay.setDate($scope.firstDay.getDate() + 1);
            }
            $scope.calendar.unshift(currentWeek);
            $scope.firstDay.setDate($scope.firstDay.getDate() - 7);
        }
        // $scope.calendar[0].month.nweeks is the number of weeks (lines) added, so
        // calendarCellHeight * it will give us the height
        return getCalendarCellHeight() * $scope.calendar[0].month.nweeks;
    }

    $scope.loadNextMonth = function() {
        var isBeginningOfMonth = false;
        if ($scope.calendar.length > 0) {
            lastWeek = $scope.calendar[$scope.calendar.length - 1];
            lastWeek.month.name = $scope.lastDay.getMonthName();
            lastWeek.month.year = $scope.lastDay.getFullYear();
        }
        while (!isBeginningOfMonth) {
            var currentWeek = createWeek($scope.lastDay);
            for (var dayCount = 0; dayCount < 7; dayCount++) {
                if ($scope.lastDay.getDate() == 1) {
                    isBeginningOfMonth = true;
                    currentWeek.firstOfTheMonth = true;
                    currentWeek.month = createMonthByFirstDay($scope.lastDay);
                }
                pushDayIntoWeek(currentWeek, $scope.lastDay);
                $scope.lastDay.setDate($scope.lastDay.getDate() + 1);
            }
            $scope.calendar.push(currentWeek);
        }
        $scope.calendar[$scope.calendar.length - 1].month.name = "";
    }

    $scope.scrollAt = 0;

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
            var calendar = $(".view-calendar-wrapper");
            var scroll = getCalendarCellHeight() * weeks;
            if (duration != 0) {
                calendar.animate({scrollTop: scroll}, duration);
            } else {
                calendar[0].scrollTop = scroll;
            }
        }
    }

    $scope.goToToday = function(duration) {
        $scope.goToDate(TODAY, duration);
    }

    $scope.showCalendar = true;

    $scope.initCalendar = function() {
        for (var i = 1; i <= INITIAL_MONTHS_SHOWN; i++) {
            if (i <= Math.ceil(INITIAL_MONTHS_SHOWN / 2.0)) {
                $scope.loadNextMonth();
            } else {
                $scope.loadPreviousMonth();
            }
        }

        // Wait some time so that getCalendarCellHeight() can return
        // something useful, since this function is used inside goToToday(...)
        $timeout(function() {
            $scope.goToToday(0);
            // BUG: A piece of the screen is cut
            //$scope.showCalendar = true;
        });
    }

    $scope.initCalendar();

    function getDay(date) {
        var w = getWeekIndex(date);
        var day = $scope.calendar[w].days[date.getDay()];
        if (!date.equalsDate(day.date)) {
            console.log("Error! CalendarController getDay() not working")
        }
        return day;
    }

    function getWeekIndex(date) {
        var d = dateDiffInDays(getSunday($scope.firstDay), date);
        return Math.floor(d / 7);
    }

    function dayRange(a, b) {
        a = a.date;
        b = b.date;
        var d = Date.min(a, b);
        var b = Date.max(a, b);
        var range = []
        while (d < b) {
            var day = getDay(d);
            range.push(day);
            d = d.tomorrow();
        }
        if (d.equalsDate(b)) {
            // Because Date holds a moment (timestamp) the last iteration relies on the time of each
            // date object, hence we make sure to include the last day here (remember d contains a
            // value which failed the loop condition)
            var day = getDay(d);
            range.push(day);
        }
        return range;
    }

    function selectDays(days) {
        _.each(days, function(day) {
            day.previousSelectedState = day.selected;
            day.selected = true;
            if (!_.contains(selectedDays, day)) {
                selectedDays.push(day);
            }
        });
    }

    function resetDays(days) {
        _.each(days, function(day) {
            day.selected = day.previousSelectedState;
            if (day.selected && !_.contains(selectedDays, day)) {
                selectedDays.push(day);
            } else if (!day.selected && _.contains(selectedDays, day)) {
                selectedDays.remove(day);
            }
        });
    }

    function unselectDays(days) {
        _.each(days, function(day) {
            day.previousSelectedState = day.selected;
            day.selected = false;
            selectedDays.remove(day);
        })
    }

    function clearSelectedDays() {
        _.each(selectedDays, function(day) {
            day.previousSelectedState = true;
            day.selected = false;
        });
        selectedDays.clear();
    }

    $scope.clearSelectedDays = function() {
        clearSelectedDays();
        twoStepStart = undefined;
    };

    function isSelected(day) {
        return _.contains(selectedDays, day);
    }

    function onSelectDays() {
        $scope.onSelectDaysParent(_.map(selectedDays, function(day) {
            return day.date;
        }));
    }

    function isCtrl(e) {
        return e.ctrlKey || e.metaKey; // metaKey is apple's cmd
    }

    var selectedDays = [];
    var twoStepStart = undefined;

    // State Machine regarding one mouse operation (down, move, up), not across operations
    var States = {
        IDLE: {},
        DOWN: {day: undefined, event: undefined},
        CLICKED: {event: undefined, day: undefined},
        DRAGGING: {start: undefined, current: undefined, previous: undefined},
        DRAGGED: {start: undefined, end: undefined}
    }
    var state = States.IDLE

    function onStateUpdate() {
        switch (state) {
            case States.DOWN:
                var e = state.event;
                if (!isCtrl(e)) {
                    clearSelectedDays();
                    selectDays([state.day]);
                }
                break;
            case States.CLICKED:
                //onDayClick(state.day, state.event);
                var e = state.event;
                var day = state.day;
                if (e.shiftKey) {
                    if (twoStepStart != undefined) {
                        var range = dayRange(twoStepStart, day);
                        selectDays(range);
                    } else {
                        selectDays([day]);
                        twoStepStart = day;
                    }
                } else {
                    if (isSelected(day) && isCtrl(e)) {
                        unselectDays([day]);
                        twoStepStart = undefined;
                    } else {
                        selectDays([day]);
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
                if (state.previous != undefined) {
                    var previousRange = dayRange(state.start, state.previous);
                    resetDays(previousRange);
                }
                var currentRange = dayRange(state.start, state.current);
                selectDays(currentRange);
                twoStepStart = undefined;
                break;
        }
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
                state.previous = undefined;
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
            onHoverDayParent: '=onHoverDay'
        },
        templateUrl: 'views/calendar.html',
        controller: 'CalendarController'
    };
});