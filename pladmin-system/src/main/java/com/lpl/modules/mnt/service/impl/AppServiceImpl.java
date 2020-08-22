package com.lpl.modules.mnt.service.impl;

import com.lpl.exception.BadRequestException;
import com.lpl.modules.mnt.domain.App;
import com.lpl.modules.mnt.domain.Deploy;
import com.lpl.modules.mnt.domain.ServerDeploy;
import com.lpl.modules.mnt.repository.AppRepository;
import com.lpl.modules.mnt.repository.DeployHistoryRepository;
import com.lpl.modules.mnt.repository.DeployRepository;
import com.lpl.modules.mnt.repository.ServerDeployRepository;
import com.lpl.modules.mnt.service.AppService;
import com.lpl.modules.mnt.service.dto.AppDto;
import com.lpl.modules.mnt.service.dto.AppQueryCriteria;
import com.lpl.modules.mnt.service.mapstruct.AppMapper;
import com.lpl.utils.FileUtils;
import com.lpl.utils.PageUtil;
import com.lpl.utils.QueryHelp;
import com.lpl.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AppServiceImpl implements AppService {

    private final AppRepository appRepository;
    private final AppMapper appMapper;
    private final DeployRepository deployRepository;
    private final ServerDeployRepository serverDeployRepository;
    private final DeployHistoryRepository deployHistoryRepository;

    /**
     * 分页查询应用
     * @param criteria
     * @param pageable
     */
    @Override
    public Object queryAll(AppQueryCriteria criteria, Pageable pageable) {
        Page<App> page = appRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(appMapper::toDto));
    }

    /**
     * 查询全部数据
     * @param criteria
     */
    @Override
    public List<AppDto> queryAll(AppQueryCriteria criteria) {
        return appMapper.toDto(appRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder)));
    }

    /**
     * 创建应用
     * @param app
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(App app) {
        verification(app);
        appRepository.save(app);
    }

    /**
     * 更新应用
     * @param app
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(App app) {
        verification(app);
        App oldApp = appRepository.findById(app.getId()).orElseGet(App::new);
        ValidationUtil.isNull(oldApp.getId(),"App","id",app.getId());

        oldApp.copy(app);
        appRepository.save(oldApp);
    }

    /**
     * 删除应用
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id : ids) {
            //删除应用时也要删除其关联的部署信息、部署历史信息
            //根据应用id查询部署信息
            Deploy deploy = deployRepository.findDeployByAppId(id);
            if (null != deploy) {
                //根据部署id删除所有服务id关系
                serverDeployRepository.deleteByDeployId(deploy.getId());

                //根据部署id查询部署历史，部署历史不为空时删除此记录
                deployHistoryRepository.deleteByDeployId(deploy.getId());

                //删除部署信息
                deployRepository.delete(deploy);
            }
            //删除应用
            appRepository.deleteById(id);
        }
    }

    /**
     * 目录校验
     * @param app
     */
    private void verification(App app){
        String opt = "/opt";
        String home = "/home";
        if (!(app.getUploadPath().startsWith(opt) || app.getUploadPath().startsWith(home))) {
            throw new BadRequestException("文件只能上传在opt目录或者home目录 ");
        }
        if (!(app.getDeployPath().startsWith(opt) || app.getDeployPath().startsWith(home))) {
            throw new BadRequestException("文件只能部署在opt目录或者home目录 ");
        }
        if (!(app.getBackupPath().startsWith(opt) || app.getBackupPath().startsWith(home))) {
            throw new BadRequestException("文件只能备份在opt目录或者home目录 ");
        }
    }

    /**
     * 导出应用到表格
     * @param queryAll
     * @param response
     * @throws IOException
     */
    @Override
    public void download(List<AppDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (AppDto appDto : queryAll) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("应用名称", appDto.getName());
            map.put("端口", appDto.getPort());
            map.put("上传目录", appDto.getUploadPath());
            map.put("部署目录", appDto.getDeployPath());
            map.put("备份目录", appDto.getBackupPath());
            map.put("启动脚本", appDto.getStartScript());
            map.put("部署脚本", appDto.getDeployScript());
            map.put("创建日期", appDto.getCreateTime());
            list.add(map);
        }
        FileUtils.downloadExcel(list, response);
    }
}
