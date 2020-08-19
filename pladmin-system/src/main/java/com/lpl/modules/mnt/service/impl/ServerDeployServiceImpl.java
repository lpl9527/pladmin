package com.lpl.modules.mnt.service.impl;

import com.lpl.modules.mnt.domain.ServerDeploy;
import com.lpl.modules.mnt.repository.ServerDeployRepository;
import com.lpl.modules.mnt.service.ServerDeployService;
import com.lpl.modules.mnt.service.dto.ServerDeployDto;
import com.lpl.modules.mnt.service.dto.ServerDeployQueryCriteria;
import com.lpl.modules.mnt.service.mapstruct.ServerDeployMapper;
import com.lpl.modules.mnt.uitl.ExecuteShellUtils;
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


/**
 * @author lpl
 * 服务器部署业务实现类
 */
@Service
@RequiredArgsConstructor
public class ServerDeployServiceImpl implements ServerDeployService {

    private final ServerDeployRepository serverDeployRepository;
    private final ServerDeployMapper serverDeployMapper;

    /**
     * 分页查询全部服务器
     * @param criteria
     */
    @Override
    public Object queryAll(ServerDeployQueryCriteria criteria, Pageable pageable){
        Page<ServerDeploy> page = serverDeployRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(serverDeployMapper::toDto));
    }

    /**
     * 查询全部服务器
     * @param criteria
     */
    @Override
    public List<ServerDeployDto> queryAll(ServerDeployQueryCriteria criteria) {
        return serverDeployMapper.toDto(serverDeployRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder)));
    }

    /**
     * 新增服务器
     * @param serverDeploy
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ServerDeploy serverDeploy) {
        serverDeployRepository.save(serverDeploy);
    }

    /**
     * 编辑服务器
     * @param serverDeploy
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ServerDeploy serverDeploy) {
        ServerDeploy oldServerDeploy = serverDeployRepository.findById(serverDeploy.getId()).orElseGet(ServerDeploy::new);
        ValidationUtil.isNull( oldServerDeploy.getId(),"ServerDeploy","id",serverDeploy.getId());

        oldServerDeploy.copy(serverDeploy);
        serverDeployRepository.save(oldServerDeploy);
    }

    /**
     * 删除服务器
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id : ids) {
            serverDeployRepository.deleteById(id);
        }
    }

    /**
     * 测试连接服务器
     * @param serverDeploy
     */
    @Override
    public Boolean testConnect(ServerDeploy serverDeploy) {
        ExecuteShellUtils executeShellUtils = null;
        try {
            //获取连接
            executeShellUtils = new ExecuteShellUtils(serverDeploy.getIp(), serverDeploy.getAccount(), serverDeploy.getPassword(), serverDeploy.getPort());
            //执行shell命令
            return executeShellUtils.execute("ls")==0;
        } catch (Exception e) {
            return false;
        }finally {
            //关闭连接
            if (executeShellUtils != null) {
                executeShellUtils.close();
            }
        }
    }

    /**
     * 导出服务器数据
     * @param queryAll
     * @param response
     * @throws IOException
     */
    @Override
    public void download(List<ServerDeployDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ServerDeployDto deployDto : queryAll) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("服务器名称", deployDto.getName());
            map.put("服务器IP", deployDto.getIp());
            map.put("端口", deployDto.getPort());
            map.put("账号", deployDto.getAccount());
            map.put("创建日期", deployDto.getCreateTime());
            list.add(map);
        }
        FileUtils.downloadExcel(list, response);
    }
}
