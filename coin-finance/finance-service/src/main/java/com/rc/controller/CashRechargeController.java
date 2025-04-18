package com.rc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.CashRecharge;
import com.rc.domain.CashRechargeAuditRecord;
import com.rc.model.CashParam;
import com.rc.model.R;
import com.rc.service.CashRechargeService;
import com.rc.util.ReportCsvUtils;
import com.rc.vo.CashTradeVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
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
@RequestMapping("/cashRecharges")
@Api(tags = "GCN充值控制器")
public class CashRechargeController {


    @Autowired
    private CashRechargeService cashRechargeService;

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
    public R<Page<CashRecharge>> findByPage(
            @ApiIgnore Page<CashRecharge> page,
            Long coinId, Long userId, String userName,
            String mobile, Byte status, String numMin, String numMax,
            String startTime, String endTime
    ) {
        Page<CashRecharge> pageData = cashRechargeService.findByPage(page, coinId, userId, userName, mobile, status, numMin, numMax, startTime, endTime);
        return R.ok(pageData);
    }

    //http://localhost:9527/finance/cashRecharge/exportCNYRecharge
    @GetMapping("/exportCNYRecharge")
    @ApiOperation(value = "导出gcn充值csv")
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
    public void recordsExport(Long coinId, Long userId, String userName,
                              String mobile, Byte status, String numMin, String numMax,
                              String startTime, String endTime){
        Page<CashRecharge> cashRechargePage = new Page<>(1, 10000);
        Page<CashRecharge> pageData = cashRechargeService.findByPage(cashRechargePage, coinId, userId, userName, mobile, status, numMin, numMax, startTime, endTime);
        List<CashRecharge> records = pageData.getRecords();
        if (!CollectionUtils.isEmpty(records)){
            String[] header = {"ID", "用户ID", "用户名", "真实用户名", "充值币种", "充值金额(USDT)", "手续费", "到账金额", "充值方式", "充值订单", "参考号", "充值时间", "完成时间", "状态", "审核备注", "审核级数"};
            String[] properties = {"id", "userId", "userName", "realName", "coinName", "num", "fee", "mum", "type", "tradeno", "remark", "created", "lastTime", "status", "auditRemark", "step"};

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

            // 类型转换适配器
            CellProcessorAdaptor typeProcessorAdaptor = new CellProcessorAdaptor() {
                @Override
                public <T> T execute(Object o, CsvContext csvContext) {
                    if (o == null) {
                        return (T) "未知";
                    }
                    String type = String.valueOf(o);
                    String typeName = "";
                    switch (type) {
                        case "alipay":
                            typeName = "支付宝";
                            break;
                        case "cai1pay":
                            typeName = "财易付";
                            break;
                        case "bank":
                            typeName = "银联";
                            break;
                        case "linepay":
                            typeName = "在线支付";
                            break;
                        default:
                            typeName = "未知";
                            break;
                    }
                    return (T) typeName;
                }
            };

            // 状态转换适配器
            CellProcessorAdaptor statusProcessorAdaptor = new CellProcessorAdaptor() {
                @Override
                public <T> T execute(Object o, CsvContext csvContext) {
                    if (o == null) {
                        return (T) "未知";
                    }
                    String status = String.valueOf(o);
                    String statusName = "";
                    switch (status) {
                        case "0":
                            statusName = "待审核";
                            break;
                        case "1":
                            statusName = "审核通过";
                            break;
                        case "2":
                            statusName = "拒绝";
                            break;
                        case "3":
                            statusName = "充值成功";
                            break;
                        default:
                            statusName = "未知";
                            break;
                    }
                    return (T) statusName;
                }
            };

            CellProcessor[] PROCESSOR = new CellProcessor[]{
                    longToStringAdaptor, longToStringAdaptor, null, null, null, //"ID", "用户ID", "用户名", "真实用户名", "充值币种"
                    moneyCellProcessorAdaptor, moneyCellProcessorAdaptor, moneyCellProcessorAdaptor,typeProcessorAdaptor,//"充值金额(USDT)", "手续费", "到账金额", "充值方式"
                    null, null, timeCellProcessorAdaptor, timeCellProcessorAdaptor, statusProcessorAdaptor, //"充值订单", "参考号", "充值时间", "完成时间", "状态"
                    null, null //"审核备注", "审核级数"
            };

            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            try {
                ReportCsvUtils.reportListCsv(requestAttributes.getResponse(),header, properties, "场外交易充值审核.csv", records, PROCESSOR);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @PostMapping("/cashRechargeUpdateStatus")
    @ApiOperation(value = "现金充值审核")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "充值记录的ID"),
            @ApiImplicitParam(name = "status", value = "充值审核状态"),
            @ApiImplicitParam(name = "remark", value = "审核备注"),
    })
    public R cashRechargeUpdateStatus(@RequestBody CashRechargeAuditRecord cashRechargeAuditRecord){
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString(); //获取当前登录用户，审核人
        boolean isOk = cashRechargeService.cashRechargeAudit(Long.valueOf(userId), cashRechargeAuditRecord);
        if (isOk){
            return R.ok();
        }else {
            return R.fail("审核失败");
        }
    }

    @GetMapping("/user/records")
    @ApiOperation(value = "查询当前用户的充值记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current",value = "当前页") ,
            @ApiImplicitParam(name = "size",value = "每页显示的大小") ,
            @ApiImplicitParam(name = "status",value = "充值的状态") ,
    })
    public R<Page<CashRecharge>> findUserCashRecharge(@ApiIgnore Page<CashRecharge> page ,Byte status){
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()) ;
        Page<CashRecharge> cashRechargePage = cashRechargeService.findUserCashRecharge(page ,userId,status) ;
        return R.ok(cashRechargePage) ;
    }

    @PostMapping("/buy")
    @ApiOperation(value = "GCN的充值操作")
    @ApiImplicitParams({
            @ApiImplicitParam( name = "cashParam",value = "现金交易的参数")
    })
    public R<CashTradeVo> buy(@RequestBody @Validated CashParam cashParam){
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()) ;
        CashTradeVo cashTradeVo = cashRechargeService.buy(userId,cashParam) ;
        return R.ok(cashTradeVo) ;
    }


}

