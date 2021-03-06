package top.goodz.future.assess.controller.system;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import top.goodz.future.response.CommonResponse;

/**
 * @Description 注册 api
 * @Author Yajun.Zhang
 * @Date 2020/8/10 22:10
 */

@RestController
@Api(tags = "后管用户注册功能api")
public class SysRegisterController {


    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    @ResponseBody
    public CommonResponse ajaxRegister(String loginName, String password) {

        System.out.println("dssg" + loginName + "p[ass" + password);
        return CommonResponse.isSuccess();
    }
}
