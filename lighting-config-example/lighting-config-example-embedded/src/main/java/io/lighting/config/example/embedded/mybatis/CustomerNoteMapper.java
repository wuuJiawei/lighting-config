package io.lighting.config.example.embedded.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerNoteMapper extends BaseMapper<CustomerNote> {
}
