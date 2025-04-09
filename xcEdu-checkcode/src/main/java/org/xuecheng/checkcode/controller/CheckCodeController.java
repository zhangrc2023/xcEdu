package org.xuecheng.checkcode.controller;

import org.xuecheng.model.RestResponse;
import org.xuecheng.checkcode.model.CheckCodeParamsDto;
import org.xuecheng.checkcode.model.CheckCodeResultDto;
import org.xuecheng.checkcode.service.CheckCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

/**
 * @description 验证码服务接口
 * @date 2022/9/29 18:39
 */
@Api(value = "验证码服务接口")
@RestController
public class CheckCodeController {

    @Resource(name = "PicCheckCodeService")
    private CheckCodeService picCheckCodeService;


    @ApiOperation(value = "生成验证信息", notes = "生成验证信息")
    @PostMapping(value = "/pic")
    public CheckCodeResultDto generatePicCheckCode(CheckCodeParamsDto checkCodeParamsDto) {
        return picCheckCodeService.generate(checkCodeParamsDto);
    }

    @ApiOperation(value = "校验", notes = "校验")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "业务名称", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "key", value = "验证key", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "code", value = "验证码", required = true, dataType = "String", paramType = "query")
    })
    @PostMapping(value = "/verify")
    public Boolean verify(String key, String code) {
        Boolean isSuccess = picCheckCodeService.verify(key, code);
        return isSuccess;
    }
}
