package software_market.service;

import software_market.dto.request.admin.*;
import software_market.vo.admin.*;

import java.util.List;
import java.util.Map;

public interface AdminService {
    Map<String, String> login(AdminLoginDTO adminLoginDTO);

    AdminInfoVO getAdminInfo(Integer userId);

    AddAdminVO addAdmin(AddAdminDTO dto);

    void changePassword(ChangePasswordDTO dto);

    void updateAdminInfo(UpdateAdminInfoDTO dto);

    void deleteUser(DeleteUserDTO dto);

    void deleteAdmin(DeleteAdminDTO dto);

    List<UserVO> showUsers(ShowUsersDTO dto);

    List<SueVO> getSues();

    void noViolation(SueActionDTO dto);

    void violation(SueActionDTO dto);

    void adminPost(AdminPostDTO dto);
}
