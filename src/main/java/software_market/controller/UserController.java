package software_market.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import software_market.config.security.UserContext;
import software_market.dto.request.user.*;
import software_market.dto.response.TokenPair;
import software_market.entity.User;
import software_market.service.UserService;
import software_market.utils.Result;
import software_market.vo.user.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    //================ 无需登录 ===============
    /**
     * 用户注册
     * @param registerDTO
     * @return
     */

    @PostMapping("/register")
    public Result<TokenPair> register(@RequestBody RegisterDTO registerDTO) {
        TokenPair pair=userService.register(registerDTO);
        return Result.success(pair,"注册成功");
    }

    /**
     * 发送验证码
     */
    @PostMapping("/validateEmail")
    public Result validateEmail(@RequestBody ValidateEmailDTO validateEmailDTO) {
        userService.validateEmail(validateEmailDTO);
        return Result.success("验证码已发送");
    }


    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<TokenPair> login(@RequestBody LoginDTO loginDTO) {
        TokenPair pair=userService.login(loginDTO);
        return Result.success(pair,"登录成功");
    }

    /**
     * 刷新 token
     */
    @PostMapping("/refresh")
    public Result<TokenPair> refresh(@RequestBody RefreshTokenDTO refreshTokenDTO) {
        TokenPair pair=userService.refresh(refreshTokenDTO);
        return Result.success(pair,"刷新成功");
    }

    /**
     * 修改密码
     */
    @PostMapping("/modifyPassword")
    public Result modifyPassword(@Valid @RequestBody ModifyPasswordDTO modifyPasswordDTO) {
        userService.modifyPassword(modifyPasswordDTO);
        return Result.success(null, "修改密码成功");
    }


    //================ 需要登录 =============

    /**
     * 用户身份验证
     * @param identityValidateDTO
     * @return
     */
    @PostMapping("/identityValidate")
    public Result identityValidate(@Valid @RequestBody IdentityValidateDTO identityValidateDTO) {
        userService.identityValidate(identityValidateDTO);
        return Result.success(null, "身份验证成功");
    }

    /**
     * 获取当前用户信息
     * @return
     */


    @GetMapping("/info")
    public Result<CurrentUserVO> info() {
        Integer userId = UserContext.getUserId();
        CurrentUserVO user = userService.getCurrentUserInfo(userId);
        return Result.success(user);
    }

    /**
     * 按照phone或者id获取用户信息
     * @param getUserInfoDTO
     * @return
     */

    @PostMapping("/getInfo")
    public User getInfo(@RequestBody GetUserInfoDTO getUserInfoDTO) {
        return userService.getUserInfo(getUserInfoDTO);
    }

    /**
     * 修改用户信息
     * @param updateUserInfoDTO
     * @return
     */
    @PostMapping("/updateUserInfo")
    public Result updateUserInfo(@RequestBody UpdateUserInfoDTO updateUserInfoDTO) {
        userService.updateUserInfo(updateUserInfoDTO);
        return Result.success(null, "修改用户信息成功");
    }

    /**
     * 修改是否接受邮箱推送
     * @param changeEmailPushDTO
     * @return
     */


    @PostMapping("/changeEmailPush")
    public Result changeEmailPush(@RequestBody ChangeEmailPushDTO changeEmailPushDTO) {
        userService.changeEmailPush(changeEmailPushDTO);
        return Result.success(null, "修改邮箱成功");
    }

    /**
     * 获取用户头像
     * @param getAvatarDTO
     * @return
     */

    @PostMapping("/getAvatar")
    public Result<AvatarVO>getAvatar(@RequestBody GetAvatarDTO getAvatarDTO) {
         AvatarVO avatarVO=userService.getAvatar(getAvatarDTO);
         return Result.success(avatarVO,"获取头像成功");
    }


    /**
     * 上传用户头像
     * @param uploadAvatarDTO
     * @return
     */

    @PostMapping("/uploadAvatar")
    public Result<UploadAvatarVO> uploadAvatar(UploadAvatarDTO uploadAvatarDTO) {
        UploadAvatarVO uploadAvatarVO=userService.uploadAvatar(uploadAvatarDTO);
        return Result.success(uploadAvatarVO, "上传头像成功");
    }

    /**
     * 修改用户头像
     * @param updateAvatarDTO
     * @return
     */
    @PostMapping("/updateAvatar")
    public Result<UpdateAvatarVO> updateAvatar(UpdateAvatarDTO updateAvatarDTO) {
        UpdateAvatarVO updateAvatarVO = userService.updateAvatar(updateAvatarDTO);
        return Result.success(updateAvatarVO, "修改头像成功");
    }


    /**
     * 用户注销
     * @param deleteMeDTO
     * @return
     */
    @PostMapping("/deleteMe")
    public Result deleteMe(DeleteMeDTO deleteMeDTO) {
        userService.deleteMe(deleteMeDTO);
        return Result.success(null, "删除用户成功");
    }


    @PostMapping("/statistics")
    public Result<UserStatisticsVO> statistics() {
        UserStatisticsVO userStatisticsVO=userService.statistics();
        return Result.success(userStatisticsVO);
    }
}
