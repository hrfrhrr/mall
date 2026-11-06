package com.baizhan.mall.user.service;

import com.baizhan.mall.user.entity.Address;
import com.baizhan.mall.user.entity.ShoppingUser;
import com.baizhan.mall.user.mapper.AddressMapper;
import com.baizhan.mall.user.mapper.ShoppingUserMapper;
import com.baizhan.mall.common.core.exception.BizException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class UserService {

    private final ShoppingUserMapper shoppingUserMapper;
    private final AddressMapper addressMapper;

    public UserService(ShoppingUserMapper shoppingUserMapper, AddressMapper addressMapper) {
        this.shoppingUserMapper = shoppingUserMapper;
        this.addressMapper = addressMapper;
    }

    public ShoppingUser getById(Long id) {
        ShoppingUser user = shoppingUserMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return user;
    }

    public List<Address> listAddresses(Long userId) {
        return addressMapper.selectList(Wrappers.<Address>lambdaQuery()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getId));
    }

    public Long createAddress(Long userId, Address address) {
        address.setId(null);
        address.setUserId(userId);
        addressMapper.insert(address);
        return address.getId();
    }

    public void removeAddress(Long userId, Long addressId) {
        int rows = addressMapper.delete(Wrappers.<Address>lambdaQuery()
                .eq(Address::getId, addressId)
                .eq(Address::getUserId, userId));
        if (rows == 0) {
            throw new BizException("地址不存在或无权删除");
        }
    }
}
