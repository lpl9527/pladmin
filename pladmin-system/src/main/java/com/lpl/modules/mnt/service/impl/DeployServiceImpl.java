package com.lpl.modules.mnt.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.lpl.exception.BadRequestException;
import com.lpl.modules.mnt.domain.Deploy;
import com.lpl.modules.mnt.domain.DeployHistory;
import com.lpl.modules.mnt.repository.DeployRepository;
import com.lpl.modules.mnt.service.DeployHistoryService;
import com.lpl.modules.mnt.service.DeployService;
import com.lpl.modules.mnt.service.ServerDeployService;
import com.lpl.modules.mnt.service.dto.AppDto;
import com.lpl.modules.mnt.service.dto.DeployDto;
import com.lpl.modules.mnt.service.dto.DeployQueryCriteria;
import com.lpl.modules.mnt.service.dto.ServerDeployDto;
import com.lpl.modules.mnt.service.mapstruct.DeployMapper;
import com.lpl.modules.mnt.uitl.ExecuteShellUtils;
import com.lpl.modules.mnt.uitl.ScpClientUtils;
import com.lpl.modules.mnt.websocket.MsgType;
import com.lpl.modules.mnt.websocket.SocketMsg;
import com.lpl.modules.mnt.websocket.WebSocketServer;
import com.lpl.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author lpl
 * 部署业务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeployServiceImpl implements DeployService {

    private final String FILE_SEPARATOR = "/";

    private final DeployRepository deployRepository;
    private final DeployMapper deployMapper;
    private final ServerDeployService serverDeployService;
    private final DeployHistoryService deployHistoryService;

    /**
     * 循环次数
     */
    private final Integer count = 30;

    /**
     * 分页查询所有部署应用
     * @param criteria
     * @param pageable
     */
    @Override
    public Object queryAll(DeployQueryCriteria criteria, Pageable pageable) {
        Page<Deploy> page = deployRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page.map(deployMapper::toDto));
    }

    /**
     * 查询全部部署
     * @param criteria
     */
    @Override
    public List<DeployDto> queryAll(DeployQueryCriteria criteria) {
        return deployMapper.toDto(deployRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder)));
    }

    /**
     * 根据id查询部署
     * @param id
     */
    @Override
    public DeployDto findById(Long id) {
        Deploy deploy = deployRepository.findById(id).orElseGet(Deploy::new);
        ValidationUtil.isNull(deploy.getId(), "Deploy", "id", id);
        return deployMapper.toDto(deploy);
    }

    /**
     * 新增部署
     * @param deploy
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Deploy deploy) {
        deployRepository.save(deploy);
    }

    /**
     * 修改部署
     * @param deploy
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Deploy deploy) {
        Deploy oldDeploy = deployRepository.findById(deploy.getId()).orElseGet(Deploy::new);
        ValidationUtil.isNull(oldDeploy.getId(), "Deploy", "id", deploy.getId());

        oldDeploy.copy(deploy);
        deployRepository.save(oldDeploy);
    }

    /**
     * 删除部署
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id : ids) {
            deployRepository.deleteById(id);
        }
    }

    /**
     * 导出部署数据
     * @param deployDtos
     * @param response
     * @throws IOException
     */
    @Override
    public void download(List<DeployDto> deployDtos, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DeployDto deployDto : deployDtos) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("应用名称", deployDto.getApp().getName());
            map.put("服务器", deployDto.getServers());
            map.put("部署日期", deployDto.getCreateTime());
            list.add(map);
        }
        FileUtils.downloadExcel(list, response);
    }

    //-------------------------------------------------------------------------------------------------

    /**
     * 部署文件到服务器
     * @param fileSavePath 文件路径
     * @param appId     应用id
     */
    @Override
    public void deploy(String fileSavePath, Long appId) {
        this.deployApp(fileSavePath, appId);
    }

    /**
     * 部署应用到服务器
     * @param fileSavePath 本地路径
     * @param id 应用id
     */
    private void deployApp(String fileSavePath, Long id) {
        //根据id查询部署
        DeployDto deploy = this.findById(id);
        if (deploy == null) {
            //通过WebSocket发送信息
            this.sendMsg("部署信息不存在", MsgType.ERROR);
            throw new BadRequestException("部署信息不存在");
        }
        //获取应用
        AppDto app = deploy.getApp();
        if (app == null) {
            this.sendMsg("包对应应用信息不存在", MsgType.ERROR);
            throw new BadRequestException("包对应应用信息不存在");
        }
        //获取应用端口
        int port = app.getPort();
        //获取服务器部署路径
        String uploadPath = app.getUploadPath();

        StringBuilder sb = new StringBuilder();
        String msg;

        //获取要部署的服务器，遍历部署
        Set<ServerDeployDto> deploys = deploy.getDeploys();
        for (ServerDeployDto deployDto : deploys) {
            //获取服务器ip
            String ip = deployDto.getIp();
            //根据ip获取执行shell工具对象
            ExecuteShellUtils executeShellUtils = this.getExecuteShellUtils(ip);
            //判断是否是第一次部署（查找服务器上是否有此文件）
            boolean flag = this.checkFile(executeShellUtils, app);
            //确认服务器上是否有这些目录
            executeShellUtils.execute("mkdir -p " + app.getUploadPath());
            executeShellUtils.execute("mkdir -p " + app.getBackupPath());
            executeShellUtils.execute("mkdir -p " + app.getDeployPath());

            msg = String.format("登陆到服务器:%s", ip);
            //根据ip获取远程linux执行工具类对象
            ScpClientUtils scpClientUtils = this.getScpClientUtils(ip);
            log.info(msg);
            this.sendMsg(msg, MsgType.INFO);
            msg = String.format("上传文件到服务器:%s<br>目录:%s下，请稍等...", ip, uploadPath);
            this.sendMsg(msg, MsgType.INFO);
            //上传文件到服务器
            scpClientUtils.putFile(fileSavePath, uploadPath);
            //如果不是第一次部署
            if (flag) {
                this.sendMsg("停止原来应用", MsgType.INFO);
                //停止应用
                this.stopApp(port, executeShellUtils);
                sendMsg("备份原来应用", MsgType.INFO);
                //备份应用
                this.backupApp(executeShellUtils, ip, app.getDeployPath()+ FILE_SEPARATOR, app.getName(), app.getBackupPath() + FILE_SEPARATOR, id);
            }
            this.sendMsg("部署应用", MsgType.INFO);
            //获取部署脚本部署文件，并启动应用
            String deployScript = app.getDeployScript();
            //执行脚本
            executeShellUtils.execute(deployScript);
            this.sleep(3);
            sendMsg("应用部署中，请耐心等待部署结果，或者稍后手动查看部署状态", MsgType.INFO);

            int i  = 0;
            boolean result = false;
            // 由于启动应用需要时间，所以需要循环获取状态，如果超过30次，则认为是启动失败
            while (i++ < this.count){
                //获取应用启动结果
                result = this.checkIsRunningStatus(port, executeShellUtils);
                if(result){
                    break;
                }
                // 休眠6秒
                this.sleep(6);
            }
            sb.append("服务器:").append(deployDto.getName()).append("<br>应用:").append(app.getName());
            //向客户端发送最终结果
            this.sendResultMsg(result, sb);
            //释放连接
            executeShellUtils.close();
        }
    }

    /**
     * 通过WebSocket向客户端返回最终结果
     * @param result
     * @param sb
     */
    private void sendResultMsg(boolean result, StringBuilder sb) {
        if (result) {
            sb.append("<br>启动成功!");
            sendMsg(sb.toString(), MsgType.INFO);
        } else {
            sb.append("<br>启动失败!");
            sendMsg(sb.toString(), MsgType.ERROR);
        }
    }

    /**
     * 获取指定端口运行状态
     * @param port
     * @param executeShellUtils
     */
    private boolean checkIsRunningStatus(int port, ExecuteShellUtils executeShellUtils) {
        String result = executeShellUtils.executeForResult(String.format("fuser -n tcp %d", port));
        return result.indexOf("/tcp:") > 0;
    }

    /**
     * 线程休眠指定秒数
     * @param second
     */
    private void sleep(int second) {
        try {
            Thread.sleep(second * 1000);
        } catch (InterruptedException e) {
            log.error(e.getMessage(),e);
        }
    }

    /**
     * 停止远程服务器上指定端口
     * @param port 端口
     * @param executeShellUtils 远程shell工具类
     */
    private void stopApp(int port, ExecuteShellUtils executeShellUtils) {
        //发送停止命令
        executeShellUtils.execute(String.format("lsof -i :%d|grep -v \"PID\"|awk '{print \"kill -9\",$2}'|sh", port));
    }

    /**
     * 备份远程服务器应用到指定目录
     * @param executeShellUtils 远程shell工具
     * @param ip 远程ip
     * @param fileSavePath 本地文件路径
     * @param appName 应用名称
     * @param backupPath 远程备份目录
     * @param id 应用id
     */
    private void backupApp(ExecuteShellUtils executeShellUtils, String ip, String fileSavePath, String appName, String backupPath, Long id) {
        //备份日期
        String deployDate = DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN);
        //备份脚本
        StringBuilder sb = new StringBuilder();
        backupPath += appName + FILE_SEPARATOR + deployDate + "\n";
        sb.append("mkdir -p ").append(backupPath);
        sb.append("mv -f ").append(fileSavePath);
        sb.append(appName).append(" ").append(backupPath);
        log.info("备份应用脚本:" + sb.toString());
        executeShellUtils.execute(sb.toString());
        //还原信息入库
        DeployHistory deployHistory = new DeployHistory();
        deployHistory.setAppName(appName);
        deployHistory.setDeployUser(SecurityUtils.getCurrentUsername());
        deployHistory.setIp(ip);
        deployHistory.setDeployId(id);
        deployHistoryService.create(deployHistory);
    }

    /**
     * 根据ip获取执行shell工具对象
     * @param ip 服务器ip
     */
    private ExecuteShellUtils getExecuteShellUtils(String ip) {
        //根据ip查询服务器信息
        ServerDeployDto serverDeployDto = serverDeployService.findByIp(ip);
        if (serverDeployDto == null) {
            sendMsg("IP对应服务器信息不存在：" + ip, MsgType.ERROR);
            throw new BadRequestException("IP对应服务器信息不存在：" + ip);
        }
        return new ExecuteShellUtils(ip, serverDeployDto.getAccount(), serverDeployDto.getPassword(),serverDeployDto.getPort());
    }

    /**
     * 根据服务器ip获取远程服务器操作对象
     * @param ip 服务器ip
     */
    private ScpClientUtils getScpClientUtils(String ip) {
        //根据ip查找服务器信息
        ServerDeployDto serverDeployDto = serverDeployService.findByIp(ip);
        if (serverDeployDto == null) {
            sendMsg("IP对应服务器信息不存在：" + ip, MsgType.ERROR);
            throw new BadRequestException("IP对应服务器信息不存在：" + ip);
        }
        return ScpClientUtils.getInstance(ip, serverDeployDto.getPort(), serverDeployDto.getAccount(), serverDeployDto.getPassword());
    }

    /**
     * 判断文件是否在服务器上存在
     * @param executeShellUtils
     * @param appDto
     */
    private boolean checkFile(ExecuteShellUtils executeShellUtils, AppDto appDto) {
        String result = executeShellUtils.executeForResult("find " + appDto.getDeployPath() + " -name " + appDto.getName());
        return result.indexOf(appDto.getName()) > 0;
    }

    /**
     * 通过WebSocket发送信息
     * @param msg 消息
     * @param msgType 消息类型
     */
    private void sendMsg(String msg, MsgType msgType) {
        try {
            WebSocketServer.sendInfo(new SocketMsg(msg, msgType), "deploy");
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
    }
}
