package com.example.demo.service;

import com.example.demo.common.BizException;
import com.example.demo.common.PageResult;
import com.example.demo.entity.Notice;
import com.example.demo.mapper.NoticeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    public PageResult<Notice> page(int page, int size) {
        int offset = (page - 1) * size;
        List<Notice> list = noticeMapper.selectPage(offset, size);
        long total = noticeMapper.count();
        return new PageResult<>(total, list);
    }

    public List<Notice> enabled() {
        return noticeMapper.selectEnabled();
    }

    public List<Notice> latest(int limit) {
        return noticeMapper.selectLatest(limit);
    }

    public Notice detail(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BizException(404, "公告不存在");
        }
        return notice;
    }

    public void create(Notice notice) {
        if (notice.getStatus() == null) {
            notice.setStatus(1);
        }
        if (notice.getType() == null) {
            notice.setType(1);
        }
        notice.setCreateTime(LocalDateTime.now());
        noticeMapper.insert(notice);
    }

    public void update(Notice notice) {
        if (noticeMapper.selectById(notice.getId()) == null) {
            throw new BizException(404, "公告不存在");
        }
        noticeMapper.update(notice);
    }

    public void updateStatus(Long id, Integer status) {
        noticeMapper.updateStatus(id, status);
    }

    public void delete(Long id) {
        noticeMapper.delete(id);
    }
}
