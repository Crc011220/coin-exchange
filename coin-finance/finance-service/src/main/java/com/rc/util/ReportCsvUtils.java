package com.rc.util;

import org.apache.commons.lang.StringUtils;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;

public class ReportCsvUtils {

    /**
     * 生成CSV报表
     * 
     * @param response HTTP响应对象
     * @param header 表头数组
     * @param properties 属性字段数组
     * @param fileName 文件名
     * @param soureList 数据源列表
     * @throws Exception 异常信息
     */
    public static void reportList(
            HttpServletResponse response,
            String[] header,
            String[] properties,
            String fileName,
            List<?> soureList
    ) throws Exception {
        if (header==null||properties==null||soureList==null||header.length<=0||properties.length<=0||soureList.size()<=0)
            return;
            
        if(StringUtils.isBlank(fileName)){
            fileName="1.csv";
        }
        
        response.setContentType("application/csv");
        response.setCharacterEncoding("GBK"); 
        response.setHeader("Content-FileName", URLEncoder.encode(fileName, "UTF-8"));
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");
        
        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
        csvWriter.writeHeader(header);
        
        for(Object obj : soureList){
            csvWriter.write(obj, properties);
        }
        
        csvWriter.close();
    }
    
    /**
     * 生成CSV报表（带单元格处理器）
     * 
     * @param response HTTP响应对象
     * @param header 表头数组
     * @param properties 属性字段数组
     * @param fileName 文件名
     * @param soureList 数据源列表
     * @param PROCESSORS 单元格处理器数组
     * @throws Exception 异常信息
     */
    public static void reportListCsv(
            HttpServletResponse response,
            String[] header,
            String[] properties,
            String fileName,
            List<?> soureList,
            CellProcessor[] PROCESSORS
    ) throws Exception {
        if (header==null||properties==null||soureList==null||header.length<=0||properties.length<=0||soureList.size()<=0)
            return;
            
        if(StringUtils.isBlank(fileName)){
            fileName="1.csv";
        }
        
        response.setContentType("application/csv");
        response.setCharacterEncoding("GBK");
        response.setHeader("Content-FileName", URLEncoder.encode(fileName, "UTF-8"));
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");
        
        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
        csvWriter.writeHeader(header);
        
        for(Object obj : soureList){
            csvWriter.write(obj, properties, PROCESSORS);
        }
        
        csvWriter.close();
    }
}