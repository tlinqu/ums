package gov.samhsa.c2s.ums.service;

import gov.samhsa.c2s.ums.config.EmailSenderProperties;
import gov.samhsa.c2s.ums.domain.RoleRepository;
import gov.samhsa.c2s.ums.domain.Scope;
import gov.samhsa.c2s.ums.domain.ScopeRepository;
import gov.samhsa.c2s.ums.domain.Telecom;
import gov.samhsa.c2s.ums.domain.User;
import gov.samhsa.c2s.ums.domain.UserActivation;
import gov.samhsa.c2s.ums.domain.UserActivationRepository;
import gov.samhsa.c2s.ums.domain.UserRepository;
import gov.samhsa.c2s.ums.domain.UserScopeAssignment;
import gov.samhsa.c2s.ums.domain.UserScopeAssignmentRepository;
import gov.samhsa.c2s.ums.domain.reference.AdministrativeGenderCode;
import gov.samhsa.c2s.ums.infrastructure.EmailSender;
import gov.samhsa.c2s.ums.infrastructure.ScimService;
import gov.samhsa.c2s.ums.service.dto.ScopeAssignmentRequestDto;
import gov.samhsa.c2s.ums.service.dto.UserActivationRequestDto;
import gov.samhsa.c2s.ums.service.dto.UserActivationResponseDto;
import gov.samhsa.c2s.ums.service.dto.UserVerificationRequestDto;
import gov.samhsa.c2s.ums.service.dto.VerificationResponseDto;
import gov.samhsa.c2s.ums.service.exception.PasswordConfirmationFailedException;
import gov.samhsa.c2s.ums.service.exception.UserActivationNotFoundException;
import gov.samhsa.c2s.ums.service.exception.UserIsAlreadyVerifiedException;
import org.cloudfoundry.identity.uaa.scim.ScimUser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import java.math.BigInteger;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static gov.samhsa.c2s.common.unit.matcher.ArgumentMatchers.matching;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.ExampleMatcher.matching;

@RunWith(MockitoJUnitRunner.class)
public class UserActivationServiceImplTest {

    Long userId=30L;
    String xForwardedProto="xForwardedProto";
    String xForwardedHost="xForwardedHost";
    int xForwardedPort=344;

    @Mock
    ModelMapper modelMapper;

    @Mock
    TokenGenerator tokenGenerator;

    @Mock
    UserActivationRepository userActivationRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    EmailTokenGenerator emailTokenGenerator;

    @Mock
    EmailSender emailSender;

    @Mock
    ScimService scimService;

    @Mock
    ScopeRepository scopeRepository;

    @Mock
    EmailSenderProperties emailSenderProperties;

    @Mock
    UserScopeAssignmentRepository userScopeAssignmentRepository;


    @InjectMocks
    UserActivationServiceImpl userActivationService=new UserActivationServiceImpl();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testInitiateUserActivation(){
        //Arrange
        String verificationCode="EGHR23";
        String emailToken="token";
        User user=mock(User.class);
        UserActivationResponseDto response=mock(UserActivationResponseDto.class);
        Instant i=Instant.now().plus(Period.ofDays(7));
        LocalDate localDate=LocalDate.now();
        Telecom telecom=mock(Telecom.class);

        Telecom telecom1=mock(Telecom.class);
        Telecom telecom2=mock(Telecom.class);

        List<Telecom> telecoms=new ArrayList<>();
        telecoms.add(telecom);
        telecoms.add(telecom1);
        telecoms.add(telecom2);

        when(userRepository.getOne(userId)).thenReturn(user);

        UserActivation userActivation=mock(UserActivation.class);

        when(emailTokenGenerator.generateEmailToken()).thenReturn(emailToken);
        when(emailSenderProperties.getEmailTokenExpirationInDays()).thenReturn(7);

        when(userActivationRepository.findOneByUserId(userId)).thenReturn(Optional.ofNullable(userActivation));
        when(userActivation.isVerified()).thenReturn(false);

        when(tokenGenerator.generateToken(7)).thenReturn("3234");

        when(userActivationRepository.save(userActivation)).thenReturn(userActivation);


        when(modelMapper.map(user,UserActivationResponseDto.class)).thenReturn(response);

        when(user.getBirthDay()).thenReturn(localDate);

        when(userActivation.getVerificationCode()).thenReturn(verificationCode);
        when(userActivation.getEmailTokenExpirationAsInstant()).thenReturn(i);

        when(telecom.getSystem()).thenReturn("email");

        when(telecom.getValue()).thenReturn("email");

        when(user.getTelecoms()).thenReturn(telecoms);

        AdministrativeGenderCode administrativeGenderCode=mock(AdministrativeGenderCode.class);
        when(user.getAdministrativeGenderCode()).thenReturn(administrativeGenderCode);

        when(administrativeGenderCode.getCode()).thenReturn("code");

        //Act

        UserActivationResponseDto userActivationResponseDto=userActivationService.initiateUserActivation(userId,xForwardedProto,xForwardedHost,xForwardedPort);

        //Assert
        assertEquals(response,userActivationResponseDto);
    }


    @Test
    public void testInitiateUserActivation_whenUserIsAlreadyVerified_throwsException() throws Exception{

        //Arrange
        thrown.expect(UserIsAlreadyVerifiedException.class);
        String verificationCode="EGHR23";
        String emailToken="token";
        User user=mock(User.class);
        UserActivationResponseDto response=mock(UserActivationResponseDto.class);
        Instant i=Instant.now().plus(Period.ofDays(7));
        LocalDate localDate=LocalDate.now();
        Telecom telecom=mock(Telecom.class);

        Telecom telecom1=mock(Telecom.class);
        Telecom telecom2=mock(Telecom.class);

        List<Telecom> telecoms=new ArrayList<>();
        telecoms.add(telecom);
        telecoms.add(telecom1);
        telecoms.add(telecom2);

        when(userRepository.getOne(userId)).thenReturn(user);

        UserActivation userActivation=mock(UserActivation.class);

        when(emailTokenGenerator.generateEmailToken()).thenReturn(emailToken);
        when(emailSenderProperties.getEmailTokenExpirationInDays()).thenReturn(7);

        when(userActivationRepository.findOneByUserId(userId)).thenReturn(Optional.ofNullable(userActivation));
        when(userActivation.isVerified()).thenReturn(true);

        //Act
        UserActivationResponseDto userActivationResponseDto=userActivationService.initiateUserActivation(userId,xForwardedProto,xForwardedHost,xForwardedPort);

        //Assert
        assertNull(userActivationResponseDto);
    }


    @Test
    public void testVerify_whenVerificationcodeAndBirthDate_IsPresent(){

        //Arrange
        UserVerificationRequestDto userVerificationRequest=mock(UserVerificationRequestDto.class);
        final String emailToken="token";
        final String verificationCode="verificationCode";
        final LocalDate birthDate=LocalDate.MIN;
        User user=mock(User.class);
        long userId=30L;
        String userIdS=String.valueOf(userId);
        Instant expire=Instant.MAX;
        final String verificationCodeNullSafe="verificationCode";


        UserActivation userActivation=mock(UserActivation.class);

        when(userVerificationRequest.getEmailToken()).thenReturn(emailToken);
        when(userVerificationRequest.getVerificationCode()).thenReturn(String.valueOf(verificationCode));
        when(userVerificationRequest.getBirthDate()).thenReturn(birthDate);

        when(userActivationRepository.findOneByEmailToken(emailToken)).thenReturn(Optional.ofNullable(userActivation));
        when(userActivation.getUser()).thenReturn(user);
        when(userActivationRepository.findOneByEmailTokenAndVerificationCode(emailToken,verificationCodeNullSafe)).thenReturn(Optional.ofNullable(userActivation));
        when(userActivation.getEmailTokenExpirationAsInstant()).thenReturn(expire);
        when(userActivation.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(user.getBirthDay()).thenReturn(birthDate);

        VerificationResponseDto verificationResponseDto=new VerificationResponseDto(true,userIdS);
        VerificationResponseDto verificationResponseDto2=userActivationService.verify(userVerificationRequest);

        assertEquals(verificationResponseDto,verificationResponseDto2);
    }


    @Test
    public void testVerify_whenVerificationCodeAndBirthDate_IsNotPresent(){
        //Arrange
        UserVerificationRequestDto userVerificationRequest=mock(UserVerificationRequestDto.class);
        final String emailToken="token";
        final String verificationCode=null;
        final LocalDate birthDate=null;
        User user=mock(User.class);
        long userId=30L;
        String userIdS=String.valueOf(userId);
        Instant expire=Instant.MAX;

        UserActivation userActivation=mock(UserActivation.class);
        when(userVerificationRequest.getEmailToken()).thenReturn(emailToken);
        when(userVerificationRequest.getVerificationCode()).thenReturn(verificationCode);
        when(userVerificationRequest.getBirthDate()).thenReturn(birthDate);

        when(userActivationRepository.findOneByEmailToken(emailToken)).thenReturn(Optional.ofNullable(userActivation));
        when(userActivation.isVerified()).thenReturn(false);
        when(userActivation.getEmailTokenExpirationAsInstant()).thenReturn(expire);

        //Act
        VerificationResponseDto verificationResponseDto=new VerificationResponseDto(true);
        VerificationResponseDto verificationResponseDto1=userActivationService.verify(userVerificationRequest);

        //assert
        assertEquals(verificationResponseDto,verificationResponseDto1);

    }

    @Test
    public void testFindUserActivationInfoByUserId_whenUserActivationRecordFound(){

        //Arrange
        User user=mock(User.class);
        UserActivation userActivation=mock(UserActivation.class);
        LocalDate birthday=LocalDate.MIN;
        final String verificationCode="verificationCode";
        Instant tokenExpiration=Instant.MIN;
        List<Telecom> telecoms=new ArrayList<>();
        Telecom telecom=mock(Telecom.class);
        Telecom telecom2=mock(Telecom.class);
        telecoms.add(telecom);
        telecoms.add(telecom2);
        String system="email";
        String value="value";

        when(userRepository.findOne(userId)).thenReturn(user);
        when(userActivationRepository.findOneByUserId(userId)).thenReturn(Optional.ofNullable(userActivation));
        UserActivationResponseDto response=mock(UserActivationResponseDto.class);
        when(modelMapper.map(user, UserActivationResponseDto.class)).thenReturn(response);

        when(user.getBirthDay()).thenReturn(birthday);
        when(userActivation.getVerificationCode()).thenReturn(verificationCode);
        when(userActivation.getEmailTokenExpirationAsInstant()).thenReturn(tokenExpiration);

        when(user.getTelecoms()).thenReturn(telecoms);

        when(telecom.getSystem()).thenReturn(system);
        when(telecom.getValue()).thenReturn(value);

        when(userActivation.isVerified()).thenReturn(true);


        //Act
        UserActivationResponseDto userActivationResponseDto=userActivationService.findUserActivationInfoByUserId(userId);

        //Assert
        assertEquals(response,userActivationResponseDto);

    }



    @Test
    public void testFindUserActivationInfByUserId_whenUserActivationRecord_NotFound() throws Exception{
        //Arrange
        thrown.expect(UserActivationNotFoundException.class);
        thrown.expectMessage("No user activation record found for user id: " + userId);

        when(userActivationRepository.findOneByUserId(userId)).thenReturn(Optional.empty());

        //Act
        UserActivationResponseDto userActivationResponseDto= userActivationService.findUserActivationInfoByUserId(userId);

        //Assert
        assertNull(userActivationResponseDto);
    }

}