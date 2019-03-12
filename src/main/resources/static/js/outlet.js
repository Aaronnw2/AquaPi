cardHtml = '<div class="card mt-3"><div class="card-body"><h5 class="card-title">{{outlet_title}}</h5><p class="card-text"><div class="container-fluid"><div class="row" id="{{outlet_id}}Card"><div id="{{outlet_id}}StateText">State: {{outlet_state}}</div><div class="col"><div class="d-flex justify-content-end"><button id="{{outlet_id}}Button" type="button" class="btn {{button_class}}" onclick="setOutletToState(this)">{{button_text}}</button></div></div></div></div></p></div></div>'

outlets = []

function getOutlets() {
    setLoading(true)
    clearOutlets()
    $.ajax({
        type: 'GET',
        url: 'outlets',
        dataType: 'json',
        success: handleGetOutletsResponse,
        error: handleError
    })
}

function handleGetOutletsResponse(response) {
    setLoading(false)
    $("button").prop('disabled', false)
    response.map(outletResponse => buildOutletCard(outletResponse))
}

function buildOutletCard(outletResponse) {
    outletId = outletResponse.outletName.replace(" ", "").toLowerCase()
    if (outletResponse.state.toLowerCase() === "on") {
        buttonText = "Turn off"
        buttonClass = "btn-danger"
    }
    else {
        buttonText = "Turn on"
        buttonClass = "btn-success"
    }
    outletHtml = cardHtml.replace("{{outlet_title}}", outletResponse.outletName)
                    .replace(/{{outlet_id}}/g, outletId)
                    .replace(/{{outlet_state}}/g, outletResponse.state.toLowerCase())
                    .replace(/{{button_text}}/g, buttonText)
                    .replace(/{{button_class}}/g, buttonClass)
    $('#outletContainer').append(outletHtml)
    outlets.push({'outletName': outletResponse.outletName, 'cardName': outletId, 'state': outletResponse.state.toLowerCase()})
}

function clearOutlets() {
    $("#outletContainer").html("")
    outlets = []
}

function setOutletToState(caller) {
    setLoading(true)
    caller.disabled = true;
    outlet = getOutletNameFromButton(caller)
    state = getNewStateFromCurrentState(outlet)
    outletUrl = 'outlets/' + outlet.outletName + '/' + state
    $.ajax({
        type: 'PUT',
        url: outletUrl,
        success: handleSetOutletsResponse(outlet),
        error: handleError
    })
}

function getOutletNameFromButton(caller) {
    outletName = caller.id.substring(0, caller.id.indexOf('Button'))
    return outlets.filter(outlet => outlet.cardName === outletName)[0]
}

function handleSetOutletsResponse(outlet) {
    setLoading(false)
    button = getButtonFromOutlet(outlet)
    button.prop('disabled', false)
    if (outlet.state === 'on') {
        outlet.state = 'off'
        button.removeClass('btn-danger')
        button.addClass('btn-success')
        button.text('Turn on')
    } else {
        outlet.state = 'on'
        button.removeClass('btn-success')
        button.addClass('btn-danger')
        button.text('Turn off')
    }
    $("#" + outlet.cardName + "StateText").text('State: ' + outlet.state)
}

function setOutletInformation(outlet, response) {
    outlet.state = response.state.toLowerCase()
    button = getButtonFromOutlet(outlet)
    if (outlet.state === 'on') {
        button.removeClass('btn-success')
        button.addClass('btn-danger')
        button.text('Turn off')
    } else {
        button.removeClass('btn-danger')
        button.addClass('btn-success')
        button.text('Turn on')
    }
}

function getButtonFromOutlet(outlet) {
    buttonName = '#' + outlet.cardName + 'Button'
    return $(buttonName)
}

function getOutletFromResponse(outletResponse) {
    return outlets.filter(outlet => outlet.outletName === outletResponse.outletName)[0]
}

function getNewStateFromCurrentState(outlet) {
    if (outlet.state === 'on') { return 'off'}
    else { return 'on' }
}