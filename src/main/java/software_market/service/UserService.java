package software_market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;
import software_market.dto.request.user.*;
import software_market.dto.response.TokenPair;
import software_market.entity.User;
import software_market.vo.user.*;


public interface UserService extends IService<User> {
    TokenPair register(RegisterDTO registerDTO);

    void validateEmail(ValidateEmailDTO validateEmailDTO);

    TokenPair login(LoginDTO loginDTO);

    void identityValidate(@Valid IdentityValidateDTO identityValidateDTO);

    CurrentUserVO getCurrentUserInfo(Integer userId);

    TokenPair refresh(RefreshTokenDTO refreshTokenDTO);

    void modifyPassword(@Valid ModifyPasswordDTO modifyPasswordDTO);

    User getUserInfo(GetUserInfoDTO getUserInfoDTO);

    void updateUserInfo(UpdateUserInfoDTO updateUserInfoDTO);

    void changeEmailPush(ChangeEmailPushDTO changeEmailPushDTO);

    AvatarVO getAvatar(GetAvatarDTO getAvatarDTO);

    UploadAvatarVO uploadAvatar(UploadAvatarDTO uploadAvatarDTO);

    UpdateAvatarVO updateAvatar(UpdateAvatarDTO updateAvatarDTO);

    void deleteMe(DeleteMeDTO deleteMeDTO);

    UserStatisticsVO statistics();
}
