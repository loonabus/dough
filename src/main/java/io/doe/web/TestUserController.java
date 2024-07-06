package io.doe.web;

import io.doe.domain.BaseRes;
import io.doe.domain.TestReq;
import io.doe.domain.TestRes;
import io.doe.domain.TestUserMapping;
import io.doe.service.TestUserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping(value="/dough")
public class TestUserController {

	private final TestUserMapping mapping;
	private final TestUserService<TestRes.TestUser> service;

	@Autowired
	public TestUserController(final TestUserMapping mapping, final TestUserService<TestRes.TestUser> service) {
		this.mapping = mapping;
		this.service = service;
	}

	@GetMapping(value="/user")
	@Operation(description="search users")
	public BaseRes<List<TestRes.TestUser>> search() {
		return BaseRes.success(service.retrieve());
	}

	@PostMapping(value="/user")
	@Operation(description="create & retrieve user")
	public BaseRes<TestRes.TestUser> create(@RequestBody @Valid final TestReq.TestUserReq req) {
		return BaseRes.success(service.create(mapping.from(req)));
	}
}
