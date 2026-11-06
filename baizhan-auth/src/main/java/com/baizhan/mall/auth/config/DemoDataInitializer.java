package com.baizhan.mall.auth.config;

import com.baizhan.mall.auth.entity.Admin;
import com.baizhan.mall.auth.entity.ShoppingUser;
import com.baizhan.mall.auth.mapper.AdminMapper;
import com.baizhan.mall.auth.mapper.ShoppingUserMapper;
import com.baizhan.mall.common.security.PasswordUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 演示账号初始化：库中历史密码为不可逆的杂散数据，为保证开箱可登录，
 * 启动时按配置写入/更新两个演示账号（BCrypt）。生产环境请关闭。
 */
@Slf4j
@Component
public class DemoDataInitializer implements CommandLineRunner {

    @Value("${baizhan.auth.init-demo:true}")
    private boolean initDemo;

    @Value("${baizhan.auth.demo-admin-password:baizhan123}")
    private String demoAdminPassword;

    @Value("${baizhan.auth.demo-user-password:user123}")
    private String demoUserPassword;

    private final AdminMapper adminMapper;
    private final ShoppingUserMapper shoppingUserMapper;

    public DemoDataInitializer(AdminMapper adminMapper, ShoppingUserMapper shoppingUserMapper) {
        this.adminMapper = adminMapper;
        this.shoppingUserMapper = shoppingUserMapper;
    }

    @Override
    public void run(String... args) {
        if (!initDemo) {
            return;
        }
        upsertAdmin("baizhan", demoAdminPassword);
        upsertUser("user001", demoUserPassword, "13812345678");
        log.info("[演示账号] admin=baizhan/{}  user=user001/{}", demoAdminPassword, demoUserPassword);
    }

    private void upsertAdmin(String username, String rawPassword) {
        Admin admin = adminMapper.selectOne(Wrappers.<Admin>lambdaQuery()
                .eq(Admin::getUsername, username).last("limit 1"));
        String encoded = PasswordUtil.encode(rawPassword);
        if (admin == null) {
            admin = new Admin();
            admin.setUsername(username);
            admin.setPassword(encoded);
            adminMapper.insert(admin);
        } else if (!PasswordUtil.matches(rawPassword, admin.getPassword())) {
            admin.setPassword(encoded);
            adminMapper.updateById(admin);
        }
    }

    private void upsertUser(String username, String rawPassword, String phone) {
        ShoppingUser user = shoppingUserMapper.selectOne(Wrappers.<ShoppingUser>lambdaQuery()
                .eq(ShoppingUser::getUsername, username).last("limit 1"));
        String encoded = PasswordUtil.encode(rawPassword);
        if (user == null) {
            user = new ShoppingUser();
            user.setUsername(username);
            user.setPassword(encoded);
            user.setPhone(phone);
            user.setStatus("Y");
            user.setNickName("演示用户");
            shoppingUserMapper.insert(user);
        } else if (!PasswordUtil.matches(rawPassword, user.getPassword())) {
            user.setPassword(encoded);
            shoppingUserMapper.updateById(user);
        }
    }
}
