/* Variables */
var stompClient = null;
var urlPrefix;
var sockJSUrl;
var subscribePath;
var sendPath;

/* Web Socket Disconnect */
function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
        stompClient = null;
    }
    console.log("Disconnected");
}

/* Web Socket Connect */
function connect(mission) {
    var socket = new SockJS(sockJSUrl);
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log("Connected - " + frame);
        showConversation("Server Connection: " + frame);
        stompClient.subscribe(subscribePath, messageReceiver);
        if (mission === "RequestAnalyseProject") {
            requestAnalyseProject();
        }
    });
}

/* Send Message in Web Socket (to Backend) */
function requestAnalyseProject() {
    $("#progress-bar").css("width", "0" );
    var projectName = $("#projectName").val();
    stompClient.send(sendPath, {}, JSON.stringify({'projectName': projectName}));
    console.log("Sent Project Name - " + projectName);
}

/* When Received Something from Web Socket (from Backend) */
function messageReceiver(ret) {
    var resp = JSON.parse(ret.body);
    if (resp.info === "BadResponse") {
        showConversation("[Error]" + resp.badInfo);
    } else if (resp.info === "ProgressResponse") {
        $("#information").children().eq(0).remove();
        showConversation("[Progress]" + resp.progressInfo);
        $("#progress-bar").css("width", (resp.progressNow * 100.0 / resp.progressTarget) + "%" );
    } else if (resp.info === "JsonResponse") {
        //TODO: 绘图
    } else {
        showConversation("[Info]" + resp.info);
    }
}

/* Update html */
function showConversation(message) {
    $("#information").prepend("<tr><td>" + message + "</td></tr>");
    console.log("Showed Conversation - " + message);
}

/* Clear html */
function clearConversation() {
    $("#information").html("");
    console.log("Cleared Conversation");
}

/* MAIN - After Html Loaded */
$(function () {
    //获取地址栏URL端口号后的内容
    var pathStr = window.location.pathname;
    console.log("window.location.pathname - " + pathStr);
    if (pathStr.length === 0) {
        pathStr = "/"
    }

    //找到非首字母的"/"符号，截取之前的子字符串（当通过打war包部署时，这个字符串是即war包的命名）
    //当长度为1时，第一次即不满足条件，不循环，pathStr.substr();执行后得到本身可能是"/"或者war包包名只有一个字符
    var i;
    for (i = 1; i < pathStr.length; i++) {
        if (pathStr[i] === "/") {
            break;
        }
    }
    pathStr = pathStr.substr(0, i);
    console.log("string from first gap - " + pathStr);

    //如果为一个"/"，则直接将pathStr省略为""
    //如果截取后的结尾名为".html"，说明首个分隔符即为"***.html"（这里默认".html"安全，即不会出现形如"***.html.war"的部署）
    //  说明直接运行到了8080端口，而非war包之中，则直接将pathStr省略为""
    if (pathStr === "/") {
        pathStr = "";
    } else if (pathStr.length >= 5 && pathStr.substr(pathStr.length - 5, 5) === ".html") {
        pathStr = "";
    }
    console.log("pathStr - " + pathStr);

    urlPrefix = window.location.protocol + "//" + window.location.host + pathStr;
    sockJSUrl = urlPrefix + "/biye-websocket";
    subscribePath = "/topic/analyse_repository";
    sendPath = "/app/analyse_repository";
    console.log("[url & path]\n" + sockJSUrl + "\n" + subscribePath + "\n" + sendPath);
    //去除form的自带的submit事件
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#analyseRepository" ).click(function() {
        clearConversation();
        disconnect();
        connect("RequestAnalyseProject");
    });
    $( "#reanalyse" ).click(function() {
        alert("TODO");
        // requestReanalyse();
    });
    $( "#deleteCache" ).click(function(){
        alert("TODO");
        // requestDeleteCache();
    });
});