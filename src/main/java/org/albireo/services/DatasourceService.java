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

package org.albireo.services;

import com.alibaba.fastjson.JSONObject;
import org.albireo.dao.DatasourceDao;
import org.albireo.dataprovider.DataProviderManager;
import org.albireo.dataprovider.annotation.DatasourceParameter;
import org.albireo.dto.ViewDashboardDatasource;
import org.albireo.pojo.DashboardDatasource;
import org.albireo.services.role.RolePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by yfyuan on 2016/8/19.
 */
@Service
public class DatasourceService {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DatasourceDao datasourceDao;

    public List<ViewDashboardDatasource> getViewDatasourceList(Supplier<List<DashboardDatasource>> daoQuery) {
        List<DashboardDatasource> list = daoQuery.get();
        List<ViewDashboardDatasource> vlist = list.stream().map(e -> (ViewDashboardDatasource) ViewDashboardDatasource.TO.apply(e)).collect(Collectors.toList());
        vlist.forEach(e -> {
            try {
                List<String> fields = DataProviderManager.getProviderFieldByType(e.getType(), DatasourceParameter.Type.Password);
                fields.forEach(f -> {
                    if (e.getConfig().containsKey(f)) {
                        e.getConfig().put(f, "");
                    }
                });
            } catch (Exception ex) {
                LOG.error("", e);
            }
        });
        return vlist;
    }

    public ServiceStatus save(String userId, String json) {
        JSONObject jsonObject = JSONObject.parseObject(json);
        DashboardDatasource datasource = new DashboardDatasource();
        datasource.setUserId(userId);
        datasource.setName(jsonObject.getString("name"));
        datasource.setType(jsonObject.getString("type"));
        datasource.setConfig(jsonObject.getString("config"));

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("user_id", datasource.getUserId());
        paramMap.put("source_name", datasource.getName());
        if (datasourceDao.countExistDatasourceName(paramMap) <= 0) {
            datasourceDao.save(datasource);
            return new ServiceStatus(ServiceStatus.Status.Success, "success");
        } else {
            return new ServiceStatus(ServiceStatus.Status.Fail, "Duplicated Name!");
        }
    }

    public ServiceStatus update(String userId, String json) {
        JSONObject jsonObject = JSONObject.parseObject(json);
        DashboardDatasource datasource = new DashboardDatasource();
        datasource.setUserId(userId);
        datasource.setName(jsonObject.getString("name"));
        datasource.setType(jsonObject.getString("type"));
        datasource.setConfig(jsonObject.getString("config"));
        datasource.setId(jsonObject.getLong("id"));
        datasource.setUpdateTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("datasource_id", datasource.getId());
        paramMap.put("user_id", datasource.getUserId());
        paramMap.put("source_name", datasource.getName());
        if (datasourceDao.countExistDatasourceName(paramMap) <= 0) {
            datasourceDao.update(datasource);
            return new ServiceStatus(ServiceStatus.Status.Success, "success");
        } else {
            return new ServiceStatus(ServiceStatus.Status.Fail, "Duplicated Name!");
        }
    }

    public ServiceStatus delete(String userId, Long id) {
        datasourceDao.delete(id, userId);
        return new ServiceStatus(ServiceStatus.Status.Success, "success");
    }

    public ServiceStatus checkDatasource(String userId, Long id) {
        DashboardDatasource datasource = datasourceDao.getDatasource(id);
        if (datasourceDao.checkDatasourceRole(userId, id, RolePermission.PATTERN_READ) == 1) {
            return new ServiceStatus(ServiceStatus.Status.Success, "success");
        } else {
            return new ServiceStatus(ServiceStatus.Status.Fail, datasource.getName());
        }
    }
}
