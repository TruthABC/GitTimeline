package backend.controller;

import backend.entity.message.ProjectMessage;
import backend.entity.response.CommonResponse;
import backend.service.AnalyseRepositorySocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class AnalyseRepositorySocketController {

    /* 消息发送到前端的工具 */
    private SimpMessagingTemplate messagingTemplate;
    private AnalyseRepositorySocketService analyseRepositorySocketService;

    @Autowired
    public AnalyseRepositorySocketController(SimpMessagingTemplate simpMessagingTemplate, AnalyseRepositorySocketService analyseRepositorySocketService) {
        this.messagingTemplate = simpMessagingTemplate;
        this.analyseRepositorySocketService = analyseRepositorySocketService;
    }

    @MessageMapping("/analyse_repository")//前端送信地址（注意前缀，常见前缀"/app"）
    @SendTo("/topic/analyse_repository")//（有返回值和return时，映射此地址）前端订阅地址，后端消息由此地址送出
    public CommonResponse AnalyseRepository(ProjectMessage message) throws Exception {
        CommonResponse response = new CommonResponse();

        try {
            CommonResponse commonResponse = analyseRepositorySocketService.analyseRepository(messagingTemplate, message.getProjectName());
            if (commonResponse != null) {
                response = commonResponse;
            } else {
                response.setInfo("执行分析时出错");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            response.setInfo("执行分析时出错");
        }

        return response;
    }

}
