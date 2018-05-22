package backend.service;

import backend.entity.response.CommonResponse;
import backend.entity.response.JsonResponse;
import backend.entity.response.ProgressResponse;
import main.APICountingManager;
import main.ProjectManager;
import net.sf.json.JSONObject;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import serializable.APICounter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class AnalyseRepositorySocketService {

    private final String DEST = "/topic/analyse_repository";

    public CommonResponse analyseRepository(SimpMessagingTemplate messagingTemplate, String url) {
        String tempMessage = "";
        CommonResponse commonResponse = new CommonResponse("项目API解析：分析完成，分析资料已缓存");

        /* 借助ProjectManager对象实现“项目仓库获取” */
        tempMessage = "初始化：ProjectManager初始化中";
        messagingTemplate.convertAndSend(DEST, new CommonResponse(tempMessage));
        System.out.println(tempMessage);
        ProjectManager pm = new ProjectManager(url);

        /* 优先读取并使用项目仓库缓存 */
        if (pm.isProjectCached()) {
            tempMessage = "项目仓库获取：已载入项目仓库与元数据缓存";
            messagingTemplate.convertAndSend(DEST, new CommonResponse(tempMessage));
            System.out.println(tempMessage);
        } else { //分支：没有缓存
            tempMessage = "项目仓库获取：未发现项目仓库与元数据缓存，开始获取项目仓库";
            messagingTemplate.convertAndSend(DEST, new CommonResponse(tempMessage));
            System.out.println(tempMessage);
            /* 下载项目到本地，并获取元数据，缓存数据 */
            pm.downloadProject();
            tempMessage = "项目仓库获取：获取项目仓库、元数据提取完成，数据已缓存";
            messagingTemplate.convertAndSend(DEST, new CommonResponse(tempMessage));
            System.out.println(tempMessage);
        }

        /* 借助APICountingManager对象实现“项目API解析” */
        tempMessage = "初始化：APICountingManager初始化中";
        messagingTemplate.convertAndSend(DEST, new CommonResponse(tempMessage));
        System.out.println(tempMessage);
        APICountingManager am = new APICountingManager(pm.getProjectCache());

        /* 优先读取并使用API解析缓存与进度 */
        if (am.getCountingNow() == 0) { //分支：没有缓存，需要从头分析
            tempMessage = "项目API解析：未发现缓存与进度信息，需要从头分析项目API";
            messagingTemplate.convertAndSend(DEST, new CommonResponse(tempMessage));
            System.out.println(tempMessage);
        } else {
            if (!am.hasNextCommit()) { //分支：有缓存，分析进度为100%
                tempMessage = "项目API解析：已载入缓存与进度信息，分析已完成";
                messagingTemplate.convertAndSend(DEST, new CommonResponse(tempMessage));
                System.out.println(tempMessage);
            } else { //分支：有缓存，分析进度在区间(0,100%)中取值，分析未完成
                tempMessage = "项目API解析：已载入缓存与进度信息，分析未完成，需要继续分析";
                messagingTemplate.convertAndSend(DEST, new CommonResponse(tempMessage));
                System.out.println(tempMessage);
            }
        }

        /* 依次返回已经分析过的结果数据，然后继续分析过程 */
        int targetProgress = am.getProject().getCommitList().size();
        int progressNow = 0;
        int cacheFlag = 2;
        tempMessage = "项目API解析：正在解析";
        messagingTemplate.convertAndSend(DEST, new CommonResponse(tempMessage));
        System.out.println(tempMessage);

        /* 有缓存时，首先将缓存的信息返回给前端 */
        if (am.getCountingNow() != 0) {
            List<APICounter> counterList = am.getCountingCache().getApiCounterList();
            for (int i = 0; i < counterList.size(); i++) {
                progressNow++;
                APICounter apiCounter = counterList.get(i);
                sendBackCounterJson(messagingTemplate, apiCounter);
            }
            tempMessage = "项目API解析(载入缓存)：总进度[" + progressNow + "/" + targetProgress + "]";
            messagingTemplate.convertAndSend(DEST, new ProgressResponse(progressNow, targetProgress, tempMessage));
            System.out.println(tempMessage);
            tempMessage = "项目API解析：正在解析";
            messagingTemplate.convertAndSend(DEST, new CommonResponse(tempMessage));
            System.out.println(tempMessage);
        }

        /* 进行分析，同时将新分析的数据返回给前端 */
        while (am.hasNextCommit()) {
            progressNow++;
            APICounter apiCounter = am.analyseNextCommit();
            sendBackCounterJson(messagingTemplate, apiCounter);
            tempMessage = "项目API解析(分析版本)：总进度[" + progressNow + "/" + targetProgress + "]";
            messagingTemplate.convertAndSend(DEST, new ProgressResponse(progressNow, targetProgress, tempMessage));
            System.out.println(tempMessage);
            if (cacheFlag  == am.getNewCountingCount()) {
                cacheFlag *= 2;
                try {
                    am.saveCountingCache();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //保存缓存
        try {
            am.saveCountingCache();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //返回其他信息
        tempMessage = "newCountingCount: " + am.getNewCountingCount() + "\n" +
                "checkoutFault: " + am.getCheckoutFault() + "\n" +
                "countAPIFault: " + am.getCountAPIFault();
        messagingTemplate.convertAndSend(DEST, new CommonResponse(tempMessage));
        System.out.println(tempMessage);

        return commonResponse;
    }

    private void sendBackCounterJson(SimpMessagingTemplate messagingTemplate, APICounter apiCounter) {
        JSONObject jsonObject = JSONObject.fromObject(apiCounter);
        messagingTemplate.convertAndSend(DEST, new JsonResponse(jsonObject.toString()));
    }

}
