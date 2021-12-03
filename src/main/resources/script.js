let webSocket = null

const createLog =
    (doc, logType, fetchApi) => {
            const button = doc.createElement("button")
            button.innerText = logType.toUpperCase()
            button.className = `${logType} log-button`

            button.addEventListener("click", () => {
                fetchApi(`/log/${logType}`, { method: "POST" })
                    .then(response => response.json())
                    .then(({result}) => insertToConsole(doc, result))
            })

            const requestContainer = doc.getElementById("requests-container")

            requestContainer.appendChild(button)
        }

const loadLogRequestElements = (document, fetchApi) => {
    [ "info", "error", "debug", "trace", "warn" ]
        .forEach(logType => createLog(document, logType, fetchApi))
}

const initializeWebSocketSwitch = doc => {
    const webSocketSwitch = doc.getElementById("web-socket-switch")
    const on = doc.getElementById("web-socket-switch-on")
    const off = doc.getElementById("web-socket-switch-off")

    webSocketSwitch.addEventListener("click", () => {
        if (webSocket === null) {
            on.className = "visible switch"
            off.className =  "not-visible"
            webSocketConnect(doc)
        } else {
            on.className = "not-visible"
            off.className = "visible switch"
            webSocketClose(doc)
        }
    })
}

const loadElements = (document, fetchApi) => {
    loadLogRequestElements(document, fetchApi)
    initializeWebSocketSwitch(document)
}

const insertToConsole = (doc, value) => {
    const div = doc.createElement("div")
    div.className = "console-row"
    const date = new Date()

    const timestampSpan = doc.createElement("span")
    timestampSpan.className = "timestamp"
    timestampSpan.innerText = date.toLocaleString()

    const valueSpan = doc.createElement("span")
    value.className = "console-data"
    valueSpan.innerText = value

    div.appendChild(timestampSpan)
    div.appendChild(valueSpan)

    const console = doc.getElementById("console")
    console.appendChild(div)
}

const webSocketConnect = doc => {
    const protocol = doc.location.protocol.startsWith("https") ? "wss" : "ws"

    webSocket = new WebSocket(`${protocol}://${doc.location.host}/log/ws`)

    webSocket.addEventListener("message", ({data}) => insertToConsole(doc, data))
}

const webSocketClose = doc => {
    webSocket.close(1000, "User closed connection")
    webSocket = null

    insertToConsole(doc, "WebSocket connection is closed")
}