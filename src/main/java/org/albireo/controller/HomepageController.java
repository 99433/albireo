/*
 * Copyright Cboard
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.albireo.controller;

import java.util.HashMap;
import java.util.Map;

import org.albireo.services.BoardService;
import org.albireo.services.HomepageService;
import org.albireo.services.ServiceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by february on 2018/12/20.
 */
@RestController
@RequestMapping("/homepage")
public class HomepageController extends BaseController {
    
    @Autowired
    private HomepageService homepageService;
    
    @Autowired
    private BoardService boardService;
    
    @Value("${admin_user_id}")
    private String adminUserId;

    @RequestMapping(value = "/saveHomepage")
    public ServiceStatus saveHomepage(@RequestParam(name = "boardId", required = true) Long boardId) {
        String userId = tlUser.get().getUserId();
        return homepageService.saveHomepage(boardId, userId);
    }
    
    @RequestMapping(value = "/resetHomepage")
    public ServiceStatus resetHomepage() {
        String userId = tlUser.get().getUserId();
        return homepageService.resetHomepage(userId);
    }
    
    @RequestMapping(value = "/selectHomepage")
    public Long selectHomepage() {
        String userId = tlUser.get().getUserId();
        return homepageService.selectHomepage(userId);
    }
    
    @RequestMapping(value = "/mine", method = RequestMethod.GET)
    public Map<String, ?> loginPage() {           
        String userId = tlUser.get().getUserId();
        // 优先查找当前用户设置的首页
        Long boardId = homepageService.selectHomepage(userId);
        if(boardId == null) {
            // 如当前用户未设置首页，查找由管理员设置的系统首页
            boardId = homepageService.selectHomepage(adminUserId);
        }
        
        Map<String, Object> result = new HashMap<String, Object>();
        if(boardId == null) {
            // 如当前用户和管理员均未设置首页，将使用默认首页
            result.put("url", "");
            result.put("templateUrl", "org/cboard/view/cboard/homepage.html");
            result.put("controller", "homepageCtrl");
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("id", boardId);
            result.put("url", "");
            result.put("params", params);
            result.put("templateUrl", "org/cboard/view/dashboard/view.html");
            result.put("controller", "dashboardViewCtrl");
        }
        return result;
    }
}
