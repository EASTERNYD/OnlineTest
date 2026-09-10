package com.example.demo.service;

import com.example.demo.common.BizException;
import com.example.demo.common.PageResult;
import com.example.demo.entity.Banner;
import com.example.demo.mapper.BannerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BannerService {

    @Autowired
    private BannerMapper bannerMapper;

    public PageResult<Banner> page(int page, int size) {
        int offset = (page - 1) * size;
        List<Banner> list = bannerMapper.selectPage(offset, size);
        long total = bannerMapper.count();
        return new PageResult<>(total, list);
    }

    public List<Banner> enabled() {
        return bannerMapper.selectEnabled();
    }

    public Banner detail(Long id) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null) {
            throw new BizException(404, "轮播图不存在");
        }
        return banner;
    }

    public void create(Banner banner) {
        if (banner.getStatus() == null) {
            banner.setStatus(1);
        }
        if (banner.getSortOrder() == null) {
            banner.setSortOrder(0);
        }
        banner.setCreateTime(LocalDateTime.now());
        bannerMapper.insert(banner);
    }

    public void update(Banner banner) {
        if (bannerMapper.selectById(banner.getId()) == null) {
            throw new BizException(404, "轮播图不存在");
        }
        bannerMapper.update(banner);
    }

    public void updateStatus(Long id, Integer status) {
        bannerMapper.updateStatus(id, status);
    }

    public void delete(Long id) {
        bannerMapper.delete(id);
    }
}
