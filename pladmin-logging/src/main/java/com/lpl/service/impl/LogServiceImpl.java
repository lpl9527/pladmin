package com.lpl.service.impl;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.lpl.domain.Log;
import com.lpl.repository.LogRepository;
import com.lpl.service.LogService;
import com.lpl.service.dto.LogQueryCriteria;
import com.lpl.service.mapstruct.LogErrorMapper;
import com.lpl.service.mapstruct.LogSmallMapper;
import com.lpl.utils.*;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private static final Logger log = LoggerFactory.getLogger(LogServiceImpl.class);
    private final LogRepository logRepository;
    private final LogSmallMapper logSmallMapper;
    private final LogErrorMapper logErrorMapper;

    /**
     * AOP保存日志数据，开启异步日志记录
     * @param username  用户名
     * @param browser   浏览器
     * @param ip    请求ip
     * @param joinPoint 连接点对象
     * @param log   日志对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(String username, String browser, String ip, ProceedingJoinPoint joinPoint, Log log) {
        //获取连接点的方法签名对象
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取连接点方法对象
        Method method = signature.getMethod();
        //获取切入点方法的@Log注解对象
        com.lpl.annotation.Log aopLog = method.getAnnotation(com.lpl.annotation.Log.class);

        //获取方法的全路径（类所在的路径拼上方法名称），其中joinPoint.getTarget()用于获取连接点所在的目标对象。
        //  补充：getThis()方法获取的是代理对象本身。getName()获取的是连接点方法名称
        String methodName = joinPoint.getTarget().getClass().getName() + "." + signature.getName() + "()";

        //获取连接点方法运行时的入参列表对应字符串
        StringBuilder params = new StringBuilder("{");
        List<Object> argValues = new ArrayList<>(Arrays.asList(joinPoint.getArgs()));
        for (Object argValue : argValues){
            params.append(argValue).append(" ");    //参数以空格分隔
        }
        params.append("}");

        assert log != null;
        //--------------------------------------------------------------
        //设置请求方法参数信息
        log.setParams(params.toString());
        //设置日志描述信息（即@Log注解的value值）
        log.setDescription(aopLog.value());
        //设置请求ip
        log.setRequestIp(ip);
        //设置用户名
        //尚未登录时，从SpringSecurity上下文中获取的当前用户名称为空，需要设置登录login请求的用户名为前台输入的用户名（即login方法的第一个参数的username属性对应的值）
        String loginPath = "login";
        if (loginPath.equals(signature.getName())) {    //如果是登录方法请求
            try{
                username = new JSONObject(argValues.get(0)).get("username").toString();
            }catch (Exception e) {
                LogServiceImpl.log.error(e.getMessage(), e);
            }
        }
        log.setUsername(username);
        //设置请求地址
        log.setAddress(StringUtils.getAddressInfo(ip));
        //设置方法全路径
        log.setMethod(methodName);
        //设置代理浏览器
        log.setBrowser(browser);

        //保存操作日志信息到数据库
        logRepository.save(log);
    }

    /**
     * 分页查询用户日志
     * @param criteria  查询条件
     * @param pageable  分页参数
     */
    @Override
    public Object queryAllByUser(LogQueryCriteria criteria, Pageable pageable) {
        //分页查询
        Page<Log> page = logRepository.findAll(((root, criteriaQuery, cb) -> QueryHelp.getPredicate(root, criteria, cb)), pageable);
        return PageUtil.toPage(page.map(logSmallMapper::toDto));
    }

    /**
     * 分页查询所有INFO类型日志
     * @param criteria
     * @param pageable
     */
    @Override
    public Object queryAll(LogQueryCriteria criteria, Pageable pageable) {
        Page<Log> page = logRepository.findAll(((root, criteriaQuery, cb) -> QueryHelp.getPredicate(root, criteria, cb)), pageable);
        String status = "ERROR";
        if (status.equals(criteria.getLogType())) {
            return PageUtil.toPage(page.map(logErrorMapper::toDto));
        }
        return page;
    }

    /**
     * 查询全部INFO类型数据，不分页
     * @param criteria
     */
    @Override
    public List<Log> queryAll(LogQueryCriteria criteria) {
        return logRepository.findAll(((root, criteriaQuery, cb) -> QueryHelp.getPredicate(root, criteria, cb)));
    }

    /**
     * 日志异常详情查询
     * @param id
     */
    @Override
    public Object findByErrDetail(Long id) {
        Log log = logRepository.findById(id).orElseGet(Log::new);
        ValidationUtil.isNull(log.getId(), "Log", "id", id);
        byte[] details = log.getExceptionDetail();
        return Dict.create().set("exception", new String(ObjectUtil.isNotNull(details) ? details : "".getBytes()));
    }

    /**
     * 删除所有INFO日志
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delAllByInfo() {
        logRepository.deleteByLogType("INFO");
    }

    /**
     * 删除所有INFO日志
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delAllByError() {
        logRepository.deleteByLogType("ERROR");
    }

    /**
     * 导出日志数据
     * @param logs
     * @param response
     * @throws IOException
     */
    @Override
    public void download(List<Log> logs, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Log log : logs) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("用户名", log.getUsername());
            map.put("IP", log.getRequestIp());
            map.put("IP来源", log.getAddress());
            map.put("描述", log.getDescription());
            map.put("浏览器", log.getBrowser());
            map.put("请求耗时/毫秒", log.getTime());
            map.put("异常详情", new String(ObjectUtil.isNotNull(log.getExceptionDetail()) ? log.getExceptionDetail() : "".getBytes()));
            map.put("创建日期", log.getCreateTime());
            list.add(map);
        }
        FileUtils.downloadExcel(list, response);
    }
}
