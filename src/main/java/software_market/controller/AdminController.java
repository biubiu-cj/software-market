package software_market.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import software_market.config.security.UserContext;
import software_market.dto.request.admin.*;
import software_market.service.AdminService;
import software_market.utils.Result;
import software_market.vo.admin.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/adminLogin")
    public Result<Map<String, String>> login(@Valid @RequestBody AdminLoginDTO dto) {
        Map<String, String> result = adminService.login(dto);
        return Result.success(result, "登录成功");
    }

    @GetMapping("/admininfo")
    public Result<AdminInfoVO> info() {
        Integer userId = UserContext.getUserId();
        AdminInfoVO vo = adminService.getAdminInfo(userId);
        return Result.success(vo);
    }

    @PostMapping("/addAdmin")
    public Result<AddAdminVO> addAdmin(@Valid @RequestBody AddAdminDTO dto) {
        AddAdminVO vo = adminService.addAdmin(dto);
        return Result.success(vo, "新增管理员成功");
    }

    @PostMapping("/changePassword")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        adminService.changePassword(dto);
        return Result.success(null, "密码修改成功");
    }

    @PostMapping("/updateAdminInfo")
    public Result<Void> updateAdminInfo(@RequestBody UpdateAdminInfoDTO dto) {
        adminService.updateAdminInfo(dto);
        return Result.success(null, "管理员信息修改成功");
    }

    @PostMapping("/deleteUser")
    public Result<Void> deleteUser(@Valid @RequestBody DeleteUserDTO dto) {
        adminService.deleteUser(dto);
        return Result.success(null, "用户删除成功");
    }

    @PostMapping("/deleteAdmin")
    public Result<Void> deleteAdmin(@Valid @RequestBody DeleteAdminDTO dto) {
        adminService.deleteAdmin(dto);
        return Result.success(null, "管理员删除成功");
    }

    @PostMapping("/showUsers")
    public Result<List<UserVO>> showUsers(@RequestBody ShowUsersDTO dto) {
        List<UserVO> users = adminService.showUsers(dto);
        return Result.success(users);
    }

    @GetMapping("/getSues")
    public List<SueVO> getSues() {
        return adminService.getSues();
    }

    @PostMapping("/noViolation")
    public Result<Void> noViolation(@Valid @RequestBody SueActionDTO dto) {
        adminService.noViolation(dto);
        return Result.success(null, "已标记为无违规");
    }

    @PostMapping("/violation")
    public Result<Void> violation(@Valid @RequestBody SueActionDTO dto) {
        adminService.violation(dto);
        return Result.success(null, "已标记为违规并处罚");
    }

    @PostMapping("/adminPost")
    public Result<Void> adminPost(@Valid @RequestBody AdminPostDTO dto) {
        adminService.adminPost(dto);
        return Result.success(null, "发帖成功");
    }
}
