triggerHtml = '<div class="card mt-3"><div class="card-body"><h5 class="card-title">{{outlet_param}} {{state_param}}</h5><p class="card-text"><div class="container-fluid d-flex flex-row pl-0 pr-0"><div class="col d-flex justify-content-start pl-0"><div class="d-flex flex-column"><div><strong>Next Time:</strong></div><div>{{next_time_param}}</div></div></div><div class="col d-flex justify-content-end pr-0"><button id="{{trigger_name}}Button" class="btn btn-sm btn-outline-danger" onclick="deleteTrigger(this)"><img src="assets/x.svg"> Delete</button></div></div></p></div></div>'

triggers = []

function getTriggers() {
    setLoading(true)
    clearTriggers()
    $.ajax({
        type: 'GET',
        url: 'triggers',
        dataType: 'json',
        success: handleGetTriggersResponse,
        error: handleError
    })
}

function addTrigger() {
    if (isValidTime($("#timeInput").val())) {
        setLoading(true)
        outletName = $("#outletSelect").val()
        outletState = $("#stateSelect").val().toUpperCase()
        cronExpression = buildCronExpression($("#timeInput").val())
        $.ajax({
            type: 'POST',
            url: 'triggers',
            headers: { "Content-Type": "application/json" },
            data: JSON.stringify({ "outletName": outletName, "state": outletState, "cronExpression": cronExpression }),
            success: function () {
                $("#addCollapseButton").click()
                getTriggers()
            },
            error: handleError
        })
    } else {
        error = new Error;
        error.state = function () { return "Time must be of the format HH:MM" }
        handleError(error)
    }
}

function deleteTrigger(button) {
    setLoading(true)
    trigger = getTriggerFromButton(button)
    uri = 'triggers/' + trigger.name
    $.ajax({
        type: 'DELETE',
        url: uri,
        success: getTriggers,
        error: handleError
    })
}

function handleGetTriggersResponse(response) {
    setLoading(false)
    response.sort(function (a, b) { return a.nextFireTime - b.nextFireTime })
        .map(triggerResponse => buildTriggerCard(triggerResponse))
}

function buildTriggerCard(response) {
    options = { weekday: 'short', hour: 'numeric', minute: 'numeric' }
    nextTime = new Date(response.nextFireTime).toLocaleString("en-US", options)
    trigger = {
        "outlet": response.outlet.outletName, "state": response.outlet.state,
        "nextTime": nextTime, "name": response.triggerName,
        "expression": response.cronExpression
    }
    triggers.push(trigger)
    html = triggerHtml.replace("{{outlet_param}}", trigger.outlet)
        .replace("{{state_param}}", trigger.state)
        .replace("{{next_time_param}}", trigger.nextTime)
        .replace("{{trigger_name}}", trigger.name)
    $('#triggerList').append(html)
}

function getTriggerFromButton(caller) {
    triggerName = caller.id.substring(0, caller.id.indexOf('Button'))
    return triggers.filter(trigger => trigger.name === triggerName)[0]
}

function isValidTime(time) {
    if (time.indexOf(":") === -1) { return false }
    if (time.split(":").length > 2) { return false }
    hour = parseInt(time.split(":")[0])
    if (hour < 0 || hour > 23) { return false }
    minute = parseInt(time.split(":")[1])
    if (minute < 0 || minute > 59) { return false }
    if (time.split(":")[1].length !== 2) { return false }
    if (minute < 10 && time.split(":")[1].split("")[0] !== "0") { return false }
    return true
}

function buildCronExpression(time) {
    hour = parseInt(time.split(":")[0])
    minute = parseInt(time.split(":")[1])
    daysOfTheWeek = buildDaysOfTheWeek()
    return "0 " + minute + " " + hour + " ? * " + daysOfTheWeek
}

function buildDaysOfTheWeek() {
    if ($("#weekendCheck").prop('checked')) { return "SAT-SUN" }
    if ($("#weekdayCheck").prop('checked')) { return "MON-FRI" }
    if (allCheckBoxesAre(true) || allCheckBoxesAre(false)) { return "MON-SUN"}
    days = []
    if ($("#monCheck").prop('checked')) { days.push("MON") }
    if ($("#tueCheck").prop('checked')) { days.push("TUE") }
    if ($("#wedCheck").prop('checked')) { days.push("WED") }
    if ($("#thuCheck").prop('checked')) { days.push("THU") }
    if ($("#friCheck").prop('checked')) { days.push("FRI") }
    if ($("#satCheck").prop('checked')) { days.push("SAT") }
    if ($("#sunCheck").prop('checked')) { days.push("SUN") }
    return days.join()
}

function setCheckBoxes(name, checkbox) {
    if (checkbox.id === "weekendCheck") {
        setWeekends()
    } else {
        setWeekdays()
    }
}

function clearTriggers() {
    $("#triggerList").html("")
    triggers = []
}

function setWeekends() {
    if ($("#weekendCheck").prop('checked')) {
        disableWeekCheckBoxes()
        $("#monCheck").prop('checked', false);
        $("#tueCheck").prop('checked', false);
        $("#wedCheck").prop('checked', false);
        $("#thuCheck").prop('checked', false);
        $("#friCheck").prop('checked', false);
        $("#satCheck").prop('checked', true);
        $("#sunCheck").prop('checked', true);
        $("#weekdayCheck").prop('checked', false);
    } else {
        enableWeekCheckBoxes()        
    }
}

function setWeekdays() {
    if ($("#weekdayCheck").prop('checked')) {
        disableWeekCheckBoxes()
        $("#monCheck").prop('checked', true);
        $("#tueCheck").prop('checked', true);
        $("#wedCheck").prop('checked', true);
        $("#thuCheck").prop('checked', true);
        $("#friCheck").prop('checked', true);
        $("#satCheck").prop('checked', false);
        $("#sunCheck").prop('checked', false);
        $("#weekendCheck").prop('checked', false);
    } else {
        enableWeekCheckBoxes()        
    }
}

function enableWeekCheckBoxes() {
    $("#monCheck").prop('disabled', false)
    $("#tueCheck").prop('disabled', false)
    $("#wedCheck").prop('disabled', false)
    $("#thuCheck").prop('disabled', false)
    $("#friCheck").prop('disabled', false)
    $("#satCheck").prop('disabled', false)
    $("#sunCheck").prop('disabled', false)
}

function disableWeekCheckBoxes() {
    $("#monCheck").prop('disabled', true)
    $("#tueCheck").prop('disabled', true)
    $("#wedCheck").prop('disabled', true)
    $("#thuCheck").prop('disabled', true)
    $("#friCheck").prop('disabled', true)
    $("#satCheck").prop('disabled', true)
    $("#sunCheck").prop('disabled', true)
}

function allCheckBoxesAre(value) {
    if ($("#monCheck").prop('checked') != value) { return false }
    if ($("#tueCheck").prop('checked') != value) { return false }
    if ($("#wedCheck").prop('checked') != value) { return false }
    if ($("#thuCheck").prop('checked') != value) { return false }
    if ($("#friCheck").prop('checked') != value) { return false }
    if ($("#satCheck").prop('checked') != value) { return false }
    if ($("#sunCheck").prop('checked') != value) { return false }
    return true;
}