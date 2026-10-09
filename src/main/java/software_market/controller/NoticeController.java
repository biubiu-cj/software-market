package software_market.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import software_market.dto.request.notice.NoticeGetDTO;
import software_market.service.NoticeService;
import software_market.utils.Result;
import software_market.vo.notice.GetNoticeNumVO;
import software_market.vo.notice.NoticeGetVO;
import software_market.vo.notice.ReadNoticeVO;

@RequestMapping("/api/auth/")
@RequiredArgsConstructor
@RestController
public class NoticeController {

    private final NoticeService noticeService;
    @GetMapping("/getNotice")
    public Result<NoticeGetVO> getNotice(NoticeGetDTO noticeGetDTO) {
        NoticeGetVO noticeGetVO = noticeService.getNotice(noticeGetDTO);
        return Result.success(noticeGetVO, "获取成功");
    }


    @GetMapping("/getNoticeNum")
    public Result<GetNoticeNumVO> getNoticeNum() {
        GetNoticeNumVO getNoticeNumVO = noticeService.getNoticeNum();
        return Result.success(getNoticeNumVO, "获取成功");
    }

    @PatchMapping("/readNotice/{noticeId}")
    public Result<ReadNoticeVO> readNotice(@PathVariable Long noticeId) {
        ReadNoticeVO readNoticeVO = noticeService.readNotice(noticeId);
        return Result.success(readNoticeVO, "获取成功");
    }
}
