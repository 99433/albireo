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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.albireo.dao.*;
import org.albireo.dto.*;
import org.albireo.pojo.*;
import org.albireo.services.AdminSerivce;
import org.albireo.services.DatasourceService;
import org.albireo.services.MenuService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by yfyuan on 2016/12/2.
 */
@RestController
@RequestMapping("/admin")
public class AdminController extends BaseController {

    private final AdminSerivce adminSerivce;

    private final UserDao userDao;

    private final RoleDao roleDao;

    @Value("${admin_user_id}")
    private String adminUserId;

    private final MenuDao menuDao;

    private final BoardDao boardDao;
    private final DatasetDao datasetDao;
    private final DatasourceDao datasourceDao;
    private final JobDao jobDao;
    private final WidgetDao widgetDao;
    private final MenuService menuService;
    private final DatasourceService datasourceService;

    public AdminController(AdminSerivce adminSerivce, UserDao userDao, RoleDao roleDao, MenuDao menuDao, BoardDao boardDao, DatasetDao datasetDao, DatasourceDao datasourceDao, JobDao jobDao, WidgetDao widgetDao, MenuService menuService, DatasourceService datasourceService) {
        this.adminSerivce = adminSerivce;
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.menuDao = menuDao;
        this.boardDao = boardDao;
        this.datasetDao = datasetDao;
        this.datasourceDao = datasourceDao;
        this.jobDao = jobDao;
        this.widgetDao = widgetDao;
        this.menuService = menuService;
        this.datasourceService = datasourceService;
    }


    @RequestMapping(value = "/saveNewUser")
    public String saveNewUser(@RequestParam(name = "user") String user) {
        JSONObject jsonObject = JSONObject.parseObject(user);
        return adminSerivce.addUser(UUID.randomUUID().toString(), jsonObject.getString("loginName"), jsonObject.getString("userName"), jsonObject.getString("userPassword"));
    }

    @RequestMapping(value = "/updateUser")
    public String updateUser(@RequestParam(name = "user") String user) {
        JSONObject jsonObject = JSONObject.parseObject(user);
        return adminSerivce.updateUser(jsonObject.getString("userId"), jsonObject.getString("loginName"), jsonObject.getString("userName"), jsonObject.getString("userPassword"));
    }

    @RequestMapping(value = "/deleteUser")
    public String deleteUser(@RequestParam(name = "userId") String userId) {
        return adminSerivce.deleteUser(userId);
    }

    @RequestMapping(value = "/getUserList")
    public List<DashboardUser> getUserList() {
        List<DashboardUser> list = userDao.getUserList();
        return list;
    }

    @RequestMapping(value = "/saveRole")
    public String saveRole(@RequestParam(name = "role") String role) {
        JSONObject jsonObject = JSONObject.parseObject(role);
        return adminSerivce.addRole(UUID.randomUUID().toString(), jsonObject.getString("roleName"), jsonObject.getString("userId"));
    }

    @RequestMapping(value = "/updateRole")
    public String updateRole(@RequestParam(name = "role") String role) {
        JSONObject jsonObject = JSONObject.parseObject(role);
        return adminSerivce.updateRole(jsonObject.getString("roleId"), jsonObject.getString("roleName"), jsonObject.getString("userId"));
    }

    @RequestMapping(value = "/deleteRole")
    public String deleteRole(@RequestParam(name = "roleId") String roleId) {
        return adminSerivce.deleteRole(roleId);
    }

    @RequestMapping(value = "/getRoleList")
    public List<DashboardRole> getRoleList() {
        List<DashboardRole> list = roleDao.getRoleList(tlUser.get().getUserId());
//        List<DashboardRole> list = roleDao.getRoleList("1");
        return list;
    }

    @RequestMapping(value = "/getRoleListAll")
    public List<DashboardRole> getRoleListAll() {
        List<DashboardRole> list = roleDao.getRoleListAll();
        return list;
    }

    @RequestMapping(value = "/updateUserRole")
    public String updateUserRole(@RequestParam(name = "userIdArr") String userIdArr, @RequestParam(name = "roleIdArr") String roleIdArr) {
        return adminSerivce.updateUserRole(
                JSONArray.parseArray(userIdArr).toArray(new String[]{}),
                JSONArray.parseArray(roleIdArr).toArray(new String[]{}),
                tlUser.get().getUserId()
        );
    }

    @RequestMapping(value = "/deleteUserRole")
    public String deleteUserRole(@RequestParam(name = "userIdArr") String userIdArr, @RequestParam(name = "roleIdArr") String roleIdArr) {
        return adminSerivce.deleteUserRoles(
                JSONArray.parseArray(userIdArr).toArray(new String[]{}),
                JSONArray.parseArray(roleIdArr).toArray(new String[]{}),
                tlUser.get().getUserId()
        );
    }

    @RequestMapping(value = "/getUserRoleList")
    public List<DashboardUserRole> getUserRoleList() {
        List<DashboardUserRole> list = userDao.getUserRoleList();
        return list;
    }

    @RequestMapping(value = "/getRoleResList")
    public List<ViewDashboardRoleRes> getRoleResList() {
        List<DashboardRoleRes> list = roleDao.getRoleResList();
        return list.stream().map(ViewDashboardRoleRes::new).collect(Collectors.toList());
    }

    @RequestMapping(value = "/updateRoleRes")
    public String updateRoleRes(@RequestParam(name = "roleIdArr") String roleIdArr, @RequestParam(name = "resIdArr") String resIdArr) {
        return adminSerivce.updateRoleRes(JSONArray.parseArray(roleIdArr).toArray(new String[]{}), resIdArr);
    }

    @RequestMapping(value = "/updateRoleResUser")
    public String updateRoleResUser(@RequestParam(name = "roleIdArr") String roleIdArr, @RequestParam(name = "resIdArr") String resIdArr) {
        return adminSerivce.updateRoleResUser(JSONArray.parseArray(roleIdArr).toArray(new String[]{}), resIdArr);
    }

    @RequestMapping(value = "/isAdmin")
    public boolean isAdmin() {
        return adminUserId.equals(tlUser.get().getUserId());
    }

    @RequestMapping(value = "/isConfig")
    public boolean isConfig(@RequestParam(name = "type") String type) {
        if (tlUser.get().getUserId().equals(adminUserId)) {
            return true;
        } else if (type.equals("widget")) {
            List<Long> menuIdList = menuDao.getMenuIdByUserRole(tlUser.get().getUserId());
            if (menuIdList.contains(1L) && menuIdList.contains(4L)) {
                return true;
            }
        }
        return false;
    }

    @RequestMapping(value = "/getDatasetList")
    public List<ViewDashboardDataset> getDatasetList() {
        List<DashboardDataset> list = datasetDao.getDatasetListAdmin(tlUser.get().getUserId());
        return Lists.transform(list, ViewDashboardDataset.TO);
    }

    @RequestMapping(value = "/getDatasetListUser")
    public List<ViewDashboardDataset> getDatasetListUser() {
        List<DashboardDataset> list = adminSerivce.getDatasetList(tlUser.get().getUserId());
        return Lists.transform(list, ViewDashboardDataset.TO);
    }

    @RequestMapping(value = "/getJobList")
    public List<ViewDashboardJob> getJobList() {
        return jobDao.getJobListAdmin(tlUser.get().getUserId()).stream().map(ViewDashboardJob::new).collect(Collectors.toList());
    }

    @RequestMapping(value = "/getBoardList")
    public List<ViewDashboardBoard> getBoardList() {
        List<DashboardBoard> list = boardDao.getBoardListAdmin(tlUser.get().getUserId());
        return Lists.transform(list, ViewDashboardBoard.TO);
    }

    @RequestMapping(value = "/getBoardListUser")
    public List<ViewDashboardBoard> getBoardListUser() {
        List<DashboardBoard> list = adminSerivce.getBoardList(tlUser.get().getUserId());
        return Lists.transform(list, ViewDashboardBoard.TO);
    }

    @RequestMapping(value = "/getWidgetList")
    public List<ViewDashboardWidget> getWidgetList() {
        List<DashboardWidget> list = widgetDao.getWidgetListAdmin(tlUser.get().getUserId());
        return Lists.transform(list, ViewDashboardWidget.TO);
    }

    @RequestMapping(value = "/getWidgetListUser")
    public List<ViewDashboardWidget> getWidgetListUser() {
        List<DashboardWidget> list = adminSerivce.getWidgetList(tlUser.get().getUserId());
        return Lists.transform(list, ViewDashboardWidget.TO);
    }

    @RequestMapping(value = "/getDatasourceList")
    public List<ViewDashboardDatasource> getDatasourceList() {
        return datasourceService.getViewDatasourceList(() -> datasourceDao.getDatasourceListAdmin(tlUser.get().getUserId()));
    }

    @RequestMapping(value = "/getMenuList")
    public List<DashboardMenu> getMenuList() {
        if (adminUserId.equals(tlUser.get().getUserId())) {
            return menuService.getMenuList();
        } else {
            List<Long> menuId = menuDao.getMenuIdByRoleAdmin(tlUser.get().getUserId());
            return menuService.getMenuList().stream().filter(e -> menuId.stream().anyMatch(id -> id.equals(e.getMenuId()))).collect(Collectors.toList());
        }
    }
}
