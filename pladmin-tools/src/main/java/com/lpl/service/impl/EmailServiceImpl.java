package com.lpl.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.extra.mail.Mail;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import com.lpl.domain.EmailConfig;
import com.lpl.domain.vo.EmailVo;
import com.lpl.exception.BadRequestException;
import com.lpl.repository.EmailRepository;
import com.lpl.service.EmailService;
import com.lpl.utils.EncryptUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.SSLSocketFactory;
import java.util.Optional;

/**
 * @author lpl
 * 邮箱工具业务实现类
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "email")
public class EmailServiceImpl implements EmailService {

    private final EmailRepository emailRepository;

    /**
     * 发送邮箱验证码
     * @param emailVo   邮件视图对象
     * @param emaiLConfig   邮件配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void send(EmailVo emailVo, EmailConfig emaiLConfig) {
        if (null == emaiLConfig) {
            throw new BadRequestException("请先配置邮箱，再操作！");
        }
        //封装
        MailAccount mailAccount = new MailAccount();
        mailAccount.setHost(emaiLConfig.getHost());     //设置smtp邮件服务器主机地址
        mailAccount.setPort(Integer.parseInt(emaiLConfig.getPort()));     //设置邮件服务器端口
        mailAccount.setUser(emaiLConfig.getUser());      //设置发件人名称
        mailAccount.setAuth(true);      //开启认证
        try{
            //对称解密邮件密码或授权码
            mailAccount.setPass(EncryptUtils.desDecrypt(emaiLConfig.getPass()));
        }catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
        mailAccount.setFrom(emaiLConfig.getUser() + "<" + emaiLConfig.getFromUser() + ">");
        //SSL发送方式
        mailAccount.setSslEnable(true);
        //使用STARTTLS安全连接
        mailAccount.setStarttlsEnable(true);

        //发送邮件
        try{
            int size = emailVo.getTos().size();     //收件人邮件列表大小

            Mail.create(mailAccount)    //配置
                    .setTos(emailVo.getTos().toArray(new String[size]))     //设置收件人
                    .setTitle(emailVo.getSubject())     //设置标题
                    .setContent(emailVo.getContent())   //设置内容
                    .setHtml(true)      //支持html格式
                    .setUseGlobalSession(false)     //关闭session
                    .send();

        }catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    //测试邮件发送
    public static void main(String[] args) {

        MailAccount account = new MailAccount();
        account.setHost("smtp.qq.com");
        account.setPort(465);
        account.setAuth(true);

        //这两个值一定要相等，否则会报认证错误（不是授权码的问题）
        account.setFrom("1481782542@qq.com");
        account.setUser("1481782542@qq.com");

        //account.setPass("csvewakqhtoyijbd");
        account.setCharset(CharsetUtil.CHARSET_UTF_8);
        account.setSocketFactoryPort(465);
        account.setSocketFactoryClass("javax.net.ssl.SSLSocketFactory");

        account.setSslEnable(true);
        account.setStarttlsEnable(true);
        //发送邮件
        MailUtil.send(account, CollUtil.newArrayList("443024317@qq.com"), "测试", "邮件来自lpl测试", false);
    }

    /**
     * 查询管理员邮件配置（实际查询的是配置id为1的邮件配置），id为1的配置是系统管理员的邮箱配置
     */
    @Override
    @Cacheable(key = "'config'")
    public EmailConfig find() {
        Optional<EmailConfig> emailConfig = emailRepository.findById(1L);
        return emailConfig.orElseGet(EmailConfig::new);
    }
}
