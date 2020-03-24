package com.atguigu.gmall.vo.ums;

import lombok.Data;

import java.io.Serializable;


@Data
public class LoginResponseVo implements Serializable {

    private Long memberLevelId;
    private String username;
    private String nickname;
    private String phone;
    //访问令牌
    private String accessToken;





}
