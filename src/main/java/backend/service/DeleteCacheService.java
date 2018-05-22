package backend.service;

import backend.entity.response.CommonResponse;
import main.ProjectManager;
import org.springframework.stereotype.Service;

@Service
public class DeleteCacheService {

    public CommonResponse deleteAllCache(String url) {
        ProjectManager pm = new ProjectManager(url);
        if (pm.isProjectCached()) {
            pm.deleteAllCache();
            return (new CommonResponse("API解析及项目仓库缓存清除成功"));
        }
        return (new CommonResponse("无需清除缓存(未找到API解析及项目仓库缓存)"));
    }

}