package com.seventeen.controller;


import com.seventeen.bean.core.SysUser;
import com.seventeen.core.Result;
import com.seventeen.core.ResultGenerator;
import com.seventeen.mapper.SysUserMapper;
import com.seventeen.service.impl.SysUserService;
import com.seventeen.util.IDGenerator;
import com.seventeen.util.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/sys/users")
@Api(tags = "用户管理")
public class SysUserController {
	@Autowired
	private SysUserService sysUserService;

	@Autowired
	private SysUserMapper SysUsermapper;

	@PostMapping
	@ApiOperation("新增用户")
	@ApiImplicitParam(name = "Authorization", value = "Bearer token", paramType = "header", required = true, defaultValue = "Bearer ")
	@CacheEvict(value = "sysUserList", allEntries = true)
	public Result<String> add(@RequestBody SysUser sysUser) {
		sysUser.setId(IDGenerator.getId());
		sysUser.setCreateDate(new Date());
		if (StringUtils.hasText(sysUser.getPassword())) {
			PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
		}
		sysUserService.insert(sysUser);
		return ResultGenerator.genSuccessResult("保存成功。");
	}


	@DeleteMapping("/{userId}")
	@ApiOperation("根据用户id删除用户")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Authorization", value = "Bearer token", paramType = "header", required = true, defaultValue = "Bearer "),
			@ApiImplicitParam(name = "userId", value = "用户id", paramType = "path", required = true) })
	@Caching(evict = { @CacheEvict(value = "sysUser", key = "#userId"),
			@CacheEvict(value = "sysUserList", allEntries = true),
			@CacheEvict(value = "sysRoleList", allEntries = true),
			@CacheEvict(value = "sysAuthorityList", allEntries = true),
			@CacheEvict(value = "sysUserDetails", allEntries = true) })
	public Result<String> delete(@PathVariable String userId) {
		int count = sysUserService.deleteById(userId);
		return ResultGenerator.genSuccessResult("删除成功" + count + "条数据。");
	}

	@DeleteMapping
	@ApiOperation("根据用户id列表删除用户")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Authorization", value = "Bearer token", paramType = "header", required = true, defaultValue = "Bearer "),
			@ApiImplicitParam(name = "userIds", value = "用户id列表，格式为1,2,3", paramType = "query", required = true) })
	@Caching(evict = { @CacheEvict(value = "sysUser", allEntries = true),
			@CacheEvict(value = "sysUserList", allEntries = true),
			@CacheEvict(value = "sysRoleList", allEntries = true),
			@CacheEvict(value = "sysAuthorityList", allEntries = true),
			@CacheEvict(value = "sysUserDetails", allEntries = true) })
	public Result<String> deleteList(@RequestParam String userIds) {
		int count = sysUserService.deleteByIds(userIds);
		return ResultGenerator.genSuccessResult("删除成功" + count + "条数据。");
	}

	@PutMapping("/{userId}")
	@ApiOperation("根据用户id修改用户信息。")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Authorization", value = "Bearer token", paramType = "header", required = true, defaultValue = "Bearer "),
			@ApiImplicitParam(name = "userId", value = "用户id", paramType = "path", required = true) })
	@Caching(evict = { @CacheEvict(value = "sysUser", key = "#userId"),
			@CacheEvict(value = "sysUserList", allEntries = true),
			@CacheEvict(value = "sysRoleList", allEntries = true),
			@CacheEvict(value = "sysAuthorityList", allEntries = true),
			@CacheEvict(value = "sysUserDetails", allEntries = true) })
	public Result<String> update(@RequestBody SysUser sysUser, @PathVariable String userId) {
		SysUser oldSysUser = sysUserService.findById(userId);
		if (oldSysUser != null) {
			sysUser.setId(userId);
			if (StringUtils.hasText(sysUser.getPassword())) {
				String oldPassword = oldSysUser.getPassword();
				PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
				String newPassword = passwordEncoder.encode(sysUser.getPassword());
				if (!oldPassword.equals(newPassword)) {// 新旧密码不一致，则更新密码最后更新时间，用作密码修改token跟着过期操作
					sysUser.setLastPasswordResetDate(new Date());
				}
				sysUser.setPassword(newPassword);
			} else {// 设为空值不更新密码
				sysUser.setPassword(null);
			}
			sysUser.setModifyDate(new Date());
			sysUserService.update(sysUser);
		}
		return ResultGenerator.genSuccessResult("更新成功。");
	}

	@GetMapping("/all")
	@ApiOperation("查询所有用户列表")
	@ApiImplicitParam(name = "Authorization", value = "Bearer token", paramType = "header", required = true, defaultValue = "Bearer ")
	@Cacheable(value = "sysUserList")
	public Result<List<SysUser>> findList() {
		return ResultGenerator.genSuccessResult(sysUserService.findAll());
	}

	@GetMapping
	@ApiOperation("分页查询用户列表")
	@ApiImplicitParam(name = "Authorization", value = "Bearer token", paramType = "header", required = true, defaultValue = "Bearer ")
	@Cacheable(value = "sysUserList")
	public Result<List<SysUser>> findList(PageInfo pageInfo) {
		return ResultGenerator.genSuccessResult(new Result(sysUserService.findAll(pageInfo),pageInfo));
	}

	@GetMapping("{userId}")
	@ApiOperation("根据用户id查询用户信息")
	@ApiImplicitParam(name = "Authorization", value = "Bearer token", paramType = "header", required = true, defaultValue = "Bearer ")
//	@Cacheable(value = "sysUser", key = "#userId")
//	@MyDs(value = "slaveDataSource")
//	@PreAuthorize("hasRole('sys_user')")
	public Result<SysUser> findOne(@PathVariable String userId) {
		return ResultGenerator.genSuccessResult(SysUsermapper.selectElementsByIds(userId));
	}

	@GetMapping("clear")
	@ApiOperation("清除所有用户缓存")
	@Caching(evict = { @CacheEvict(value = "sysUser", allEntries = true),
			@CacheEvict(value = "sysUserList", allEntries = true) })
	public Result<String> clearCache() {
		return ResultGenerator.genSuccessResult("用户缓存清除成功。");
	}
}
