$(document).ready(function () {
    $("#outletContainer").show()
    $("#triggerContainer").hide()
    $("#historyContainer").hide()
    getOutlets()
})

loading = [{"outlets": false}, {"schedule": false}, {"history": false}]

function showControl() {
    $("#outletContainer").show()
    $("#controlLink").addClass('active')
    $("#triggerContainer").hide()
    $("#scheduleLink").removeClass('active')
    $("#historyContainer").hide()
    $('#historyLink').removeClass('active')
    getOutlets()
    $("#hamburgerMenu").click()
}

function showSchedule() {
    $("#triggerContainer").show()
    $("#scheduleLink").addClass('active')
    $("#outletContainer").hide()
    $("#controlLink").removeClass('active')
    $("#historyContainer").hide()
    $('#historyLink').removeClass('active')
    getTriggers()
    $("#hamburgerMenu").click()
}

function showHistory() {
    $("#historyContainer").show()
    $("#historyLink").addClass('active')
    $("#outletContainer").hide()
    $("#controlLink").removeClass('active')
    $("#triggerContainer").hide()
    $('#scheduleLink').removeClass('active')
    getHistory()
    $("#hamburgerMenu").click()
}

function refresh() {
    getOutlets()
    getTriggers()
    getHistory()
}

function handleError(error) {
    setLoading(false)
    message = 'Error contacting AqPi: ' + error.state()
    $('#alert').html('<div class="alert alert-danger fade show"><a class="close" data-dismiss="alert">×</a><span>' + message + '</span></div>')
    setTimeout(function() {
        $(".alert").alert('close');
    }, 2000);
    $("button").prop('disabled', false)
}

function setLoading(setOn) {
    if (setOn) { $("#progressBar").show() }
    else { $("#progressBar").hide() }
}