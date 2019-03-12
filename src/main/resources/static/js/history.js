logHtml = '<tr><th scope="row">{{timestamp_param}}</th><td>{{outlet_param}}</td><td>{{state_param}}</td></tr>'

function getHistory() {
    setLoading(true)
    $("#historyTable > tbody").html("");
    $.ajax({
        type: 'GET',
        url: 'outlets/history',
        dataType: 'json',
        success: handleGetHistoryResponse,
        error: handleError
    })
    $.ajax({
        type: 'GET',
        url: 'temps',
        dataType: 'json',
        success: handleTempsResponse,
        error: handleError
    })
}

function clearOldHistory() {
    setLoading(true)
    $.ajax({
        type: 'DELETE',
        url: 'outlets/history/old',
        success: getHistory,
        error: handleError
    })
}

function clearAllHistory() {
    setLoading(true)
    $.ajax({
        type: 'DELETE',
        url: 'outlets/history/all',
        success: getHistory,
        error: handleError
    })
}

function handleGetHistoryResponse(response) {
    setLoading(false)
    response.sort(function(a,b) { return b.time - a.time })
        .map(log => buildLogEntry(log))
}

function handleTempsResponse(response) {
    var tempData = response.map(record => record.temperature)
    var timeData = response.map(record => convertToDate(record.time))
    var ctx = $("#chart")
    var myChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: timeData,
            datasets: [{
                label: 'Temperature',
                data: tempData,
                borderWidth: 1
            }]
        },
        options: {}
    });
}

function convertToDate(millis) {
    options = {weekday:'short', day: 'numeric', hour: 'numeric', minute: 'numeric'}
    return new Date(millis).toLocaleString("en-US", options)
}

function buildLogEntry(log) {
    options = {weekday:'short', month: 'short', day: 'numeric', hour: 'numeric', minute: 'numeric'}
    date = new Date(log.time).toLocaleString("en-US", options)
    html = logHtml.replace("{{timestamp_param}}", date)
                .replace("{{outlet_param}}", log.outlet)
                .replace("{{state_param}}", log.state.toLowerCase())
    $('#historyTable > tbody:last').append(html)
}