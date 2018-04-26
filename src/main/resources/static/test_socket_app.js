var stompClient = null;
var urlPrefix;
var sockJSUrl;
var greetingsSubscribePath;
var sendPath;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    var socket = new SockJS(sockJSUrl);
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe(greetingsSubscribePath, function (greeting) {
            showGreeting(JSON.parse(greeting.body).info);
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    stompClient.send(sendPath, {}, JSON.stringify({'projectName': $("#name").val()}));
}

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    var pathStr = window.location.pathname;
    var i;
    //找到非首字母的"/"符号，截取之前的子字符串(即打包为war时的war包命名)
    for (i = 1; i < pathStr.length; i++) {
        if (pathStr[i] === "/") {
            break;
        }
    }
    pathStr = pathStr.substr(0, i);
    //如果长度为1，说明为一个"/"，则直接将pathStr省略为""
    //如果截取后的结尾名为".html"，说明直接运行到了8080端口，而非war包之中，则直接将pathStr省略为""
    if (pathStr.length === 1) {
        pathStr = "";
    } else if (pathStr.length >= 5 && pathStr.substr(pathStr.length - 5, 5) === ".html") {
        pathStr = "";
    }
    urlPrefix = window.location.protocol + "//" + window.location.host + pathStr;
    sockJSUrl = urlPrefix + "/biye-websocket";
    greetingsSubscribePath = "/topic/test_socket";
    sendPath = "/app/test_socket";
    console.log("[url & path]\n" + sockJSUrl + "\n" + greetingsSubscribePath + "\n" + sendPath);
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendName(); });
});