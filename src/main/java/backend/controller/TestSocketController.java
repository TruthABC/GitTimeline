package backend.controller;

import backend.entity.message.ProjectMessage;
import backend.entity.response.CommonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class TestSocketController {

    /* 消息发送到前端的工具 */
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public TestSocketController(SimpMessagingTemplate simpMessagingTemplate) {
        this.messagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/test_socket")//前端送信地址（注意前缀，常见前缀"/app"）
    @SendTo("/topic/test_socket")//（有返回值和return时，映射此地址）前端订阅地址，后端消息由此地址送出
    public void greeting(ProjectMessage message) throws Exception {
        for (int i = 5; i > 0 ; i--) {
            Thread.sleep(1000);
            messagingTemplate.convertAndSend("/topic/test_socket",new CommonResponse("["+ i +"] " + message.getProjectName()));
        }
    }

}
