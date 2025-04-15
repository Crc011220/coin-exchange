package com.rc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.CashRecharge;
import com.rc.domain.CoinRecharge;
import com.rc.domain.CoinWithdraw;
import com.rc.model.R;
import com.rc.service.CoinRechargeService;
import com.rc.util.ReportCsvUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.util.CsvContext;
import springfox.documentation.annotations.ApiIgnore;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/coinRecharges")
@Api(tags = "币种充值记录控制器")
public class CoinRechargeController {

    @Autowired
    private CoinRechargeService coinRechargeService;

    @GetMapping("/records")
    @ApiOperation(value = "条件分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页"),
            @ApiImplicitParam(name = "size", value = "每页显示的条数"),
            @ApiImplicitParam(name = "coinId", value = "币种的Id"),
            @ApiImplicitParam(name = "userId", value = "用户的Id"),
            @ApiImplicitParam(name = "userName", value = "用户的名称"),
            @ApiImplicitParam(name = "mobile", value = "用户的手机号"),
            @ApiImplicitParam(name = "status", value = "充值审核状态"),
            @ApiImplicitParam(name = "numMin", value = "充值最小金额"),
            @ApiImplicitParam(name = "numMax", value = "充值最大金额"),
            @ApiImplicitParam(name = "startTime", value = "充值开始时间"),
            @ApiImplicitParam(name = "endTime", value = "充值结束时间"),

    })
    public R<Page<CoinRecharge>> findByPage(@ApiIgnore Page<CoinRecharge> page,
                                            Long coinId, Long userId, String userName,
                                            String mobile, Byte status, String numMin, String numMax,
                                            String startTime, String endTime
    ){
        Page<CoinRecharge> pageData = coinRechargeService.findByPage(page, coinId, userId, userName, mobile, status, numMin, numMax, startTime, endTime);
        return R.ok(pageData);
    }

    @GetMapping("/exportCoinRecharge")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "coinId", value = "币种的Id"),
            @ApiImplicitParam(name = "userId", value = "用户的Id"),
            @ApiImplicitParam(name = "userName", value = "用户的名称"),
            @ApiImplicitParam(name = "mobile", value = "用户的手机号"),
            @ApiImplicitParam(name = "status", value = "充值审核状态"),
            @ApiImplicitParam(name = "numMin", value = "充值最小金额"),
            @ApiImplicitParam(name = "numMax", value = "充值最大金额"),
            @ApiImplicitParam(name = "startTime", value = "充值开始时间"),
            @ApiImplicitParam(name = "endTime", value = "充值结束时间"),

    })
    public void recordsExport(
            Long coinId,
            Long userId, String userName,
            String mobile, Byte status, String numMin, String numMax,
            String startTime, String endTime
    ){
        Page<CoinRecharge> page = new Page<>(1, 10000);
        Page<CoinRecharge> pageData = coinRechargeService.findByPage(page, coinId, userId, userName, mobile, status, numMin, numMax, startTime, endTime);
        List<CoinRecharge> records = pageData.getRecords();

        if (!CollectionUtils.isEmpty(records)){
            String[] header = {"订单ID", "用户ID", "用户名", "币种名称", "充值数量", "收款地址", "交易ID", "充值时间", "完成时间"};
            String[] properties = {"id", "userId", "userName", "coinName", "amount", "address", "txid", "created", "lastUpdateTime"};


            // 长整型转字符串适配器
            CellProcessorAdaptor longToStringAdaptor = new CellProcessorAdaptor() {
                @Override
                public <T> T execute(Object o, CsvContext csvContext) {
                    if (o == null) {
                        return (T) "";
                    }
                    return (T) String.valueOf(o);
                }
            };

            // 金额格式化适配器
            DecimalFormat decimalFormat = new DecimalFormat("0.00000000");
            CellProcessorAdaptor moneyCellProcessorAdaptor = new CellProcessorAdaptor() {
                @Override
                public <T> T execute(Object o, CsvContext csvContext) {
                    if (o == null) {
                        return (T) "0.00000000";
                    }
                    return (T) decimalFormat.format(o);
                }
            };

            // 时间格式化适配器
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            CellProcessorAdaptor timeCellProcessorAdaptor = new CellProcessorAdaptor() {
                @Override
                public <T> T execute(Object o, CsvContext csvContext) {
                    if (o == null) {
                        return (T) "";
                    }
                    Date date = (Date) o;
                    String format = simpleDateFormat.format(date);
                    return (T) format;
                }
            };

            CellProcessor[] PROCESSOR = new CellProcessor[]{
                    longToStringAdaptor, longToStringAdaptor, null, //"订单ID", "用户ID", "用户名",
                    null, moneyCellProcessorAdaptor, // "币种名称" "充值数量"
                    null, null, timeCellProcessorAdaptor, timeCellProcessorAdaptor, // "收款地址", "交易ID", "充值时间", "完成时间"
            };

            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            try {
                ReportCsvUtils.reportListCsv(requestAttributes.getResponse(),header, properties, "数字货币提现审核.csv", records, PROCESSOR);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
