package com.nowcoder.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
@Deprecated
public interface LoginTicketMapper extends BaseMapper<LoginTicket> {
}
