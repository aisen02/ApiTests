package tests.swaggertests;

import assertions.Conditions;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import models.swagger.FullUser;
import models.swagger.Info;
import models.swagger.JwtAuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import services.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static assertions.Conditions.hasMessage;
import static assertions.Conditions.hasStatusCode;
import static io.restassured.RestAssured.given;

public class UserNewTests {

    private static Random random;
    private static UserService userService;

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "http://85.192.34.140:8080";
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        random = new Random();
        userService = new UserService();
    }

    private FullUser getRandomUser() {
        int rundomNumber = Math.abs(random.nextInt(100000));
        return FullUser.builder()
                .login("aisen"+rundomNumber)
                .pass("aisimem002").build();
    }

    private FullUser getAdminUser() {
        return FullUser.builder()
                .pass("admin")
                .login("admin").build();
    }

    @Test
    public void positiveRegisterTest(){

        FullUser user = getRandomUser();

        userService.register(user)
                .should(hasMessage("User created"))
                .should(hasStatusCode(201));

    }

    @Test
    public void negativeRegisterLoginExistsTest(){

        FullUser user = getRandomUser();

        userService.register(user);

        userService.register(user)
                .should(hasStatusCode(400))
                .should(hasMessage("Login already exist"));

    }

    @Test
    public void negativeRegisterNoPasswordTest(){

        FullUser user = getRandomUser();
        user.setPass(null);

        userService.register(user)
                .should(hasStatusCode(400))
                .should(hasMessage("Missing login or password"));

    }

    @Test
    public void positiveAdminAuthTest(){

        FullUser user = getAdminUser();

        String token = userService.auth(user)
                .should(hasStatusCode(200))
                .asJwt();

        Assertions.assertNotNull(token);

    }

    @Test
    public void positiveNewUserAuthTest(){

        FullUser user = getRandomUser();
        userService.register(user)
                .should(hasStatusCode(201))
                .should(hasMessage("User created"));

        String token = userService.auth(user)
                .should(hasStatusCode(200))
                .asJwt();

        Assertions.assertNotNull(token);

    }

    @Test
    public void negativeAuthTest(){
        FullUser user = getAdminUser();
        user.setPass("user.getPass()");
        user.setLogin("user.getLogin()");

        userService.auth(user)
                .should(hasStatusCode(401));

    }

    @Test
    public void positiveGetUserInfoTest(){

        FullUser user = getAdminUser();

        String token = userService.auth(user)
                .should(hasStatusCode(200))
                .asJwt();

        Assertions.assertNotNull(token);

        userService.getUserInfo(token)
                        .should(hasStatusCode(200));
    }

    @Test
    public void negativeGetUserInfoInvalidJwtTest(){

        userService.getUserInfo("asfafa")
                .should(hasStatusCode(401));

    }

    @Test
    public void negativeGetUserInfoWithoutJwtTest(){
        given()
                .get("/api/user")
                .then().statusCode(401);
    }

    @Test
    public void positiveChangeUserPasswordTest(){
        FullUser user = getRandomUser();
        String oldPassword = user.getPass();

        userService.register(user)
                .should(hasStatusCode(201))
                .should(hasMessage("User created"));

        String token = userService.auth(user)
                .should(hasStatusCode(200))
                .asJwt();

        String newPassword = "UpPAssBay";

        userService.updatePass(newPassword, token)
                .should(hasStatusCode(200))
                .should(hasMessage("User password successfully changed"));


        user.setPass(newPassword);
        token = userService.auth(user)
                        .should(hasStatusCode(200)).asJwt();

        FullUser userUpdate = userService.getUserInfo(token)
                        .should(hasStatusCode(200)).as(FullUser.class);

        Assertions.assertNotEquals(userUpdate.getPass(), oldPassword);
    }

    @Test
    public void negativeChangeAdminPasswordTest(){

        FullUser user = getAdminUser();

        String token = userService.auth(user)
                .should(hasStatusCode(200)).asJwt();

        Assertions.assertNotNull(token);

        String updatedPassValue = "newPassUpdated";

        userService.updatePass(updatedPassValue, token)
                .should(hasStatusCode(400))
                .should(hasMessage("Cant update base users"));


    }

    @Test
    public void negativeDeleteAdminTest(){
        FullUser user = getAdminUser();
        String token = userService.auth(user)
                .should(hasStatusCode(200))
                .asJwt();

        Assertions.assertNotNull(token);

        userService.deleteUser(token)
                .should(hasStatusCode(400))
                .should(hasMessage("Cant delete base users"));

    }

    @Test
    public void positiveDeleteNewUserTest(){
        FullUser user = getRandomUser();
        userService.register(user)
                .should(hasStatusCode(201))
                .should(hasMessage("User created"));

        String token = userService.auth(user).should(hasStatusCode(200))
                .asJwt();

        Assertions.assertNotNull(token);

        userService.deleteUser(token)
                .should(hasStatusCode(200))
                .should(hasMessage("User successfully deleted"));

    }

    @Test
    public void positiveGetAllUserTest(){
        List<String> users = userService.getAllUsers()
                .should(hasStatusCode(200))
                .asList(String.class);

        Assertions.assertTrue(users.size()>=3);
    }
}
