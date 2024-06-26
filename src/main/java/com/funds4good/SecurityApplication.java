package com.funds4good;

import com.funds4good.Config.AppConstants;
import com.funds4good.Models.Role;
import com.funds4good.Repository.RoleRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import java.util.List;

@SpringBootApplication
@EnableCaching
@EnableAsync
@RequiredArgsConstructor
public class SecurityApplication implements CommandLineRunner {
	private final RoleRepo roleRepo;
	@Bean
	public ModelMapper modelMapper(){
		return new ModelMapper();
	}

	public static void main(String[] args) {
		SpringApplication.run(SecurityApplication.class, args);
	}

	@Override
	public void run(String... args){
		try{
			Role role1 = new Role();
			role1.setId(AppConstants.ROLE_ADMIN);
			role1.setName("ROLE_ADMIN");

			Role role2 = new Role();
			role2.setId(AppConstants.ROLE_NORMAL);
			role2.setName("ROLE_NORMAL");

			Role role3 = new Role();
			role3.setId(AppConstants.ROLE_OTHER);
			role3.setName("ROLE_OTHER");

			List<Role> roles= List.of(role1, role2, role3);
			this.roleRepo.saveAll(roles);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
}
