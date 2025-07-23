package gift.service.user;

import gift.common.mapper.ModelMapper;
import gift.common.util.PasswordEncoder;
import gift.entity.User;
import gift.common.model.CustomPage;
import gift.repository.user.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public CustomPage<User> findAllBy(Pageable pageable) {
        return ModelMapper.toCustomPage(userRepository.findAllBy(pageable));
    }

    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 ID의 사용자를 찾을 수 없습니다. : " + userId));
    }
    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일의 사용자를 찾을 수 없습니다. : " + email));
    }

    @Override
    @Transactional
    public User create(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateKeyException("이미 존재하는 이메일입니다: " + user.getEmail());
        }
        if (user.getRoles().isEmpty()) {
            throw new IllegalArgumentException("사용자 역할은 최소 하나 이상이어야 합니다.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User update(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("업데이트할 사용자 정보가 유효하지 않습니다.");
        }
        User existingUser = findById(user.getId());
        // 비밀번호 업데이트
        if (user.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // 이메일 업데이트
        if (user.getEmail() != null) {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new DuplicateKeyException("이미 존재하는 이메일입니다: " + user.getEmail());
            }
            existingUser.setEmail(user.getEmail());
        }

        // 역할 업데이트
        if (user.getRoles() != null) {
            existingUser.setRoles(user.getUserRoles());
        }
        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteById(Long userId) {
        findById(userId); // user가 존재하는지 확인
        userRepository.deleteById(userId);
    }

    @Override
    public Boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public User getReference(Long userId) {
        return userRepository.getReferenceById(userId);
    }
}
