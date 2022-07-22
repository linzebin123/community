package com.nowcoder.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class LoginTicket {
    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    private int userId;
    private String ticket;
    private int status;
    private Date expired;
}
