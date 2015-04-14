'use strict';

App.controller("RemindUsersModalController", function($scope, $timeout, $modalInstance, $dialogs, data, extAppApi,
                                                      DateUtils, RemindUsersGroup) {
    // when the ext app does not allow the website to post in his behalf, only the summary is presented
    $scope.onlySummary = extAppApi.post === undefined;

    $scope.group = data.group;
    $scope.days = data.days;
    $scope.groupInput = {};

    var ua = window.navigator.userAgent;
    $scope.enableTags = ua.indexOf('MSIE ') == -1 && ua.indexOf('Trident/') == -1; // check if IE
    $scope.messageTooltip = "You can tag people by typing @ and their name";
    if (!$scope.enableTags) {
        $scope.messageTooltip = "Tagging is not supported for Internet Explorer";
    }

    if ($scope.onlySummary) {
        $scope.messageTooltip += "<br>This is only a summary as posting from an external app is not supported.";
    }

    $scope.tagAutocomplete = {
        show: false,
        top: 10,
        left: 20,
        el: undefined
    };

    $scope.extAppGroup = RemindUsersGroup.load($scope.group.id);
    $scope.groupChosen = $scope.extAppGroup != null;

    var employeeNameMap = {};

    $scope.remindUsersTextArea = {
        el: undefined,
        text: ""
    };

    function generateDefaultText() {
        if ($scope.days.length === 0) { // this is not supposed to happen
            console.log("Error: something went wrong. No days selected");
            $modalInstance.close();
        }

        var sortedDays = $scope.days.sort(function(a,b) {
            return a.date.getTime() - b.date.getTime();
        });

        var period = {
            startDay: sortedDays[0].date,
            endDay: sortedDays[0].date,
            days: [sortedDays[0]]
        };

        function undefinedOrEmpty(a) {
            return a == null || a.length === 0;
        }

        function generatePeriodSummary(period) {
            var summary = "";

            if (_.every($scope.group.assignmentTypes, function(assignmentType) {
                if (period.days[0].content == null || period.days[0].content.assignments == null) { return true; }
                return undefinedOrEmpty(period.days[0].content.assignments[assignmentType.id]);
            })) {
                return summary;
            }
            if (!DateUtils.equalsDate(period.startDay, period.endDay)) {
                summary += period.startDay.toLocaleString().split(" ")[0] +
                           " - " + period.endDay.toLocaleString().split(" ")[0] + "\n";
            } else {
                summary += period.startDay.toLocaleString().split(" ")[0] + "\n";
            }
            $scope.group.assignmentTypes.forEach(function(assignmentType) {
                var assignments = period.days[0].content.assignments[assignmentType.id];
                if (undefinedOrEmpty(assignments)) {
                    return;
                }
                summary += "- " + assignmentType.name + ":";
                assignments.sort();
                assignments.forEach(function(employee) {
                    summary+=  " <b>" + employee.name + "</b>\u200C,"; // &zwnj; marks the end of the string
                    employeeNameMap[employee.name] = {
                        id: employee.extAppId,
                        full_name: employee.name
                    };
                });
                summary = summary.slice(0, -1); // remove last comma
                summary += "\n";
            });

            summary += "\n";
            return summary;
        }

        function hasSameAssignments(day1, day2) {
            return _.every($scope.group.assignmentTypes, function(assignmentType) {
                if (day1.content == null) {
                    day1.content = {
                        assignments: []
                    };
                }
                if (day2.content == null) {
                    day2.content = {
                        assignments: []
                    };
                }

                if(undefinedOrEmpty(day1.content.assignments[assignmentType.id]) &&
                   undefinedOrEmpty(day2.content.assignments[assignmentType.id])){
                    return true;
                }
                return _.isEqual(day1.content.assignments[assignmentType.id], day2.content.assignments[assignmentType.id]);
            });
        }

        $scope.remindUsersTextArea.text += $scope.group.name + ":\n\n";

        for (var i = 1; i < sortedDays.length; i++) {
            if (DateUtils.differenceInDays(period.endDay, sortedDays[i].date) <= 1 &&
                hasSameAssignments(period.days[0], sortedDays[i])) {
                period.endDay = sortedDays[i].date;
                period.days.push(sortedDays[i]);
            }
            else {
                $scope.remindUsersTextArea.text += generatePeriodSummary(period);
                period = {
                    startDay: sortedDays[i].date,
                    endDay: sortedDays[i].date,
                    days: [sortedDays[i]]
                };
            }
        }
        $scope.remindUsersTextArea.text += generatePeriodSummary(period);

    }

    var regexTag = /<b\>((?:[^\n](?:(?!<\/b\>)))+[^n])<\/b\>/g; // <b>Name</b>
    var regexInvalid = /<b\>((?:[^\n](?:(?!<\/b\>)))+[^n])<\/b\>(?=([^\u200C]|$))/g; // <b>Name</b> \u200C is the unicode to &zwnj;

    function checkNameTags() {
        var text = $scope.remindUsersTextArea.text;
        text = text.replace(regexTag, function(match0, match1) {
            if (employeeNameMap[match1] == null) {
                return match1;
            }
            return match0;
        });

        text = text.replace(regexInvalid, function(match0, match1) {
            return match1;
        });
        $scope.remindUsersTextArea.text = text;
    }

    var styleRegex = /\sstyle\=\"[^\n\"]+\;\"/g;
    function removeStyle() {
        var text = $scope.remindUsersTextArea.text;
        text = text.replace(styleRegex, "");
        if ($scope.remindUsersTextArea.text != text) {
            $scope.remindUsersTextArea.text = text;
            return true;
        }
        return false;
    }

    var htmlTagRegex = /<\/?((span)|(i)|(strong))(\s)*\>/g;
    function removeHtmlTags(text) {
        return text.replace(htmlTagRegex, "");
    }

    $timeout(generateDefaultText);

    $scope.onKeyDownGroupName = function(){
        $scope.extAppGroup = undefined;
        $scope.groupChosen = false;
    };

    $scope.onSelectAutocompleteGroupName = function(group){
        $scope.extAppGroup = group;
    };

    $scope.onEnterGroupName = function() {
        if($scope.extAppGroup != null) {
            $scope.groupChosen = true;
        }
        $scope.remindUsersTextArea.el.focus();
    };

    var savedCursor;
    var savedScroll;
    function saveCursor()
    {
        savedScroll = $scope.remindUsersTextArea.el.scrollTop();
        savedCursor = window.getSelection().getRangeAt(0);
    }

    function restoreCursor()
    {
        $scope.remindUsersTextArea.el.focus();
        if (savedCursor != null) {
            if (window.getSelection)
            {
                var s = window.getSelection();
                if (s.rangeCount > 0) {
                    s.removeAllRanges();
                }
                s.addRange(savedCursor);
            }
            else if (document.createRange())
            {
                window.getSelection().addRange(savedCursor);
            }
            $scope.remindUsersTextArea.el.scrollTop(savedScroll);
        }

    }

    $scope.onKeyPress = function(e) {
        if (!$scope.enableTags) {
            return;
        }
        $timeout(function() {
            if (e.keyCode == 64 /* @ */ || e.key == "@") {
                saveCursor();
                var clientRect = window.getSelection().getRangeAt(0).getClientRects()[0]; // get cursor position
                $scope.tagAutocomplete.show = true;
                $scope.tagAutocomplete.top = clientRect.top - 1; // -1 for empiric adjust
                $scope.tagAutocomplete.left = clientRect.left;
                $timeout(function() {
                    $scope.tagAutocomplete.el.find("input").focus();
                }, 10); // timeout to allow angularjs to show el before focusing
            }
        }, 10);
    };

    $scope.onKeyUp = function() {
            /**
             *  Hack: as content editable divs don't have angularjs two way binding support via ngModel, I had to write
             *  a custom ngModel, which only works on keyup events. So we need this timeout to allow the binding to work
             */
            $timeout(function(){
                if(removeStyle()) { // user pasted something
                    document.execCommand('insertHTML', false, '\u200C'); // isolate pasted content
                }
                $scope.remindUsersTextArea.text = removeHtmlTags($scope.remindUsersTextArea.text);
                checkNameTags();
            }, 10);
        };

    var backcount = 0;
    $scope.onKeyUpTag = function(e) {
        if (e.keyCode == 8 && $scope.tagAutocomplete.el.find("input").val() === "") { // backspace
            backcount++;
            if (backcount == 2) { // exit tag mode with 2 backspaces
                restoreCursor();
                document.execCommand('delete');
                $scope.clearTagInput();
            }
        } else {
            backcount = 0;
        }
    };

    $scope.onSelectAutocompleteTag = function(user){
        $scope.taggedUser = user;
    };

    $scope.clearTagInput = function() {
        $scope.tagAutocomplete.el.find("input").val("");
        $scope.tagAutocomplete.show = false;
    };

    $scope.onEnterTag = function() {
        restoreCursor();
        $scope.clearTagInput();
        document.execCommand('delete'); // delete @
        if ($scope.taggedUser) {
            employeeNameMap[$scope.taggedUser.full_name] = {
                id: $scope.taggedUser.id,
                full_name: $scope.taggedUser.full_name
            };
            var tag = "<b>" + $scope.taggedUser.full_name + "</b>\u200C";
            document.execCommand('insertHTML', false, tag); // insert tag
        }
    };

    $scope.ok = function(){
        if ($scope.onlySummary) {
            $modalInstance.close();
            return;
        }
        if ($scope.extAppGroup == null) {
            $scope.groupInput.shake();
            return;
        }
        RemindUsersGroup.save($scope.group.id, $scope.extAppGroup);
        checkNameTags();
        removeStyle();
        var tagList = [];
        var i = 0;
        var message = $scope.remindUsersTextArea.text.replace(regexTag, function(match, name) {
            tagList[i] = employeeNameMap[name];
            var str = "{" + i + "}";
            i++;
            return str;
        }).replace(/\u200C/g, "");

        message = removeHtmlTags(message);

        extAppApi.post($scope.extAppGroup.id, message, tagList, function(){
            $modalInstance.close();
        });
    };

    $scope.cancel = function() {
        $modalInstance.close();
    };

    /* The contenteditable div create child divs every time the user presses enter... this prevents this behaviour */
    function preventContentEditableDivs() {
        $scope.remindUsersTextArea.el.keydown(function(e) {
            if (e.keyCode === 13) {
                document.execCommand('insertHTML', false, '<br><br>');
                return false;
            }
        });
    }

    $timeout(preventContentEditableDivs);

});

