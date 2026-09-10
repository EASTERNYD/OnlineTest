package com.example.demo.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.example.demo.common.BizException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * EasyExcel 导入导出工具
 */
public class ExcelUtil {

    public static <T> List<T> read(MultipartFile file, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        try (InputStream is = file.getInputStream()) {
            EasyExcel.read(is, clazz, new ReadListener<T>() {
                @Override
                public void invoke(T data, AnalysisContext context) {
                    list.add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                }
            }).sheet().doRead();
        } catch (Exception e) {
            throw new BizException("Excel 解析失败: " + e.getMessage());
        }
        return list;
    }

    public static void writeTemplate(HttpServletResponse response, Class<?> head, String fileName) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + encoded + ".xlsx");
            EasyExcel.write(response.getOutputStream(), head).sheet("模板").doWrite(new ArrayList<>());
        } catch (Exception e) {
            throw new BizException("模板下载失败: " + e.getMessage());
        }
    }
}
