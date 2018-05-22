package backend.controller;

import backend.entity.response.CommonResponse;
import backend.service.DeleteCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeleteCacheController {

    private DeleteCacheService deleteCacheService;

    @Autowired
    public DeleteCacheController (DeleteCacheService deleteCacheService) {
        this.deleteCacheService = deleteCacheService;
    }

    @RequestMapping("/delete_cache")
    @CrossOrigin
    public CommonResponse deleteCache(@RequestParam(value="url") String url) {
        return deleteCacheService.deleteAllCache(url);
    }

}