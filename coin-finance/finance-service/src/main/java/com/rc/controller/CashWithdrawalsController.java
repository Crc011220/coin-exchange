package com.rc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.CashRecharge;
import com.rc.domain.CashWithdrawAuditRecord;
import com.rc.domain.CashWithdrawals;
import com.rc.model.CashSellParam;
import com.rc.model.R;
import com.rc.service.CashWithdrawalsService;
import com.rc.util.ReportCsvUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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
@RequestMapping("/cashWithdrawals")
@Api(tags = "提现记录的控制器")
public class CashWithdrawalsController {

    @Autowired
    private CashWithdrawalsService cashWithdrawalsService;

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
    public R<Page<CashWithdrawals>> findByPage(
            @ApiIgnore Page<CashWithdrawals> page,
            Long userId, String userName,
            String mobile, Byte status, String numMin, String numMax,
            String startTime, String endTime
    ){
        Page<CashWithdrawals> pageData = cashWithdrawalsService.findByPage(page, userId, userName, mobile, status, numMin, numMax, startTime, endTime);
        return R.ok(pageData);
    }

    @GetMapping("exportCNYWithDrawals")
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
            Long userId, String userName,
            String mobile, Byte status, String numMin, String numMax,
            String startTime, String endTime
    ){
        Page<CashWithdrawals> cashWithdrawalsPage = new Page<>(1, 10000);
        Page<CashWithdrawals> pageData = cashWithdrawalsService.findByPage(cashWithdrawalsPage, userId, userName, mobile, status, numMin, numMax, startTime, endTime);
        List<CashWithdrawals> records = pageData.getRecords();
        if (!CollectionUtils.isEmpty(records)){
            String[] header = {"ID", "用户ID", "用户名", "提现金额(USDT)", "手续费", "到账金额(CNY)", "提现开户名", "银行名称", "账号", "申请时间", "完成时间", "状态", "审核备注", "审核级数"};
            String[] properties = {"id", "userId", "userName", "num", "fee", "num", "truename", "bank", "bankCard", "created", "lastTime", "status", "remark", "step"};


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
                            statusName = "提现成功";
                            break;
                        default:
                            statusName = "未知";
                            break;
                    }
                    return (T) statusName;
                }
            };


            CellProcessor[] PROCESSOR = new CellProcessor[]{
                    longToStringAdaptor, longToStringAdaptor, null, //"ID", "用户ID", "用户名"
                    moneyCellProcessorAdaptor, moneyCellProcessorAdaptor, moneyCellProcessorAdaptor, //"提现金额(USDT)", "手续费", "到账金额(CNY)"
                    null, null, null, //"提现开户名", "银行名称", "账号"
                    timeCellProcessorAdaptor, timeCellProcessorAdaptor, //"申请时间", "完成时间"
                    statusProcessorAdaptor, null, //"状态", "审核备注"
                    null //"审核级数"
            };
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            try {
                ReportCsvUtils.reportListCsv(requestAttributes.getResponse(),header, properties, "场外交易提现审核.csv", records, PROCESSOR);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    @PostMapping("/updateWithdrawalsStatus")
    @ApiOperation(value = "更新提现状态")
    public R updateWithdrawalsStatus(@RequestBody CashWithdrawAuditRecord cashWithdrawAuditRecord){
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());

        boolean isOk = cashWithdrawalsService.updateWithdrawalsStatus(cashWithdrawAuditRecord, userId);
        if (!isOk){
            return R.fail("审核失败");
        }
        return R.ok();
    }

    @GetMapping("/user/records")
    @ApiOperation(value = "查询当前用户的提现记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current",value = "当前页") ,
            @ApiImplicitParam(name = "size",value = "每页显示的大小") ,
            @ApiImplicitParam(name = "status",value = "充值的状态") ,
    })
    public R<Page<CashWithdrawals>> findUserCashRecharge(@ApiIgnore Page<CashWithdrawals> page ,Byte status){
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()) ;
        Page<CashWithdrawals> cashWithdrawPage = cashWithdrawalsService.findUserCashWithdraw(page ,userId,status) ;
        return R.ok(cashWithdrawPage) ;
    }

    @PostMapping("/sell")
    @ApiOperation(value = "GCN的卖出操作")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cashSellParam", value = "cashSell的参数")
    })
    public R<Object> sell(@RequestBody @Validated CashSellParam cashSellParam) {
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        boolean isOk = cashWithdrawalsService.sell(userId, cashSellParam);
        if (isOk) {
            return R.ok("提交申请成功");
        }
        return R.fail("提交申请失败");
    }





}
