package gov.samhsa.c2s.ums.web;

import gov.samhsa.c2s.ums.service.UserAvatarService;
import gov.samhsa.c2s.ums.service.dto.AvatarBytesAndMetaDto;
import gov.samhsa.c2s.ums.service.dto.UserAvatarDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user-avatars")
public class UserAvatarRestController {
    private final UserAvatarService userAvatarService;

    @Autowired
    public UserAvatarRestController(UserAvatarService userAvatarService) {
        this.userAvatarService = userAvatarService;
    }

    @GetMapping("/user/{userId}/avatar")
    public UserAvatarDto getUserAvatar(@PathVariable Long userId) {
        return userAvatarService.getUserAvatarByUserId(userId);
    }

    @PostMapping("/user/{userId}/avatar")
    public UserAvatarDto saveNewUserAvatar(
            @PathVariable Long userId,
            @RequestBody AvatarBytesAndMetaDto avatarFile
    ) {
        return userAvatarService.saveUserAvatar(userId, avatarFile);
    }

    @DeleteMapping("/user/{userId}/avatar")
    public void deleteUserAvatar(@PathVariable Long userId) {
        userAvatarService.deleteUserAvatar(userId);
    }
}
