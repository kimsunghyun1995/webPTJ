package com.ssafy.nnd.controller;

import java.util.Optional;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.nnd.dto.BasicResponse;
import com.ssafy.nnd.dto.LoginVo;
import com.ssafy.nnd.dto.Member;
import com.ssafy.nnd.dto.MemberBoard;
import com.ssafy.nnd.dto.SignupRequest;
import com.ssafy.nnd.repository.MemberRepository;
import com.ssafy.nnd.service.JwtService;

@CrossOrigin
@RestController
public class MemberController {

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private ModelMapper modelMapper;

	@GetMapping("/member/init")
	public LoginVo getUser() {
		System.out.println("!!!!!!!!!!");
		long memberId = jwtService.getMemberId();
		System.out.println(memberId);
		Optional<Member> loginMember = memberRepository.findMemberByIdx(memberId);
		System.out.println(loginMember.get().toString());
		return modelMapper.map(loginMember.get(), LoginVo.class);
	}

	@PostMapping("/member/login")
	public Object login(@RequestBody Member member) {

		String email = member.getEmail();
		String password = member.getPassword();

		Optional<Member> memberOpt = memberRepository.findMemberByEmailAndPassword(email, password);

		System.out.println("memberOpt: " + memberOpt.toString());

		ResponseEntity response = null;

		if (memberOpt.isPresent()) {
			System.out.println("suces");
			Member loginmember = memberRepository.findByEmail(email);
			String token = jwtService.create("member", loginmember, "user");
			System.out.println(token);
			final BasicResponse result = new BasicResponse();
			result.status = true;
			result.data = token;
			response = new ResponseEntity<>(result, HttpStatus.OK);
		} else { //로그인 성공
			System.out.println("fail");
			response = new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
			//추가 사항 비밀번호 아이디 중에 뭐가 틀렸는지 알려주기
		}
		return response;
	}


	@PostMapping("/member/signup")
	public String signup(@Valid @RequestBody SignupRequest request) {

		//		 try {
		//	            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		//	            user.setPassword(passwordEncoder.encode(user.getPassword()));
		//	            userRepository.save(user);
		//	        } catch(Exception e) {
		//	            return "failed";
		//	        }
		//	        return "success";
		//		

		Optional<Member> memberOpt = memberRepository.findMemberByEmail(request.getEmail());
		Member member= new Member(request.getName(),request.getEmail(), request.getPassword(), request.getPassword(), request.getProfile(),"null",request.getGitaddr(),request.getMemberstack());

		ResponseEntity response = null;

		if (memberOpt.isPresent()) { //  있는 회원입니다.
			System.out.println("exist!!!");
			response = new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		} else {
			System.out.println("suces");
			memberRepository.save(member);
			final BasicResponse result = new BasicResponse();
			result.status = true;
			result.data = "success";
			response = new ResponseEntity<>(result, HttpStatus.OK);
		}

		return "!!";
	}

}


