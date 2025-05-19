package com.certificate.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.certificate.common.constant.Constants;
import com.certificate.entity.User;
import com.certificate.service.UserService;
import com.certificate.util.JwtUtil;
import com.certificate.vo.ResponseVO;
import com.certificate.vo.user.OrgUserCreateVO;
import com.certificate.vo.user.OrgUserUpdateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/org/users")
public class OrgUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取机构用户列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param keyword 关键字
     * @param request HTTP请求
     * @return 用户列表
     */
    @GetMapping
    public ResponseVO<IPage<User>> getOrgUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {

        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        // 创建分页对象
        Page<User> page = new Page<>(pageNum, pageSize);

        // 查询用户列表
        IPage<User> userPage = userService.getOrgUserList(orgId, keyword, page);

        // 处理敏感信息
        for (User user : userPage.getRecords()) {
            user.setPassword(null);
        }

        return ResponseVO.success("获取用户列表成功", userPage);
    }

    /**
     * 创建机构用户
     * @param createVO 创建信息
     * @param request HTTP请求
     * @return 创建结果
     */
    @PostMapping
    public ResponseVO<Boolean> createOrgUser(
            @RequestBody @Valid OrgUserCreateVO createVO,
            HttpServletRequest request) {

        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        try {
            boolean result = userService.createOrgUser(createVO, orgId);
            return result ?
                    ResponseVO.success("创建用户成功", true) :
                    ResponseVO.error("创建用户失败");
        } catch (Exception e) {
            return ResponseVO.error(e.getMessage());
        }
    }

    /**
     * 获取用户详情
     * @param id 用户ID
     * @param request HTTP请求
     * @return 用户详情
     */
    @GetMapping("/{id}")
    public ResponseVO<User> getOrgUserDetail(
            @PathVariable Long id,
            HttpServletRequest request) {

        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        // 获取用户
        User user = userService.getById(id);
        if (user == null) {
            return ResponseVO.error("用户不存在");
        }

        // 验证用户是否属于当前机构
        if (!user.getOrgId().equals(orgId)) {
            return ResponseVO.error("无权查看此用户");
        }

        // 处理敏感信息
        user.setPassword(null);

        return ResponseVO.success("获取用户详情成功", user);
    }

    /**
     * 更新用户信息
     * @param id 用户ID
     * @param updateVO 更新信息
     * @param request HTTP请求
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseVO<Boolean> updateOrgUser(
            @PathVariable Long id,
            @RequestBody @Valid OrgUserUpdateVO updateVO,
            HttpServletRequest request) {

        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        try {
            boolean result = userService.updateOrgUser(id, updateVO, orgId);
            return result ?
                    ResponseVO.success("更新用户成功", true) :
                    ResponseVO.error("更新用户失败");
        } catch (Exception e) {
            return ResponseVO.error(e.getMessage());
        }
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseVO<Boolean> deleteOrgUser(
            @PathVariable Long id,
            HttpServletRequest request) {

        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        // 获取用户
        User user = userService.getById(id);
        if (user == null) {
            return ResponseVO.error("用户不存在");
        }

        // 验证用户是否属于当前机构
        if (!user.getOrgId().equals(orgId)) {
            return ResponseVO.error("无权删除此用户");
        }

        // 删除用户
        boolean result = userService.removeById(id);

        return result ?
                ResponseVO.success("删除用户成功", true) :
                ResponseVO.error("删除用户失败");
    }

    /**
     * 修改用户状态
     * @param id 用户ID
     * @param status 状态
     * @param request HTTP请求
     * @return 修改结果
     */
    @PostMapping("/{id}/status")
    public ResponseVO<Boolean> updateOrgUserStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            HttpServletRequest request) {

        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        // 验证状态参数
        if (status != Constants.UserStatus.ENABLED && status != Constants.UserStatus.DISABLED) {
            return ResponseVO.error("无效的状态参数");
        }

        try {
            boolean result = userService.updateUserStatus(id, status, orgId);
            String statusText = status == Constants.UserStatus.ENABLED ? "启用" : "禁用";
            return result ?
                    ResponseVO.success(statusText + "用户成功", true) :
                    ResponseVO.error(statusText + "用户失败");
        } catch (Exception e) {
            return ResponseVO.error(e.getMessage());
        }
    }

    /**
     * 获取机构下的有效用户列表(简化版)
     * @param request HTTP请求
     * @return 用户列表
     */
    @GetMapping("/valid")
    public ResponseVO<List<User>> getValidUsers(HttpServletRequest request) {
        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        // 查询该机构下的有效用户
        List<User> users = userService.list(
                new LambdaQueryWrapper<User>()
                        .eq(User::getOrgId, orgId)
                        .eq(User::getStatus, Constants.UserStatus.ENABLED)
                        .orderByDesc(User::getCreateTime)
        );

        // 处理敏感信息
        for (User user : users) {
            user.setPassword(null);
        }

        return ResponseVO.success("获取有效用户列表成功", users);
    }
}