package com.online_library_service.Service;

import com.online_library_service.dto.MembershipDto;
import com.online_library_service.dto.UserDto;
import com.online_library_service.dto.UserExpiryEvent;
import com.online_library_service.entity.User;
import com.online_library_service.entity.UserBookHistory;
import com.online_library_service.exception.BadRequestException;
import com.online_library_service.exception.ResourceNotFoundException;
import com.online_library_service.kafka.MembershipEventProducer;
import com.online_library_service.repository.UserBookHistoryRepository;
import com.online_library_service.repository.UserRepository;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

	private final UserRepository userRepository;
	private final UserBookHistoryRepository historyRepository;
	private final ModelMapper modelMapper;
	private final MembershipEventProducer membershipEventProducer;

	public List<UserDto> getAllUsers() {
		log.info("Fetching all active users");
		return userRepository.findAll().stream().filter(User::isActive)
				.map(user -> modelMapper.map(user, UserDto.class)).collect(Collectors.toList());
	}

	@Cacheable(value = "users", key = "#id")
	public UserDto getUserById(Long id) {
		log.info("Fetching user by id: {}", id);
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
		if (!user.isActive())
			throw new BadRequestException("User is deactivated");
		return modelMapper.map(user, UserDto.class);
	}

	public List<UserBookHistory> getUserHistory(Long id) {
		log.info("Fetching history for user id: {}", id);
		return historyRepository.findByUserId(id);
	}

	@CachePut(value = "users", key = "#id")
	public UserDto updateUser(Long id, UserDto userDto) {
		log.info("Updating user id: {}", id);
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
		if (!user.isActive())
			throw new BadRequestException("User is deactivated");

		modelMapper.map(userDto, user);
		userRepository.save(user);
		return modelMapper.map(user, UserDto.class);
	}

	@CacheEvict(value = "users", key = "#id")
	public void deleteUser(Long id) {
		log.info("Deactivating user id: {}", id);
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
		user.setActive(false);
		userRepository.save(user);
	}

	@Cacheable(value = "memberships", key = "#id")
	public String checkMembership(Long id) {
		log.info("Checking membership for user id: {}", id);
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
		if (!user.isActive())
			throw new BadRequestException("User is deactivated");

		if (LocalDate.now().isAfter(user.getMembershipEndDate())) {
			log.info("Membership expired for userId={}", id);

			UserExpiryEvent event = UserExpiryEvent.builder().userId(user.getId()).email(user.getEmail())
					.name(user.getName()).membershipEndDate(user.getMembershipEndDate()).reason("MEMBERSHIP_EXPIRED")
					.build();

			membershipEventProducer.publishExpiryEvent(event);

			return "expired";
		}
		return "active";
	}

	@CachePut(value = "users", key = "#id")
	public void extendMembership(Long id, MembershipDto dto) {
		log.info("Extending membership for user id: {}", id);
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
		if (!user.isActive())
			throw new BadRequestException("User is deactivated");

		user.setMembershipEndDate(user.getMembershipEndDate().plusMonths(dto.getMonths()));
		userRepository.save(user);
	}
}
