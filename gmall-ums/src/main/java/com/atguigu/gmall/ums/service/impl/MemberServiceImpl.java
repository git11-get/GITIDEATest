package com.atguigu.gmall.ums.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.ums.entity.Member;
import com.atguigu.gmall.ums.entity.MemberReceiveAddress;
import com.atguigu.gmall.ums.mapper.MemberMapper;
import com.atguigu.gmall.ums.mapper.MemberReceiveAddressMapper;
import com.atguigu.gmall.ums.service.MemberService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;

import java.util.List;

/**
 * <p>
 * 会员表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2020-03-07
 */
@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {


    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private MemberReceiveAddressMapper memberReceiveAddressMapper;

    @Override
    public Member login(String username, String password) {

        String digest = DigestUtils.md5DigestAsHex(password.getBytes());

        Member member = memberMapper.selectOne(new QueryWrapper<Member>()
                .eq("username", username)
                .eq("password", digest));
        return member;
    }

    @Override
    public List<MemberReceiveAddress> getMemberAddress(Long id) {
        return memberReceiveAddressMapper.selectList(new QueryWrapper<MemberReceiveAddress>().eq("member_id", id));
    }

    @Override
    public MemberReceiveAddress getMemberAddressByAddressId(Long addressId) {
        return memberReceiveAddressMapper.selectById(addressId);
    }


}
